package com.scudata.server.odbc;

import com.scudata.common.Logger;
import com.scudata.server.ConnectionProxyManager;
import com.scudata.server.IProxy;

/**
 * ���Ӵ�����
 * 
 * @author Joancy
 *
 */
public class ConnectionProxy extends IProxy
{
	String userName;
	long loginTime = 0;
	boolean closed = false;

	/**
	 * ����һ�����Ӵ������
	 * @param cpm ���Ӵ��������
	 * @param id ����Ψһ��
	 * @param user ��¼�û���
	 */
	public ConnectionProxy(ConnectionProxyManager cpm, int id, String user){
		super(cpm, id);
		this.userName = user;
		loginTime = System.currentTimeMillis();
		access();
		Logger.debug(this+" connected.");
	}
	
	/**
	 * ��ȡStatement����
	 * @param id ������
	 * @return Statement����
	 * @throws Exception
	 */
	public StatementProxy getStatementProxy(int id) throws Exception{
		StatementProxy sp = (StatementProxy)getProxy(id); 
		if(sp==null){
			throw new Exception("Statement "+id+" is not exist or out of time!");
		}
		return sp; 
	}
	
	/**
	 * ��ȡ�û���
	 * @return �û���
	 */
	public String getUserName(){
		return userName;
	}
	
	/**
	 * ��ȡ��¼ʱ��
	 * @return ������ʾ����ʱ��
	 */
	public long getLoginTime(){
		return loginTime;
	}
	
	/**
	 * �Ƿ��ѹر�
	 * @return �ѹرշ���true�����򷵻�false
	 */
	public boolean isClosed(){
		return closed;
	}
	
	/**
	 * �رյ�ǰ�������
	 */
	public void close() {
		closed = true;
		Logger.debug(this+" closed.");
	}

	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString() {
		return "Connection "+getId();
	}
	
}