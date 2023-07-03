package com.scudata.expression.fn.math;

import java.math.BigInteger;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.LongArray;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;


/**
 * bit1(x) ����x�Ķ����Ʊ�ʾʱ1�ĸ���
 * @author RunQian
 *
 */
public class Bit1 extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
		}
	}
	
	// ȡ���Ķ�����λ��1�ĸ���
	private static int bitCount(Object obj) {
		if (obj instanceof Long) {
			return Long.bitCount((Long)obj);
		} else if (obj instanceof BigInteger) {
			return ((BigInteger)obj).bitCount();
		} else if (obj instanceof Number) {
			return Integer.bitCount(((Number)obj).intValue());
		} else if (obj == null) {
			return 0;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	public Object calculate(Context ctx) {
		Object obj = param.getLeafExpression().calculate(ctx);
		return ObjectCache.getInteger(bitCount(obj));
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		int len = array.size();
		IntArray result = new IntArray(len);

		if (array instanceof IntArray) {
			IntArray intArray = (IntArray)array;
			for (int i = 1; i <= len; ++i) {
				if (intArray.isNull(i)) {
					result.pushInt(0);
				} else {
					result.pushInt(Integer.bitCount(intArray.getInt(i)));
				}
			}
		} else if (array instanceof LongArray) {
			LongArray longArray = (LongArray)array;
			for (int i = 1; i <= len; ++i) {
				if (longArray.isNull(i)) {
					result.pushInt(0);
				} else {
					result.pushInt(Long.bitCount(longArray.getLong(i)));
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				result.pushInt(bitCount(array.get(i)));
			}
		}
		
		return result;
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		if (!param.isLeaf() || option == null || option.indexOf('1') == -1) {
			return super.calculateAll(ctx, signArray, sign);
		}
		
		IArray array = param.getLeafExpression().calculateAll(ctx);
		int len = array.size();
		IntArray result = new IntArray(len);

		if (array instanceof IntArray) {
			IntArray intArray = (IntArray)array;
			for (int i = 1; i <= len; ++i) {
				if (intArray.isNull(i)) {
					result.pushInt(0);
				} else if (signArray.isTrue(i) == sign) {
					result.pushInt(Integer.bitCount(intArray.getInt(i)));
				} else {
					result.pushInt(0);
				}
			}
		} else if (array instanceof LongArray) {
			LongArray longArray = (LongArray)array;
			for (int i = 1; i <= len; ++i) {
				if (longArray.isNull(i)) {
					result.pushInt(0);
				} else if (signArray.isTrue(i) == sign) {
					result.pushInt(Long.bitCount(longArray.getLong(i)));
				} else {
					result.pushInt(0);
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				if (signArray.isTrue(i) == sign) {
					result.pushInt(bitCount(array.get(i)));
				} else {
					result.pushInt(0);
				}
			}
		}
		
		return result;
	}
}
