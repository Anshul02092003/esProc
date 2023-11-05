package com.scudata.expression.operator;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * �������%
 * ȡ����������
 * @author RunQian
 *
 */
public class Mod extends Operator {
	public Mod() {
		priority = PRI_MOD;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"%\"" + mm.getMessage("operator.missingLeftOperation"));
		} else if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"%\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
		right.checkValidity();
	}

	public Object calculate(Context ctx) {
		Object o1 = left.calculate(ctx);
		Object o2 = right.calculate(ctx);

		//��������У����Ǽ������������
		if (o1 instanceof Sequence) {
			if (o2 instanceof Sequence) {
				return CursorUtil.xor((Sequence)o1, (Sequence)o2);
				//return ((Sequence)o1).xor((Sequence)o2);
			} else if (o2 == null) {
				return o1;
			} else {
				Sequence seq2 = new Sequence(1);
				seq2.add(o2);
				return CursorUtil.xor((Sequence)o1, seq2);
				//return ((Sequence)o1).xor(seq2);
			}
		} else if (o2 instanceof Sequence) {
			if (o1 == null) {
				return o2;
			} else {
				Sequence seq1 = new Sequence(1);
				seq1.add(o1);
				return CursorUtil.xor(seq1, (Sequence)o2);
				//return seq1.xor((Sequence)o2);
			}
		}
		
		return Variant.mod(o1, o2);
	}

	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray leftArray = left.calculateAll(ctx);
		IArray rightArray = right.calculateAll(ctx);
		return leftArray.memberMod(rightArray);
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
		return leftArray.memberMod(rightArray);
	}
	
	/**
	 * ������ʽ��ȡֵ��Χ
	 * @param ctx ����������
	 * @return
	 */
	public IArray calculateRange(Context ctx) {
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray.isMemberEquals(1, 2)) {
			IArray rightArray = right.calculateAll(ctx);
			return leftArray.memberMod(rightArray);
		} else {
			return null;
		}
	}
}
