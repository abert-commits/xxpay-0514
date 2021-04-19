package org.xxpay.task.reconciliation.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账差错类型枚举
 */
public enum MistakeTypeEnum {

	BANK_MISS((byte) 10, "银行漏单"), // 银行不存在该订单
	PLATFORM_MISS((byte) 12, "平台漏单"), // 平台不存在该订单
	PLATFORM_SHORT_STATUS_MISMATCH((byte) 13, "平台短款，状态不符"), // 银行支付成功，平台支付不成功（比较常见）
	PLATFORM_SHORT_CASH_MISMATCH((byte) 14, "平台短款，金额不符"), // 平台需支付金额比银行实际支付金额少（基本不会出现）
	PLATFORM_OVER_CASH_MISMATCH((byte) 15, "平台长款,金额不符"), // 银行实际支付金额比平台需支付金额少
	PLATFORM_OVER_STATUS_MISMATCH((byte) 16, "平台长款,状态不符"), // 平台支付成功，银行支付不成功（基本不会出现）
	FEE_MISMATCH((byte) 17, "手续费不匹配");

	/** 描述 */
	private Byte code;
	private String desc;

	private MistakeTypeEnum(Byte code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Byte getCode() {
		return code;
	}

	public void setCode(Byte code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public static Map<String, Map<String, Object>> toMap() {
		MistakeTypeEnum[] ary = MistakeTypeEnum.values();
		Map<String, Map<String, Object>> enumMap = new HashMap<String, Map<String, Object>>();
		for (int num = 0; num < ary.length; num++) {
			Map<String, Object> map = new HashMap<String, Object>();
			String key = ary[num].name();
			map.put("desc", ary[num].getDesc());
			map.put("name", ary[num].name());
			enumMap.put(key, map);
		}
		return enumMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List toList() {
		MistakeTypeEnum[] ary = MistakeTypeEnum.values();
		List list = new ArrayList();
		for (int i = 0; i < ary.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("desc", ary[i].getDesc());
			map.put("name", ary[i].name());
			list.add(map);
		}
		return list;
	}

	public static MistakeTypeEnum getEnum(String name) {
		MistakeTypeEnum[] arry = MistakeTypeEnum.values();
		for (int i = 0; i < arry.length; i++) {
			if (arry[i].name().equalsIgnoreCase(name)) {
				return arry[i];
			}
		}
		return null;
	}

	/**
	 * 取枚举的json字符串
	 *
	 * @return
	 */
	public static String getJsonStr() {
		MistakeTypeEnum[] enums = MistakeTypeEnum.values();
		StringBuffer jsonStr = new StringBuffer("[");
		for (MistakeTypeEnum senum : enums) {
			if (!"[".equals(jsonStr.toString())) {
				jsonStr.append(",");
			}
			jsonStr.append("{id:'").append(senum).append("',desc:'").append(senum.getDesc()).append("'}");
		}
		jsonStr.append("]");
		return jsonStr.toString();
	}
}
