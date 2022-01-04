package com.scudata.expression.mfn.file;

import com.scudata.dm.Context;
import com.scudata.expression.FileFunction;

/**
 * �ж��ļ��Ƿ����
 * f.exists()
 * @author RunQian
 *
 */
public class Exists extends FileFunction {
	public Object calculate(Context ctx) {
		return Boolean.valueOf(file.isExists());
	}
}
