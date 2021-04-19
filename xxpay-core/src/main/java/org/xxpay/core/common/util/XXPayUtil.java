package org.xxpay.core.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.vo.OrderCostFeeVO;
import org.xxpay.core.entity.MchAgentpayPassage;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.text.ParseException;
import java.util.*;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 支付工具类
 * @date 2017-07-05
 * @Copyright: www.xxpay.org
 */
public class XXPayUtil {

    private static final MyLog _log = MyLog.getLog(XXPayUtil.class);

    public static final BigDecimal MIN_SERVICE_CHARGE = new BigDecimal(400); //最低手续费
    public static final BigDecimal AMOUNT_INCREASE_BASENUM = new BigDecimal(1000400); //手续费倍增基数

    public static Map<String, Object> makeRetMap(String retCode, String retMsg, String resCode, String errCode, String errDesc) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (retCode != null) retMap.put(PayConstant.RETURN_PARAM_RETCODE, retCode);
        if (retMsg != null) retMap.put(PayConstant.RETURN_PARAM_RETMSG, retMsg);
        if (resCode != null) retMap.put(PayConstant.RESULT_PARAM_RESCODE, resCode);
        if (errCode != null) retMap.put(PayConstant.RESULT_PARAM_ERRCODE, errCode);
        if (errDesc != null) retMap.put(PayConstant.RESULT_PARAM_ERRDES, errDesc);
        return retMap;
    }

    public static Map<String, Object> makeRetMap(String retCode, String retMsg, String resCode, PayEnum payEnum) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (retCode != null) retMap.put(PayConstant.RETURN_PARAM_RETCODE, retCode);
        if (retMsg != null) retMap.put(PayConstant.RETURN_PARAM_RETMSG, retMsg);
        if (resCode != null) retMap.put(PayConstant.RESULT_PARAM_RESCODE, resCode);
        if (payEnum != null) {
            retMap.put(PayConstant.RESULT_PARAM_ERRCODE, payEnum.getCode());
            retMap.put(PayConstant.RESULT_PARAM_ERRDES, payEnum.getMessage());
        }
        return retMap;
    }

    public static String makeRetData(Map retMap, String resKey) {
        if (PayConstant.RETURN_VALUE_SUCCESS.equals(retMap.get(PayConstant.RETURN_PARAM_RETCODE))) {
            String sign = PayDigestUtil.getSign(retMap, resKey);
            retMap.put(PayConstant.RESULT_PARAM_SIGN, sign);
        }
        _log.info("生成响应数据:{}", retMap);
        return JSON.toJSONString(retMap);
    }

    public static String makeRetData(JSONObject retObj, String resKey) {
        if (PayConstant.RETURN_VALUE_SUCCESS.equals(retObj.get(PayConstant.RETURN_PARAM_RETCODE))) {
            String sign = PayDigestUtil.getSign(retObj, resKey);
            retObj.put(PayConstant.RESULT_PARAM_SIGN, sign);
        }

        _log.info("生成响应数据:{}", retObj);
        return JSON.toJSONString(retObj);
    }

    public static String makeRetFail(Map retMap) {
        _log.info("生成响应数据:{}", retMap);
        return JSON.toJSONString(retMap);
    }

    /**
     * 验证支付中心签名
     *
     * @param params
     * @return
     */
    public static boolean verifyPaySign(Map<String, Object> params, String key) {
        String sign = (String) params.get("sign"); // 签名
        params.remove("sign");    // 不参与签名
        String checkSign = PayDigestUtil.getSign(params, key);
        if (!checkSign.equalsIgnoreCase(sign)) {
            return false;
        }
        return true;
    }


    /**
     * http请求 form表单提交
     *
     * @param url
     * @param params
     * @return
     */
    public static String http(String url, Map<String, String> params) {
        URL u = null;
        HttpURLConnection con = null;
        // 构建请求参数
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("&");
            }
            sb.substring(0, sb.length() - 1);
        }
        System.out.println("send_url:" + url);
        System.out.println("send_data:" + sb.toString());
        // 尝试发送请求
        try {
            u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            //// POST 只能为大写，严格限制，post会不识别
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            osw.write(sb.toString());
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        // 读取返回内容
        StringBuffer buffer = new StringBuffer();
        try {
            //一定要有返回值，否则无法把请求发送给server端。
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                buffer.append(temp);
                buffer.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    /**
     * 验证VV平台支付中心签名
     *
     * @param params
     * @return
     */
    public static boolean verifyPaySign(Map<String, Object> params, String key, String... noSigns) {
        String sign = (String) params.get("sign"); // 签名
        params.remove("sign");    // 不参与签名
        if (noSigns != null && noSigns.length > 0) {
            for (String noSign : noSigns) {
                params.remove(noSign);
            }
        }
        String checkSign = PayDigestUtil.getSign(params, key);
        if (!checkSign.equalsIgnoreCase(sign)) {
            return false;
        }
        return true;
    }

    public static String genUrlParams(Map<String, Object> paraMap) {
        if (paraMap == null || paraMap.isEmpty()) return "";
        StringBuffer urlParam = new StringBuffer();
        Set<String> keySet = paraMap.keySet();
        int i = 0;
        for (String key : keySet) {
            urlParam.append(key).append("=");
            if (paraMap.get(key) instanceof String) {
                urlParam.append(URLEncoder.encode((String) paraMap.get(key)));
            } else {
                urlParam.append(paraMap.get(key));
            }
            if (++i == keySet.size()) break;
            urlParam.append("&");
        }
        return urlParam.toString();
    }

    public static String genUrlParams2(Map<String, String> paraMap) {
        if (paraMap == null || paraMap.isEmpty()) return "";
        StringBuffer urlParam = new StringBuffer();
        Set<String> keySet = paraMap.keySet();
        int i = 0;
        for (String key : keySet) {
            urlParam.append(key).append("=").append(paraMap.get(key));
            if (++i == keySet.size()) break;
            urlParam.append("&");
        }
        return urlParam.toString();
    }

    /**
     * 发起HTTP/HTTPS请求(method=POST)
     *
     * @param url
     * @return
     */
    public static String call4Post(String url) {
        try {
            URL url1 = new URL(url);
            if ("https".equals(url1.getProtocol())) {
                return HttpClient.callHttpsPost(url);
            } else if ("http".equals(url1.getProtocol())) {
                return HttpClient.callHttpPost(url);
            } else {
                return "";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断返回结果是否成功
     *
     * @param retMap
     * @return
     */
    public static Boolean isSuccess(Map retMap) {
        if (retMap == null) return false;
        if (retMap.get("retCode") == null) return false;
        return "SUCCESS".equalsIgnoreCase(retMap.get("retCode").toString());
    }

    /**
     * 根据费率类型和金额,返回费用(type )
     *
     * @param type     费用类型 1:费率,2:固定金额
     * @param feeRate  费率
     * @param feeEvery 每笔金额
     * @param amount   需要算的金额
     * @return
     */
    public static Long getFee(Byte type, BigDecimal feeRate, Long feeEvery, Long amount) {
        Long fee = 0l;
        if (type == 1) {
            fee = XXPayUtil.calOrderMultiplyRate(amount, feeRate);
        } else if (type == 2) {
            fee = feeEvery;
        }
        return fee;
    }

    /**
     * 判断IP是否允许
     *
     * @param ip
     * @param whiteIps
     * @param blackIps
     * @return
     */
    public static Boolean ipAllow(String ip, String whiteIps, String blackIps) {
        if (StringUtils.isBlank(ip)) {
            return true;
        }
        String[] whiteIp_s = {};
        if (StringUtils.isNotBlank(whiteIps)) {
            whiteIp_s = whiteIps.split(",");
        }
        String[] blackIp_s = {};
        if (StringUtils.isNotBlank(blackIps)) {
            blackIp_s = blackIps.split(",");
        }
        // 白名单为空,黑名单为空
        if (whiteIp_s.length == 0 && blackIp_s.length == 0) {
            return true;
        }
        // 白名单为空,黑名单不为空
        if (whiteIp_s.length == 0 && blackIp_s.length > 0) {
            return !contain(blackIp_s, ip);
        }
        // 白名单不为空,黑名单为空
        if (whiteIp_s.length > 0 && blackIp_s.length == 0) {
            return contain(whiteIp_s, ip);
        }
        // 白名单不为空,黑名单不为空
        if (whiteIp_s.length > 0 && blackIp_s.length > 0) {
            if (contain(blackIp_s, ip)) {    // 如果在黑名单,则返回false
                return false;
            }
            return contain(whiteIp_s, ip);
        }
        return false;
    }

    /**
     * 判断IP是否允许(强校验)
     * 1. 必须在白名单中
     * 2. 如果在黑名单,则白名单中失效
     *
     * @param ip
     * @param whiteIps
     * @param blackIps
     * @return
     */
    public static Boolean ipAllow4Strong(String ip, String whiteIps, String blackIps) {
        // 没有IP则返回false
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String[] whiteIp_s = {};
        if (StringUtils.isNotBlank(whiteIps)) {
            whiteIp_s = whiteIps.split(",");
        }
        String[] blackIp_s = {};
        if (StringUtils.isNotBlank(blackIps)) {
            blackIp_s = blackIps.split(",");
        }
        // 白名单为空,返回false
        if (whiteIp_s.length == 0) {
            return false;
        }
        // 如果不在白名单,返回false
        if (!contain(whiteIp_s, ip)) {
            return false;
        }
        // 如果黑名单不为空,则判断是否在黑名单中
        if (blackIp_s.length > 0) {
            return !contain(blackIp_s, ip);
        }
        return true;
    }

    /**
     * 判断是否包含IP
     *
     * @param ips ip数组
     * @param ip  ip地址
     * @return
     */
    public static boolean contain(String[] ips, String ip) {
        if (ips == null || ips.length == 0) return false;
        if (StringUtils.isBlank(ip)) return false;
        for (String p : ips) {
            if (p.equals(ip)) return true;
        }
        return false;
    }

    /**
     * 判断是否包含IP
     *
     * @param ips 使用半角逗号分隔的ip
     * @param ip  ip地址
     * @return
     */
    public static boolean contain(String ips, String ip) {
        if (StringUtils.isBlank(ips)) return false;
        String[] ip_s = {};
        if (StringUtils.isNotBlank(ips)) {
            ip_s = ips.split(",");
        }
        return contain(ip_s, ip);
    }

    /**
     * <p><b>Description: </b>计算订单的分润情况 和 各种费用
     * <p>2018年9月20日 下午4:13:47
     *
     * @param amount          订单金额  （保持与数据库的格式一致 ，单位：分）
     * @param channelRate     通道费率   （保持与数据库的格式一致 ，百分比之前的数字，如费率为0.55%，则传入 0.55）
     * @param agentRate       代理商设置费率，说明同上，  如果为null  说明商家没有代理商
     * @param parentAgentRate 一级代理商设置费率，说明同上
     * @param mchRate         商家设置费率，说明同上
     * @return
     * @author matf
     */
    public static OrderCostFeeVO calOrderCostFeeAndIncome(Long amount, BigDecimal channelRate, BigDecimal agentRate, BigDecimal parentAgentRate, BigDecimal mchRate) {

        //通道手续费（上游成本）
        Long channelCostFee = calOrderMultiplyRate(amount, channelRate);

        //一级代理商成本费用   即  ：一级代理商需要支付给平台的费用
        Long parentAgentCostFee = 0L; //当该二级代理不存在一级代理商时 一级代理商费用为0
        if (parentAgentRate != null) {
            parentAgentCostFee = calOrderMultiplyRate(amount, parentAgentRate);
        }

        //二级代理商成本费用   即  ：二级代理商需要支付给平台或一级代理的费用
        Long agentCostFee = 0L; //当该二级代理不存在一级代理商时 二级代理商费用为0
        if (agentRate != null) {
            if (agentRate.compareTo(BigDecimal.ZERO) == 1) {
                agentCostFee = calOrderMultiplyRate(amount, agentRate);
            }
        }

        //商家成本 订单金额*商户费率
        Long mchCostFee = calOrderMultiplyRate(amount, mchRate);

        //一级代理商利润 ： (二级代理商费用 - 一级代理商费用) 或者  0
        Long parentAgentProfit = parentAgentRate != null ? agentCostFee - parentAgentCostFee : 0L;

        //代理费率-一级代理费率-二级代理费率（新）
        Long agentProfit = agentCostFee;

        //商户入账金额 订单金额-商户成功
        Long mchIncome = amount - mchCostFee;

        //平台利润 商户成本-通道成本-代理费率
        Long platProfit = mchCostFee - channelCostFee - (agentCostFee + parentAgentCostFee);

        //计算结果不允许出现负值
        if (agentProfit < 0) {
            _log.warn("[代理商&商户]费率设置异常:agentProfit={}, amount={}, channelRate={}, agentRate={}, parentAgentRate={}, mchRate={}", agentProfit, amount, channelRate, agentRate, parentAgentRate, mchRate);
            agentProfit = 0L;
        }

        if (parentAgentProfit < 0) {
            _log.warn("[一级代理&二级代理]费率设置异常:agentProfit={}, amount={}, channelRate={}, agentRate={}, parentAgentRate={}, mchRate={}", agentProfit, amount, channelRate, agentRate, parentAgentRate, mchRate);
            parentAgentProfit = 0L;
        }

        if (platProfit < 0) {
            _log.warn("[代理商&通道]费率设置异常:platProfit={}, amount={}, channelRate={}, agentRate={}, parentAgentRate={}, mchRate={}", platProfit, amount, channelRate, agentRate, parentAgentRate, mchRate);
            platProfit = 0L;
        }

        return new OrderCostFeeVO(channelCostFee, agentCostFee, parentAgentCostFee, mchCostFee, platProfit, agentProfit, parentAgentProfit, mchIncome);

    }

    /**
     * <p><b>Description: </b>计算订单的各种费用  （订单金额 * 费率  结果四舍五入并保留0位小数 ）
     * 适用于计算
     * <p>2018年9月20日 下午2:16:34
     *
     * @param amount 订单金额  （保持与数据库的格式一致 ，单位：分）
     * @param rate   费率   （保持与数据库的格式一致 ，百分比之前的数字，如费率为0.55%，则传入 0.55）
     * @return
     * @author matf
     */
    public static Long calOrderMultiplyRate(Long amount, BigDecimal rate) {
        //费率还原 回真实数值即/100, 并乘以订单金额   结果四舍五入并保留0位小数
        return new BigDecimal(amount).multiply(rate).divide(BigDecimal.valueOf(100), 6, BigDecimal.ROUND_HALF_UP).longValue();

    }


    public static Long calOrderMultiplyRateDown(Long amount, BigDecimal rate) {
        //费率还原 回真实数值即/100, 并乘以订单金额   结果四舍五入并保留0位小数
        return new BigDecimal(amount).multiply(rate).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_DOWN).longValue();

    }

    //post 请求
    public static String doPostQueryCmd(String strURL, String req) {
        String result = null;


        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(strURL);
            URLConnection con = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection) con;
            httpUrlConnection.setRequestMethod("POST");
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes("utf-8");
            out.write(outBuf);
            out.close();

            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String data = null;

            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            _log.info("res:" + sb.toString());
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info("res:" + ex.getStackTrace() + ex.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    _log.info("res:" + e.getStackTrace() + e.getMessage());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    _log.info("res:" + e.getStackTrace() + e.getMessage());
                }
            }
        }
        if (result == null)
            return "";
        else
            return result;
    }


    public static String doPostQueryCmd(String strURL, String req, Integer timeOut) {
        String result = null;


        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(strURL);
            URLConnection con = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection) con;
            httpUrlConnection.setRequestMethod("POST");
            con.setUseCaches(false);
            con.setConnectTimeout(timeOut);
            con.setDoInput(true);
            con.setDoOutput(true);

            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes("utf-8");
            out.write(outBuf);
            out.close();

            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String data = null;

            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            _log.info("res:" + sb.toString());
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null)
            return "";
        else
            return result;
    }

    public static String doPostQueryCmd(String strURL, String req, String charset, String contentType) {
        String result = null;
        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(strURL);
            URLConnection con = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection) con;
            httpUrlConnection.setRequestMethod("POST");
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", contentType); // 设置发送数据的格式
            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes("utf-8");
            out.write(outBuf);
            out.close();

            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String data = null;

            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            _log.info("res:" + sb.toString());
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null)
            return "";
        else
            return result;
    }


    public static String doPostQueryCmd(String strURL, String req, String charset) {
        String result = null;
        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(strURL);
            URLConnection con = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection) con;
            httpUrlConnection.setRequestMethod("POST");
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes(charset);
            out.write(outBuf);
            out.close();

            in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
            StringBuffer sb = new StringBuffer();
            String data = null;

            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            _log.info("res:" + sb.toString());
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null)
            return "";
        else
            return result;
    }

    public static String doPostQueryCmd(String strURL, String req, Map<String, String> headers) {
        String result = null;
        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(strURL);
            URLConnection con = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection) con;
            httpUrlConnection.setRequestMethod("POST");
            // 设置连接主机服务器的超时时间：15000毫秒
            Iterator<Map.Entry<String, String>> entries = headers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println(key + ":" + value);
                httpUrlConnection.setRequestProperty(key, value);
            }

            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes("utf-8");
            out.write(outBuf);
            out.close();


            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String data = null;

            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            _log.info("res:" + sb.toString());
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null)
            return "";
        else
            return result;
    }

    public static String doGetQueryCmd(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();// 关闭远程连接
        }

        return result;
    }

    public static String doGet(String URL) {
        HttpURLConnection conn = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            //创建远程url连接对象
            URL url = new URL(URL);
            //通过远程url连接对象打开一个连接，强转成HTTPURLConnection类
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Accept", "application/json");
            //发送请求
            conn.connect();
            //通过conn取得输入流，并使用Reader读取
            if (200 == conn.getResponseCode()) {
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                    System.out.println(line);
                }
            } else {
                System.out.println("ResponseCode is an error code:" + conn.getResponseCode());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            conn.disconnect();
        }
        return result.toString();
    }

    /**
     * 发送HttpPost请求
     *
     * @param strURL 服务地址
     * @param params json字符串,例如: "{ \"id\":\"12345\" }" ;其中属性名必须带双引号<br/>
     * @return 成功:返回json字符串<br/>
     */
    public static String postJson(String strURL, String params) {
        BufferedReader reader = null;
        try {
            URL url = new URL(strURL);// 创建连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式
            // connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            //一定要用BufferedReader 来接收响应， 使用字节来接收响应的方法是接收不到内容的
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(params);
            out.flush();
            out.close();
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            String res = "";
            while ((line = reader.readLine()) != null) {
                res += line;
            }
            reader.close();
            return res;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "error"; // 自定义错误信息
    }

    //map 转string 以$拼接
    public static String mapToString(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key).toString();
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        result = sb.toString().substring(0, sb.length() - 1);
        return result;
    }
    //map 转string 以$拼接
    public static String mapToStringO(Map<String, Object> params) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() + "=>" + PayDigestUtil.getSortJson((JSONObject) entry.getValue()) + "&");
                } else {
                    list.add(entry.getKey() + "=>" + entry.getValue() + "&");
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result = result.substring(0, result.length() - 1);
        return result;
    }

    //map 转string 以$拼接
    public static String mapToStringByURLcode(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key).toString();
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + "=" + URLEncoder.encode(value, "utf-8") + "&");
        }
        result = sb.toString().substring(0, sb.length() - 1);
        return result;
    }

    public static String mapToStringEmity(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
//            if (value == null || value.equals("")) {
//                continue;
//            }
            sb.append(key + "=" + value + "&");
        }
        result = sb.toString().substring(0, sb.length() - 1);
        return result;
    }

    //map 转string 以$拼接
    public static String mapToStringByObj(Map<String, Object> params) {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            if (params.get(key) == null) {
                continue;
            }
            String value = params.get(key).toString();
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        result = sb.toString().substring(0, sb.length() - 1);
        return result;
    }


    public static Map<String, String> JSONObjectToMap(JSONObject jsondata) {
        //map对象
        Map<String, String> data = new HashMap<>();
        //循环转换
        Iterator it = jsondata.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            data.put(entry.getKey(), entry.getValue());
        }

        return data;
    }

    public static Map<String, Object> JSONObjectToMap2(JSONObject jsondata) {
        //map对象
        Map<String, Object> data = new HashMap<>();
        //循环转换
        Iterator it = jsondata.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
            data.put(entry.getKey(), entry.getValue());
        }

        return data;
    }

    public static SortedMap<String, String> JSONObjectToSortedMap(JSONObject jsondata) {
        //map对象
        SortedMap<String, String> data = new TreeMap<>();
        //循环转换
        Iterator it = jsondata.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            data.put(entry.getKey(), entry.getValue());
        }

        return data;
    }

    /**
     * 处理URL参数串，把参数名和参数值转化成键值对的形式
     */
    public static Map<String, String> convertParamsString2Map(String paramsString) {
        //为了避免base64 里面的=号干扰预先把== 转换为##
        paramsString = paramsString.replace("==", "##");
        paramsString = paramsString.replace("=\"", "#\"");
        Map<String, String> paramsMap = new HashMap<String, String>();
        String[] tempParams = paramsString.split("&");
        String name = null;
        String value = null;
        if (tempParams != null && tempParams.length > 0) {
            for (int i = 0; i < tempParams.length; i++) {
                String[] tempArray = tempParams[i].split("=");
                if (tempArray.length == 2) {
                    name = tempArray[0];
                    value = tempArray[1];
                } else {
                    if (tempArray.length != 1)
                        continue;
                    else {
                        name = tempArray[0];
                        value = "";
                    }
                }
                if (name != "") {
                    value = value.replace("##", "==");
                    value = value.replace("#\"", "=\"");
                    paramsMap.put(name, value);
                }
            }
        }
        return paramsMap;
    }


    //map 转string 以$拼接
    public static String mapToStringValue(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + value);
        }
        result = sb.toString();
        return result;
    }


    public static Map<String, String> StrConvertToMap(String str) {
        Map<String, String> map = new HashMap<>();
        String[] tempParams = str.split("&");
        for (String item : tempParams) {
            String[] i = item.split("=");
            map.put(i[0], i[1]);
        }
        return map;
    }

    /**
     * FROM表单转JSONObject
     *
     * @param formHtml
     * @return
     */
    public static JSONObject formBuildByJSONObject(String formHtml) {
        JSONObject jsonObject = new JSONObject();
        Document doc = Jsoup.parse(formHtml);
        Elements formElements = doc.getElementsByTag("form");
        Element formElement = formElements.get(0);
        String action = formElement.attr("action");
        Elements inputElements = formElement.getElementsByTag("input");
        for (Element inputElement : inputElements) {
            String name = inputElement.attr("name");
            String value = inputElement.attr("value");
            jsonObject.put(name, value);
        }
        jsonObject.put("action", action);
        return jsonObject;
    }

    /**
     * FROM表单转map
     *
     * @param formHtml
     * @return
     */
    public static Map<String, String> formBuildByMap(String formHtml) {
        Map<String, String> map = new HashMap();
        Document doc = Jsoup.parse(formHtml);
        Elements formElements = doc.getElementsByTag("form");
        Element formElement = formElements.get(0);
        String action = formElement.attr("action");
        Elements inputElements = formElement.getElementsByTag("input");
        for (Element inputElement : inputElements) {
            String name = inputElement.attr("name");
            String value = inputElement.attr("value");
            map.put(name, value);
        }
        map.put("action", action);
        return map;
    }


    /// <summary>
    /// 拼接form表单
    /// </summary>
    /// <param name="sParaTemp">键值对</param>
    /// <param name="gateway">请地址</param>
    /// <returns></returns>
    public static String buildRequestHtml(Map<String, String> sParaTemp, String gateway) {
        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<html><head></head><body>");
        sbHtml.append("<form id='submitform' action='" + gateway + "' method='post'>");
        for (String key : sParaTemp.keySet()) {
            String value = sParaTemp.get(key);
            sbHtml.append("<input type='hidden' name='" + key + "' value='" + value + "'/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type='submit' value='submit' style='display:none;'></form>");
        sbHtml.append("<script>document.forms['submitform'].submit();</script></body></html>");
        return sbHtml.toString();
    }


    /**
     * FROM表单转url
     *
     * @param formHtml
     * @return
     */
    public static String buildWapUrl(String formHtml) {
        try {
            Document doc = Jsoup.parse(formHtml);
            Elements formElements = doc.getElementsByTag("form");
            Element formElement = formElements.get(0);
            String action = formElement.attr("action");
            Elements inputElements = formElement.getElementsByTag("input");
            StringBuilder stringBuilder = new StringBuilder();
            for (Element inputElement : inputElements) {
                String name = inputElement.attr("name");
                String value = inputElement.attr("value");
                if (value.contains("http")) {
                    value = URLEncoder.encode(value, "utf-8");
                }
                stringBuilder.append(name + "=" + value + "&");
            }
            return action + "?" + stringBuilder.toString();
        } catch (Exception ex) {
            return formHtml;
        }
    }


    public static String CurlPost(String[] cmds) {
        ProcessBuilder process = new ProcessBuilder(cmds);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            _log.info("res:" + builder.toString());
            return builder.toString();

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        }

        return "";
    }

    public static void main(String[] args) {

        System.out.println("a " + System.getProperty("line.separator"));

    }

}
