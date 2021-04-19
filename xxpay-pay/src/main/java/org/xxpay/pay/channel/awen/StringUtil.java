package org.xxpay.pay.channel.awen;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类, 继承org.apache.commons.lang3.StringUtils类
 */
public class StringUtil extends org.apache.commons.lang.StringUtils {

	private static final String CHARSET_NAME = "UTF-8";

	public static final String SPACE = " ";
	public static final String DOT = ".";
	public static final String SLASH = "/";
	public static final String BACKSLASH = "\\";
	public static final String EMPTY = "";
	public static final String CRLF = "\r\n";
	public static final String NEWLINE = "\n";
	public static final String UNDERLINE = "_";
	public static final String COMMA = ",";

	public static final String HTML_NBSP = "&nbsp;";
	public static final String HTML_AMP = "&amp";
	public static final String HTML_QUOTE = "&quot;";
	public static final String HTML_LT = "&lt;";
	public static final String HTML_GT = "&gt;";

	public static final String EMPTY_JSON = "{}";

	/**
	 * 转换为字节数组
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] getBytes(String str) {
		if (str != null) {
			try {
				return str.getBytes(CHARSET_NAME);
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		} else {
			return null;
		}
	}


	/**
	 * 是否包含字符串
	 * 
	 * @param str
	 *            验证字符串
	 * @param strs
	 *            字符串组
	 * @return 包含返回true
	 */
	public static boolean inString(String str, String... strs) {
		if (str != null) {
			for (String s : strs) {
				if (str.equals(trim(s))) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getRandomStr(int len, String romdomStr) {
		String prefix = "";
		return getRandomStr(prefix, len, romdomStr);
	}

	/**
	 * 根据前缀获取一段随机的字符串。如果前缀长度大于或len，那么直接返回前缀
	 * 
	 * @param prefix
	 * @param len
	 * @return
	 */
	public static String getRandomStr(String prefix, int len, String romdomStr) {
		int prefixLen = prefix.length();
		if (prefixLen >= len)
			return prefix;
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		sb.append(prefix);
		int length = len - prefixLen;
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(romdomStr.length());
			sb.append(romdomStr.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 根据前缀获取一段随机的字符串。如果前缀长度大于或len，那么直接返回前缀
	 * 
	 * @param prefix
	 * @param len
	 * @return
	 */
	public static Set<String> getRandomStr(String prefix, int len, int count, String romdomStr) {
		Set<String> randomStrSet = new HashSet<String>();// 用来防止生成的抵用券码重复问题
		while (randomStrSet.size() < count) {
			randomStrSet.add(getRandomStr(prefix, len, romdomStr));
		}
		return randomStrSet;
	}

	/**
	 * 如果不为空，则设置值
	 * 
	 * @param target
	 * @param source
	 */
	public static void setValueIfNotBlank(String target, String source) {
		if (isNotBlank(source)) {
			target = source;
		}
	}

	/**
	 * 替换掉HTML标签方法
	 */
	public static String replaceHtml(String html) {
		if (isBlank(html)) {
			return "";
		}
		String regEx = "<.+?>";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(html);
		String s = m.replaceAll("");
		return s;
	}

	/**
	 * 转换为JS获取对象值，生成三目运算返回结果
	 * 
	 * @param objectString
	 *            对象串 例如：row.user.id
	 *            返回：!row?'':!row.user?'':!row.user.id?'':row.user.id
	 */
	public static String jsGetVal(String objectString) {
		StringBuilder result = new StringBuilder();
		StringBuilder val = new StringBuilder();
		String[] vals = split(objectString, ".");
		for (int i = 0; i < vals.length; i++) {
			val.append("." + vals[i]);
			result.append("!" + (val.substring(1)) + "?'':");
		}
		result.append(val.substring(1));
		return result.toString();
	}

	public static boolean isNullOrEmpty(Object obj) {
		if (obj instanceof String) {
			return obj == null || "".equals(obj);
		}
		return obj == null;
	}

	/**
	 * 去掉指定后缀
	 * 
	 * @param str
	 *            字符串
	 * @param suffix
	 *            后缀
	 * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
	 */
	public static String removeSuffix(String str, String suffix) {
		if (isEmpty(str) || isEmpty(suffix)) {
			return str;
		}

		if (str.endsWith(suffix)) {
			return str.substring(0, str.length() - suffix.length());
		}
		return str;
	}

	/**
	 * 格式化文本, {} 表示占位符<br>
	 * 例如：format("aaa {} ccc", "bbb") ----> aaa bbb ccc
	 * 
	 * @param template
	 *            文本模板，被替换的部分用 {} 表示
	 * @param values
	 *            参数值
	 * @return 格式化后的文本
	 */
	public static String format(String template, Object... values) {
		if (values == null || values.length == 0) {
			return template;
		}

		final StringBuilder sb = new StringBuilder();
		final int length = template.length();

		int valueIndex = 0;
		char currentChar;
		for (int i = 0; i < length; i++) {
			if (valueIndex >= values.length) {
				sb.append(sub(template, i, length));
				break;
			}

			currentChar = template.charAt(i);
			if (currentChar == '{') {
				final char nextChar = template.charAt(++i);
				if (nextChar == '}') {
					sb.append(values[valueIndex++]);
				} else {
					sb.append('{').append(nextChar);
				}
			} else {
				sb.append(currentChar);
			}
		}
		return sb.toString();
	}
	
	/*
	 * 格式化文本，例如：format("aaa {0} ccc", "bbb") ----> aaa bbb ccc
	 */
	public static String format2(String template, Object... values) {
		return MessageFormat.format(template, values);
	}

	/**
	 * 改进JDK subString<br>
	 * index从0开始计算，最后一个字符为-1<br>
	 * 如果from和to位置一样，返回 "" <br>
	 * 如果from或to为负数，则按照length从后向前数位置，如果绝对值大于字符串长度，则from归到0，to归到length<br>
	 * 如果经过修正的index中from大于to，则互换from和to example: <br>
	 * abcdefgh 2 3 -> c <br>
	 * abcdefgh 2 -3 -> cde <br>
	 * 
	 * @param string
	 *            String
	 * @param fromIndex
	 *            开始的index（包括）
	 * @param toIndex
	 *            结束的index（不包括）
	 * @return 字串
	 */
	public static String sub(String string, int fromIndex, int toIndex) {
		int len = string.length();
		if (fromIndex < 0) {
			fromIndex = len + fromIndex;
			if (fromIndex < 0) {
				fromIndex = 0;
			}
		} else if (fromIndex >= len) {
			fromIndex = len - 1;
		}
		if (toIndex < 0) {
			toIndex = len + toIndex;
			if (toIndex < 0) {
				toIndex = len;
			}
		} else if (toIndex > len) {
			toIndex = len;
		}
		if (toIndex < fromIndex) {
			int tmp = fromIndex;
			fromIndex = toIndex;
			toIndex = tmp;
		}
		if (fromIndex == toIndex) {
			return EMPTY;
		}
		char[] strArray = string.toCharArray();
		char[] newStrArray = Arrays.copyOfRange(strArray, fromIndex, toIndex);
		return new String(newStrArray);
	}

	/**
	 * 是否包含空字符串
	 * 
	 * @param strs
	 *            字符串列表
	 * @return 是否包含空字符串
	 */
	public static boolean hasBlank(String... strs) {
		if (strs == null || strs.length == 0) {
			return true;
		}
		for (String str : strs) {
			if (isBlank(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将下划线方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
	 * 例如：hello_world->HelloWorld
	 *
	 * @param name
	 *            转换前的下划线大写方式命名的字符串
	 * @return 转换后的驼峰式命名的字符串
	 */
	public static String toCamelCase(String name) {
		if (name == null) {
			return null;
		}
		if (name.contains(UNDERLINE)) {
			name = name.toLowerCase();

			StringBuilder sb = new StringBuilder(name.length());
			boolean upperCase = false;
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);

				if (c == '_') {
					upperCase = true;
				} else if (upperCase) {
					sb.append(Character.toUpperCase(c));
					upperCase = false;
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		} else
			return name;
	}

	/**
	 * 小写首字母<br>
	 * 例如：str = Name, return name
	 * 
	 * @param str
	 *            字符串
	 * @return 字符串
	 */
	public static String lowerFirst(String str) {
		if (isBlank(str)) {
			return str;
		}
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * 将驼峰式命名的字符串转换为下划线方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。</br>
	 * 例如：HelloWorld->hello_world
	 *
	 * @param camelCaseStr
	 *            转换前的驼峰式命名的字符串
	 * @return 转换后下划线大写方式命名的字符串
	 */
	public static String toUnderlineCase(String camelCaseStr) {
		if (camelCaseStr == null) {
			return null;
		}

		final int length = camelCaseStr.length();
		StringBuilder sb = new StringBuilder();
		char c;
		boolean isPreUpperCase = false;
		for (int i = 0; i < length; i++) {
			c = camelCaseStr.charAt(i);
			boolean isNextUpperCase = true;
			if (i < (length - 1)) {
				isNextUpperCase = Character.isUpperCase(camelCaseStr.charAt(i + 1));
			}
			if (Character.isUpperCase(c)) {
				if (!isPreUpperCase || !isNextUpperCase) {
					if (i > 0)
						sb.append(UNDERLINE);
				}
				isPreUpperCase = true;
			} else {
				isPreUpperCase = false;
			}
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}

	/**
	 * 首字母变小写
	 */
	public static String firstCharToLowerCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'A' && firstChar <= 'Z') {
			char[] arr = str.toCharArray();
			arr[0] += ('a' - 'A');
			return new String(arr);
		}
		return str;
	}

	/**
	 * 首字母变大写
	 */
	public static String firstCharToUpperCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'a' && firstChar <= 'z') {
			char[] arr = str.toCharArray();
			arr[0] -= ('a' - 'A');
			return new String(arr);
		}
		return str;
	}	

	public static String toSortPairString(Map<String, String> map){
		if(map == null) {
			return ""; 
		}
        //使用TreeMap对key进行排序
        Map<String, String> treeMap = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry: map.entrySet()){
        	if(entry.getValue() == null || entry.getValue().equals("")) {
        		continue;
        	}
        	treeMap.put(entry.getKey(), entry.getValue());
        }
		return toPairString(treeMap, false);
	}
	
	public static String toPairString(Map<String, String> map){
		return toPairString(map, false);
	}
	
    /*
     * 把map转换成key=value&key2=value2的格式字符串
     */
    public static String toPairString(Map<String, String> map, boolean ignoreEmptyValue){
        if(map == null) return "";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String,String> entry: map.entrySet()){
        	if(!ignoreEmptyValue || !isNullOrEmpty(entry.getValue())){
        		if(builder.length()>0){
                    builder.append("&");
                }
                builder.append(entry.getKey() + "=" + entry.getValue());
        	}
        }
        return builder.toString();
    }
    
    /*
     * 返回一个新的map，其中value为原来string[]第一个索引值
     */
    public static Map<String, String> toSignleValueMap(Map<String, String[]> map) {
		if(map == null) return null;
		Map<String, String> returnMap = new LinkedHashMap<String, String>();
		for(Map.Entry<String, String[]> entry: map.entrySet()){
			String value = entry.getValue() == null ? null : entry.getValue()[0];
			returnMap.put(entry.getKey(), value);
		}
		return returnMap;
	}
    
    public static Map<String,String> strToMap(String mapStr){
    	//将map.toString后的串反转成map  
    	String str1 = mapStr.replaceAll("\\{|\\}", "");//mapStr是一个map  toString后的字符串。  
        String str2 = str1.replaceAll(" ", "");  
        String str3 = str2.replaceAll(",", "&");  
    	Map<String, String> map = null;  
    	if((null != str3) && (!"".equals(str3.trim()))) {  
    		String[] resArray = str3.split("&");  
    		if(0 != resArray.length){
    			map = new HashMap<String, String>(resArray.length);  
    			for (String arrayStr : resArray) {  
    				if ((null != arrayStr) && (!"".equals(arrayStr.trim()))){  
    					int index = arrayStr.indexOf("=");  
    					if (-1 != index) {  
							map.put(arrayStr.substring(0, index), arrayStr.substring(index + 1));  
    					}  
    				}  
    			}  
    		}  
    	}  
    	return map; 
    }
    
    public static String toCamelCaseByMapStr(String mapStr){
		if (mapStr == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String[] keyValueArray = mapStr.split("&");
		int start = 0;
		for(String keyValue : keyValueArray) {
			String key = keyValue.substring(0, keyValue.indexOf("="));
			String value =  keyValue.substring(keyValue.indexOf("="));
			if(start != 0) {
				sb.append("&");
			}
			start = start +1;
			if (key.contains(UNDERLINE)) {
				boolean upperCase = false;
				for (int i = 0; i < key.length(); i++) {
					char c = key.charAt(i);
					if (c == '_') {
						upperCase = true;
					} else if (upperCase) {
						sb.append(Character.toUpperCase(c));
						upperCase = false;
					} else {
						sb.append(c);
					}
				}
			}else{
				sb.append(key);
			}
			sb.append(value);
		}
		return sb.toString();
	}
    
    public static String toUnderlineCaseByMapStr(String mapStr) {
		if (mapStr == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String[] keyValueArray = mapStr.split("&");
		int start = 0;
		for(String keyValue : keyValueArray) {
			String key = keyValue.substring(0, keyValue.indexOf("="));
			String value =  keyValue.substring(keyValue.indexOf("="));
			if(start != 0) {
				sb.append("&");
			}
			start = start +1;
			final int length = key.length();
			char c;
			boolean isPreUpperCase = false;
			for (int i = 0; i < length; i++) {
				c = key.charAt(i);
				boolean isNextUpperCase = true;
				if (i < (length - 1)) {
					isNextUpperCase = Character.isUpperCase(key.charAt(i + 1));
				}
				if (Character.isUpperCase(c)) {
					if (!isPreUpperCase || !isNextUpperCase) {
						if (i > 0)
							sb.append(UNDERLINE);
					}
					isPreUpperCase = true;
				} else {
					isPreUpperCase = false;
				}
				sb.append(Character.toLowerCase(c));
			}
			sb.append(value);
		}
		return sb.toString();
	}
    
    public static void main(String[] args) {
    	String mapStr = "merNo=0000010001&message=success&money=50000.00&notifyUrl=http://127.0.0.1:8010/hrpay-mer-demo/transNotify&orderId=TO20170930000006&respCode=1&serialVersionUID=-294282676734780049&signType=MD5&status=1&transId=T20170930113109&sign=A7521292342E5F6F35A0EF93669BCF17";
    	String mapStrNew = StringUtil.toUnderlineCaseByMapStr(mapStr);
    	String mapStrNew1 = StringUtil.toCamelCaseByMapStr(mapStrNew);
    	System.out.println(mapStr);
    	System.out.println(mapStrNew);
    	System.out.println(mapStrNew1);
    	
	}

}
