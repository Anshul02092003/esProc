package com.scudata.parallel;

/**
 * �ಥ�������ӿ�
 * @author Joancy
 *
 */
public interface MulticastListener{
	/**
	 * ����������������һ���ڵ����ַ
	 * @param host ����IP��ַ
	 * @param port �����˿ں�
	 */
  public void addUnitClient(String host, int port);
}
