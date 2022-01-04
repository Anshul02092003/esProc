package com.scudata.expression.fn.financial;

import java.util.Date;

import com.scudata.common.DateFactory;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * �����м�֤ȯ��Ӧ����Ϣ��
 * @author yanjing
 * Faccrintm(settlement,issue;rate,par)  ����һ���Ը�Ϣ֤ȯ
 * 
 * @param issue �м�֤ȯ�ķ�����
 * @param settlement Ϊ�м�֤ȯ�ĵ�����/֤ȯ�Ľ�����
 * @param rate  �м�֤ȯ����ϢƱ����
 * @param par Ϊ�м�֤ȯ��Ʊ���ֵ�����ʡ�� par���� par Ϊ ��1,000��
 *   
 * 	 @e 30/360, 
 * 	 @1 ʵ������/��ʵ��������
 * 	 @0 ʵ������/360�� 
 * 	 @5 ʵ������/365��
 * 	 ȱʡΪ30/360
 * @param calc_method �߼�ֵ��ָ�����������������״μ�Ϣ����ʱ���ڼ�����Ӧ����Ϣ�ķ�����
 *                    ���ֵΪ TRUE (1)���򷵻شӷ����յ������յ���Ӧ����Ϣ��
 *                    ���ֵΪ FALSE (0)���򷵻ش��״μ�Ϣ�յ������յ�Ӧ����Ϣ��ȱʡΪ TRUE��
 * @return
 * 
 * 
 */
public class Accrintm extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Faccrintm:" +
									  mm.getMessage("function.missingParam"));
		}
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
			}
		}
		return accrintm(result);

	}
	
	private Double accrintm(Object[] result){
		Date issue;
		Date settlement;
		double rate;
		double par=1000;
		long basis=0;
		MessageManager mm = EngineMessage.get();
		if(result[0]==null || result[1]==null || result[2]==null){
			throw new RQException("The first four params of Faccrintm:" + mm.getMessage("function.paramValNull"));
		}
		else{
			for(int i=0;i<=1;i++){
				if (!(result[i] instanceof Date)) {
					throw new RQException("The "+i+"th param of Faccrintm:" + mm.getMessage("function.paramTypeError"));
				}
			}
			settlement=(Date)result[0];
			issue=(Date)result[1];
			
			if (!(result[2] instanceof Number)) {
				throw new RQException("The third param of Faccrintm:" + mm.getMessage("function.paramTypeError"));
			}
			rate=Variant.doubleValue(result[2]);
		}
		if(result[3]!=null) par=Variant.doubleValue(result[3]);
		
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		long realdays=0;
		if(basis==0 || basis==4){
			realdays=Variant.interval(issue, settlement, "m")*30;
			realdays=realdays-(DateFactory.get().day(issue)-DateFactory.get().day(settlement));
		}
		else realdays=Variant.interval(issue, settlement, "d");
		
		double ydays=360;
		if(basis==0 || basis==2 || basis==4){
			ydays=360;
		}
		else if(basis==3) ydays=365;
		else ydays=DateFactory.get().daysInYear((Date)settlement);
		return new Double(par*rate*realdays/ydays);
	}

}
