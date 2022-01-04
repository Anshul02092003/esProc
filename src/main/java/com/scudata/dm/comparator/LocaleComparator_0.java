package com.scudata.dm.comparator;

import java.util.Comparator;

import com.scudata.util.Variant;

// Env.Locale��Ӧ�ıȽ�����ȱʡ�Ƚ��ַ�����unicodeֵ
public class LocaleComparator_0 implements Comparator<Object> {
	private Comparator<Object> locCmp;

	public LocaleComparator_0(Comparator<Object> locCmp) {
		this.locCmp = locCmp;
	}

	public int compare(Object o1, Object o2) {
		return Variant.compare_0(o1, o2, locCmp);
	}
}
