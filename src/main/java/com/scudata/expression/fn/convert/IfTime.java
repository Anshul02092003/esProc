package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * iftime(exp) �ж�exp�Ƿ�Ϊʱ������
 * @author runqian
 *
 */
public class IfTime extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iftime" + mm.getMessage("function.invalidParam"));
		}

		Object result1 = param.getLeafExpression().calculate(ctx);
		if (result1 instanceof java.sql.Time) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
