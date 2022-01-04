package com.scudata.expression.mfn.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.op.Conj;
import com.scudata.expression.Expression;
import com.scudata.expression.OperableFunction;
import com.scudata.resources.EngineMessage;

// ��Ա�ĺ���
/**
 * ���α��ܵ��������к�������
 * op.conj() op.conj(x)��op�����е��α��ܵ�
 * @author RunQian
 *
 */
public class AttachConj extends OperableFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			Conj op = new Conj(this, null);
			return operable.addOperation(op, ctx);
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			Conj op = new Conj(this, exp);
			return operable.addOperation(op, ctx);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("conj" + mm.getMessage("function.invalidParam"));
		}
	}
}
