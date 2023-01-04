package com.scudata.expression.fn.convert;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ����ת��������ȡ��������ֵ���ʽ����ֵ�ַ����е��������֣���������������ת����32λ������
 * @author runqian
 *
 */
public class ToInteger extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("int" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("int" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof Integer) {
			return result;
		} else if (result instanceof Number) {
			return ObjectCache.getInteger(((Number)result).intValue());
		} else if (result instanceof String) {
			try {
				double d = Double.parseDouble((String)result);
				return ObjectCache.getInteger((int)d);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (result == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("int" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		if (array instanceof IntArray) {
			return array;
		}

		int len = array.size();
		IntArray result = new IntArray(len);
		result.setTemporary(true);
		
		if (array.isNumberArray()) {
			for (int i = 1; i <= len; ++i) {
				if (array.isNull(i)) {
					result.pushNull();
				} else {
					result.pushInt(array.getInt(i));
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Number) {
					result.pushInt(((Number)obj).intValue());
				} else if (obj instanceof String) {
					result.pushInt(Integer.parseInt((String)obj));
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
