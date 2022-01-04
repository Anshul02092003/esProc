package com.scudata.expression.mfn.vdb;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.VSFunction;
import com.scudata.resources.EngineMessage;

/**
 * �鵵ָ��·�����鵵��·��������д��ռ�õĿռ���С����ѯ�ٶȻ���
 * v.archive(p)
 * @author RunQian
 *
 */
public class Archive extends VSFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("archive" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object path = param.getLeafExpression().calculate(ctx);
			return vs.archive(path);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("archive" + mm.getMessage("function.invalidParam"));
		}
	}
}