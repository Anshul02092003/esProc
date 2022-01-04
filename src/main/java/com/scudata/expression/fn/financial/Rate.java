package com.scudata.expression.fn.financial;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * 
 * @author yanjing
 * 
 * rate(nper,pmt,pv,fv,guess)  �������ĸ�������
 *  @t �ڳ�����,ʡ��Ϊ��ĩ����
 * @param Nper Ϊ�ܸ���������
 * @param pmt  Ϊÿ�ڵ�Ͷ�ʶ�, ����ֵ����������ڼ䱣�ֲ��䡣���ʡ�� pmt���������� pv/fv ������
 * @param Pv Ϊ��ֵ,�൱�ڳ��ڵ�Ͷ�ʡ����ʡ�� PV���������ֵΪ�㣬���ұ������ pmt ������
 * @param Fv Ϊδ��ֵ�������һ��֧����ʣ���Ͷ�ʶ����Ϊ��ĩ����׷�ӵ�Ͷ�ʶ���ʡ�� fv���������ֵΪ���ұ������ pmt ���������磬�����Ҫ�� 18 ��Ͷ���ڽ���ʱͶ�ʣ�50,000���� ��50,000 ����δ��ֵ��
 * @param guess Ϊ��������,ȱʡΪ10%,����20�κ�������û�������򱨴�
 * @return ��@p����δ��ֵ��@p������ֵ
 */
public class Rate extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			throw new RQException("Frate:" +
									  mm.getMessage("function.missingParam"));
		}
		
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if (result[i] != null && !(result[i] instanceof Number)) {
					throw new RQException("The "+i+"th param of Frate" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		return rate(result);
	}

	private Double rate(Object[] result){
		double nper=0;
		double pmt=0;
		double pv=0;
		double fv=0;
		double guess=0.1;
		double type=0;
		if(option!=null && option.indexOf("t")>=0) type=1;
		int size=result.length;
		if(result[0]!=null) nper=Variant.doubleValue(result[0]);
		if(result[1]!=null) pmt=Variant.doubleValue(result[1]);
		if(result[2]!=null) pv=Variant.doubleValue(result[2]);
		if(size>=4 && result[3]!=null) fv=Variant.doubleValue(result[3]);
		if(size>=5 && result[4]!=null) guess=Variant.doubleValue(result[4]);
		
		if(nper<=0) return new Double(0);
		int j;
		double lvalue=0;
		double lguess=guess;
		double step=0.01;
		for(j=1;j<=100;j++){
			double value=pv*Math.pow(1+guess, nper)+pmt*(1+guess*type)*((Math.pow(1+guess, nper)-1)/guess)+fv;
			if(value<0.0000001 && value>-0.0000001) break;
			else if((value>0.0000001 && lvalue<-0.0000001) || (value<-0.0000001 && lvalue>0.0000001)){
				double tmp1=value;
				double tmp2=guess;
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
				lvalue=tmp1;
				lguess=tmp2;
				continue;
			}
			else if(value>0.0000001){ 
				lguess=guess;
				guess-=step;
			}
			else if(value<-0.0000001){  
				lguess=guess;
				guess+=step;
			}
			if(guess==0 || guess<0){ 
				guess=step/2;
				step=step/10;
			}
			lvalue=value;
		}
		if(j>100){
			throw new RQException("No perfect result for Frate, please change another guess, and try again!");
		}
		return new Double(guess);
	}
}
