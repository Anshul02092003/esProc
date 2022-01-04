package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * �����������������Ԫ��ȡֵΪ�棨�ǿղ��Ҳ���false���ĸ���
 * A.countif(Ai:xi,��)
 * @author RunQian
 *
 */
public class Countif extends SequenceFunction {
	public Object calculate(Context ctx) {
		return Sumif.posSelect("countif", srcSequence, param, option, ctx).count(option);
	}
}
