package org.xxpay.core.entity;

import java.io.Serializable;

public class Nginx implements Serializable {
    private Integer id;

    /**
     * nginx ip 地址
     *
     * @mbggenerated
     */
    private String nginxIP;

    /**
     * nginx 端口
     *
     * @mbggenerated
     */
    private String nginxPort;

    /**
     * 0 --不可用 1--可用
     *
     * @mbggenerated
     */
    private String status;

    /**
     * 权重
     *
     * @mbggenerated
     */
    private Integer weights;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNginxIP() {
        return nginxIP;
    }

    public void setNginxIP(String nginxIP) {
        this.nginxIP = nginxIP;
    }

    public String getNginxPort() {
        return nginxPort;
    }

    public void setNginxPort(String nginxPort) {
        this.nginxPort = nginxPort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWeights() {
        return weights;
    }

    public void setWeights(Integer weights) {
        this.weights = weights;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", nginxIP=").append(nginxIP);
        sb.append(", nginxPort=").append(nginxPort);
        sb.append(", status=").append(status);
        sb.append(", weights=").append(weights);
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
        Nginx other = (Nginx) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNginxIP() == null ? other.getNginxIP() == null : this.getNginxIP().equals(other.getNginxIP()))
            && (this.getNginxPort() == null ? other.getNginxPort() == null : this.getNginxPort().equals(other.getNginxPort()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getWeights() == null ? other.getWeights() == null : this.getWeights().equals(other.getWeights()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNginxIP() == null) ? 0 : getNginxIP().hashCode());
        result = prime * result + ((getNginxPort() == null) ? 0 : getNginxPort().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getWeights() == null) ? 0 : getWeights().hashCode());
        return result;
    }
}