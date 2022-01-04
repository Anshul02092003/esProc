package com.scudata.expression.mfn.vdb;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.VSFunction;
import com.scudata.resources.EngineMessage;

/**
 * ���õ�ǰĿ¼��������д����������ڴ�·��
 * h.home(p)
 * @author RunQian
 *
 */
public class Home extends VSFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			return vs;
		} else if (param.isLeaf()) {
			Object path = param.getLeafExpression().calculate(ctx);
			return vs.home(path);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("home" + mm.getMessage("function.invalidParam"));
		}
	}
}
