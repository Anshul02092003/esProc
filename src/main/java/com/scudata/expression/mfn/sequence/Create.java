package com.scudata.expression.mfn.sequence;

import com.scudata.dm.Context;
import com.scudata.expression.SequenceFunction;

/**
 * ʹ���������е�һ����¼�����ݽṹ�����������
 * T.create() P.create()
 * @author RunQian
 *
 */
public class Create extends SequenceFunction {
	public Object calculate(Context ctx) {
		return srcSequence.create();
	}
}
