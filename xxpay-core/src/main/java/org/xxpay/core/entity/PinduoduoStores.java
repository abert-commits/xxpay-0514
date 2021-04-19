package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoStores implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 店铺名称
     *
     * @mbggenerated
     */
    private String name;

    /**
     * 代理商id
     *
     * @mbggenerated
     */
    private Integer admin_uid;

    /**
     * 商铺额度限制
     *
     * @mbggenerated
     */
    private Long store_remain_total;

    /**
     * 下单额度
     *
     * @mbggenerated
     */
    private Long order_total;

    /**
     * 成团额度
     *
     * @mbggenerated
     */
    private Long cur_total;

    /**
     * 状态(0禁用,1启用)
     *
     * @mbggenerated
     */
    private Boolean status;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date ctime;

    /**
     * 修改时间
     *
     * @mbggenerated
     */
    private Date mtime;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAdmin_uid() {
        return admin_uid;
    }

    public void setAdmin_uid(Integer admin_uid) {
        this.admin_uid = admin_uid;
    }

    public Long getStore_remain_total() {
        return store_remain_total;
    }

    public void setStore_remain_total(Long store_remain_total) {
        this.store_remain_total = store_remain_total;
    }

    public Long getOrder_total() {
        return order_total;
    }

    public void setOrder_total(Long order_total) {
        this.order_total = order_total;
    }

    public Long getCur_total() {
        return cur_total;
    }

    public void setCur_total(Long cur_total) {
        this.cur_total = cur_total;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
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
        sb.append(", name=").append(name);
        sb.append(", admin_uid=").append(admin_uid);
        sb.append(", store_remain_total=").append(store_remain_total);
        sb.append(", order_total=").append(order_total);
        sb.append(", cur_total=").append(cur_total);
        sb.append(", status=").append(status);
        sb.append(", ctime=").append(ctime);
        sb.append(", mtime=").append(mtime);
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
        PinduoduoStores other = (PinduoduoStores) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getAdmin_uid() == null ? other.getAdmin_uid() == null : this.getAdmin_uid().equals(other.getAdmin_uid()))
            && (this.getStore_remain_total() == null ? other.getStore_remain_total() == null : this.getStore_remain_total().equals(other.getStore_remain_total()))
            && (this.getOrder_total() == null ? other.getOrder_total() == null : this.getOrder_total().equals(other.getOrder_total()))
            && (this.getCur_total() == null ? other.getCur_total() == null : this.getCur_total().equals(other.getCur_total()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()))
            && (this.getC_id() == null ? other.getC_id() == null : this.getC_id().equals(other.getC_id()))
            && (this.getD_id() == null ? other.getD_id() == null : this.getD_id().equals(other.getD_id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getAdmin_uid() == null) ? 0 : getAdmin_uid().hashCode());
        result = prime * result + ((getStore_remain_total() == null) ? 0 : getStore_remain_total().hashCode());
        result = prime * result + ((getOrder_total() == null) ? 0 : getOrder_total().hashCode());
        result = prime * result + ((getCur_total() == null) ? 0 : getCur_total().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        result = prime * result + ((getC_id() == null) ? 0 : getC_id().hashCode());
        result = prime * result + ((getD_id() == null) ? 0 : getD_id().hashCode());
        return result;
    }
}