package com.scudata.expression.mfn.file;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.FileFunction;

/**
 * ���ı��ļ����߼��ļ���������
 * f.import(Fi:type:fmt,��;k:n,s)
 * @author RunQian
 *
 */
public class Import extends FileFunction {
	public Object calculate(Context ctx) {
		// file.cursor��@xѡ���ֹ��д��@x���ļ�ɾ��
		String option = this.option;
		if (option != null) {
			option = option.replace('x', ' ');
		}
		
		ICursor cursor = CreateCursor.createCursor("import", file, cs, param, option, ctx);
		Sequence seq = cursor.fetch();
		
		if (seq != null) {
			return seq;
		} else {
			DataStruct ds = cursor.getDataStruct();
			if (ds != null) {
				return new Table(ds);
			} else {
				return null;
			}
		}
	}
}
