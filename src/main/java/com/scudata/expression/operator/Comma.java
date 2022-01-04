package com.scudata.expression.operator;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;

/**
 * �������,
 * a=1,b=3,c=6�������һ�����ʽ��ֵ
 * @author RunQian
 *
 */
public class Comma extends Operator {
	public Comma() {
		priority = PRI_CMA;
	}

  public Object calculate(Context ctx) {
	  if (left == null) {
		  MessageManager mm = EngineMessage.get();
		  throw new RQException("\",\"" + mm.getMessage("operator.missingLeftOperation"));
	  }
	  
	  if (right == null) {
		  MessageManager mm = EngineMessage.get();
		  throw new RQException("\",\"" + mm.getMessage("operator.missingRightOperation"));
	  }

	  left.calculate(ctx);
	  return right.calculate(ctx);
  }

  public byte calcExpValueType(Context ctx) {
	  if (right == null) {
		  MessageManager mm = EngineMessage.get();
		  throw new RQException("\",\"" +	mm.getMessage("operator.missingRightOperation"));
	  }
	  return right.calcExpValueType(ctx);
  }
}
