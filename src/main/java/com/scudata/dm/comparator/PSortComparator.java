package com.scudata.dm.comparator;

import java.util.Comparator;

/**
 * PSortItem����Ƚ��������ڷ���λ�õ�����
 * @author WangXiaoJun
 *
 */
public class PSortComparator implements Comparator<Object> {
	private Comparator<Object> comparator;

	/**
	 * ��ָ���Ƚ����������
	 * @param comparator Comparator ����Ϊ��
	 */
	public PSortComparator(Comparator<Object> comparator) {
		this.comparator = comparator;
	}

	public int compare(Object o1, Object o2) {
		return comparator.compare(((PSortItem)o1).value, ((PSortItem)o2).value);
	}
}
