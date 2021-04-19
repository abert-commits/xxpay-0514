package org.xxpay.core.service;

import org.xxpay.core.entity.PinduoduoAddress;
import org.xxpay.core.entity.PinduoduoUser;

import java.util.List;


public interface IPinduoduoUserService {

    Integer count(PinduoduoUser user);

    List<PinduoduoUser> select(int offset, int limit, PinduoduoUser user);

    int add(PinduoduoUser user);

    List<PinduoduoUser> selectUser(PinduoduoUser user);

    int addAddress(PinduoduoAddress address, PinduoduoUser user);

    int update(PinduoduoUser user);

    List<PinduoduoAddress> getAddress();
}
