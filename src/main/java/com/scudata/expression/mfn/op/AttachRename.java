package com.scudata.expression.mfn.op;

import com.scudata.dm.Context;
import com.scudata.dm.op.Rename;
import com.scudata.expression.OperableFunction;
import com.scudata.expression.ParamInfo2;

/**
 * ���α��ܵ����Ӷ��ֶν�������������
 * op.rename(F:F',��) op���α��ܵ�
 * @author RunQian
 *
 */
public class AttachRename extends OperableFunction {
	public Object calculate(Context ctx) {
		ParamInfo2 pi = ParamInfo2.parse(param, "rename", true, false);
		String []srcFields = pi.getExpressionStrs1();
		String []newFields = pi.getExpressionStrs2();
		
		Rename rename = new Rename(this, srcFields, newFields);
		return operable.addOperation(rename, ctx);
	}
}
