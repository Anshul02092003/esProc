package com.scudata.expression.mfn.sequence;

import com.scudata.array.ArrayUtil;
import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.IndexTable;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.expression.Relation;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ��������ֵ���Ҽ�¼
 * A.find(k)
 * @author RunQian
 *
 */
public class Find extends SequenceFunction {
	private Sequence prevSequence;
	private IndexTable indexTable;
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("find" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object key;
		if (param.isLeaf()) {
			key = param.getLeafExpression().calculate(ctx);
		} else {
			int count = param.getSubSize();
			Sequence seq = new Sequence(count);
			key = seq;
			for (int i = 0; i < count; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("find" + mm.getMessage("function.invalidParam"));
				}
				
				seq.add(sub.getLeafExpression().calculate(ctx));
			}
		}

		if (option != null) {
			boolean isSorted = option.indexOf('b') != -1;
			boolean isMultiRow = option.indexOf('k') != -1;
			if (isMultiRow && key instanceof Sequence) {
				Sequence keys = (Sequence)key;
				int len = keys.length();
				Sequence result = new Sequence(len);
				for (int i = 1; i <= len; ++i) {
					result.add(srcSequence.findByKey(keys.getMem(i), isSorted));
				}
				
				return result;
			} else {
				return srcSequence.findByKey(key, isSorted);
			}
		} else {
			return srcSequence.findByKey(key, false);
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (leftValue instanceof Sequence && param.isLeaf()) {
				Sequence srcSequence = (Sequence)leftValue;
				if (prevSequence != srcSequence) {
					prevSequence = srcSequence;
					indexTable = srcSequence.newIndexTable();
				}
				
				IArray array = param.getLeafExpression().calculateAll(ctx);
				int []index = indexTable.findAllPos(array);
				int len = index.length;
				Object []rs = new Object[len];
				for (int i = 1; i < len; ++i) {
					if (index[i] > 0) {
						rs[i] = srcSequence.getMem(index[i]);
					}
				}
				ObjectArray result = new ObjectArray(rs, len - 1);
				result.setTemporary(true);
				return result;
			}
		}
		
		return calculateAll(leftArray, ctx);
	}

	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (leftValue instanceof Sequence && param.isLeaf()) {
				Sequence srcSequence = (Sequence)leftValue;
				if (prevSequence != srcSequence) {
					prevSequence = srcSequence;
					indexTable = srcSequence.newIndexTable();
				}
				
				BoolArray boolArray = ArrayUtil.booleanValue(signArray, sign);
				IArray array = param.getLeafExpression().calculateAll(ctx, boolArray, true);
				int []index = indexTable.findAllPos(array, boolArray);
				
				int len = index.length;
				Object []rs = new Object[len];
				for (int i = 1; i < len; ++i) {
					if (index[i] > 0) {
						rs[i] = srcSequence.getMem(index[i]);
					}
				}
				
				ObjectArray result = new ObjectArray(rs, len - 1);
				result.setTemporary(true);
				return result;
			}
		}
		
		return calculateAll(leftArray, ctx, signArray, sign);
	}
	
	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (leftValue instanceof Sequence && param.isLeaf()) {
				Sequence srcSequence = (Sequence)leftValue;
				if (prevSequence != srcSequence) {
					prevSequence = srcSequence;
					indexTable = srcSequence.newIndexTable();
				}
				
				BoolArray result = leftResult.isTrue();
				IArray array = param.getLeafExpression().calculateAll(ctx, result, true);
				int []index = indexTable.findAllPos(array, result);

				for (int i = 1, len = index.length; i < len; ++i) {
					if (index[i] < 1) {
						result.set(i, false);
					}
				}
				
				return result;
			}
		}
		
		return calculateAnd(leftArray, ctx, leftResult);
	}

	/**
	 * �жϸ�����ֵ��Χ�Ƿ����㵱ǰ�������ʽ
	 * @param ctx ����������
	 * @return ȡֵ����Relation. -1��ֵ��Χ��û������������ֵ��0��ֵ��Χ��������������ֵ��1��ֵ��Χ��ֵ����������
	 */
	public int isValueRangeMatch(Context ctx) {
		if (option != null && option.indexOf('k') != -1) {
			return Relation.PARTICALMATCH;
		}
		
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (leftValue instanceof Sequence && param.isLeaf()) {
				IArray array = param.getLeafExpression().calculateRange(ctx);
				if (array == null) {
					return Relation.PARTICALMATCH;
				}
							
				Sequence srcSequence = (Sequence)leftValue;
				Object minValue = array.get(1);
				Object maxValue = array.get(2);
				
				if (Variant.isEquals(minValue, maxValue)) {
					boolean isSorted = option != null && option.indexOf('b') != -1;
					Object value = srcSequence.findByKey(minValue, isSorted);
					if (Variant.isTrue(value)) {
						return Relation.ALLMATCH;
					} else {
						return Relation.UNMATCH;
					}
				} else {
					return Relation.PARTICALMATCH;
				}
			}
		}

		return Relation.PARTICALMATCH;
	}
}
