package com.scudata.expression.mfn;

import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.expression.MemberFunction;

/**
 * ���ؼ�¼�ļ������û�������򷵻������ֶ���ɵ����У�������Ǽ�¼�򷵻ر���
 * v.v()
 * @author RunQian
 *
 */
public class Value extends MemberFunction {
	protected Object src;
	
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}

	public void setDotLeftObject(Object obj) {
		src = obj;
	}

	public Object calculate(Context ctx) {
		if (src instanceof Record) {
			return ((Record)src).value();
		} else {
			return src;
		}
	}
}
