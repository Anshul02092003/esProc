package com.scudata.expression.operator;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;

/**
 * �������+=
 * @author RunQian
 *
 */
public class AddAssign extends Operator {
	public AddAssign() {
		priority = PRI_EVL;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+=\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+=\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		return left.addAssign(right.calculate(ctx), ctx);
	}
	
	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	public boolean canCalculateAll() {
		return false;
	}
}
