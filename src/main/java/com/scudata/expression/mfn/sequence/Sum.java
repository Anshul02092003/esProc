package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ������Ԫ�����
 * A.sum(), A.sum(x)
 * @author RunQian
 *
 */
public class Sum extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			return srcSequence.sum();
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			return srcSequence.calc(exp, ctx).sum();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sum" + mm.getMessage("function.invalidParam"));
		}
	}
}
