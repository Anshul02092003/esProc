package com.scudata.expression.fn.financial;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;



/**
 * ���ڹ̶����ʺ�ÿ�ڵȶ�Ͷ��ģʽ,����һ��Ͷ�ʵ�δ��ֵ/��ֵ��
 * @author yanjing
 * 

 * Fnper@t(rate,pmt,pv,fv)   �ȶϢʱ�����㻹������
 * @param Rate Ϊÿ�ڵ�����, ����ֵ����������ڼ䱣�ֲ��䡣
 * @param pmt  ÿ�ڵĻ�������ʡ�ԡ�
 * @param Pv Ϊ��ֵ,�൱�ڴ����
 * @param Fv Ϊδ��ֵ�������һ�ڻ����ʣ��Ĵ���

 * @return
 * 
 * ���type=0:
 *  	Fnper=log(1+rate) (-fv*rate+pmt)/(pmt+pv*rate)
 * ���type=1:  @t
 * 		Fnper@t=log(1+rate) (-fv*rate+pmt)/(pmt+pv*rate/(1+rate))
 */
public class Nper extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fnper:" +
									  mm.getMessage("function.missingParam"));
		}
		
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if (result[i] != null && !(result[i] instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("The "+i+"th param of Fnper:" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		return pmt(result);
	}
	
	private Object pmt(Object[] result){
		double rate=0;
		double pmt=0;
		double pv=0;
		double fv=0;

		if(result[0]==null || result[1]==null || result[2]==null){
			MessageManager mm = EngineMessage.get();
			throw new RQException("The first three params of Fnper:" + mm.getMessage("function.paramValNull"));
		}
		else{
			rate=Variant.doubleValue(result[0]);
			pmt=Variant.doubleValue(result[1]);
			pv=Variant.doubleValue(result[2]);
		}
		if(result[3]!=null) fv=Variant.doubleValue(result[3]);
		if(option==null || option.indexOf("t")<0){  //type=0 ��ĩ����
			return new Double(Math.log((-fv*rate+pmt)/(pmt+pv*rate))/Math.log(1.0+rate));
		}else{   //type=1  �ڳ�����
			return new Double(Math.log((pmt-fv*rate)/(pmt+pv*rate/(1.0+rate)))/Math.log(1.0+rate));
		}
	}
}
