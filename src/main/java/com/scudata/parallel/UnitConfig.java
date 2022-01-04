package com.scudata.parallel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.scudata.app.config.ConfigConsts;
import com.scudata.app.config.ConfigWriter;
import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.StringUtils;
import com.scudata.parallel.XmlUtil;
import com.scudata.resources.ParallelMessage;

/**
 * �ֻ�������
 * @author Joancy
 *
 */
public class UnitConfig extends ConfigWriter {
	// version 3
	private int tempTimeOut = 12; // ��ʱ�ļ����ʱ�䣬��Ϊ��λ��0Ϊ��������λ����Сʱ��
	private int proxyTimeOut = 12; // �ļ��Լ��α����Ĺ���ʱ�䣬��Ϊ��λ��0Ϊ��������λ����Сʱ��
	private int interval = 30 * 60; // �����������ʱ�ļ����ڵ�ʱ������0Ϊ�������ڡ���λ��
	boolean autoStart=false;
	private List<Host> hosts = null;
	
//	�ͻ��˰�����
	private boolean checkClient = false;
	private List<String> enabledClientsStart = null;
	private List<String> enabledClientsEnd = null;
	
	MessageManager mm = ParallelMessage.get();
	
	/**
	 * �������ļ�������������Ϣ
	 * @param is �����ļ���
	 * @throws Exception ���س���ʱ�׳��쳣
	 */
	public void load(InputStream is) throws Exception {
		load(is, true);
	}
	
	/**
	 * �������ļ����ֽ����ݼ���������Ϣ
	 * @param buf �����ļ��ֽ�����
	 * @throws Exception ���س����׳��쳣
	 */
	public void load(byte[] buf) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		load(bais, true);
		bais.close();
	}
	
	/**
	 * ����ip��port����Host���󣬲���Host����ά����hosts����
	 * @param hosts hosts����
	 * @param ip IP��ַ
	 * @param port �˿ں�
	 * @return ��ip��port����Host����
	 */
	public static Host getHost(List<Host> hosts, String ip, int port) {
		Host h = null;
		for (int i = 0, size = hosts.size(); i < size; i++) {
			h = hosts.get(i);
			if (h.getIp().equals(ip) && h.getPort()==port) {
				return h;
			}
		}
		h = new Host(ip,port);
		hosts.add(h);
		return h;
	}

	/**
	 * ���������ļ�������
	 * @param is �����ļ���
	 * @param showDebug �Ƿ����������õĵ�����Ϣ
	 * @throws Exception �ļ���ʽ�������׳��쳣
	 */
	public void load(InputStream is, boolean showDebug) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document xmlDocument = docBuilder.parse(is);
		NodeList nl = xmlDocument.getChildNodes();
		Node root = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equalsIgnoreCase("Server")) {
				root = n;
			}
		}
		if (root == null) {
			throw new Exception(mm.getMessage("UnitConfig.errorxml"));
		}
		String ver = XmlUtil.getAttribute(root, "Version");
		if (ver==null || Integer.parseInt( ver )<3) {
			throw new RuntimeException(mm.getMessage("UnitConfig.updateversion",UnitContext.UNIT_XML));
		}

		// version 3
		// Server ����
		Node subNode = XmlUtil.findSonNode(root, "tempTimeout");
		String buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			tempTimeOut = Integer.parseInt(buf);
			if (tempTimeOut > 0) {
				if (showDebug)
					Logger.debug("Using TempTimeOut=" + tempTimeOut
							+ " hour(s).");
			}
		}

		subNode = XmlUtil.findSonNode(root, "interval");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			int t = Integer.parseInt(buf);
			if (t > 0)
				interval = t;// ���ò���ȷʱ��ʹ��ȱʡ�����
		}

		subNode = XmlUtil.findSonNode(root, "autostart");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			autoStart = new Boolean(buf);
		}

		subNode = XmlUtil.findSonNode(root, "proxyTimeout");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			proxyTimeOut = Integer.parseInt(buf);
			if (proxyTimeOut > 0) {
				if (showDebug)
					Logger.debug("Using ProxyTimeOut=" + proxyTimeOut
							+ " hour(s).");
			}
		}

		Node nodeHosts = XmlUtil.findSonNode(root, "Hosts");
		NodeList hostsList = nodeHosts.getChildNodes();
		hosts = new ArrayList<Host>();

		for (int i = 0; i < hostsList.getLength(); i++) {
			Node xmlNode = hostsList.item(i);
			if (!xmlNode.getNodeName().equalsIgnoreCase("Host")) {
				continue;
			}
			buf = XmlUtil.getAttribute(xmlNode, "ip");
			String sPort = XmlUtil.getAttribute(xmlNode, "port");
			Host host = new Host(buf,Integer.parseInt(sPort));
			
			buf = XmlUtil.getAttribute(xmlNode, "maxTaskNum");
			if(StringUtils.isValidString(buf)){
				host.setMaxTaskNum(Integer.parseInt(buf));
			}
			
			buf = XmlUtil.getAttribute(xmlNode, "preferredTaskNum");
			if(StringUtils.isValidString(buf)){
				host.setPreferredTaskNum(Integer.parseInt(buf));
			}
			
			hosts.add(host);
		}// hosts
		
		Node nodeECs = XmlUtil.findSonNode(root, "EnabledClients");
		buf = XmlUtil.getAttribute(nodeECs, "check");
		if (StringUtils.isValidString(buf)){
			checkClient = new Boolean(buf);
		}

		NodeList ecList = nodeECs.getChildNodes();
		enabledClientsStart = new ArrayList<String>();
		enabledClientsEnd = new ArrayList<String>();

		for (int i = 0; i < ecList.getLength(); i++) {
			Node xmlNode = ecList.item(i);
			if (!xmlNode.getNodeName().equalsIgnoreCase("Host"))
				continue;
			buf = XmlUtil.getAttribute(xmlNode, "start");
			if (!StringUtils.isValidString(buf))
				continue;
			enabledClientsStart.add(buf);
			buf = XmlUtil.getAttribute(xmlNode, "end");
			enabledClientsEnd.add(buf);
		}
	}

	/**
	 * �������ļ�ת��Ϊ�ֽ�����
	 * @return �ֽ�����
	 * @throws Exception ת������ʱ�׳��쳣
	 */
	public byte[] toFileBytes() throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		save(baos);
		return baos.toByteArray();
	}
	
	/**
	 * �������ļ���Ϣ���浽�����out
	 * @param out �����
	 * @throws SAXException д����ʱ�׳��쳣
	 */
	public void save(OutputStream out) throws SAXException {
		Result resultxml = new StreamResult(out);
		handler.setResult(resultxml);
		level = 0;
		handler.startDocument();
		// ���ø��ڵ�Ͱ汾
		handler.startElement("", "", "SERVER", getAttributesImpl(new String[] {
				ConfigConsts.VERSION, "3" }));
		level = 1;
		writeAttribute("TempTimeOut", tempTimeOut + "");
		writeAttribute("Interval", interval + "");
		writeAttribute("AutoStart", autoStart + "");
		writeAttribute("ProxyTimeOut", proxyTimeOut + "");

		startElement("Hosts", null);
		if (hosts != null) {
			for (int i = 0, size = hosts.size(); i < size; i++) {
				level = 2;
				Host h = hosts.get(i);
				startElement("Host", getAttributesImpl(new String[] { "ip",
						h.ip,"port",h.port+"",
						"maxTaskNum",h.maxTaskNum+"","preferredTaskNum",h.preferredTaskNum+""}));
				endElement("Host");
			}
			level = 1;
			endElement("Hosts");
		} else {
			endEmptyElement("Hosts");
		}
		
		level = 1;
		startElement("EnabledClients", getAttributesImpl(new String[] { "check",
				checkClient+"" }));
		if (enabledClientsStart != null) {
			level = 2;
			for (int i = 0, size = enabledClientsStart.size(); i < size; i++) {
				String start = enabledClientsStart.get(i);
				String end = enabledClientsEnd.get(i);
				startElement("Host", getAttributesImpl(new String[] {
						"start",start,"end",end }));
				endElement("Host");
			}
			level = 1;
			endElement("EnabledClients");
		} else {
			endEmptyElement("EnabledClients");
		}
		
		handler.endElement("", "", "SERVER");
		// �ĵ�����,ͬ��������
		handler.endDocument();
	}

	/**
	 * ��ȡ��ʱ�ļ���ʱʱ�䣬��λͳһΪСʱ
	 * ����ͬgetTempTimeOutHour���������ڼ���
	 * @return ��ʱʱ��
	 */
	public int getTempTimeOut() {
		return tempTimeOut;
	}

	/**
	 * ��ȡ��ʱ�ļ���ʱʱ�䣬��λСʱ
	 * @return ��ʱʱ��
	 */
	public int getTempTimeOutHour() {
		return tempTimeOut;
	}

	/**
	 * ������ʱ�ļ���ʱʱ��
	 * @param tempTimeOut ʱ��
	 */
	public void setTempTimeOut(int tempTimeOut) {
		this.tempTimeOut = tempTimeOut;
	}

	/**
	 * ��Сʱ���ó�ʱʱ�䣬����ͬsetTempTimeOut
	 * �������ڴ������
	 * @param tempTimeOutHour ʱ��
	 */
	public void setTempTimeOutHour(int tempTimeOutHour) {
		this.tempTimeOut = tempTimeOutHour;
	}

	public int getProxyTimeOut() {
		return proxyTimeOut;
	}

	public boolean isAutoStart(){
		return autoStart;
	}
	public void setAutoStart(boolean as){
		autoStart = as;
	}
	/**
	 * ȡ�����������ʱ�䣨��λΪСʱ��
	 * @return ����ʱʱ��
	 */
	public int getProxyTimeOutHour() {
		return proxyTimeOut;
	}

	/**
	 * ���ô���ʱʱ��
	 * @param proxyTimeOut ��ʱʱ��
	 */
	public void setProxyTimeOut(int proxyTimeOut) {
		this.proxyTimeOut = proxyTimeOut;
	}

	/**
 * ����ͬsetProxyTimeOut
 * @param proxyTimeOutHour
 */
	public void setProxyTimeOutHour(int proxyTimeOutHour) {
		this.proxyTimeOut = proxyTimeOutHour;// * 3600;
	}

	/**
	 * ����Ƿ�ʱ��ʱ����(��λΪ��)
	 * @return ʱ����
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * ���ü�鳬ʱ��ʱ����
	 * @param interval ʱ����
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * �г���ǰ�ֻ��µ����н��̵�ַ
	 * @return ���������б�
	 */
	public List<Host> getHosts() {
		return hosts;
	}

	/**
	 * ���ý��������б�
	 * @param hosts ���������б�
	 */
	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

	/**
	 * �Ƿ�У��ͻ���
	 * @return
	 */
	public boolean isCheckClients() {
		return checkClient;
	}

	public void setCheckClients(boolean b) {
		this.checkClient = b;
	}

	public List<String> getEnabledClientsStart() {
		return enabledClientsStart;
	}

	public void setEnabledClientsStart(List<String> enHosts) {
		this.enabledClientsStart = enHosts;
	}
	
	public List<String> getEnabledClientsEnd() {
		return enabledClientsEnd;
	}

	public void setEnabledClientsEnd(List<String> enHosts) {
		this.enabledClientsEnd = enHosts;
	}


	public static class Host {
//		�ʺ���ҵ��ȱʡΪCPU����,���ܱ�����
		int preferredTaskNum = Runtime.getRuntime().availableProcessors();
		
		int maxTaskNum = preferredTaskNum*2;
		String ip;
		int port;
		
		public Host(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}
		
		public int getPort(){
			return port;
		}
		
		public int getMaxTaskNum(){
			return maxTaskNum;
		}
		public void setMaxTaskNum(int max){
			maxTaskNum = max;
		}
		
		public int getPreferredTaskNum(){
			return preferredTaskNum;
		}
		public void setPreferredTaskNum(int num){
			preferredTaskNum = num;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(ip);
			sb.append(":[");
			sb.append(port);
			sb.append("]");
			return sb.toString();
		}

	}

}
