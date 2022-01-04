package com.scudata.expression.fn.financial;

import java.util.Date;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

import java.util.Calendar;


/**
 * �����ڽ�����֮����һ����Ϣ�յ�����
 * @author yanjing
 * 
 * coupncd(settlement,maturity,frequency,basis) 
 * 
 * @param settlement ֤ȯ�Ľ�����
 * @param maturity  ֤ȯ�ĵ�����
 * @param frequency Ϊ�긶Ϣ�������������֧����frequency = 1����������֧����frequency = 2��
 *                               ����֧����frequency = 4
 * @param basis �ռ�����׼����,0/4 30/360, 1 ʵ������/��ʵ��������2 ʵ������/360�� 3 ʵ������/365��ȱʡΪ0
 * @return
 * 
 * 
 */
public class Coupcd extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		if(param==null || param.isLeaf()){
			throw new RQException("Fcoupcd:" +
									  mm.getMessage("function.missingParam"));
		}
		
		Object[] result=new Object[2];
		for(int i=0;i<2;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if(result[i]==null){
					throw new RQException("The "+i+"th param of Fcoupcd:" + mm.getMessage("function.paramValNull"));
				}
				if (!(result[i] instanceof Date)) {
					throw new RQException("The "+i+"th param of Fcoupcd:" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		Date settlement=(Date)result[0];
		Date maturity=(Date)result[1];
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fcoupcd should be later than settlement");
		}
		double frequency=1;
		int basis=0;
		
		if(option!=null && option.indexOf("2")>=0) frequency=2;
		else if(option!=null && option.indexOf("4")>=0) frequency=4;
		else frequency=1;
		
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		if(option==null || option.indexOf("p")<0)
			return coupncd(maturity, settlement, frequency, basis);
		else
			return couppcd(maturity, settlement, frequency, basis);
	}
	
	private Object coupncd(Date maturity,Date settlement,double frequency,int basis){
		
		long m=Variant.interval(settlement, maturity, "m");
		int z=new Double(12/frequency).intValue();
		int n=new Long(m/z).intValue();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(maturity);
		calendar.add(Calendar.MONTH, -n*z);
		Date end=new Date();
		end.setTime(calendar.getTimeInMillis());
		Date start=new Date();
		if(Variant.compare(settlement, end)==1){
			start=end;
			calendar.add(Calendar.MONTH, z);
			end.setTime(calendar.getTimeInMillis());
		}
		else{
			calendar.add(Calendar.MONTH, -z);
			start.setTime(calendar.getTimeInMillis());
		}
		
		return end;
	}
	private Object couppcd(Date maturity,Date settlement,double frequency,int basis){
		
		long m=Variant.interval(settlement, maturity, "m");
		int z=new Double(12/frequency).intValue();
		int n=new Long(m/z).intValue();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(maturity);
		calendar.add(Calendar.MONTH, -n*z);
		Date end=new Date();
		end.setTime(calendar.getTimeInMillis());
		Date start=new Date();
		if(Variant.compare(settlement, end)==1){
			start=end;
			calendar.add(Calendar.MONTH, z);
			end.setTime(calendar.getTimeInMillis());
		}
		else{
			calendar.add(Calendar.MONTH, -z);
			start.setTime(calendar.getTimeInMillis());
		}
		
		return start;
	}
}
