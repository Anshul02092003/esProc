package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * parse(s) �����ַ���s�󣬰��ַ�����������Ӧ���������͡�
 * @author runqian
 *
 */
public class Parse extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("parse" + mm.getMessage("function.invalidParam"));
		}

		Object obj = param.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			return obj;
		}
		
		String str = (String)obj;
		if (option != null) {
			if (option.indexOf('q') != -1) {
				int start = 0; // ��ʼλ�ã����˿հ����ǲ��������ſ�ͷ
				int len = str.length();
				for (; start < len; ++start) {
					char c = str.charAt(start);
					if (c == '"' || c == '\'') {
						int match = Sentence.scanQuotation(str, start);
						if (match > 0) {
							if (option.indexOf('e') != -1) {
								return StringUtils.unicode(str.substring(start + 1, match));
							} else {
								return str.substring(start, match + 1);
							}
						}
					} else if (!Character.isWhitespace(c)) {
						break;
					}
				}
			}
			
			if (option.indexOf('e') != -1) {
				str = StringUtils.unicode(str);
				int last = str.length() - 1;
				
				if (last > 0) {
					char s = str.charAt(0);
					char e = str.charAt(last);
					if (s == e && (s == '"'|| s == '\'')) {
						return str.substring(1, last);
					} else {
						return Variant.parse(str, true);
					}
				}
			} else if (option.indexOf('n') != -1) {
				int start = 0; // ��ʼλ�ã�ǰ������пհ׷���֧����.��-��ʼ
				int len = str.length();
				for (; start < len; ++start) {
					char c = str.charAt(start);
					if (!Character.isWhitespace(c)) {
						if ((c >= '0' && c <= '9') || c == '-' || c == '.') {
							break;
						} else {
							return null;
						}
					}
				}
				
				if (start == len) {
					return null;
				}
				
				int end = start + 1;
				for (; end < len; ++end) {
					char c = str.charAt(end);
					if ((c < '0' || c > '9') && c != '.') {
						break;
					}
				}
				
				if (start != 0 || end != len) {
					return Variant.parseNumber(str.substring(start, end));
				} else {
					return Variant.parseNumber(str);
				}
			}
		}

		return Variant.parse(str, false);
	}
}
