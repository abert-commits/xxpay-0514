package org.xxpay.pay.channel.sandpay.util;

import cn.com.sand.online.agent.service.sdk.EncryptUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandHttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(SandHttpUtil.class);
    private EncryptUtil encyptUtil = new EncryptUtil();

    public SandHttpUtil() {
    }

    public SandHttpUtil(String publicKeyPath, String privateKeyPath, String keyPassword) {
        encyptUtil = new EncryptUtil(publicKeyPath, privateKeyPath, keyPassword);
    }

    public String post(String url, String merchId, String transCode, String data) throws Exception {
        String res = this.post(url, this.encyptUtil.genEncryptData(merchId, transCode, data));
        return res == null?null:this.encyptUtil.decryptRetData(res);
    }

    public String post(String url, String merchId, String transCode, String accessType, String plId, String data) throws Exception {
        String res = this.post(url, this.encyptUtil.genEncryptData(merchId, transCode, accessType, plId, data));
        return res == null?null:this.encyptUtil.decryptRetData(res);
    }

    /**
     * 发起http请求获取返回结果
     * @param req_url 请求地址
     * @return
     */
    public static String doPost(String req_url) {
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(req_url);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

            httpUrlConn.setDoOutput(false);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("POST");
            httpUrlConn.connect();

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return buffer.toString();
    }

    /**
     * HttpClinetPost请求
     * @param url
     * @return
     */
    public static String sendPost(String url) {
        //1.获得一个httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //2.生成一个post请求
        HttpPost httppost = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            //3.执行get请求并返回结果
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //4.处理结果，这里将结果返回为字符串
        HttpEntity entity = response.getEntity();
        String result = null;
        try {
            result = EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送HttpGet请求
     * @param url
     * @return
     */
    public static String sendGet(String url) {
        //1.获得一个httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //2.生成一个get请求
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            //3.执行get请求并返回结果
            response = httpclient.execute(httpget);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String result = null;
        try {
            //4.处理结果，这里将结果返回为字符串
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 发起http请求获取返回结果
     * @param req_url 请求地址
     * @return
     */
    public static String doGet(String req_url) {
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(req_url);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

            httpUrlConn.setDoOutput(false);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return buffer.toString();
    }

    private String post(String url, List<NameValuePair> formparams) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        try {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            logger.info("executing request url:{} ", httppost.getURI());
            CloseableHttpResponse e = httpclient.execute(httppost);

            try {
                HttpEntity entity = e.getEntity();
                String res = EntityUtils.toString(entity, "UTF-8");
                res = URLDecoder.decode(res, "UTF-8");
                if(!StringUtils.isBlank(res)) {
                    logger.info("res:{}", res);
                    String var10 = res;
                    return var10;
                }

                logger.info("null response");
            } finally {
                e.close();
            }
        } catch (ClientProtocolException var32) {
            var32.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException var33) {
            var33.printStackTrace();
            return null;
        } catch (IOException var34) {
            var34.printStackTrace();
            return null;
        } finally {
            try {
                httpclient.close();
            } catch (IOException var30) {
                var30.printStackTrace();
            }

        }

        return null;
    }
}