package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlipayConfigExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public AlipayConfigExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        return offset;
    }

    protected abstract static class GeneratedCriteria implements Serializable {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("Id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("Id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("Id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("Id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("Id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("Id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("Id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("Id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("Id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("Id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("Id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("Id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andPIDIsNull() {
            addCriterion("PID is null");
            return (Criteria) this;
        }

        public Criteria andPIDIsNotNull() {
            addCriterion("PID is not null");
            return (Criteria) this;
        }

        public Criteria andPIDEqualTo(String value) {
            addCriterion("PID =", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDNotEqualTo(String value) {
            addCriterion("PID <>", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDGreaterThan(String value) {
            addCriterion("PID >", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDGreaterThanOrEqualTo(String value) {
            addCriterion("PID >=", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDLessThan(String value) {
            addCriterion("PID <", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDLessThanOrEqualTo(String value) {
            addCriterion("PID <=", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDLike(String value) {
            addCriterion("PID like", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDNotLike(String value) {
            addCriterion("PID not like", value, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDIn(List<String> values) {
            addCriterion("PID in", values, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDNotIn(List<String> values) {
            addCriterion("PID not in", values, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDBetween(String value1, String value2) {
            addCriterion("PID between", value1, value2, "PID");
            return (Criteria) this;
        }

        public Criteria andPIDNotBetween(String value1, String value2) {
            addCriterion("PID not between", value1, value2, "PID");
            return (Criteria) this;
        }

        public Criteria andAPPIDIsNull() {
            addCriterion("APPID is null");
            return (Criteria) this;
        }

        public Criteria andAPPIDIsNotNull() {
            addCriterion("APPID is not null");
            return (Criteria) this;
        }

        public Criteria andAPPIDEqualTo(String value) {
            addCriterion("APPID =", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDNotEqualTo(String value) {
            addCriterion("APPID <>", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDGreaterThan(String value) {
            addCriterion("APPID >", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDGreaterThanOrEqualTo(String value) {
            addCriterion("APPID >=", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDLessThan(String value) {
            addCriterion("APPID <", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDLessThanOrEqualTo(String value) {
            addCriterion("APPID <=", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDLike(String value) {
            addCriterion("APPID like", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDNotLike(String value) {
            addCriterion("APPID not like", value, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDIn(List<String> values) {
            addCriterion("APPID in", values, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDNotIn(List<String> values) {
            addCriterion("APPID not in", values, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDBetween(String value1, String value2) {
            addCriterion("APPID between", value1, value2, "APPID");
            return (Criteria) this;
        }

        public Criteria andAPPIDNotBetween(String value1, String value2) {
            addCriterion("APPID not between", value1, value2, "APPID");
            return (Criteria) this;
        }

        public Criteria andAgentIdIsNull() {
            addCriterion("AgentId is null");
            return (Criteria) this;
        }

        public Criteria andAgentIdIsNotNull() {
            addCriterion("AgentId is not null");
            return (Criteria) this;
        }

        public Criteria andAgentIdEqualTo(Long value) {
            addCriterion("AgentId =", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotEqualTo(Long value) {
            addCriterion("AgentId <>", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdGreaterThan(Long value) {
            addCriterion("AgentId >", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdGreaterThanOrEqualTo(Long value) {
            addCriterion("AgentId >=", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdLessThan(Long value) {
            addCriterion("AgentId <", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdLessThanOrEqualTo(Long value) {
            addCriterion("AgentId <=", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdIn(List<Long> values) {
            addCriterion("AgentId in", values, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotIn(List<Long> values) {
            addCriterion("AgentId not in", values, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdBetween(Long value1, Long value2) {
            addCriterion("AgentId between", value1, value2, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotBetween(Long value1, Long value2) {
            addCriterion("AgentId not between", value1, value2, "agentId");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameIsNull() {
            addCriterion("AppCertPublickeyFileName is null");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameIsNotNull() {
            addCriterion("AppCertPublickeyFileName is not null");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameEqualTo(String value) {
            addCriterion("AppCertPublickeyFileName =", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameNotEqualTo(String value) {
            addCriterion("AppCertPublickeyFileName <>", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameGreaterThan(String value) {
            addCriterion("AppCertPublickeyFileName >", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("AppCertPublickeyFileName >=", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameLessThan(String value) {
            addCriterion("AppCertPublickeyFileName <", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameLessThanOrEqualTo(String value) {
            addCriterion("AppCertPublickeyFileName <=", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameLike(String value) {
            addCriterion("AppCertPublickeyFileName like", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameNotLike(String value) {
            addCriterion("AppCertPublickeyFileName not like", value, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameIn(List<String> values) {
            addCriterion("AppCertPublickeyFileName in", values, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameNotIn(List<String> values) {
            addCriterion("AppCertPublickeyFileName not in", values, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameBetween(String value1, String value2) {
            addCriterion("AppCertPublickeyFileName between", value1, value2, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAppCertPublickeyFileNameNotBetween(String value1, String value2) {
            addCriterion("AppCertPublickeyFileName not between", value1, value2, "appCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameIsNull() {
            addCriterion("AliPayCertPublickeyFileName is null");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameIsNotNull() {
            addCriterion("AliPayCertPublickeyFileName is not null");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameEqualTo(String value) {
            addCriterion("AliPayCertPublickeyFileName =", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameNotEqualTo(String value) {
            addCriterion("AliPayCertPublickeyFileName <>", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameGreaterThan(String value) {
            addCriterion("AliPayCertPublickeyFileName >", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("AliPayCertPublickeyFileName >=", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameLessThan(String value) {
            addCriterion("AliPayCertPublickeyFileName <", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameLessThanOrEqualTo(String value) {
            addCriterion("AliPayCertPublickeyFileName <=", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameLike(String value) {
            addCriterion("AliPayCertPublickeyFileName like", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameNotLike(String value) {
            addCriterion("AliPayCertPublickeyFileName not like", value, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameIn(List<String> values) {
            addCriterion("AliPayCertPublickeyFileName in", values, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameNotIn(List<String> values) {
            addCriterion("AliPayCertPublickeyFileName not in", values, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameBetween(String value1, String value2) {
            addCriterion("AliPayCertPublickeyFileName between", value1, value2, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andAliPayCertPublickeyFileNameNotBetween(String value1, String value2) {
            addCriterion("AliPayCertPublickeyFileName not between", value1, value2, "aliPayCertPublickeyFileName");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyIsNull() {
            addCriterion("RSAPrivateKey is null");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyIsNotNull() {
            addCriterion("RSAPrivateKey is not null");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyEqualTo(String value) {
            addCriterion("RSAPrivateKey =", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyNotEqualTo(String value) {
            addCriterion("RSAPrivateKey <>", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyGreaterThan(String value) {
            addCriterion("RSAPrivateKey >", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyGreaterThanOrEqualTo(String value) {
            addCriterion("RSAPrivateKey >=", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyLessThan(String value) {
            addCriterion("RSAPrivateKey <", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyLessThanOrEqualTo(String value) {
            addCriterion("RSAPrivateKey <=", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyLike(String value) {
            addCriterion("RSAPrivateKey like", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyNotLike(String value) {
            addCriterion("RSAPrivateKey not like", value, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyIn(List<String> values) {
            addCriterion("RSAPrivateKey in", values, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyNotIn(List<String> values) {
            addCriterion("RSAPrivateKey not in", values, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyBetween(String value1, String value2) {
            addCriterion("RSAPrivateKey between", value1, value2, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRSAPrivateKeyNotBetween(String value1, String value2) {
            addCriterion("RSAPrivateKey not between", value1, value2, "RSAPrivateKey");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("UpdateTime is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("UpdateTime is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("UpdateTime =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("UpdateTime <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("UpdateTime >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("UpdateTime >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("UpdateTime <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("UpdateTime <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("UpdateTime in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("UpdateTime not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("UpdateTime between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("UpdateTime not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("CreateTime is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("CreateTime is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("CreateTime =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("CreateTime <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("CreateTime >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("CreateTime >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("CreateTime <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("CreateTime <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("CreateTime in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("CreateTime not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("CreateTime between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("CreateTime not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIsNull() {
            addCriterion("CompanyName is null");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIsNotNull() {
            addCriterion("CompanyName is not null");
            return (Criteria) this;
        }

        public Criteria andCompanyNameEqualTo(String value) {
            addCriterion("CompanyName =", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotEqualTo(String value) {
            addCriterion("CompanyName <>", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameGreaterThan(String value) {
            addCriterion("CompanyName >", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameGreaterThanOrEqualTo(String value) {
            addCriterion("CompanyName >=", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLessThan(String value) {
            addCriterion("CompanyName <", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLessThanOrEqualTo(String value) {
            addCriterion("CompanyName <=", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLike(String value) {
            addCriterion("CompanyName like", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotLike(String value) {
            addCriterion("CompanyName not like", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIn(List<String> values) {
            addCriterion("CompanyName in", values, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotIn(List<String> values) {
            addCriterion("CompanyName not in", values, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameBetween(String value1, String value2) {
            addCriterion("CompanyName between", value1, value2, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotBetween(String value1, String value2) {
            addCriterion("CompanyName not between", value1, value2, "companyName");
            return (Criteria) this;
        }

        public Criteria andEmailIsNull() {
            addCriterion("Email is null");
            return (Criteria) this;
        }

        public Criteria andEmailIsNotNull() {
            addCriterion("Email is not null");
            return (Criteria) this;
        }

        public Criteria andEmailEqualTo(String value) {
            addCriterion("Email =", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotEqualTo(String value) {
            addCriterion("Email <>", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailGreaterThan(String value) {
            addCriterion("Email >", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailGreaterThanOrEqualTo(String value) {
            addCriterion("Email >=", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLessThan(String value) {
            addCriterion("Email <", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLessThanOrEqualTo(String value) {
            addCriterion("Email <=", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLike(String value) {
            addCriterion("Email like", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotLike(String value) {
            addCriterion("Email not like", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailIn(List<String> values) {
            addCriterion("Email in", values, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotIn(List<String> values) {
            addCriterion("Email not in", values, "email");
            return (Criteria) this;
        }

        public Criteria andEmailBetween(String value1, String value2) {
            addCriterion("Email between", value1, value2, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotBetween(String value1, String value2) {
            addCriterion("Email not between", value1, value2, "email");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("Status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("Status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("Status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("Status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("Status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("Status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("Status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("Status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("Status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("Status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("Status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("Status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("Remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("Remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("Remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("Remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("Remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("Remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("Remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("Remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("Remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("Remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("Remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("Remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("Remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("Remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIsNull() {
            addCriterion("ParentAgentId is null");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIsNotNull() {
            addCriterion("ParentAgentId is not null");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdEqualTo(String value) {
            addCriterion("ParentAgentId =", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotEqualTo(String value) {
            addCriterion("ParentAgentId <>", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdGreaterThan(String value) {
            addCriterion("ParentAgentId >", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdGreaterThanOrEqualTo(String value) {
            addCriterion("ParentAgentId >=", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdLessThan(String value) {
            addCriterion("ParentAgentId <", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdLessThanOrEqualTo(String value) {
            addCriterion("ParentAgentId <=", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdLike(String value) {
            addCriterion("ParentAgentId like", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotLike(String value) {
            addCriterion("ParentAgentId not like", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIn(List<String> values) {
            addCriterion("ParentAgentId in", values, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotIn(List<String> values) {
            addCriterion("ParentAgentId not in", values, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdBetween(String value1, String value2) {
            addCriterion("ParentAgentId between", value1, value2, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotBetween(String value1, String value2) {
            addCriterion("ParentAgentId not between", value1, value2, "parentAgentId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria implements Serializable {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion implements Serializable {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}