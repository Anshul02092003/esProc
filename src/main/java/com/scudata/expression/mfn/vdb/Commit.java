package com.scudata.expression.mfn.vdb;

import com.scudata.dm.Context;
import com.scudata.expression.VDBFunction;

/**
 * �ύ����������
 * v.commit()
 * @author RunQian
 *
 */
public class Commit extends VDBFunction {
	public Object calculate(Context ctx) {
		return vdb.commit();
	}
}