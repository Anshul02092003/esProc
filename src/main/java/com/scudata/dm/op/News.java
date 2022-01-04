package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �α��ܵ��ĸ��ӵĺϲ��������������
 * cs.news(...)
 * @author RunQian
 *
 */
public class News extends Operation  {
	private Expression gexp; // ����ֵΪ���еı��ʽ
	private Expression[] newExps; // �ֶα��ʽ����
	private String []names; // �ֶ�������
	private String opt; // ѡ��
	private DataStruct newDs; // �ṹ�����ݽṹ
	
	public News(Expression gexp, Expression []newExps, String []names, String opt) {
		this(null, gexp, newExps, names, opt);
	}
	
	public News(Function function, Expression gexp, Expression []newExps, String []names, String opt) {
		super(function);

		this.gexp = gexp;
		this.newExps = newExps;
		this.names = names;
		this.opt = opt;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(gexp, ctx);
		Expression []dupExps = dupExpressions(newExps, ctx);
		return new News(function, dupExp, dupExps, names, opt);
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		Table result;
		if (newDs != null) {
			result = seq.newTables(gexp, newExps, newDs, opt, ctx);
		} else {
			result = seq.newTables(gexp, names, newExps, opt, ctx);
			if (result != null) {
				newDs = result.dataStruct();
			}
		}
		
		if (result.length() != 0) {
			return result;
		} else {
			return null;
		}
	}
}
