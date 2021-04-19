package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.AgentpayPassage;
import org.xxpay.core.entity.AgentpayPassageExample;
import org.xxpay.core.entity.MchAgentpayPassage;
import org.xxpay.core.entity.MchAgentpayPassageExample;
import org.xxpay.core.service.IMchAgentpayPassageService;
import org.xxpay.service.dao.mapper.AgentpayPassageMapper;
import org.xxpay.service.dao.mapper.MchAgentpayPassageMapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 商户代付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IMchAgentpayPassageService", version = "1.0.0", retries = -1)
public class MchAgentpayPassageServiceImpl implements IMchAgentpayPassageService {

    @Autowired
    private MchAgentpayPassageMapper mchAgentpayPassageMapper;

    @Autowired
    private AgentpayPassageMapper agentpayPassageMapper;

    @Override
    public int add(MchAgentpayPassage mchAgentpayPassage) {
        return mchAgentpayPassageMapper.insertSelective(mchAgentpayPassage);
    }

    @Override
    public int update(MchAgentpayPassage mchAgentpayPassage) {
        return mchAgentpayPassageMapper.updateByPrimaryKeySelective(mchAgentpayPassage);
    }

    @Override
    public int updateByMchId(MchAgentpayPassage updateMchAgentpayPassage, Long mchId) {
        MchAgentpayPassage mchAgentpayPassage = new MchAgentpayPassage();
        mchAgentpayPassage.setMchId(mchId);
        MchAgentpayPassageExample example = new MchAgentpayPassageExample();
        MchAgentpayPassageExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        return mchAgentpayPassageMapper.updateByExampleSelective(updateMchAgentpayPassage, example);
    }

    @Override
    public MchAgentpayPassage findById(Integer id) {
        return mchAgentpayPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchAgentpayPassage findByMchIdAndAgentpayPassageId(Long mchId, Integer agentpayPassageId) {
        MchAgentpayPassage mchAgentpayPassage = new MchAgentpayPassage();
        mchAgentpayPassage.setMchId(mchId);
        mchAgentpayPassage.setAgentpayPassageId(agentpayPassageId);
        List<MchAgentpayPassage> mchAgentpayPassageList = selectAll(mchAgentpayPassage);
        if(CollectionUtils.isEmpty(mchAgentpayPassageList)) return null;
        return mchAgentpayPassageList.get(0);
    }

    @Override
    public List<MchAgentpayPassage> select(int offset, int limit, MchAgentpayPassage mchAgentpayPassage) {
        MchAgentpayPassageExample example = new MchAgentpayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayPassage);
        return mchAgentpayPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(MchAgentpayPassage mchAgentpayPassage) {
        MchAgentpayPassageExample example = new MchAgentpayPassageExample();
        MchAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayPassage);
        return mchAgentpayPassageMapper.countByExample(example);
    }

    @Override
    public List<MchAgentpayPassage> selectAll(MchAgentpayPassage mchAgentpayPassage) {
        return selectAll(mchAgentpayPassage, "createTime DESC");
    }

    public List<MchAgentpayPassage> selectAll(MchAgentpayPassage mchAgentpayPassage, String OrderByClause) {
        MchAgentpayPassageExample example = new MchAgentpayPassageExample();
        example.setOrderByClause(OrderByClause);
        MchAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayPassage);
        return mchAgentpayPassageMapper.selectByExample(example);
    }

    @Override
    public List<MchAgentpayPassage> selectAllByMchId(Long mchId) {
        MchAgentpayPassage mchAgentpayPassage = new MchAgentpayPassage();
        mchAgentpayPassage.setMchId(mchId);
        return selectAll(mchAgentpayPassage);
    }

    @Override
    public List<MchAgentpayPassage> selectAvailable(Long mchId) {
        // TODO 以下逻辑可以优化为sql关联查询实现
        MchAgentpayPassage mchAgentpayPassage = new MchAgentpayPassage();
        mchAgentpayPassage.setMchId(mchId);
        mchAgentpayPassage.setStatus(MchConstant.PUB_YES);
        // 得到商户已经配置的代付通道
        List<MchAgentpayPassage> mchAgentpayPassageList = selectAll(mchAgentpayPassage, "isDefault DESC");
        if(CollectionUtils.isEmpty(mchAgentpayPassageList)) return mchAgentpayPassageList;
        List<Integer> ids = new LinkedList<>();
        for(MchAgentpayPassage mchAgentpayPassage1 : mchAgentpayPassageList) {
            ids.add(mchAgentpayPassage1.getAgentpayPassageId());
        }
        // 查询商户配的,有开启的代付通道
        AgentpayPassageExample example = new AgentpayPassageExample();
        AgentpayPassageExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        criteria.andStatusEqualTo(MchConstant.PUB_YES);
        List<AgentpayPassage> agentpayPassageList = agentpayPassageMapper.selectByExample(example);
        // 如果为空,则返回空代付通道
        if(CollectionUtils.isEmpty(agentpayPassageList)) {
            return null;
        }
        Map<String, AgentpayPassage> map = new HashMap<>();
        for(AgentpayPassage agentpayPassage : agentpayPassageList) {
            map.put(agentpayPassage.getId()+"", agentpayPassage);
        }
        List<MchAgentpayPassage> list = new LinkedList<>();
        for(MchAgentpayPassage mp : mchAgentpayPassageList) {
            if(map.get(String.valueOf(mp.getAgentpayPassageId())) != null) {
                list.add(mp);
            }
        }
        // 返回有效的商户代付通道
        return list;
    }

    void setCriteria(MchAgentpayPassageExample.Criteria criteria, MchAgentpayPassage obj) {
        if(obj != null) {
            if(obj.getMchId() != null) criteria.andMchIdEqualTo(obj.getMchId());
            if(obj.getAgentpayPassageId() != null) criteria.andAgentpayPassageIdEqualTo(obj.getAgentpayPassageId());
            if(obj.getIsDefault() != null) criteria.andIsDefaultEqualTo(obj.getIsDefault());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
