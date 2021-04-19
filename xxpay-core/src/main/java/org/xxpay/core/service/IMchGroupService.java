package org.xxpay.core.service;

import org.xxpay.core.entity.MchGroup;
import org.xxpay.core.entity.PayDataStatistics;

import java.util.List;
import java.util.Map;

public interface IMchGroupService {

    int add(MchGroup mchGroup);

    int update(MchGroup mchGroup);

    MchGroup findByMchGroupId(Integer mchGroupId);

    MchGroup findByGroupName(String groupName);

    List<MchGroup> selectAll();

}
