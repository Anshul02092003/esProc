package com.scudata.expression.fn.financial;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;



/**
 * 
 * @author yanjing
 * 
 * Firr(values,guess)	����һ���ֽ������ڲ������ʣ���Щ�ֽ������ڷ���
 *  @param guess	�Ժ��� irr �������Ĺ���ֵ, 	ʹ�õ��������㺯�� irr������20����δ�ҵ�����򱨴�
	 *              1����io=guess,���㾻��ֵvalue@pn(io)
	 *				3����value@pn(io)=0����irr=io��
	 *					��value@pn(io)>0�����������io��
	 *					��value@pn(io)<0���������Сio��
	 *				4���ظ�����3)��ֱ���ҵ���������������i1��i2������value@pn(i1) >0��value@pn(i2)<0������i2-ilһ�㲻����2%-5%��
	 *				5���������Բ�ֵ��ʽ���Ƽ����ڲ�������irr������㹫ʽΪ��
	 *					IRR= valuel*(i2-i1)/(value1-value2)+i1
 * @param values	һ���ֽ���
 * 
 */
public class Irr extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {

		if(param==null){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The params of Firr" + mm.getMessage("function.missingParam"));
		}else if(param.isLeaf()){
			Object[] result=new Object[1];
			result[0] = param.getLeafExpression().calculate(ctx);
			return irr(result);
		}else{
			int size=param.getSubSize();
			Object[] result=new Object[size];
			for(int i=0;i<size;i++){
				IParam sub = param.getSub(i);
				if (sub != null) {
					result[i] = sub.getLeafExpression().calculate(ctx);
				}
			}
			return irr(result);
		}
		

	}
	
	private Double irr(Object[] result){
		if(result[0]==null || !(result[0] instanceof Sequence) ){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Firr" + mm.getMessage("function.paramTypeError"));
		}

		ListBase1 mems = ((Sequence)result[0]).getMems();
		int n=mems.size();
		
		double guess=0.1;
		if(result.length>1 && result[1]!=null && result[1] instanceof Number) guess=Variant.doubleValue(result[1]);
		int j;
		double lvalue=0;
		double lguess=guess;
		double step=0.01;
		for(j=1;j<=100;j++){
			double value=0;
			for(int i=1;i<=n;i++){
				Object obj1 = mems.get(i);
				if (obj1!=null && obj1 instanceof Number) {
					value+=Variant.doubleValue(obj1)/Math.pow(1+guess, i);
				}
			}
			if(value<0.0000001 && value>-0.0000001) break;
			//���һ��һ��,�������Բ�ֵ��ʽ
			else if((lvalue>0.0000001 && value<-0.0000001) || (lvalue<-0.0000001 && value>0.0000001)){
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
				lguess=tmp2;
				lvalue=tmp1;
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
		if(j>100){
			throw new RQException("No perfect result for Firr, please change another guess, and try again!");
		}
		return new Double(guess);
	}
}
