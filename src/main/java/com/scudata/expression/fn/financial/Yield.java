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

import java.util.Calendar;


/**
 * @author yanjing
 * 
 * Fyield(settlement,maturity;rate,pr,redemption) ���ض��ڸ�Ϣ�м�֤ȯ��������
 * Fyield@d(settlement,maturity;0,pr,redemption) �����ۼ۷��е��м�֤ȯ����������
 * Fyield@m(settlement,maturity,issue;rate,pr) ���ص��ڸ�Ϣ�м�֤ȯ����������
 *   �ޣ�����֧����frequency = 1��
 *   @2 ��������֧����frequency = 2��
 *   @4 ����֧����frequency = 4
 *   
 * 	 @e 30/360, 
 * 	 @1 ʵ������/��ʵ��������
 * 	 @0 ʵ������/360�� 
 * 	 @5 ʵ������/365��
 * 	 ȱʡΪ30/360* * 
 * 
 * @param settlement ֤ȯ�Ľ�����
 * @param maturity  ֤ȯ�ĵ�����
 * @param issue ������
 * @param Rate Ϊ�м�֤ȯ�����ʡ�
 * @param Pr Ϊ��ֵ ��100 ���м�֤ȯ�ļ۸�
 * @param Redemption Ϊ��ֵ ��100 ���м�֤ȯ���峥��ֵ�� 
 */
public class Yield extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<5){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fyield:" +
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
		if(option==null || (option.indexOf('d')==-1 && option.indexOf('m')==-1)){
			return yield(result);
		}else if(option.indexOf('d')>=0){
			return yielddisc(result);
		}else{
			return yieldMAT(result);
		}
	}

	private Object yield(Object[] result){
		Date settlement;
		Date maturity;
		double rate;
		double pr;
		double redemption;
		int frequency;
		int basis=0;
		
		int size=result.length;
		for(int i=0;i<size;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield:" + mm.getMessage("function.paramValNull"));
			}
		}
		for(int i=0;i<=1;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fyield should be later than settlement");
		}
		for(int i=2;i<size;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield:" + mm.getMessage("function.paramTypeError"));
			}
		}
		rate=Variant.doubleValue(result[2]);
		pr=Variant.doubleValue(result[3]);
		redemption=Variant.doubleValue(result[4]);
		if(rate<0 || pr<=0 || redemption<=0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The rate,pr or redemption of Fyield:" + mm.getMessage("function.valueNoSmall"));
		}
		if(option!=null && option.indexOf("2")>=0) frequency=2;
		else if(option!=null && option.indexOf("4")>=0) frequency=4;
		else frequency=1;
		
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		double m=Price.interval(settlement, maturity,basis, 'm');//�����պ͵�����֮�������
		int z=12/frequency;//ÿ����Ϣ�ڵ�����
		int n=new Double(Math.ceil(m/z)).intValue();//�����պ͵�����֮��ĸ�Ϣ����
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(maturity);
		calendar.add(Calendar.MONTH, -n*z);
		Date end=new Date();//��һ����Ϣ��
		end.setTime(calendar.getTimeInMillis());
		Date start=new Date();//��һ����Ϣ��
		if(Variant.compare(settlement, end)==1){
			start.setTime(end.getTime());
			calendar.add(Calendar.MONTH, z);
			end.setTime(calendar.getTimeInMillis());
		}
		else{
			calendar.add(Calendar.MONTH, -z);
			start.setTime(calendar.getTimeInMillis());
		}
		double dsr=Price.interval(settlement, maturity, basis,'d');

		double E=Price.interval(start, end, basis,'d');
		
		double A=Price.interval(start,settlement, basis,'d');
		if(n<=1){
			return new Double((redemption/100.0+rate/frequency-pr/100.0-A*rate/E/frequency)*frequency*E/dsr/(pr/100.0+A*rate/E/frequency));
		}
		long dsc=Price.interval(settlement,end,basis,'d');//����������һ��Ϣ��֮�������
		double lguess=0.1;
		double guess=0.1;
		double lvalue=0;
		double step=0.01;
		for(int i=1;i<=100;i++){
			double tmp1=redemption/Math.pow(1.0+guess/frequency, n-1.0+dsc/E);
			double tmp2=0;
			for(double k=1;k<=n;k++){
				tmp2+=100.0*rate/frequency/Math.pow(1+guess/frequency, k-1.0+dsc/E);
			}
			double tmp3=100.0*rate*A/frequency/E;
			double value=tmp1+tmp2-tmp3-pr;
			if(value<0.0000001 && value>-0.0000001) break;
			else if((lvalue>0.0000001 && value<-0.0000001) || (lvalue<-0.0000001 && value>0.0000001)){
				double temp1=value;
				double temp2=guess;
				if(value>lvalue){
					double tmp=value;
					value=lvalue;
					lvalue=tmp;
					tmp=guess;
					guess=lguess;
					lguess=tmp;
				}
				guess= lvalue*(guess-lguess)/(lvalue-value)+lguess;
				step=step/10;
				lvalue=temp1;
				lguess=temp2;
				continue;
			}
			else if(value>0.0000001){ 
				lguess=guess;
				lvalue=value;
				guess+=step;
			}
			else if(value<-0.0000001){ 
				lguess=guess;
				lvalue=value;
				guess-=step;
			}
			if(guess==-1){ 
				guess+=step/2;
				step=step/10;
			}
		}
		return new Double(guess);
	}

	private Object yielddisc(Object[] result){
		Date settlement;
		Date maturity;
		double pr;
		double redemption;
		long basis=0;
		
		int size=result.length;
		for(int i=0;i<size;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@d:" + mm.getMessage("function.paramValNull"));
			}
		}
		for(int i=0;i<=1;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@d:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fyield@d should be later than settlement");
		}
		for(int i=2;i<size;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@d:" + mm.getMessage("function.paramTypeError"));
			}
		}
		pr=Variant.doubleValue(result[3]);
		redemption=Variant.doubleValue(result[4]);
		
		if(pr<=0 || redemption<=0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The pr or redemption of Fyield@d:" + mm.getMessage("function.valueNoSmall"));
		}
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		long m=Variant.interval(settlement, maturity, "m");//�����պ͵�����֮�������
		double B=360;
		double DSM;//�����պ͵�����֮�������
		
		if(basis==0 || basis==4){
			DSM=30.0*m+DateFactory.get().day(maturity)-DateFactory.get().day(settlement);
		}
		else DSM=Variant.interval(settlement, maturity, null);
		if(basis==3){
			B=365;
		}
		else if(basis==1){
			B=DateFactory.get().daysInYear(maturity);
		}
		return new Double((redemption-pr)*B/DSM/pr);
	}

	private Object yieldMAT(Object[] result){
		Date settlement;
		Date maturity;
		Date issue;
		double rate;
		double pr;
		long basis=0;
		
		int size=result.length;
		for(int i=0;i<size;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@m:" + mm.getMessage("function.paramValNull"));
			}
		}
		for(int i=0;i<=2;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@m:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		issue=(Date)result[2];
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fyield@m should be later than settlement");
		}
		for(int i=3;i<size;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fyield@m:" + mm.getMessage("function.paramTypeError"));
			}
		}
		rate=Variant.doubleValue(result[3]);
		pr=Variant.doubleValue(result[4]);
		
		if(rate<0 || pr<=0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The rate or pr of Fyield@m:" + mm.getMessage("function.valueNoSmall"));
		}
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		long m=Variant.interval(settlement, maturity, "m");//�����պ͵�����֮�������
		double B=360;
		double DSM;//�����պ͵�����֮�������
		
		if(basis==0 || basis==4){
			DSM=30.0*m+DateFactory.get().day(maturity)-DateFactory.get().day(settlement);
		}
		else DSM=Variant.interval(settlement, maturity, null);
		if(basis==3){
			B=365;
		}
		else if(basis==1){
			B=DateFactory.get().daysInYear(maturity);
		}
		m=Variant.interval(issue, maturity, "m");
		double DIM;//�����պ͵�����֮�������
		if(basis==0 || basis==4){
			DIM=30.0*m+DateFactory.get().day(maturity)-DateFactory.get().day(issue);
		}
		else DIM=Variant.interval(issue, maturity, null);
		double A;
		m=Variant.interval(issue, settlement, "m");
		if(basis==0 || basis==4){
			A=30.0*m+DateFactory.get().day(settlement)-DateFactory.get().day(issue);
		}
		else A=Variant.interval(issue, settlement, null);

		return new Double(((100.0+DIM*rate*100.0/B)/(pr+A*rate*100.0/B)-1.0)*B/DSM);
	}

}
