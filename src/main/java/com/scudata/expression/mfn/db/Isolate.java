package com.scudata.expression.mfn.db;

import com.scudata.dm.Context;
import com.scudata.expression.DBFunction;

/**
 * ����ѡ���������ӵ�����������𲢷���ԭ���𣨶�Ӧ��ѡ���ַ�������ѡ����JDBCȱʡ
 * db.isolate() ѡ��@ncurs�ֱ��Ӧnone,commit,uncommit,repeatable,serializable
 * @author RunQian
 *
 */
public class Isolate extends DBFunction {
	public Object calculate(Context ctx) {
		return db.isolate(option);
	}
}
