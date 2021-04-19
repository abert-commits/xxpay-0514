package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.alipay.AlipayPaymentService;
import org.xxpay.pay.service.RpcCommonService;

import java.net.URLDecoder;
import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
@Component
public abstract class BasePayment extends BaseService implements PaymentInterface {


    @Autowired
    public RpcCommonService rpcCommonService;

    @Autowired
    public PayConfig payConfig;

    public abstract String getChannelName();

    public String getOrderId(PayOrder payOrder) {
        return null;
    }

    public Long getAmount(PayOrder payOrder) {
        return null;
    }

    public JSONObject pay(PayOrder payOrder) {
        return null;
    }

    public JSONObject query(PayOrder payOrder) {
        return null;
    }

    public JSONObject close(PayOrder payOrder) {
        return null;
    }

    /**
     * 获取三方支付配置信息
     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
     *
     * @param payOrder
     * @return
     */
    public String getPayParam(PayOrder payOrder) {
        String payParam = "";
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
        if (payPassageAccount != null && payPassageAccount.getStatus() == MchConstant.PUB_YES) {
            payParam = payPassageAccount.getParam();
        }
        if (StringUtils.isBlank(payParam)) {
            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
        }
        return payParam;
    }




    public String GetRandMPid(String payOrderId) {
        List<String> list = new LinkedList<>();
        list.add("2088122694910540");
        list.add("2088902269803450");
        list.add("2088312737886370");
        list.add("2088132289744280");
        list.add("2088622834578690");
        list.add("2088122685418730");
        list.add("2088222361589490");
        list.add("2088022921230190");
        list.add("2088702239856340");
        list.add("2088622865690320");
        list.add("2088422351724020");
        list.add("2088622485672390");
        list.add("2088332663836680");
        list.add("2088112451478180");
        list.add("2088522148506320");
        list.add("2088432964899070");
        list.add("2088822654296240");
        list.add("2088202240920180");
        list.add("2088522714391600");
        list.add("2088432988976590");
        list.add("2088232509117900");
        list.add("2088522795084220");
        list.add("2088722956480460");
        list.add("2088312140581020");
        list.add("2088732694018020");
        list.add("2088722559676350");
        list.add("2088032725879420");
        list.add("2088132136202010");
        list.add("2088912434154450");
        list.add("2088812772712990");
        list.add("2088912577875640");
        list.add("2088722399804930");
        list.add("2088432415001680");
        list.add("2088612435638290");
        list.add("2088512795617920");
        list.add("2088532571176350");
        list.add("2088422733603860");
        list.add("2088812230300640");
        list.add("2088032614879870");
        list.add("2088612190987020");
        list.add("2088222380905770");
        list.add("2088722115960860");
        list.add("2088732208028560");
        list.add("2088512329579470");
        list.add("2088422993447740");
        list.add("2088222559038450");
        list.add("2088622782516960");
        list.add("2088732613157440");
        list.add("2088012520219060");
        list.add("2088622512938780");
        list.add("2088622854574420");
        list.add("2088012162961830");
        list.add("2088922354968260");
        list.add("2088012169317650");
        list.add("2088632064155340");
        list.add("2088222844077670");
        list.add("2088432754275550");
        list.add("2088332259563380");
        list.add("2088122762860060");
        list.add("2088722049151170");
        list.add("2088632563617030");
        list.add("2088032906537920");
        list.add("2088122550877960");
        list.add("2088722415686130");
        list.add("2088522084858000");
        list.add("2088802589615530");
        list.add("2088232593507720");
        list.add("2088522068256640");
        list.add("2088912581419580");
        list.add("2088522319161250");
        list.add("2088902898039390");
        list.add("2088022993817000");
        list.add("2088222294312950");
        list.add("2088922854602360");
        list.add("2088802598502010");
        list.add("2088122050979060");
        list.add("2088512718547910");
        list.add("2088022138465080");
        list.add("2088622892092060");
        list.add("2088522089016260");
        list.add("2088022365331560");
        list.add("2088722235816640");
        list.add("2088132833643270");
        list.add("2088432490633090");
        list.add("2088532362776770");
        list.add("2088912982275510");
        list.add("2088522473606150");
        list.add("2088432930187390");
        list.add("2088122606417830");
        list.add("2088532931115610");
        list.add("2088222850089910");
        list.add("2088722326984730");
        list.add("2088012196492240");
        list.add("2088712174140120");
        list.add("2088702724500040");
        list.add("2088232994515540");
        list.add("2088032294825500");
        list.add("2088132813925190");
        list.add("2088632968633670");
        list.add("2088532138736940");
        list.add("2088132840618950");
        int num = (int) (Math.random() * 101);
        int yu = num % 100;
        return list.get(yu);
    }
}
