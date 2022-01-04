package com.scudata.parallel;

import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.dm.cursor.ICursor;
import com.scudata.server.unit.UnitServer;

/**
 * Զ���α����
 * ��ֹʹ�û���һ������ȡ���ݽṹ�����α��¼Ϊ��ʱ������row�������һ�к󣬻��Զ�close�����α�
 * Ȼ����ɺ����α���������α걻���ˣ���û�ˣ�
 * @author Joancy
 *
 */
public class RemoteCursorProxy extends ICursor {
	RemoteCursorProxyManager rcpm;
	ICursor cs;
	int proxyId = -1;
	
	private long lastAccessTime = -1;
	
	/**
	 * ����Զ���α����
	 * @param cs �α�
	 */
	public RemoteCursorProxy(ICursor cs) {
		this(null,cs,-1);
	}
	
	/**
	 * ����Զ���α����
	 * @param rcpm Զ���α���������
	 * @param cs �α����
	 * @param id ������
	 */
	public RemoteCursorProxy(RemoteCursorProxyManager rcpm, ICursor cs, int id) {
		if(rcpm==null){
			this.rcpm = RemoteCursorProxyManager.getInstance();
			this.proxyId = UnitServer.nextId();
			this.rcpm.addProxy(this);
		}else{
			this.rcpm = rcpm;
			this.proxyId = id;
		}
		this.cs = cs;
		access();
	}

	ICursor getCursor() {
		return cs;
	} 

	int getProxyID() {
		return proxyId;
	}

	protected long skipOver(long n) {
		return cs.skip(n);
	}

	/**
	 * ���ٵ�ǰ����
	 */
	public void destroy() {
		cs.close();
	}

	/**
	 * �ر�ʱ����ǰ����������еĴ����б���ɾ��
	 */
	public synchronized void close() {
		destroy();
		rcpm.delProxy(proxyId);
	}

	protected Sequence get(int n) {
		Sequence tmp = cs.fetch(n);
		access();
		return tmp;
	}

	/**
	 * ʵ��toString�ı�����
	 */
	public String toString() {
		return "RemoteCursorProxy :" + proxyId;
	}
	
	/**
	 * ȡ���ݽṹ
	 */
	public DataStruct getDataStruct() {
		if(dataStruct!=null) return dataStruct;
		dataStruct = cs.getDataStruct();
			
		return dataStruct;
	}
	
	void access() {
		lastAccessTime = System.currentTimeMillis();
	}

	/**
	 * ��ʱ���
	 * @param timeOut ��ʱ��ʱ��
	 * @return �����ʱ�����ٶ��󣬷���true�����򷵻�false
	 */
	public boolean checkTimeOut(int timeOut) {
		// ������룬timeOut��λΪ��
		if ((System.currentTimeMillis() - lastAccessTime) / 1000 > timeOut) {
			Logger.info(this + " is timeout.");
			destroy();
			return true;
		}
		return false;
	}
}
