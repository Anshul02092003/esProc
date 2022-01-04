package com.scudata.expression.mfn.record;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.expression.IParam;
import com.scudata.expression.RecordFunction;
import com.scudata.resources.EngineMessage;

/**
 * �Լ�¼��������ݹ��ѯ
 * r.prior(F,r',n)
 * @author RunQian
 *
 */
public class Prior extends RecordFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("prior" + mm.getMessage("function.missingParam"));
		}
		
		String field;
		Record parent = null;
		int maxLevel = 1000;
		
		if (param.isLeaf()) {
			field = param.getLeafExpression().getIdentifierName();
		} else {
			int size = param.getSubSize();
			if (size > 3) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("prior" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("prior" + mm.getMessage("function.invalidParam"));
			}
			
			field = sub.getLeafExpression().getIdentifierName();
			sub = param.getSub(1);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Record) {
					parent = (Record)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prior" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			if (size > 2) {
				sub = param.getSub(2);
				if (sub != null) {
					Object obj = sub.getLeafExpression().calculate(ctx);
					if (obj instanceof Number) {
						maxLevel = ((Number)obj).intValue();
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("prior" + mm.getMessage("function.paramTypeError"));
					}
				}
			}
		}

		return srcRecord.prior(field, parent, maxLevel);
	}
}