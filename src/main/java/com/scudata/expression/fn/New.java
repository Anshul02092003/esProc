package com.scudata.expression.fn;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.Node;
import com.scudata.expression.ParamInfo2;
import com.scudata.resources.EngineMessage;

/**
 * ����һ����¼
 * new(xi:Fi,��) ����һ���ֶ�����ΪFi�ֶ�ֵΪxi�ļ�¼��
 * @author runqian
 *
 */
public class New extends Function {
	public Node optimize(Context ctx) {
		param.optimize(ctx);
		return this;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("new" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		ParamInfo2 pi = ParamInfo2.parse(param, "new", false, false);
		Expression []exps = pi.getExpressions1();
		Object []vals = pi.getValues1(ctx);
		String []names = pi.getExpressionStrs2();

		int colCount = names.length;
		for (int i = 0; i < colCount; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				if (exps[i] != null) {
					names[i] = exps[i].getFieldName();
				}
			}
		}

		Table table = new Table(names, 1);
		BaseRecord r = table.newLast();
		r.setStart(0, vals);

		return option == null || option.indexOf('t') == -1 ? r : table;
	}
}
