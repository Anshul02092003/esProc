package com.scudata.expression.mfn.db;

import com.scudata.dm.Context;
import com.scudata.expression.DBFunction;

/**
 * �ύ���ݿ����
 * db.commit()
 * @author RunQian
 *
 */
public class Commit extends DBFunction {
	public Object calculate(Context ctx) {
		db.commit();
		return null;
	}
}