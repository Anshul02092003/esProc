package com.scudata.dw.compress;

import java.io.IOException;

import com.scudata.dw.BufferReader;

/**
 * �ڴ���
 * ʹ�û������ʹ�һ������
 * @author runqian
 *
 */
public abstract class Column implements Cloneable {
	public static final int BLOCK_RECORD_COUNT = 8192; // ÿ���¼��
	/**
	 * ׷��һ�е�����
	 * @param data
	 */
	abstract public void addData(Object data);
	
	/**
	 * ȡ��row�е�����
	 * @param row
	 * @return
	 */
	abstract public Object getData(int row);
	
	abstract public Column clone();
	
	/**
	 * ��br��һ������׷�ӵ���
	 * @param br
	 * @throws IOException
	 */
	abstract public void appendData(BufferReader br) throws IOException ;
}
