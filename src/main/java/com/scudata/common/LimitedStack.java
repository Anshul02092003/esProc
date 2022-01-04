package com.scudata.common;

import java.util.EmptyStackException;
import java.util.Vector;

/**
 * ���ƴ�С�Ķ�ջ
 */
public final class LimitedStack {

	/**
	 * ��Ա����
	 */
	private int maxSize = Integer.MAX_VALUE;
	/**
	 * ��ջ
	 */
	private Vector<Object> stack = new Vector<Object>();

	/**
	 * ���캯��
	 */
	public LimitedStack() {
	}

	/**
	 * ���캯��
	 * 
	 * @param maxSize
	 *            ��ջ��󳤶�
	 */
	public LimitedStack(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("limit must be bigger than 0");
		}
		this.maxSize = maxSize;
	}

	/**
	 * ���ö�ջ��󳤶ȣ�������������ȵ�ջ������
	 */
	public void setMaxSize(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("limit must be bigger than 0");
		}
		this.maxSize = maxSize;
		while (maxSize < size()) {
			stack.removeElementAt(0);
		}
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

	public Object push(Object item) {
		if (size() >= maxSize) {
			stack.removeElementAt(0);
		}
		stack.addElement(item);
		return item;
	}

	/**
	 * ջ��Ԫ�س�ջ
	 * 
	 * @return ��ջ��Ԫ��
	 */
	public Object pop() {
		Object obj = peek();
		stack.removeElementAt(size() - 1);
		return obj;
	}

	/**
	 * ȡջ��Ԫ��
	 * 
	 * @return ջ��Ԫ��
	 */
	public Object peek() {
		int len = size();
		if (len == 0) {
			throw new EmptyStackException();
		}
		return stack.elementAt(len - 1);
	}

	/**
	 * ���ջ
	 */
	public void clear() {
		stack.clear();
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
	 * ת�ַ���
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
