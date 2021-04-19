package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PayDataStatistics implements Serializable {

    /**
     * 商户Id
     *
     * @mbggenerated
     */
    private Integer mchId;

    /**
     * 产品Id
     */
    private Integer productId;

    /**
     * 商户名称
     *
     * @mbggenerated
     */
    private String mchName;

    /**
     * 通道编号
     *
     * @mbggenerated
     */
    private Long passageId;

    /**
     * 通道名称
     */
    private String passageName;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 成功金额
     *
     * @mbggenerated
     */
    private Long totalAmount;

    /**
     * 手续费及
     * @return
     */
    private Long handlingfee;

    /**
     * 成功率
     */
    private String successRate;

    /**
     * 结算金额
     * @return
     */
    private Long amount;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    private Integer limit;

    private Integer offset;

    private String createTimeStart;

    private String createTimeEnd;

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getMchId() {
        return mchId;
    }

    public void setMchId(Integer mchId) {
        this.mchId = mchId;
    }

    public String getMchName() {
        return mchName;
    }

    public void setMchName(String mchName) {
        this.mchName = mchName;
    }

    public Long getPassageId() {
        return passageId;
    }

    public void setPassageId(Long passageId) {
        this.passageId = passageId;
    }

    public String getPassageName() {
        return passageName;
    }

    public void setPassageName(String passageName) {
        this.passageName = passageName;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getHandlingfee() {
        return handlingfee;
    }

    public void setHandlingfee(Long handlingfee) {
        this.handlingfee = handlingfee;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}