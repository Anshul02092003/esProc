package com.scudata.expression.mfn.cursor;

import com.scudata.dm.Context;
import com.scudata.expression.CursorFunction;

/**
 * �����α굽����ͷ��
 * cs.reset()
 * @author RunQian
 *
 */
public class Reset extends CursorFunction {
	public Object calculate(Context ctx) {
		cursor.reset();
		return cursor;
	}
}
