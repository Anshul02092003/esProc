package com.scudata.expression;

import com.scudata.array.ArrayUtil;
import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.dm.Context;

/**
 * ��ϵ������࣬���ڶ����ϵ����
 * @author WangXiaoJun
 *
 */
public abstract class Relation extends Operator {
	public static final int EQUAL = 1; // ����
	public static final int GREATER = 2; // ����
	public static final int GREATER_EQUAL = 3; // ���ڵ���
	public static final int LESS  = 4; // С��
	public static final int LESS_EQUAL = 5; // С�ڵ���
	public static final int NOT_EQUAL = 6; // ������
	public static final int AND = 7; // �߼���
	public static final int OR = 8; // �߼���

	// ƥ��������ֵ��Ҫ����
	public static final int UNMATCH = -1; // ȫ��ƥ��
	public static final int PARTICALMATCH = 0; // ����ƥ��
	public static final int ALLMATCH = 1; // ȫƥ��
	
	/**
	 * ��������ֵ��λ�ã�ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @param ralation ����Relation�ж���ĳ���
	 * @return ������Relation�еĳ���
	 */
	public static int getInverseRelation(int ralation) {
		switch(ralation) {
		case GREATER:
			return LESS;
		case GREATER_EQUAL:
			return LESS_EQUAL;
		case LESS:
			return GREATER;
		case LESS_EQUAL:
			return GREATER_EQUAL;
		default:
			return ralation;
		}
	}
	
	/**
	 * ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public abstract int getRelation();
	
	/**
	 * ��������ֵ��λ�ã�ȡ��ֵ����ֵ�ıȽϹ�ϵ
	 * @return ������Relation�еĳ���
	 */
	public abstract int getInverseRelation();

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray leftArray = left.calculateAll(ctx);
		IArray rightArray = right.calculateAll(ctx);
		return leftArray.calcRelation(rightArray, getRelation());
	}

	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		BoolArray result = ArrayUtil.booleanValue(signArray, sign);
		IArray leftArray = left.calculateAll(ctx, result, true);
		IArray rightArray = right.calculateAll(ctx, result, true);
		leftArray.calcRelations(rightArray, getRelation(), result, true);
		return result;
	}

	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		BoolArray result = leftResult.isTrue();
		IArray leftArray = left.calculateAll(ctx, result, true);
		IArray rightArray = right.calculateAll(ctx, result, true);
		leftArray.calcRelations(rightArray, getRelation(), result, true);
		return result;
	}
	
	/**
	 * �����߼��������||���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult ||�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateOr(Context ctx, IArray leftResult) {
		BoolArray result = leftResult.isTrue();
		IArray leftArray = left.calculateAll(ctx, result, false);
		IArray rightArray = right.calculateAll(ctx, result, false);
		leftArray.calcRelations(rightArray, getRelation(), result, false);
		return result;
	}
}
