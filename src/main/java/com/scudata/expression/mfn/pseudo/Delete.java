package com.scudata.expression.mfn.pseudo;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.PseudoFunction;
import com.scudata.resources.EngineMessage;

/**
 * ��ָ�����ݴ������ɾ���������������
 * T.delete(P)
 * @author RunQian
 *
 */
public class Delete extends PseudoFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("delete" + mm.getMessage("function.missingParam"));
		}
		
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof Sequence) {
			return pseudo.delete((Sequence)obj, option);
		} else if (obj != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("delete" + mm.getMessage("function.paramTypeError"));
		} else {
			return null;
		}
	}
}
