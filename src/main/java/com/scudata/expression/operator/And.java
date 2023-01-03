package com.scudata.expression.operator;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �������&&
 * @author RunQian
 *
 */
public class And extends Relation {
	public And() {
		priority = PRI_AND;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"&&\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"&&\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		Object value = left.calculate(ctx);
		if (Variant.isTrue(value)) {
			value = right.calculate(ctx);
			if (value instanceof Boolean) {
				return value;
			} else {
				return Boolean.valueOf(value != null);
			}
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getRelation() {
		return AND;
	}
	
	/**
	 * ��������ֵ��λ�ã�ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getInverseRelation() {
		return AND;
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		// �����ʽ��falseʱ�������Ҳ���ʽ
		IArray leftResult = left.calculateAll(ctx);
		return right.calculateAnd(ctx, leftResult);
		/*if (right instanceof Relation) {
			return ((Relation)right).calculateAnd(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, true);
			return leftResult.calcRelation(array, AND);
		}*/
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray leftResult = left.calculateAll(ctx, signArray, sign);
		if (right instanceof Relation) {
			return ((Relation)right).calculateAnd(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, true);
			return leftResult.calcRelation(array, AND);
		}
	}
	
	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		leftResult = left.calculateAnd(ctx, leftResult);
		return right.calculateAnd(ctx, leftResult);

		/*if (left instanceof Relation) {
			leftResult = ((Relation)left).calculateAnd(ctx, leftResult);
		} else {
			IArray array = left.calculateAll(ctx, leftResult, true);
			leftResult = leftResult.calcRelation(array, AND);
		}
		
		if (right instanceof Relation) {
			return ((Relation)right).calculateAnd(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, true);
			return leftResult.calcRelation(array, AND);
		}*/
	}
	
	/**
	 * �����߼��������||���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult ||�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateOr(Context ctx, IArray leftResult) {
		// x1 or (x2 and x3)
		IArray result = calculateAll(ctx, leftResult, false);
		return leftResult.calcRelation(result, OR);
	}

	/**
	 * �жϸ�����ֵ��Χ�Ƿ����㵱ǰ�������ʽ
	 * @param ctx ����������
	 * @return ȡֵ����Relation. -1��ֵ��Χ��û������������ֵ��0��ֵ��Χ��������������ֵ��1��ֵ��Χ��ֵ����������
	 */
	public int isValueRangeMatch(Context ctx) {
		int result = left.isValueRangeMatch(ctx);
		if (result == UNMATCH) {
			return result;
		}
		
		int rightResult = right.isValueRangeMatch(ctx);
		return result < rightResult ? result : rightResult;
	}
}
