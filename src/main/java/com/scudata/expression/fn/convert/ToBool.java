package com.scudata.expression.fn.convert;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.array.StringArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * bool(expression) �����ʽexpression����������ת��Ϊ�����͡�
 * ת�����򣺵�����ֵΪnull���ַ���"false"(��С����)������ֵfalseʱ����false�����򷵻�true��
 * @author runqian
 *
 */
public class ToBool extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bool" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bool" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof Boolean) {
			return obj;
		} else if (obj instanceof String) {
			if (((String)obj).equals("false")) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} else if (obj == null)  {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		if (array instanceof StringArray) {
			int len = array.size();
			StringArray strArray = (StringArray)array;
			boolean []values = new boolean[len + 1];
			
			for (int i = 1; i <= len; ++i) {
				String str = strArray.getString(i);
				values[i] = str != null && !str.equals("false");
			}
			
			BoolArray result = new BoolArray(values, len);
			result.setTemporary(true);
			return result;
		} else if (array instanceof ObjectArray) {
			int len = array.size();
			boolean []values = new boolean[len + 1];
			
			for (int i = 1; i <= len; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Boolean) {
					values[i] = (Boolean)obj;
				} else if (obj instanceof String) {
					values[i] = obj != null && !obj.equals("false");
				} else {
					values[i] = obj != null;
				}
			}
			
			BoolArray result = new BoolArray(values, len);
			result.setTemporary(true);
			return result;
		} else {
			return array.isTrue();
		}
	}
}
