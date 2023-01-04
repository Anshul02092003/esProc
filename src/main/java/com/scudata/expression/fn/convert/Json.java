package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.JSONUtil;

/**
 * json(x) ��x��json��ʽ��ʱ����x����������أ���x�Ǽ�¼������ʱ��������json��ʽ�����ء�
 * @author runqian
 *
 */
public class Json extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("json" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("json" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object val = param.getLeafExpression().calculate(ctx);
		if (val instanceof String) {
			if (option == null || option.indexOf('v') == -1) {
				char[] chars = ((String)val).toCharArray();
				return JSONUtil.parseJSON(chars, 0, chars.length - 1, option);
			} else {
				Expression exp = new Expression(cs, ctx, (String)val);
				return exp.calculate(ctx);
			}
		} else if (val instanceof Sequence) {
			return JSONUtil.toJSON((Sequence)val);
		} else if (val instanceof BaseRecord) {
			return JSONUtil.toJSON((BaseRecord)val);
		} else if (val == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("json" + mm.getMessage("function.paramTypeError"));
		}
	}
}
