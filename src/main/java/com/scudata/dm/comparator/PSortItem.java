package com.scudata.dm.comparator;

/**
 * ������󷵻�λ����Ϣ������
 * @author WangXiaoJun
 *
 */
public class PSortItem {
	public int index; // ��1��ʼ����
	public Object value;
	
	public PSortItem(int i, Object obj) {
		index = i;
		value = obj;
	}
	
	public int getIndex() {
		return index;
	}
}
