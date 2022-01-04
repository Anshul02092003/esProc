package com.scudata.expression.mfn.xo;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.XOFunction;
import com.scudata.resources.EngineMessage;

/**
 * ����xo.xlsclose()�� ��@r@w��ʽ�򿪵�Excel������Ҫ�ر�
 *
 */
public class XlsClose extends XOFunction {
	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		if (param != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsclose"
					+ mm.getMessage("function.invalidParam"));
		}
		try {
			file.xlsclose();
			return null;
		} catch (RQException e) {
			throw e;
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
}
