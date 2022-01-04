package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;


/**
 * ����������м����ֵ
 * mmean(A)��AΪ����ʱ���Ϊ��Ա�ܺͣ�AΪ������߶�ά����ʱ�������������еĳ�Ա��ֵ���������л��߶�ά����
 * mmean(A, n)�������n�㣬n֧�����оۺ϶��
 * @author bd
 *
 */
public class MMean extends Function {
	public Object calculate (Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("mmean" + mm.getMessage("function.missingParam"));
		} else {
			Object oa = null;
			Object o2 = null;
			if (param.isLeaf()) {
				// ֻ��һ��������msum(A), �൱��msum(A, 1)
				oa = param.getLeafExpression().calculate(ctx);
			}
			else if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("mmean" + mm.getMessage("function.invalidParam"));
			}
			else {
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null || sub2 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("mmean" + mm.getMessage("function.invalidParam"));
				}
				oa = sub1.getLeafExpression().calculate(ctx);
				o2 = sub2.getLeafExpression().calculate(ctx);
			}
			boolean ifNull = option != null && option.contains("n");
			if (oa instanceof Sequence) {
				MulMatrix A = new MulMatrix((Sequence)oa);
				if (option != null && option.contains("a")) {
					// ȫ�ۺϣ���2��ά�Ȳ�����Ч
					double sum = A.sumAll();
					double d = A.countAll();
					return sum/d;
				}
				if (o2 instanceof Sequence) {
					// ��2������Ϊ����ʱ����ҪΪ���У��������ظ������Լ�С��0���ߴ��ڶ�ά���в�������Ч��Ҳ������
					Sequence nseq = ((Sequence) o2).id(null);
					int len = ((Sequence) o2).length();
					if (len != nseq.length()) {
						throw new RQException("mmean: Demension vector members must be unique.");
					}
					Object result = mean(A, nseq, ifNull);
					if (result instanceof MulMatrix) {
						return ((MulMatrix) result).toSequence();
					}
					return result;
				}
				else if (o2 != null && !(o2 instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("mmean" + mm.getMessage("function.paramTypeError"));
				}
				else {
					int b = 0;
					if (o2 instanceof Number) {
						b = ((Number) o2).intValue();
					}
					if (b == 0) {
						b = A.autoLevel();
					}
					Object result = mean(A, b, ifNull);
					if (result instanceof MulMatrix) {
						result = ((MulMatrix) result).toSequence();
					}
					return result;
				}
			}
			MessageManager mm = EngineMessage.get();
			throw new RQException("mmean" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	protected static Object mean(MulMatrix A, int level, boolean ifNull) {
		Object res =  A.sum(level, ifNull);
		double d = A.count(level);
		if ( res instanceof Number) {
			return ((Number) res).doubleValue()/d;
		}
		else {
			MulMatrix mm = (MulMatrix) res;
			mm.divide(d);
			return mm;
		}
	}
	
	protected static Object mean(MulMatrix A, Sequence nseq, boolean ifNull) {
		int len = nseq.length();
		double d = 1;
		for(int i = 1; i <= len; i++) {
			Object o = nseq.get(i);
			if (o instanceof Number) {
				int level = ((Number) o).intValue() - i + 1;
				d = d * A.count(level);
				o = A.sum(level, ifNull);
				if (o instanceof MulMatrix) {
					A = (MulMatrix) o;
				}
				else {
					// �ۺϽ��Ϊ�����ˣ��޷��پۺ��ˣ�����
					double sum = ((Number) o).doubleValue();
					return sum/d;
				}
			}
		}
		A.divide(d);
		return A;
	}
}
