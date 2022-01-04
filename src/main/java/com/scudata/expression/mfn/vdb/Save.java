package com.scudata.expression.mfn.vdb;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.IParam;
import com.scudata.expression.VSFunction;
import com.scudata.resources.EngineMessage;

/**
 * ������д�뵽ָ��·���ı�
 * h.save(x,p,F)
 * @author RunQian
 *
 */
public class Save extends VSFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("save" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object value = param.getLeafExpression().calculate(ctx);
			return vs.save(value);
		}
		
		int size = param.getSubSize();
		if (size > 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("save" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		if (sub1 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("save" + mm.getMessage("function.invalidParam"));
		}
		
		Object path = sub1.getLeafExpression().calculate(ctx);
		Object name = null;
		
		if (size > 2) {
			IParam sub2 = param.getSub(2);
			if (sub2 != null) {
				name = sub2.getLeafExpression().calculate(ctx);
			}
		}
		
		if (sub0 != null) {
			Object value = sub0.getLeafExpression().calculate(ctx);
			return vs.save(value, path, name);
		} else {
			// û�и����������򴴽�Ŀ¼
			return vs.makeDir(path, name);
		}
	}
}
