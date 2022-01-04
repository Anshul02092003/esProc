package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �α��ܵ����ӳټ��㴦����
 * @author RunQian
 *
 */
public class Calculate extends Operation {
	private Expression exp; // ������ʽ

	/**
	 * ���������
	 * @param exp ������ʽ
	 */
	public Calculate(Expression exp) {
		this(null, exp);
	}
	
	/**
	 * ���������
	 * @param function ��ǰ������Ӧ�ı��ʽ��ĺ���
	 * @param exp ������ʽ
	 */
	public Calculate(Function function, Expression exp) {
		super(function);
		this.exp = exp;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(exp, ctx);
		return new Calculate(function, dupExp);
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		return seq.calc(exp, ctx);
	}
}
