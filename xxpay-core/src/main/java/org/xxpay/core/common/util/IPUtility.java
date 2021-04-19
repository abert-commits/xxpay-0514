package org.xxpay.core.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @Description: IP地址工具类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public class IPUtility {

	/**
	 * getLocalhostIp(获取本机ip地址)
	 * @throws UnknownHostException 
	 * @Exception 异常对象 
	 * @since  CodingExample　Ver(编码范例查看) 1.1
	 */
	public static String getLocalhostIp() {
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return null;
		}
		return ip;
	}
	
	public static List<String> getIpAddrs() throws Exception {
		List<String> IPs = new ArrayList<String>();
		Enumeration<NetworkInterface> allNetInterfaces = null;
		allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			Enumeration<?> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address && ip.getHostAddress().indexOf(".") != -1) {
					IPs.add(ip.getHostAddress());
				}
			}
		}
		return IPs;
	}    		

	/**
	 * 兼容Linux系统
	 * @return
	 */
	public static String getLocalIP() {
		String ip = "";
		try {
			Enumeration<?> e1 = (Enumeration<?>) NetworkInterface
					.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) e1.nextElement();
				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = (InetAddress) e2.nextElement();
					if (ia instanceof Inet6Address)
						continue;
					if (!ia.isLoopbackAddress()) {
						ip = ia.getHostAddress();
						break;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			return "";
		}
		return ip;
	}

	public static final String LOCAL_IP = "127.0.0.1";//本地ip地址
	public static final String DEFAULT_IP = "0:0:0:0:0:0:0:1";//默认ip地址
	public static final int DEFAULT_IP_LENGTH = 15;//默认ip地址长度

	/**
	 * 获取合法ip地址
	 * @param request
	 * @return
	 */
	public static String getRealIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");//squid 服务代理
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");//apache服务代理
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");//weblogic 代理
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");//有些代理
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP"); //nginx代理
		}

		/*
		 * 如果此时还是获取不到ip地址，那么最后就使用request.getRemoteAddr()来获取
		 * */
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			if(StringUtils.equals(ip,LOCAL_IP) || StringUtils.equals(ip,DEFAULT_IP)){
				//根据网卡取本机配置的IP
				InetAddress iNet = null;
				try {
					iNet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {

				}
				ip= iNet.getHostAddress();
			}
		}

		//对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		//"***.***.***.***".length() = 15
		if(!StringUtils.isEmpty(ip) && ip.length()> DEFAULT_IP_LENGTH){
			if(ip.indexOf(",") > 0){
				ip = ip.substring(0,ip.indexOf(","));
			}
		}
		return ip;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(IPUtility.getLocalIP());
	}

}
