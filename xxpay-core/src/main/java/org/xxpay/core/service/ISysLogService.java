package org.xxpay.core.service;

import org.xxpay.core.entity.SysLog;

import java.util.List;

public interface ISysLogService {

    int add(SysLog record);

    List<SysLog> select(int offset, int limit, SysLog record);

    Integer count(SysLog record);

}
