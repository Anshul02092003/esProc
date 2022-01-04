package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * �Էָ������������еĳ�Ա��Ϊ�ַ���������������
 * A.concat(d)
 * @author RunQian
 *
 */
public class Concat extends SequenceFunction {
	public Object calculate(Context ctx) {
		String sep = "";
		if (param != null) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("concat" + mm.getMessage("function.paramTypeError"));
			}

			sep = (String)obj;
		}

		return srcSequence.toString(sep, option);
	}
}
