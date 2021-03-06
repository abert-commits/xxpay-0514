package org.xxpay.core.service;

import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.PayDataStatistics;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderExport;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
public interface IPayOrderService {

    PayOrder find(PayOrder payOrder);

    PayOrder findByPayOrderId(String payOrderId);

    PayOrder findByMchIdAndPayOrderId(Long mchId, String payOrderId);

    PayOrder findByMchOrderNo(String mchOrderNo);

    List<PayOrder> select(Long mchId, int offset, int limit, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd);

    List<PayOrder> select(int offset, int limit, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd);

    List<PayOrderExport> exportOrder(int offset, int limit, PayOrderExport payOrderExport);

    Integer count(Long mchId, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd);

    Integer count(PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd);

    Integer count(PayOrder payOrder, List<Byte> statusList);

    int updateByPayOrderId(String payOrderId, PayOrder payOrder);

    Long sumAmount(PayOrder payOrder, List<Byte> statusList);

    List<PayOrder> select(String channelMchId, String billDate, List<Byte> statusList);

    int updateStatus4Ing(String payOrderId, String channelOrderNo);

    int updateStatus4Ing(String payOrderId, String channelOrderNo, String channelAttach);

    int updateStatus4Success(String payOrderId);

    int updateStatus4Fail(String payOrderId);

    void updateCodeAndErrorMessage(PayOrder payOrder);

    int updateStatus4Success(String payOrderId, String channelOrderNo);

    int updateStatus4Success(String payOrderId, String channelOrderNo, String channelAttach);

    int updateStatus4Complete(String payOrderId);

    int createPayOrder(PayOrder payOrder);

    PayOrder selectPayOrder(String payOrderId);

    PayOrder selectByMchIdAndPayOrderId(Long mchId, String payOrderId);

    PayOrder selectByMchIdAndMchOrderNo(Long mchId, String mchOrderNo);

    // ??????????????????(????????????????????????????????????)
    List<PayOrder> selectAllBill(int offset, int limit, String billDate);

    /**
     * ????????????
     *
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    Map count4Income(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd);

    /**
     * ??????????????????
     *
     * @param agentId
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    List<Map> count4MchTop(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd);

    /**
     * ?????????????????????
     * @param createTimeStart
     * @param createTimeEnd
     * @param createTimeEnd2
     * @param createTimeStart2
     * @return
     */
    /*List<Map> count4AgentTop(String agentId, String bizType, String createTimeStart, String createTimeEnd);*/

    /**
     * ????????????(idName?????????passageId,productId,channelType,channelId)
     *
     * @param idName
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    List<Map> count4Pay(String idName, String createTimeStart, String createTimeEnd);

    List<Map> count4PayProduct(String createTimeStart, String createTimeEnd);

    Long count4PayProducts(String createTimeStart, String createTimeEnd);

    List<PayDataStatistics> count5DataStatistics(int offset, int limit, PayDataStatistics payDataStatistics);

    Long countDataStatisAmount(int offset, int limit, PayDataStatistics payDataStatistics);

    List<PayDataStatistics> merchanttopupdata(int offset, int limit, PayDataStatistics payDataStatistics);

    /**
     * ????????????????????????????????????
     *
     * @param payPassageAccountId
     * @param creatTimeStart
     * @param createTimeEnd
     * @return
     */
    Long sumAmount4PayPassageAccount(int payPassageAccountId, Date creatTimeStart, Date createTimeEnd);

    Map count4All(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount);

    Map count4Success(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount);

    Map count4Fail(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount);

    List<Map> daySuccessRate(int offset, int limit, String createTimeStart, String createTimeEnd, Long mchId);

    List<Map> hourSuccessRate(int offset, int limit, String createTimeStart, String createTimeEnd, Long mchId);

    Map<String, Object> countDaySuccessRate(String createTimeStart, String createTimeEnd, Long mchId);

    Map<String, Object> countHourSuccessRate(String createTimeStart, String createTimeEnd, Long mchId);

    Map dateRate(String dayStart, String dayEnd);

    Map hourRate(String dayStart, String dayEnd);

    Map orderDayAmount(Long mchId, String dayStart, String dayEnd);

    /**
     * ??????????????????????????????
     *
     * @param payOrderId
     * @param timeOut    ??????????????????????????????
     * @return
     */
    Long getOrderTimeLeft(String payOrderId, Long timeOut);

    /**
     * ??????????????????(?????????????????????????????????????????????)
     *
     * @param payOrder
     * @return
     */
    Long getAvailableAmount(PayOrder payOrder, Long payTimeOut, Long incrRange, Long incrStep);

    /**
     * ??????????????????????????????(????????????????????????????????????????????????????????????????????????????????????)
     *
     * @param amount
     * @param rightCardNo
     * @param payTimeOut
     * @return
     */
    PayOrder findByAmount(Long amount, String rightCardNo, Long payTimeOut);


//    <if test="agentId != null">AND AgentId = #{agentId}</if>
//    <if test="pid != null">AND Param1 = #{pid}</if>
//    <if test="createTimeStart != null">AND CreateTime &gt;= #{createTimeStart}</if>
//    <if test="createTimeEnd != null">AND CreateTime &lt;= #{createTimeEnd}</if>


    /**
     * ???????????????????????????????????????
     *
     * @param
     * @return
     */
    Map count4AllByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd);


    /**
     * ???????????????????????????????????????
     *
     * @param
     * @return
     */
    Map count4SuccessByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd);


    /**
     * ???????????????????????????????????????
     *
     * @param
     * @return
     */
    Map count4FailByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd);


    int updateOrderSuccess(String payOrderId, String channelOrderNo);

    /**
     * ??????????????????
     *
     * @param
     * @return
     */
    Map count4Refund(Map map);




    //?????????????????????  ?????????????????????

    /**
     * ????????????
     *
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    Map mchCount4Income(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd);


    //??????
    Map mchCount4Success(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount);


}
