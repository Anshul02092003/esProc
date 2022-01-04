package com.scudata.dm;

import java.io.IOException;

/**
 * ��������
 */
public interface ILineInput {
	/**
	 * ������һ�У�����������򷵻�null
	 * @return Object[]
	 * @throws IOException
	 */
	Object[] readLine() throws IOException;
	
	/**
	 * ������һ�У�����������򷵻�false�����򷵻�true
	 * @return boolean
	 * @throws IOException
	 */
	boolean skipLine() throws IOException;
	
	/**
	 * �ر�����
	 * @throws IOException
	 */
	void close() throws IOException;
}
