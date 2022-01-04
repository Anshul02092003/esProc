package com.scudata.common;

import java.text.MessageFormat;
import java.util.*;

/**
 * ��Ϣ�ı�������
 */
public class MessageManager {
	private static List<ClassLoader> clList = new ArrayList<ClassLoader>(8); 
	static {
		clList.add(MessageManager.class.getClassLoader());
	}
	
	/** ���Ӳ�����Դ����·�� */
	public static synchronized void addClassLoader(ClassLoader cl) {
		for(ClassLoader tmp : clList) {
			if(cl==tmp) return;
		}
		clList.add(cl);
	}
	
	private ResourceBundle bundle;

	private MessageManager(String fileName) {
		this(fileName, Locale.getDefault());
	}

	private MessageManager(String fileName, Locale loc) {
		for(ClassLoader tmp: clList) {
			try {
				bundle = ResourceBundle.getBundle(fileName, loc, tmp);
				if(bundle!=null) break;
			}catch (MissingResourceException ex) {
			}
		}
		if(bundle!=null) return;
		for(ClassLoader tmp: clList) {
			try {
				bundle = ResourceBundle.getBundle(fileName, Locale.US, tmp);
				if(bundle!=null) break;
			}catch (MissingResourceException ex) {
			}
		}
		if(bundle==null) throw new MissingResourceException(
			"Can't find bundle for base name " + fileName, "", null);
	}

	private MessageManager(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ı���
	 *
	 * @param key
	 *            ��ֵ
	 */
	public String getMessage(String key) {
		if (key == null)
			throw new IllegalArgumentException("key may not be a null value");
		String msg = key;// null; �ĳ��Ҳ������巵��Key��������Ӣ�Ŀ��Բ��ö�����Դ������ȱʡ����Key��xq
							// 2014.9.25
		try {
			msg = bundle.getString(key);
		} catch (MissingResourceException e) {
		}
		return msg;
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args
	 *            ����
	 */
	public String getMessage(String key, Object[] args) {
		String value = getMessage(key);

		try {
			if (args == null)
				return value;
			return MessageFormat.format(value, args);
		} catch (IllegalArgumentException e) {
			StringBuffer buf = new StringBuffer(64);
			buf.append(value);
			buf.append('\n');
			for (int i = 0; i < args.length; i++) {
				if (i > 0)
					buf.append(',');
				buf.append("arg[").append(i).append("]=").append(args[i]);
			}
			throw new IllegalArgumentException(buf.toString());
		}
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 */
	public String getMessage(String key, Object arg1) {
		return getMessage(key, new Object[] { arg1 });
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 * @param args2
	 *            �ڶ�������
	 */
	public String getMessage(String key, Object arg1, Object arg2) {
		return getMessage(key, new Object[] { arg1, arg2 });
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 * @param args2
	 *            �ڶ�������
	 * @param args3
	 *            ����������
	 */
	public String getMessage(String key, Object arg1, Object arg2, Object arg3) {
		return getMessage(key, new Object[] { arg1, arg2, arg3 });
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 * @param args2
	 *            �ڶ�������
	 * @param args3
	 *            ����������
	 * @param args4
	 *            ���ĸ�����
	 */
	public String getMessage(String key, Object arg1, Object arg2, Object arg3,
			Object arg4) {
		return getMessage(key, new Object[] { arg1, arg2, arg3, arg4 });
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 * @param args2
	 *            �ڶ�������
	 * @param args3
	 *            ����������
	 * @param args4
	 *            ���ĸ�����
	 * @param args5
	 *            ���������
	 */
	public String getMessage(String key, Object arg1, Object arg2, Object arg3,
			Object arg4, Object arg5) {
		return getMessage(key, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

	/**
	 * ����Դ���а���ֵȡ��Ϣ�ļ���������ָ���Ĳ������и�ʽ��
	 *
	 * @param key
	 *            ��ֵ
	 * @param args1
	 *            ��һ������
	 * @param args2
	 *            �ڶ�������
	 * @param args3
	 *            ����������
	 * @param args4
	 *            ���ĸ�����
	 * @param args5
	 *            ���������
	 * @param args6
	 *            ����������
	 */
	public String getMessage(String key, Object arg1, Object arg2, Object arg3,
			Object arg4, Object arg5, Object arg6) {
		return getMessage(key, new Object[] { arg1, arg2, arg3, arg4, arg5,
				arg6 });
	}

	private static Hashtable mgrs = new Hashtable();

	/**
	 * IDE�м����Ȩ���ܻ��л����ԣ���ʱӦ������Ѿ����ɵ�Manager������������Դ�����õ��л�ǰ��
	 * wunan 2020/7/9
	 */
	public synchronized static void clearManagers() {
		mgrs.clear();
	}

	/**
	 * ȡָ���ļ�����Ϣ�ı�������
	 *
	 * @param fileName
	 *            �ļ���
	 */
	public synchronized static MessageManager getManager(String fileName) {
		MessageManager mgr = (MessageManager) mgrs.get(fileName);
		if (mgr == null) {
			mgr = new MessageManager(fileName);
			mgrs.put(fileName, mgr);
		}
		return mgr;
	}

	/**
	 * ȡָ����Դ������Ϣ�ı�������
	 *
	 * @param bundle
	 *            ��Դ��
	 */
	public synchronized static MessageManager getManager(ResourceBundle bundle) {
		return new MessageManager(bundle);
	}

	/**
	 * ȡָ����ָ���������Ϣ�ı�������
	 *
	 * @param fileName
	 *            �ļ���
	 * @param loc
	 *            ����
	 */
	public synchronized static MessageManager getManager(String fileName,
			Locale loc) {
		String bundleName = fileName + "_" + loc.toString();
		MessageManager mgr = (MessageManager) mgrs.get(bundleName);
		if (mgr == null) {
			mgr = new MessageManager(fileName, loc);
			mgrs.put(bundleName, mgr);
		}
		return mgr;
	}
}
