package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.Date;

public class AlipayConfig implements Serializable {
    /**
     * 主键
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * PID
     *
     * @mbggenerated
     */
    private String PID;

    /**
     * APPid
     *
     * @mbggenerated
     */
    private String APPID;

    /**
     * 代理id
     *
     * @mbggenerated
     */
    private Long agentId;

    /**
     * 应用公钥证书文件名字
     *
     * @mbggenerated
     */
    private String appCertPublickeyFileName;

    /**
     * 支付宝公钥证书文件名字
     *
     * @mbggenerated
     */
    private String aliPayCertPublickeyFileName;

    /**
     * RSAPrivateKey
     *
     * @mbggenerated
     */
    private String RSAPrivateKey;

    /**
     * 修改时间
     *
     * @mbggenerated
     */
    private Date updateTime;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date createTime;

    /**
     * 支付宝企业号名称
     *
     * @mbggenerated
     */
    private String companyName;

    /**
     * 企业邮箱（支付宝登录账号）
     *
     * @mbggenerated
     */
    private String email;

    /**
     * 0:启用，1：禁用
     *
     * @mbggenerated
     */
    private Integer status;

    private String remark;

    private String parentAgentId;

    /**
     * 应用公钥证书
     *
     * @mbggenerated
     */
    private byte[] appCertPublickey;

    /**
     * 支付宝公钥证书
     *
     * @mbggenerated
     */
    private byte[] aliPayCertPublickey;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAppCertPublickeyFileName() {
        return appCertPublickeyFileName;
    }

    public void setAppCertPublickeyFileName(String appCertPublickeyFileName) {
        this.appCertPublickeyFileName = appCertPublickeyFileName;
    }

    public String getAliPayCertPublickeyFileName() {
        return aliPayCertPublickeyFileName;
    }

    public void setAliPayCertPublickeyFileName(String aliPayCertPublickeyFileName) {
        this.aliPayCertPublickeyFileName = aliPayCertPublickeyFileName;
    }

    public String getRSAPrivateKey() {
        return RSAPrivateKey;
    }

    public void setRSAPrivateKey(String RSAPrivateKey) {
        this.RSAPrivateKey = RSAPrivateKey;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getParentAgentId() {
        return parentAgentId;
    }

    public void setParentAgentId(String parentAgentId) {
        this.parentAgentId = parentAgentId;
    }

    public byte[] getAppCertPublickey() {
        return appCertPublickey;
    }

    public void setAppCertPublickey(byte[] appCertPublickey) {
        this.appCertPublickey = appCertPublickey;
    }

    public byte[] getAliPayCertPublickey() {
        return aliPayCertPublickey;
    }

    public void setAliPayCertPublickey(byte[] aliPayCertPublickey) {
        this.aliPayCertPublickey = aliPayCertPublickey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", PID=").append(PID);
        sb.append(", APPID=").append(APPID);
        sb.append(", agentId=").append(agentId);
        sb.append(", appCertPublickeyFileName=").append(appCertPublickeyFileName);
        sb.append(", aliPayCertPublickeyFileName=").append(aliPayCertPublickeyFileName);
        sb.append(", RSAPrivateKey=").append(RSAPrivateKey);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", companyName=").append(companyName);
        sb.append(", email=").append(email);
        sb.append(", status=").append(status);
        sb.append(", remark=").append(remark);
        sb.append(", parentAgentId=").append(parentAgentId);
        sb.append(", appCertPublickey=").append(appCertPublickey);
        sb.append(", aliPayCertPublickey=").append(aliPayCertPublickey);
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
        AlipayConfig other = (AlipayConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPID() == null ? other.getPID() == null : this.getPID().equals(other.getPID()))
            && (this.getAPPID() == null ? other.getAPPID() == null : this.getAPPID().equals(other.getAPPID()))
            && (this.getAgentId() == null ? other.getAgentId() == null : this.getAgentId().equals(other.getAgentId()))
            && (this.getAppCertPublickeyFileName() == null ? other.getAppCertPublickeyFileName() == null : this.getAppCertPublickeyFileName().equals(other.getAppCertPublickeyFileName()))
            && (this.getAliPayCertPublickeyFileName() == null ? other.getAliPayCertPublickeyFileName() == null : this.getAliPayCertPublickeyFileName().equals(other.getAliPayCertPublickeyFileName()))
            && (this.getRSAPrivateKey() == null ? other.getRSAPrivateKey() == null : this.getRSAPrivateKey().equals(other.getRSAPrivateKey()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getCompanyName() == null ? other.getCompanyName() == null : this.getCompanyName().equals(other.getCompanyName()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getParentAgentId() == null ? other.getParentAgentId() == null : this.getParentAgentId().equals(other.getParentAgentId()))
            && (this.getAppCertPublickey() == null ? other.getAppCertPublickey() == null : this.getAppCertPublickey().equals(other.getAppCertPublickey()))
            && (this.getAliPayCertPublickey() == null ? other.getAliPayCertPublickey() == null : this.getAliPayCertPublickey().equals(other.getAliPayCertPublickey()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPID() == null) ? 0 : getPID().hashCode());
        result = prime * result + ((getAPPID() == null) ? 0 : getAPPID().hashCode());
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + ((getAppCertPublickeyFileName() == null) ? 0 : getAppCertPublickeyFileName().hashCode());
        result = prime * result + ((getAliPayCertPublickeyFileName() == null) ? 0 : getAliPayCertPublickeyFileName().hashCode());
        result = prime * result + ((getRSAPrivateKey() == null) ? 0 : getRSAPrivateKey().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCompanyName() == null) ? 0 : getCompanyName().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getParentAgentId() == null) ? 0 : getParentAgentId().hashCode());
        result = prime * result + ((getAppCertPublickey() == null) ? 0 : getAppCertPublickey().hashCode());
        result = prime * result + ((getAliPayCertPublickey() == null) ? 0 : getAliPayCertPublickey().hashCode());
        return result;
    }
}