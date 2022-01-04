package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ����ת��
 * @author bd
 *
 */
public class Transpose extends Function{
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("transpose" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object result1 = param.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("transpose" + mm.getMessage("function.paramTypeError"));
			}
			// ����������ת�ã����ı��Ա�������ͣ���ת�ú����һ����������ȱʧֵ��0d
			Sequence seq = (Sequence) result1;
			int rows = seq.length();
			int cols = 0;
			for (int r = 1; r <= rows; r++) {
				Object o = seq.get(r);
				if (o instanceof Sequence) {
					cols = Math.max(cols, ((Sequence) o).length());
				}
			}
			Double zero = Double.valueOf(0);
			if (cols == 0) {
				Sequence result = new Sequence(rows);
				// һλ���У�ֻ��ת����ʽ����
				for (int r = 1; r <= rows; r++ ) {
					Object o = seq.get(r);
					Sequence sub = new Sequence(1);
					sub.add(o);
					result.add(sub);
				}
				return result;
			}
			Sequence result = new Sequence(cols);
			for (int c = 1; c <= cols; c++) {
				Sequence sub = new Sequence(rows);
				for (int r = 1; r <= rows; r++) {
					Object o = seq.get(r);
					if (o instanceof Sequence) {
						Sequence subSeq = (Sequence) o;
						if (subSeq.length() >= c) {
							o = subSeq.get(c);
						}
					}
					else if (c > 1) {
						o = zero;
					}
					sub.add(o);
				}
				result.add(sub);
			}
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("transpose" + mm.getMessage("function.invalidParam"));
		}
	}
}
