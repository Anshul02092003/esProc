package com.scudata.expression.fn;

import java.sql.Timestamp;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����̨�����ӡ��Ϣ
 * output@g@t(x,��) �����һ������������Ϣ�����ʱ�ö��Ÿ���������̨����Ĵ�ӡ��Ϣ��Tab�ָ���
 * @author runqian
 *
 */
public class Output extends Function {
	public Node optimize(Context ctx) {
		param.optimize(ctx);
		return this;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("output" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		Expression []exps = getParamExpressions("output", false);
		String msg = null;
		for (int i = 0, size = exps.length; i < size; ++i) {
			Object obj = exps[i].calculate(ctx);
			if (i == 0) {
				msg = Variant.toString(obj);
			} else {
				msg += '\t' + Variant.toString(obj);
			}
		}
		
		boolean isTime = false, isLF = true;
		if (option != null) {
			if (option.indexOf('e') != -1) {
				Logger.error(msg);
				return null;
			} else if (option.indexOf('g') != -1) {
				if (option.indexOf('1') != -1) {
					Logger.debug(msg);
				} else if (option.indexOf('3') != -1) {
					Logger.warning(msg);
				} else if (option.indexOf('4') != -1) {
					Logger.severe(msg);
				} else {
					Logger.info(msg);
				}

				return null;
			}
			
			if (option.indexOf('t') != -1) isTime = true;
			if (option.indexOf('s') != -1) isLF = false;
		}
		
		if (isTime) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			msg = Variant.toString(time) + '\t' + msg;
		}

		if (isLF) {
			System.out.println(msg);
		} else {
			System.out.print(msg);
		}
		
		return null;
	}
}
