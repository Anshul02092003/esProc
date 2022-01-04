package com.scudata.dm.comparator;

import java.util.Comparator;

import com.scudata.util.Variant;

/**
 * ʹ��ָ�����ԱȽ������бȽϵ�����Ƚ�����null���������
 * @author WangXiaoJun
 *
 */
public class LocaleArrayComparator_0 implements Comparator<Object> {
	private Comparator<Object> locCmp;
	private final int len;
	
	public LocaleArrayComparator_0(Comparator<Object> locCmp, int len) {
		this.locCmp = locCmp;
		this.len = len;
	}
	
	public int compare(Object o1, Object o2) {
		return Variant.compareArrays_0((Object[])o1, (Object[])o2, len, locCmp);
	}
}
