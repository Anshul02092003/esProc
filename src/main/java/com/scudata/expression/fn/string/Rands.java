package com.scudata.expression.fn.string;

import java.util.Random;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * rands(s,l) ��s�е��ַ��������һ������Ϊl���ַ���
 * @author runqian
 *
 */
public class Rands extends Function {
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}

		return this;
	}

	public Object calculate(Context ctx) {
		if (param == null || param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.missingParam"));
		} else if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.invalidParam"));
		}

		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.invalidParam"));
		}

		Object o1 = sub1.getLeafExpression().calculate(ctx);
		if (!(o1 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.paramTypeError"));
		}

		Object o2 = sub2.getLeafExpression().calculate(ctx);
		if (!(o2 instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.paramTypeError"));
		}

		return rands((String)o1, ((Number)o2).intValue(), ctx);
	}

	private static String rands(String src, int len, Context ctx) {
		if (len < 1) return null;

		int srcLen = src.length();
		if (srcLen > 1) {
			char []pads = src.toCharArray();
			Random rand = ctx.getRandom();
			char []chars = new char[len];
			for (int i = 0; i < len; ++i) {
				chars[i] = pads[rand.nextInt(srcLen)];
			}

			return new String(chars);
		} else if (srcLen == 1) {
			char c = src.charAt(0);
			char []chars = new char[len];
			for (int i = 0; i < len; ++i) {
				chars[i] = c;
			}

			return new String(chars);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rands" + mm.getMessage("function.invalidParam"));
		}
	}
}
