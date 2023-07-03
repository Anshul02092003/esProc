package com.scudata.expression.fn.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ������İ�λȡ��
 * @author yanjing
 *
 */
public class Not extends Function {
	public static Object not(Object obj) {
		if (obj instanceof BigDecimal) {
			BigInteger bi = ((BigDecimal)obj).toBigInteger().not();
			return new BigDecimal(bi);
		} else if (obj instanceof BigInteger) {
			BigInteger bi = ((BigInteger)obj).not();
			return new BigDecimal(bi);
		} else if (obj instanceof Number) {
			return ~((Number)obj).longValue();
		} else if (obj == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("not" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("not" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("not" + mm.getMessage("function.invalidParam"));
		}
	}
	
	public Object calculate(Context ctx) {
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof BigDecimal) {
			BigInteger bi = ((BigDecimal)obj).toBigInteger().not();
			return new BigDecimal(bi);
		} else if (obj instanceof BigInteger) {
			BigInteger bi = ((BigInteger)obj).not();
			return new BigDecimal(bi);
		} else if (obj instanceof Number) {
			return ~((Number)obj).longValue();
		} else if (obj == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("not" + mm.getMessage("function.paramTypeError"));
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		return array.bitwiseNot();
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		return array.bitwiseNot();
	}
}
