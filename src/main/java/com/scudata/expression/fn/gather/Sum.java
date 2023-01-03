package com.scudata.expression.fn.gather;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.LongArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���
 * sum(n1,��)
 * @author RunQian
 *
 */
public class Sum extends Gather {
	private Expression exp;

	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sum" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
	}
	
	public Object gather(Context ctx) {
		return exp.calculate(ctx);
	}

	public Object gather(Object oldValue, Context ctx) {
		return Variant.add(exp.calculate(ctx), oldValue);
	}

	public Expression getRegatherExpression(int q) {
		String str = "sum(#" + q + ")";
		return new Expression(str);
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sum" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).sum();
			} else {
				return obj;
			}
		}

		IParam sub = param.getSub(0);
		Object result = sub == null ? null : sub.getLeafExpression().calculate(ctx);

		for (int i = 1, size = param.getSubSize(); i < size; ++i) {
			sub = param.getSub(i);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				result = Variant.add(result, obj);
			}
		}

		return result;
	}
	
	// ������seq��һ�»���ֵ
	public Object gather(Sequence seq) {
		return seq.sum();
	}
	
	public Expression getExp() {
		return exp;
	}
	
	/**
	 * �������м�¼��ֵ�����ܵ����������
	 * @param result �������
	 * @param resultSeqs ÿ����¼��Ӧ�Ľ����������
	 * @param ctx ����������
	 * @return IArray �������
	 */
	public IArray gather(IArray result, int []resultSeqs, Context ctx) {
		IArray array = exp.calculateAll(ctx);
		if (result == null) {
			if (array instanceof IntArray) {
				result = new LongArray(Env.INITGROUPSIZE);
			} else {
				result = array.newInstance(Env.INITGROUPSIZE);
				if (result instanceof IntArray) {
					result = new LongArray(Env.INITGROUPSIZE);
				}
			}
		}
		
		int resultSize = result.size();
		for (int i = 1, len = array.size(); i <= len; ++i) {
			if (resultSize < resultSeqs[i]) {
				result.add(array, i);
				resultSize++;
			} else {
				result = result.memberAdd(resultSeqs[i], array, i);
			}
		}
		
		return result;
	}
	
	/**
	 * ��̷̳���Ķ��λ�������
	 * @param result һ���̵߳ķ�����
	 * @param result2 ��һ���̵߳ķ�����
	 * @param seqs ��һ���̵߳ķ������һ���̷߳���Ķ�Ӧ��ϵ
	 * @param ctx ����������
	 * @return
	 */
	public void gather2(IArray result, IArray result2, int []seqs, Context ctx) {
		for (int i = 1, len = result2.size(); i <= len; ++i) {
			if (seqs[i] != 0) {
				result.memberAdd(seqs[i], result2, i);
			}
		}
	}
}
