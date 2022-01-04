package com.scudata.resources;

import java.util.Locale;

import com.scudata.common.MessageManager;

/**
 * Ӧ����ص���Դ��
 *
 */
public class AppMessage {

	private AppMessage() {
	}

	public static MessageManager get() {
		return MessageManager.getManager("com.scudata.resources.appMessage");
	}

	public static MessageManager get(Locale locale) {
		return MessageManager.getManager("com.scudata.resources.appMessage",
				locale);
	}

}
