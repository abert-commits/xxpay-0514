package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.WxUser;
import org.xxpay.core.entity.WxUserExample;

import java.util.List;
import java.util.Map;

public interface WxUserMapper {
    int countByExample(WxUserExample example);

    int deleteByExample(WxUserExample example);

    int deleteByPrimaryKey(Long userId);

    int insert(WxUser record);

    int insertSelective(WxUser record);

    List<WxUser> selectByExampleWithBLOBs(WxUserExample example);

    List<WxUser> selectByExample(WxUserExample example);

    WxUser selectByPrimaryKey(Long userId);

    int updateByExampleSelective(@Param("record") WxUser record, @Param("example") WxUserExample example);

    int updateByExampleWithBLOBs(@Param("record") WxUser record, @Param("example") WxUserExample example);

    int updateByExample(@Param("record") WxUser record, @Param("example") WxUserExample example);

    int updateByPrimaryKeySelective(WxUser record);

    int updateByPrimaryKeyWithBLOBs(WxUser record);

    int updateByPrimaryKey(WxUser record);

    /**
     * 得到一个有效的收款用户
     * @param param
     * @return
     */
    WxUser selectByAvailable(Map param);

    /**
     * 更新今日收款数据
     * @param param
     * @return
     */
    int updateDayByRandomId(Map param);

    /**
     * 初始今日收款数据
     * @param param
     * @return
     */
    int updateDayByInit(Map param);
}