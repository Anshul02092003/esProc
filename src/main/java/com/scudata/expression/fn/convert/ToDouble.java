package com.scudata.expression.fn.convert;

import com.scudata.array.DoubleArray;
import com.scudata.array.IArray;
import com.scudata.array.NumberArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ���ַ���������ת����64λ��˫���ȸ�����
 * @author runqian
 *
 */
public class ToDouble extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("float" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("float" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof Double) {
			return result;
		} else if (result instanceof Number) {
			return ((Number)result).doubleValue();
		} else if (result instanceof String) {
			try {
				return new Double((String)result);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (result == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("float" + mm.getMessage("function.paramTypeError"));
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		if (array instanceof DoubleArray) {
			return array;
		}

		int len = array.size();
		DoubleArray result = new DoubleArray(len);
		result.setTemporary(true);
		
		if (array instanceof NumberArray) {
			NumberArray numberArray = (NumberArray)array;
			for (int i = 1; i <= len; ++i) {
				if (numberArray.isNull(i)) {
					result.pushNull();
				} else {
					result.pushDouble(numberArray.getDouble(i));
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Number) {
					result.pushDouble(((Number)obj).doubleValue());
				} else if (obj instanceof String) {
					result.pushDouble(Double.parseDouble((String)obj));
				} else if (obj == null) {
					result.pushNull();
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("float" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		return result;
	}
}
