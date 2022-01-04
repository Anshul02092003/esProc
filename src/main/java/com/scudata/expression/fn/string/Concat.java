package com.scudata.expression.fn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * concat(xi,��) ���������ӳ�Ϊ�ַ������Ҵ�ƴ��ʱ�������š�
 * @author runqian
 *
 */
public class Concat extends Function {
	private static void concat(Object obj, StringBuffer out) {
		if (obj instanceof Sequence) {
			Sequence seq = (Sequence)obj;
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				concat(seq.get(i), out);
			}
		} else if (obj != null) {
			out.append(obj.toString());
		}
	}
	
	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("concat" + mm.getMessage("function.missingParam"));
		}

		StringBuffer sb = new StringBuffer();
		if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			concat(obj, sb);
		} else {
			for (int i = 0, size = param.getSubSize(); i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("concat" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				concat(obj, sb);
			}
		}

		return sb.toString();
	}
}
