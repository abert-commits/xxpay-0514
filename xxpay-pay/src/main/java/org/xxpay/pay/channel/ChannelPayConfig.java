package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author: tom
 * @date: 20/3/17
 * @description: 配置
 */
@Component
public class ChannelPayConfig {

    // 商户ID
    private String mchId;
    // 商户Key
    private String mD5Key;
    // 请求地址
    private String reqUrl;
    // RSA公钥
    private String rsaPublicKey;
    // RSA私钥
    private String rsaprivateKey;
    // 私钥密码
    private String rsapassWord;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    // 通道ID
    private String channelId;


    public ChannelPayConfig(){}

    public ChannelPayConfig(String payParam) {
        Assert.notNull(payParam, "init swiftpay config error");
        JSONObject object = JSONObject.parseObject(payParam);
        this.mchId = object.getString("mchId");
        this.mD5Key = object.getString("mD5Key");
        this.rsaPublicKey = object.getString("rsaPublicKey");
        this.rsaprivateKey = object.getString("rsaprivateKey");
        this.rsapassWord = object.getString("rsapassWord");
        this.channelId = object.getString("channelId");
        this.reqUrl = object.getString("reqUrl");
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }


    public String getmD5Key() {
        return mD5Key;
    }

    public void setmD5Key(String mD5Key) {
        this.mD5Key = mD5Key;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getRsaprivateKey() {
        return rsaprivateKey;
    }

    public void setRsaprivateKey(String rsaprivateKey) {
        this.rsaprivateKey = rsaprivateKey;
    }

    public String getRsapassWord() {
        return rsapassWord;
    }

    public void setRsapassWord(String rsapassWord) {
        this.rsapassWord = rsapassWord;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }
}
