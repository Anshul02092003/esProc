package com.scudata.expression.mfn.db;

import com.scudata.dm.Context;
import com.scudata.expression.DBFunction;

// 
/**
 * ȡ��һ�����ݿ����ִ�еĴ�����룬0��ʾ�޴�
 * db.error()
 * @author RunQian
 *
 */
public class Error extends DBFunction {
	public Object calculate(Context ctx) {
		return db.error(option);
	}
}
