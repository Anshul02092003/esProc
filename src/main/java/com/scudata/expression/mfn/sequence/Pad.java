package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ��ָ��ֵ�������е�ָ������
 * A.pad(x,n)
 * @author RunQian
 *
 */
public class Pad extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null || param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pad" + mm.getMessage("function.missingParam"));
		}
		
		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pad" + mm.getMessage("function.invalidParam"));
		}

		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		if (sub0 == null || sub1 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pad" + mm.getMessage("function.invalidParam"));
		}

		Object val = sub0.getLeafExpression().calculate(ctx);
		Object obj = sub1.getLeafExpression().calculate(ctx);
		if (!(obj instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pad" + mm.getMessage("function.paramTypeError"));
		}

		return srcSequence.pad(val, ((Number)obj).intValue(), option);
	}
}
