package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PinduoduoUserExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public PinduoduoUserExample() {
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

        public Criteria andPhoneIsNull() {
            addCriterion("phone is null");
            return (Criteria) this;
        }

        public Criteria andPhoneIsNotNull() {
            addCriterion("phone is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneEqualTo(String value) {
            addCriterion("phone =", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotEqualTo(String value) {
            addCriterion("phone <>", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThan(String value) {
            addCriterion("phone >", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("phone >=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThan(String value) {
            addCriterion("phone <", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThanOrEqualTo(String value) {
            addCriterion("phone <=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLike(String value) {
            addCriterion("phone like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotLike(String value) {
            addCriterion("phone not like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneIn(List<String> values) {
            addCriterion("phone in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotIn(List<String> values) {
            addCriterion("phone not in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneBetween(String value1, String value2) {
            addCriterion("phone between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotBetween(String value1, String value2) {
            addCriterion("phone not between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenIsNull() {
            addCriterion("access_token is null");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenIsNotNull() {
            addCriterion("access_token is not null");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenEqualTo(String value) {
            addCriterion("access_token =", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenNotEqualTo(String value) {
            addCriterion("access_token <>", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenGreaterThan(String value) {
            addCriterion("access_token >", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenGreaterThanOrEqualTo(String value) {
            addCriterion("access_token >=", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenLessThan(String value) {
            addCriterion("access_token <", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenLessThanOrEqualTo(String value) {
            addCriterion("access_token <=", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenLike(String value) {
            addCriterion("access_token like", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenNotLike(String value) {
            addCriterion("access_token not like", value, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenIn(List<String> values) {
            addCriterion("access_token in", values, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenNotIn(List<String> values) {
            addCriterion("access_token not in", values, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenBetween(String value1, String value2) {
            addCriterion("access_token between", value1, value2, "access_token");
            return (Criteria) this;
        }

        public Criteria andAccess_tokenNotBetween(String value1, String value2) {
            addCriterion("access_token not between", value1, value2, "access_token");
            return (Criteria) this;
        }

        public Criteria andAcidIsNull() {
            addCriterion("acid is null");
            return (Criteria) this;
        }

        public Criteria andAcidIsNotNull() {
            addCriterion("acid is not null");
            return (Criteria) this;
        }

        public Criteria andAcidEqualTo(String value) {
            addCriterion("acid =", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidNotEqualTo(String value) {
            addCriterion("acid <>", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidGreaterThan(String value) {
            addCriterion("acid >", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidGreaterThanOrEqualTo(String value) {
            addCriterion("acid >=", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidLessThan(String value) {
            addCriterion("acid <", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidLessThanOrEqualTo(String value) {
            addCriterion("acid <=", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidLike(String value) {
            addCriterion("acid like", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidNotLike(String value) {
            addCriterion("acid not like", value, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidIn(List<String> values) {
            addCriterion("acid in", values, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidNotIn(List<String> values) {
            addCriterion("acid not in", values, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidBetween(String value1, String value2) {
            addCriterion("acid between", value1, value2, "acid");
            return (Criteria) this;
        }

        public Criteria andAcidNotBetween(String value1, String value2) {
            addCriterion("acid not between", value1, value2, "acid");
            return (Criteria) this;
        }

        public Criteria andUidIsNull() {
            addCriterion("uid is null");
            return (Criteria) this;
        }

        public Criteria andUidIsNotNull() {
            addCriterion("uid is not null");
            return (Criteria) this;
        }

        public Criteria andUidEqualTo(Long value) {
            addCriterion("uid =", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotEqualTo(Long value) {
            addCriterion("uid <>", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThan(Long value) {
            addCriterion("uid >", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThanOrEqualTo(Long value) {
            addCriterion("uid >=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThan(Long value) {
            addCriterion("uid <", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThanOrEqualTo(Long value) {
            addCriterion("uid <=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidIn(List<Long> values) {
            addCriterion("uid in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotIn(List<Long> values) {
            addCriterion("uid not in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidBetween(Long value1, Long value2) {
            addCriterion("uid between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotBetween(Long value1, Long value2) {
            addCriterion("uid not between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andUinIsNull() {
            addCriterion("uin is null");
            return (Criteria) this;
        }

        public Criteria andUinIsNotNull() {
            addCriterion("uin is not null");
            return (Criteria) this;
        }

        public Criteria andUinEqualTo(String value) {
            addCriterion("uin =", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotEqualTo(String value) {
            addCriterion("uin <>", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinGreaterThan(String value) {
            addCriterion("uin >", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinGreaterThanOrEqualTo(String value) {
            addCriterion("uin >=", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinLessThan(String value) {
            addCriterion("uin <", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinLessThanOrEqualTo(String value) {
            addCriterion("uin <=", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinLike(String value) {
            addCriterion("uin like", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotLike(String value) {
            addCriterion("uin not like", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinIn(List<String> values) {
            addCriterion("uin in", values, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotIn(List<String> values) {
            addCriterion("uin not in", values, "uin");
            return (Criteria) this;
        }

        public Criteria andUinBetween(String value1, String value2) {
            addCriterion("uin between", value1, value2, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotBetween(String value1, String value2) {
            addCriterion("uin not between", value1, value2, "uin");
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

        public Criteria andIpIsNull() {
            addCriterion("ip is null");
            return (Criteria) this;
        }

        public Criteria andIpIsNotNull() {
            addCriterion("ip is not null");
            return (Criteria) this;
        }

        public Criteria andIpEqualTo(String value) {
            addCriterion("ip =", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotEqualTo(String value) {
            addCriterion("ip <>", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpGreaterThan(String value) {
            addCriterion("ip >", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpGreaterThanOrEqualTo(String value) {
            addCriterion("ip >=", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpLessThan(String value) {
            addCriterion("ip <", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpLessThanOrEqualTo(String value) {
            addCriterion("ip <=", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpLike(String value) {
            addCriterion("ip like", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotLike(String value) {
            addCriterion("ip not like", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpIn(List<String> values) {
            addCriterion("ip in", values, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotIn(List<String> values) {
            addCriterion("ip not in", values, "ip");
            return (Criteria) this;
        }

        public Criteria andIpBetween(String value1, String value2) {
            addCriterion("ip between", value1, value2, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotBetween(String value1, String value2) {
            addCriterion("ip not between", value1, value2, "ip");
            return (Criteria) this;
        }

        public Criteria andIs_expiredIsNull() {
            addCriterion("is_expired is null");
            return (Criteria) this;
        }

        public Criteria andIs_expiredIsNotNull() {
            addCriterion("is_expired is not null");
            return (Criteria) this;
        }

        public Criteria andIs_expiredEqualTo(Boolean value) {
            addCriterion("is_expired =", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredNotEqualTo(Boolean value) {
            addCriterion("is_expired <>", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredGreaterThan(Boolean value) {
            addCriterion("is_expired >", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_expired >=", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredLessThan(Boolean value) {
            addCriterion("is_expired <", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredLessThanOrEqualTo(Boolean value) {
            addCriterion("is_expired <=", value, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredIn(List<Boolean> values) {
            addCriterion("is_expired in", values, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredNotIn(List<Boolean> values) {
            addCriterion("is_expired not in", values, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredBetween(Boolean value1, Boolean value2) {
            addCriterion("is_expired between", value1, value2, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_expiredNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_expired not between", value1, value2, "is_expired");
            return (Criteria) this;
        }

        public Criteria andIs_limitIsNull() {
            addCriterion("is_limit is null");
            return (Criteria) this;
        }

        public Criteria andIs_limitIsNotNull() {
            addCriterion("is_limit is not null");
            return (Criteria) this;
        }

        public Criteria andIs_limitEqualTo(Boolean value) {
            addCriterion("is_limit =", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitNotEqualTo(Boolean value) {
            addCriterion("is_limit <>", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitGreaterThan(Boolean value) {
            addCriterion("is_limit >", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_limit >=", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitLessThan(Boolean value) {
            addCriterion("is_limit <", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitLessThanOrEqualTo(Boolean value) {
            addCriterion("is_limit <=", value, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitIn(List<Boolean> values) {
            addCriterion("is_limit in", values, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitNotIn(List<Boolean> values) {
            addCriterion("is_limit not in", values, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limit between", value1, value2, "is_limit");
            return (Criteria) this;
        }

        public Criteria andIs_limitNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limit not between", value1, value2, "is_limit");
            return (Criteria) this;
        }

        public Criteria andNo_addrIsNull() {
            addCriterion("no_addr is null");
            return (Criteria) this;
        }

        public Criteria andNo_addrIsNotNull() {
            addCriterion("no_addr is not null");
            return (Criteria) this;
        }

        public Criteria andNo_addrEqualTo(Boolean value) {
            addCriterion("no_addr =", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrNotEqualTo(Boolean value) {
            addCriterion("no_addr <>", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrGreaterThan(Boolean value) {
            addCriterion("no_addr >", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrGreaterThanOrEqualTo(Boolean value) {
            addCriterion("no_addr >=", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrLessThan(Boolean value) {
            addCriterion("no_addr <", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrLessThanOrEqualTo(Boolean value) {
            addCriterion("no_addr <=", value, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrIn(List<Boolean> values) {
            addCriterion("no_addr in", values, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrNotIn(List<Boolean> values) {
            addCriterion("no_addr not in", values, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrBetween(Boolean value1, Boolean value2) {
            addCriterion("no_addr between", value1, value2, "no_addr");
            return (Criteria) this;
        }

        public Criteria andNo_addrNotBetween(Boolean value1, Boolean value2) {
            addCriterion("no_addr not between", value1, value2, "no_addr");
            return (Criteria) this;
        }

        public Criteria andUse_timeIsNull() {
            addCriterion("use_time is null");
            return (Criteria) this;
        }

        public Criteria andUse_timeIsNotNull() {
            addCriterion("use_time is not null");
            return (Criteria) this;
        }

        public Criteria andUse_timeEqualTo(Integer value) {
            addCriterion("use_time =", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeNotEqualTo(Integer value) {
            addCriterion("use_time <>", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeGreaterThan(Integer value) {
            addCriterion("use_time >", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeGreaterThanOrEqualTo(Integer value) {
            addCriterion("use_time >=", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeLessThan(Integer value) {
            addCriterion("use_time <", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeLessThanOrEqualTo(Integer value) {
            addCriterion("use_time <=", value, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeIn(List<Integer> values) {
            addCriterion("use_time in", values, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeNotIn(List<Integer> values) {
            addCriterion("use_time not in", values, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeBetween(Integer value1, Integer value2) {
            addCriterion("use_time between", value1, value2, "use_time");
            return (Criteria) this;
        }

        public Criteria andUse_timeNotBetween(Integer value1, Integer value2) {
            addCriterion("use_time not between", value1, value2, "use_time");
            return (Criteria) this;
        }

        public Criteria andToday_totalIsNull() {
            addCriterion("today_total is null");
            return (Criteria) this;
        }

        public Criteria andToday_totalIsNotNull() {
            addCriterion("today_total is not null");
            return (Criteria) this;
        }

        public Criteria andToday_totalEqualTo(Integer value) {
            addCriterion("today_total =", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalNotEqualTo(Integer value) {
            addCriterion("today_total <>", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalGreaterThan(Integer value) {
            addCriterion("today_total >", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalGreaterThanOrEqualTo(Integer value) {
            addCriterion("today_total >=", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalLessThan(Integer value) {
            addCriterion("today_total <", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalLessThanOrEqualTo(Integer value) {
            addCriterion("today_total <=", value, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalIn(List<Integer> values) {
            addCriterion("today_total in", values, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalNotIn(List<Integer> values) {
            addCriterion("today_total not in", values, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalBetween(Integer value1, Integer value2) {
            addCriterion("today_total between", value1, value2, "today_total");
            return (Criteria) this;
        }

        public Criteria andToday_totalNotBetween(Integer value1, Integer value2) {
            addCriterion("today_total not between", value1, value2, "today_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalIsNull() {
            addCriterion("is_limit_total is null");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalIsNotNull() {
            addCriterion("is_limit_total is not null");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalEqualTo(Boolean value) {
            addCriterion("is_limit_total =", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalNotEqualTo(Boolean value) {
            addCriterion("is_limit_total <>", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalGreaterThan(Boolean value) {
            addCriterion("is_limit_total >", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_limit_total >=", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalLessThan(Boolean value) {
            addCriterion("is_limit_total <", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalLessThanOrEqualTo(Boolean value) {
            addCriterion("is_limit_total <=", value, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalIn(List<Boolean> values) {
            addCriterion("is_limit_total in", values, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalNotIn(List<Boolean> values) {
            addCriterion("is_limit_total not in", values, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limit_total between", value1, value2, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andIs_limit_totalNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limit_total not between", value1, value2, "is_limit_total");
            return (Criteria) this;
        }

        public Criteria andComment_timeIsNull() {
            addCriterion("comment_time is null");
            return (Criteria) this;
        }

        public Criteria andComment_timeIsNotNull() {
            addCriterion("comment_time is not null");
            return (Criteria) this;
        }

        public Criteria andComment_timeEqualTo(Integer value) {
            addCriterion("comment_time =", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeNotEqualTo(Integer value) {
            addCriterion("comment_time <>", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeGreaterThan(Integer value) {
            addCriterion("comment_time >", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeGreaterThanOrEqualTo(Integer value) {
            addCriterion("comment_time >=", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeLessThan(Integer value) {
            addCriterion("comment_time <", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeLessThanOrEqualTo(Integer value) {
            addCriterion("comment_time <=", value, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeIn(List<Integer> values) {
            addCriterion("comment_time in", values, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeNotIn(List<Integer> values) {
            addCriterion("comment_time not in", values, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeBetween(Integer value1, Integer value2) {
            addCriterion("comment_time between", value1, value2, "comment_time");
            return (Criteria) this;
        }

        public Criteria andComment_timeNotBetween(Integer value1, Integer value2) {
            addCriterion("comment_time not between", value1, value2, "comment_time");
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

        public Criteria andExpired_limit_noaddrIsNull() {
            addCriterion("expired_limit_noaddr is null");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrIsNotNull() {
            addCriterion("expired_limit_noaddr is not null");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrEqualTo(String value) {
            addCriterion("expired_limit_noaddr =", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrNotEqualTo(String value) {
            addCriterion("expired_limit_noaddr <>", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrGreaterThan(String value) {
            addCriterion("expired_limit_noaddr >", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrGreaterThanOrEqualTo(String value) {
            addCriterion("expired_limit_noaddr >=", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrLessThan(String value) {
            addCriterion("expired_limit_noaddr <", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrLessThanOrEqualTo(String value) {
            addCriterion("expired_limit_noaddr <=", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrLike(String value) {
            addCriterion("expired_limit_noaddr like", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrNotLike(String value) {
            addCriterion("expired_limit_noaddr not like", value, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrIn(List<String> values) {
            addCriterion("expired_limit_noaddr in", values, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrNotIn(List<String> values) {
            addCriterion("expired_limit_noaddr not in", values, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrBetween(String value1, String value2) {
            addCriterion("expired_limit_noaddr between", value1, value2, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andExpired_limit_noaddrNotBetween(String value1, String value2) {
            addCriterion("expired_limit_noaddr not between", value1, value2, "expired_limit_noaddr");
            return (Criteria) this;
        }

        public Criteria andAddress_idIsNull() {
            addCriterion("address_id is null");
            return (Criteria) this;
        }

        public Criteria andAddress_idIsNotNull() {
            addCriterion("address_id is not null");
            return (Criteria) this;
        }

        public Criteria andAddress_idEqualTo(Long value) {
            addCriterion("address_id =", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idNotEqualTo(Long value) {
            addCriterion("address_id <>", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idGreaterThan(Long value) {
            addCriterion("address_id >", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idGreaterThanOrEqualTo(Long value) {
            addCriterion("address_id >=", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idLessThan(Long value) {
            addCriterion("address_id <", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idLessThanOrEqualTo(Long value) {
            addCriterion("address_id <=", value, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idIn(List<Long> values) {
            addCriterion("address_id in", values, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idNotIn(List<Long> values) {
            addCriterion("address_id not in", values, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idBetween(Long value1, Long value2) {
            addCriterion("address_id between", value1, value2, "address_id");
            return (Criteria) this;
        }

        public Criteria andAddress_idNotBetween(Long value1, Long value2) {
            addCriterion("address_id not between", value1, value2, "address_id");
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