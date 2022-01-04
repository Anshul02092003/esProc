package com.scudata.expression.fn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * left(string,n) ���Դ�ַ���string��ߵ��Ӵ����䳤��Ϊn����n<0ʱ��n����ֵΪstring���ĳ��ȼ�nֵ��
 * @author runqian
 *
 */
public class Left extends Function {

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("left" + mm.getMessage("function.missingParam"));
		}

		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("left" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("left" + mm.getMessage("function.invalidParam"));
		}

		Object result1 = sub1.getLeafExpression().calculate(ctx);
		if (result1 == null) {
			return null;
		} else if (!(result1 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("left" + mm.getMessage("function.paramTypeError"));
		}

		Object result2 = sub2.getLeafExpression().calculate(ctx);
		if (!(result2 instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("left" + mm.getMessage("function.paramTypeError"));
		}
		
		String str = (String) result1;
		int n = ((Number)result2).intValue();
		if (n < 0) {
			n += str.length();
			if (n <= 0) {
				return "";
				//MessageManager mm = EngineMessage.get();
				//throw new RQException("left" + mm.getMessage("function.valueNoSmall"));
			} else {
				return str.substring(0, n);
			}
		} else if (n >= str.length()) {
			return result1;
		} else {
			return str.substring(0, n);
		}
	}
}
