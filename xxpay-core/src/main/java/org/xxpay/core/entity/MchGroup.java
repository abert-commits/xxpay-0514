package org.xxpay.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class MchGroup implements Serializable {
    private Integer groupId;
    private  String groupName;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    private BigDecimal rate;
}

