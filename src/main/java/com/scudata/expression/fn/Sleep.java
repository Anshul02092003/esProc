package com.scudata.expression.fn;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * sleep(n) ������������˯��״̬n����
 * @author runqian
 *
 */
public class Sleep extends Function {
	public Node optimize(Context ctx) {
		param.optimize(ctx);
		return this;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sleep" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		if (param.isLeaf()) {
			Object o = param.getLeafExpression().calculate(ctx);
			if (o instanceof Number) {
				try {
					Thread.sleep(((Number)o).longValue());
					return o;
				} catch (InterruptedException e) {
					throw new RQException(e.getMessage(), e);
				}
			} else if (o == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sleep" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sleep" + mm.getMessage("function.invalidParam"));
		}
	}
}
