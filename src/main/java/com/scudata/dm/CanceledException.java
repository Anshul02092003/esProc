package com.scudata.dm;

import com.scudata.common.MessageManager;
import com.scudata.resources.ParallelMessage;

/**
 * ��ҵ��ȡ����������쳣
 * @author Joancy
 *
 */
public class CanceledException extends RuntimeException {
	private static final long serialVersionUID = 4988636705167197473L;
	static MessageManager mm = ParallelMessage.get();

	public static String TYPE_DATASTORE = mm.getMessage("CanceledException.DataStore");
	public static String TYPE_IDE = "Canceled by IDE.";
	public static String TYPE_OTHER = "Canceled by other task.";
	
	/**
	 * ȱʡ���캯��
	 */
	public CanceledException() {
	}

	/**
	 * ���캯��
	 * @param msg ȡ��ԭ��
	 */
	public CanceledException(String msg) {
		super(msg);
	}

	/**
	 * ���캯��
	 * @param msg ȡ��ԭ��
	 * @param cause ���������쳣
	 */
	public CanceledException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * ���캯��
	 * @param cause ���������쳣
	 */
	public CanceledException(Throwable cause) {
		super(cause);
	}
}
