package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.PayProduct;
import org.xxpay.core.service.ICommonService;
import org.xxpay.core.service.IPayProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/9/11
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.ICommonService", version = "1.0.0", retries = -1)
public class CommontServiceImpl implements ICommonService {

    @Autowired
    private IPayProductService payProductService;

    @Override
    public Map<String, PayProduct> getPayProdcutMap(PayProduct payProduct) {
        Map<String, PayProduct> payProductMap = new HashMap<>();
        List<PayProduct> payProductList = payProductService.selectAll(payProduct);
        if(CollectionUtils.isEmpty(payProductList)) return payProductMap;
        for(PayProduct product : payProductList) {
            payProductMap.put(String.valueOf(product.getId()), product);
        }
        return payProductMap;
    }
}
