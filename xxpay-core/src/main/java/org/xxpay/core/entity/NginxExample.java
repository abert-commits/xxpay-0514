package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NginxExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public NginxExample() {
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

        public Criteria andNginxIPIsNull() {
            addCriterion("nginxIP is null");
            return (Criteria) this;
        }

        public Criteria andNginxIPIsNotNull() {
            addCriterion("nginxIP is not null");
            return (Criteria) this;
        }

        public Criteria andNginxIPEqualTo(String value) {
            addCriterion("nginxIP =", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPNotEqualTo(String value) {
            addCriterion("nginxIP <>", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPGreaterThan(String value) {
            addCriterion("nginxIP >", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPGreaterThanOrEqualTo(String value) {
            addCriterion("nginxIP >=", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPLessThan(String value) {
            addCriterion("nginxIP <", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPLessThanOrEqualTo(String value) {
            addCriterion("nginxIP <=", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPLike(String value) {
            addCriterion("nginxIP like", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPNotLike(String value) {
            addCriterion("nginxIP not like", value, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPIn(List<String> values) {
            addCriterion("nginxIP in", values, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPNotIn(List<String> values) {
            addCriterion("nginxIP not in", values, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPBetween(String value1, String value2) {
            addCriterion("nginxIP between", value1, value2, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxIPNotBetween(String value1, String value2) {
            addCriterion("nginxIP not between", value1, value2, "nginxIP");
            return (Criteria) this;
        }

        public Criteria andNginxPortIsNull() {
            addCriterion("nginxPort is null");
            return (Criteria) this;
        }

        public Criteria andNginxPortIsNotNull() {
            addCriterion("nginxPort is not null");
            return (Criteria) this;
        }

        public Criteria andNginxPortEqualTo(String value) {
            addCriterion("nginxPort =", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortNotEqualTo(String value) {
            addCriterion("nginxPort <>", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortGreaterThan(String value) {
            addCriterion("nginxPort >", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortGreaterThanOrEqualTo(String value) {
            addCriterion("nginxPort >=", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortLessThan(String value) {
            addCriterion("nginxPort <", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortLessThanOrEqualTo(String value) {
            addCriterion("nginxPort <=", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortLike(String value) {
            addCriterion("nginxPort like", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortNotLike(String value) {
            addCriterion("nginxPort not like", value, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortIn(List<String> values) {
            addCriterion("nginxPort in", values, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortNotIn(List<String> values) {
            addCriterion("nginxPort not in", values, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortBetween(String value1, String value2) {
            addCriterion("nginxPort between", value1, value2, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andNginxPortNotBetween(String value1, String value2) {
            addCriterion("nginxPort not between", value1, value2, "nginxPort");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andWeightsIsNull() {
            addCriterion("weights is null");
            return (Criteria) this;
        }

        public Criteria andWeightsIsNotNull() {
            addCriterion("weights is not null");
            return (Criteria) this;
        }

        public Criteria andWeightsEqualTo(Integer value) {
            addCriterion("weights =", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsNotEqualTo(Integer value) {
            addCriterion("weights <>", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsGreaterThan(Integer value) {
            addCriterion("weights >", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsGreaterThanOrEqualTo(Integer value) {
            addCriterion("weights >=", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsLessThan(Integer value) {
            addCriterion("weights <", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsLessThanOrEqualTo(Integer value) {
            addCriterion("weights <=", value, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsIn(List<Integer> values) {
            addCriterion("weights in", values, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsNotIn(List<Integer> values) {
            addCriterion("weights not in", values, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsBetween(Integer value1, Integer value2) {
            addCriterion("weights between", value1, value2, "weights");
            return (Criteria) this;
        }

        public Criteria andWeightsNotBetween(Integer value1, Integer value2) {
            addCriterion("weights not between", value1, value2, "weights");
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