package com.scudata.expression.operator;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���������!
 * @author RunQian
 *
 */
public class Not extends Operator {
	public Not() {
		priority = PRI_NOT;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"!\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		right.checkValidity();
	}
	
	public Object calculate(Context ctx) {
		return Boolean.valueOf(Variant.isFalse(right.calculate(ctx)));
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
		IArray rightArray = right.calculateAll(ctx);
		return rightArray.not();
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray rightArray = right.calculateAll(ctx, signArray, sign);
		return rightArray.not();
	}
}
