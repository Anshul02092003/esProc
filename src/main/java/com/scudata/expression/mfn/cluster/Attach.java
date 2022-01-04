package com.scudata.expression.mfn.cluster;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.ClusterTableMetaDataFunction;
import com.scudata.resources.EngineMessage;

/**
 * ȡ��Ⱥ���ĸ������Ϊ��Ⱥ������Ӹ���
 * T.attach(T��,C��)
 * @author RunQian
 *
 */
public class Attach extends ClusterTableMetaDataFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("attach" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			String tableName = param.getLeafExpression().getIdentifierName();
			return table.getTableMetaData(tableName);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("attach" + mm.getMessage("function.invalidParam"));
		}
	}
}
