package com.scudata.expression.mfn.dw;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.ParamInfo2;
import com.scudata.expression.TableMetaDataFunction;

/**
 * �޸������ֶ���
 * T.rename(F:F��,��)
 * @author RunQian
 *
 */
public class Rename extends TableMetaDataFunction {
	public Object calculate(Context ctx) {
		ParamInfo2 pi = ParamInfo2.parse(param, "rename", true, false);
		String []srcFields = pi.getExpressionStrs1();
		String []newFields = pi.getExpressionStrs2();
		try {
			table.rename(srcFields, newFields, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		return table;
	}

}
