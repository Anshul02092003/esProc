package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �α�͹ܵ��ӳټ��㺯������
 * @author RunQian
 *
 */
public abstract class Operation {
	protected Function function; // ��ǰ������Ӧ�ı��ʽ��ĺ���
	
	/**
	 * ���캯��
	 */
	public Operation() {
	}
	
	/**
	 * ���캯��
	 * @param function ��ǰ������Ӧ�ı��ʽ��ĺ���
	 */
	public Operation(Function function) {
		this.function = function;
	}
	
	/**
	 * ȡ��ǰ������Ӧ�ı��ʽ��ĺ���
	 * @return Function
	 */
	public Function getFunction() {
		return function;
	}
	
	/**
	 * ���õ�ǰ������Ӧ�ı��ʽ��ĺ���
	 * @param function
	 */
	public void setFunction(Function function) {
		this.function = function;
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public abstract Sequence process(Sequence seq, Context ctx);
	
	/**
	 * ȡ�����Ƿ�����Ԫ������������˺�������ټ�¼
	 * �˺��������α�ľ�ȷȡ����������ӵĲ�������ʹ��¼��������ֻ�谴���������ȡ������
	 * @return true���ᣬfalse������
	 */
	public boolean isDecrease() {
		return false;
	}
	
	/**
	 * ����ȫ���������ʱ���ã�group������Ҫ֪�����ݽ�����ȷ�����һ�������
	 * @param ctx ����������
	 * @return ���ӵĲ������������
	 */
	public Sequence finish(Context ctx) {
		return null;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public abstract Operation duplicate(Context ctx);
	
	/**
	 * ���Ʊ��ʽ
	 * @param exp ���ʽ
	 * @param ctx ����������
	 * @return ���ƺ�ı��ʽ
	 */
	public static Expression dupExpression(Expression exp, Context ctx) {
		if (exp != null) {
			return exp.newExpression(ctx);
		} else {
			return null;
		}
	}
	
	/**
	 * ���Ʊ��ʽ����
	 * @param exps ���ʽ����
	 * @param ctx ����������
	 * @return ���ƺ�ı��ʽ����
	 */
	public static Expression[] dupExpressions(Expression []exps, Context ctx) {
		if (exps == null) {
			return null;
		}
		
		int len = exps.length;
		Expression []dupExps = new Expression[len];
		for (int i = 0; i < len; ++i) {
			if (exps[i] != null) dupExps[i] = exps[i].newExpression(ctx);
		}
		
		return dupExps;
	}
	
	/**
	 * ���Ʊ��ʽ���������
	 * @param exps ���ʽ���������
	 * @param ctx ����������
	 * @return ���ƺ�ı��ʽ���������
	 */
	public static Expression[][] dupExpressions(Expression [][]exps, Context ctx) {
		if (exps == null) {
			return null;
		}
		
		int len = exps.length;
		Expression [][]dupExps = new Expression[len][];
		for (int i = 0; i < len; ++i) {
			dupExps[i] = dupExpressions(exps[i], ctx);
		}
		
		return dupExps;
	}
}