package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ��������ָ��λ�ã���λ�����У���Ԫ���γ������з���
 * A.swap(p1, p2)
 * @author RunQian
 *
 */
public class Swap extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null || param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("swap" + mm.getMessage("function.missingParam"));
		}
		
		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("swap" + mm.getMessage("function.invalidParam"));
		}

		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		if (sub0 == null || sub1 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("swap" + mm.getMessage("function.invalidParam"));
		}

		Object param1 = sub0.getLeafExpression().calculate(ctx);
		if (!(param1 instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("swap" + mm.getMessage("function.paramTypeError"));
		}

		Object param2 = sub1.getLeafExpression().calculate(ctx);
		if (!(param2 instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("swap" + mm.getMessage("function.paramTypeError"));
		}

		return srcSequence.swap((Sequence)param1, (Sequence)param2);
	}
}
