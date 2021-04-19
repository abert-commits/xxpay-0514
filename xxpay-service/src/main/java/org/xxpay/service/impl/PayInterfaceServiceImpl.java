package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.PayInterface;
import org.xxpay.core.entity.PayInterfaceExample;
import org.xxpay.core.service.IPayInterfaceService;
import org.xxpay.service.dao.mapper.PayInterfaceMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 支付接口
 */
@Service(interfaceName = "org.xxpay.core.service.IPayInterfaceService", version = "1.0.0", retries = -1)
public class PayInterfaceServiceImpl implements IPayInterfaceService {

    @Autowired
    private PayInterfaceMapper payInterfaceMapper;

    @Override
    public int add(PayInterface payInterface) {
        return payInterfaceMapper.insertSelective(payInterface);
    }

    @Override
    public int update(PayInterface payInterface) {
        return payInterfaceMapper.updateByPrimaryKeySelective(payInterface);
    }

    @Override
    public PayInterface findByCode(String ifCode) {
        return payInterfaceMapper.selectByPrimaryKey(ifCode);
    }

    @Override
    public List<PayInterface> select(int offset, int limit, PayInterface payInterface) {
        PayInterfaceExample example = new PayInterfaceExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PayInterfaceExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payInterface);
        return payInterfaceMapper.selectByExample(example);
    }

    @Override
    public Integer count(PayInterface payInterface) {
        PayInterfaceExample example = new PayInterfaceExample();
        PayInterfaceExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payInterface);
        return payInterfaceMapper.countByExample(example);
    }

    @Override
    public List<PayInterface> selectAll(PayInterface payInterface) {
        PayInterfaceExample example = new PayInterfaceExample();
        example.setOrderByClause("createTime DESC");
        PayInterfaceExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payInterface);
        return payInterfaceMapper.selectByExample(example);
    }

    @Override
    public List<PayInterface> selectAllByTypeCode(String ifTypeCode) {
        PayInterface payInterface = new PayInterface();
        payInterface.setIfTypeCode(ifTypeCode);
        return selectAll(payInterface);
    }

    void setCriteria(PayInterfaceExample.Criteria criteria, PayInterface obj) {
        if(obj != null) {
            if(obj.getIfTypeCode() != null) criteria.andIfTypeCodeEqualTo(obj.getIfTypeCode());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
            if (StringUtils.isNotBlank(obj.getIfCode())){
                criteria.andIfCodeEqualTo("%"+obj.getIfCode()+"%");
            }
            if (StringUtils.isNotBlank(obj.getIfName())){
                criteria.andIfNameEqualTo("%"+obj.getIfName()+"%");
            }
        }
    }
}
