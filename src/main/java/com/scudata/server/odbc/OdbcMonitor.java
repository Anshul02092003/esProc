package com.scudata.server.odbc;


import com.scudata.dm.*;
import com.scudata.server.ConnectionProxyManager;

/**
 * ODBC������
 * ����ʱ(System.currentTimeMillis()-proxy.lastAccessTime()>service.getTimeout)�Ķ���رղ�ɾ��
 * @author Joancy
 *
 */
public class OdbcMonitor extends Thread {
	volatile boolean stop = false;

	/**
	 * ����ODBC������
	 */
	public OdbcMonitor(){
		this.setName(toString());
	}
	
	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString(){
		return "OdbcMonitor";
	}

	/**
	 * ֹͣ�����߳�
	 */
	public void stopThread() {
		stop = true;
	}

	/**
	 * ���м����߳�
	 */
	public void run() {
		// timeOutΪ0ʱ������鳬ʱ
		int interval = 0;
		int conTimeOut = 0;
		try{
			OdbcContext jc = OdbcServer.getInstance().getContext();
			interval = jc.getConPeriod();
			conTimeOut = jc.getConTimeOut();
		}catch(Exception x){
			x.printStackTrace();
		}
		
		if (interval == 0) {
			return;
		}
		
		if (interval == 0 || conTimeOut == 0) {
			return;
		}
		//��ʱ�ĵ�λ��ΪСʱ���Ȼ�����Ϊ��λ xq 2016��12��22��
		conTimeOut = conTimeOut * 3600;
		while (!stop) {
			try {
				sleep(interval * 1000);
				JobSpaceManager.checkTimeOut(conTimeOut);
				ConnectionProxyManager.getInstance().checkTimeOut(conTimeOut);
			} catch (Exception x) {
			}
		}
	}
}
