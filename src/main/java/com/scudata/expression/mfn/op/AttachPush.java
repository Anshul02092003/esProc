package com.scudata.expression.mfn.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.op.Channel;
import com.scudata.dm.op.Push;
import com.scudata.expression.IParam;
import com.scudata.expression.OperableFunction;
import com.scudata.resources.EngineMessage;

/**
 * ���α��ܵ������������ݵ��ܵ�������
 * op.push(chi��) op���α��ܵ�
 * @author RunQian
 *
 */
public class AttachPush extends OperableFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("push" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof Channel)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("push" + mm.getMessage("function.paramTypeError"));
			}
			
			Push push = new Push(this, (Channel)obj);
			return operable.addOperation(push, ctx);
		} else {
			for (int i = 0, size = param.getSubSize(); i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("push" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Channel)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("push" + mm.getMessage("function.paramTypeError"));
				}
				
				Push push = new Push(this, (Channel)obj);
				operable.addOperation(push, ctx);
			}
			
			return operable;
		}
	}
}
