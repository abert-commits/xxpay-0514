package org.xxpay.service.dao.mapper;

import org.xxpay.core.entity.MchGroup;

import java.util.List;

public interface MchGroupMapper {

    int add(MchGroup mchGroup);

    int update(MchGroup mchGroup);

    MchGroup findByMchGroupId(Integer mchGroupId);

    MchGroup findByGroupName(String groupName);


    List<MchGroup> selectAll();
}
