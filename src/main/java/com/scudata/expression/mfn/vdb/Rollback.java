package com.scudata.expression.mfn.vdb;

import com.scudata.dm.Context;
import com.scudata.expression.VDBFunction;

/**
 * �ع�����������
 * @author RunQian
 *
 */
public class Rollback extends VDBFunction {
	public Object calculate(Context ctx) {
		vdb.rollback();
		return null;
	}
}
