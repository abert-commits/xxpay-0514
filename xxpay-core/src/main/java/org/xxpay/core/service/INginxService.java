package org.xxpay.core.service;

import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.SysLog;

import java.util.List;

public interface INginxService {


    List<Nginx> select(int offset, int limit, Nginx nginx);

    Nginx nginxWeights(Nginx nginx);


}
