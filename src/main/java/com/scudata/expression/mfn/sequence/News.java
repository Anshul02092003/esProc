package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.ParamInfo2;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ������е������ֶ������㣬�ϲ����������
 * A.news(X;xi:Fi,��)
 * @author RunQian
 *
 */
public class News extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("news" + mm.getMessage("function.missingParam"));
		} else if (param.getType() != IParam.Semicolon || param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("news" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		if (sub0 == null || !sub0.isLeaf() || sub1 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("news" + mm.getMessage("function.invalidParam"));
		}
		
		Expression gexp = sub0.getLeafExpression();
		ParamInfo2 pi = ParamInfo2.parse(sub1, "news", false, false);
		Expression []exps = pi.getExpressions1();
		String []names = pi.getExpressionStrs2();
		
		return srcSequence.newTables(gexp, names, exps, option, ctx);
	}
}
