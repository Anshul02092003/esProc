package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * �������еĳ���
 * A.len()
 * @author RunQian
 *
 */
public class Len extends SequenceFunction {
	public Object calculate(Context ctx) {
		return srcSequence.length();
	}
}
