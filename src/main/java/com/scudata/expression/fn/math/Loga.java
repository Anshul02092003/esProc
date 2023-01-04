package com.scudata.expression.fn.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ����ָ���ĵ���������һ�����Ķ�����log(number,base), ��baseʡ����Ϊ10
 * @author yanjing
 *
 */
public class Loga extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("lg" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object result1=null;
		Object result2=null;
		if(param.isLeaf()){
			result1 = param.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("lg" + mm.getMessage("function.invalidParam"));
			}
		}else{
			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("lg" + mm.getMessage("function.invalidParam"));
			}
			result1 = sub1.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The first param of lg" + mm.getMessage("function.paramTypeError"));
			}
			if(sub2!=null){
				result2 = sub2.getLeafExpression().calculate(ctx);
				if (result2 != null && !(result2 instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("The second param of lg" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		double n=Variant.doubleValue(result1);
		double b=10;
		if (result2 != null) {
			b=Variant.doubleValue(result2);
		}
		return new Double(Math.log(n)/Math.log(b));
	}

}
