package com.scudata.common;

/**
 * ����Դ������ܽ��ܽӿ�
 *
 */
public interface IPwd {
	/**
	 * ����
	 * @param pwd ����
	 * @return ���ܺ������
	 */
	public String encrypt(String pwd);

	/**
	 * ����
	 * @param pwd ���ܵ�����
	 * @return ԭ���루���ܺ�
	 */
	public String decrypt(String pwd);
}