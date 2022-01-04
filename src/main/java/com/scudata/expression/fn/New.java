package com.scudata.expression.fn;

import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.Node;
import com.scudata.expression.ParamInfo2;

/**
 * ����һ����¼
 * new(xi:Fi,��) ����һ���ֶ�����ΪFi�ֶ�ֵΪxi�ļ�¼��
 * @author runqian
 *
 */
public class New extends Function {
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}
		
		return this;
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
					names[i] = exps[i].getIdentifierName();
				}
			}
		}

		Table table = new Table(names, 1);
		Record r = table.newLast();
		r.setStart(0, vals);

		return option == null || option.indexOf('t') == -1 ? r : table;
	}
}
