package com.scudata.expression.mfn.record;

import com.scudata.dm.Context;
import com.scudata.dm.Table;
import com.scudata.expression.RecordFunction;

/**
 * ʹ�ü�¼�����ݽṹ�����������
 * r.create()
 * @author RunQian
 *
 */
public class Create extends RecordFunction {
	public Object calculate(Context ctx) {
		Table table = new Table(srcRecord.dataStruct());
		return table;
	}
}
