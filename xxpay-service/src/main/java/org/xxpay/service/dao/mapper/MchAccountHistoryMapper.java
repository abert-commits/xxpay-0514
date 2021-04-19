package org.xxpay.service.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchAccountHistory;
import org.xxpay.core.entity.MchAccountHistoryExample;

import java.util.Map;

public interface MchAccountHistoryMapper {
    int countByExample(MchAccountHistoryExample example);

    int deleteByExample(MchAccountHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MchAccountHistory record);

    int insertSelective(MchAccountHistory record);

    List<MchAccountHistory> selectByExample(MchAccountHistoryExample example);

    MchAccountHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MchAccountHistory record, @Param("example") MchAccountHistoryExample example);

    int updateByExample(@Param("record") MchAccountHistory record, @Param("example") MchAccountHistoryExample example);

    int updateByPrimaryKeySelective(MchAccountHistory record);

    int updateByPrimaryKey(MchAccountHistory record);

    /**
     * 查询商户待结算汇总数据
     * @param param
     * @return
     */
    Map selectSettDailyCollect4Mch(Map param);

    /**
     * 查询代理商待结算汇总数据
     * @param param
     * @return
     */
    Map selectSettDailyCollect4Agent(Map param);

    /**
     * 更新商户结算状态
     * @param param
     */
    void updateCompleteSett4Mch(Map param);

    /**
     * 更新代理商结算状态
     * @param param
     */
    void updateCompleteSett4Agent(Map param);

    /**
     * 查询代理商风险预存期内的待结算记录
     * @param param
     * @return
     */
    List<MchAccountHistory> selectNotSettCollect4Agent1(Map param);

    /**
     * 查询代理商风险预存期内的待结算记录
     * @param param
     * @return
     */
    List<MchAccountHistory> selectNotSettCollect4Agent2(Map param);

    /**
     * 统计数据
     * @param param
     * @return
     */
    Map count4Data(Map param);

    /**
     * 统计数据
     * @param param
     * @return
     */
	Map count4Data2(Map param);

	/**
     * 代理商分润排行
     * @param param
     * @return
     */
	List<Map> count4AgentProfitTop(Map param);

}