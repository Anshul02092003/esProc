package com.scudata.expression.mfn.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.StringFunction;
import com.scudata.resources.EngineMessage;

/**
 * ��������ʽ�Դ���ƥ�䣬���ز������ɵ����У���ƥ�䷵�ؿ�
 * s.regex(rs)
 * @author RunQian
 *
 */
public class Regex extends StringFunction {
	public Object calculate(Context ctx) {
		int flags = 0;
		if (option != null) {
			if (option.indexOf('c') != -1) flags |= Pattern.CASE_INSENSITIVE;
			if (option.indexOf('u') != -1) flags |= Pattern.UNICODE_CASE;
		}

		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("regex" + mm.getMessage("function.invalidParam"));
		}

		Object obj = param.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("regex" + mm.getMessage("function.paramTypeError"));
		}

		Pattern pattern = Pattern.compile((String)obj, flags);
		Matcher m = pattern.matcher(srcStr);
		if (!m.find()) return null;

		int gcount = m.groupCount();
		if (gcount == 0) {
			return srcStr;
		} else if (gcount == 1) {
			Sequence seq = new Sequence(3);
			do {
				seq.add(m.group(1));
			} while(m.find());
			
			return seq;
		} else {
			Sequence seq = new Sequence(gcount);
			do {
				for (int i = 1; i <= gcount; ++i) {
					seq.add(m.group(i));
				}
			} while (m.find());
			
			return seq;
		}
	}
}
