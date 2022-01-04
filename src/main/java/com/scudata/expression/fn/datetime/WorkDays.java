package com.scudata.expression.fn.datetime;

import java.util.Calendar;
import java.util.Date;

import com.scudata.common.DateFactory;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * workdays(b,e,h)
 * ��������b������e֮��Ĺ��������У�����b��e��h��(��)�������У���h�г�Ա������ĩ���Ǽ��գ�
 * ����ĩ��Ǽ��գ���Ϊ��ĩʱ�����ݼ��㣬��Ϊ������
 * @author runqian
 *
 */
public class WorkDays extends Function {
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}
	
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("workdays" + mm.getMessage("function.missingParam"));
		}

		int size = param.getSubSize();
		if (size != 2 && size != 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("workdays" + mm.getMessage("function.invalidParam"));
		}

		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("workdays" + mm.getMessage("function.invalidParam"));
		}

		Object result1 = sub1.getLeafExpression().calculate(ctx);
		Object result2 = sub2.getLeafExpression().calculate(ctx);
		if (!(result1 instanceof Date) || !(result2 instanceof Date)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("workdays" + mm.getMessage("function.paramTypeError"));
		}

		Sequence offDays = null;
		if (size == 3) {
			IParam sub3 = param.getSub(2);
			if (sub3 != null) {
				Object obj = sub3.getLeafExpression().calculate(ctx);
				if (obj instanceof Sequence) {
					offDays = (Sequence)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("workdays" + mm.getMessage("function.paramTypeError"));
				}
			}
		}

		Date date1 = (Date)result1;
		Date date2 = (Date)result2;
		
		if (!(date1 instanceof java.sql.Date)) {
			date1 = DateFactory.get().toDate(date1);
		}
		
		if (!(date2 instanceof java.sql.Date)) {
			date2 = DateFactory.get().toDate(date2);
		}
		
		Sequence seq = new Sequence();
		long time1 = date1.getTime();
		long time2 = date2.getTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time1);

		if (time1 <= time2) {
			while (time1 <= time2) {
				if (WorkDay.isWorkDay(calendar, offDays)) {
					seq.add(new java.sql.Date(calendar.getTimeInMillis()));
				}
				
				calendar.add(Calendar.DATE, 1);
				time1 = calendar.getTimeInMillis();
			}
		} else {
			while (time1 >= time2) {
				if (WorkDay.isWorkDay(calendar, offDays)) {
					seq.add(new java.sql.Date(calendar.getTimeInMillis()));
				}
				
				calendar.add(Calendar.DATE, -1);
				time1 = calendar.getTimeInMillis();
			}
		}
		
		return seq;
	}
}
