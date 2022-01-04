package com.scudata.parallel;

/**
 * ��ҵ�ӿ�
 * @author Joancy
 *
 */
public interface ITask{
	/**
	 * ˢ�·���ʱ��
	 */
	public void access();
	
	/**
	 * ���÷���ʱ�� 
	 */
	public void resetAccess();
	
	/**
	 * ȡ������
	 * @return ���
	 */
	public int getTaskID();
	
	 /**
	  * ������ʱ
	  * @param timeOut ��ʱʱ��
	  * @return ��������true������false
	  */
	public boolean checkTimeOut(int timeOut);
	
	/**
	 * �رյ�ǰ��ҵ
	 */
	public void close();
	
	/**
	 * ȡԶ���α���������
	 * @return ������
	 */
	public RemoteCursorProxyManager getCursorManager();
}
