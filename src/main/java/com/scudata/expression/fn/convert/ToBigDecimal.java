package com.scudata.expression.fn.convert;

import java.math.BigDecimal;

import com.scudata.array.IArray;
import com.scudata.array.LongArray;
import com.scudata.array.NumberArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���ַ�������ֵ�͵���ֵת���ɴ󸡵���
 * decimal(stringExp) ����stringExp�����������ֺ�С������ɵ��ַ�����
 * decimal(numberExp) ����numberExpֻ�����ڵ���64λ������64λ��Ҫ���ַ���stringExp ����numberExp��
 * @author runqian
 *
 */
public class ToBigDecimal extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("decimal" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("decimal" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof String) {
			try {
				return new BigDecimal((String)result);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (result == null) {
			return null;
		} else {
			return Variant.toBigDecimal(result);
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
		Object []resultValues = new Object[len + 1];
		
		if (array instanceof LongArray) {
			LongArray longArray = (LongArray)array;
			for (int i = 1; i <= len; ++i) {
				resultValues[i] = new BigDecimal(longArray.getLong(i));
			}
		} else if (array instanceof NumberArray) {
			NumberArray numberArray = (NumberArray)array;
			for (int i = 1; i <= len; ++i) {
				resultValues[i] = new BigDecimal(numberArray.getDouble(i));
			}
		} else {
			try {
				for (int i = 1; i <= len; ++i) {
					Object obj = array.get(i);
					if (obj instanceof String) {
						resultValues[i] = new BigDecimal((String)obj);
					} else if (obj != null) {
						resultValues[i] = Variant.toBigDecimal(obj);
					}
				}
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
		ObjectArray result = new ObjectArray(resultValues, len);
		result.setTemporary(true);
		return result;
	}
}
