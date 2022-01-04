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
 * Faccrint@24105e(first_interest , settlement, issue; rate, par)
 * Faccrint(issue,first_interest,settlement,rate,par,frequency,basis,calc_method)  ���ڸ�Ϣ֤ȯ
 * 
 * @param issue �м�֤ȯ�ķ�����
 * @param first_interest	֤ȯ���״μ�Ϣ��
 * @param settlement Ϊ�м�֤ȯ�ĵ�����/֤ȯ�Ľ�����
 * @param rate  �м�֤ȯ����ϢƱ����
 * @param par Ϊ�м�֤ȯ��Ʊ���ֵ�����ʡ�� par���� par Ϊ ��1,000��
 *   �ޣ�����֧����frequency = 1��
 *   @2 ��������֧����frequency = 2��
 *   @4 ����֧����frequency = 4
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
public class Accrint extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		
		if(param==null || param.isLeaf() || param.getSubSize()<4){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Faccrint:" +
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
		return accrint(result);
	}
	private Object accrint(Object[] result){
		Date issue;
		Date first_interest;
		Date settlement;
		double rate;
		double par=1000;
		long frequency=1;
		long basis=0;
		MessageManager mm = EngineMessage.get();
		if(result[0]==null || result[1]==null || result[2]==null || result[3]==null){
			throw new RQException("The first four params of Faccrint:" + mm.getMessage("function.paramValNull"));
		}
		else{
			for(int i=0;i<=2;i++){
				if (!(result[i] instanceof Date)) {
					throw new RQException("The "+i+"th param of Faccrint:" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			first_interest=(Date)result[0];
			settlement=(Date)result[1];
			issue=(Date)result[2];
			if (!(result[3] instanceof Number)) {
				throw new RQException("The 4th param of Faccrint:" + mm.getMessage("function.paramTypeError"));
			}
			rate=Variant.doubleValue(result[3]);
		}
		int size=result.length;
		if(size>4 && result[4]!=null) par=Variant.doubleValue(result[4]);

		if(option!=null && option.indexOf("2")>=0) frequency=2;
		else if(option!=null && option.indexOf("4")>=0) frequency=4;
		else frequency=1;
		
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
			ydays=360/frequency;
		}
		else if(basis==3) ydays=365/frequency;
		else ydays=Variant.interval(issue, first_interest, "d")+1;
		return new Double(par*rate/frequency*realdays/ydays);
	}
	
}
