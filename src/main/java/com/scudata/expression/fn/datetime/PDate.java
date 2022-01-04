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
 * pdate(dateExp) ���ָ������dateExp��������/��/���ȵ������һ�������һ��
 * @author runqian
 *
 */
public class PDate extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pdate" + mm.getMessage("function.invalidParam"));
		}

		Object result = param.getLeafExpression().calculate(ctx);
		if (result == null) {
			return null;
		}

		if (result instanceof String) {
			result = Variant.parseDate((String)result);
		}

		if (!(result instanceof Date)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pdate" + mm.getMessage("function.paramTypeError"));
		}

		Date date = (Date)result;
		if (option != null) {
			if (option.indexOf('w') != -1) {
				if (option.indexOf('e') == -1) {
					return DateFactory.get().weekBegin(date);
				} else {
					return DateFactory.get().weekEnd(date);
				}
			} else if (option.indexOf('m') != -1) {
				if (option.indexOf('e') == -1) {
					return DateFactory.get().monthBegin(date);
				} else {
					return DateFactory.get().monthEnd(date);
				}
			} else if (option.indexOf('q') != -1) {
				if (option.indexOf('e') == -1) {
					return DateFactory.get().quaterBegin(date);
				} else {
					return DateFactory.get().quaterEnd(date);
				}
			} else if (option.indexOf('y') != -1) {
				if (option.indexOf('e') == -1) {
					return DateFactory.get().yearBegin(date);
				} else {
					return DateFactory.get().yearEnd(date);
				}
			}
		}

		return DateFactory.get().weekBegin(date);
	}
}
