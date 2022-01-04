package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * ����������ϣ����������ʵ�൱�ڽⷽ��AX=Y������AΪϵ������YΪ��������
 * ��AΪm*m����������ʱ��������Ψһ�⣻��AΪm*n����m>n��������ʱ������С���˷���������Ͻ�
 * @author bd
 */
public class Linefit extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("linefit" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("linefit" + mm.getMessage("function.invalidParam"));
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("linefit" + mm.getMessage("function.invalidParam"));
			}

			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("linefit" + mm.getMessage("function.invalidParam"));
			}
			Object o1 = sub1.getLeafExpression().calculate(ctx);
			Object o2 = sub2.getLeafExpression().calculate(ctx);
			if (o1 instanceof Sequence && o2 instanceof Sequence) {
				Matrix A = new Matrix((Sequence)o1);
				Matrix B = new Matrix((Sequence)o2);
				boolean oneline = B.getRows() == 1;
				if (oneline) {
					B = B.transpose();
				}
				
				if (A.getCols() == 0 || A.getRows() == 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("linefit" + mm.getMessage("function.paramTypeError"));
				}
				else if (B.getCols() == 0 || B.getRows() == 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("linefit" + mm.getMessage("function.paramTypeError"));
				}
				Matrix X = A.solve(B);
				if (X == null) {
					return null;
				}
				if (oneline) {
					double[][] vs = X.getArray();
					int rows = vs.length;
					if (rows > 0) {
						int cols = vs[0].length;
						if (cols == 1) {
							Sequence result = new Sequence(rows);
							for (int i = 0; i < rows; i++) {
								result.add(vs[i][0]);
							}
							return result;
						}
						else if (rows == 1) {
							Sequence result = new Sequence(cols);
							for (int i = 0; i < cols; i++) {
								result.add(vs[0][i]);
							}
							return result;
						}
					}
				}
				return X.toSequence(option, false);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("linefit" + mm.getMessage("function.paramTypeError"));
			}
		}
	}
}
