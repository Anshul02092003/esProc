package com.scudata.expression.mfn.table;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Machines;
import com.scudata.expression.TableFunction;
import com.scudata.parallel.Cluster;
import com.scudata.parallel.ClusterMemoryTable;
import com.scudata.resources.EngineMessage;

/**
 * �ѱ����ڱ��Ƴɼ�Ⱥ��д�ڱ�
 * T.dup(h)
 * @author RunQian
 *
 */
public class Dup extends TableFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("dup" + mm.getMessage("function.missingParam"));
		}
		
		Object obj = param.getLeafExpression().calculate(ctx);
		Machines mc = new Machines();
		if (!mc.set(obj)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("hosts" + mm.getMessage("function.invalidParam"));
		}
		
		Cluster cluster = new Cluster(mc.getHosts(), mc.getPorts(), ctx);
		return ClusterMemoryTable.dupLocal(cluster, srcTable);
	}
}
