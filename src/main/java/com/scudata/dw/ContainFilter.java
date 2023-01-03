package com.scudata.dw;

import com.scudata.array.IArray;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.util.Variant;

/**
 * ������������
 * �����ж�һ�������Ƿ�����ڸ�����������
 * @author runqian
 *
 */
public class ContainFilter extends IFilter {
	public static final int BINARYSEARCH_COUNT = 3; // Ԫ�ظ������ڴ�ֵ���ö��ַ�����
	private IArray values;
	/**
	 * ������
	 * @param column �ж���
	 * @param priority ���ȼ�
	 * @param sequence ����������
	 * @param opt ����bʱ��ʾsequence�Ѿ�����
	 */
	public ContainFilter(ColumnMetaData column, int priority, Sequence sequence, String opt) {
		super(column, priority);
		values = sequence.getMems();
		if (opt == null || opt.indexOf('b') == -1) {
			values.sort();
		}
	}
	
	public ContainFilter(ColumnMetaData column, int priority, Sequence sequence, String opt, Node node) {
		super(column, priority);
		values = sequence.getMems();
		if (opt == null || opt.indexOf('b') == -1) {
			values.sort();
		}
		if (node != null) exp = new Expression(node);
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
		values = sequence.getMems();
		if (opt == null || opt.indexOf('b') == -1) {
			values.sort();
		}
	}
	
	public boolean match(Object value) {
		return values.binarySearch(value) > 0;
	}
	
	public boolean match(Object minValue, Object maxValue) {
		IArray values = this.values;
		int len = values.size();
		
		// ���ַ�������Сֵ�������е�λ��
		int low1 = 1;
		int high1 = len;
		while (low1 <= high1) {
			int mid = (low1 + high1) >>> 1;
			int cmp = Variant.compare(values.get(mid), minValue, true);
			
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
			int cmp = Variant.compare(values.get(mid), maxValue, true);
			
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