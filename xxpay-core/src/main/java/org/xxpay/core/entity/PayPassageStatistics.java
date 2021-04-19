package org.xxpay.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class PayPassageStatistics implements Serializable {
    /**
     * 支付通道ID
     *
     * @mbggenerated
     */
    private Integer id;


    /**
     * 通道名称
     *
     * @mbggenerated
     */
    private String passageName;


    /**
     * 通道费率百分比
     *
     * @mbggenerated
     */
    private String passageRate;

    /**
     * 前天充值
     *
     * @mbggenerated
     */
    private Long twoDaysRecharge;

    /**
     * 昨天充值
     *
     * @mbggenerated
     */
    private Long oneDaysRecharge;


    /**
     * 今日充值
     *
     * @mbggenerated
     */
    private Long currentDaysRecharge;

    /**
     * 商户入账
     *
     * @mbggenerated
     */
    private Long mchIncome;
    /**
     * 提交订单数
     *
     * @mbggenerated
     */
    private Long ordersNumber;
    /**
     * 成功订单
     *
     * @mbggenerated
     */
    private Long successOrdersNumber;


    /**
     * 成功率
     *
     * @mbggenerated
     */
    private String successRate;

    /**
     * 统计开始时间
     *
     * @mbggenerated
     */
    private String createTimeStart;

    /**
     * 统计结束时间
     */
    private String createTimeEnd;

    /**
     * 昨天时间
     *
     * @mbggenerated
     */
    private String oneDaysDate;

    /**
     * 前天时间
     *
     * @mbggenerated
     */
    private String twoDaysDate;

    private Integer limit;

    private Integer offset;

    public Long getMchIncome() {
        return mchIncome;
    }

    public void setMchIncome(Long mchIncome) {
        this.mchIncome = mchIncome;
    }

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassageName() {
        return passageName;
    }

    public void setPassageName(String passageName) {
        this.passageName = passageName;
    }

    public String getPassageRate() {
        return passageRate;
    }

    public void setPassageRate(String passageRate) {
        this.passageRate = passageRate;
    }

    public Long getTwoDaysRecharge() {
        return twoDaysRecharge;
    }

    public void setTwoDaysRecharge(Long twoDaysRecharge) {
        this.twoDaysRecharge = twoDaysRecharge;
    }

    public Long getOneDaysRecharge() {
        return oneDaysRecharge;
    }

    public void setOneDaysRecharge(Long oneDaysRecharge) {
        this.oneDaysRecharge = oneDaysRecharge;
    }

    public Long getCurrentDaysRecharge() {
        return currentDaysRecharge;
    }

    public void setCurrentDaysRecharge(Long currentDaysRecharge) {
        this.currentDaysRecharge = currentDaysRecharge;
    }

    public Long getOrdersNumber() {
        return ordersNumber;
    }

    public void setOrdersNumber(Long ordersNumber) {
        this.ordersNumber = ordersNumber;
    }

    public Long getSuccessOrdersNumber() {
        return successOrdersNumber;
    }

    public void setSuccessOrdersNumber(Long successOrdersNumber) {
        this.successOrdersNumber = successOrdersNumber;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public String getOneDaysDate() {
        return oneDaysDate;
    }

    public void setOneDaysDate(String oneDaysDate) {
        this.oneDaysDate = oneDaysDate;
    }

    public String getTwoDaysDate() {
        return twoDaysDate;
    }

    public void setTwoDaysDate(String twoDaysDate) {
        this.twoDaysDate = twoDaysDate;
    }
}