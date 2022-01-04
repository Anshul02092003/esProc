package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * ������ʽ�����ַ���
 * format (s,��) ���ɴ�������ʾs�Ĳ�����ͨ��Java��ʽ������ʹ�������͵�����ת����һ���ַ�����
 * @author runqian
 *
 */
public class Format extends Function {
	public Object calculate(Context ctx) {
		if (param == null || param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("format"+mm.getMessage("function.missingParam"));
		}
		
		IParam sub0 = param.getSub(0);
		if (sub0 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("format" + mm.getMessage("function.invalidParam"));
		}
		
		Object obj = sub0.getLeafExpression().calculate(ctx);
		String fmt;
		if (obj instanceof String) {
			fmt = (String)obj;
		} else if (obj != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("format" + mm.getMessage("function.paramTypeError"));
		} else {
			return null;
		}
		
		int size = param.getSubSize();
		Object []args = new Object[size - 1];
		for (int i = 1; i < size; ++i) {
			IParam sub = param.getSub(i);
			if (sub != null) {
				args[i - 1] = sub.getLeafExpression().calculate(ctx);
			}
		}
		
		return String.format(fmt, args);
	}
}
