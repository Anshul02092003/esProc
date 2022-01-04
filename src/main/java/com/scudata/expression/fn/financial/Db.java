package com.scudata.expression.fn.financial;

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
 * db(cost,salvage,life,period,month)   ʹ�ù̶����ݼ���������һ���ʲ��ڸ������ڵ��۾�ֵ
 * 
 * @return
 * 
 * 
 */
public class Db extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		
		if(param==null || param.isLeaf() || param.getSubSize()<4){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fdb:" +
									  mm.getMessage("function.missingParam"));
		}
		
		Object[] result=new Object[5];
		int size=param.getSubSize();
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
			}
		}
		return db(result);
	}
	
	/** 
	 * db(cost,salvage,life,period,month)   ʹ�ù̶����ݼ���������һ���ʲ��ڸ������ڵ��۾�ֵ
	 * @param Cost Ϊ�ʲ�ԭֵ
	 * @param Salvage Ϊ�ʲ����۾���ĩ�ļ�ֵ����ʱҲ��Ϊ�ʲ���ֵ��
	 * @param Life Ϊ�۾����ޣ���ʱҲ�����ʲ���ʹ��������
	 * @param Period Ϊ��Ҫ�����۾�ֵ����ݡ�Period ����ʹ���� life ��ͬ�ĵ�λ
	 * @param Month Ϊ��һ����·�������ʡ�ԣ������Ϊ 12
	 * @return
	 */
	private Object db(Object[] result){
		double cost;
		double salvage;
		double life;
		double period;
		double month=12;
		MessageManager mm = EngineMessage.get();
		for(int i=0;i<=3;i++){
			if(result[i]==null){
				throw new RQException("The "+i+"th param of Fdb:" + mm.getMessage("function.paramValNull"));
			}
			if (!(result[i] instanceof Number)) {
				throw new RQException("The "+i+"th param of Fdb:" + mm.getMessage("function.paramTypeError"));
			}
		}
		cost=Variant.doubleValue(result[0]);
		salvage=Variant.doubleValue(result[1]);
		life=Variant.doubleValue(result[2]);
		period=Variant.doubleValue(result[3]);
		if(result[4]!=null && result[4] instanceof Number){
			month=Variant.doubleValue(result[4]);
		}
		double rate=1.0 - Math.pow(salvage/cost,1.0/life);
		rate=new Long(Math.round(rate*1000)).doubleValue()/1000.0;
		double value=cost * rate * month / 12.0;
		if(period==1){
			return new Double(value);
		}else{			
			for(int i=2;i<=life;i++){
				double tmp=(cost - value ) * rate;
				if(period==i) return new Double(tmp);
				value+=tmp;
			}
			return new Double((cost - value) * rate * (12.0 - month) / 12.0);
			
		}
	}
}
