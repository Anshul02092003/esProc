package com.scudata.dm.comparator;

import java.util.Comparator;

import com.scudata.util.Variant;

/**
 * ʹ��ָ�����ԱȽ������бȽϵ�����Ƚ�����null������С����
 * @author WangXiaoJun
 *
 */
public class LocaleArrayComparator implements Comparator<Object> {
	private Comparator<Object> locCmp;
	private final int len;
	
	public LocaleArrayComparator(Comparator<Object> locCmp, int len) {
		this.locCmp = locCmp;
		this.len = len;
	}
	
	public int compare(Object o1, Object o2) {
		return Variant.compareArrays((Object[])o1, (Object[])o2, len, locCmp);
	}
}
