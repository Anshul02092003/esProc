package com.scudata.expression.mfn.db;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.DBFunction;
import com.scudata.resources.EngineMessage;

/**
 * ������Ϊspn�Ļع��㣬���ֲ���ʡ���Ҳ����ظ�
 * db.savepoint(spn)
 * @author RunQian
 *
 */
public class SavePoint extends DBFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("savepoint" + mm.getMessage("function.missingParam"));
		}
		
		Object obj = param.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("savepoint" + mm.getMessage("function.paramTypeError"));
		}
		
		return db.savepoint((String)obj);
	}
}
