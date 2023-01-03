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
 * �������||
 * @author RunQian
 *
 */
public class Or extends Relation {
	public Or() {
		priority = PRI_OR;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"||\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"||\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		Object value = left.calculate(ctx);
		if (Variant.isTrue(value)) {
			return Boolean.TRUE;
		} else {
			value = right.calculate(ctx);
			if (value instanceof Boolean) {
				return value;
			} else {
				return Boolean.valueOf(value != null);
			}
		}
	}

	/**
	 * ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getRelation() {
		return OR;
	}
	
	/**
	 * ��������ֵ��λ�ã�ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public int getInverseRelation() {
		return OR;
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		// �����ʽ��trueʱ�������Ҳ���ʽ
		IArray leftResult = left.calculateAll(ctx);
		if (right instanceof Relation) {
			return ((Relation)right).calculateOr(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, false);
			return leftResult.calcRelation(array, OR);
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
		IArray leftResult = left.calculateAll(ctx, signArray, sign);
		if (right instanceof Relation) {
			return ((Relation)right).calculateOr(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, false);
			return leftResult.calcRelation(array, OR);
		}
	}
	
	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		// x1 and (x2 or x3)
		IArray result = calculateAll(ctx, leftResult, true);
		return leftResult.calcRelation(result, AND);
	}
	
	/**
	 * �����߼��������||���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult ||�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateOr(Context ctx, IArray leftResult) {
		if (left instanceof Relation) {
			leftResult = ((Relation)left).calculateOr(ctx, leftResult);
		} else {
			IArray array = left.calculateAll(ctx, leftResult, false);
			leftResult = leftResult.calcRelation(array, OR);
		}

		if (right instanceof Relation) {
			return ((Relation)right).calculateOr(ctx, leftResult);
		} else {
			IArray array = right.calculateAll(ctx, leftResult, false);
			return leftResult.calcRelation(array, OR);
		}
	}

	/**
	 * �жϸ�����ֵ��Χ�Ƿ����㵱ǰ�������ʽ
	 * @param ctx ����������
	 * @return ȡֵ����Relation. -1��ֵ��Χ��û������������ֵ��0��ֵ��Χ��������������ֵ��1��ֵ��Χ��ֵ����������
	 */
	public int isValueRangeMatch(Context ctx) {
		int result = left.isValueRangeMatch(ctx);
		if (result == ALLMATCH) {
			return result;
		}
		
		int rightResult = right.isValueRangeMatch(ctx);
		return result < rightResult ? rightResult : result;
	}
}
