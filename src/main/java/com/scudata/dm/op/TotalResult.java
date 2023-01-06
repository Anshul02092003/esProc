package com.scudata.dm.op;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;

/**
 * ���ڶ�������������ִ�л�������
 * @author RunQian
 *
 */
public class TotalResult implements IResult {
	private Expression []calcExps; // ���ܱ��ʽ����
	private Context ctx; // ����������
	private IGroupsResult groupsResult;
	
	public TotalResult(Expression[] calcExps, Context ctx, IGroupsResult groupsResult) {
		this.calcExps = calcExps;
		this.ctx = ctx;	
		this.groupsResult = groupsResult;
	}
	
	public Expression[] getCalcExps() {
		return calcExps;
	}
	
	/**
	 * �������ͽ���ʱ����
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
	}
		
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return
	  */
	public Object result() {
		Table table = groupsResult.getResultTable();
		if (table == null || table.length() == 0) {
			return null;
		} else {
			BaseRecord r = table.getRecord(1);
			int count = calcExps.length;
			if (count == 1) {
				return r.getNormalFieldValue(0);
			} else {
				Sequence seq = new Sequence(count);
				for (int i = 0; i < count; ++i) {
					seq.add(r.getNormalFieldValue(i));
				}
				
				return seq;
			}
		}
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		if (table == null || table.length() == 0) return;
		groupsResult.push(table, ctx);
	}

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param cursor �α�����
	 */
	public void push(ICursor cursor) {
		groupsResult.push(cursor);
	}
		
	public Object combineResult(Object []results) {
		Expression []calcExps = this.calcExps;
		Node []gathers = Sequence.prepareGatherMethods(calcExps, ctx);
		int valCount = gathers.length;
		String []names = new String[valCount];
		Table table = new Table(names);
		
		for (int i = 0, count = results.length; i < count; ++i) {
			if (valCount == 1) {
				BaseRecord r = table.newLast();
				r.setNormalFieldValue(0, results[i]);
			} else if (results[i] != null) {
				Sequence seq = (Sequence)results[i];
				table.newLast(seq.toArray());
			}
		}
		
		if (table.length() == 0) {
			return null;
		}
		
		Expression []calcExps2 = new Expression[valCount];
		for (int i = 0, q = 1; i < valCount; ++i, ++q) {
			Node gather = calcExps[i].getHome();
			gather.prepare(ctx);
			calcExps2[i] = gather.getRegatherExpression(q);
		}

		table = table.groups(null, null, calcExps2, null, null, ctx);
		BaseRecord r = table.getRecord(1);
		if (valCount == 1) {
			return r.getNormalFieldValue(0);
		} else {
			return new Sequence(r.getFieldValues());
		}
	}
}
