package org.xxpay.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PayCashCollConfigExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public PayCashCollConfigExample() {
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
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameIsNull() {
            addCriterion("TransInUserName is null");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameIsNotNull() {
            addCriterion("TransInUserName is not null");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameEqualTo(String value) {
            addCriterion("TransInUserName =", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameNotEqualTo(String value) {
            addCriterion("TransInUserName <>", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameGreaterThan(String value) {
            addCriterion("TransInUserName >", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("TransInUserName >=", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameLessThan(String value) {
            addCriterion("TransInUserName <", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameLessThanOrEqualTo(String value) {
            addCriterion("TransInUserName <=", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameLike(String value) {
            addCriterion("TransInUserName like", value, "transInUserName");
            return (Criteria) this;
        }


        public Criteria andRemarkLike(String value) {
            addCriterion("Remark like", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameNotLike(String value) {
            addCriterion("TransInUserName not like", value, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameIn(List<String> values) {
            addCriterion("TransInUserName in", values, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameNotIn(List<String> values) {
            addCriterion("TransInUserName not in", values, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameBetween(String value1, String value2) {
            addCriterion("TransInUserName between", value1, value2, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserNameNotBetween(String value1, String value2) {
            addCriterion("TransInUserName not between", value1, value2, "transInUserName");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountIsNull() {
            addCriterion("TransInUserAccount is null");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountIsNotNull() {
            addCriterion("TransInUserAccount is not null");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountEqualTo(String value) {
            addCriterion("TransInUserAccount =", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountNotEqualTo(String value) {
            addCriterion("TransInUserAccount <>", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountGreaterThan(String value) {
            addCriterion("TransInUserAccount >", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountGreaterThanOrEqualTo(String value) {
            addCriterion("TransInUserAccount >=", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountLessThan(String value) {
            addCriterion("TransInUserAccount <", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountLessThanOrEqualTo(String value) {
            addCriterion("TransInUserAccount <=", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountLike(String value) {
            addCriterion("TransInUserAccount like", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountNotLike(String value) {
            addCriterion("TransInUserAccount not like", value, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountIn(List<String> values) {
            addCriterion("TransInUserAccount in", values, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountNotIn(List<String> values) {
            addCriterion("TransInUserAccount not in", values, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountBetween(String value1, String value2) {
            addCriterion("TransInUserAccount between", value1, value2, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserAccountNotBetween(String value1, String value2) {
            addCriterion("TransInUserAccount not between", value1, value2, "transInUserAccount");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdIsNull() {
            addCriterion("TransInUserId is null");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdIsNotNull() {
            addCriterion("TransInUserId is not null");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdEqualTo(String value) {
            addCriterion("TransInUserId =", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdNotEqualTo(String value) {
            addCriterion("TransInUserId <>", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdGreaterThan(String value) {
            addCriterion("TransInUserId >", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("TransInUserId >=", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdLessThan(String value) {
            addCriterion("TransInUserId <", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdLessThanOrEqualTo(String value) {
            addCriterion("TransInUserId <=", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdLike(String value) {
            addCriterion("TransInUserId like", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdNotLike(String value) {
            addCriterion("TransInUserId not like", value, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdIn(List<String> values) {
            addCriterion("TransInUserId in", values, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdNotIn(List<String> values) {
            addCriterion("TransInUserId not in", values, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdBetween(String value1, String value2) {
            addCriterion("TransInUserId between", value1, value2, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInUserIdNotBetween(String value1, String value2) {
            addCriterion("TransInUserId not between", value1, value2, "transInUserId");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageIsNull() {
            addCriterion("TransInPercentage is null");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageIsNotNull() {
            addCriterion("TransInPercentage is not null");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageEqualTo(BigDecimal value) {
            addCriterion("TransInPercentage =", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageNotEqualTo(BigDecimal value) {
            addCriterion("TransInPercentage <>", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageGreaterThan(BigDecimal value) {
            addCriterion("TransInPercentage >", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("TransInPercentage >=", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageLessThan(BigDecimal value) {
            addCriterion("TransInPercentage <", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageLessThanOrEqualTo(BigDecimal value) {
            addCriterion("TransInPercentage <=", value, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageIn(List<BigDecimal> values) {
            addCriterion("TransInPercentage in", values, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageNotIn(List<BigDecimal> values) {
            addCriterion("TransInPercentage not in", values, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("TransInPercentage between", value1, value2, "transInPercentage");
            return (Criteria) this;
        }

        public Criteria andTransInPercentageNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("TransInPercentage not between", value1, value2, "transInPercentage");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("Status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("Status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("Status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("Status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("Status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("Status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("Status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("Status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("Status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("Status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdIsNull() {
            addCriterion("BelongPayAccountId is null");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdIsNotNull() {
            addCriterion("BelongPayAccountId is not null");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdEqualTo(Integer value) {
            addCriterion("BelongPayAccountId =", value, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdNotEqualTo(Integer value) {
            addCriterion("BelongPayAccountId <>", value, "belongPayAccountId");
            return (Criteria) this;
        }


        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("Type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdGreaterThan(Integer value) {
            addCriterion("BelongPayAccountId >", value, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("BelongPayAccountId >=", value, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdLessThan(Integer value) {
            addCriterion("BelongPayAccountId <", value, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdLessThanOrEqualTo(Integer value) {
            addCriterion("BelongPayAccountId <=", value, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdIn(List<Integer> values) {
            addCriterion("BelongPayAccountId in", values, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdNotIn(List<Integer> values) {
            addCriterion("BelongPayAccountId not in", values, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdBetween(Integer value1, Integer value2) {
            addCriterion("BelongPayAccountId between", value1, value2, "belongPayAccountId");
            return (Criteria) this;
        }

        public Criteria andBelongPayAccountIdNotBetween(Integer value1, Integer value2) {
            addCriterion("BelongPayAccountId not between", value1, value2, "belongPayAccountId");
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