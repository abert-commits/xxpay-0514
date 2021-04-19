package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoGoods implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 代理商id
     *
     * @mbggenerated
     */
    private Integer admin_uid;

    /**
     * 店铺id
     *
     * @mbggenerated
     */
    private Integer stores_id;
    /**
     * 店铺 名称
     *
     * @mbggenerated
     */
    private String stores_name;
    /**
     * 商品名称
     *
     * @mbggenerated
     */
    private String goods_name;

    /**
     * 商品链接
     *
     * @mbggenerated
     */
    private String goods_url;

    /**
     * 商品id
     *
     * @mbggenerated
     */
    private Long goods_id;

    /**
     * 拼多多sku_id
     *
     * @mbggenerated
     */
    private Long sku_id;

    /**
     * 组id
     *
     * @mbggenerated
     */
    private Long group_id;

    /**
     * 默认价格
     *
     * @mbggenerated
     */
    private Integer normal_price;

    /**
     * 错误次数
     *
     * @mbggenerated
     */
    private Integer error_count;

    /**
     * 是否超库存(0否,1是)
     *
     * @mbggenerated
     */
    private Boolean is_store_limit;

    /**
     * 状态(0禁用,1启用)
     *
     * @mbggenerated
     */
    private Boolean status;

    /**
     * 最后使用时间
     *
     * @mbggenerated
     */
    private Integer last_use_time;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date ctime;

    /**
     * 是否上架(0否,1是)
     *
     * @mbggenerated
     */
    private Boolean is_upper;

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

    public String getStores_name() {
        return stores_name;
    }

    public void setStores_name(String stores_name) {
        this.stores_name = stores_name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdmin_uid() {
        return admin_uid;
    }

    public void setAdmin_uid(Integer admin_uid) {
        this.admin_uid = admin_uid;
    }

    public Integer getStores_id() {
        return stores_id;
    }

    public void setStores_id(Integer stores_id) {
        this.stores_id = stores_id;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getGoods_url() {
        return goods_url;
    }

    public void setGoods_url(String goods_url) {
        this.goods_url = goods_url;
    }

    public Long getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(Long goods_id) {
        this.goods_id = goods_id;
    }

    public Long getSku_id() {
        return sku_id;
    }

    public void setSku_id(Long sku_id) {
        this.sku_id = sku_id;
    }

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public Integer getNormal_price() {
        return normal_price;
    }

    public void setNormal_price(Integer normal_price) {
        this.normal_price = normal_price;
    }

    public Integer getError_count() {
        return error_count;
    }

    public void setError_count(Integer error_count) {
        this.error_count = error_count;
    }

    public Boolean getIs_store_limit() {
        return is_store_limit;
    }

    public void setIs_store_limit(Boolean is_store_limit) {
        this.is_store_limit = is_store_limit;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getLast_use_time() {
        return last_use_time;
    }

    public void setLast_use_time(Integer last_use_time) {
        this.last_use_time = last_use_time;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Boolean getIs_upper() {
        return is_upper;
    }

    public void setIs_upper(Boolean is_upper) {
        this.is_upper = is_upper;
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
        sb.append(", admin_uid=").append(admin_uid);
        sb.append(", stores_id=").append(stores_id);
        sb.append(", goods_name=").append(goods_name);
        sb.append(", goods_url=").append(goods_url);
        sb.append(", goods_id=").append(goods_id);
        sb.append(", sku_id=").append(sku_id);
        sb.append(", group_id=").append(group_id);
        sb.append(", normal_price=").append(normal_price);
        sb.append(", error_count=").append(error_count);
        sb.append(", is_store_limit=").append(is_store_limit);
        sb.append(", status=").append(status);
        sb.append(", last_use_time=").append(last_use_time);
        sb.append(", ctime=").append(ctime);
        sb.append(", is_upper=").append(is_upper);
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
        PinduoduoGoods other = (PinduoduoGoods) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAdmin_uid() == null ? other.getAdmin_uid() == null : this.getAdmin_uid().equals(other.getAdmin_uid()))
            && (this.getStores_id() == null ? other.getStores_id() == null : this.getStores_id().equals(other.getStores_id()))
            && (this.getGoods_name() == null ? other.getGoods_name() == null : this.getGoods_name().equals(other.getGoods_name()))
            && (this.getGoods_url() == null ? other.getGoods_url() == null : this.getGoods_url().equals(other.getGoods_url()))
            && (this.getGoods_id() == null ? other.getGoods_id() == null : this.getGoods_id().equals(other.getGoods_id()))
            && (this.getSku_id() == null ? other.getSku_id() == null : this.getSku_id().equals(other.getSku_id()))
            && (this.getGroup_id() == null ? other.getGroup_id() == null : this.getGroup_id().equals(other.getGroup_id()))
            && (this.getNormal_price() == null ? other.getNormal_price() == null : this.getNormal_price().equals(other.getNormal_price()))
            && (this.getError_count() == null ? other.getError_count() == null : this.getError_count().equals(other.getError_count()))
            && (this.getIs_store_limit() == null ? other.getIs_store_limit() == null : this.getIs_store_limit().equals(other.getIs_store_limit()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getLast_use_time() == null ? other.getLast_use_time() == null : this.getLast_use_time().equals(other.getLast_use_time()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getIs_upper() == null ? other.getIs_upper() == null : this.getIs_upper().equals(other.getIs_upper()))
            && (this.getC_id() == null ? other.getC_id() == null : this.getC_id().equals(other.getC_id()))
            && (this.getD_id() == null ? other.getD_id() == null : this.getD_id().equals(other.getD_id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAdmin_uid() == null) ? 0 : getAdmin_uid().hashCode());
        result = prime * result + ((getStores_id() == null) ? 0 : getStores_id().hashCode());
        result = prime * result + ((getGoods_name() == null) ? 0 : getGoods_name().hashCode());
        result = prime * result + ((getGoods_url() == null) ? 0 : getGoods_url().hashCode());
        result = prime * result + ((getGoods_id() == null) ? 0 : getGoods_id().hashCode());
        result = prime * result + ((getSku_id() == null) ? 0 : getSku_id().hashCode());
        result = prime * result + ((getGroup_id() == null) ? 0 : getGroup_id().hashCode());
        result = prime * result + ((getNormal_price() == null) ? 0 : getNormal_price().hashCode());
        result = prime * result + ((getError_count() == null) ? 0 : getError_count().hashCode());
        result = prime * result + ((getIs_store_limit() == null) ? 0 : getIs_store_limit().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getLast_use_time() == null) ? 0 : getLast_use_time().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getIs_upper() == null) ? 0 : getIs_upper().hashCode());
        result = prime * result + ((getC_id() == null) ? 0 : getC_id().hashCode());
        result = prime * result + ((getD_id() == null) ? 0 : getD_id().hashCode());
        return result;
    }
}