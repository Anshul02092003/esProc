package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * �����������������Ԫ�ص����ֵ
 * A.maxif(Ai:xi,��)
 * @author RunQian
 *
 */
public class Maxif extends SequenceFunction {
	public Object calculate(Context ctx) {
		return Sumif.posSelect("maxif", srcSequence, param, option, ctx).max();
	}
}
