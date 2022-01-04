package com.scudata.dm.comparator;

import java.util.Comparator;

/**
 * ��������Ƚ���
 * @author WangXiaoJun
 *
 */
public class DescComparator implements Comparator<Object> {
	private Comparator<Object> comparator;

	public DescComparator(Comparator<Object> comparator) {
		this.comparator = comparator;
	}

	public DescComparator() {
		this.comparator = new BaseComparator();
	}

	public int compare(Object o1, Object o2) {
		return comparator.compare(o2, o1);
	}
}
