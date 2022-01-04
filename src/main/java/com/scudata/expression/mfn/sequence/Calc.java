package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ������е�ĳ����ĳЩԪ�ؼ�����ʽ
 *  A.calc(k,x) A.calc(p,x)
 * @author RunQian
 *
 */
public class Calc extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("calc" + mm.getMessage("function.missingParam"));
		}

		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("calc" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("calc" + mm.getMessage("function.invalidParam"));
		}

		Object param1 = sub1.getLeafExpression().calculate(ctx);
		Expression exp2 = sub2.getLeafExpression();

		if (param1 instanceof Number) {
			return srcSequence.calc(((Number)param1).intValue(), exp2, ctx);
		} else if (param1 instanceof Sequence) {
			return srcSequence.calc((Sequence)param1, exp2, ctx);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("calc" + mm.getMessage("function.paramTypeError"));
		}
	}
}
