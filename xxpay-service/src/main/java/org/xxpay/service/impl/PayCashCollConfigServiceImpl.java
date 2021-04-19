package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayCashCollConfigExample;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.core.service.IPayCashCollConfigService;
import org.xxpay.service.dao.mapper.PayCashCollConfigMapper;
import org.xxpay.service.dao.mapper.PayPassageAccountMapper;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "org.xxpay.core.service.IPayCashCollConfigService", version = "1.0.0", retries = -1)
public class PayCashCollConfigServiceImpl implements IPayCashCollConfigService {

    @Autowired
    private PayCashCollConfigMapper recordMapper;

    @Autowired
    private PayPassageAccountMapper payPassageAccountMapper;


    @Override
    public List<PayCashCollConfig> select(int offset, int limit, PayCashCollConfig config) {
        PayCashCollConfigExample example = new PayCashCollConfigExample();
        example.setOrderByClause("status desc");
        example.setOffset(offset);
        example.setLimit(limit);
        PayCashCollConfigExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, config);
        return recordMapper.selectByExample(example);
    }


    @Override
    public List<PayCashCollConfig> select(int offset, int limit, Map<String, Object> map) {
        PayCashCollConfigExample example = new PayCashCollConfigExample();
        example.setOrderByClause("status desc");
        example.setOffset(offset);
        example.setLimit(limit);
        PayCashCollConfigExample.Criteria criteria = example.createCriteria();
        if (map.containsKey("Type") && map.get("Type") != null) {
            criteria.andTypeEqualTo(Integer.valueOf(map.get("Type").toString()));
        }

        if (map.containsKey("Status") && map.get("Status") != null && map.get("Status").toString() != "-99") {
            criteria.andStatusEqualTo((byte) Integer.parseInt(map.get("Status").toString()));
        }

        if (map.containsKey("PayAccountId") && map.get("PayAccountId") != null && StringUtils.isNotBlank(map.get("PayAccountId").toString())) {
            criteria.andBelongPayAccountIdEqualTo(Integer.valueOf(map.get("PayAccountId").toString()));
        }

        if (map.containsKey("TransInUserIds") && map.get("TransInUserIds") != null) {
            List<String> listUserId = JSON.toJavaObject(((JSONObject) map.get("TransInUserIds")), List.class);
            criteria.andTransInUserIdNotIn(listUserId);
        }

        return recordMapper.selectByExample(example);
    }


    @Override
    public List<PayCashCollConfig> selectAll(PayCashCollConfig config) {
        PayCashCollConfigExample example = new PayCashCollConfigExample();
        PayCashCollConfigExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, config);
        return recordMapper.selectByExample(example);
    }

    @Override
    public Integer count(PayCashCollConfig config) {
        PayCashCollConfigExample example = new PayCashCollConfigExample();
        PayCashCollConfigExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, config);
        return recordMapper.countByExample(example);
    }

    @Override
    public PayCashCollConfig findById(Integer id) {
        return recordMapper.selectByPrimaryKey(id);
    }

    private void check(PayCashCollConfig config) {

        if (config.getTransInPercentage().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(RetEnum.RET_MGR_CASH_COLL_LE_ZERO);
        }

        if (config.getBelongPayAccountId() != 0) {

            PayPassageAccount payPassageAccount = payPassageAccountMapper.selectByPrimaryKey(config.getBelongPayAccountId());
            if (payPassageAccount == null) throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
//            if ("alipay_qr_pc".equals(payPassageAccount.getIfCode()) && !"alipay_qr_pc".equals(payPassageAccount.getIfCode())) {
//                throw new ServiceException(RetEnum.RET_MGR_CASH_COLL_IS_NOT_ALIPAY);
//            }

        }

        //查询子账户分账pid是否重复
        PayCashCollConfigExample exa = new PayCashCollConfigExample();
        PayCashCollConfigExample.Criteria criteria = exa.createCriteria();
        criteria.andBelongPayAccountIdEqualTo(config.getBelongPayAccountId()).andTransInUserIdEqualTo(config.getTransInUserId());
        if (config.getId() != null) {
            criteria.andIdNotEqualTo(config.getId());
        }
        int row = recordMapper.countByExample(exa);
        if (row > 0) throw new ServiceException(RetEnum.RET_MGR_CASH_COLL_PID_EXISTS);

        //查询子账户分账配置比例是否超出范围
        exa = new PayCashCollConfigExample();
        criteria = exa.createCriteria();
        criteria.andStatusEqualTo(MchConstant.PUB_YES).andBelongPayAccountIdEqualTo(config.getBelongPayAccountId());
        if (config.getId() != null) {
            criteria.andIdNotEqualTo(config.getId());
        }

//        BigDecimal sumPercentage = recordMapper.sumPercentageByExample(exa);
//        if (sumPercentage.add(config.getTransInPercentage()).compareTo(new BigDecimal(100)) > 0) {
//            throw new ServiceException(RetEnum.RET_MGR_CASH_COLL_LT_MAX_PERCENTAGE);
//        }

    }

    @Override
    public int add(PayCashCollConfig config) {

        check(config);
        return recordMapper.insertSelective(config);
    }

    @Override
    public int update(PayCashCollConfig config) {
        check(config);
        return recordMapper.updateByPrimaryKeySelective(config);
    }


    void setCriteria(PayCashCollConfigExample.Criteria criteria, PayCashCollConfig obj) {
        if (obj != null) {
            if (obj.getId() != null) criteria.andIdEqualTo(obj.getId());
            if (obj.getStatus() != null && obj.getStatus().byteValue() != -99)
                criteria.andStatusEqualTo(obj.getStatus());
            if (obj.getBelongPayAccountId() != null) criteria.andBelongPayAccountIdEqualTo(obj.getBelongPayAccountId());
            if (obj.getType() != null) criteria.andTypeEqualTo(obj.getType());
            if (StringUtils.isNotBlank(obj.getRemark())) criteria.andRemarkLike("%" + obj.getRemark() + "%");
            if (StringUtils.isNotBlank(obj.getTransInUserAccount()) && obj.getTransInUserAccount().contains("-")) {
                List<String> listTransInUserAccount = Arrays.asList(obj.getTransInUserAccount().split("-"));
                criteria.andTransInUserAccountNotIn(listTransInUserAccount);
            }
        }
    }
}
