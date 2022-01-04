package com.scudata.expression.fn.financial;

import java.util.Date;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * @author yanjing
 * 
 * Fduration(settlement,maturity;coupon,yld) ���ض��ڸ�Ϣ�м�֤ȯ��������
 *   �ޣ�����֧����frequency = 1��
 *   @2 ��������֧����frequency = 2��
 *   @4 ����֧����frequency = 4
 *   
 * 	 @e 30/360, 
 * 	 @1 ʵ������/��ʵ��������
 * 	 @0 ʵ������/360�� 
 * 	 @5 ʵ������/365��
 * 	 ȱʡΪ30/360 
 * 
 * @param settlement ֤ȯ�Ľ�����
 * @param maturity  ֤ȯ�ĵ�����
 * @param coupon ��ϢƱ����
 * @param yld ��������
 */
public class Duration extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		if(param==null || param.isLeaf() || param.getSubSize()<4){
			throw new RQException("Fduration:" +
									  mm.getMessage("function.missingParam"));
		}
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if(result[i]==null){
					throw new RQException("The "+i+"th param of Fduration:" + mm.getMessage("function.paramValNull"));
				}
				if (i<=1 && !(result[i] instanceof Date)) {
					throw new RQException("The "+i+"th param of Fduration:" + mm.getMessage("function.paramTypeError"));
				}
				if (i>1 && !(result[i] instanceof Number)) {
					throw new RQException("The "+i+"th param of Fduration:" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		Date settlement=(Date)result[0];
		Date maturity=(Date)result[1];
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of FCoups should be later than settlement");
		}
		double coupon=Variant.doubleValue(result[2]);
		double yld=Variant.doubleValue(result[3]);
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
		
		return duration(maturity, settlement,coupon,yld, frequency, basis);
		
	}

	private Object duration(Date maturity,Date settlement,double coupon,double yld,double frequency,int basis){
		
		double m=Price.interval(settlement, maturity,basis, 'm');//�����պ͵�����֮�������
		double z=12/frequency;//ÿ����Ϣ�ڵ�����
		double T=Math.ceil(m/z);//�����պ͵�����֮��ĸ�Ϣ����
		
		double c=coupon/frequency;
		double y=yld/frequency;
		
		return new Double((1+1/y-(1+y+T*(c-y))/(c*Math.pow(1+y, T)-c+y))/frequency);
	}

	
}
