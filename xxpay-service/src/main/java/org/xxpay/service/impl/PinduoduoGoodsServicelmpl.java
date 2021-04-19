package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IPinduoduoGoodsService;
import org.xxpay.core.service.IPinduoduoOrderService;
import org.xxpay.service.dao.mapper.PinduoduoGoodsMapper;
import org.xxpay.service.dao.mapper.PinduoduoOrdersMapper;

import java.net.MalformedURLException;
import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IPinduoduoGoodsService", version = "1.0.0", retries = -1)
public class PinduoduoGoodsServicelmpl implements IPinduoduoGoodsService {

    @Autowired
    private PinduoduoGoodsMapper goodsMapper;

    @Override
    public Integer count(PinduoduoGoods goods) {
        PinduoduoGoodsExample example = new PinduoduoGoodsExample();
        PinduoduoGoodsExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, goods);
        return goodsMapper.countByExample(example);
    }

    @Override
    public List<PinduoduoGoods> select(int offset, int limit, PinduoduoGoods goods) {
        PinduoduoGoodsExample example = new PinduoduoGoodsExample();
        example.setOrderByClause("ctime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PinduoduoGoodsExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, goods);
        return goodsMapper.selectByExample(example);
    }

    @Override
    public int add(PinduoduoGoods goods) {


        return goodsMapper.insertSelective(goods);
    }

    @Override
    public int update(PinduoduoGoods goods) {
        return goodsMapper.updateByPrimaryKeySelective(goods);
    }

    @Override
    public int delete(PinduoduoGoods goods) {
        return goodsMapper.deleteByPrimaryKey(goods.getId());
    }

    @Override
    public void adds(List<PinduoduoGoods> goodsList) {
        try {
            for (int i = 0; i < goodsList.size(); i++) {
                goodsMapper.insertSelective(goodsList.get(i));
            }
        } catch (Exception e) {
            throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
        }
    }

    @Override
    public PinduoduoGoods getGoods(Integer Id) {
        return goodsMapper.selectByPrimaryKey(Id);
    }

    void setCriteria(PinduoduoGoodsExample.Criteria criteria, PinduoduoGoods goods) {
        if (goods != null) {
            if (goods.getStores_id() != null) criteria.andStores_idEqualTo(goods.getStores_id());
            if (goods.getGoods_id() != null) {
                criteria.andGoods_idEqualTo(goods.getGoods_id());
            }
            if (goods.getId() != null) {
                criteria.andIdEqualTo(goods.getId());
            }
            if (goods.getIs_upper()!= null) {
                criteria.andIs_upperEqualTo(goods.getIs_upper());
            }
            if (goods.getStatus() != null) {
                criteria.andStatusEqualTo(goods.getStatus());
            }
        }

    }
}
