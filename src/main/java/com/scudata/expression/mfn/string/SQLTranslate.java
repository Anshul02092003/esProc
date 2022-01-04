package com.scudata.expression.mfn.string;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.sql.SQLUtil;
import com.scudata.expression.StringFunction;
import com.scudata.resources.EngineMessage;

/**
 * ����׼SQL�еĺ��������ָ�����ݿ�ĸ�ʽ�����ձ���������
 * sql.sqltranslate(dbtype)
 * @author RunQian
 *
 */
public class SQLTranslate extends StringFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sqltranslate" + mm.getMessage("function.missingParam"));
		}
		
		Object obj = param.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sqltranslate" + mm.getMessage("function.paramTypeError"));
		}
		
		if (srcStr == null || srcStr.length() == 0) return null;
		
		//int type = DBTypes.getDBType((String)obj);
		return SQLUtil.translate(srcStr, (String)obj);
	}
}