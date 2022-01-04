package com.scudata.expression.mfn.file;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.FileFunction;
import com.scudata.resources.EngineMessage;

/**
 * �������ļ���ȡ��ָ�����Է��أ�ʡ���򷵻����������γ����
 * f.property(p)
 * @author RunQian
 *
 */
public class Property extends FileFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			try {
				return file.getProperties(option);
			} catch( Exception e) {
				throw new RQException(e.getMessage(), e);
			}
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("property" + mm.getMessage("function.paramTypeError"));
			}

			try {
				return file.getProperty((String)obj, option);
			} catch( Exception e) {
				throw new RQException(e.getMessage(), e);
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("property" + mm.getMessage("function.invalidParam"));
		}
	}
}
