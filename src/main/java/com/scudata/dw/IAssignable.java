package com.scudata.dw;

/**
 * �ɸ�ֵ�Ķ��� ���������¶���
 * @author LW
 *
 */
public interface IAssignable {
	public int getDataType();
	public int compareTo(IAssignable o);
	public Object toObject();
}
