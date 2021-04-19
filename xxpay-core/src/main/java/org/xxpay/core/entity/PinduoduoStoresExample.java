package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PinduoduoStoresExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public PinduoduoStoresExample() {
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

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidIsNull() {
            addCriterion("admin_uid is null");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidIsNotNull() {
            addCriterion("admin_uid is not null");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidEqualTo(Integer value) {
            addCriterion("admin_uid =", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidNotEqualTo(Integer value) {
            addCriterion("admin_uid <>", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidGreaterThan(Integer value) {
            addCriterion("admin_uid >", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidGreaterThanOrEqualTo(Integer value) {
            addCriterion("admin_uid >=", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidLessThan(Integer value) {
            addCriterion("admin_uid <", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidLessThanOrEqualTo(Integer value) {
            addCriterion("admin_uid <=", value, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidIn(List<Integer> values) {
            addCriterion("admin_uid in", values, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidNotIn(List<Integer> values) {
            addCriterion("admin_uid not in", values, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidBetween(Integer value1, Integer value2) {
            addCriterion("admin_uid between", value1, value2, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andAdmin_uidNotBetween(Integer value1, Integer value2) {
            addCriterion("admin_uid not between", value1, value2, "admin_uid");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalIsNull() {
            addCriterion("store_remain_total is null");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalIsNotNull() {
            addCriterion("store_remain_total is not null");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalEqualTo(Long value) {
            addCriterion("store_remain_total =", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalNotEqualTo(Long value) {
            addCriterion("store_remain_total <>", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalGreaterThan(Long value) {
            addCriterion("store_remain_total >", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalGreaterThanOrEqualTo(Long value) {
            addCriterion("store_remain_total >=", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalLessThan(Long value) {
            addCriterion("store_remain_total <", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalLessThanOrEqualTo(Long value) {
            addCriterion("store_remain_total <=", value, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalIn(List<Long> values) {
            addCriterion("store_remain_total in", values, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalNotIn(List<Long> values) {
            addCriterion("store_remain_total not in", values, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalBetween(Long value1, Long value2) {
            addCriterion("store_remain_total between", value1, value2, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andStore_remain_totalNotBetween(Long value1, Long value2) {
            addCriterion("store_remain_total not between", value1, value2, "store_remain_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalIsNull() {
            addCriterion("order_total is null");
            return (Criteria) this;
        }

        public Criteria andOrder_totalIsNotNull() {
            addCriterion("order_total is not null");
            return (Criteria) this;
        }

        public Criteria andOrder_totalEqualTo(Long value) {
            addCriterion("order_total =", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalNotEqualTo(Long value) {
            addCriterion("order_total <>", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalGreaterThan(Long value) {
            addCriterion("order_total >", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalGreaterThanOrEqualTo(Long value) {
            addCriterion("order_total >=", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalLessThan(Long value) {
            addCriterion("order_total <", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalLessThanOrEqualTo(Long value) {
            addCriterion("order_total <=", value, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalIn(List<Long> values) {
            addCriterion("order_total in", values, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalNotIn(List<Long> values) {
            addCriterion("order_total not in", values, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalBetween(Long value1, Long value2) {
            addCriterion("order_total between", value1, value2, "order_total");
            return (Criteria) this;
        }

        public Criteria andOrder_totalNotBetween(Long value1, Long value2) {
            addCriterion("order_total not between", value1, value2, "order_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalIsNull() {
            addCriterion("cur_total is null");
            return (Criteria) this;
        }

        public Criteria andCur_totalIsNotNull() {
            addCriterion("cur_total is not null");
            return (Criteria) this;
        }

        public Criteria andCur_totalEqualTo(Long value) {
            addCriterion("cur_total =", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalNotEqualTo(Long value) {
            addCriterion("cur_total <>", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalGreaterThan(Long value) {
            addCriterion("cur_total >", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalGreaterThanOrEqualTo(Long value) {
            addCriterion("cur_total >=", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalLessThan(Long value) {
            addCriterion("cur_total <", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalLessThanOrEqualTo(Long value) {
            addCriterion("cur_total <=", value, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalIn(List<Long> values) {
            addCriterion("cur_total in", values, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalNotIn(List<Long> values) {
            addCriterion("cur_total not in", values, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalBetween(Long value1, Long value2) {
            addCriterion("cur_total between", value1, value2, "cur_total");
            return (Criteria) this;
        }

        public Criteria andCur_totalNotBetween(Long value1, Long value2) {
            addCriterion("cur_total not between", value1, value2, "cur_total");
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

        public Criteria andStatusEqualTo(Boolean value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Boolean value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Boolean value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Boolean value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Boolean value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Boolean value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Boolean> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Boolean> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Boolean value1, Boolean value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Boolean value1, Boolean value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCtimeIsNull() {
            addCriterion("ctime is null");
            return (Criteria) this;
        }

        public Criteria andCtimeIsNotNull() {
            addCriterion("ctime is not null");
            return (Criteria) this;
        }

        public Criteria andCtimeEqualTo(Date value) {
            addCriterion("ctime =", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeNotEqualTo(Date value) {
            addCriterion("ctime <>", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeGreaterThan(Date value) {
            addCriterion("ctime >", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeGreaterThanOrEqualTo(Date value) {
            addCriterion("ctime >=", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeLessThan(Date value) {
            addCriterion("ctime <", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeLessThanOrEqualTo(Date value) {
            addCriterion("ctime <=", value, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeIn(List<Date> values) {
            addCriterion("ctime in", values, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeNotIn(List<Date> values) {
            addCriterion("ctime not in", values, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeBetween(Date value1, Date value2) {
            addCriterion("ctime between", value1, value2, "ctime");
            return (Criteria) this;
        }

        public Criteria andCtimeNotBetween(Date value1, Date value2) {
            addCriterion("ctime not between", value1, value2, "ctime");
            return (Criteria) this;
        }

        public Criteria andMtimeIsNull() {
            addCriterion("mtime is null");
            return (Criteria) this;
        }

        public Criteria andMtimeIsNotNull() {
            addCriterion("mtime is not null");
            return (Criteria) this;
        }

        public Criteria andMtimeEqualTo(Date value) {
            addCriterion("mtime =", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeNotEqualTo(Date value) {
            addCriterion("mtime <>", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeGreaterThan(Date value) {
            addCriterion("mtime >", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeGreaterThanOrEqualTo(Date value) {
            addCriterion("mtime >=", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeLessThan(Date value) {
            addCriterion("mtime <", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeLessThanOrEqualTo(Date value) {
            addCriterion("mtime <=", value, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeIn(List<Date> values) {
            addCriterion("mtime in", values, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeNotIn(List<Date> values) {
            addCriterion("mtime not in", values, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeBetween(Date value1, Date value2) {
            addCriterion("mtime between", value1, value2, "mtime");
            return (Criteria) this;
        }

        public Criteria andMtimeNotBetween(Date value1, Date value2) {
            addCriterion("mtime not between", value1, value2, "mtime");
            return (Criteria) this;
        }

        public Criteria andC_idIsNull() {
            addCriterion("c_id is null");
            return (Criteria) this;
        }

        public Criteria andC_idIsNotNull() {
            addCriterion("c_id is not null");
            return (Criteria) this;
        }

        public Criteria andC_idEqualTo(Integer value) {
            addCriterion("c_id =", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idNotEqualTo(Integer value) {
            addCriterion("c_id <>", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idGreaterThan(Integer value) {
            addCriterion("c_id >", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idGreaterThanOrEqualTo(Integer value) {
            addCriterion("c_id >=", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idLessThan(Integer value) {
            addCriterion("c_id <", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idLessThanOrEqualTo(Integer value) {
            addCriterion("c_id <=", value, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idIn(List<Integer> values) {
            addCriterion("c_id in", values, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idNotIn(List<Integer> values) {
            addCriterion("c_id not in", values, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idBetween(Integer value1, Integer value2) {
            addCriterion("c_id between", value1, value2, "c_id");
            return (Criteria) this;
        }

        public Criteria andC_idNotBetween(Integer value1, Integer value2) {
            addCriterion("c_id not between", value1, value2, "c_id");
            return (Criteria) this;
        }

        public Criteria andD_idIsNull() {
            addCriterion("d_id is null");
            return (Criteria) this;
        }

        public Criteria andD_idIsNotNull() {
            addCriterion("d_id is not null");
            return (Criteria) this;
        }

        public Criteria andD_idEqualTo(Integer value) {
            addCriterion("d_id =", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idNotEqualTo(Integer value) {
            addCriterion("d_id <>", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idGreaterThan(Integer value) {
            addCriterion("d_id >", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idGreaterThanOrEqualTo(Integer value) {
            addCriterion("d_id >=", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idLessThan(Integer value) {
            addCriterion("d_id <", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idLessThanOrEqualTo(Integer value) {
            addCriterion("d_id <=", value, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idIn(List<Integer> values) {
            addCriterion("d_id in", values, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idNotIn(List<Integer> values) {
            addCriterion("d_id not in", values, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idBetween(Integer value1, Integer value2) {
            addCriterion("d_id between", value1, value2, "d_id");
            return (Criteria) this;
        }

        public Criteria andD_idNotBetween(Integer value1, Integer value2) {
            addCriterion("d_id not between", value1, value2, "d_id");
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