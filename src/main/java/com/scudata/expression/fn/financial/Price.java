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
 * Fprice(settlement,maturity;rate,yld,redemption) ���ض��ڸ�Ϣ����ֵ ��100 ���м�֤ȯ�ļ۸�
 * Fprice@d(settlement,maturity;discount,0,redemption) �����ۼ۷��е���ֵ ��100 ���м�֤ȯ�ļ۸�
 * Fprice@m(settlement,maturity,issue;rate,yld) ���ص��ڸ�Ϣ����ֵ ��100 ���м�֤ȯ�ļ۸�
 *   �ޣ�����֧����frequency = 1��
 *   @2 ��������֧����frequency = 2��
 *   @4 ����֧����frequency = 4
 *   
 * 	 @e 30/360, 
 * 	 @1 ʵ������/��ʵ��������
 * 	 @0 ʵ������/360�� 
 * 	 @5 ʵ������/365��
 * 	 ȱʡΪ30/360* 
 * @param settlement ֤ȯ�Ľ�����
 * @param maturity  ֤ȯ�ĵ�����
 * @param Rate Ϊ�м�֤ȯ�����ʡ�
 * @param Yld Ϊ�м�֤ȯ���������ʡ�
 * @param discount Ϊ�м�֤ȯ��������
 * @param issue ������
 * @param Redemption Ϊ��ֵ ��100 ���м�֤ȯ���峥��ֵ��
 */
public class Price extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		if(param==null || param.isLeaf() || param.getSubSize()<5){
			throw new RQException("Fprice:" +
									  mm.getMessage("function.missingParam"));
		}
		
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if(result[i]==null){
					throw new RQException("The "+i+"th param of Fprice:" + mm.getMessage("function.paramValNull"));
				}
			}
		}
		if(option==null || (option.indexOf('d')==-1 && option.indexOf('m')==-1)){
			return price(result);
		}else if(option.indexOf('d')>=0){
			return pricedisc(result);
		}else{
			return priceMAT(result);
		}
	}
	
	/**
	 * price(settlement,maturity,rate,yld,redemption,frequency,basis) ���ض��ڸ�Ϣ����ֵ ��100 ���м�֤ȯ�ļ۸�
	 * 
	 * 
	 * @param settlement ֤ȯ�Ľ�����
	 * @param maturity  ֤ȯ�ĵ�����
	 * @param Rate Ϊ�м�֤ȯ�����ʡ�
	 * @param Yld Ϊ�м�֤ȯ���������ʡ�
	 * @param Redemption Ϊ��ֵ ��100 ���м�֤ȯ���峥��ֵ��
	 * @param frequency Ϊ�긶Ϣ�������������֧����frequency = 1����������֧����frequency = 2��
	 *                               ����֧����frequency = 4
	 * @param basis �ռ�����׼����,0/4 30/360, 1 ʵ������/��ʵ��������2 ʵ������/360�� 3 ʵ������/365��ȱʡΪ0
	 * 
	 * @return
	 */
	private Object price(Object[] result){
		Date settlement;
		Date maturity;
		double rate;
		double yld;
		double redemption;
		int frequency;
		int basis=0;

		for(int i=0;i<=1;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fprice should be later than settlement");
		}
		for(int i=2;i<5;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice:" + mm.getMessage("function.paramTypeError"));
			}
		}
		rate=Variant.doubleValue(result[2]);
		yld=Variant.doubleValue(result[3]);
		redemption=Variant.doubleValue(result[4]);
		if(rate<0 || yld<0 || redemption<=0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The rate,yld or redemption of Fprice:" + mm.getMessage("function.valueNoSmall"));
		}
		
		if(option!=null && option.indexOf("2")>=0) frequency=2;
		else if(option!=null && option.indexOf("4")>=0) frequency=4;
		else frequency=1;
		
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		double m=interval(settlement, maturity,basis, 'm');//�����պ͵�����֮�������
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
		double dsc=interval(settlement, end, basis,'d');

		double E=interval(start, end, basis,'d');
		
		double A=interval(start,settlement, basis,'d');
		double n1=n;
		double tmp1=redemption/Math.pow(1.0+yld/frequency, n1-1.0+dsc/E);
		double tmp2=0;
		for(double k=1.0;k<=n1;k++){
			tmp2+=100.0*rate/frequency/Math.pow(1.0+yld/frequency, k-1.0+dsc/E);
		}
		double tmp3=100.0*rate*A/frequency/E;
		return new Double(tmp1+tmp2-tmp3);
	}

	private Object pricedisc(Object[] result){
		Date settlement;
		Date maturity;
		double discount;
		double redemption;
		long basis=0;
		
		for(int i=0;i<=1;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice@d:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fprice@d should be later than settlement");
		}
		for(int i=2;i<4;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice@d:" + mm.getMessage("function.paramTypeError"));
			}
		}
		discount=Variant.doubleValue(result[2]);
		redemption=Variant.doubleValue(result[4]);
		
		if(discount<=0 || redemption<=0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The discount or redemption of Fprice@d:" + mm.getMessage("function.valueNoSmall"));
		}
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		long m=Variant.interval(settlement, maturity, "m");//�����պ͵�����֮�������
		long B=360;
		long DSM;
		
		if(basis==0 || basis==4){
			DSM=30*m+DateFactory.get().day(maturity)-DateFactory.get().day(settlement);
		}
		else DSM=Variant.interval(settlement, maturity, null);
		if(basis==3){
			B=365;
		}
		else if(basis==1){
			B=DateFactory.get().daysInYear(maturity);
		}
		return new Double(redemption-discount*redemption*DSM/B);
	}
	/**
	 * ���ص��ڸ�Ϣ����ֵ ��100 ���м�֤ȯ�ļ۸�
	 * price@m(settlement,maturity,issue,rate,yld,basis)
	 * @param settlement ֤ȯ�Ľ�����
	 * @param maturity  ֤ȯ�ĵ�����
	 * @param issue ������
	 * @param Rate Ϊ�м�֤ȯ�����ʡ�
	 * @param Yld Ϊ�м�֤ȯ���������ʡ�
	 * @param basis �ռ�����׼����,0/4 30/360, 1 ʵ������/��ʵ��������2 ʵ������/360�� 3 ʵ������/365��ȱʡΪ0
	 
	 * @return
	 */
	private Object priceMAT(Object[] result){
		Date settlement;
		Date maturity;
		Date issue;
		double rate;
		double yld;
		long basis=0;
		
		for(int i=0;i<=2;i++){
			if (!(result[i] instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice@m:" + mm.getMessage("function.paramTypeError"));
			}
		}	
		settlement=(Date)result[0];
		maturity=(Date)result[1];
		issue=(Date)result[2];
		if(Variant.compare(settlement, maturity)==1){
			throw new RQException("The maturity of Fprice@m should be later than settlement");
		}
		for(int i=3;i<5;i++){
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fprice@m:" + mm.getMessage("function.paramTypeError"));
			}
		}
		rate=Variant.doubleValue(result[3]);
		yld=Variant.doubleValue(result[4]);
		
		if(rate<0 || yld<0){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The rate or yld of Fprice@m:" + mm.getMessage("function.valueNoSmall"));
		}
		
		if(option!=null && option.indexOf("1")>=0) basis=1;
		else if(option!=null && option.indexOf("0")>=0) basis=2;
		else if(option!=null && option.indexOf("5")>=0) basis=3;
		else if(option!=null && option.indexOf("e")>=0) basis=4;
		else basis=0;
		
		long m=Variant.interval(settlement, maturity, "m");//�����պ͵�����֮�������
		long B=360;
		long DSM;
		
		if(basis==0 || basis==4){
			DSM=30*m+DateFactory.get().day(maturity)-DateFactory.get().day(settlement);
		}
		else DSM=Variant.interval(settlement, maturity, null);
		if(basis==3){
			B=365;
		}
		else if(basis==1){
			B=DateFactory.get().daysInYear(maturity);
		}
		m=Variant.interval(issue, maturity, "m");
		long DIM;
		if(basis==0 || basis==4){
			DIM=30*m+DateFactory.get().day(maturity)-DateFactory.get().day(issue);
		}
		else DIM=Variant.interval(issue, maturity, null);
		long A;
		m=Variant.interval(issue, settlement, "m");
		if(basis==0 || basis==4){
			A=30*m+DateFactory.get().day(settlement)-DateFactory.get().day(issue);
		}
		else A=Variant.interval(issue, settlement, null);
		double tmp1=100+DIM*rate*100/B;
		double tmp2=1+DSM*yld/B;
		double tmp3=A*rate*100/B;
		return new Double(tmp1/tmp2-tmp3);
	}
	public static long interval(Date date1,Date date2,int basis,char mark){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date1);
		int m1=calendar.get(Calendar.MONTH)+1;
		int y1=calendar.get(Calendar.YEAR);
		int d1=calendar.get(Calendar.DAY_OF_MONTH);
		calendar.setTime(date2);
		int m2=calendar.get(Calendar.MONTH)+1;
		int y2=calendar.get(Calendar.YEAR);
		int d2=calendar.get(Calendar.DAY_OF_MONTH);
		if(mark=='d'){
			if(basis==0 || basis==4){  //ÿ�°�30����
				
				return ((y2-y1)*12+m2-m1)*30+d2-d1;
			}else{  //��ʵ��������
				long day = 24 * 3600 * 1000;
				long interval = date2.getTime() / day - date1.getTime() / day;
				return interval;
			}
		}else if(mark=='m'){
			int m=(y2-y1)*12+m2-m1;
			if(d1>d2) m-=1;
			return m;
		}
		return 0;
	}
}
