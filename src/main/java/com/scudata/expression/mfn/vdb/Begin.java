package com.scudata.expression.mfn.vdb;

import com.scudata.dm.Context;
import com.scudata.expression.VDBFunction;

/**
 * ��������
 * v.begin()
 * @author RunQian
 *
 */
public class Begin extends VDBFunction {
	public Object calculate(Context ctx) {
		return vdb.begin();
	}
}
