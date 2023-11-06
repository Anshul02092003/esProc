package com.scudata.expression.mfn.op;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.op.Conj;
import com.scudata.expression.Expression;
import com.scudata.expression.OperableFunction;
import com.scudata.expression.ParamParser;
import com.scudata.resources.EngineMessage;

// ��Ա�ĺ���
/**
 * ���α��ܵ��������к�������
 * op.conj() op.conj(x)��op�����е��α��ܵ�
 * @author RunQian
 *
 */
public class AttachConj extends OperableFunction {
	/**
	 * ���ú�������
	 * @param cs �������
	 * @param ctx ����������
	 * @param param ���������ַ���
	 */
	public void setParameter(ICellSet cs, Context ctx, String param) {
		strParam = param;
		this.cs = cs;
		
		// A.conj(x,��)�Ѳ�������һ�����崴���ɶ��ű��ʽ
		this.param = ParamParser.newLeafParam(param, cs, ctx);
		if (next != null) {
			next.setParameter(cs, ctx, param);
		}
	}
	
	public Object calculate(Context ctx) {
		if (param == null) {
			Conj op = new Conj(this, null);
			if (cs != null) {
				op.setCurrentCell(cs.getCurrent());
			}
			
			return operable.addOperation(op, ctx);
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			Conj op = new Conj(this, exp);
			if (cs != null) {
				op.setCurrentCell(cs.getCurrent());
			}
			
			return operable.addOperation(op, ctx);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("conj" + mm.getMessage("function.invalidParam"));
		}
	}
}
