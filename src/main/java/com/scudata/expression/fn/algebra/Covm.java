package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * Э���������
 * @author bd
 *
 */
public class Covm extends Function{
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("covm" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object result1 = param.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("covm" + mm.getMessage("function.paramTypeError"));
			}
			// edited by bd, 2021.11.17, ��dis�����У�����������Ϊ�Ǻ�����
			Sequence s1 = (Sequence) result1;
			Matrix A = new Matrix(s1);
			
			Object o11 = s1.length() > 0 ? s1.get(1) : null;
			if (!(o11 instanceof Sequence)) {
				// AΪ�����ж����������ת�ɺ�����
				A = A.transpose();
			}
			if (A.getCols() == 0 || A.getRows() == 0) {
				return new Sequence(0);
			}
			Matrix X = A.covm();
			if (X == null) {
				return null;
			}
			return X.toSequence(option, true);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("covm" + mm.getMessage("function.invalidParam"));
		}
	}
}
