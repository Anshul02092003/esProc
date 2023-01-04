package com.scudata.expression.fn.convert;

import com.scudata.array.IArray;
import com.scudata.array.NumberArray;
import com.scudata.array.StringArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ���ݸ�����Unicode�������ASCII���ȡ��Ӧ���ַ�
 * @author runqian
 *
 */
public class ToChar extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("char" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("char" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof Number) {
			char[] c = new char[] {(char)((Number)result).intValue()};
			return new String(c);
		} else if (result == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("char" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		int len = array.size();
		String []resultValues = new String[len + 1];
		char []chars = new char[1];
		
		if (array instanceof NumberArray) {
			NumberArray numberArray = (NumberArray)array;
			for (int i = 1; i <= len; ++i) {
				if (!array.isNull(i)) {
					chars[0] = (char)numberArray.getInt(i);
					resultValues[i] =  new String(chars);
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Number) {
					chars[0] = (char)((Number)obj).intValue();
					resultValues[i] =  new String(chars);
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("char" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		StringArray result = new StringArray(resultValues, len);
		result.setTemporary(true);
		return result;
	}
}
