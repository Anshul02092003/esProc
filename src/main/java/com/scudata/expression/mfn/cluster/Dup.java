package com.scudata.expression.mfn.cluster;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.ClusterMemoryTableFunction;
import com.scudata.resources.EngineMessage;

/**
 * �Ѽ�Ⱥ�ڱ�ƴ�ɱ����ڱ�
 * T.dup()
 * @author RunQian
 *
 */
public class Dup extends ClusterMemoryTableFunction {
	public Object calculate(Context ctx) {
		if (param != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("dup" + mm.getMessage("function.invalidParam"));
		}
		
		return table.dup();
	}
}
