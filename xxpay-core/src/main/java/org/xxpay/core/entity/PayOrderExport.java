package org.xxpay.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单导出实体
 */
public class PayOrderExport implements Serializable {



    /**
     * 支付订单号
     *
     * @mbggenerated
     */
    private String payOrderId;

    /**
     * 商户ID
     *
     * @mbggenerated
     */
    private Long mchId;


    /**
     * 商户名称
     *
     * @mbggenerated
     */
    private String mchName;

    /**
     * 商户类型:1-平台账户,2-私有账户
     *
     * @mbggenerated
     */
    private Byte mchType;

    /**
     * 商户费率
     *
     * @mbggenerated
     */
    private BigDecimal mchRate;

    /**
     * 商户入账,单位分
     *
     * @mbggenerated
     */
    private Long mchIncome;

    /**
     * 应用ID
     *
     * @mbggenerated
     */
    private String appId;

    /**
     * 商户订单号
     *
     * @mbggenerated
     */
    private String mchOrderNo;

    /**
     * 代理商ID
     *
     * @mbggenerated
     */
    private Long agentId;

    /**
     * 一级代理商ID
     *
     * @mbggenerated
     */
    private Long parentAgentId;

    /**
     * 代理商费率
     *
     * @mbggenerated
     */
    private BigDecimal agentRate;

    /**
     * 一级代理商费率
     *
     * @mbggenerated
     */
    private BigDecimal parentAgentRate;

    /**
     * 代理商利润,单位分
     *
     * @mbggenerated
     */
    private Long agentProfit;

    /**
     * 一级代理商利润
     *
     * @mbggenerated
     */
    private Long parentAgentProfit;

    /**
     * 支付产品ID
     *
     * @mbggenerated
     */
    private Integer productId;

    /**
     * 通道ID
     *
     * @mbggenerated
     */
    private Integer passageId;

    /**
     * 通道账户ID
     *
     * @mbggenerated
     */
    private Integer passageAccountId;

    /**
     * 渠道类型,对接支付接口类型代码
     *
     * @mbggenerated
     */
    private String channelType;

    /**
     * 渠道ID,对应支付接口代码
     *
     * @mbggenerated
     */
    private String channelId;

    /**
     * 支付金额,单位分
     *
     * @mbggenerated
     */
    private Long amount;

    /**
     * 三位货币代码,人民币:cny
     *
     * @mbggenerated
     */
    private String currency;

    /**
     * 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
     *
     * @mbggenerated
     */
    private Byte status;

    /**
     * 客户端IP
     *
     * @mbggenerated
     */
    private String clientIp;

    /**
     * 设备
     *
     * @mbggenerated
     */
    private String device;

    /**
     * 商品标题
     *
     * @mbggenerated
     */
    private String subject;

    /**
     * 商品描述信息
     *
     * @mbggenerated
     */
    private String body;

    /**
     * 特定渠道发起额外参数
     *
     * @mbggenerated
     */
    private String extra;

    /**
     * 渠道用户标识,如微信openId,支付宝账号
     *
     * @mbggenerated
     */
    private String channelUser;

    /**
     * 渠道商户ID
     *
     * @mbggenerated
     */
    private String channelMchId;

    /**
     * 渠道订单号
     *
     * @mbggenerated
     */
    private String channelOrderNo;

    /**
     * 渠道数据包
     *
     * @mbggenerated
     */
    private String channelAttach;

    /**
     * 平台利润,单位分
     *
     * @mbggenerated
     */
    private Long platProfit;

    /**
     * 渠道费率
     *
     * @mbggenerated
     */
    private BigDecimal channelRate;

    /**
     * 渠道成本,单位分
     *
     * @mbggenerated
     */
    private Long channelCost;

    /**
     * 是否退款,0-未退款,1-退款
     *
     * @mbggenerated
     */
    private Byte isRefund;

    /**
     * 退款次数
     *
     * @mbggenerated
     */
    private Integer refundTimes;

    /**
     * 成功退款金额,单位分
     *
     * @mbggenerated
     */
    private Long successRefundAmount;

    /**
     * 渠道支付错误码
     *
     * @mbggenerated
     */
    private String errCode;

    /**
     * 渠道支付错误描述
     *
     * @mbggenerated
     */
    private String errMsg;

    /**
     * 扩展参数1
     *
     * @mbggenerated
     */
    private String param1;

    /**
     * 扩展参数2
     *
     * @mbggenerated
     */
    private String param2;

    /**
     * 通知地址
     *
     * @mbggenerated
     */
    private String notifyUrl;

    /**
     * 跳转地址
     *
     * @mbggenerated
     */
    private String returnUrl;

    /**
     * 订单失效时间
     *
     * @mbggenerated
     */
    private Date expireTime;

    /**
     * 订单支付成功时间
     *
     * @mbggenerated
     */
    private Date paySuccTime;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date createTime;

    /**
     * 更新时间
     *
     * @mbggenerated
     */
    private Date updateTime;

    /**
     * 最小交易金额
     */
    private Long minAmount;

    /**
     * 最大交易金额
     */
    private Long maxAmount;

    /**
     * 通道名称
     * @return
     */
    private String passageName;

    public Long getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getHighest() {
        return highest;
    }

    public void setHighest(Integer highest) {
        this.highest = highest;
    }

    private  Integer highest;

    public String getPassageName() {
        return passageName;
    }

    public void setPassageName(String passageName) {
        this.passageName = passageName;
    }

    public String getMchName() {
        return mchName;
    }

    public void setMchName(String mchName) {
        this.mchName = mchName;
    }

    public Byte getMchType() {
        return mchType;
    }

    public void setMchType(Byte mchType) {
        this.mchType = mchType;
    }

    public BigDecimal getMchRate() {
        return mchRate;
    }

    public void setMchRate(BigDecimal mchRate) {
        this.mchRate = mchRate;
    }

    public Long getMchIncome() {
        return mchIncome;
    }

    public void setMchIncome(Long mchIncome) {
        this.mchIncome = mchIncome;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Long getParentAgentId() {
        return parentAgentId;
    }

    public void setParentAgentId(Long parentAgentId) {
        this.parentAgentId = parentAgentId;
    }

    public BigDecimal getAgentRate() {
        return agentRate;
    }

    public void setAgentRate(BigDecimal agentRate) {
        this.agentRate = agentRate;
    }

    public BigDecimal getParentAgentRate() {
        return parentAgentRate;
    }

    public void setParentAgentRate(BigDecimal parentAgentRate) {
        this.parentAgentRate = parentAgentRate;
    }

    public Long getAgentProfit() {
        return agentProfit;
    }

    public void setAgentProfit(Long agentProfit) {
        this.agentProfit = agentProfit;
    }

    public Long getParentAgentProfit() {
        return parentAgentProfit;
    }

    public void setParentAgentProfit(Long parentAgentProfit) {
        this.parentAgentProfit = parentAgentProfit;
    }

    public Integer getPassageAccountId() {
        return passageAccountId;
    }

    public void setPassageAccountId(Integer passageAccountId) {
        this.passageAccountId = passageAccountId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getChannelUser() {
        return channelUser;
    }

    public void setChannelUser(String channelUser) {
        this.channelUser = channelUser;
    }

    public String getChannelMchId() {
        return channelMchId;
    }

    public void setChannelMchId(String channelMchId) {
        this.channelMchId = channelMchId;
    }

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public void setChannelOrderNo(String channelOrderNo) {
        this.channelOrderNo = channelOrderNo;
    }

    public String getChannelAttach() {
        return channelAttach;
    }

    public void setChannelAttach(String channelAttach) {
        this.channelAttach = channelAttach;
    }

    public Long getPlatProfit() {
        return platProfit;
    }

    public void setPlatProfit(Long platProfit) {
        this.platProfit = platProfit;
    }

    public BigDecimal getChannelRate() {
        return channelRate;
    }

    public void setChannelRate(BigDecimal channelRate) {
        this.channelRate = channelRate;
    }

    public Long getChannelCost() {
        return channelCost;
    }

    public void setChannelCost(Long channelCost) {
        this.channelCost = channelCost;
    }

    public Byte getIsRefund() {
        return isRefund;
    }

    public void setIsRefund(Byte isRefund) {
        this.isRefund = isRefund;
    }

    public Integer getRefundTimes() {
        return refundTimes;
    }

    public void setRefundTimes(Integer refundTimes) {
        this.refundTimes = refundTimes;
    }

    public Long getSuccessRefundAmount() {
        return successRefundAmount;
    }

    public void setSuccessRefundAmount(Long successRefundAmount) {
        this.successRefundAmount = successRefundAmount;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getPaySuccTime() {
        return paySuccTime;
    }

    public void setPaySuccTime(Date paySuccTime) {
        this.paySuccTime = paySuccTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 产品类型:1-收款,2-充值
     *
     * @mbggenerated
     */
    private Byte productType;

    /**
     * 创建开始时间
     */
    private Date createTimeStart;

    /**
     * 创建结束时间
     */
    private Date createTimeEnd;


    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getMchOrderNo() {
        return mchOrderNo;
    }

    public void setMchOrderNo(String mchOrderNo) {
        this.mchOrderNo = mchOrderNo;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getPassageId() {
        return passageId;
    }

    public void setPassageId(Integer passageId) {
        this.passageId = passageId;
    }

    public Byte getProductType() {
        return productType;
    }

    public void setProductType(Byte productType) {
        this.productType = productType;
    }
}
