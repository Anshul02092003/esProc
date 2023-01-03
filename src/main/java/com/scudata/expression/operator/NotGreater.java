package com.scudata.expression.operator;

import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �������<=
 * @author RunQian
 *
 */
public class NotGreater extends Relation {
	public NotGreater() {
		priority = PRI_NGT;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"<=\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"<=\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		if (Variant.compare(left.calculate(ctx), right.calculate(ctx), true) <= 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getRelation() {
		return LESS_EQUAL;
	}

	/**
	 * ��������ֵ��λ�ã�ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getInverseRelation() {
		return GREATER_EQUAL;
	}

	/**
	 * �жϸ�����ֵ��Χ�Ƿ����㵱ǰ�������ʽ
	 * @param ctx ����������
	 * @return ȡֵ����Relation. -1��ֵ��Χ��û������������ֵ��0��ֵ��Χ��������������ֵ��1��ֵ��Χ��ֵ����������
	 */
	public int isValueRangeMatch(Context ctx) {
		IArray leftArray = left.calculateRange(ctx);
		if (leftArray == null) {
			return PARTICALMATCH;
		}
		
		IArray rightArray = right.calculateRange(ctx);
		if (rightArray instanceof ConstArray) {
			Object value = rightArray.get(1);
			int cmp1 = Variant.compare(leftArray.get(1), value, true);
			
			if (cmp1 <= 0) {
				int cmp2 = Variant.compare(leftArray.get(2), value, true);
				return cmp2 <= 0 ?  ALLMATCH: PARTICALMATCH;
			} else {
				return UNMATCH;
			}
		} else if (leftArray instanceof ConstArray) {
			Object value = leftArray.get(1);
			int cmp1 = Variant.compare(value, rightArray.get(1), true);
			
			if (cmp1 <= 0) {
				return ALLMATCH;
			} else {
				int cmp2 = Variant.compare(value, rightArray.get(2), true);
				return cmp2 <= 0 ? PARTICALMATCH : UNMATCH;
			}
		} else {
			// �޷������жϵ�ʱ���������ж�
			return PARTICALMATCH;
		}
	}
}
