package com.scudata.util;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;

/**
 * �鲢�ֶ�����Ϊһ�����α�ڵ�
 * @author RunQian
 *
 */
public class LoserTreeNode_CS1 implements ILoserTreeNode {
	private ICursor cs;
	private int field;
	private boolean isNullMin = true; // null�Ƿ���Сֵ
	
	private Sequence data; // ���������
	private int seq; // -1��ʾ������ȡ��
	private Object value; // ��ǰ��¼��Ӧ��ֵ�����ڱȽ�
	
	public LoserTreeNode_CS1(ICursor cs, int field, boolean isNullMin) {
		this.field = field;
		this.isNullMin = isNullMin;
		data = cs.fuzzyFetch(ICursor.FETCHCOUNT_M);
		if (data != null && data.length() > 0) {
			this.cs = cs;
			seq = 1;
			BaseRecord r = (BaseRecord)data.getMem(1);
			value = r.getNormalFieldValue(field);
		} else {
			this.cs = null;
			seq = -1;
			value = null;
		}
	}
	
	public Object popCurrent() {
		Object obj = data.getMem(seq);
		if (seq < data.length()) {
			BaseRecord r = (BaseRecord)data.getMem(++seq);
			value = r.getNormalFieldValue(field);
		} else {
			data = cs.fuzzyFetch(ICursor.FETCHCOUNT_M);
			if (data != null && data.length() > 0) {
				seq = 1;
				BaseRecord r = (BaseRecord)data.getMem(1);
				value = r.getNormalFieldValue(field);
			} else {
				seq = -1;
				cs = null;
				value = null;
			}
		}
		
		return obj;
	}
	
	public boolean hasNext() {
		return seq != -1;
	}
	
	public int compareTo(ILoserTreeNode other) {
		if (isNullMin) {
			return Variant.compare(value, ((LoserTreeNode_CS1)other).value, true);
		} else {
			return Variant.compare_0(value, ((LoserTreeNode_CS1)other).value);
		}
	}
}
