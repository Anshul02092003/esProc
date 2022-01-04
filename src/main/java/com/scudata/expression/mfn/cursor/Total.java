package com.scudata.expression.mfn.cursor;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.CursorFunction;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ����α����������ܣ�����ֵ����
 * cs.total(y,��) ֻ��һ��yʱ���ص�ֵ
 * @author RunQian
 *
 */
public class Total extends CursorFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("total" + mm.getMessage("function.missingParam"));
		}
		
		Expression []exps = param.toArray("total", false);
		return cursor.total(exps, ctx);
	}
}
