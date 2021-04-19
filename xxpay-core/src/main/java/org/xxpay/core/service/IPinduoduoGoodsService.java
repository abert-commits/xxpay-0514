package org.xxpay.core.service;

import org.xxpay.core.entity.PinduoduoGoods;
import org.xxpay.core.entity.PinduoduoPreOrders;

import java.util.List;


public interface IPinduoduoGoodsService {

    Integer count(PinduoduoGoods goods);

    List<PinduoduoGoods> select(int offset, int limit, PinduoduoGoods goods);

    int add(PinduoduoGoods goods);

    int update(PinduoduoGoods goods);

    int delete(PinduoduoGoods goods);

    void adds(List<PinduoduoGoods> goodsList);


    PinduoduoGoods getGoods(Integer Id);
}
