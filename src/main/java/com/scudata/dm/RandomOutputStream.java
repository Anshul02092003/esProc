package com.scudata.dm;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ���Ըı����λ�õ������
 * @author WangXiaoJun
 *
 */
public abstract class RandomOutputStream extends OutputStream {
	/**
	 * �������λ��
	 * @param newPosition
	 * @throws IOException
	 */
	public abstract void position(long newPosition) throws IOException;
	
	/**
	 * ���ص�ǰ���λ��
	 * @return
	 * @throws IOException
	 */
	public abstract long position() throws IOException;
	
	/**
	 * ��������ɹ�����true
	 * @return
	 * @throws IOException
	 */
	public abstract boolean tryLock() throws IOException;
	
	/**
	 * �ȴ�����ֱ�����ɹ�
	 * @return boolean
	 * @throws IOException
	 */
	public boolean lock() throws IOException {
		return true;
	}
	
	/**
	 * ȡ��ָ��λ�ÿ�ʼ��������
	 * @param pos λ��
	 * @return InputStream
	 * @throws IOException
	 */
	public InputStream getInputStream(long pos) throws IOException {
		return null;
	}
}