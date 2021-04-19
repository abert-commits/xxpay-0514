package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoOrders implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 管理员id
     *
     * @mbggenerated
     */
    private Integer adminUid;

    /**
     * 员工id
     *
     * @mbggenerated
     */
    private Integer staffId;

    /**
     * 订单编号
     *
     * @mbggenerated
     */
    private String orderSn;

    /**
     * api调用者生成的订单编号
     *
     * @mbggenerated
     */
    private String apiOrderSn;

    /**
     * ip地址
     *
     * @mbggenerated
     */
    private String ip;

    /**
     * 拼多多fp_id
     *
     * @mbggenerated
     */
    private String fpId;

    /**
     * 总价
     *
     * @mbggenerated
     */
    private Integer total;

    /**
     * 是否支付(0否,1是)
     *
     * @mbggenerated
     */
    private Boolean isPay;

    /**
     * 通知url
     *
     * @mbggenerated
     */
    private String notifyUrl;

    /**
     * 是否通知(0未通知,1已通知)
     *
     * @mbggenerated
     */
    private Boolean isNotify;

    /**
     * 支付方式(9支付宝,38微信)
     *
     * @mbggenerated
     */
    private Byte payType;

    /**
     * 来自哪个平台(1自行出码,0支付平台)
     *
     * @mbggenerated
     */
    private Byte fromPlatform;

    /**
     * 订单状态(0待付款,1待发货,2待收货,3待评价,4交易已取消)
     *
     * @mbggenerated
     */
    private Integer status;

    /**
     * 修改日期
     *
     * @mbggenerated
     */
    private Date mtime;

    /**
     * 创建日期
     *
     * @mbggenerated
     */
    private Date ctime;

    /**
     * 商品id
     *
     * @mbggenerated
     */
    private Integer gId;

    /**
     * 用户id
     *
     * @mbggenerated
     */
    private Integer userId;

    /**
     * 商户id
     *
     * @mbggenerated
     */
    private Integer cId;

    /**
     * 通道id
     *
     * @mbggenerated
     */
    private Integer pId;

    /**
     * 代理商id
     *
     * @mbggenerated
     */
    private Integer dId;

    private Integer notifyStatus;

    private Integer notifyNumber;

    private Integer notifyTime;

    /**
     * 手机号
     *
     * @mbggenerated
     */
    private String phone;

    /**
     * 店铺名称
     *
     * @mbggenerated
     */
    private String storesName;

    /**
     * 店铺ID
     *
     * @mbggenerated
     */
    private Integer storesId;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(Integer adminUid) {
        this.adminUid = adminUid;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getApiOrderSn() {
        return apiOrderSn;
    }

    public void setApiOrderSn(String apiOrderSn) {
        this.apiOrderSn = apiOrderSn;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFpId() {
        return fpId;
    }

    public void setFpId(String fpId) {
        this.fpId = fpId;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getIsPay() {
        return isPay;
    }

    public void setIsPay(Boolean isPay) {
        this.isPay = isPay;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Boolean getIsNotify() {
        return isNotify;
    }

    public void setIsNotify(Boolean isNotify) {
        this.isNotify = isNotify;
    }

    public Byte getPayType() {
        return payType;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public Byte getFromPlatform() {
        return fromPlatform;
    }

    public void setFromPlatform(Byte fromPlatform) {
        this.fromPlatform = fromPlatform;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Integer getgId() {
        return gId;
    }

    public void setgId(Integer gId) {
        this.gId = gId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getcId() {
        return cId;
    }

    public void setcId(Integer cId) {
        this.cId = cId;
    }

    public Integer getpId() {
        return pId;
    }

    public void setpId(Integer pId) {
        this.pId = pId;
    }

    public Integer getdId() {
        return dId;
    }

    public void setdId(Integer dId) {
        this.dId = dId;
    }

    public Integer getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(Integer notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public Integer getNotifyNumber() {
        return notifyNumber;
    }

    public void setNotifyNumber(Integer notifyNumber) {
        this.notifyNumber = notifyNumber;
    }

    public Integer getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(Integer notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStoresName() {
        return storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public Integer getStoresId() {
        return storesId;
    }

    public void setStoresId(Integer storesId) {
        this.storesId = storesId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", adminUid=").append(adminUid);
        sb.append(", staffId=").append(staffId);
        sb.append(", orderSn=").append(orderSn);
        sb.append(", apiOrderSn=").append(apiOrderSn);
        sb.append(", ip=").append(ip);
        sb.append(", fpId=").append(fpId);
        sb.append(", total=").append(total);
        sb.append(", isPay=").append(isPay);
        sb.append(", notifyUrl=").append(notifyUrl);
        sb.append(", isNotify=").append(isNotify);
        sb.append(", payType=").append(payType);
        sb.append(", fromPlatform=").append(fromPlatform);
        sb.append(", status=").append(status);
        sb.append(", mtime=").append(mtime);
        sb.append(", ctime=").append(ctime);
        sb.append(", gId=").append(gId);
        sb.append(", userId=").append(userId);
        sb.append(", cId=").append(cId);
        sb.append(", pId=").append(pId);
        sb.append(", dId=").append(dId);
        sb.append(", notifyStatus=").append(notifyStatus);
        sb.append(", notifyNumber=").append(notifyNumber);
        sb.append(", notifyTime=").append(notifyTime);
        sb.append(", phone=").append(phone);
        sb.append(", storesName=").append(storesName);
        sb.append(", storesId=").append(storesId);
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
        PinduoduoOrders other = (PinduoduoOrders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAdminUid() == null ? other.getAdminUid() == null : this.getAdminUid().equals(other.getAdminUid()))
            && (this.getStaffId() == null ? other.getStaffId() == null : this.getStaffId().equals(other.getStaffId()))
            && (this.getOrderSn() == null ? other.getOrderSn() == null : this.getOrderSn().equals(other.getOrderSn()))
            && (this.getApiOrderSn() == null ? other.getApiOrderSn() == null : this.getApiOrderSn().equals(other.getApiOrderSn()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getFpId() == null ? other.getFpId() == null : this.getFpId().equals(other.getFpId()))
            && (this.getTotal() == null ? other.getTotal() == null : this.getTotal().equals(other.getTotal()))
            && (this.getIsPay() == null ? other.getIsPay() == null : this.getIsPay().equals(other.getIsPay()))
            && (this.getNotifyUrl() == null ? other.getNotifyUrl() == null : this.getNotifyUrl().equals(other.getNotifyUrl()))
            && (this.getIsNotify() == null ? other.getIsNotify() == null : this.getIsNotify().equals(other.getIsNotify()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getFromPlatform() == null ? other.getFromPlatform() == null : this.getFromPlatform().equals(other.getFromPlatform()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getgId() == null ? other.getgId() == null : this.getgId().equals(other.getgId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getcId() == null ? other.getcId() == null : this.getcId().equals(other.getcId()))
            && (this.getpId() == null ? other.getpId() == null : this.getpId().equals(other.getpId()))
            && (this.getdId() == null ? other.getdId() == null : this.getdId().equals(other.getdId()))
            && (this.getNotifyStatus() == null ? other.getNotifyStatus() == null : this.getNotifyStatus().equals(other.getNotifyStatus()))
            && (this.getNotifyNumber() == null ? other.getNotifyNumber() == null : this.getNotifyNumber().equals(other.getNotifyNumber()))
            && (this.getNotifyTime() == null ? other.getNotifyTime() == null : this.getNotifyTime().equals(other.getNotifyTime()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getStoresName() == null ? other.getStoresName() == null : this.getStoresName().equals(other.getStoresName()))
            && (this.getStoresId() == null ? other.getStoresId() == null : this.getStoresId().equals(other.getStoresId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAdminUid() == null) ? 0 : getAdminUid().hashCode());
        result = prime * result + ((getStaffId() == null) ? 0 : getStaffId().hashCode());
        result = prime * result + ((getOrderSn() == null) ? 0 : getOrderSn().hashCode());
        result = prime * result + ((getApiOrderSn() == null) ? 0 : getApiOrderSn().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getFpId() == null) ? 0 : getFpId().hashCode());
        result = prime * result + ((getTotal() == null) ? 0 : getTotal().hashCode());
        result = prime * result + ((getIsPay() == null) ? 0 : getIsPay().hashCode());
        result = prime * result + ((getNotifyUrl() == null) ? 0 : getNotifyUrl().hashCode());
        result = prime * result + ((getIsNotify() == null) ? 0 : getIsNotify().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getFromPlatform() == null) ? 0 : getFromPlatform().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getgId() == null) ? 0 : getgId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getcId() == null) ? 0 : getcId().hashCode());
        result = prime * result + ((getpId() == null) ? 0 : getpId().hashCode());
        result = prime * result + ((getdId() == null) ? 0 : getdId().hashCode());
        result = prime * result + ((getNotifyStatus() == null) ? 0 : getNotifyStatus().hashCode());
        result = prime * result + ((getNotifyNumber() == null) ? 0 : getNotifyNumber().hashCode());
        result = prime * result + ((getNotifyTime() == null) ? 0 : getNotifyTime().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getStoresName() == null) ? 0 : getStoresName().hashCode());
        result = prime * result + ((getStoresId() == null) ? 0 : getStoresId().hashCode());
        return result;
    }
}