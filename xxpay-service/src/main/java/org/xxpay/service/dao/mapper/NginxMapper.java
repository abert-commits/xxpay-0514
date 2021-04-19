package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.NginxExample;

import java.util.List;

public interface NginxMapper {
    int countByExample(NginxExample example);

    int deleteByExample(NginxExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Nginx record);

    int insertSelective(Nginx record);

    List<Nginx> selectByExample(NginxExample example);

    Nginx selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Nginx record, @Param("example") NginxExample example);

    int updateByExample(@Param("record") Nginx record, @Param("example") NginxExample example);

    int updateByPrimaryKeySelective(Nginx record);

    int updateByPrimaryKey(Nginx record);
}