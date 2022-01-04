package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;

/**
 * �ڴ������ܽ������
 * @author RunQian
 *
 */
abstract public class IGroupsResult implements IResult {
	/**
	 * ���ݲ���ȡ�ڴ������ܴ�����
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return IGroupsResult
	 */
	public static IGroupsResult instance(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx) {
		if (exps != null) {
			int count = exps.length;
			if (names == null) {
				names = new String[count];
			}
			
			for (int i = 0; i < count; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					names[i] = exps[i].getFieldName();
				}
			}
		}

		if (calcExps != null) {			
			int valCount = calcExps.length;
			if (calcNames == null) {
				calcNames = new String[valCount];
			}
			
			for (int i = 0; i < valCount; ++i) {
				if (calcNames[i] == null || calcNames[i].length() == 0) {
					calcNames[i] = calcExps[i].getFieldName();
				}
			}
		}
		
		boolean XOpt = false;
		if (opt != null && opt.indexOf('X') != -1)
			XOpt = true;
		if (exps != null && exps.length == 1 && !XOpt) {
			String gname = names == null ? null : names[0];
			return new Groups1Result(exps[0], gname, calcExps, calcNames, opt, ctx);
		} else {
			return new GroupsResult(exps, names, calcExps, calcNames, opt, ctx);
		}
	}
	
	/**
	 * ���ݲ���ȡ�ڴ������ܴ�����
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity ���ڷ�������Ĺ�ϣ������
	 * @return IGroupsResult
	 */
	public static IGroupsResult instance(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx, int capacity) {
		if (exps != null) {
			int count = exps.length;
			if (names == null) {
				names = new String[count];
			}
			
			for (int i = 0; i < count; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					names[i] = exps[i].getFieldName();
				}
			}
		}

		if (calcExps != null) {			
			int valCount = calcExps.length;
			if (calcNames == null) {
				calcNames = new String[valCount];
			}
			
			for (int i = 0; i < valCount; ++i) {
				if (calcNames[i] == null || calcNames[i].length() == 0) {
					calcNames[i] = calcExps[i].getFieldName();
				}
			}
		}
		
		if (exps != null && exps.length == 1) {
			String gname = names == null ? null : names[0];
			return new Groups1Result(exps[0], gname, calcExps, calcNames, opt, ctx, capacity);
		} else {
			return new GroupsResult(exps, names, calcExps, calcNames, opt, ctx, capacity);
		}
	}

	/**
	 * ȡ��������ݽṹ
	 * @return DataStruct
	 */
	abstract public DataStruct getResultDataStruct();
	
	/**
	 * ȡ������ʽ
	 * @return ���ʽ����
	 */
	abstract public Expression[] getExps();

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	abstract public String[] getNames();

	/**
	 * ȡ���ܱ��ʽ
	 * @return ���ʽ����
	 */
	abstract public Expression[] getCalcExps();

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	abstract public String[] getCalcNames();

	/**
	 * ȡѡ��
	 * @return
	 */
	abstract public String getOption();
	
	/**
	 * ȡ�Ƿ����������
	 * @return true���ǣ����ݰ������ֶ�����false������
	 */
	abstract public boolean isSortedGroup();
	
	/**
	 * ��������ʱ��ȡ��ÿ���̵߳��м������������Ҫ���ж��λ���
	 * @return Table
	 */
	abstract public Table getTempResult();

	/**
	 * ȡ������ܽ��
	 * @return Table
	 */
	abstract public Table getResultTable();
	
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return
	  */
	public Object result() {
		return getResultTable();
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	abstract public void push(Sequence table, Context ctx);

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param cursor �α�����
	 */
	abstract public void push(ICursor cursor);

	/**
	 * ���÷�������@nѡ��ʹ��
	 * @param groupCount
	 */
	abstract public void setGroupCount(int groupCount);
	
	/**
	 * ��·����ʱ�԰�����·���������ϲ����ж��η�����ܣ��õ����յĻ��ܽ��
	 * @param results ����·�ķ��������ɵ�����
	 * @return ���յĻ��ܽ��
	 */
	abstract public Object combineResult(Object []results);
}
