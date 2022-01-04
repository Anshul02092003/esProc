package com.scudata.server;

import com.scudata.server.IProxy;

/**
 * ���Ӵ��������
 * 
 * @author Joancy
 *
 */
public class ConnectionProxyManager extends IProxy{
	static ConnectionProxyManager instance = new ConnectionProxyManager();
	
	private ConnectionProxyManager(){
		super(null, 0);
	}
	
	/**
	 * 	���Ǹ���÷�����������������Ҫ���ڣ�ά�ֳ�ʼ̬-1����
	 */
	public void access() {
	}

	/**
	 * ��ȡ��������Ψһʵ��
	 * @return ������ʵ��
	 */
	public static ConnectionProxyManager getInstance(){
		return instance;
	}
	
	/**
	 * ���ݱ�Ż�ô������
	 * @param id ���
	 * @return �������
	 * @throws Exception û�ҵ���Ӧ����ʱ�׳��쳣
	 */
	public IProxy getConnectionProxy(int id) throws Exception{
		IProxy cp = getProxy(id);
		if(cp==null){
			throw new Exception("Connection "+id+" is not exist or out of time!");
		}
		return cp;
	}
	
	/**
	 * ʵ�ִ���ӿڣ��������ĸ÷���������
	 */
	public void close() {
	}

	/**
	 * ��ȡ�ı�����
	 * @return �����ı�
	 */
	public String toString() {
		return "ConnectionProxyManager";
	}
	
	
}