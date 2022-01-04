package com.scudata.expression.fn.convert;

import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���������������ת�����ַ���
 * string(expression{, format}:loc) 
 * ���������������ת�����ַ��ͣ�ת�������п��Ը�ʽ����locΪ���ԣ���������ʱ�����ã�ȱʡʹ��ϵͳ���ԡ�
 * @author runqian
 *
 */
public class ToString extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("string"+mm.getMessage("function.missingParam"));
		}
		
		Object val;
		String fmt = null;
		String locale = null;
		
		if (param.isLeaf()) {
			val = param.getLeafExpression().calculate(ctx);
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("string" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("string" + mm.getMessage("function.invalidParam"));
			}
			
			val = sub0.getLeafExpression().calculate(ctx);
			if (sub1.isLeaf()) {
				Object obj = sub1.getLeafExpression().calculate(ctx);
				if (obj instanceof String) {
					fmt = (String)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("string" + mm.getMessage("function.paramTypeError"));
				}
			} else {
				IParam fmtParam = sub1.getSub(0);
				IParam locParam = sub1.getSub(1);
				if (fmtParam == null || locParam == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("string" + mm.getMessage("function.invalidParam"));
				}

				Object obj = fmtParam.getLeafExpression().calculate(ctx);
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("string" + mm.getMessage("function.paramTypeError"));
				}
				
				fmt = (String)obj;
				obj = locParam.getLeafExpression().calculate(ctx);
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("string" + mm.getMessage("function.paramTypeError"));
				}
				
				locale = (String)obj;
			}
		}
		
		if (val instanceof String) {
			String str = (String)val;
			String opt = this.option;
			if (opt != null) {
				if (opt.indexOf('u') != -1) {
					StringBuffer sb = new StringBuffer(str.length() + 16);
					StringUtils.deunicode(str, sb, "\"'");
					str = sb.toString();
				} else if (opt.indexOf('e') != -1) {
					str = Escape.add(str);
				}
				
				if (opt.indexOf('q') != -1) {
					str = '"' + str + '"';
				}
			}
			
			return str;
		} else {
			if (fmt == null) {
				return Variant.toString(val);
			} else if (locale == null) {
				return Variant.format(val, fmt);
			} else {
				return Variant.format(val, fmt, locale);
			}
		}
	}
}
