package com.scudata.dm.cursor;

/**
 * ��·�α�ӿڣ������Ǳ��ض�·�α��Ⱥ�α�
 * @author RunQian
 *
 */
public interface IMultipath {
	/**
	 * ȡ�α�·��
	 * @return ·��
	 */
	public int getPathCount();
	
	/**
	 * ȡÿһ·��Ӧ���α�
	 * @return �α�����
	 */
	public ICursor[] getParallelCursors();
}