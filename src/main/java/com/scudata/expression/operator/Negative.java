package com.scudata.expression.operator;

import java.util.Date;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����������-
 * @author RunQian
 *
 */
public class Negative extends Operator {
	public Negative() {
		priority = PRI_NEGT;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"-\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		right.checkValidity();
	}
	
	public Object calculate(Context ctx) {
		Object rightResult = right.calculate(ctx);
		if (rightResult instanceof Number) {
			return Variant.negate((Number)rightResult);
		} else if (rightResult == null) {
			return null;
		} else if (rightResult instanceof Date) {
			return Variant.negate((Date)rightResult);
		} else if (rightResult instanceof String) {
			return Variant.negate((String)rightResult);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"-\"" +mm.getMessage("operator.numberRightOperation"));
		}
	}
	
	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	public boolean canCalculateAll() {
		return right.canCalculateAll();
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = right.calculateAll(ctx);
		return array.negate();
	}

	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray array = right.calculateAll(ctx, signArray, sign);
		return array.negate();
	}
}
