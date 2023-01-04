package com.scudata.expression.operator;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �������+
 * @author RunQian
 *
 */
public class Add extends Operator {
	public Add() {
		priority = PRI_ADD;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}
	
	public Object calculate(Context ctx) {
		return Variant.add(left.calculate(ctx), right.calculate(ctx));
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray leftArray = left.calculateAll(ctx);
		IArray rightArray = right.calculateAll(ctx);
		return leftArray.memberAdd(rightArray);
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray leftArray = left.calculateAll(ctx, signArray, sign);
		IArray rightArray = right.calculateAll(ctx, signArray, sign);
		return leftArray.memberAdd(rightArray);
	}
}
