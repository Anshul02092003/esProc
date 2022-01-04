package com.scudata.dm;

import com.scudata.expression.Expression;

/**
 * �ڴ����������
 * @author WangXiaoJun
 *
 */
abstract public class IndexTable {
	/**
	 * ���źż������ɶ����״����
	 * @param code
	 * @param field
	 * @return
	 */
	public static IndexTable instance_s(Sequence code, int field) {
		SerialBytesIndexTable it = new SerialBytesIndexTable();
		it.create(code, field);
		return it;
	}
	
	/**
	 * Ϊ���д�������
	 * @param code ����
	 * @return
	 */
	public static IndexTable instance(Sequence code) {
		return instance(code, code.length());
	}
	
	/**
	 * Ϊ���д�������
	 * @param code ����
	 * @param capacity ����������
	 * @return
	 */
	public static IndexTable instance(Sequence code, int capacity) {
		HashIndexTable it = new HashIndexTable(capacity);
		it.create(code);
		return it;
	}
	
	/**
	 * Ϊ���а�ָ�����ʽ��������
	 * @param code ����
	 * @param exp �ֶα��ʽ
	 * @param ctx
	 * @return
	 */
	public static IndexTable instance(Sequence code, Expression exp, Context ctx) {
		return instance(code, exp, ctx, code.length());
	}
	
	/**
	 * Ϊ���а�ָ�����ʽ��������
	 * @param code ����
	 * @param exp �ֶα��ʽ
	 * @param ctx
	 * @param capacity ��ϣ������
	 * @return
	 */
	public static IndexTable instance(Sequence code, Expression exp, Context ctx, int capacity) {
		HashIndexTable it = new HashIndexTable(capacity);
		it.create(code, exp, ctx);
		return it;
	}

	/**
	 * Ϊ���а�ָ�����ʽ�������ֶ�����
	 * @param code ����
	 * @param exps �ֶα��ʽ����
	 * @param ctx
	 * @return
	 */
	public static IndexTable instance(Sequence code, Expression []exps, Context ctx) {
		return instance(code, exps, ctx, code.length());
	}

	public static IndexTable instance(Sequence code, Expression []exps, Context ctx, int capacity) {
		if (exps == null) {
			return instance(code, capacity);
		} else if (exps.length == 1) {
			return instance(code, exps[0], ctx, capacity);
		} else {
			HashArrayIndexTable it = new HashArrayIndexTable(capacity);
			it.create(code, exps, ctx);
			return it;
		}
	}

	public static IndexTable instance(Sequence code, int []fields, int capacity, String opt) {
		if (fields.length == 1) {
			HashIndexTable it = new HashIndexTable(capacity, opt);
			it.create(code, fields[0]);
			return it;
		} else {
			HashArrayIndexTable it = new HashArrayIndexTable(capacity, opt);
			it.create(code, fields);
			return it;
		}
	}
	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ���˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param key ��
	 * @return
	 */
	abstract public Object find(Object key);

	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ���˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param keys ��ֵ����
	 * @return
	 */
	abstract public Object find(Object []keys);
}
