package com.scudata.vdb;

import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * Ŀ¼������
 * @author RunQian
 *
 */
class DirFilter {
	private Object rightValue;
	private Sequence values; // ���б�ʾ������ϵ
	private boolean valueSign; // true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������

	public DirFilter(Object rightValue, boolean valueSign) {
		this.rightValue = rightValue;
		this.valueSign = valueSign;
		if (rightValue instanceof Sequence) {
			values = (Sequence)rightValue;
		}
	}
	
	public boolean match(Object value) {
		if (values == null) {
			if (valueSign) {
				return Variant.isEquals(value, rightValue);
			} else {
				return true;
			}
		} else {
			return values.contains(value, false);
		}
	}
}