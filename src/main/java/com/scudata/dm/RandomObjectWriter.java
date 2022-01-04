package com.scudata.dm;

import java.io.IOException;

/**
 * ���Ըı����λ�õ�д����
 * @author WangXiaoJun
 *
 */
final public class RandomObjectWriter extends ObjectWriter {
	RandomOutputStream ros;
	
	/**
	 * �������Ըı����λ�õ�д����
	 * @param out ���Ըı����λ�õ������
	 */
	public RandomObjectWriter(RandomOutputStream out) {
		super(out);
		this.ros = out;
	}

	/**
	 * ���ص�ǰ���λ��
	 * @return
	 * @throws IOException
	 */
	public long position() throws IOException {
		return ros.position() + count;
	}
	
	/**
	 * �������λ��
	 * @param newPosition
	 * @throws IOException
	 */
	public void position(long newPosition) throws IOException {
		flush();
		ros.position(newPosition);
	}
}
