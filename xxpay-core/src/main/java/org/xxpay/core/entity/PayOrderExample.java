package org.xxpay.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PayOrderExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    private Integer passageId;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public Integer getPassageId() {
        return passageId;
    }

    public void setPassageId(Integer passageId) {
        this.passageId = passageId;
    }

    public PayOrderExample() {
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

        public Criteria andPayOrderIdIsNull() {
            addCriterion("t_pay_order.PayOrderId is null");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdIsNotNull() {
            addCriterion("t_pay_order.PayOrderId is not null");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdEqualTo(String value) {
            addCriterion("t_pay_order.PayOrderId =", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdNotEqualTo(String value) {
            addCriterion("t_pay_order.PayOrderId <>", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdGreaterThan(String value) {
            addCriterion("t_pay_order.PayOrderId >", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.PayOrderId >=", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdLessThan(String value) {
            addCriterion("t_pay_order.PayOrderId <", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.PayOrderId <=", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdLike(String value) {
            addCriterion("t_pay_order.PayOrderId like", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdNotLike(String value) {
            addCriterion("t_pay_order.PayOrderId not like", value, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdIn(List<String> values) {
            addCriterion("t_pay_order.PayOrderId in", values, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdNotIn(List<String> values) {
            addCriterion("t_pay_order.PayOrderId not in", values, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdBetween(String value1, String value2) {
            addCriterion("t_pay_order.PayOrderId between", value1, value2, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andPayOrderIdNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.PayOrderId not between", value1, value2, "payOrderId");
            return (Criteria) this;
        }

        public Criteria andMchIdIsNull() {
            addCriterion("t_pay_order.MchId is null");
            return (Criteria) this;
        }

        public Criteria andMchIdIsNotNull() {
            addCriterion("t_pay_order.MchId is not null");
            return (Criteria) this;
        }

        public Criteria andMchIdEqualTo(Long value) {
            addCriterion("t_pay_order.MchId =", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdNotEqualTo(Long value) {
            addCriterion("t_pay_order.MchId <>", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdGreaterThan(Long value) {
            addCriterion("t_pay_order.MchId >", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.MchId >=", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdLessThan(Long value) {
            addCriterion("t_pay_order.MchId <", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.MchId <=", value, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdIn(List<Long> values) {
            addCriterion("t_pay_order.MchId in", values, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdNotIn(List<Long> values) {
            addCriterion("t_pay_order.MchId not in", values, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.MchId between", value1, value2, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchIdNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.MchId not between", value1, value2, "mchId");
            return (Criteria) this;
        }

        public Criteria andMchTypeIsNull() {
            addCriterion("t_pay_order.MchType is null");
            return (Criteria) this;
        }

        public Criteria andMchTypeIsNotNull() {
            addCriterion("t_pay_order.MchType is not null");
            return (Criteria) this;
        }

        public Criteria andMchTypeEqualTo(Byte value) {
            addCriterion("t_pay_order.MchType =", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeNotEqualTo(Byte value) {
            addCriterion("t_pay_order.MchType <>", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeGreaterThan(Byte value) {
            addCriterion("t_pay_order.MchType >", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.MchType >=", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeLessThan(Byte value) {
            addCriterion("t_pay_order.MchType <", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeLessThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.MchType <=", value, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeIn(List<Byte> values) {
            addCriterion("t_pay_order.MchType in", values, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeNotIn(List<Byte> values) {
            addCriterion("t_pay_order.MchType not in", values, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.MchType between", value1, value2, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.MchType not between", value1, value2, "mchType");
            return (Criteria) this;
        }

        public Criteria andMchRateIsNull() {
            addCriterion("t_pay_order.MchRate is null");
            return (Criteria) this;
        }

        public Criteria andMchRateIsNotNull() {
            addCriterion("t_pay_order.MchRate is not null");
            return (Criteria) this;
        }

        public Criteria andMchRateEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.MchRate =", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateNotEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.MchRate <>", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateGreaterThan(BigDecimal value) {
            addCriterion("t_pay_order.MchRate >", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.MchRate >=", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateLessThan(BigDecimal value) {
            addCriterion("t_pay_order.MchRate <", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.MchRate <=", value, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.MchRate in", values, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateNotIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.MchRate not in", values, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.MchRate between", value1, value2, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.MchRate not between", value1, value2, "mchRate");
            return (Criteria) this;
        }

        public Criteria andMchIncomeIsNull() {
            addCriterion("t_pay_order.MchIncome is null");
            return (Criteria) this;
        }

        public Criteria andMchIncomeIsNotNull() {
            addCriterion("t_pay_order.MchIncome is not null");
            return (Criteria) this;
        }

        public Criteria andMchIncomeEqualTo(Long value) {
            addCriterion("t_pay_order.MchIncome =", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeNotEqualTo(Long value) {
            addCriterion("t_pay_order.MchIncome <>", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeGreaterThan(Long value) {
            addCriterion("t_pay_order.MchIncome >", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.MchIncome >=", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeLessThan(Long value) {
            addCriterion("t_pay_order.MchIncome <", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.MchIncome <=", value, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeIn(List<Long> values) {
            addCriterion("t_pay_order.MchIncome in", values, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeNotIn(List<Long> values) {
            addCriterion("t_pay_order.MchIncome not in", values, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.MchIncome between", value1, value2, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andMchIncomeNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.MchIncome not between", value1, value2, "mchIncome");
            return (Criteria) this;
        }

        public Criteria andAppIdIsNull() {
            addCriterion("t_pay_order.AppId is null");
            return (Criteria) this;
        }

        public Criteria andAppIdIsNotNull() {
            addCriterion("t_pay_order.AppId is not null");
            return (Criteria) this;
        }

        public Criteria andAppIdEqualTo(String value) {
            addCriterion("t_pay_order.AppId =", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdNotEqualTo(String value) {
            addCriterion("t_pay_order.AppId <>", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdGreaterThan(String value) {
            addCriterion("t_pay_order.AppId >", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.AppId >=", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdLessThan(String value) {
            addCriterion("t_pay_order.AppId <", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.AppId <=", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdLike(String value) {
            addCriterion("t_pay_order.AppId like", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdNotLike(String value) {
            addCriterion("t_pay_order.AppId not like", value, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdIn(List<String> values) {
            addCriterion("t_pay_order.AppId in", values, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdNotIn(List<String> values) {
            addCriterion("t_pay_order.AppId not in", values, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdBetween(String value1, String value2) {
            addCriterion("t_pay_order.AppId between", value1, value2, "appId");
            return (Criteria) this;
        }

        public Criteria andAppIdNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.AppId not between", value1, value2, "appId");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoIsNull() {
            addCriterion("t_pay_order.MchOrderNo is null");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoIsNotNull() {
            addCriterion("t_pay_order.MchOrderNo is not null");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoEqualTo(String value) {
            addCriterion("t_pay_order.MchOrderNo =", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoNotEqualTo(String value) {
            addCriterion("t_pay_order.MchOrderNo <>", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoGreaterThan(String value) {
            addCriterion("t_pay_order.MchOrderNo >", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.MchOrderNo >=", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoLessThan(String value) {
            addCriterion("t_pay_order.MchOrderNo <", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.MchOrderNo <=", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoLike(String value) {
            addCriterion("t_pay_order.MchOrderNo like", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoNotLike(String value) {
            addCriterion("t_pay_order.MchOrderNo not like", value, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoIn(List<String> values) {
            addCriterion("t_pay_order.MchOrderNo in", values, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoNotIn(List<String> values) {
            addCriterion("t_pay_order.MchOrderNo not in", values, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoBetween(String value1, String value2) {
            addCriterion("t_pay_order.MchOrderNo between", value1, value2, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andMchOrderNoNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.MchOrderNo not between", value1, value2, "mchOrderNo");
            return (Criteria) this;
        }

        public Criteria andAgentIdIsNull() {
            addCriterion("t_pay_order.AgentId is null");
            return (Criteria) this;
        }

        public Criteria andAgentIdIsNotNull() {
            addCriterion("t_pay_order.AgentId is not null");
            return (Criteria) this;
        }

        public Criteria andAgentIdEqualTo(Long value) {
            addCriterion("t_pay_order.AgentId =", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotEqualTo(Long value) {
            addCriterion("t_pay_order.AgentId <>", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdGreaterThan(Long value) {
            addCriterion("t_pay_order.AgentId >", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.AgentId >=", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdLessThan(Long value) {
            addCriterion("t_pay_order.AgentId <", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.AgentId <=", value, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdIn(List<Long> values) {
            addCriterion("t_pay_order.AgentId in", values, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotIn(List<Long> values) {
            addCriterion("t_pay_order.AgentId not in", values, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.AgentId between", value1, value2, "agentId");
            return (Criteria) this;
        }

        public Criteria andAgentIdNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.AgentId not between", value1, value2, "agentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIsNull() {
            addCriterion("t_pay_order.parentAgentId is null");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIsNotNull() {
            addCriterion("t_pay_order.parentAgentId is not null");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentId =", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentId <>", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdGreaterThan(Long value) {
            addCriterion("t_pay_order.parentAgentId >", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentId >=", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdLessThan(Long value) {
            addCriterion("t_pay_order.parentAgentId <", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentId <=", value, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdIn(List<Long> values) {
            addCriterion("t_pay_order.parentAgentId in", values, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotIn(List<Long> values) {
            addCriterion("t_pay_order.parentAgentId not in", values, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.parentAgentId between", value1, value2, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andParentAgentIdNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.parentAgentId not between", value1, value2, "parentAgentId");
            return (Criteria) this;
        }

        public Criteria andAgentRateIsNull() {
            addCriterion("t_pay_order.AgentRate is null");
            return (Criteria) this;
        }

        public Criteria andAgentRateIsNotNull() {
            addCriterion("t_pay_order.AgentRate is not null");
            return (Criteria) this;
        }

        public Criteria andAgentRateEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate =", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateNotEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate <>", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateGreaterThan(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate >", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate >=", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateLessThan(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate <", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.AgentRate <=", value, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.AgentRate in", values, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateNotIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.AgentRate not in", values, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.AgentRate between", value1, value2, "agentRate");
            return (Criteria) this;
        }

        public Criteria andAgentRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.AgentRate not between", value1, value2, "agentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateIsNull() {
            addCriterion("t_pay_order.parentAgentRate is null");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateIsNotNull() {
            addCriterion("t_pay_order.parentAgentRate is not null");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate =", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateNotEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate <>", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateGreaterThan(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate >", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate >=", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateLessThan(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate <", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.parentAgentRate <=", value, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.parentAgentRate in", values, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateNotIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.parentAgentRate not in", values, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.parentAgentRate between", value1, value2, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andParentAgentRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.parentAgentRate not between", value1, value2, "parentAgentRate");
            return (Criteria) this;
        }

        public Criteria andAgentProfitIsNull() {
            addCriterion("t_pay_order.AgentProfit is null");
            return (Criteria) this;
        }

        public Criteria andAgentProfitIsNotNull() {
            addCriterion("t_pay_order.AgentProfit is not null");
            return (Criteria) this;
        }

        public Criteria andAgentProfitEqualTo(Long value) {
            addCriterion("t_pay_order.AgentProfit =", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitNotEqualTo(Long value) {
            addCriterion("t_pay_order.AgentProfit <>", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitGreaterThan(Long value) {
            addCriterion("t_pay_order.AgentProfit >", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.AgentProfit >=", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitLessThan(Long value) {
            addCriterion("t_pay_order.AgentProfit <", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.AgentProfit <=", value, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitIn(List<Long> values) {
            addCriterion("t_pay_order.AgentProfit in", values, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitNotIn(List<Long> values) {
            addCriterion("t_pay_order.AgentProfit not in", values, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.AgentProfit between", value1, value2, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andAgentProfitNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.AgentProfit not between", value1, value2, "agentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitIsNull() {
            addCriterion("t_pay_order.parentAgentProfit is null");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitIsNotNull() {
            addCriterion("t_pay_order.parentAgentProfit is not null");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentProfit =", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitNotEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentProfit <>", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitGreaterThan(Long value) {
            addCriterion("t_pay_order.parentAgentProfit >", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentProfit >=", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitLessThan(Long value) {
            addCriterion("t_pay_order.parentAgentProfit <", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.parentAgentProfit <=", value, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitIn(List<Long> values) {
            addCriterion("t_pay_order.parentAgentProfit in", values, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitNotIn(List<Long> values) {
            addCriterion("t_pay_order.parentAgentProfit not in", values, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.parentAgentProfit between", value1, value2, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andParentAgentProfitNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.parentAgentProfit not between", value1, value2, "parentAgentProfit");
            return (Criteria) this;
        }

        public Criteria andProductIdIsNull() {
            addCriterion("t_pay_order.ProductId is null");
            return (Criteria) this;
        }

        public Criteria andProductIdIsNotNull() {
            addCriterion("t_pay_order.ProductId is not null");
            return (Criteria) this;
        }

        public Criteria andProductIdEqualTo(Integer value) {
            addCriterion("t_pay_order.ProductId =", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdNotEqualTo(Integer value) {
            addCriterion("t_pay_order.ProductId <>", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdGreaterThan(Integer value) {
            addCriterion("t_pay_order.ProductId >", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.ProductId >=", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdLessThan(Integer value) {
            addCriterion("t_pay_order.ProductId <", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdLessThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.ProductId <=", value, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdIn(List<Integer> values) {
            addCriterion("t_pay_order.ProductId in", values, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdNotIn(List<Integer> values) {
            addCriterion("t_pay_order.ProductId not in", values, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.ProductId between", value1, value2, "productId");
            return (Criteria) this;
        }

        public Criteria andProductIdNotBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.ProductId not between", value1, value2, "productId");
            return (Criteria) this;
        }

        public Criteria andPassageIdIsNull() {
            addCriterion("t_pay_order.PassageId is null");
            return (Criteria) this;
        }

        public Criteria andPassageIdIsNotNull() {
            addCriterion("t_pay_order.PassageId is not null");
            return (Criteria) this;
        }

        public Criteria andPassageIdEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageId =", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdNotEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageId <>", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdGreaterThan(Integer value) {
            addCriterion("t_pay_order.PassageId >", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageId >=", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdLessThan(Integer value) {
            addCriterion("t_pay_order.PassageId <", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdLessThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageId <=", value, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdIn(List<Integer> values) {
            addCriterion("t_pay_order.PassageId in", values, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdNotIn(List<Integer> values) {
            addCriterion("t_pay_order.PassageId not in", values, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.PassageId between", value1, value2, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageIdNotBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.PassageId not between", value1, value2, "passageId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdIsNull() {
            addCriterion("t_pay_order.PassageAccountId is null");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdIsNotNull() {
            addCriterion("t_pay_order.PassageAccountId is not null");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageAccountId =", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdNotEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageAccountId <>", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdGreaterThan(Integer value) {
            addCriterion("t_pay_order.PassageAccountId >", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageAccountId >=", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdLessThan(Integer value) {
            addCriterion("t_pay_order.PassageAccountId <", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdLessThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.PassageAccountId <=", value, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdIn(List<Integer> values) {
            addCriterion("t_pay_order.PassageAccountId in", values, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdNotIn(List<Integer> values) {
            addCriterion("t_pay_order.PassageAccountId not in", values, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.PassageAccountId between", value1, value2, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andPassageAccountIdNotBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.PassageAccountId not between", value1, value2, "passageAccountId");
            return (Criteria) this;
        }

        public Criteria andChannelTypeIsNull() {
            addCriterion("t_pay_order.ChannelType is null");
            return (Criteria) this;
        }

        public Criteria andChannelTypeIsNotNull() {
            addCriterion("t_pay_order.ChannelType is not null");
            return (Criteria) this;
        }

        public Criteria andChannelTypeEqualTo(String value) {
            addCriterion("t_pay_order.ChannelType =", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelType <>", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelType >", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelType >=", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLessThan(String value) {
            addCriterion("t_pay_order.ChannelType <", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelType <=", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLike(String value) {
            addCriterion("t_pay_order.ChannelType like", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotLike(String value) {
            addCriterion("t_pay_order.ChannelType not like", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeIn(List<String> values) {
            addCriterion("t_pay_order.ChannelType in", values, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelType not in", values, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelType between", value1, value2, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelType not between", value1, value2, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelIdIsNull() {
            addCriterion("t_pay_order.ChannelId is null");
            return (Criteria) this;
        }

        public Criteria andChannelIdIsNotNull() {
            addCriterion("t_pay_order.ChannelId is not null");
            return (Criteria) this;
        }

        public Criteria andChannelIdEqualTo(String value) {
            addCriterion("t_pay_order.ChannelId =", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelId <>", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelId >", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelId >=", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdLessThan(String value) {
            addCriterion("t_pay_order.ChannelId <", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelId <=", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdLike(String value) {
            addCriterion("t_pay_order.ChannelId like", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdNotLike(String value) {
            addCriterion("t_pay_order.ChannelId not like", value, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdIn(List<String> values) {
            addCriterion("t_pay_order.ChannelId in", values, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelId not in", values, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelId between", value1, value2, "channelId");
            return (Criteria) this;
        }

        public Criteria andChannelIdNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelId not between", value1, value2, "channelId");
            return (Criteria) this;
        }

        public Criteria andAmountIsNull() {
            addCriterion("t_pay_order.Amount is null");
            return (Criteria) this;
        }

        public Criteria andAmountIsNotNull() {
            addCriterion("t_pay_order.Amount is not null");
            return (Criteria) this;
        }

        public Criteria andAmountEqualTo(Long value) {
            addCriterion("t_pay_order.Amount =", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountEqualToMinAmount(Long value) {
            addCriterion("t_pay_order.Amount >=", value, "minAmount");
            return (Criteria) this;
        }

        public Criteria andAmountEqualToMaxAmount(Long value) {
            addCriterion("t_pay_order.Amount <=", value, "maxAmount");
            return (Criteria) this;
        }

        public Criteria andAmountNotEqualTo(Long value) {
            addCriterion("t_pay_order.Amount <>", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThan(Long value) {
            addCriterion("t_pay_order.Amount >", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.Amount >=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThan(Long value) {
            addCriterion("t_pay_order.Amount <", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.Amount <=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountIn(List<Long> values) {
            addCriterion("t_pay_order.Amount in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotIn(List<Long> values) {
            addCriterion("t_pay_order.Amount not in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.Amount between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.Amount not between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andCurrencyIsNull() {
            addCriterion("t_pay_order.Currency is null");
            return (Criteria) this;
        }

        public Criteria andCurrencyIsNotNull() {
            addCriterion("t_pay_order.Currency is not null");
            return (Criteria) this;
        }

        public Criteria andCurrencyEqualTo(String value) {
            addCriterion("t_pay_order.Currency =", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyNotEqualTo(String value) {
            addCriterion("t_pay_order.Currency <>", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyGreaterThan(String value) {
            addCriterion("t_pay_order.Currency >", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Currency >=", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyLessThan(String value) {
            addCriterion("t_pay_order.Currency <", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Currency <=", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyLike(String value) {
            addCriterion("t_pay_order.Currency like", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyNotLike(String value) {
            addCriterion("t_pay_order.Currency not like", value, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyIn(List<String> values) {
            addCriterion("t_pay_order.Currency in", values, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyNotIn(List<String> values) {
            addCriterion("t_pay_order.Currency not in", values, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyBetween(String value1, String value2) {
            addCriterion("t_pay_order.Currency between", value1, value2, "currency");
            return (Criteria) this;
        }

        public Criteria andCurrencyNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Currency not between", value1, value2, "currency");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("t_pay_order.Status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("t_pay_order.Status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("t_pay_order.Status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("t_pay_order.Status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("t_pay_order.Status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.Status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("t_pay_order.Status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.Status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("t_pay_order.Status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("t_pay_order.Status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.Status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.Status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andClientIpIsNull() {
            addCriterion("t_pay_order.ClientIp is null");
            return (Criteria) this;
        }

        public Criteria andClientIpIsNotNull() {
            addCriterion("t_pay_order.ClientIp is not null");
            return (Criteria) this;
        }

        public Criteria andClientIpEqualTo(String value) {
            addCriterion("t_pay_order.ClientIp =", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpNotEqualTo(String value) {
            addCriterion("t_pay_order.ClientIp <>", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpGreaterThan(String value) {
            addCriterion("t_pay_order.ClientIp >", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ClientIp >=", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpLessThan(String value) {
            addCriterion("t_pay_order.ClientIp <", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ClientIp <=", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpLike(String value) {
            addCriterion("t_pay_order.ClientIp like", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpNotLike(String value) {
            addCriterion("t_pay_order.ClientIp not like", value, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpIn(List<String> values) {
            addCriterion("t_pay_order.ClientIp in", values, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpNotIn(List<String> values) {
            addCriterion("t_pay_order.ClientIp not in", values, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpBetween(String value1, String value2) {
            addCriterion("t_pay_order.ClientIp between", value1, value2, "clientIp");
            return (Criteria) this;
        }

        public Criteria andClientIpNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ClientIp not between", value1, value2, "clientIp");
            return (Criteria) this;
        }

        public Criteria andDeviceIsNull() {
            addCriterion("t_pay_order.Device is null");
            return (Criteria) this;
        }

        public Criteria andDeviceIsNotNull() {
            addCriterion("t_pay_order.Device is not null");
            return (Criteria) this;
        }

        public Criteria andDeviceEqualTo(String value) {
            addCriterion("t_pay_order.Device =", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceNotEqualTo(String value) {
            addCriterion("t_pay_order.Device <>", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceGreaterThan(String value) {
            addCriterion("t_pay_order.Device >", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Device >=", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceLessThan(String value) {
            addCriterion("t_pay_order.Device <", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Device <=", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceLike(String value) {
            addCriterion("t_pay_order.Device like", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceNotLike(String value) {
            addCriterion("t_pay_order.Device not like", value, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceIn(List<String> values) {
            addCriterion("t_pay_order.Device in", values, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceNotIn(List<String> values) {
            addCriterion("t_pay_order.Device not in", values, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceBetween(String value1, String value2) {
            addCriterion("t_pay_order.Device between", value1, value2, "device");
            return (Criteria) this;
        }

        public Criteria andDeviceNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Device not between", value1, value2, "device");
            return (Criteria) this;
        }

        public Criteria andSubjectIsNull() {
            addCriterion("t_pay_order.Subject is null");
            return (Criteria) this;
        }

        public Criteria andSubjectIsNotNull() {
            addCriterion("t_pay_order.Subject is not null");
            return (Criteria) this;
        }

        public Criteria andSubjectEqualTo(String value) {
            addCriterion("t_pay_order.Subject =", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotEqualTo(String value) {
            addCriterion("t_pay_order.Subject <>", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectGreaterThan(String value) {
            addCriterion("t_pay_order.Subject >", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Subject >=", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectLessThan(String value) {
            addCriterion("t_pay_order.Subject <", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Subject <=", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectLike(String value) {
            addCriterion("t_pay_order.Subject like", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotLike(String value) {
            addCriterion("t_pay_order.Subject not like", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectIn(List<String> values) {
            addCriterion("t_pay_order.Subject in", values, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotIn(List<String> values) {
            addCriterion("t_pay_order.Subject not in", values, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectBetween(String value1, String value2) {
            addCriterion("t_pay_order.Subject between", value1, value2, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Subject not between", value1, value2, "subject");
            return (Criteria) this;
        }

        public Criteria andBodyIsNull() {
            addCriterion("t_pay_order.Body is null");
            return (Criteria) this;
        }

        public Criteria andBodyIsNotNull() {
            addCriterion("t_pay_order.Body is not null");
            return (Criteria) this;
        }

        public Criteria andBodyEqualTo(String value) {
            addCriterion("t_pay_order.Body =", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyNotEqualTo(String value) {
            addCriterion("t_pay_order.Body <>", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyGreaterThan(String value) {
            addCriterion("t_pay_order.Body >", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Body >=", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyLessThan(String value) {
            addCriterion("t_pay_order.Body <", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Body <=", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyLike(String value) {
            addCriterion("t_pay_order.Body like", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyNotLike(String value) {
            addCriterion("t_pay_order.Body not like", value, "body");
            return (Criteria) this;
        }

        public Criteria andBodyIn(List<String> values) {
            addCriterion("t_pay_order.Body in", values, "body");
            return (Criteria) this;
        }

        public Criteria andBodyNotIn(List<String> values) {
            addCriterion("t_pay_order.Body not in", values, "body");
            return (Criteria) this;
        }

        public Criteria andBodyBetween(String value1, String value2) {
            addCriterion("t_pay_order.Body between", value1, value2, "body");
            return (Criteria) this;
        }

        public Criteria andBodyNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Body not between", value1, value2, "body");
            return (Criteria) this;
        }

        public Criteria andExtraIsNull() {
            addCriterion("t_pay_order.Extra is null");
            return (Criteria) this;
        }

        public Criteria andExtraIsNotNull() {
            addCriterion("t_pay_order.Extra is not null");
            return (Criteria) this;
        }

        public Criteria andExtraEqualTo(String value) {
            addCriterion("t_pay_order.Extra =", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraNotEqualTo(String value) {
            addCriterion("t_pay_order.Extra <>", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraGreaterThan(String value) {
            addCriterion("t_pay_order.Extra >", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Extra >=", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraLessThan(String value) {
            addCriterion("t_pay_order.Extra <", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Extra <=", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraLike(String value) {
            addCriterion("t_pay_order.Extra like", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraNotLike(String value) {
            addCriterion("t_pay_order.Extra not like", value, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraIn(List<String> values) {
            addCriterion("t_pay_order.Extra in", values, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraNotIn(List<String> values) {
            addCriterion("t_pay_order.Extra not in", values, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraBetween(String value1, String value2) {
            addCriterion("t_pay_order.Extra between", value1, value2, "extra");
            return (Criteria) this;
        }

        public Criteria andExtraNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Extra not between", value1, value2, "extra");
            return (Criteria) this;
        }

        public Criteria andChannelUserIsNull() {
            addCriterion("t_pay_order.ChannelUser is null");
            return (Criteria) this;
        }

        public Criteria andChannelUserIsNotNull() {
            addCriterion("t_pay_order.ChannelUser is not null");
            return (Criteria) this;
        }

        public Criteria andChannelUserEqualTo(String value) {
            addCriterion("t_pay_order.ChannelUser =", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelUser <>", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelUser >", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelUser >=", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserLessThan(String value) {
            addCriterion("t_pay_order.ChannelUser <", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelUser <=", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserLike(String value) {
            addCriterion("t_pay_order.ChannelUser like", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserNotLike(String value) {
            addCriterion("t_pay_order.ChannelUser not like", value, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserIn(List<String> values) {
            addCriterion("t_pay_order.ChannelUser in", values, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelUser not in", values, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelUser between", value1, value2, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelUserNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelUser not between", value1, value2, "channelUser");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdIsNull() {
            addCriterion("t_pay_order.ChannelMchId is null");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdIsNotNull() {
            addCriterion("t_pay_order.ChannelMchId is not null");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdEqualTo(String value) {
            addCriterion("t_pay_order.ChannelMchId =", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelMchId <>", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelMchId >", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelMchId >=", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdLessThan(String value) {
            addCriterion("t_pay_order.ChannelMchId <", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelMchId <=", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdLike(String value) {
            addCriterion("t_pay_order.ChannelMchId like", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdNotLike(String value) {
            addCriterion("t_pay_order.ChannelMchId not like", value, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdIn(List<String> values) {
            addCriterion("t_pay_order.ChannelMchId in", values, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelMchId not in", values, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelMchId between", value1, value2, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelMchIdNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelMchId not between", value1, value2, "channelMchId");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoIsNull() {
            addCriterion("t_pay_order.ChannelOrderNo is null");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoIsNotNull() {
            addCriterion("t_pay_order.ChannelOrderNo is not null");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoEqualTo(String value) {
            addCriterion("t_pay_order.ChannelOrderNo =", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelOrderNo <>", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelOrderNo >", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelOrderNo >=", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoLessThan(String value) {
            addCriterion("t_pay_order.ChannelOrderNo <", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelOrderNo <=", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoLike(String value) {
            addCriterion("t_pay_order.ChannelOrderNo like", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoNotLike(String value) {
            addCriterion("t_pay_order.ChannelOrderNo not like", value, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoIn(List<String> values) {
            addCriterion("t_pay_order.ChannelOrderNo in", values, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelOrderNo not in", values, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelOrderNo between", value1, value2, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelOrderNoNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelOrderNo not between", value1, value2, "channelOrderNo");
            return (Criteria) this;
        }

        public Criteria andChannelAttachIsNull() {
            addCriterion("t_pay_order.ChannelAttach is null");
            return (Criteria) this;
        }

        public Criteria andChannelAttachIsNotNull() {
            addCriterion("t_pay_order.ChannelAttach is not null");
            return (Criteria) this;
        }

        public Criteria andChannelAttachEqualTo(String value) {
            addCriterion("t_pay_order.ChannelAttach =", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachNotEqualTo(String value) {
            addCriterion("t_pay_order.ChannelAttach <>", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachGreaterThan(String value) {
            addCriterion("t_pay_order.ChannelAttach >", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelAttach >=", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachLessThan(String value) {
            addCriterion("t_pay_order.ChannelAttach <", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ChannelAttach <=", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachLike(String value) {
            addCriterion("t_pay_order.ChannelAttach like", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachNotLike(String value) {
            addCriterion("t_pay_order.ChannelAttach not like", value, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachIn(List<String> values) {
            addCriterion("t_pay_order.ChannelAttach in", values, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachNotIn(List<String> values) {
            addCriterion("t_pay_order.ChannelAttach not in", values, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelAttach between", value1, value2, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andChannelAttachNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ChannelAttach not between", value1, value2, "channelAttach");
            return (Criteria) this;
        }

        public Criteria andPlatProfitIsNull() {
            addCriterion("t_pay_order.PlatProfit is null");
            return (Criteria) this;
        }

        public Criteria andPlatProfitIsNotNull() {
            addCriterion("t_pay_order.PlatProfit is not null");
            return (Criteria) this;
        }

        public Criteria andPlatProfitEqualTo(Long value) {
            addCriterion("t_pay_order.PlatProfit =", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitNotEqualTo(Long value) {
            addCriterion("t_pay_order.PlatProfit <>", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitGreaterThan(Long value) {
            addCriterion("t_pay_order.PlatProfit >", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.PlatProfit >=", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitLessThan(Long value) {
            addCriterion("t_pay_order.PlatProfit <", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.PlatProfit <=", value, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitIn(List<Long> values) {
            addCriterion("t_pay_order.PlatProfit in", values, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitNotIn(List<Long> values) {
            addCriterion("t_pay_order.PlatProfit not in", values, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.PlatProfit between", value1, value2, "platProfit");
            return (Criteria) this;
        }

        public Criteria andPlatProfitNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.PlatProfit not between", value1, value2, "platProfit");
            return (Criteria) this;
        }

        public Criteria andChannelRateIsNull() {
            addCriterion("t_pay_order.ChannelRate is null");
            return (Criteria) this;
        }

        public Criteria andChannelRateIsNotNull() {
            addCriterion("t_pay_order.ChannelRate is not null");
            return (Criteria) this;
        }

        public Criteria andChannelRateEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate =", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateNotEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate <>", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateGreaterThan(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate >", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate >=", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateLessThan(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate <", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("t_pay_order.ChannelRate <=", value, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.ChannelRate in", values, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateNotIn(List<BigDecimal> values) {
            addCriterion("t_pay_order.ChannelRate not in", values, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.ChannelRate between", value1, value2, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("t_pay_order.ChannelRate not between", value1, value2, "channelRate");
            return (Criteria) this;
        }

        public Criteria andChannelCostIsNull() {
            addCriterion("t_pay_order.ChannelCost is null");
            return (Criteria) this;
        }

        public Criteria andChannelCostIsNotNull() {
            addCriterion("t_pay_order.ChannelCost is not null");
            return (Criteria) this;
        }

        public Criteria andChannelCostEqualTo(Long value) {
            addCriterion("t_pay_order.ChannelCost =", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostNotEqualTo(Long value) {
            addCriterion("t_pay_order.ChannelCost <>", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostGreaterThan(Long value) {
            addCriterion("t_pay_order.ChannelCost >", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.ChannelCost >=", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostLessThan(Long value) {
            addCriterion("t_pay_order.ChannelCost <", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.ChannelCost <=", value, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostIn(List<Long> values) {
            addCriterion("t_pay_order.ChannelCost in", values, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostNotIn(List<Long> values) {
            addCriterion("t_pay_order.ChannelCost not in", values, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.ChannelCost between", value1, value2, "channelCost");
            return (Criteria) this;
        }

        public Criteria andChannelCostNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.ChannelCost not between", value1, value2, "channelCost");
            return (Criteria) this;
        }

        public Criteria andIsRefundIsNull() {
            addCriterion("t_pay_order.IsRefund is null");
            return (Criteria) this;
        }

        public Criteria andIsRefundIsNotNull() {
            addCriterion("t_pay_order.IsRefund is not null");
            return (Criteria) this;
        }

        public Criteria andIsRefundEqualTo(Byte value) {
            addCriterion("t_pay_order.IsRefund =", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundNotEqualTo(Byte value) {
            addCriterion("t_pay_order.IsRefund <>", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundGreaterThan(Byte value) {
            addCriterion("t_pay_order.IsRefund >", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundGreaterThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.IsRefund >=", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundLessThan(Byte value) {
            addCriterion("t_pay_order.IsRefund <", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundLessThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.IsRefund <=", value, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundIn(List<Byte> values) {
            addCriterion("t_pay_order.IsRefund in", values, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundNotIn(List<Byte> values) {
            addCriterion("t_pay_order.IsRefund not in", values, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.IsRefund between", value1, value2, "isRefund");
            return (Criteria) this;
        }

        public Criteria andIsRefundNotBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.IsRefund not between", value1, value2, "isRefund");
            return (Criteria) this;
        }

        public Criteria andRefundTimesIsNull() {
            addCriterion("t_pay_order.RefundTimes is null");
            return (Criteria) this;
        }

        public Criteria andRefundTimesIsNotNull() {
            addCriterion("t_pay_order.RefundTimes is not null");
            return (Criteria) this;
        }

        public Criteria andRefundTimesEqualTo(Integer value) {
            addCriterion("t_pay_order.RefundTimes =", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesNotEqualTo(Integer value) {
            addCriterion("t_pay_order.RefundTimes <>", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesGreaterThan(Integer value) {
            addCriterion("t_pay_order.RefundTimes >", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.RefundTimes >=", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesLessThan(Integer value) {
            addCriterion("t_pay_order.RefundTimes <", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesLessThanOrEqualTo(Integer value) {
            addCriterion("t_pay_order.RefundTimes <=", value, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesIn(List<Integer> values) {
            addCriterion("t_pay_order.RefundTimes in", values, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesNotIn(List<Integer> values) {
            addCriterion("t_pay_order.RefundTimes not in", values, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.RefundTimes between", value1, value2, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andRefundTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("t_pay_order.RefundTimes not between", value1, value2, "refundTimes");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountIsNull() {
            addCriterion("t_pay_order.SuccessRefundAmount is null");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountIsNotNull() {
            addCriterion("t_pay_order.SuccessRefundAmount is not null");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountEqualTo(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount =", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountNotEqualTo(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount <>", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountGreaterThan(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount >", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountGreaterThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount >=", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountLessThan(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount <", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountLessThanOrEqualTo(Long value) {
            addCriterion("t_pay_order.SuccessRefundAmount <=", value, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountIn(List<Long> values) {
            addCriterion("t_pay_order.SuccessRefundAmount in", values, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountNotIn(List<Long> values) {
            addCriterion("t_pay_order.SuccessRefundAmount not in", values, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.SuccessRefundAmount between", value1, value2, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andSuccessRefundAmountNotBetween(Long value1, Long value2) {
            addCriterion("t_pay_order.SuccessRefundAmount not between", value1, value2, "successRefundAmount");
            return (Criteria) this;
        }

        public Criteria andErrCodeIsNull() {
            addCriterion("t_pay_order.ErrCode is null");
            return (Criteria) this;
        }

        public Criteria andErrCodeIsNotNull() {
            addCriterion("t_pay_order.ErrCode is not null");
            return (Criteria) this;
        }

        public Criteria andErrCodeEqualTo(String value) {
            addCriterion("t_pay_order.ErrCode =", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeNotEqualTo(String value) {
            addCriterion("t_pay_order.ErrCode <>", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeGreaterThan(String value) {
            addCriterion("t_pay_order.ErrCode >", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ErrCode >=", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeLessThan(String value) {
            addCriterion("t_pay_order.ErrCode <", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ErrCode <=", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeLike(String value) {
            addCriterion("t_pay_order.ErrCode like", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeNotLike(String value) {
            addCriterion("t_pay_order.ErrCode not like", value, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeIn(List<String> values) {
            addCriterion("t_pay_order.ErrCode in", values, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeNotIn(List<String> values) {
            addCriterion("t_pay_order.ErrCode not in", values, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeBetween(String value1, String value2) {
            addCriterion("t_pay_order.ErrCode between", value1, value2, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrCodeNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ErrCode not between", value1, value2, "errCode");
            return (Criteria) this;
        }

        public Criteria andErrMsgIsNull() {
            addCriterion("t_pay_order.ErrMsg is null");
            return (Criteria) this;
        }

        public Criteria andErrMsgIsNotNull() {
            addCriterion("t_pay_order.ErrMsg is not null");
            return (Criteria) this;
        }

        public Criteria andErrMsgEqualTo(String value) {
            addCriterion("t_pay_order.ErrMsg =", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgNotEqualTo(String value) {
            addCriterion("t_pay_order.ErrMsg <>", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgGreaterThan(String value) {
            addCriterion("t_pay_order.ErrMsg >", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ErrMsg >=", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgLessThan(String value) {
            addCriterion("t_pay_order.ErrMsg <", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ErrMsg <=", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgLike(String value) {
            addCriterion("t_pay_order.ErrMsg like", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgNotLike(String value) {
            addCriterion("t_pay_order.ErrMsg not like", value, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgIn(List<String> values) {
            addCriterion("t_pay_order.ErrMsg in", values, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgNotIn(List<String> values) {
            addCriterion("t_pay_order.ErrMsg not in", values, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgBetween(String value1, String value2) {
            addCriterion("t_pay_order.ErrMsg between", value1, value2, "errMsg");
            return (Criteria) this;
        }

        public Criteria andErrMsgNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ErrMsg not between", value1, value2, "errMsg");
            return (Criteria) this;
        }

        public Criteria andParam1IsNull() {
            addCriterion("t_pay_order.Param1 is null");
            return (Criteria) this;
        }

        public Criteria andParam1IsNotNull() {
            addCriterion("t_pay_order.Param1 is not null");
            return (Criteria) this;
        }

        public Criteria andParam1EqualTo(String value) {
            addCriterion("t_pay_order.Param1 =", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1NotEqualTo(String value) {
            addCriterion("t_pay_order.Param1 <>", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1GreaterThan(String value) {
            addCriterion("t_pay_order.Param1 >", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1GreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Param1 >=", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1LessThan(String value) {
            addCriterion("t_pay_order.Param1 <", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1LessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Param1 <=", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1Like(String value) {
            addCriterion("t_pay_order.Param1 like", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1NotLike(String value) {
            addCriterion("t_pay_order.Param1 not like", value, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1In(List<String> values) {
            addCriterion("t_pay_order.Param1 in", values, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1NotIn(List<String> values) {
            addCriterion("t_pay_order.Param1 not in", values, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1Between(String value1, String value2) {
            addCriterion("t_pay_order.Param1 between", value1, value2, "param1");
            return (Criteria) this;
        }

        public Criteria andParam1NotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Param1 not between", value1, value2, "param1");
            return (Criteria) this;
        }

        public Criteria andParam2IsNull() {
            addCriterion("t_pay_order.Param2 is null");
            return (Criteria) this;
        }

        public Criteria andParam2IsNotNull() {
            addCriterion("t_pay_order.Param2 is not null");
            return (Criteria) this;
        }

        public Criteria andParam2EqualTo(String value) {
            addCriterion("t_pay_order.Param2 =", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2NotEqualTo(String value) {
            addCriterion("t_pay_order.Param2 <>", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2GreaterThan(String value) {
            addCriterion("t_pay_order.Param2 >", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2GreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Param2 >=", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2LessThan(String value) {
            addCriterion("t_pay_order.Param2 <", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2LessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.Param2 <=", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2Like(String value) {
            addCriterion("t_pay_order.Param2 like", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2NotLike(String value) {
            addCriterion("t_pay_order.Param2 not like", value, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2In(List<String> values) {
            addCriterion("t_pay_order.Param2 in", values, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2NotIn(List<String> values) {
            addCriterion("t_pay_order.Param2 not in", values, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2Between(String value1, String value2) {
            addCriterion("t_pay_order.Param2 between", value1, value2, "param2");
            return (Criteria) this;
        }

        public Criteria andParam2NotBetween(String value1, String value2) {
            addCriterion("t_pay_order.Param2 not between", value1, value2, "param2");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlIsNull() {
            addCriterion("t_pay_order.NotifyUrl is null");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlIsNotNull() {
            addCriterion("t_pay_order.NotifyUrl is not null");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlEqualTo(String value) {
            addCriterion("t_pay_order.NotifyUrl =", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlNotEqualTo(String value) {
            addCriterion("t_pay_order.NotifyUrl <>", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlGreaterThan(String value) {
            addCriterion("t_pay_order.NotifyUrl >", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.NotifyUrl >=", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlLessThan(String value) {
            addCriterion("t_pay_order.NotifyUrl <", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.NotifyUrl <=", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlLike(String value) {
            addCriterion("t_pay_order.NotifyUrl like", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlNotLike(String value) {
            addCriterion("t_pay_order.NotifyUrl not like", value, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlIn(List<String> values) {
            addCriterion("t_pay_order.NotifyUrl in", values, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlNotIn(List<String> values) {
            addCriterion("t_pay_order.NotifyUrl not in", values, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlBetween(String value1, String value2) {
            addCriterion("t_pay_order.NotifyUrl between", value1, value2, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andNotifyUrlNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.NotifyUrl not between", value1, value2, "notifyUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlIsNull() {
            addCriterion("t_pay_order.ReturnUrl is null");
            return (Criteria) this;
        }

        public Criteria andReturnUrlIsNotNull() {
            addCriterion("t_pay_order.ReturnUrl is not null");
            return (Criteria) this;
        }

        public Criteria andReturnUrlEqualTo(String value) {
            addCriterion("t_pay_order.ReturnUrl =", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlNotEqualTo(String value) {
            addCriterion("t_pay_order.ReturnUrl <>", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlGreaterThan(String value) {
            addCriterion("t_pay_order.ReturnUrl >", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlGreaterThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ReturnUrl >=", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlLessThan(String value) {
            addCriterion("t_pay_order.ReturnUrl <", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlLessThanOrEqualTo(String value) {
            addCriterion("t_pay_order.ReturnUrl <=", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlLike(String value) {
            addCriterion("t_pay_order.ReturnUrl like", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlNotLike(String value) {
            addCriterion("t_pay_order.ReturnUrl not like", value, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlIn(List<String> values) {
            addCriterion("t_pay_order.ReturnUrl in", values, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlNotIn(List<String> values) {
            addCriterion("t_pay_order.ReturnUrl not in", values, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlBetween(String value1, String value2) {
            addCriterion("t_pay_order.ReturnUrl between", value1, value2, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andReturnUrlNotBetween(String value1, String value2) {
            addCriterion("t_pay_order.ReturnUrl not between", value1, value2, "returnUrl");
            return (Criteria) this;
        }

        public Criteria andExpireTimeIsNull() {
            addCriterion("t_pay_order.ExpireTime is null");
            return (Criteria) this;
        }

        public Criteria andExpireTimeIsNotNull() {
            addCriterion("t_pay_order.ExpireTime is not null");
            return (Criteria) this;
        }

        public Criteria andExpireTimeEqualTo(Date value) {
            addCriterion("t_pay_order.ExpireTime =", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeNotEqualTo(Date value) {
            addCriterion("t_pay_order.ExpireTime <>", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeGreaterThan(Date value) {
            addCriterion("t_pay_order.ExpireTime >", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.ExpireTime >=", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeLessThan(Date value) {
            addCriterion("t_pay_order.ExpireTime <", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeLessThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.ExpireTime <=", value, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeIn(List<Date> values) {
            addCriterion("t_pay_order.ExpireTime in", values, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeNotIn(List<Date> values) {
            addCriterion("t_pay_order.ExpireTime not in", values, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.ExpireTime between", value1, value2, "expireTime");
            return (Criteria) this;
        }

        public Criteria andExpireTimeNotBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.ExpireTime not between", value1, value2, "expireTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeIsNull() {
            addCriterion("t_pay_order.PaySuccTime is null");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeIsNotNull() {
            addCriterion("t_pay_order.PaySuccTime is not null");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeEqualTo(Date value) {
            addCriterion("t_pay_order.PaySuccTime =", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeNotEqualTo(Date value) {
            addCriterion("t_pay_order.PaySuccTime <>", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeGreaterThan(Date value) {
            addCriterion("t_pay_order.PaySuccTime >", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.PaySuccTime >=", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeLessThan(Date value) {
            addCriterion("t_pay_order.PaySuccTime <", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeLessThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.PaySuccTime <=", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeIn(List<Date> values) {
            addCriterion("t_pay_order.PaySuccTime in", values, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeNotIn(List<Date> values) {
            addCriterion("t_pay_order.PaySuccTime not in", values, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.PaySuccTime between", value1, value2, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccTimeNotBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.PaySuccTime not between", value1, value2, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("t_pay_order.CreateTime is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("t_pay_order.CreateTime is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("t_pay_order.CreateTime =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("t_pay_order.CreateTime <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("t_pay_order.CreateTime >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.CreateTime >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andPaySuccessTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.paySuccTime >=", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andHighest() {

            addCriterion(" timestampdiff(day,DATE_FORMAT(t_pay_order.CreateTime,'%Y-%m-%d'),DATE_FORMAT(t_pay_order.PaySuccTime,'%Y-%m-%d')) >0");
//            addCriterion(" timestampdiff(day,t_pay_order.CreateTime,t_pay_order.PaySuccTime) >0");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("t_pay_order.CreateTime <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.CreateTime <=", value, "createTime");
            return (Criteria) this;
        }
        public Criteria andpaySuccTimeLessThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.paySuccTime <=", value, "paySuccTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("t_pay_order.CreateTime in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("t_pay_order.CreateTime not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.CreateTime between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.CreateTime not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("t_pay_order.UpdateTime is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("t_pay_order.UpdateTime is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("t_pay_order.UpdateTime =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("t_pay_order.UpdateTime <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("t_pay_order.UpdateTime >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.UpdateTime >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("t_pay_order.UpdateTime <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("t_pay_order.UpdateTime <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("t_pay_order.UpdateTime in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("t_pay_order.UpdateTime not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.UpdateTime between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("t_pay_order.UpdateTime not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andProductTypeIsNull() {
            addCriterion("t_pay_order.ProductType is null");
            return (Criteria) this;
        }

        public Criteria andProductTypeIsNotNull() {
            addCriterion("t_pay_order.ProductType is not null");
            return (Criteria) this;
        }

        public Criteria andProductTypeEqualTo(Byte value) {
            addCriterion("t_pay_order.ProductType =", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeNotEqualTo(Byte value) {
            addCriterion("t_pay_order.ProductType <>", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeGreaterThan(Byte value) {
            addCriterion("t_pay_order.ProductType >", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.ProductType >=", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeLessThan(Byte value) {
            addCriterion("t_pay_order.ProductType <", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeLessThanOrEqualTo(Byte value) {
            addCriterion("t_pay_order.ProductType <=", value, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeIn(List<Byte> values) {
            addCriterion("t_pay_order.ProductType in", values, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeNotIn(List<Byte> values) {
            addCriterion("t_pay_order.ProductType not in", values, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.ProductType between", value1, value2, "productType");
            return (Criteria) this;
        }

        public Criteria andProductTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("t_pay_order.ProductType not between", value1, value2, "productType");
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