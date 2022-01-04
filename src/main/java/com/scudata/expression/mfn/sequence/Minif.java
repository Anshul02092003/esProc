package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * �����������������������Ԫ�ص���Сֵ
 * A.minif(Ai:xi,��)
 * @author RunQian
 *
 */
public class Minif extends SequenceFunction {
	public Object calculate(Context ctx) {
		return Sumif.posSelect("minif", srcSequence, param, option, ctx).min();
	}
}
