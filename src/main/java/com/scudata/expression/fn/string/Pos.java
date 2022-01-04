package com.scudata.expression.fn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * pos(s1, s2{, begin}) ����ĸ��s1����ʼλ��Ϊbegin���Ӵ�s2��λ�ã��Ҳ�������null��
 * @c	��Сд������
 * @h	ֻ��ͷ��
 * @z	��ǰ�ң���@hʱֻ��β��
 * @author runqian
 *
 */
public class Pos extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pos" + mm.getMessage("function.missingParam"));
		}

		int size = param.getSubSize();
		if (size != 2 && size != 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pos" + mm.getMessage("function.invalidParam"));
		}

		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pos" + mm.getMessage("function.invalidParam"));
		}

		Object obj = sub1.getLeafExpression().calculate(ctx);
		if (obj == null) {
			return null;
		} else if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pos" + mm.getMessage("function.paramTypeError"));
		}

		String str1 = (String)obj;
		obj = sub2.getLeafExpression().calculate(ctx);
		if (obj == null) {
			return null;
		} else if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pos" + mm.getMessage("function.paramTypeError"));
		}

		String str2 = (String)obj;
		boolean isFirst = true, ignoreCase = false, headOnly = false;
		if (option != null) {
			if (option.indexOf('z') != -1) isFirst = false;
			if (option.indexOf('c') != -1) ignoreCase = true;
			if (option.indexOf('h') != -1) headOnly = true;
		}

		int begin = isFirst ? 0 : str1.length() - 1;
		if (size > 2) {
			IParam sub3 = param.getSub(2);
			if (sub3 != null) {
				obj = sub3.getLeafExpression().calculate(ctx);
				if (obj != null) {
					if (!(obj instanceof Number)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("pos" + mm.getMessage("function.paramTypeError"));
					}
					
					begin = ((Number)obj).intValue() - 1;
				}
			}
		}
		
		if (ignoreCase) {
			if (isFirst) {
				if (headOnly) {
					if (startsWithIgnoreCase(str1, str2)) {
						return 1;
					} else {
						return null;
					}
				} else {
					int index = StringUtils.indexOfIgnoreCase(str1, str2, begin);
					return index < 0 ? null : index + 1;
				}
			} else {
				if (headOnly) {
					if (endsWithIgnoreCase(str1, str2)) {
						return str1.length() - str2.length() + 1;
					} else {
						return null;
					}
				} else {
					int index = StringUtils.indexOfIgnoreCase(str1, str2, begin);
					return index < 0 ? null : index + 1;
				}
			}
		} else {
			if (isFirst) {
				if (headOnly) {
					if (str1.startsWith(str2, 0)) {
						return 1;
					} else {
						return null;
					}
				} else {
					int index = str1.indexOf(str2, begin);
					return index < 0 ? null : index + 1;
				}
			} else {
				if (headOnly) {
					int index = str1.length() - str2.length();
					if (str1.startsWith(str2, index)) {
						return index + 1;
					} else {
						return null;
					}
				} else {
					int index = str1.lastIndexOf(str2, begin);
					return index < 0 ? null : index + 1;
				}
			}
		}
	}
	
	private static boolean startsWithIgnoreCase(String source, String target) {
		int targetCount = target.length();
		if (targetCount == 0) {
			return true;
		}
		
		int sourceCount = source.length();
		if (sourceCount < targetCount) {
			return false;
		}
		
		for (int j = 0, k = 0; k < targetCount; ++j, ++k) {
			if (source.charAt(j) != target.charAt(k) && Character.toUpperCase(source.charAt(j)) != Character.toUpperCase(target.charAt(k))) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean endsWithIgnoreCase(String source, String target) {
		int targetCount = target.length();
		if (targetCount == 0) {
			return true;
		}
		
		int sourceCount = source.length();
		if (sourceCount < targetCount) {
			return false;
		}
		
		for (int j = sourceCount - targetCount, k = 0; k < targetCount; ++j, ++k) {
			if (source.charAt(j) != target.charAt(k) && Character.toUpperCase(source.charAt(j)) != Character.toUpperCase(target.charAt(k))) {
				return false;
			}
		}
		
		return true;
	}
}