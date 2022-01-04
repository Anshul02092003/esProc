package com.scudata.expression.fn.gather;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
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
}
