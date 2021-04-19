package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoUser implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 手机号码
     *
     * @mbggenerated
     */
    private String phone;

    /**
     * 拼多多access_token
     *
     * @mbggenerated
     */
    private String access_token;

    /**
     * 拼多多acid
     *
     * @mbggenerated
     */
    private String acid;

    /**
     * 拼多多uid
     *
     * @mbggenerated
     */
    private Long uid;

    /**
     * 拼多多uin
     *
     * @mbggenerated
     */
    private String uin;

    /**
     * 管理员id
     *
     * @mbggenerated
     */
    private Integer admin_uid;

    /**
     * 管理员ip地址
     *
     * @mbggenerated
     */
    private String ip;

    /** 0：false 1：true
     * 是否过期(0未过期,1过期,2未填收货地址,3下单失败)
     *
     * @mbggenerated
     */
    private Boolean is_expired;

    /**
     * 是否受限(0未受限,1受限)
     *
     * @mbggenerated
     */
    private Boolean is_limit;

    /**
     * 是否无地址(0有地址,1无地址)
     *
     * @mbggenerated
     */
    private Boolean no_addr;

    /**
     * 使用时间
     *
     * @mbggenerated
     */
    private Integer use_time;

    /**
     * 今日总额
     *
     * @mbggenerated
     */
    private Integer today_total;

    /**
     * 是否超总额(0未超额,1超额)
     *
     * @mbggenerated
     */
    private Boolean is_limit_total;

    /**
     * 提交日期
     *
     * @mbggenerated
     */
    private Integer comment_time;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date ctime;

    /**
     * 是否禁用(1启用,0禁用)
     *
     * @mbggenerated
     */
    private Boolean status;

    /**
     * 超额字符串
     *
     * @mbggenerated
     */
    private String expired_limit_noaddr;

    /**
     * 地址id
     *
     * @mbggenerated
     */
    private Long address_id;

    /**
     * 商户id
     *
     * @mbggenerated
     */
    private Integer c_id;

    /**
     * 代理商id
     *
     * @mbggenerated
     */
    private Integer d_id;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAcid() {
        return acid;
    }

    public void setAcid(String acid) {
        this.acid = acid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public Integer getAdmin_uid() {
        return admin_uid;
    }

    public void setAdmin_uid(Integer admin_uid) {
        this.admin_uid = admin_uid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getIs_expired() {
        return is_expired;
    }

    public void setIs_expired(Boolean is_expired) {
        this.is_expired = is_expired;
    }

    public Boolean getIs_limit() {
        return is_limit;
    }

    public void setIs_limit(Boolean is_limit) {
        this.is_limit = is_limit;
    }

    public Boolean getNo_addr() {
        return no_addr;
    }

    public void setNo_addr(Boolean no_addr) {
        this.no_addr = no_addr;
    }

    public Integer getUse_time() {
        return use_time;
    }

    public void setUse_time(Integer use_time) {
        this.use_time = use_time;
    }

    public Integer getToday_total() {
        return today_total;
    }

    public void setToday_total(Integer today_total) {
        this.today_total = today_total;
    }

    public Boolean getIs_limit_total() {
        return is_limit_total;
    }

    public void setIs_limit_total(Boolean is_limit_total) {
        this.is_limit_total = is_limit_total;
    }

    public Integer getComment_time() {
        return comment_time;
    }

    public void setComment_time(Integer comment_time) {
        this.comment_time = comment_time;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getExpired_limit_noaddr() {
        return expired_limit_noaddr;
    }

    public void setExpired_limit_noaddr(String expired_limit_noaddr) {
        this.expired_limit_noaddr = expired_limit_noaddr;
    }

    public Long getAddress_id() {
        return address_id;
    }

    public void setAddress_id(Long address_id) {
        this.address_id = address_id;
    }

    public Integer getC_id() {
        return c_id;
    }

    public void setC_id(Integer c_id) {
        this.c_id = c_id;
    }

    public Integer getD_id() {
        return d_id;
    }

    public void setD_id(Integer d_id) {
        this.d_id = d_id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", phone=").append(phone);
        sb.append(", access_token=").append(access_token);
        sb.append(", acid=").append(acid);
        sb.append(", uid=").append(uid);
        sb.append(", uin=").append(uin);
        sb.append(", admin_uid=").append(admin_uid);
        sb.append(", ip=").append(ip);
        sb.append(", is_expired=").append(is_expired);
        sb.append(", is_limit=").append(is_limit);
        sb.append(", no_addr=").append(no_addr);
        sb.append(", use_time=").append(use_time);
        sb.append(", today_total=").append(today_total);
        sb.append(", is_limit_total=").append(is_limit_total);
        sb.append(", comment_time=").append(comment_time);
        sb.append(", ctime=").append(ctime);
        sb.append(", status=").append(status);
        sb.append(", expired_limit_noaddr=").append(expired_limit_noaddr);
        sb.append(", address_id=").append(address_id);
        sb.append(", c_id=").append(c_id);
        sb.append(", d_id=").append(d_id);
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
        PinduoduoUser other = (PinduoduoUser) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getAccess_token() == null ? other.getAccess_token() == null : this.getAccess_token().equals(other.getAccess_token()))
            && (this.getAcid() == null ? other.getAcid() == null : this.getAcid().equals(other.getAcid()))
            && (this.getUid() == null ? other.getUid() == null : this.getUid().equals(other.getUid()))
            && (this.getUin() == null ? other.getUin() == null : this.getUin().equals(other.getUin()))
            && (this.getAdmin_uid() == null ? other.getAdmin_uid() == null : this.getAdmin_uid().equals(other.getAdmin_uid()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getIs_expired() == null ? other.getIs_expired() == null : this.getIs_expired().equals(other.getIs_expired()))
            && (this.getIs_limit() == null ? other.getIs_limit() == null : this.getIs_limit().equals(other.getIs_limit()))
            && (this.getNo_addr() == null ? other.getNo_addr() == null : this.getNo_addr().equals(other.getNo_addr()))
            && (this.getUse_time() == null ? other.getUse_time() == null : this.getUse_time().equals(other.getUse_time()))
            && (this.getToday_total() == null ? other.getToday_total() == null : this.getToday_total().equals(other.getToday_total()))
            && (this.getIs_limit_total() == null ? other.getIs_limit_total() == null : this.getIs_limit_total().equals(other.getIs_limit_total()))
            && (this.getComment_time() == null ? other.getComment_time() == null : this.getComment_time().equals(other.getComment_time()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getExpired_limit_noaddr() == null ? other.getExpired_limit_noaddr() == null : this.getExpired_limit_noaddr().equals(other.getExpired_limit_noaddr()))
            && (this.getAddress_id() == null ? other.getAddress_id() == null : this.getAddress_id().equals(other.getAddress_id()))
            && (this.getC_id() == null ? other.getC_id() == null : this.getC_id().equals(other.getC_id()))
            && (this.getD_id() == null ? other.getD_id() == null : this.getD_id().equals(other.getD_id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getAccess_token() == null) ? 0 : getAccess_token().hashCode());
        result = prime * result + ((getAcid() == null) ? 0 : getAcid().hashCode());
        result = prime * result + ((getUid() == null) ? 0 : getUid().hashCode());
        result = prime * result + ((getUin() == null) ? 0 : getUin().hashCode());
        result = prime * result + ((getAdmin_uid() == null) ? 0 : getAdmin_uid().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getIs_expired() == null) ? 0 : getIs_expired().hashCode());
        result = prime * result + ((getIs_limit() == null) ? 0 : getIs_limit().hashCode());
        result = prime * result + ((getNo_addr() == null) ? 0 : getNo_addr().hashCode());
        result = prime * result + ((getUse_time() == null) ? 0 : getUse_time().hashCode());
        result = prime * result + ((getToday_total() == null) ? 0 : getToday_total().hashCode());
        result = prime * result + ((getIs_limit_total() == null) ? 0 : getIs_limit_total().hashCode());
        result = prime * result + ((getComment_time() == null) ? 0 : getComment_time().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getExpired_limit_noaddr() == null) ? 0 : getExpired_limit_noaddr().hashCode());
        result = prime * result + ((getAddress_id() == null) ? 0 : getAddress_id().hashCode());
        result = prime * result + ((getC_id() == null) ? 0 : getC_id().hashCode());
        result = prime * result + ((getD_id() == null) ? 0 : getD_id().hashCode());
        return result;
    }
}