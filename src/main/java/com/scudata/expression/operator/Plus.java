package com.scudata.expression.operator;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;

/**
 * �����������+
 * @author RunQian
 *
 */
public class Plus extends Operator {
	public Plus() {
		this.priority = PRI_PLUS;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		right.checkValidity();
	}
	
	public Object calculate(Context ctx) {
		Object rightResult = right.calculate(ctx);
		if (rightResult instanceof Number || rightResult == null) {
			return rightResult;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException("\"+\"" + mm.getMessage("operator.numberRightOperation"));
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
		if (array.isNumberArray()) {
			return array;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+\"" + mm.getMessage("operator.numberRightOperation"));
		}
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
		if (array.isNumberArray()) {
			return array;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"+\"" + mm.getMessage("operator.numberRightOperation"));
		}
	}
}
