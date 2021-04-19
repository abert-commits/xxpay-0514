package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.util.WeightUtil;
import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.NginxExample;
import org.xxpay.core.service.INginxService;
import org.xxpay.core.service.INginxService;
import org.xxpay.service.dao.mapper.NginxMapper;
import org.xxpay.service.dao.mapper.NginxMapper;

import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.INginxService", version = "1.0.0", retries = -1)
public class NginxServiceImpl implements INginxService {
    
    @Autowired
    private NginxMapper nginxMapper;

    void setCriteria(NginxExample.Criteria criteria, Nginx obj) {
        if(obj != null) {
            if(obj.getStatus() != null) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
    @Override
    public List<Nginx> select(int offset, int limit, Nginx nginx) {
        NginxExample example = new NginxExample();
        example.setOrderByClause("weights asc");
        example.setOffset(offset);
        example.setLimit(limit);
        NginxExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, nginx);
        return nginxMapper.selectByExample(example);
    }

    @Override
    public Nginx nginxWeights(Nginx nginx) {
        NginxExample example = new NginxExample();
        example.setOrderByClause("weights asc");
        example.setOffset(0);
        example.setLimit(5000000);
        NginxExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, nginx);
        List<Nginx> nginxList= nginxMapper.selectByExample(example);
        WeightUtil<Nginx> weightUtil=new WeightUtil<Nginx>();
        return weightUtil.choose(nginxList);
    }
}
