package com.scudata.expression.mfn.vdb;

import com.scudata.dm.Context;
import com.scudata.expression.VSFunction;

/**
 * ���ص�ǰ·���Ľ�ֵ
 * h.path()
 * @author RunQian
 *
 */
public class Path extends VSFunction {
	public Object calculate(Context ctx) {
		return vs.path(option);
	}
}
