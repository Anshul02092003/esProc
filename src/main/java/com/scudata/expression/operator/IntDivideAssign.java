package com.scudata.expression.operator;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ������ֵ�������\=
 * @author RunQian
 *
 */
public class IntDivideAssign extends Operator {
	public IntDivideAssign() {
		priority = PRI_EVL;
	}

	public Object calculate(Context ctx) {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"\\=\"" + mm.getMessage("operator.missingLeftOperation"));
		}

		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"\\=\"" + mm.getMessage("operator.missingRightOperation"));
		}

		Object o1 = left.calculate(ctx);
		Object o2 = right.calculate(ctx);
		Object result = null;
		if (o1 == null) {
			if (o2 != null && !(o2 instanceof Sequence) && !(o2 instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\"\\\"" + mm.getMessage("function.paramTypeError"));
			}
		} else if (o1 instanceof Sequence) {
			if (o2 == null) {
				result = o1;
			} else {
				if (o2 instanceof Sequence) {
					result = ((Sequence)o1).diff((Sequence)o2, false);
				} else {
					Sequence s2 = new Sequence(1);
					s2.add(o2);
					result = ((Sequence)o1).diff(s2, false);
				}
			}
		} else {
			result = Variant.intDivide(o1, o2);
		}
		
		return left.assign(result, ctx);
	}
}
