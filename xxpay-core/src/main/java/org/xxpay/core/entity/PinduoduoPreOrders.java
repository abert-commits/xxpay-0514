package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoPreOrders implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date startingTime;

    /**
     * 结束时间
     *
     * @mbggenerated
     */
    private Date endTime;

    /**
     * 频率
     *
     * @mbggenerated
     */
    private Long frequency;

    /**
     * 总订单数
     *
     * @mbggenerated
     */
    private Long totalOrders;

    /**
     * 创建日期
     *
     * @mbggenerated
     */
    private Date ctime;

    /**
     * 上一次创建预订单时间
     *
     * @mbggenerated
     */
    private Date lastTime;

    /**
     * 商品id
     *
     * @mbggenerated
     */
    private Long goodsId;

    /**
     * 标题
     *
     * @mbggenerated
     */
    private String name;

    /**
     * 成功单数
     *
     * @mbggenerated
     */
    private Long completionsNumber;

    /**
     * 0.执行中，1.执行完成，2停止
     *
     * @mbggenerated
     */
    private Integer status;

    /**
     * 支付类型 WECHAT = 38; ALIPAY = 9;
     *
     * @mbggenerated
     */
    private String payCode;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCompletionsNumber() {
        return completionsNumber;
    }

    public void setCompletionsNumber(Long completionsNumber) {
        this.completionsNumber = completionsNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPayCode() {
        return payCode;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", startingTime=").append(startingTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", frequency=").append(frequency);
        sb.append(", totalOrders=").append(totalOrders);
        sb.append(", ctime=").append(ctime);
        sb.append(", lastTime=").append(lastTime);
        sb.append(", goodsId=").append(goodsId);
        sb.append(", name=").append(name);
        sb.append(", completionsNumber=").append(completionsNumber);
        sb.append(", status=").append(status);
        sb.append(", payCode=").append(payCode);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PinduoduoPreOrders other = (PinduoduoPreOrders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStartingTime() == null ? other.getStartingTime() == null : this.getStartingTime().equals(other.getStartingTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getFrequency() == null ? other.getFrequency() == null : this.getFrequency().equals(other.getFrequency()))
            && (this.getTotalOrders() == null ? other.getTotalOrders() == null : this.getTotalOrders().equals(other.getTotalOrders()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getLastTime() == null ? other.getLastTime() == null : this.getLastTime().equals(other.getLastTime()))
            && (this.getGoodsId() == null ? other.getGoodsId() == null : this.getGoodsId().equals(other.getGoodsId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getCompletionsNumber() == null ? other.getCompletionsNumber() == null : this.getCompletionsNumber().equals(other.getCompletionsNumber()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getPayCode() == null ? other.getPayCode() == null : this.getPayCode().equals(other.getPayCode()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStartingTime() == null) ? 0 : getStartingTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getFrequency() == null) ? 0 : getFrequency().hashCode());
        result = prime * result + ((getTotalOrders() == null) ? 0 : getTotalOrders().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getLastTime() == null) ? 0 : getLastTime().hashCode());
        result = prime * result + ((getGoodsId() == null) ? 0 : getGoodsId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getCompletionsNumber() == null) ? 0 : getCompletionsNumber().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPayCode() == null) ? 0 : getPayCode().hashCode());
        return result;
    }
}