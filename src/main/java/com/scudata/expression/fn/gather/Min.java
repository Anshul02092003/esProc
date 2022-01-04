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
 * ����Сֵ
 * min(n1,��)
 * @author RunQian
 *
 */
public class Min extends Gather {
	private Expression exp;

	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("min" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
	}
	
	public Object gather(Context ctx) {
		return exp.calculate(ctx);
	}

	// ������null
	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (val != null && (oldValue == null || Variant.compare(val, oldValue, true) < 0)) {
			return val;
		} else {
			return oldValue;
		}
	}

	public Expression getRegatherExpression(int q) {
		String str = "min(#" + q + ")";
		return new Expression(str);
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("min" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).min();
			} else {
				return obj;
			}
		} else {
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("min" + mm.getMessage("function.invalidParam"));
			}
			
			Object minVal = sub.getLeafExpression().calculate(ctx);	
			for (int i = 1, size = param.getSubSize(); i < size; ++i) {
				sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("min" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj != null && (minVal == null || Variant.compare(minVal, obj, true) > 0)) {
					minVal = obj;
				}
			}
	
			return minVal;
		}
	}

	// ������seq��һ�»���ֵ
	public Object gather(Sequence seq) {
		return seq.min();
	}
}
