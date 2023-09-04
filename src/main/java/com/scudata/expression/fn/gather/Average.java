package com.scudata.expression.fn.gather;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.LongArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.AvgValue;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ��ƽ��ֵ
 * avg(n1,��)
 * @author RunQian
 *
 */
public class Average extends Gather {
	private Expression exp;
	private String countFieldName; // ����������ֶ�����
	
	public void setCountFieldName(String countFieldName) {
		this.countFieldName = countFieldName;
	}

	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("avg" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
	}

	public Object gather(Context ctx) {
		Object val = exp.calculate(ctx);
		if (val instanceof AvgValue) {
			return val;
		} else {
			return new AvgValue(val);
		}
	}
	
	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (oldValue == null) {
			if (val instanceof AvgValue) {
				return val;
			} else {
				return new AvgValue(val);
			}
		} else {
			((AvgValue)oldValue).add(val);
			return oldValue;
		}
	}

	public Expression getRegatherExpression(int q) {
		if (countFieldName == null) {
			String str = "avg(#" + q + ")";
			return new Expression(str);
		} else {
			String str = "sum(#" + q + ")";
			return new Expression(str);
		}
	}
	
	public boolean needFinish() {
		return countFieldName == null;
	}

	public Object finish(Object val) {
		if (val instanceof AvgValue) {
			return ((AvgValue)val).getAvgValue();
		} else {
			return val;
		}
	}

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("avg" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj == null) {
				return null;
			} else if (obj instanceof Sequence) {
				return ((Sequence)obj).average();
			} else if (obj instanceof Number) {
				return obj;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("avg" + mm.getMessage("function.paramTypeError"));
			}
		}

		Object result = null;
		int count = 0;
		for (int i = 0, size = param.getSubSize(); i < size; ++i) {
			IParam sub = param.getSub(i);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj != null) {
					if (!(obj instanceof Number)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("avg" + mm.getMessage("function.paramTypeError"));
					}

					count++;
					result = Variant.add(result, obj);
				}
			}
		}

		return Variant.avg(result, count);
	}
	
	public Expression getExp() {
		return exp;
	}
	
	/**
	 * �������м�¼��ֵ�����ܵ����������
	 * @param sumResult ��ͽ������
	 * @param countResult �����������
	 * @param resultSeqs ÿ����¼��Ӧ�Ľ����������
	 * @param ctx ����������
	 * @return IArray �������
	 */
	public IArray gather(IArray sumResult, LongArray countResult, int []resultSeqs, Context ctx) {
		IArray array = exp.calculateAll(ctx);
		if (sumResult == null) {
			if (array instanceof IntArray) {
				sumResult = new LongArray(Env.INITGROUPSIZE);
			} else {
				sumResult = array.newInstance(Env.INITGROUPSIZE);
				if (sumResult instanceof IntArray) {
					sumResult = new LongArray(Env.INITGROUPSIZE);
				}
			}
		}
		
		for (int i = 1, len = array.size(); i <= len; ++i) {
			if (sumResult.size() < resultSeqs[i]) {
				sumResult.add(array, i);
				if (array.isNull(i)) {
					countResult.addLong(0);
				} else {
					countResult.addLong(1);
				}
			} else {
				if (!array.isNull(i)) {
					sumResult = sumResult.memberAdd(resultSeqs[i], array, i);
					countResult.plus1(resultSeqs[i]);
				}
			}
		}
		
		return sumResult;
	}
}
