package com.scudata.dm;

import java.io.IOException;

/**
 * ��������ӿ�
 * @author WangXiaoJun
 *
 */
public interface ILineOutput {
	/**
	 * д��һ������
	 * @param items ��ֵ��ɵ�����
	 * @throws IOException
	 */
	void writeLine(Object []items) throws IOException;
	
	/**
	 * �ر����
	 * @throws IOException
	 */
	void close() throws IOException;
}
