package com.scudata.expression.fn.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * remainder(a,b) ȡ�࣬a,b����ֵ
 * b>0ʱ����[0,b)֮�����xʹ��(a-x)/b������
 * b<0ʱ������[b,-b)֮�����xʹ��(a-x)/2/b������
 * @author RunQian
 *
 */
public class Remainder extends Function {
	private Expression exp1;
	private Expression exp2;

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("remainder" + mm.getMessage("function.missingParam"));
		} else if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("remainder" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("remainder" + mm.getMessage("function.invalidParam"));
		}

		exp1 = sub1.getLeafExpression();
		exp2 = sub2.getLeafExpression();
	}

	public Object calculate(Context ctx) {
		Object o1 = exp1.calculate(ctx);
		Object o2 = exp2.calculate(ctx);
		return Variant.remainder(o1, o2);
	}
}
