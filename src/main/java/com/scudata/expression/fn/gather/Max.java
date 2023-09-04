package com.scudata.expression.fn.gather;

import com.scudata.array.IArray;
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
 * �����ֵ
 * max(n1,��)
 * @author RunQian
 *
 */
public class Max extends Gather {
	private Expression exp;

	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("max" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
	}
	
	public Object gather(Context ctx) {
		return exp.calculate(ctx);
	}

	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (Variant.compare(val, oldValue, true) > 0) {
			return val;
		} else {
			return oldValue;
		}
	}

	public Expression getRegatherExpression(int q) {
		String str = "max(#" + q + ")";
		return new Expression(str);
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("max" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).max();
			} else {
				return obj;
			}
		} else {
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("max" + mm.getMessage("function.invalidParam"));
			}
			
			Object maxVal = sub.getLeafExpression().calculate(ctx);
			for (int i = 1, size = param.getSubSize(); i < size; ++i) {
				sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("max" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (Variant.compare(maxVal, obj, true) < 0) {
					maxVal = obj;
				}
			}
	
			return maxVal;
		}
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
			result = array.newInstance(Env.INITGROUPSIZE);
		}
		
		for (int i = 1, len = array.size(); i <= len; ++i) {
			if (result.size() < resultSeqs[i]) {
				result.add(array, i);
			} else if (result.compareTo(resultSeqs[i], array, i) < 0) {
				result.set(resultSeqs[i], array, i);
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
			if (seqs[i] != 0 && result.compareTo(seqs[i], result2, i) < 0) {
				result.set(seqs[i], result2, i);
			}
		}
	}
}
