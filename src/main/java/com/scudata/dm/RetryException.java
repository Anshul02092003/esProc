package com.scudata.dm;

/**
 * ��ҵ������Ҫ����ʱ���׳����쳣
 * @author Joancy
 *
 */
public class RetryException extends RuntimeException {
	private static final long serialVersionUID = -1177620135140049645L;

	/**
	 * �չ��캯��
	 */
	public RetryException() {
	}

	/**
	 * ���캯��
	 * @param msg ����ԭ��
	 */
	public RetryException(String msg) {
		super(msg);
	}

	/**
	 * ���캯��
	 * @param msg ����ԭ��
	 * @param cause ���������쳣
	 */
	public RetryException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * ���캯��
	 * @param cause ���������쳣
	 */
	public RetryException(Throwable cause) {
		super(cause);
	}
}
