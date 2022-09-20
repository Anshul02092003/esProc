package com.scudata.dm.comparator;

import java.util.Comparator;

import com.ibm.icu.text.Collator;
import com.scudata.common.ICloneable;
import com.scudata.common.RQException;
import com.scudata.util.Variant;

// Env.Locale��Ӧ�ıȽ�����ȱʡ�Ƚ��ַ�����unicodeֵ
public class LocaleComparator implements Comparator<Object>, ICloneable {
	private final Collator collator; // ���ԱȽ����������ַ����Ƚ�
	private boolean throwExcept = true;

	public LocaleComparator(Collator collator) {
		this.collator = collator;
	}

	public LocaleComparator(Collator collator, boolean throwExcept) {
		this.collator = collator;
		this.throwExcept = throwExcept;
	}
	
	public Object deepClone() {
		try {
			return new LocaleComparator((Collator)collator.clone());
		} catch (CloneNotSupportedException e) {
			throw new RQException(e);
		}
	}

	public int compare(Object o1, Object o2) {
		return Variant.compare(o1, o2, collator, throwExcept);
	}
}
