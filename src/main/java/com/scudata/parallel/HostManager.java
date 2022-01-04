package com.scudata.parallel;

import com.scudata.common.MessageManager;
import com.scudata.dm.Env;
import com.scudata.resources.ParallelMessage;

/**
 * �ֻ�������
 * 
 * @author Joancy
 *
 */
public class HostManager {
	private static HostManager instance = null;
	// �����߳�ִ��ʱ��host���ᱻ��ֵ���ڷֻ��������Ż���host
	String host = null;
	int port;
	
	static int preferredTaskNum = Runtime.getRuntime().availableProcessors();
	static int maxTaskNum = preferredTaskNum * 4;

	static MessageManager mm = ParallelMessage.get();
	
	private HostManager() {
	}

	/**
	 * �ֻ���������Ψһʵ��
	 * @return �ֻ�������
	 */
	public static HostManager instance() {
		if (instance == null) {
			instance = new HostManager();
		}
		return instance;
	}

	/**
	 * ��ȡ�ֻ���ip
	 * @return ip��ַ
	 */
	public String getHost() {
		return host;
	}

	/**
	 * ���÷ֻ�IP
	 * @param ip ip��ַ
	 */
	public void setHost(String ip) {
		host = ip;
		Env.setLocalHost(ip);
	}

	/**
	 * ��ȡ�ֻ��˿ں�
	 * @return �˿ں�
	 */
	public int getPort() {
		return port;
	}

	/**
	 * ���÷ֻ��Ķ˿ں�
	 * @param p �˿ں�
	 */
	public void setPort(int p) {
		port = p;
		Env.setLocalPort(p);
	}

	/**
	 * ��ȡ�ֻ������ҵ��
	 * @return
	 */
	public int getMaxTaskNum() {
		return maxTaskNum;
	}

	/**
	 * ���������ҵ��Ŀ
	 * @param num ��ҵ��Ŀ
	 */
	public void setMaxTaskNum(int num) {
		maxTaskNum = num;
	}

	/**
	 * �ֻ��ʺ���ҵ��
	 * @return
	 */
	public int getPreferredTaskNum() {
		return preferredTaskNum;
	}

	/**
	 * ���÷ֻ����ʺ���ҵ��
	 * @param num �ʺ���ҵ��
	 */
	public void setPreferredTaskNum(int num) {
		preferredTaskNum = num;
	}

	/**
	 * ��ȡ��ǰ�������������
	 * @return ��ǰ����������Ŀ
	 */
	public int getCurrentTasks() {
		return PerfMonitor.getConcurrentTasks();
	}

	/**
	 * ʵ��toString������Ϣ
	 */
	public String toString() {
		if (host == null) {
			return "local";
		}
		return host + ":" + port;
	}

	/**
	 * �Ƿ�windowsϵͳ
	 * @return windows����true�����򷵻�false
	 */
	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.startsWith("Windows");
	}


}
