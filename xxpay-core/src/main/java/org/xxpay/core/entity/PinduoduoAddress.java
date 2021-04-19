package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class PinduoduoAddress implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 省份id
     *
     * @mbggenerated
     */
    private Short address_province;

    /**
     * 城市id
     *
     * @mbggenerated
     */
    private Short address_city;

    /**
     * 区县id
     *
     * @mbggenerated
     */
    private Short address_district;

    /**
     * 详细地址
     *
     * @mbggenerated
     */
    private String address_concret;

    /**
     * 使用时间
     *
     * @mbggenerated
     */
    private Date date;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getAddress_province() {
        return address_province;
    }

    public void setAddress_province(Short address_province) {
        this.address_province = address_province;
    }

    public Short getAddress_city() {
        return address_city;
    }

    public void setAddress_city(Short address_city) {
        this.address_city = address_city;
    }

    public Short getAddress_district() {
        return address_district;
    }

    public void setAddress_district(Short address_district) {
        this.address_district = address_district;
    }

    public String getAddress_concret() {
        return address_concret;
    }

    public void setAddress_concret(String address_concret) {
        this.address_concret = address_concret;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", address_province=").append(address_province);
        sb.append(", address_city=").append(address_city);
        sb.append(", address_district=").append(address_district);
        sb.append(", address_concret=").append(address_concret);
        sb.append(", date=").append(date);
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
        PinduoduoAddress other = (PinduoduoAddress) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAddress_province() == null ? other.getAddress_province() == null : this.getAddress_province().equals(other.getAddress_province()))
            && (this.getAddress_city() == null ? other.getAddress_city() == null : this.getAddress_city().equals(other.getAddress_city()))
            && (this.getAddress_district() == null ? other.getAddress_district() == null : this.getAddress_district().equals(other.getAddress_district()))
            && (this.getAddress_concret() == null ? other.getAddress_concret() == null : this.getAddress_concret().equals(other.getAddress_concret()))
            && (this.getDate() == null ? other.getDate() == null : this.getDate().equals(other.getDate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAddress_province() == null) ? 0 : getAddress_province().hashCode());
        result = prime * result + ((getAddress_city() == null) ? 0 : getAddress_city().hashCode());
        result = prime * result + ((getAddress_district() == null) ? 0 : getAddress_district().hashCode());
        result = prime * result + ((getAddress_concret() == null) ? 0 : getAddress_concret().hashCode());
        result = prime * result + ((getDate() == null) ? 0 : getDate().hashCode());
        return result;
    }
}