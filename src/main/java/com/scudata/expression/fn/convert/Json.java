package com.scudata.expression.fn.convert;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.JSONUtil;

/**
 * json(x) ��x��json��ʽ��ʱ����x����������أ���x�Ǽ�¼������ʱ��������json��ʽ�����ء�
 * @author runqian
 *
 */
public class Json extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("json" + mm.getMessage("function.invalidParam"));
		}

		Object val = param.getLeafExpression().calculate(ctx);
		if (val == null) {
			return null;
		} else if (val instanceof String) {
			char[] chars = ((String)val).toCharArray();
			return JSONUtil.parseJSON(chars, 0, chars.length - 1);
		} else if (val instanceof Sequence) {
			return JSONUtil.toJSON((Sequence)val);
		} else if (val instanceof Record) {
			StringBuffer sb = new StringBuffer(1024);
			JSONUtil.toJSON(val, sb);
			return sb.toString();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("json" + mm.getMessage("function.paramTypeError"));
		}
	}
}
