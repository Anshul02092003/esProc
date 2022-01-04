package com.scudata.server;

import com.scudata.app.config.RaqsoftConfig;

/**
 * �������ӿ�
 * Ŀǰʵ�������ַ�����
 * 1��UnitServer �ڵ��
 * 2��OdbcServer odbc������
 * 3��HttpServer http������
 * 
 * @author Joancy
 *
 */
public interface IServer extends Runnable {
	public static long devTimeout = 48*60*60*1000;//����
	public static long MAX_COUNT = 200;//���������200�μ���
	public static long startTime = System.currentTimeMillis();
	
	/**
	 * ��ȡ��ǰ������������״̬
	 * @return �������з���true�����򷵻�false
	 */
	public boolean isRunning();
	
	/**
	 * ��ȡ��ǰ�������ܷ��ڴ򿪽��洰�ں��Զ���������
	 * @return ��Ҫ�Զ���������true�����򷵻�false
	 */
	public boolean isAutoStart();
	
	/**
	 * ��ȡ����������IP��ַ
	 * @return IP��ַ
	 */
	public String getHost();
	
	/**
	 * ֹͣ����
	 */
	public void shutDown();
	
	/**
	 * ���÷�����������Ϣ
	 * @param rc ����
	 */
	public void setRaqsoftConfig(RaqsoftConfig rc);
	
	/**
	 * ��ȡ������Ϣ
	 */
	public RaqsoftConfig getRaqsoftConfig();
	
	/**
	 * ���÷���������������
	 * @param listen
	 */
	public void setStartUnitListener(StartUnitListener listen);

}
