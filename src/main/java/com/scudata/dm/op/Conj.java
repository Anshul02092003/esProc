package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �������α��ܵ��ϵ����к�������
 * @author WangXiaoJun
 *
 */
public class Conj extends Operation {
	private Expression newExp;

	/**
	 * ����ȡ��Ա������
	 * @param newExp ������ʽ
	 */
	public Conj(Expression newExp) {
		this(null, newExp);
	}
	
	/**
	 * ����ȡ��Ա������
	 * @param function ��ǰ������Ӧ�ı��ʽ��ĺ���
	 * @param newExp ������ʽ
	 */
	public Conj(Function function, Expression newExp) {
		super(function);
		this.newExp = newExp;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(newExp, ctx);
		return new Conj(function, dupExp);
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		return seq.conj(newExp, ctx);
	}
}
