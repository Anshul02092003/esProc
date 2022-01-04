package com.scudata.dm.comparator;

import java.util.Comparator;

import com.scudata.util.Variant;

/**
 * null������С����ıȽ���
 * @author WangXiaoJun
 *
 */
public class BaseComparator implements Comparator<Object> {
	private boolean throwExcept = true;
	
	public BaseComparator() {
	}
	
	public BaseComparator(boolean throwExcept) {
		this.throwExcept = throwExcept;
	}

	public int compare(Object o1, Object o2) {
		return Variant.compare(o1, o2, throwExcept);
	}
}
