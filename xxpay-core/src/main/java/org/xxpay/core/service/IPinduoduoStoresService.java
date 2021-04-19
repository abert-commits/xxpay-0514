package org.xxpay.core.service;


import org.xxpay.core.entity.PinduoduoAddress;
import org.xxpay.core.entity.PinduoduoStores;

import java.util.List;

public interface IPinduoduoStoresService {

    Integer count(PinduoduoStores stores);

    List<PinduoduoStores> select(int offset, int limit, PinduoduoStores stores);

    int add(PinduoduoStores stores);

    List<PinduoduoStores> selectStores(PinduoduoStores stores);
    
    int update(PinduoduoStores stores);

}
