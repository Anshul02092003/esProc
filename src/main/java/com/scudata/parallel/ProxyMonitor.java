package com.scudata.parallel;

import com.scudata.dm.*;
import com.scudata.server.unit.UnitServer;

/**
 * ���������
 * ��ʱ(System.currentTimeMillis()-proxy.lastAccessTime()>service.getTimeout)�Ķ���رղ�ɾ��
 * @author Joancy
 *
 */

public class ProxyMonitor extends Thread {
	volatile boolean stop = false;

	/**
	 * �������������
	 */
	public ProxyMonitor(){
		this.setName(toString());
	}
	
	/**
	 * ʵ��toString����
	 */
	public String toString(){
		return "ProxyMonitor";
	}

	/**
	 * ֹͣ�߳�
	 */
	public void stopThread() {
		stop = true;
	}

	/**
	 * �����߳�
	 */
	public void run() {
		// timeOutΪ0ʱ������鳬ʱ
		UnitContext uc = UnitServer.instance.getUnitContext();
		int interval = uc.getInterval();
		int proxyTimeOut = uc.getProxyTimeOut();
		if (interval == 0 || proxyTimeOut == 0) {
			return;
		}
		
		//��ʱ�ĵ�λ��ΪСʱ���Ȼ�����Ϊ��λ xq 2016��12��22��
		proxyTimeOut = proxyTimeOut*3600;
		
		while (!stop) {
			try {
				sleep(interval * 1000);
				TaskManager.checkTimeOut(proxyTimeOut);//������ص��α����
				RemoteFileProxyManager.checkTimeOut(proxyTimeOut);
				RemoteCursorProxyManager.checkTimeOut(proxyTimeOut);
				JobSpaceManager.checkTimeOut(proxyTimeOut);
				com.scudata.server.ConnectionProxyManager.getInstance().checkTimeOut(proxyTimeOut);
			} catch (Exception x) {
			}
		}
	}
}
