package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PinduoduoAddressExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public PinduoduoAddressExample() {
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

        public Criteria andAddress_provinceIsNull() {
            addCriterion("address_province is null");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceIsNotNull() {
            addCriterion("address_province is not null");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceEqualTo(Short value) {
            addCriterion("address_province =", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceNotEqualTo(Short value) {
            addCriterion("address_province <>", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceGreaterThan(Short value) {
            addCriterion("address_province >", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceGreaterThanOrEqualTo(Short value) {
            addCriterion("address_province >=", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceLessThan(Short value) {
            addCriterion("address_province <", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceLessThanOrEqualTo(Short value) {
            addCriterion("address_province <=", value, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceIn(List<Short> values) {
            addCriterion("address_province in", values, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceNotIn(List<Short> values) {
            addCriterion("address_province not in", values, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceBetween(Short value1, Short value2) {
            addCriterion("address_province between", value1, value2, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_provinceNotBetween(Short value1, Short value2) {
            addCriterion("address_province not between", value1, value2, "address_province");
            return (Criteria) this;
        }

        public Criteria andAddress_cityIsNull() {
            addCriterion("address_city is null");
            return (Criteria) this;
        }

        public Criteria andAddress_cityIsNotNull() {
            addCriterion("address_city is not null");
            return (Criteria) this;
        }

        public Criteria andAddress_cityEqualTo(Short value) {
            addCriterion("address_city =", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityNotEqualTo(Short value) {
            addCriterion("address_city <>", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityGreaterThan(Short value) {
            addCriterion("address_city >", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityGreaterThanOrEqualTo(Short value) {
            addCriterion("address_city >=", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityLessThan(Short value) {
            addCriterion("address_city <", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityLessThanOrEqualTo(Short value) {
            addCriterion("address_city <=", value, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityIn(List<Short> values) {
            addCriterion("address_city in", values, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityNotIn(List<Short> values) {
            addCriterion("address_city not in", values, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityBetween(Short value1, Short value2) {
            addCriterion("address_city between", value1, value2, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_cityNotBetween(Short value1, Short value2) {
            addCriterion("address_city not between", value1, value2, "address_city");
            return (Criteria) this;
        }

        public Criteria andAddress_districtIsNull() {
            addCriterion("address_district is null");
            return (Criteria) this;
        }

        public Criteria andAddress_districtIsNotNull() {
            addCriterion("address_district is not null");
            return (Criteria) this;
        }

        public Criteria andAddress_districtEqualTo(Short value) {
            addCriterion("address_district =", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtNotEqualTo(Short value) {
            addCriterion("address_district <>", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtGreaterThan(Short value) {
            addCriterion("address_district >", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtGreaterThanOrEqualTo(Short value) {
            addCriterion("address_district >=", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtLessThan(Short value) {
            addCriterion("address_district <", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtLessThanOrEqualTo(Short value) {
            addCriterion("address_district <=", value, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtIn(List<Short> values) {
            addCriterion("address_district in", values, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtNotIn(List<Short> values) {
            addCriterion("address_district not in", values, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtBetween(Short value1, Short value2) {
            addCriterion("address_district between", value1, value2, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_districtNotBetween(Short value1, Short value2) {
            addCriterion("address_district not between", value1, value2, "address_district");
            return (Criteria) this;
        }

        public Criteria andAddress_concretIsNull() {
            addCriterion("address_concret is null");
            return (Criteria) this;
        }

        public Criteria andAddress_concretIsNotNull() {
            addCriterion("address_concret is not null");
            return (Criteria) this;
        }

        public Criteria andAddress_concretEqualTo(String value) {
            addCriterion("address_concret =", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretNotEqualTo(String value) {
            addCriterion("address_concret <>", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretGreaterThan(String value) {
            addCriterion("address_concret >", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretGreaterThanOrEqualTo(String value) {
            addCriterion("address_concret >=", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretLessThan(String value) {
            addCriterion("address_concret <", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretLessThanOrEqualTo(String value) {
            addCriterion("address_concret <=", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretLike(String value) {
            addCriterion("address_concret like", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretNotLike(String value) {
            addCriterion("address_concret not like", value, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretIn(List<String> values) {
            addCriterion("address_concret in", values, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretNotIn(List<String> values) {
            addCriterion("address_concret not in", values, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretBetween(String value1, String value2) {
            addCriterion("address_concret between", value1, value2, "address_concret");
            return (Criteria) this;
        }

        public Criteria andAddress_concretNotBetween(String value1, String value2) {
            addCriterion("address_concret not between", value1, value2, "address_concret");
            return (Criteria) this;
        }

        public Criteria andDateIsNull() {
            addCriterion("date is null");
            return (Criteria) this;
        }

        public Criteria andDateIsNotNull() {
            addCriterion("date is not null");
            return (Criteria) this;
        }

        public Criteria andDateEqualTo(Date value) {
            addCriterion("date =", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateNotEqualTo(Date value) {
            addCriterion("date <>", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateGreaterThan(Date value) {
            addCriterion("date >", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateGreaterThanOrEqualTo(Date value) {
            addCriterion("date >=", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateLessThan(Date value) {
            addCriterion("date <", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateLessThanOrEqualTo(Date value) {
            addCriterion("date <=", value, "date");
            return (Criteria) this;
        }

        public Criteria andDateIn(List<Date> values) {
            addCriterion("date in", values, "date");
            return (Criteria) this;
        }

        public Criteria andDateNotIn(List<Date> values) {
            addCriterion("date not in", values, "date");
            return (Criteria) this;
        }

        public Criteria andDateBetween(Date value1, Date value2) {
            addCriterion("date between", value1, value2, "date");
            return (Criteria) this;
        }

        public Criteria andDateNotBetween(Date value1, Date value2) {
            addCriterion("date not between", value1, value2, "date");
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