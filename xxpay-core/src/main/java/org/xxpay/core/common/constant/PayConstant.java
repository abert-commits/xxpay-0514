package org.xxpay.core.common.constant;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @Description: 支付常量类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public class PayConstant {

	public final static String PAY_CHANNEL_HX_PAOFEN="paofen";

	public final static String PAY_CHANNEL_WX_MICROPAY = "wxpay_micropay"; 			// 微信刷卡支付
	public final static String PAY_CHANNEL_WX_JSAPI = "wxpay_jsapi"; 				// 微信公众号支付
	public final static String PAY_CHANNEL_WX_NATIVE = "wxpay_native";				// 微信原生扫码支付
	public final static String PAY_CHANNEL_WX_APP = "wxpay_app";					// 微信APP支付
	public final static String PAY_CHANNEL_WX_MWEB = "wxpay_mweb";					// 微信H5支付
	public final static String PAY_CHANNEL_IAP = "iap";							// 苹果应用内支付
	public final static String PAY_CHANNEL_ALIPAY_MOBILE = "alipay_mobile";		// 支付宝移动支付
	public final static String PAY_CHANNEL_ALIPAY_PC = "alipay_pc";	    		// 支付宝PC支付
	public final static String PAY_CHANNEL_ALIPAY_WAP = "alipay_wap";	    	// 支付宝WAP支付
	public final static String PAY_CHANNEL_ALIPAY_QR = "alipay_qr";	    		// 支付宝当面付之扫码支付
	public final static String PAY_CHANNEL_ALIPAY_BAR = "alipay_bar";	    	// 支付宝当面付之条码支付

	public final static String CHANNEL_NAME_WXPAY = "wxpay"; 				// 渠道名称:微信
	public final static String CHANNEL_NAME_ALIPAY = "alipay"; 				// 渠道名称:支付宝
	public final static String CHANNEL_NAME_JDPAY = "jdpay"; 				// 渠道名称:京东
	public final static String CHANNEL_NAME_KQPAY = "kqpay"; 				// 渠道名称:快钱
	public final static String CHANNEL_NAME_SWIFTPAY = "swiftpay"; 			// 渠道名称:威富通
	public final static String CHANNEL_NAME_GOMEPAY = "gomepay"; 			// 渠道名称:银盈通
	public final static String CHANNEL_NAME_ACCOUNTPAY = "accountpay"; 	    // 渠道名称:账户支付
	public final static String CHANNEL_NAME_SANDPAY = "sandpay"; 	    	// 渠道名称:杉德支付
	public final static String CHANNEL_NAME_SICPAY = "sicpay"; 	    		// 渠道名称:高汇通支付
	public final static String CHANNEL_NAME_MAXPAY = "maxpay"; 	    		// 渠道名称:拉卡拉支付
	public final static String CHANNEL_NAME_SILVERSPAY = "silverspay"; 		// 渠道名称:睿联支付
	public final static String CHANNEL_NAME_TRANSFARPAY = "transfarpay"; 	// 渠道名称:传化支付
	public final static String CHANNEL_NAME_HCPAY = "hcpay"; 				// 渠道名称:汇潮支付
	public final static String CHANNEL_NAME_YYKPAY = "yykpay"; 				// 渠道名称:易游酷支付
	public final static String CHANNEL_NAME_MAYIPAY = "mayipay"; 		    // 渠道名称:蚂蚁支付
	public final static String CHANNEL_NAME_AFEI = "afei"; 		    		// 渠道名称:阿飞支付
	public final static String CHANNEL_NAME_DAMI = "dami"; 		    		// 渠道名称:大咪闲鱼
	public final static String CHANNEL_NAME_XINJIE = "xinjie"; 		    		// 渠道名称:信捷微信直充
	public final static String CHANNEL_NAME_XIAOMF = "xiaomf"; 		    		// 渠道名称:小蜜蜂支付
	public final static String CHANNEL_NAME_ZHUANPAY = "zhuanpay"; 		    // 渠道名称:赚赚支付
	public final static String CHANNEL_NAME_QUZHIFU = "qupay"; 		    // 渠道名称:趣支付
	public final static String CHANNEL_NAME_HUIJINPAY = "huijinpay"; 		    // 渠道名称:汇锦支付
	public final static String CHANNEL_NAME_BIANFU = "bianfupay"; 		    // 渠道名称:便付支付
	public final static String CHANNEL_NAME_YUANBAO	 = "yuanbaopay"; 		    // 渠道名称:元宝支付
	public final static String CHANNEL_NAME_YINHEPAY = "yinhepay"; 		    // 渠道名称:银河支付
	public final static String CHANNEL_NAME_JINHUIAY = "jinhuipay"; 		// 渠道名称:金汇支付
	public final static String CHANNEL_NAME_LONGGE = "longgepay"; 		// 渠道名称:龙哥支付
	public final static String CHANNEL_NAME_FG= "fgpay"; 		// 渠道名称:fg支付
	public final static String CHANNEL_NAME_HUANYA= "huanyapay"; 		// 渠道名称:环亚支付
	public final static String CHANNEL_NAME_FEIBAO= "feibao"; 		// 渠道名称:飞宝支付
	public final static String CHANNEL_NAME_XINGCHEN= "xingchen"; 		// 渠道名称:星辰支付
	public final static String CHANNEL_NAME_EIGHTYEIGHT= "eightyeight"; 		// 渠道名称:星辰支付
	public final static String CHANNEL_NAME_LALA= "lala"; 		// 渠道名称:拉拉现金红包
	public final static String CHANNEL_NAME_KANGXI= "kangxi"; 		// 渠道名称:拉拉现金红包
	public final static String CHANNEL_NAME_QINGSONG= "qingsong"; 		// 渠道名称:轻松医疗码
	public final static String CHANNEL_NAME_KUKU= "kuku"; 		// 渠道名称:酷酷支付
	public final static String CHANNEL_NAME_FEIFAN= "feifan"; 		// 渠道名称:非凡闲鱼
	public final static String CHANNEL_NAME_XIAOTAOZI= "xiaotaozi"; 		// 渠道名称:小桃子
	public final static String CHANNEL_NAME_QPAY= "qpay"; 		// 渠道名称:Q支付
	public final static String CHANNEL_NAME_JUN= "jun"; 		// 渠道名称:俊支付
	public final static String CHANNEL_NAME_PHONEBILL= "phonebill"; 		// 渠道名称:韩信租用直充
	public final static String CHANNEL_NAME_QIANGU= "qiangu"; 		// 渠道名称:千古闲鱼
	public final static String CHANNEL_NAME_FUYOU= "fuyou"; 		// 渠道名称:富有支付
	public final static String CHANNEL_NAME_XIANYU= "xianyu"; 		// 渠道名称:韩信咸鱼支付
	public final static String CHANNEL_NAME_HXIANYU= "hxianyu"; 		// 渠道名称:韩信咸鱼支付
	public final static String CHANNEL_NAME_WUXING= "wuxing"; 		// 渠道名称:五行微信话费直充
	public final static String CHANNEL_NAME_HXBILL= "hxbill"; 		// 渠道名称:韩信直充
	public final static String CHANNEL_NAME_HAITIAN= "haitian"; 		// 渠道名称:海天
	public final static String CHANNEL_NAME_ZAOXIA= "zaoxiapay"; 		// 渠道名称:造虾支付
	public final static String CHANNEL_NAME_HONGYUN= "hongyunpay"; 		// 渠道名称:鸿运支付
	public final static String CHANNEL_NAME_JIUPAY= "jiupay"; 		// 渠道名称:玖玖付
	public final static String CHANNEL_NAME_JUHE= "juhepay"; 		// 渠道名称:聚合支付
	public final static String CHANNEL_NAME_TENGAN= "tengan"; 		// 渠道名称:腾安支付
	public final static String CHANNEL_NAME_LIKE= "likepay"; 		// 渠道名称:立刻支付
	public final static String CHANNEL_NAME_TIANXIAN= "tianxianpay"; 		// 渠道名称:天衔支付
	public final static String CHANNEL_NAME_YUNKUOPAY= "yunkuopay"; 		// 渠道名称:云阔支付
	public final static String CHANNEL_NAME_QINGLONG= "qinglongpay"; 		// 渠道名称:青龙支付
	public final static String CHANNEL_NAME_BEIBEILE= "beibeilepay"; 		// 渠道名称:贝贝支付
	public final static String CHANNEL_NAME_WUDUPAY= "wudupay"; 		// 渠道名称:雾都支付
	public final static String CHANNEL_NAME_SHANCHENG= "shancheng"; 		// 渠道名称:山城支付
	public final static String CHANNEL_NAME_FISH= "fish"; 		// 渠道名称:鱼儿支付
	public final static String CHANNEL_NAME_FEIYA= "feiya"; 		// 渠道名称:飞亚支付
	public final static String CHANNEL_NAME_HUIYING= "huiyingpay"; 		// 渠道名称:汇盈支付
	public final static String CHANNEL_NAME_JUFENG= "jufengpay"; 		// 渠道名称:飓风支付
	public final static String CHANNEL_NAME_WUER= "wuerpay"; 		// 渠道名称:五二支付
    public final static String CHANNEL_NAME_TIANSHI= "tianshipay"; 		// 渠道名称:天使支付
	public final static String CHANNEL_NAME_GUAGNSU= "guangsupay"; 		// 渠道名称:光速支付
	public final static String CHANNEL_NAME_LONGTONG= "longtongpay"; 		// 渠道名称:隆通支付
	public final static String CHANNEL_NAME_DACAISHEN= "dacaispay"; 		// 渠道名称:大财神支付
	public final static String CHANNEL_NAME_AM= "ampay"; 		// 渠道名称:阿姆小支付
    public final static String CHANNEL_NAME_YONGHONG= "yonghongpay"; 		// 渠道名称:勇弘支付
	public final static String CHANNEL_NAME_JIUZUN= "jiuzunpay"; 		// 渠道名称:九尊支付
	public final static String CHANNEL_NAME_JUJIN= "jujinpay"; 		// 渠道名称:聚金支付
	public final static String CHANNEL_NAME_XINAN= "xinanpay"; 		// 渠道名称:信安支付
	public final static String CHANNEL_NAME_DADA= "dadapay"; 		// 渠道名称:DD支付
	public final static String CHANNEL_NAME_JINDA= "jindapay"; 		// 渠道名称:金达支付
	public final static String CHANNEL_NAME_LG= "lgpay"; 		// 渠道名称:聚合支付
	public final static String CHANNEL_NAME_HUINONG= "huinongpay"; 		// 渠道名称:惠农支付
	public final static String CHANNEL_NAME_YIQIANPAY= "yiqianpay"; 		// 渠道名称:易乾支付
	public final static String CHANNEL_NAME_BRIA= "bripay"; 		// 渠道名称:bri支付
	public final static String CHANNEL_NAME_DOUDOU= "Doudoupay"; 		// 渠道名称:豆豆支付
	public final static String CHANNEL_NAME_ZHUROUPAY = "zhuroupay"; 		// 渠道名称:猪肉支付
	public final static String CHANNEL_NAME_BAOTONG= "baotongpay"; 		// 渠道名称:宝通支付
	public final static String CHANNEL_NAME_DABAO= "dabaopay"; 		// 渠道名称:大宝支付
	public final static String CHANNEL_NAME_YIZHIFU= "yizpay"; 		// 渠道名称:易支支付
	public final static String CHANNEL_NAME_MIDOU= "midoupay"; 		// 渠道名称:米兜支付
	public final static String CHANNEL_NAME_QIFU= "qifupay"; 		// 渠道名称:启富支付
	public final static String CHANNEL_NAME_TANTAN= "tantanpay"; 		// 渠道名称:泰坦支付
	public final static String CHANNEL_NAME_XUNFUPAY= "xunfupay"; 		// 渠道名称:迅付支付
	public final static String CHANNEL_NAME_FAGUANG= "fagpay"; 		// 渠道名称:发光支付
	public final static String CHANNEL_NAME_HB= "hbpay"; 		// 渠道名称:HB支付
	public final static String CHANNEL_NAME_LAIKE= "laikepay"; 		// 渠道名称:莱兄支付
	public final static String CHANNEL_NAME_GOFU= "gofupay"; 		// 渠道名称:GO支付
	public final static String CHANNEL_NAME_DAYU= "dayupay"; 		// 渠道名称:大宇支付
	public final static String CHANNEL_NAME_LEITING= "leitingpay"; 		// 渠道名称:雷霆支付
	public final static String CHANNEL_NAME_ZHANCHE= "zhanchepay"; 		// 渠道名称:战车支付
    public final static String CHANNEL_NAME_WUYI= "wuyi"; 		// 渠道名称:五一支付
    public final static String CHANNEL_NAME_HUANY= "huanypay"; 		// 渠道名称:大宇支付
	public final static String CHANNEL_NAME_YIFU= "yifupay"; 		// 渠道名称:翼付支付
	public final static String CHANNEL_NAME_HUAFEI= "huafeipay"; 		// 渠道名称:华非支付
	public final static String CHANNEL_NAME_HENRY= "henrypay"; 		// 渠道名称:亨利支付
	public final static String CHANNEL_NAME_DINGH= "dinghongpay"; 		// 渠道名称:鼎红支付
    public final static String CHANNEL_NAME_DINGSHEN= "dingshengpay"; 		// 渠道名称:鼎红支付
	public final static String CHANNEL_NAME_ANDAPAY= "andapay"; 		// 渠道名称:安达支付
	public final static String CHANNEL_NAME_YISHENG= "yishengpay"; 		// 渠道名称:亿盛支付
	public final static String CHANNEL_NAME_YUNFEIPAY= "yunfeipay"; 		// 渠道名称:云飞支付
	public final static String CHANNEL_NAME_ANYI= "anyipay"; 		// 渠道名称:安逸支付
	public final static String CHANNEL_NAME_WANGXIANG= "wanxpay"; 		// 渠道名称:万象支付
	public final static String CHANNEL_NAME_XINFU= "xinfupay"; 		// 渠道名称:新富支付
	public final static String CHANNEL_NAME_JIASHENG= "jiaspay"; 		// 渠道名称:嘉生支付
	public final static String CHANNEL_NAME_EGM= "egmpay"; 		// 渠道名称:EGM支付
    public final static String CHANNEL_NAME_XIAOAO= "xiaoaopay"; 		// 渠道名称:笑傲EGM支付
	public final static String CHANNEL_NAME_SHUNFU= "shunfupay"; 		// 渠道名称:瞬付支付
    public final static String CHANNEL_NAME_LIYANG= "liyangpay"; 		// 渠道名称:黎阳支付
	public final static String CHANNEL_NAME_JUFUTONG= "jufutpay"; 		// 渠道名称:聚富通支付
	public final static String CHANNEL_NAME_JUNYUN= "juyunpay"; 		// 渠道名称:聚云支付
	public final static String CHANNEL_NAME_DASHEN= "dashenpay"; 		// 渠道名称:大神支付
	public final static String CHANNEL_NAME_MASHANG= "mashangpay"; 		// 渠道名称:马上支付
	public final static String CHANNEL_NAME_YuanYuan= "yuanyuan"; 		// 渠道名称:yuanyuan支付

	public final static String CHANNEL_NAME_DAFA= "dafapay"; 		// 渠道名称:大发支付
	public final static String CHANNEL_NAME_XIAOCAO= "xiaocao"; 		// 渠道名称:大发支付
	public final static String CHANNEL_NAME_HUOJI= "huojipay"; 		// 渠道名称:火鸡支付
	public final static String CHANNEL_NAME_CC= "cc"; 				// 渠道名称:cc支付
	public final static String CHANNEL_NAME_LANGCHAO= "langchao"; 		// 渠道名称:浪潮支付
	public final static String CHANNEL_NAME_AFANDA= "afanda"; 		// 渠道名称:大发支付
	public final static String CHANNEL_NAME_GOUIQI= "gouqipay"; 		// 渠道名称:枸杞支付
	public final static String CHANNEL_NAME_DAPAO= "dapao"; 		// 渠道名称:大炮支付
	public final static String CHANNEL_NAME_HANTIAOTIAO= "hantiaotiao"; 		// 渠道名称:韩信
	public final static String CHANNEL_NAME_QIANGUI= "qianguipay"; 		// 渠道名称:钱柜支付
	public final static String CHANNEL_NAME_TTPAY= "ttpay"; 		// 渠道名称:TT支付
    public final static String CHANNEL_NAME_JIAOWAWA= "jiaowapay"; 		// 渠道名称:叫娃支付
	public final static String CHANNEL_NAME_FENGXIANG= "fengxpay"; 		// 渠道名称:风向支付
	public final static String CHANNEL_NAME_DATONG= "datongpay"; 		// 渠道名称:大通支付
	public final static String CHANNEL_NAME_LIANZHONG= "lianzpay"; 		// 渠道名称:联众支付
	public final static String CHANNEL_NAME_LILI= "lili"; 		// 渠道名称:丽丽现金红包  xingfukejiPaymentService
	public final static String CHANNEL_NAME_SANYE= "sanye"; 		// 渠道名称:三叶支付
	public final static String CHANNEL_NAME_FUNIUKEJI= "funiukeji"; 		// 渠道名称:富牛科技
	public final static String CHANNEL_NAME_BINGZHANG= "bingzhang"; 		// 渠道名称：兵长双通道
	public final static String CHANNEL_NAME_FULIAN= "fulianpay"; 		// 渠道名称:付联支付
	public final static String CHANNEL_NAME_SHUNFENG= "shunfengpay"; 		// 渠道名称:顺丰支付
	public final static String CHANNEL_NAME_WAWAPAY= "wawapay"; 		// 渠道名称:娃娃支付
	public final static String CHANNEL_NAME_HAIZEIWPAY= "haizeiw"; 		// 渠道名称:海贼王支付
	public final static String CHANNEL_NAME_BAIBAI= "baibaipay"; 		// 渠道名称:摆摆支付
	public final static String CHANNEL_NAME_JUNZHIFU= "junpay"; 		// 渠道名称:俊支付
	public final static String CHANNEL_NAME_WUHUANG= "wuhuangpay"; 		// 渠道名称:吾皇支付
	public final static String CHANNEL_NAME_HUATONGFU= "huatongfupay"; 		// 渠道名称: 华通付
	public final static String CHANNEL_NAME_JIANDAN= "jiandanpay"; 		// 渠道名称: 简单付
	public final static String CHANNEL_NAME_NEWFEIFAN= "newfeifan"; 		// 渠道名称: 新非凡
	public final static String CHANNEL_NAME_KAKA = "kakapay"; 		    // 渠道名称:卡卡支付

	public final static String CHANNEL_NAME_TAIYANG= "taiyangpay"; 		// 渠道名称: 太阳支付
	public final static String CHANNEL_NAME_ABC= "abcpay"; 		// 渠道名称: ABC支付
	public final static String CHANNEL_NAME_AXIANG= "axiangpay"; 		// 渠道名称: 阿翔支付
	public final static String CHANNEL_NAME_KUAICAITONG= "kuaicaitongpay"; 		// 渠道名称: 阿翔支付
	public final static String CHANNEL_NAME_YINGDI= "yingdi"; 		// 渠道名称: 影帝支付
	public final static String CHANNEL_NAME_YILIPAY= "yilipay"; 		// 渠道名称: 一粒米支付
	public final static String CHANNEL_NAME_EFU= "efupay"; 		// 渠道名称: e付
	public final static String CHANNEL_NAME_YPAY= "Ypay"; 		// 渠道名称: y付
	public final static String CHANNEL_NAME_PUSIPAY= "pusipay"; 		// 渠道名称: 普思支付
	public final static String CHANNEL_NAME_KAIXINPAY= "kaixinpay"; 		// 渠道名称: 开心付
	public final static String CHANNEL_NAME_JUYIXINPAY= "juyixinpay"; 		// 渠道名称: 聚义鑫
	public final static String CHANNEL_NAME_WAWAJIAOPAY = "wawajiaopay"; 		    // 渠道名称:娃娃叫支付
	public final static String CHANNEL_NAME_QIAOQIAO = "qiaoqiao"; 		    // 渠道名称:巧巧支付
	public final static String CHANNEL_NAME_AWEN = "awen"; 		    // 渠道名称:阿文支付
	public final static String CHANNEL_NAME_BX = "bxpay"; 		    // 渠道名称:Bx支付
	public final static String CHANNEL_NAME_LADING = "lading"; 			// 拉丁支付

	public final static String CHANNEL_NAME_XXO = "xxopay"; 		    // 渠道名称:xxo支付
	public final static String CHANNEL_NAME_XUANWU = "xuanwu"; 		    // 渠道名称:玄武支付
	public final static String CHANNEL_NAME_MYZF = "myzf"; 		    // 渠道名称:观音桥转卡支付
	public final static String CHANNEL_NAME_YIBAOHUAFEI = "yibpay"; 		    // 渠道名称:易宝话费支付
    public final static String CHANNEL_NAME_YT = "ytpay"; 		    // 渠道名称:YT支付
	public final static String CHANNEL_NAME_XINGDA = "xingda"; 		    // 渠道名称:兴达支付
	public final static String CHANNEL_NAME_CHUANQI= "chuanqi"; 		    // 渠道名称:传奇支付
	public final static String CHANNEL_NAME_QT= "qt"; 		    // 渠道名称:qt支付
	public final static String CHANNEL_NAME_JIUYUE= "jiuyue"; 		    // 渠道名称:九月支付
	public final static String CHANNEL_NAME_SHUNAN= "shunanpay"; 		// 渠道名称:顺安支付
	public final static String CHANNEL_NAME_JPAY = "jpay"; 		    // 渠道名称:jpay支付
	public final static String CHANNEL_NAME_WOPAY = "wo"; 		    // 渠道名称:沃支付
	public final static String CHANNEL_NAME_AIZHIFU = "aizhifu"; 		    // 渠道名称:爱支付支付
	public final static String CHANNEL_NAME_LIBAI = "libai"; 		    // 渠道名称:李白支付
	public final static String CHANNEL_NAME_ZHONGYING = "zhongyin"; 		    // 渠道名称:中银支付
	public final static String CHANNEL_NAME_HUITENG = "huiteng"; 		    // 渠道名称:辉腾支付
	public final static String CHANNEL_NAME_AAA = "Aaapay"; 		    // 渠道名称:AAA支付
	public final static String CHANNEL_NAME_AAAA = "Aaaapay"; 		    // 渠道名称:AAAA支付
	public final static String CHANNEL_NAME_PINDUODUO= "pinduoduo"; 		    // 渠道名称:拼多多支付
	public final static String CHANNEL_NAME_YUANZHIFU= "yuanzhifu"; 		    // 渠道名称:源支付
	public final static String CHANNEL_NAME_ZHUBAO= "zhubao"; 		    // 渠道名称:源支付
	public final static String CHANNEL_NAME_RUIZHIPAY= "ruizhifu"; 		    // 渠道名称:瑞支付
	public final static String CHANNEL_NAME_ERZHANGGUI= "erzhanggui"; 		    // 渠道名称:二掌柜支付
	public final static String CHANNEL_NAME_ENZUO= "enzuo"; 		// 渠道名称:恩佐支付
	public final static String CHANNEL_NAME_HANXIN= "Hxgames"; 		// 渠道名称:天选支付
	public final static String CHANNEL_NAME_LIGE= "lige"; 		// 渠道名称:黎哥支付

	public final static String CHANNEL_NAME_YAFEI= "YAFEI"; 		// 渠道名称 亚非支付
	public final static String CHANNEL_NAME_HAHA= "haha"; 		// 渠道名称: 哈哈支付
	public final static String CHANNEL_NAME_YIBAO= "YIBAO"; 		// 渠道名称: 易宝支付
	public final static String CHANNEL_NAME_AIPAY= "aipay"; 		// 渠道名称: 艾支付
	public final static String CHANNEL_NAME_QQPAY= "qqpay"; 		// 渠道名称: QQ支付
	public final static String CHANNEL_NAME_MAYUN= "mayun"; 		// 渠道名称: 马云支付

	public final static String CHANNEL_NAME_TIANGON= "tiangon"; 		// 渠道名称: 天宫支付

	public final static String CHANNEL_NAME_DUODUO= "duoduo"; 		// 渠道名称: 多多支付

	public final static String CHANNEL_NAME_LONGTENG= "longteng"; 		// 渠道名称: 龙腾支付
	public final static String CHANNEL_NAME_YILIANBAO= "yilianbao"; 		// 渠道名称: 易联宝支付
	public final static String CHANNEL_NAME_ZHONGLIAN= "zhonglpay"; 		// 渠道名称: 众联支付
	public final static String CHANNEL_NAME_ZHONGKONG= "zhongkong";					//   渠道名称：中控支付

	public final static String CHANNEL_NAME_YUNZHIFU= "yunpay"; 		// 渠道名称: 云支付

	public final static String CHANNEL_NAME_JJPAY= "jjpay"; 		// 渠道名称: JJ支付

	public final static String CHANNEL_NAME_HUICHAO= "huichao"; 		// 渠道名称: 汇潮支付


	public final static String CHANNEL_NAME_SHANGHE= "shanghe"; 		    // 渠道名称:商河支付

	public final static String CHANNEL_NAME_HONRUI= "honrui"; 		    // 渠道名称: 宏瑞支付

	public final static String CHANNEL_NAME_WAWA= "wawa"; 		    // 渠道名称: 宏瑞支付
	public final static String CHANNEL_NAME_FENGWANG= "fengwang"; 		    // 渠道名称: 峰王支付
	public final static String CHANNEL_NAME_XUNFEI= "xunfei"; 		    // 渠道名称: 讯飞支付
	public final static String CHANNEL_NAME_KAMI= "kami"; 		// 渠道名称:卡密支付
	public final static String CHANNEL_NAME_DANGXIAKAMI= "dangxiakami"; 		// 渠道名称:当下卡密支付

	public final static String CHANNEL_NAME_YIRUN= "YIRUN"; 		// 渠道名称:亿润支付
	public final static String CHANNEL_NAME_XIAOQIAN= "xiaoqpay"; 		// 渠道名称:小钱支付
	public final static String CHANNEL_NAME_HAIXING= "HAIXING"; 		// 渠道名称:  海鑫付

	public final static String CHANNEL_NAME_LIUWEI= "liuwei"; 		    // 渠道名称:六维支付

	public final static String CHANNEL_NAME_ADI= "adi"; 		// 渠道名称:  阿弟付

	public final static String PAY_CHANNEL_SWIFTPAY_WXPAY_NATIVE = CHANNEL_NAME_SWIFTPAY + "_wxpay_native";			// 威富通微信扫码
	public final static String PAY_CHANNEL_SWIFTPAY_ALIPAY_NATIVE = CHANNEL_NAME_SWIFTPAY + "_alipay_native";		// 威富通微支付宝扫码
	public final static String PAY_CHANNEL_SWIFTPAY_MICROPAY = CHANNEL_NAME_SWIFTPAY + "_micropay";					// 威富通统一刷卡

	public final static String PAY_CHANNEL_ACCOUNTPAY_BALANCE = CHANNEL_NAME_ACCOUNTPAY + "_balance";	    		// 账户支付余额支付

	public final static String PAY_CHANNEL_SANDPAY_AGENTPAY = CHANNEL_NAME_SANDPAY + "_agentpay";					// 杉德代付
	public final static String PAY_CHANNEL_SICPAY_AGENTPAY = CHANNEL_NAME_SICPAY + "_agentpay";						// 高汇通代付
	public final static String PAY_CHANNEL_MAXPAY_AGENTPAY = CHANNEL_NAME_MAXPAY + "_agentpay";						// 拉卡拉代付
	public final static String PAY_CHANNEL_TRANSFARPAY_AGENTPAY = CHANNEL_NAME_TRANSFARPAY + "_agentpay";			// 传化代付

	public final static String PAY_CHANNEL_SILVERSPAY_GATEWAY = CHANNEL_NAME_SILVERSPAY + "_gateway"; 				// 睿联支付(跳转网关快捷)
	public final static String PAY_CHANNEL_HCPAY_GATEWAY = CHANNEL_NAME_HCPAY + "_gateway"; 						// 汇潮支付(跳转网关快捷)
	public final static String PAY_CHANNEL_YYKPAY_CARD = CHANNEL_NAME_YYKPAY + "_card"; 							// 易游酷充值卡支付

	
	public final static byte PAY_STATUS_EXPIRED = -2; 	// 订单过期
	public final static byte PAY_STATUS_FAILED = -1; 	// 支付失败
	public final static byte PAY_STATUS_INIT = 0; 		// 初始态
	public final static byte PAY_STATUS_PAYING = 1; 	// 支付中
	public final static byte PAY_STATUS_SUCCESS = 2; 	// 支付成功
	public final static byte PAY_STATUS_COMPLETE = 3; 	// 业务完成
	public final static byte PAY_STATUS_REFUND = 4; 	// 已退款

	public final static byte TRANS_STATUS_INIT = 0; 		// 初始态
	public final static byte TRANS_STATUS_TRANING = 1; 		// 转账中
	public final static byte TRANS_STATUS_SUCCESS = 2; 		// 成功
	public final static byte TRANS_STATUS_FAIL = 3; 		// 失败
	public final static byte TRANS_STATUS_COMPLETE = 4; 	// 业务完成

	public final static byte TRANS_RESULT_INIT = 0; 		// 不确认结果
	public final static byte TRANS_RESULT_REFUNDING = 1; 	// 等待手动处理
	public final static byte TRANS_RESULT_SUCCESS = 2; 		// 确认成功
	public final static byte TRANS_RESULT_FAIL = 3; 		// 确认失败

	public final static byte REFUND_STATUS_INIT = 0; 		// 初始态
	public final static byte REFUND_STATUS_REFUNDING = 1; 	// 转账中
	public final static byte REFUND_STATUS_SUCCESS = 2; 	// 成功
	public final static byte REFUND_STATUS_FAIL = 3; 		// 失败
	public final static byte REFUND_STATUS_COMPLETE = 4; 	// 业务完成

	public final static byte REFUND_RESULT_INIT = 0; 		// 不确认结果
	public final static byte REFUND_RESULT_REFUNDING = 1; 	// 等待手动处理
	public final static byte REFUND_RESULT_SUCCESS = 2; 	// 确认成功
	public final static byte REFUND_RESULT_FAIL = 3; 		// 确认失败

	public final static byte AGENTPAY_STATUS_INIT = 0; 		// 待处理(初始态)
	public final static byte AGENTPAY_STATUS_ING = 1; 		// 代付中
	public final static byte AGENTPAY_STATUS_SUCCESS = 2; 	// 成功
	public final static byte AGENTPAY_STATUS_FAIL = 3; 		// 失败

	public final static String MCH_NOTIFY_TYPE_PAY = "1";		// 商户通知类型:支付订单
	public final static String MCH_NOTIFY_TYPE_TRANS = "2";		// 商户通知类型:转账订单
	public final static String MCH_NOTIFY_TYPE_REFUND = "3";	// 商户通知类型:退款订单
	public final static String MCH_NOTIFY_TYPE_AGENTPAY = "4";	// 商户通知类型:代付订单

	public final static byte MCH_NOTIFY_STATUS_NOTIFYING = 1;	// 通知中
	public final static byte MCH_NOTIFY_STATUS_SUCCESS = 2;		// 通知成功
	public final static byte MCH_NOTIFY_STATUS_FAIL = 3;		// 通知失败


	public final static String RESP_UTF8 = "UTF-8";			// 通知业务系统使用的编码

	public static final String RETURN_PARAM_RETCODE = "retCode";	// 通讯返回码
	public static final String RETURN_PARAM_RETMSG = "retMsg";
	public static final String RESULT_PARAM_RESCODE = "resCode";
	public static final String RESULT_PARAM_ERRCODE = "errCode";
	public static final String RESULT_PARAM_ERRDES = "errDes";
	public static final String RESULT_PARAM_SIGN = "sign";

	public static final String RETURN_VALUE_SUCCESS = "SUCCESS";
	public static final String RETURN_VALUE_FAIL = "FAIL";
	public static final String RETURN_VALUE_OK = "ok";
	public static final String RETURN_VALUE_OK_1 = "OK";

	public static final Integer RESULT_VALUE_SUCCESS = 0;
	public static final Integer RESULT_VALUE_FAIL = -1;

	public static final String RESPONSE_RESULT = "resResult";
	public static final String JUMP_URL = "jumpUrl";

	public static final String RETURN_ALIPAY_VALUE_SUCCESS = "success";
	public static final String RETURN_ALIPAY_VALUE_FAIL = "fail";

	public static final String RETURN_SWIFTPAY_VALUE_SUCCESS = "success";
	public static final String RETURN_SWIFTPAY_VALUE_FAIL = "fail";
	public static final String RETURN_SILVERSPAY_VALUE_SUCCESS = "ok";
	public static final String RETURN_HCPAY_VALUE_SUCCESS = "ok";
	public static final String RETURN_YYKPAY_VALUE_SUCCESS = "SUCCESS";
	public static final String RETURN_QQPAY_VALUE_SUCCESS = "allpay_success";

	public static class JdConstant {
		public final static String CONFIG_PATH = "jd" + File.separator + "jd";	// 京东支付配置文件路径
	}

	public static class WxConstant {
		public final static String TRADE_TYPE_APP = "APP";									// APP支付
		public final static String TRADE_TYPE_JSPAI = "JSAPI";								// 公众号支付或小程序支付
		public final static String TRADE_TYPE_NATIVE = "NATIVE";							// 原生扫码支付
		public final static String TRADE_TYPE_MWEB = "MWEB";								// H5支付

	}

	public static class IapConstant {
		public final static String CONFIG_PATH = "iap" + File.separator + "iap";		// 苹果应用内支付
	}

	public static class AlipayConstant {
		public final static String CONFIG_PATH = "alipay" + File.separator + "alipay";	// 支付宝移动支付
		public final static String TRADE_STATUS_WAIT = "WAIT_BUYER_PAY";		// 交易创建,等待买家付款
		public final static String TRADE_STATUS_CLOSED = "TRADE_CLOSED";		// 交易关闭
		public final static String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";		// 交易成功
		public final static String TRADE_STATUS_FINISHED = "TRADE_FINISHED";	// 交易成功且结束

	}

	public static final String NOTIFY_BUSI_PAY = "NOTIFY_VV_PAY_RES";
	public static final String NOTIFY_BUSI_TRANS = "NOTIFY_VV_TRANS_RES";

	public static final String PAY_METHOD_FORM_JUMP = "formJump";	// 表单跳转
	public static final String PAY_METHOD_SDK_JUMP = "sdkJump";	// 表单跳转
	public static final String PAY_METHOD_URL_JUMP = "urlJump";	// URL跳转

	public static final String PAY_METHOD_CODE_IMG = "codeImg";		// 二维码图片

	public static boolean retIsSuccess(JSONObject retObj) {
		if(retObj == null) return false;
		String value = retObj.getString(PayConstant.RETURN_PARAM_RETCODE);
		if(StringUtils.isBlank(value)) return false;
		return "success".equalsIgnoreCase(value);
	}


	public static class newTradeTypeConstant {
		public final static String TRADE_TYPE_ALIPAY = "ALIPAY";									// 支付宝WAP h5
		public final static String TRADE_TYPE_DANGDANG = "DANGDANG";									// 当当支付
		public final static String TRADE_TYPE_ALIPAY_PDD = "ALIPAY_PDD";							// 支付宝拼多多
		public final static String TRADE_TYPE_ALIPAY_PDDSDK = "ALIPAY_PDDSDK";							// 支付宝拼多多

		public final static String TRADE_TYPE_ALIPAY_XY = "ALIPAY_XY";							// 咸鱼代付

		public final static String TRADE_TYPE_ALIPAY_XJ = "ALIPAY_XJ";							    // 支付宝现金红包
		public final static String TRADE_TYPE_ALIPAY_SDK = "ALIPAY_SDK";							// 支付宝SDK
		public final static String TRADE_TYPE_ALIPAYQR = "ALIPAYQR";								// 支付宝扫码
		public final static String TRADE_TYPE_WXPAY_SDK = "WXPAY_SDK";								// 微信SDK

		public final static String TRADE_TYPE_WXPAY_PDD = "WXPAY_PDD";								// 微信拼多多
		public final static String TRADE_TYPE_WXPAY = "WXPAY";							        	// 微信 WAP h5
		public final static String TRADE_TYPE_WXPAYZXQR = "WXPAYZXQR";								// 微信扫码
		public final static String TRADE_TYPE_YUNPAY = "YUNPAY";								    // 云闪付WAP h5
		public final static String TRADE_TYPE_YUNPAYQR = "YUNPAYQR";								// 云闪付扫码
		public final static String TRADE_TYPE_UnionPay = "UnionPay";								// 网关支付
		public final static String TRADE_TYPE_QQ_QR = "QQ_QR";								   //  QQ 扫码
		public final static String TRADE_TYPE_QQ_WAP = "QQ_WAP";								// QQ H5支付

		public final static String TRADE_TYPE_UnionPayQR= "UnionPayQR";								// 银联扫码
		public final static String TRADE_TYPE_UnionPay_WAP = "UnionPay_WAP";						// 银联H5
		public final static String TRADE_TYPE_Express = "express";								// 快捷支付

	}
}
