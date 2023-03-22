package com.scudata.expression.operator;

import com.scudata.array.ConstArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;

/**
 * �������//
 * ���г�Ա���
 * @author RunQian
 *
 */
public class MemDivide extends Operator {
	public MemDivide() {
		priority = PRI_DIV;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"//\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"//\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		Object obj = left.calculate(ctx);
		if (!(obj instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"//\"" + mm.getMessage("function.paramTypeError"));
		}

		Sequence seq = (Sequence)obj;
		obj = right.calculate(ctx);

		if (obj instanceof Sequence) {
			return seq.memberDivide((Sequence)obj);
		} else {
			ConstArray array = new ConstArray(obj, seq.length());
			return seq.memberDivide(new Sequence(array));
		}
	}
}
