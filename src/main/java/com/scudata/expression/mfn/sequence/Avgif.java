package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * �����������������Ԫ�ص�ƽ��ֵ
 * A.avgif(Ai:xi,��)
 * @author RunQian
 *
 */
public class Avgif extends SequenceFunction {
	public Object calculate(Context ctx) {
		return Sumif.posSelect("avgif", srcSequence, param, option, ctx).average();
	}
}