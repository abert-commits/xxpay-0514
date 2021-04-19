package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.PinduoduoAddress;
import org.xxpay.core.entity.PinduoduoAddressExample;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.core.entity.PinduoduoUserExample;
import org.xxpay.core.service.IPinduoduoUserService;
import org.xxpay.service.dao.mapper.PinduoduoAddressMapper;
import org.xxpay.service.dao.mapper.PinduoduoUserMapper;

import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IPinduoduoUserService", version = "1.0.0", retries = -1)
public class PinduoduoUserServicelmpl implements IPinduoduoUserService {

    @Autowired
    private PinduoduoUserMapper userMapper;
    @Autowired
    private PinduoduoAddressMapper addressMapper;
    @Override
    public Integer count(PinduoduoUser user) {
        PinduoduoUserExample example = new PinduoduoUserExample();
        PinduoduoUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,user);
        return userMapper.countByExample(example);
    }

    @Override
    public List<PinduoduoUser> select(int offset, int limit, PinduoduoUser user) {
        PinduoduoUserExample example = new PinduoduoUserExample();
        example.setOrderByClause("ctime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PinduoduoUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,user);
        return userMapper.selectByExample(example);
    }

    @Override
    public int add(PinduoduoUser user) {
        PinduoduoUserExample example =new PinduoduoUserExample();
        PinduoduoUserExample.Criteria criteria = example.createCriteria();
        criteria.andPhoneEqualTo(user.getPhone());
        List<PinduoduoUser> user1=userMapper.selectByExample(example);
        if(user1.size()==0){
            return userMapper.insertSelective(user);
        }else{
            return userMapper.updateByExampleSelective(user,example);
        }

    }

    @Override
    public List<PinduoduoUser> selectUser(PinduoduoUser user) {
        PinduoduoUserExample example =new PinduoduoUserExample();
        PinduoduoUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,user);
        return userMapper.selectByExample(example);
    }

    @Override
    public int addAddress(PinduoduoAddress address, PinduoduoUser user) {
        //保存 地址
        addressMapper.insert(address);
        //修改用户默认地址
        PinduoduoUserExample example = new PinduoduoUserExample();
        PinduoduoUserExample.Criteria criteria = example.createCriteria();
        criteria.andPhoneEqualTo(user.getPhone());
        return userMapper.updateByExampleSelective(user,example);
    }

    @Override
    public int update(PinduoduoUser user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public List<PinduoduoAddress> getAddress() {
        PinduoduoAddressExample example = new PinduoduoAddressExample();
        example.setOrderByClause("date DESC");
        return addressMapper.selectByExample(example);
    }

    void setCriteria(PinduoduoUserExample.Criteria criteria, PinduoduoUser user) {
        if(user != null) {
             if(user.getPhone() != null) criteria.andPhoneLike("%"+user.getPhone()+"%");
            if(user.getIs_expired() != null) criteria.andIs_expiredEqualTo(user.getIs_expired());
            if(user.getIs_limit() != null) criteria.andIs_limitEqualTo(user.getIs_limit());
        /*    if(StringUtils.isNotBlank(refundOrder.getChannelOrderNo())) criteria.andChannelOrderNoEqualTo(refundOrder.getChannelOrderNo());
            if(refundOrder.getStatus() != null && refundOrder.getStatus() != -99) criteria.andStatusEqualTo(refundOrder.getStatus());*/
        }

    }
}
