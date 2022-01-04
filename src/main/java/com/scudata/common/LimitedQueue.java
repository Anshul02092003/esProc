package com.scudata.common;

import java.util.EmptyStackException;
import java.util.Vector;

/**
 * ���ƴ�С�Ķ���
 */
public final class LimitedQueue {

	/**
	 * �����ĳ�Ա����
	 */
	private int maxSize = Integer.MAX_VALUE;
	/**
	 * ����
	 */
	private Vector<Object> stack = new Vector<Object>();

	/**
	 * �Ƿ����˱仯
	 */
	private boolean changed = false;

	/**
	 * ���캯��
	 */
	public LimitedQueue() {
	}

	/**
	 * ���캯��
	 * 
	 * @param maxSize
	 *            ��󳤶�
	 */
	public LimitedQueue(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("limit must be bigger than 0");
		}
		this.maxSize = maxSize;
	}

	/**
	 * ȡ�Ƿ����˱仯
	 * 
	 * @return
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * �����Ƿ����˱仯
	 */
	public void setUnChanged() {
		changed = false;
	}

	/**
	 * ������󳤶ȣ�������������ȵ�����
	 */
	public void setMaxSize(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("limit must be bigger than 0");
		}
		this.maxSize = maxSize;
		while (maxSize < size()) {
			stack.removeElementAt(size() - 1);
		}
		changed = true;
	}

	/**
	 * ȡ��ջ��󳤶�
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * ȡ��ջ��Ԫ�ظ���
	 */
	public int size() {
		return stack.size();
	}

	/**
	 * Ԫ�ؽ���ջ��
	 * 
	 * @param item
	 *            ��ջ��Ԫ��
	 */

	public Object add(Object item) {
		if (size() >= maxSize) {
			stack.removeElementAt(0);
		}
		stack.addElement(item);
		changed = true;
		return item;
	}

	/**
	 * �����ȡ��Ա
	 * 
	 * @param index
	 *            ���
	 * @return
	 */
	public Object get(int index) {
		return stack.get(index);
	}

	/**
	 * ���׸�Ԫ�شӶ����е�������������ǿյ��׳�EmptyStackException
	 * 
	 * @return
	 */
	public Object poll() {
		Object obj = peek();
		stack.removeElementAt(0);
		changed = true;
		return obj;
	}

	/**
	 * �鿴�׸�Ԫ�أ������Ƴ��׸�Ԫ�أ���������ǿյ��׳�EmptyStackException
	 * 
	 * @return
	 */
	public Object peek() {
		int len = size();
		if (len == 0) {
			throw new EmptyStackException();
		}
		return stack.elementAt(0);
	}

	/**
	 * ���ջ
	 */
	public void clear() {
		stack.clear();
		changed = true;
	}

	/**
	 * ����Ƿ��ջ
	 */
	public boolean empty() {
		return size() == 0;
	}

	/**
	 * ���ջ�Ƿ���
	 */
	public boolean isFull() {
		return size() == maxSize;
	}

	/**
	 * ת���ַ���
	 */
	public String toString() {
		int len = size();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(stack.elementAt(i)).append(';');
		}
		return sb.toString();
	}

}
