package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * �ж������Ƿ���ָ�����л�Ϊ�û���
 * A.eq(B)
 * @author RunQian
 *
 */
public class Eq extends SequenceFunction {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("eq" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("eq" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof Sequence) {
			boolean b = srcSequence.isPeq((Sequence)obj);
			return Boolean.valueOf(b);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("eq" + mm.getMessage("function.paramTypeError"));
		}
	}
}
