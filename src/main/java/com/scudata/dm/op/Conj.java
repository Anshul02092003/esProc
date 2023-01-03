package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
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
		Expression exp = this.newExp;
		int len = seq.length();
		Sequence result = null;

		if (exp != null) {
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(seq);
			stack.push(current);

			try {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					Object obj = exp.calculate(ctx);
					if (obj instanceof Sequence) {
						if (result == null) {
							result = (Sequence)obj;
						} else {
							result = result.append((Sequence)obj);
						}
					} else if (obj != null) {
						if (result == null) {
							result = new Sequence();
						}
						
						result.add(obj);
					}
				}
			} finally {
				stack.pop();
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				Object obj = seq.getMem(i);
				if (obj instanceof Sequence) {
					if (result == null) {
						result = (Sequence)obj;
					} else {
						result = result.append((Sequence)obj);
					}
				} else if (obj != null) {
					if (result == null) {
						result = new Sequence();
					}
					
					result.add(obj);
				}
			}
		}

		return result;
	}
}
