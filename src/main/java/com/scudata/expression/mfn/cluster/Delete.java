package com.scudata.expression.mfn.cluster;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.ClusterTableMetaDataFunction;
import com.scudata.resources.EngineMessage;

/**
 * ��ָ�����ݴӼ�Ⱥ�����ɾ���������������
 * T.delete(P)
 * @author RunQian
 *
 */
public class Delete extends ClusterTableMetaDataFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("delete" + mm.getMessage("function.missingParam"));
		}
		
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof Sequence) {
			return table.delete((Sequence)obj, option);
		} else if (obj != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("delete" + mm.getMessage("function.paramTypeError"));
		} else {
			return null;
		}
	}
}
