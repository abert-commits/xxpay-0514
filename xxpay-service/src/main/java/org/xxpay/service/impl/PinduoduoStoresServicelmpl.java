package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IPinduoduoStoresService;
import org.xxpay.core.service.IPinduoduoStoresService;
import org.xxpay.service.dao.mapper.PinduoduoAddressMapper;
import org.xxpay.service.dao.mapper.PinduoduoStoresMapper;
import org.xxpay.service.dao.mapper.PinduoduoStoresMapper;

import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IPinduoduoStoresService", version = "1.0.0", retries = -1)
public class PinduoduoStoresServicelmpl implements IPinduoduoStoresService {

    @Autowired
    private PinduoduoStoresMapper storesMapper;

    @Override
    public Integer count(PinduoduoStores stores) {
        PinduoduoStoresExample example = new PinduoduoStoresExample();
        PinduoduoStoresExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,stores);
        return storesMapper.countByExample(example);
    }

    @Override
    public List<PinduoduoStores> select(int offset, int limit, PinduoduoStores stores) {
        PinduoduoStoresExample example = new PinduoduoStoresExample();
        example.setOrderByClause("ctime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PinduoduoStoresExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,stores);
        return storesMapper.selectByExample(example);
    }

    @Override
    public int add(PinduoduoStores stores) {
        PinduoduoStoresExample example =new PinduoduoStoresExample();
        PinduoduoStoresExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(stores.getName());
        List<PinduoduoStores> stores1=storesMapper.selectByExample(example);
        if(stores1.size()==0){
            return storesMapper.insertSelective(stores);
        }else{
            return storesMapper.updateByExampleSelective(stores,example);
        }

    }

    @Override
    public List<PinduoduoStores> selectStores(PinduoduoStores stores) {
        PinduoduoStoresExample example =new PinduoduoStoresExample();
        PinduoduoStoresExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,stores);
        return storesMapper.selectByExample(example);
    }

    @Override
    public int update(PinduoduoStores stores) {
        return storesMapper.updateByPrimaryKeySelective(stores);
    }


    void setCriteria(PinduoduoStoresExample.Criteria criteria, PinduoduoStores stores) {
        if(stores != null) {
             if(stores.getName() != null) criteria.andNameLike("%"+stores.getName()+"%");

        /*    if(StringUtils.isNotBlank(refundOrder.getChannelOrderNo())) criteria.andChannelOrderNoEqualTo(refundOrder.getChannelOrderNo());
            if(refundOrder.getStatus() != null && refundOrder.getStatus() != -99) criteria.andStatusEqualTo(refundOrder.getStatus());*/
        }

    }
}
