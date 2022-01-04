package com.scudata.expression.fn.datetime;

import java.util.Date;

import com.scudata.common.DateFactory;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * hour(datetimeExp) ��ȡָ��ʱ��datetimeExpλ��һ���е��ĸ�ʱ��
 * @author runqian
 *
 */
public class Hour extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("hour" + mm.getMessage("function.invalidParam"));
		}
		
		Object result = param.getLeafExpression().calculate(ctx);
		if (result == null) {
			return null;
		}
		
		if (result instanceof String) {
			result = Variant.parseDate((String)result);
		}
		
		if (result instanceof Date) {
			return DateFactory.get().hour((Date)result);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("hour" + mm.getMessage("function.paramTypeError"));
		}
	}
}
