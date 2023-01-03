package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ifdate(exp) �ж�����exp�Ƿ�Ϊ�����ͻ�����ʱ������
 * @author runqian
 *
 */
public class IfDate extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ifdate" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ifdate" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof java.sql.Time) {
			return Boolean.FALSE;
		} else if (result instanceof java.util.Date) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
