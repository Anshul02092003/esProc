package com.scudata.expression.fn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * replace(src,a,b) ���ַ���src�����ַ���a��Ϊ�ַ���b
 * @author runqian
 *
 */
public class Replace extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.missingParam"));
		}
		
		if (param.getSubSize() != 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		IParam sub3 = param.getSub(2);
		if (sub1 == null || sub2 == null || sub3 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.invalidParam"));
		}
		
		Object str1 = sub1.getLeafExpression().calculate(ctx);
		if (str1 == null) {
			return null;
		} else if (!(str1 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.paramTypeError"));
		}

		Object str2 = sub2.getLeafExpression().calculate(ctx);
		if (str2 == null) {
			return null;
		} else if (!(str2 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.paramTypeError"));
		}

		Object str3 = sub3.getLeafExpression().calculate(ctx);
		if (str3 == null) {
			return null;
		} else if (!(str3 instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("replace" + mm.getMessage("function.paramTypeError"));
		}
		
		int flag = Sentence.IGNORE_PARS;
		if (option != null) {
			if (option.indexOf('q') == -1) {
				//����������ַ�Ҳ��Ҫ�任
				flag += Sentence.IGNORE_QUOTE;
			}
			
			if (option.indexOf('1') != -1) {
				// ֻ�滻��һ��
				flag += Sentence.ONLY_FIRST;
			}
			
			if (option.indexOf('c') != -1) {
				// ���Դ�Сд
				flag += Sentence.IGNORE_CASE;
			}
		} else {
			flag += Sentence.IGNORE_QUOTE;
		}
		
		return Sentence.replace((String)str1, 0, (String)str2, (String)str3, flag);
	}
}
