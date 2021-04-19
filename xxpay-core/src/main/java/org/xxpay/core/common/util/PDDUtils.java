package org.xxpay.core.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.RetEnum;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class PDDUtils {
    private static final MyLog _log = MyLog.getLog(PDDUtils.class);

    public static final String NginxList="nginx_list";


    /**
     * 登录url
     *
     * @var string
     */
    private static String login_url = "http://%s:%s/proxy/api/login?pdduid=0&hostname=://mobile.yangkeduo.com";
    /**
     * 手机验证码登录
     *
     * @param mobile 手机号
     * @param code   验证码
     * @return
     */
    public static String login(String mobile, String code,String nginxUrl,String port) {
        JSONObject json = new JSONObject();
        json.put("app_id", 5);
        json.put("mobile", mobile);
        json.put("code", code);
        return postJson(String.format(login_url,nginxUrl,port), json.toJSONString(),new HashMap<>());
    }

    /**
     * 添加收货地址url
     *
     * @var string
     */
    private static String add_address_url = "http://%s:%s/proxy/api/api/origenes/address?pdduid=%s&hostname=://mobile.yangkeduo.com";
    public static JSONObject saveAddress(Long uid, String accessToken, String json,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("AccessToken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");
        String result = postJson(String.format(add_address_url,nginxUrl,port, uid), json, map);
        JSONObject res = JSONObject.parseObject(result);
        if (res.get("error_msg") != null) {
            _log.info("新增地址 res:" + res);
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
        }
        return res;
    }
    /**
     * 添加收货地址url 批量添加的时候使用的
     *
     * @var string
     */
    public static String saveAddress1(Long uid, String accessToken, String json,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("AccessToken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        return postJson(String.format(add_address_url,nginxUrl,port, uid), json, map);
    }

    /**
     * 地址模板url
     *
     * @var string
     */
    private static String regions_url = "http://%s:%s/proxy/api/api/galen/v2/regions/%d?pdduid=%d&hostname=://mobile.yangkeduo.com";
    public static JSONObject getAddressTpl(int regionId, Long uid, String accessToken,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("AccessToken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        String result = postJson(String.format(regions_url,nginxUrl,port, regionId, uid), null, map);
        JSONObject res = JSONObject.parseObject(result);
        if (res.get("error_msg") != null) {
            _log.info("地址模板url res:" + res);
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
        }
        return res;
    }
    /**
     * 下单url
     *
     * @var string
     */
    private static String order_url = "http://%s:%s/proxy/api/order?pdduid=%d&hostname=://mobile.yangkeduo.com";
    public static JSONObject orderUrl(Long uid, String accessToken,String uin, String json,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("AccessToken",accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        map.put("Cookie","ua=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%205.1.1%3B%20zh-cn%3B%20Redmi%20Note%203%20Build%2FLMY47V)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F46.0.2490.85%20Mobile%20Safari%2F537.36%20XiaoMi%2FMiuiBrowser%2F8.1.4;" +
                " _nano_fp=XpdJn5maXpPxX0Tbn9_Zn4szkflpHDlMqN294jVe;" +
                " pdd_user_uin="+uin+"; undefined=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%205.1.1%3B%20zh-cn%3B%20Redmi%20Note%203%20Build%2FLMY47V)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F46.0.2490.85%20Mobile%20Safari%2F537.36%20XiaoMi%2FMiuiBrowser%2F8.1.4;" +
                " rec_list_personal=rec_list_personal_v4f52r; webp=1;" +
                " mlp-fresher-mix=mlp-fresher-mix_c4YDeX; msec=1800000; pdd_user_id="+uid+"; PDDAccessToken="+accessToken+"");
        String result = postJson(String.format(order_url,nginxUrl,port, uid), json, map);
        JSONObject res = JSONObject.parseObject(result);
        if (res.get("error_msg") != null) {
            _log.info("下单url报错 res:" + res);
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
        }
        return res;
    }


    /**
     * 商品详情url
     *
     * @var string
     */
    private static String goods_url = "http://%s:%s/goods.html?goods_id=%d&hostname=://mobile.yangkeduo.com";
    public static JSONObject getGoodsUrl(String url, String accessToken,String nginxUrl,String port) {
        String goods_id = "";
        if (url.contains("&")) {
            goods_id = url.substring(url.indexOf("?") + 10, url.indexOf("&"));
        } else {
            goods_id = url.substring(url.indexOf("?") + 10);
        }
        if (StringUtils.isBlank(goods_id)) {
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR, "URL不正确  请确认");
        }
        Map map = new HashMap();
        map.put("Cookie", "PDDAccessToken=" + accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        String html = doGetQueryCmd(String.format(goods_url,nginxUrl,port, Long.parseLong(goods_id)), map);
        String html1 = html.substring(html.indexOf("window.rawData=")).replace(" ", "");
        if (html1.contains("window.rawData=null")) {
            throw new ServiceException(RetEnum.RET_PDD_PHONE_EXPIRED_ERROR);
        }
        String html2 = html1.substring(html1.indexOf("{"), html1.indexOf("}};")+2);
        JSONObject jsonObject = JSONObject.parseObject(html2);
        return jsonObject;
    }


    /**
     * 支付url
     *
     * @var string
     */
    private static String repay_url = "http://%s:%s/proxy/api/order/prepay?pdduid=%d&hostname=://mobile.yangkeduo.com";
    public static JSONObject repayUrl(Long uid, String accessToken,String uin, String json,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("AccessToken",accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");
       // map.put("Accept","*/*");
        map.put("accept-language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6");
        map.put("cache-control", "max-age=0");
        map.put("sec-fetch-dest", "document");
        map.put("sec-fetch-mode", "navigate");
        map.put("sec-fetch-site", "none");
        map.put("sec-fetch-user", "?1");
        map.put("upgrade-insecure-requests", "1");
        map.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36");
        map.put("Cookie","ua=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%205.1.1%3B%20zh-cn%3B%20Redmi%20Note%203%20Build%2FLMY47V)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F46.0.2490.85%20Mobile%20Safari%2F537.36%20XiaoMi%2FMiuiBrowser%2F8.1.4;" +
                " _nano_fp=XpdJn5maXpPxX0Tbn9_Zn4szkflpHDlMqN294jVe;" +
                " pdd_user_uin="+uin+"; undefined=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%205.1.1%3B%20zh-cn%3B%20Redmi%20Note%203%20Build%2FLMY47V)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F46.0.2490.85%20Mobile%20Safari%2F537.36%20XiaoMi%2FMiuiBrowser%2F8.1.4;" +
                " rec_list_personal=rec_list_personal_v4f52r; webp=1;" +
                " mlp-fresher-mix=mlp-fresher-mix_c4YDeX; msec=1800000; pdd_user_id="+uid+"; PDDAccessToken="+accessToken+"");
        String result = postJson(String.format(repay_url,nginxUrl,port, uid), json, map);
        JSONObject res = JSONObject.parseObject(result);
        if (res.get("error_msg") != null) {
            _log.info("支付url 报错 res:" + res);
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
        }
        return res;
    }
    /**
     * 微信代付url
     *
     * @var string
     */
    private static String wepay_url = "http://%s:%s/friend_pay.html?fp_id=%s&hostname=://mobile.yangkeduo.com";
    /**
     * 订单列表url
     *
     * @var string
     */
    private static String order_list_url = "http://%s:%s/proxy/api/api/aristotle/order_list?pdduid=%d&hostname=://mobile.yangkeduo.com";
    public static JSONObject orderListUrl(String accessToken, Long uid,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("accesstoken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        //  map.put("accept-encoding", "gzip, deflate, br");
        map.put("accept-language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6");
        map.put("cache-control", "max-age=0");
        map.put("sec-fetch-dest", "document");
        map.put("sec-fetch-mode", "navigate");
        map.put("sec-fetch-site", "none");
        map.put("sec-fetch-user", "?1");
        map.put("upgrade-insecure-requests", "1");
        map.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("user-agent", "Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; Redmi Note 3 Build/LMY47V) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/46.0.2490.85 Mobile Safari/537.36 XiaoMi/MiuiBrowser/8.1.4");

        String[] arrays = {"9", "30", "31", "35", "38", "52", "-1"};
        JSONObject json = new JSONObject();
        json.put("size", 10);
        json.put("offset", 0);
        json.put("page", 1);
        json.put("timeout", 1300);
        json.put("type", "all");
        json.put("pay_channel_list", arrays);
        String html = postJson(String.format(order_list_url,nginxUrl,port, uid), json.toJSONString(), map);
        System.out.println(html);
        JSONObject jsonObject = JSONObject.parseObject(html);
        return jsonObject;
    }

    /**
     * 查询商品列表
     *
     * @var string
     */
    private static String rec_order_list = "http://%s:%s/proxy/api/rec_order_list?pdduid=%d&offset=%d&count=%d&list_id=%s&hostname=://mobile.yangkeduo.com";
    public static JSONObject recOrderList(String accessToken, Long uid,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("accesstoken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        // map.put("accept-encoding", "gzip, deflate, br");
        map.put("accept-language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6");
        map.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        map.put("user-agent", "Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; Redmi Note 3 Build/LMY47V) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/46.0.2490.85 Mobile Safari/537.36 XiaoMi/MiuiBrowser/8.1.4");
        Random random = new Random();
        int number = random.nextInt(3) + 1;
        String json = doGetQueryCmd(String.format(rec_order_list,nginxUrl,port, uid, number, 20, RandomStringUtils.randomAlphanumeric(31)), map);
        System.out.println(json);
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject;
    }

    /**
     * 查询地址列表
     *
     * @var string
     */
    private static String addresses = "http://%s:%s/addresses.html?refer_page_name=personal&hostname=mobile.yangkeduo.com";
    public static String addressesList(String accessToken, Long uid, String pdd_user_uin,String nginxUrl,String port) {
        Map map = new HashMap();
        map.put("accesstoken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");

        map.put("Cookie", "PDDAccessToken=" + accessToken + "; api_uid=" + uid + "; pdd_user_uin=" + pdd_user_uin);
        String html = doGetQueryCmd(String.format(addresses,nginxUrl,port), map);
        return html;
    }


    /**
     * 确认收货url
     *
     * @var string
     */
    private static String received_url = "http://%s:%s/proxy/api/order/%s/received?pdduid=%d&hostname=://mobile.yangkeduo.com";

    /**
     * 订单详情url
     *
     * @var string
     */
    private static String order_detial_url = "http://%s:%s/order.html?order_sn=%s&hostname=://mobile.yangkeduo.com";
    public static String orderDetialUrl(String accessToken,String nginxUrl,String port,String order_sn) {
        Map map = new HashMap();
        map.put("accesstoken", accessToken);
        map.put("referer", "https://mobile.yangkeduo.com");
        map.put("Cookie", "PDDAccessToken=" + accessToken + "");
        String html = doGetQueryCmd(String.format(order_detial_url,nginxUrl,port,order_sn), map);
        return html;
    }
    /**
     * 商品详情url
     *
     * @var string
     */
    private static String goods_detial_url = "http://%s:%s/goods.html?goods_id=%d&hostname=://mobile.yangkeduo.com";


    public static String doGetQueryCmd(String httpurl, Map<String, String> headers) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = "";// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            Iterator<Map.Entry<String, String>> entries = headers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println(key + ":" + value);
                connection.setRequestProperty(key, value);
            }

            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (200 != connection.getResponseCode()) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    result += line;
                }
                br.close();
            } else {
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

    /**
     * 发送HttpPost请求
     *
     * @param strURL 服务地址
     * @param params json字符串,例如: "{ \"id\":\"12345\" }" ;其中属性名必须带双引号<br/>
     * @return 成功:返回json字符串<br/>
     */
    public static String postJson(String strURL, String params, Map<String, String> headers) {
        BufferedReader reader = null;
        try {

            URL url = new URL(strURL);// 创建连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式

            Iterator<Map.Entry<String, String>> entries = headers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println(key + ":" + value);
                connection.setRequestProperty(key, value);
            }
            connection.setRequestProperty("content-type", "application/json;charset=UTF-8"); // 设置发送数据的格式
            connection.setDoInput(true);
            connection.connect();
            //一定要用BufferedReader 来接收响应， 使用字节来接收响应的方法是接收不到内容的
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(params);
            out.flush();
            out.close();
            String res = "";

            if (200 != connection.getResponseCode()) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                reader.close();
            } else {
                // 读取响应
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                reader.close();
            }
            return res;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "error"; // 自定义错误信息
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

}