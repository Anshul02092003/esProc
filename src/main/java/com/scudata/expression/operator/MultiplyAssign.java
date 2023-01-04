package com.scudata.expression.operator;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �������*=
 * �˵���
 * @author RunQian
 *
 */
public class MultiplyAssign extends Operator {
	public MultiplyAssign() {
		priority = PRI_EVL;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"*=\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"*=\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		Object o1 = left.calculate(ctx);
		Object o2 = right.calculate(ctx);
		return left.assign(Variant.multiply(o1, o2), ctx);
	}

	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	public boolean canCalculateAll() {
		return false;
	}
}
