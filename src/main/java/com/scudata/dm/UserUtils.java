package com.scudata.dm;

import java.util.List;

import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.LineInputCursor;

/**
 * ���⹤����
 */
public final class UserUtils {
	/**
	 * ������������α�
	 * @param lineInput ILineInput ������
	 * @param opt String ѡ�� t����һ��Ϊ���⣬i�������ֻ��1��ʱ���س�����
	 * @return ICursor �α�
	 */
	public static ICursor newCursor(ILineInput lineInput, String opt) {
		return new LineInputCursor(lineInput, opt);
	}
	
	/**
	 * �������������������
	 * @param lineInput ILineInput ������
	 * @param opt String ѡ�� t����һ��Ϊ���⣬i�������ֻ��1��ʱ���س�����
	 * @return Sequence ��������
	 */
	public static Sequence newTable(ILineInput lineInput, String opt) {
		return newCursor(lineInput, opt).fetch();
	}
	
	/**
	 * �������������
	 * @param values Object[] ֵ����
	 * @return Sequence
	 */
	public static Sequence newSequence(Object []values) {
		return new Sequence(values);
	}
	
	/**
	 * ��List��������
	 * @param list List ֵList
	 * @return Sequence
	 */
	public static Sequence newSequence(List<Object> list) {
		return new Sequence(list.toArray());
	}
}