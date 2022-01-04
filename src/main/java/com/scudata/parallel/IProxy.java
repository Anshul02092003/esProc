package com.scudata.parallel;

import com.scudata.dm.IResource;
import com.scudata.server.unit.UnitServer;

/**
 * ����ӿ�
 * @author Joancy
 *
 */
public abstract class IProxy implements IResource {
	private int proxyId = UnitServer.nextId();
	
	/**
	 * ȡ������
	 * @return ����Ψһ��
	 */
	public int getProxyId() {
		return proxyId;
	}
	
	/**
	 * ���ô�����
	 * @param proxyId
	 */
	public void setProxyId(int proxyId) {
		this.proxyId = proxyId;
	}
}