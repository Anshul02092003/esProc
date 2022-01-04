package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * isalpha(s) �ж��ַ���s�Ƿ�ȫ����ĸ���ɡ����sΪ����������Ϊascii���ж϶�Ӧ���ַ��Ƿ�Ϊ��ĸ��
 * @author runqian
 *
 */
public class IsAlpha extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("isalpha" + mm.getMessage("function.invalidParam"));
		}

		Object result1 = param.getLeafExpression().calculate(ctx);
		if (result1 instanceof String) {
			String str = (String)result1;
			if (str.length() == 0) return Boolean.FALSE;

			for (int i = 0, len = str.length(); i < len; ++i) {
				char c = str.charAt(i);
				if (c < 'A' || (c > 'Z' && c < 'a') || c > 'z') {
					return Boolean.FALSE;
				}
			}

			return Boolean.TRUE;
		} else if (result1 instanceof Number) {
			int c = ((Number)result1).intValue();
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} else {
			return Boolean.FALSE;
		}
	}
}
