package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �α��ܵ����ӵļ�����ʽ���㴦����
 * op.run(xi,��) op.run(xi:Fi:,��) op���α��ܵ�
 * @author RunQian
 *
 */
public class Run extends Operation {
	private Expression exp; // ���ʽ�����Ϊ�����������xi:Fi��ʽ�Ĳ���
	
	private Expression []assignExps; // �ֶ������ʽ����
	private Expression []exps; // ֵ���ʽ����

	public Run(Expression exp) {
		this(null, exp);
	}
	
	public Run(Function function, Expression exp) {
		super(function);
		this.exp = exp;
	}
	
	public Run(Expression []assignExps, Expression []exps) {
		this(null, assignExps, exps);
	}

	public Run(Function function, Expression []assignExps, Expression []exps) {
		super(function);
		this.assignExps = assignExps;
		this.exps = exps;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		if (exp != null) {
			Expression dupExp = dupExpression(exp, ctx);
			return new Run(function, dupExp);
		} else {
			Expression []assignExps1 = dupExpressions(assignExps, ctx);
			Expression []exps1 = dupExpressions(exps, ctx);
			return new Run(function, assignExps1, exps1);
		}
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (exp != null) {
			seq.run(exp, ctx);
		} else {
			seq.run(assignExps, exps, ctx);
		}

		return seq;
	}
}
