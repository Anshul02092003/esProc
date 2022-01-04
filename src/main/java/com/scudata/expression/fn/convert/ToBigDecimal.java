package com.scudata.expression.fn.convert;

import java.math.BigDecimal;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���ַ�������ֵ�͵���ֵת���ɴ󸡵���
 * decimal(stringExp) ����stringExp�����������ֺ�С������ɵ��ַ�����
 * decimal(numberExp) ����numberExpֻ�����ڵ���64λ������64λ��Ҫ���ַ���stringExp ����numberExp��
 * @author runqian
 *
 */
public class ToBigDecimal extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("decimal" + mm.getMessage("function.invalidParam"));
		}
		
		Object result = param.getLeafExpression().calculate(ctx);
		if (result == null) {
			return null;
		} else if (result instanceof String) {
			try {
				return new BigDecimal((String)result);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return Variant.toBigDecimal(result);
		}
	}
}
