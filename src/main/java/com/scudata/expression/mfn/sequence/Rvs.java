package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * ��ת���е�Ԫ�������Ԫ��
 * A.rvs()
 * @author RunQian
 *
 */
public class Rvs extends SequenceFunction {
	public Object calculate(Context ctx) {
		return srcSequence.rvs();
	}
}