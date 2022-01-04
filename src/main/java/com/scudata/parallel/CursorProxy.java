package com.scudata.parallel;

import com.scudata.dm.cursor.ICursor;

/**
 * �α����
 * 
 * @author Joancy
 *
 */
public class CursorProxy extends IProxy {
	private ICursor cursor;
	private int unit; // �ڵ����ţ���0��ʼ����
		
	/**
	 * �����α����
	 * @param cursor �α�
	 * @param unit �ڵ�����
	 */
	public CursorProxy(ICursor cursor, int unit) {
		this.cursor = cursor;
		this.unit = unit;
	}
	
	/**
	 * �ر��α����
	 */
	public void close() {
		if (cursor != null) {
			cursor.close();
		}
	}
	
	/**
	 * ��ȡ�α����
	 * @return �α�
	 */
	public ICursor getCursor() {
		return cursor;
	}
	
	/**
	 * �����α����
	 * @param cursor �α�
	 */
	void setCursor(ICursor cursor) {
		this.cursor = cursor;
	}
	
	/**
	 * ��ȡ�ֻ����
	 * @return ���
	 */
	public int getUnit() {
		return unit;
	}
	
	/**
	 * ���÷ֻ����
	 * @param unit ���
	 */
	void setUnit(int unit) {
		this.unit = unit;
	}
}