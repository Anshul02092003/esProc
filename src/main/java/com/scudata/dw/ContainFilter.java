package com.scudata.dw;

import com.scudata.dm.Sequence;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.Variant;

/**
 * ������������
 * �����ж�һ�������Ƿ�����ڸ�����������
 * @author runqian
 *
 */
class ContainFilter extends IFilter {
	public static final int BINARYSEARCH_COUNT = 3; // Ԫ�ظ������ڴ�ֵ���ö��ַ�����
	private Object []values;

	/**
	 * ������
	 * @param column �ж���
	 * @param priority ���ȼ�
	 * @param sequence ����������
	 * @param opt ����bʱ��ʾsequence�Ѿ�����
	 */
	public ContainFilter(ColumnMetaData column, int priority, Sequence sequence, String opt) {
		super(column, priority);
		values = sequence.toArray();
		if (opt == null || opt.indexOf('b') == -1) {
			MultithreadUtil.sort(values);
		}
	}
	
	/**
	 * ������ (�����д�)
	 * @param columnName ������
	 * @param priority ���ȼ�
	 * @param sequence ����������
	 * @param opt ����bʱ��ʾsequence�Ѿ�����
	 */
	public ContainFilter(String columnName, int priority, Sequence sequence, String opt) {
		this.columnName = columnName;
		this.priority = priority;
		values = sequence.toArray();
		if (opt == null || opt.indexOf('b') == -1) {
			MultithreadUtil.sort(values);
		}
	}
	
	public boolean match(Object value) {
		Object []values = this.values;
		int len = values.length;
		if (len > BINARYSEARCH_COUNT) {
			// ���ַ�����
			int low = 0;
			int high = len - 1;

			while (low <= high) {
				int mid = (low + high) >>> 1;
				int cmp = Variant.compare(values[mid], value, true);
				
				if (cmp < 0)
					low = mid + 1;
				else if (cmp > 0)
					high = mid - 1;
				else
					return true; // key found
			}
		} else {
			for (Object v : values) {
				if (Variant.isEquals(value, v)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean match(Object minValue, Object maxValue) {
		Object []values = this.values;
		int len = values.length;
		
		// ���ַ�������Сֵ�������е�λ��
		int low1 = 0;
		int high1 = len - 1;
		while (low1 <= high1) {
			int mid = (low1 + high1) >>> 1;
			int cmp = Variant.compare(values[mid], minValue, true);
			
			if (cmp < 0)
				low1 = mid + 1;
			else if (cmp > 0)
				high1 = mid - 1;
			else
				return true; // key found
		}
		
		// ����Сֵ�ȼ������ֵ����߿���Сֵ�������ֵ��û�з��������ļ�¼
		if (low1 >= len || Variant.isEquals(minValue, maxValue)) {
			return false;
		}
		
		// ���ַ��������ֵ�������е�λ��
		int low2 = 0;
		int high2 = len - 1;
		while (low2 <= high2) {
			int mid = (low2 + high2) >>> 1;
			int cmp = Variant.compare(values[mid], maxValue, true);
			
			if (cmp < 0)
				low2 = mid + 1;
			else if (cmp > 0)
				high2 = mid - 1;
			else
				return true; // key found
		}
		
		// �������Сֵ�Ϳ����ֵ�ڼ����еĲ���λ����ͬ��û�з��������ļ�¼
		return low1 != low2;
	}
}