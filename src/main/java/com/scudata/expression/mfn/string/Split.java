package com.scudata.expression.mfn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.StringFunction;
import com.scudata.resources.EngineMessage;

/**
 * ���ַ�����ָ���ָ���������У�û��ָ���ָ������ɵ��ַ���ɵ�����
 * s.split(d)
 * @author RunQian
 *
 */
public class Split extends StringFunction {
	public Object calculate(Context ctx) {
		String sep = "";
		if (param != null) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("split" + mm.getMessage("function.paramTypeError"));
			}

			sep = (String)obj;
		}
		
		return Sequence.toSequence(srcStr, sep, option);
	}
}