package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * norm(A)��������������Ĺ�һ��normalize����
 * @author bd
 *
 */
public class Normalize extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("norm" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object result1 = param.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("norm" + mm.getMessage("function.paramTypeError"));
			}
			boolean norm = option == null || option.indexOf('0')<0;
			//edited by bd, 2021.12.21, �޸��㷨�����@sѡ��֧�ֱ�׼��һ����ʱ��ֵ0����׼��Ϊ1
			boolean std = option != null && option.indexOf('s')>-1;
			Matrix A = normalize((Sequence) result1, norm, std);
			return A.toSequence(option, true);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("norm" + mm.getMessage("function.invalidParam"));
		}
	}
	
	protected Matrix normalize(Sequence result, boolean norm, boolean std) {
		Matrix A = new Matrix(result);
		// ��������Ϊ���о���
		Object o1 = result.get(1);
		if (! (o1 instanceof Sequence)) {
			A = A.transpose(); 
		}
		double[][] vs = A.getArray();
		int rows = A.getRows();
		int cols = A.getCols();
		for (int r = 0; r < rows; r++) {
			double[] row = vs[r];
			double avg = 0; 
			double sqrsum = 0;
			for (int c = 0; c < cols; c++) {
				avg += row[c];
				if (norm && !std) {
					sqrsum += row[c]*row[c];
				}
			}
			avg = avg/cols;
			if (norm && !std) {
				sqrsum = Math.sqrt(sqrsum);
			}
			else if (std) {
				sqrsum = Var.std(row, true);
			}
			for (int c = 0; c < cols; c++) {
				vs[r][c] = vs[r][c]-avg;
				if (norm && sqrsum!=0) {
					vs[r][c] = vs[r][c]/sqrsum;
				}
			}
		}
		return A;
	}
	
	protected static Vector normalize(Vector A) {
		double[] vs = A.getValue();
		int cols = A.len();
		double avg = 0;
		for (int c = 0; c < cols; c++) {
			avg += vs[c];
		}
		avg = avg/cols;
		for (int c = 0; c < cols; c++) {
			vs[c] = vs[c]-avg;
		}
		return A;
	}

}
