package com.scudata.parallel;

import java.io.Serializable;

class RemoteMemoryTable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String host;
	private int port;
	private int proxyId; // �����
	private int recordCount; // ��¼��
	
	// ������Ƿֲ�����Ҫ����������
	private Object startKeyValue; // ������¼������ֵ
	private int keyCount;
	
	private String distribute; // �ֲ����ʽ
	private int part; // ����
	
	public RemoteMemoryTable() {
	}
	
	/**
	 * @param host �ڵ��ip
	 * @param port �ڵ���˿�
	 * @param proxyId Զ�̴����ʶ
	 * @param recordCount ��¼��
	 */
	public RemoteMemoryTable(String host, int port, int proxyId, int recordCount) {
		this.host = host;
		this.port = port;
		this.proxyId = proxyId;
		this.recordCount = recordCount;
	}
	
	/**
	 * @param startKeyValue ������¼����ֵ�����ֶ�����ʱΪ����ֵ����
	 * @param keyCount ��������
	 */
	public void setStartKeyValue(Object startKeyValue, int keyCount) {
		this.startKeyValue = startKeyValue;
		this.keyCount = keyCount;
	}

	public void setDistribute(String distribute, int part) {
		this.distribute = distribute;
		this.part = part;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getProxyId() {
		return proxyId;
	}

	public void setProxyId(int proxyId) {
		this.proxyId = proxyId;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public Object getStartKeyValue() {
		return startKeyValue;
	}

	public void setStartKeyValue(Object startKeyValue) {
		this.startKeyValue = startKeyValue;
	}

	public int getKeyCount() {
		return keyCount;
	}

	public void setKeyCount(int keyCount) {
		this.keyCount = keyCount;
	}
	
	public String getDistribute() {
		return distribute;
	}
	
	public int getPart() {
		return part;
	}
}