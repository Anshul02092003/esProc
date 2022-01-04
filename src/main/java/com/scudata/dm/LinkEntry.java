package com.scudata.dm;

/**
 * ������Ŀ
 * @author WangXiaoJun
 *
 * @param <E>
 */
public class LinkEntry<E> {
	private E element;
	private LinkEntry<E> next;
	
	/**
	 * ����������Ŀ
	 * @param element Ԫ��ֵ
	 */
	public LinkEntry(E element) {
		this.element = element;
	}
	
	/**
	 * ����������Ŀ
	 * @param element Ԫ��ֵ
	 * @param next ��һ��������Ŀ
	 */
	public LinkEntry(E element, LinkEntry<E> next) {
		this.element = element;
		this.next = next;
	}
	
	/**
	 * ȡԪ��ֵ
	 * @return
	 */
	public E getElement() {
		return element;
	}
	
	/**
	 * ȡ��һ����Ŀ
	 * @return
	 */
	public LinkEntry<E> getNext() {
		return next;
	}
	
	/**
	 * ������һ����Ŀ
	 * @param next
	 */
	public void setNext(LinkEntry<E> next) {
		this.next = next;
	}
}