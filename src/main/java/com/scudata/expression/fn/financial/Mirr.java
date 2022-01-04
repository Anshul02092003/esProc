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
 * Fmirr(values,finance_rate,reinvest_rate)  ����ĳһ�����ڼ����ֽ����������ڲ�������
 * @param valuesΪһ�����顣��Щ��ֵ�����Ÿ��ڵ�һϵ��֧������ֵ�������루��ֵ��
 * @param Finance_rate Ϊ�ֽ�����ʹ�õ��ʽ�֧�������ʡ�
 * @param Reinvest_rate Ϊ���ֽ�����Ͷ�ʵ�������
 * 
 */
public class Mirr extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fmirr:" +
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
		return mirr(result);
	}

	private Double mirr(Object[] result){

		if(result[0]==null || result[1]==null || result[2]==null){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fmirr" + mm.getMessage("function.paramValNull"));
		}
		if (!(result[0] instanceof Sequence) ||!(result[1] instanceof Number) || !(result[2] instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fmirr" + mm.getMessage("function.paramTypeError"));
		}
		double frate=Variant.doubleValue(result[1]);
		double rrate=Variant.doubleValue(result[2]);
		ListBase1 mems = ((Sequence)result[0]).getMems();
		int m=mems.size();
		double nvalue=0;
		double pvalue=0;
		for(int i=1;i<=m;i++){
			Object obj = mems.get(i);
			if(obj!=null && obj instanceof Number){
				double tmp=Variant.doubleValue(obj);
				if(tmp>=0){ 
					pvalue+=tmp/Math.pow(1+rrate, i);
				}
				else{
					nvalue+=tmp/Math.pow(1+frate, i);
				}
			}
		}
		return new Double(Math.pow(-pvalue*Math.pow(1+rrate, m)/(nvalue*(1.0+frate)),1.0/(m-1.0))-1.0);
	}

}
