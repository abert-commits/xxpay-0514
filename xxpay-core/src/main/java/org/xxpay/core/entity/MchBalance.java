package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class MchBalance implements Serializable {

    /**
     * 商户ID
     */
    private Long mchId;
    /**
     * 名称
     */
    private String name;
    /**
     * 金额
     */
    private Long amount;
    /**
     * 最后一次订单
     */
    private String lastOrder;
    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date createTime;
    /**
     * 商户充值
     */
    private Long merchantRecharge;
    /**
     * 调账金额
     */
    private Long transferAmount;
    /**
     * 提现金额
     */
    private Long withdrawalAmount;
    /**
     * 提现单数
     */
    private Long withdrawalOrder;
    /**
     * 提现手续费
     */
    private Long withdrawalFee;
    /**
     * 昨天余额
     */
    private Long yesterdayBalance;

    /**
     * 误差
     */
    private Long error;
    /**
     *  查询 结束时间
     *
     * @mbggenerated
     */
    private Date createTimeTow;

    public Long getError() {
        return error;
    }

    public void setError(Long error) {
        this.error = error;
    }

    public Date getCreateTimeTow() {
        return createTimeTow;
    }

    public void setCreateTimeTow(Date createTimeTow) {
        this.createTimeTow = createTimeTow;
    }

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getLastOrder() {
        return lastOrder;
    }

    public void setLastOrder(String lastOrder) {
        this.lastOrder = lastOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getMerchantRecharge() {
        return merchantRecharge;
    }

    public void setMerchantRecharge(Long merchantRecharge) {
        this.merchantRecharge = merchantRecharge;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public Long getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Long withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public Long getWithdrawalOrder() {
        return withdrawalOrder;
    }

    public void setWithdrawalOrder(Long withdrawalOrder) {
        this.withdrawalOrder = withdrawalOrder;
    }

    public Long getWithdrawalFee() {
        return withdrawalFee;
    }

    public void setWithdrawalFee(Long withdrawalFee) {
        this.withdrawalFee = withdrawalFee;
    }

    public Long getYesterdayBalance() {
        return yesterdayBalance;
    }

    public void setYesterdayBalance(Long yesterdayBalance) {
        this.yesterdayBalance = yesterdayBalance;
    }
}
