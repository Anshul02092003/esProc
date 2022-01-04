package com.scudata.resources;

import java.util.Locale;

import com.scudata.common.MessageManager;

/**
 * ������ش������Դ��
 * @author Joancy
 *
 */
public class ParallelMessage {

	private ParallelMessage() {}

	public static MessageManager get() {
		return MessageManager.getManager("com.scudata.resources.parallelMessage");
	}

	public static MessageManager get(Locale locale) {
		return MessageManager.getManager("com.scudata.resources.parallelMessage", locale);
	}

}
