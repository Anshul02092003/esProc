package com.scudata.expression.mfn.file;

import com.scudata.dm.Context;
import com.scudata.expression.FileFunction;

/**
 * ȡ�ļ�����޸�����ʱ��
 * f.date()
 * @author RunQian
 *
 */
public class Date extends FileFunction {
	public Object calculate(Context ctx) {
		return file.lastModified();
	}
}
