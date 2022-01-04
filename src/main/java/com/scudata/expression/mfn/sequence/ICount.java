package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ����������ȡֵΪ�棨�ǿղ��Ҳ���false���ķ��ظ�Ԫ������
 * A.icount()
 * @author RunQian
 *
 */
public class ICount extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			return srcSequence.icount(option);
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			return srcSequence.calc(exp, ctx).icount(option);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.invalidParam"));
		}
	}
}
