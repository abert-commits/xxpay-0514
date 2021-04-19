package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.WxUser;
import org.xxpay.core.entity.WxUserExample;
import org.xxpay.core.service.IWxUserService;
import org.xxpay.service.dao.mapper.WxUserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IWxUserService", version = "1.0.0", retries = -1)
public class WxUserServiceImpl implements IWxUserService {

    @Autowired
    private WxUserMapper wxUserMapper;

    @Override
    public int add(WxUser wxUser) {
        return wxUserMapper.insertSelective(wxUser);
    }

    @Override
    public int update(WxUser wxUser) {
        return wxUserMapper.updateByPrimaryKeySelective(wxUser);
    }

    @Override
    public int updateByRandomId(WxUser wxUser, String randomId) {
        WxUserExample example = new WxUserExample();
        WxUserExample.Criteria criteria = example.createCriteria();
        criteria.andRandomIdEqualTo(randomId);
        return wxUserMapper.updateByExampleSelective(wxUser, example);
    }

    @Override
    public WxUser find(WxUser wxUser) {
        WxUserExample example = new WxUserExample();
        WxUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, wxUser);
        List<WxUser> wxUserList = wxUserMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(wxUserList)) return null;
        return wxUserList.get(0);
    }

    @Override
    public WxUser findByUserId(Long userId) {
        return wxUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public WxUser findByAccount(String account) {
        WxUserExample example = new WxUserExample();
        WxUserExample.Criteria criteria = example.createCriteria();
        criteria.andAccountEqualTo(account);
        List<WxUser> wxUserList = wxUserMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(wxUserList)) return null;
        return wxUserList.get(0);
    }

    @Override
    public List<WxUser> select(int offset, int limit, WxUser wxUser) {
        WxUserExample example = new WxUserExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        WxUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, wxUser);
        return wxUserMapper.selectByExample(example);
    }

    @Override
    public List<WxUser> select4sync(int offset, int limit) {
        WxUserExample example = new WxUserExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        WxUserExample.Criteria criteria = example.createCriteria();
        criteria.andLoginStatusNotEqualTo(-1);
        return wxUserMapper.selectByExample(example);
    }

    @Override
    public Integer count(WxUser wxUser) {
        WxUserExample example = new WxUserExample();
        WxUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, wxUser);
        return wxUserMapper.countByExample(example);
    }

    /**
     * 得到一个有效的收款用户
     * @param dayMaxAmount
     * @param dayMaxNumber
     * @return
     */
    @Override
    public WxUser findByAvailable(String serverId, Long dayMaxAmount, Long dayMaxNumber) {
        Map param = new HashMap();
        param.put("serverId", serverId);
        param.put("dayMaxAmount", dayMaxAmount);
        param.put("dayMaxNumber", dayMaxNumber);
        return wxUserMapper.selectByAvailable(param);
    }

    /**
     * 更新今日收款,今日订单数等数据
     * @param randomId
     * @param amount
     * @return
     */
    @Override
    public int updateDayByRandomId(String randomId, Long amount) {
        Map param = new HashMap();
        param.put("randomId", randomId);
        param.put("amount", amount);
        return wxUserMapper.updateDayByRandomId(param);
    }

    /**
     * 初始化今日收款,今日订单数等数据
     * @return
     */
    @Override
    public int updateDayByInit() {
        Map param = new HashMap();
        return wxUserMapper.updateDayByInit(param);
    }

    void setCriteria(WxUserExample.Criteria criteria, WxUser wxUser) {
        if(wxUser != null) {
            if(wxUser.getUserId() != null) criteria.andUserIdEqualTo(wxUser.getUserId());
            if(StringUtils.isNotEmpty(wxUser.getAccount())) criteria.andAccountEqualTo(wxUser.getAccount());
            if(StringUtils.isNotEmpty(wxUser.getServerId())) criteria.andServerIdEqualTo(wxUser.getServerId());
            if(StringUtils.isNotEmpty(wxUser.getRandomId())) criteria.andRandomIdEqualTo(wxUser.getRandomId());
            if(wxUser.getStatus() != null && wxUser.getStatus() != -99) criteria.andStatusEqualTo(wxUser.getStatus());
            if(wxUser.getLoginStatus() != null && wxUser.getLoginStatus() != -99) criteria.andLoginStatusEqualTo(wxUser.getLoginStatus());
            if(wxUser.getInStatus() != null && wxUser.getInStatus() != -99) criteria.andInStatusEqualTo(wxUser.getInStatus());
        }
    }

}
