package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ȡָ��ֵ�������е�����
 * A.rank(y) A.rank(y,x)
 * @author RunQian
 *
 */
public class Rank extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rank" + mm.getMessage("function.missingParam"));
		}
		
		Sequence seq = srcSequence;
		Object value;
		if (param.isLeaf()) {
			value = param.getLeafExpression().calculate(ctx);
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("rank" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("rank" + mm.getMessage("function.invalidParam"));
			}
			
			value = sub0.getLeafExpression().calculate(ctx);
			seq = seq.calc(sub1.getLeafExpression(), ctx);
		}
		
		return seq.rank(value, option);
	}
}
