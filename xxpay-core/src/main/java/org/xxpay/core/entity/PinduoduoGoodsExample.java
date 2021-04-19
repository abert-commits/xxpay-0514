package org.xxpay.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PinduoduoGoodsExample implements Serializable {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private static final long serialVersionUID = 1L;

    private Integer limit;

    private Integer offset;

    public PinduoduoGoodsExample() {
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

        public Criteria andStores_idIsNull() {
            addCriterion("stores_id is null");
            return (Criteria) this;
        }

        public Criteria andStores_idIsNotNull() {
            addCriterion("stores_id is not null");
            return (Criteria) this;
        }

        public Criteria andStores_idEqualTo(Integer value) {
            addCriterion("stores_id =", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idNotEqualTo(Integer value) {
            addCriterion("stores_id <>", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idGreaterThan(Integer value) {
            addCriterion("stores_id >", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idGreaterThanOrEqualTo(Integer value) {
            addCriterion("stores_id >=", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idLessThan(Integer value) {
            addCriterion("stores_id <", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idLessThanOrEqualTo(Integer value) {
            addCriterion("stores_id <=", value, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idIn(List<Integer> values) {
            addCriterion("stores_id in", values, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idNotIn(List<Integer> values) {
            addCriterion("stores_id not in", values, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idBetween(Integer value1, Integer value2) {
            addCriterion("stores_id between", value1, value2, "stores_id");
            return (Criteria) this;
        }

        public Criteria andStores_idNotBetween(Integer value1, Integer value2) {
            addCriterion("stores_id not between", value1, value2, "stores_id");
            return (Criteria) this;
        }

        public Criteria andGoods_nameIsNull() {
            addCriterion("goods_name is null");
            return (Criteria) this;
        }

        public Criteria andGoods_nameIsNotNull() {
            addCriterion("goods_name is not null");
            return (Criteria) this;
        }

        public Criteria andGoods_nameEqualTo(String value) {
            addCriterion("goods_name =", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameNotEqualTo(String value) {
            addCriterion("goods_name <>", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameGreaterThan(String value) {
            addCriterion("goods_name >", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameGreaterThanOrEqualTo(String value) {
            addCriterion("goods_name >=", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameLessThan(String value) {
            addCriterion("goods_name <", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameLessThanOrEqualTo(String value) {
            addCriterion("goods_name <=", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameLike(String value) {
            addCriterion("goods_name like", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameNotLike(String value) {
            addCriterion("goods_name not like", value, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameIn(List<String> values) {
            addCriterion("goods_name in", values, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameNotIn(List<String> values) {
            addCriterion("goods_name not in", values, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameBetween(String value1, String value2) {
            addCriterion("goods_name between", value1, value2, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_nameNotBetween(String value1, String value2) {
            addCriterion("goods_name not between", value1, value2, "goods_name");
            return (Criteria) this;
        }

        public Criteria andGoods_urlIsNull() {
            addCriterion("goods_url is null");
            return (Criteria) this;
        }

        public Criteria andGoods_urlIsNotNull() {
            addCriterion("goods_url is not null");
            return (Criteria) this;
        }

        public Criteria andGoods_urlEqualTo(String value) {
            addCriterion("goods_url =", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlNotEqualTo(String value) {
            addCriterion("goods_url <>", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlGreaterThan(String value) {
            addCriterion("goods_url >", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlGreaterThanOrEqualTo(String value) {
            addCriterion("goods_url >=", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlLessThan(String value) {
            addCriterion("goods_url <", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlLessThanOrEqualTo(String value) {
            addCriterion("goods_url <=", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlLike(String value) {
            addCriterion("goods_url like", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlNotLike(String value) {
            addCriterion("goods_url not like", value, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlIn(List<String> values) {
            addCriterion("goods_url in", values, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlNotIn(List<String> values) {
            addCriterion("goods_url not in", values, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlBetween(String value1, String value2) {
            addCriterion("goods_url between", value1, value2, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_urlNotBetween(String value1, String value2) {
            addCriterion("goods_url not between", value1, value2, "goods_url");
            return (Criteria) this;
        }

        public Criteria andGoods_idIsNull() {
            addCriterion("goods_id is null");
            return (Criteria) this;
        }

        public Criteria andGoods_idIsNotNull() {
            addCriterion("goods_id is not null");
            return (Criteria) this;
        }

        public Criteria andGoods_idEqualTo(Long value) {
            addCriterion("goods_id =", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idNotEqualTo(Long value) {
            addCriterion("goods_id <>", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idGreaterThan(Long value) {
            addCriterion("goods_id >", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idGreaterThanOrEqualTo(Long value) {
            addCriterion("goods_id >=", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idLessThan(Long value) {
            addCriterion("goods_id <", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idLessThanOrEqualTo(Long value) {
            addCriterion("goods_id <=", value, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idIn(List<Long> values) {
            addCriterion("goods_id in", values, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idNotIn(List<Long> values) {
            addCriterion("goods_id not in", values, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idBetween(Long value1, Long value2) {
            addCriterion("goods_id between", value1, value2, "goods_id");
            return (Criteria) this;
        }

        public Criteria andGoods_idNotBetween(Long value1, Long value2) {
            addCriterion("goods_id not between", value1, value2, "goods_id");
            return (Criteria) this;
        }

        public Criteria andSku_idIsNull() {
            addCriterion("sku_id is null");
            return (Criteria) this;
        }

        public Criteria andSku_idIsNotNull() {
            addCriterion("sku_id is not null");
            return (Criteria) this;
        }

        public Criteria andSku_idEqualTo(Long value) {
            addCriterion("sku_id =", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idNotEqualTo(Long value) {
            addCriterion("sku_id <>", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idGreaterThan(Long value) {
            addCriterion("sku_id >", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idGreaterThanOrEqualTo(Long value) {
            addCriterion("sku_id >=", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idLessThan(Long value) {
            addCriterion("sku_id <", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idLessThanOrEqualTo(Long value) {
            addCriterion("sku_id <=", value, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idIn(List<Long> values) {
            addCriterion("sku_id in", values, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idNotIn(List<Long> values) {
            addCriterion("sku_id not in", values, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idBetween(Long value1, Long value2) {
            addCriterion("sku_id between", value1, value2, "sku_id");
            return (Criteria) this;
        }

        public Criteria andSku_idNotBetween(Long value1, Long value2) {
            addCriterion("sku_id not between", value1, value2, "sku_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idIsNull() {
            addCriterion("group_id is null");
            return (Criteria) this;
        }

        public Criteria andGroup_idIsNotNull() {
            addCriterion("group_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroup_idEqualTo(Long value) {
            addCriterion("group_id =", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idNotEqualTo(Long value) {
            addCriterion("group_id <>", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idGreaterThan(Long value) {
            addCriterion("group_id >", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idGreaterThanOrEqualTo(Long value) {
            addCriterion("group_id >=", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idLessThan(Long value) {
            addCriterion("group_id <", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idLessThanOrEqualTo(Long value) {
            addCriterion("group_id <=", value, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idIn(List<Long> values) {
            addCriterion("group_id in", values, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idNotIn(List<Long> values) {
            addCriterion("group_id not in", values, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idBetween(Long value1, Long value2) {
            addCriterion("group_id between", value1, value2, "group_id");
            return (Criteria) this;
        }

        public Criteria andGroup_idNotBetween(Long value1, Long value2) {
            addCriterion("group_id not between", value1, value2, "group_id");
            return (Criteria) this;
        }

        public Criteria andNormal_priceIsNull() {
            addCriterion("normal_price is null");
            return (Criteria) this;
        }

        public Criteria andNormal_priceIsNotNull() {
            addCriterion("normal_price is not null");
            return (Criteria) this;
        }

        public Criteria andNormal_priceEqualTo(Integer value) {
            addCriterion("normal_price =", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceNotEqualTo(Integer value) {
            addCriterion("normal_price <>", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceGreaterThan(Integer value) {
            addCriterion("normal_price >", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceGreaterThanOrEqualTo(Integer value) {
            addCriterion("normal_price >=", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceLessThan(Integer value) {
            addCriterion("normal_price <", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceLessThanOrEqualTo(Integer value) {
            addCriterion("normal_price <=", value, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceIn(List<Integer> values) {
            addCriterion("normal_price in", values, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceNotIn(List<Integer> values) {
            addCriterion("normal_price not in", values, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceBetween(Integer value1, Integer value2) {
            addCriterion("normal_price between", value1, value2, "normal_price");
            return (Criteria) this;
        }

        public Criteria andNormal_priceNotBetween(Integer value1, Integer value2) {
            addCriterion("normal_price not between", value1, value2, "normal_price");
            return (Criteria) this;
        }

        public Criteria andError_countIsNull() {
            addCriterion("error_count is null");
            return (Criteria) this;
        }

        public Criteria andError_countIsNotNull() {
            addCriterion("error_count is not null");
            return (Criteria) this;
        }

        public Criteria andError_countEqualTo(Integer value) {
            addCriterion("error_count =", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countNotEqualTo(Integer value) {
            addCriterion("error_count <>", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countGreaterThan(Integer value) {
            addCriterion("error_count >", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countGreaterThanOrEqualTo(Integer value) {
            addCriterion("error_count >=", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countLessThan(Integer value) {
            addCriterion("error_count <", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countLessThanOrEqualTo(Integer value) {
            addCriterion("error_count <=", value, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countIn(List<Integer> values) {
            addCriterion("error_count in", values, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countNotIn(List<Integer> values) {
            addCriterion("error_count not in", values, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countBetween(Integer value1, Integer value2) {
            addCriterion("error_count between", value1, value2, "error_count");
            return (Criteria) this;
        }

        public Criteria andError_countNotBetween(Integer value1, Integer value2) {
            addCriterion("error_count not between", value1, value2, "error_count");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitIsNull() {
            addCriterion("is_store_limit is null");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitIsNotNull() {
            addCriterion("is_store_limit is not null");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitEqualTo(Boolean value) {
            addCriterion("is_store_limit =", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitNotEqualTo(Boolean value) {
            addCriterion("is_store_limit <>", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitGreaterThan(Boolean value) {
            addCriterion("is_store_limit >", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_store_limit >=", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitLessThan(Boolean value) {
            addCriterion("is_store_limit <", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitLessThanOrEqualTo(Boolean value) {
            addCriterion("is_store_limit <=", value, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitIn(List<Boolean> values) {
            addCriterion("is_store_limit in", values, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitNotIn(List<Boolean> values) {
            addCriterion("is_store_limit not in", values, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitBetween(Boolean value1, Boolean value2) {
            addCriterion("is_store_limit between", value1, value2, "is_store_limit");
            return (Criteria) this;
        }

        public Criteria andIs_store_limitNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_store_limit not between", value1, value2, "is_store_limit");
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

        public Criteria andLast_use_timeIsNull() {
            addCriterion("last_use_time is null");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeIsNotNull() {
            addCriterion("last_use_time is not null");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeEqualTo(Integer value) {
            addCriterion("last_use_time =", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeNotEqualTo(Integer value) {
            addCriterion("last_use_time <>", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeGreaterThan(Integer value) {
            addCriterion("last_use_time >", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeGreaterThanOrEqualTo(Integer value) {
            addCriterion("last_use_time >=", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeLessThan(Integer value) {
            addCriterion("last_use_time <", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeLessThanOrEqualTo(Integer value) {
            addCriterion("last_use_time <=", value, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeIn(List<Integer> values) {
            addCriterion("last_use_time in", values, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeNotIn(List<Integer> values) {
            addCriterion("last_use_time not in", values, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeBetween(Integer value1, Integer value2) {
            addCriterion("last_use_time between", value1, value2, "last_use_time");
            return (Criteria) this;
        }

        public Criteria andLast_use_timeNotBetween(Integer value1, Integer value2) {
            addCriterion("last_use_time not between", value1, value2, "last_use_time");
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

        public Criteria andIs_upperIsNull() {
            addCriterion("is_upper is null");
            return (Criteria) this;
        }

        public Criteria andIs_upperIsNotNull() {
            addCriterion("is_upper is not null");
            return (Criteria) this;
        }

        public Criteria andIs_upperEqualTo(Boolean value) {
            addCriterion("is_upper =", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperNotEqualTo(Boolean value) {
            addCriterion("is_upper <>", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperGreaterThan(Boolean value) {
            addCriterion("is_upper >", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_upper >=", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperLessThan(Boolean value) {
            addCriterion("is_upper <", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperLessThanOrEqualTo(Boolean value) {
            addCriterion("is_upper <=", value, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperIn(List<Boolean> values) {
            addCriterion("is_upper in", values, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperNotIn(List<Boolean> values) {
            addCriterion("is_upper not in", values, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperBetween(Boolean value1, Boolean value2) {
            addCriterion("is_upper between", value1, value2, "is_upper");
            return (Criteria) this;
        }

        public Criteria andIs_upperNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_upper not between", value1, value2, "is_upper");
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