package com.scudata.expression.fn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * substr(s1,s2) ���ַ���s1�У�����ߵ�1���ַ���ʼ�����ַ���s2���ҵ��󷵻�s2λ�ú���Ĵ�����s2�������ַ���ʱ���ؿ�
 * @l	����s2ǰ����ִ�
 * @c	��Сд������
 * @q	������Ĳ���
 * @author runqian
 *
 */
public class SubString extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("substr" + mm.getMessage("function.missingParam"));
		}

		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("substr" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("substr" + mm.getMessage("function.invalidParam"));
		}

		Object o1 = sub1.getLeafExpression().calculate(ctx);
		if (o1 == null) {
			return null;
		} else if (!(o1 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("substr" + mm.getMessage("function.paramTypeError"));
		}

		Object o2 = sub2.getLeafExpression().calculate(ctx);
		if (!(o2 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("substr" + mm.getMessage("function.paramTypeError"));
		}

		boolean isRight = true, ignoreCase = false, containQuotation = true;
		if (option != null) {
			if (option.indexOf('l') != -1) isRight = false;
			if (option.indexOf('c') != -1) ignoreCase = true;
			if (option.indexOf('q') != -1) containQuotation = false;
		}
		
		String src = (String)o1;
		String str = (String)o2;
		if (containQuotation) {
			int index;
			if (ignoreCase) {
				index = StringUtils.indexOfIgnoreCase(src, str, 0);
			} else {
				index = src.indexOf(str);
			}
			
			if (index != -1) {
				if (isRight) {
					return src.substring(index + str.length());
				} else {
					return src.substring(0, index);
				}
			} else {
				return null;
			}
		} else {
			int i = 0;
			while (true) {
				int index;
				if (ignoreCase) {
					index = StringUtils.indexOfIgnoreCase(src, str, i);
				} else {
					index = src.indexOf(str, i);
				}
				
				if (index == -1) {
					return null;
				}
				
				int q = src.indexOf('"', i);
				if (q < 0) {
					q = src.indexOf('\'', i);
					if (q < 0) {
						if (isRight) {
							return src.substring(index + str.length());
						} else {
							return src.substring(0, index);
						}
					}
				}
				
				int match = Sentence.scanQuotation(src, q);
				if (match > 0 && q <= index && match >= index) {
					i = match + 1;
				} else {
					if (isRight) {
						return src.substring(index + str.length());
					} else {
						return src.substring(0, index);
					}
				}
			}
		}
	}
}
