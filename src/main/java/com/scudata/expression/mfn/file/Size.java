package com.scudata.expression.mfn.file;

import com.scudata.dm.Context;
import com.scudata.expression.FileFunction;

/**
 * ȡ�ļ���С
 * f.size()
 * @author RunQian
 *
 */
public class Size extends FileFunction {
	public Object calculate(Context ctx) {
		return new Long(file.size());
	}
}
