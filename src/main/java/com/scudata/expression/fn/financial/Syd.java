package com.scudata.expression.fn.financial;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * ����ĳ���ʲ��������ܺ��۾ɷ������ָ���ڼ���۾�ֵ
 * @author yanjing
 * 
 * Fsyd(cost,salvage,life,per)   

 * 
 * @param Cost Ϊ�ʲ�ԭֵ
 * @param Salvage Ϊ�ʲ����۾���ĩ�ļ�ֵ����ʱҲ��Ϊ�ʲ���ֵ��
 * @param Life Ϊ�۾����ޣ���ʱҲ�����ʲ���ʹ��������
 * @param Per Ϊָ���ڼ䣬�䵥λ�� life ��ͬ
 * @return
 * 
 * 
 */
public class Syd extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fsyd:" +
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
		return syd(result);

	}
	
	/** 
	 * @param Cost Ϊ�ʲ�ԭֵ
	 * @param Salvage Ϊ�ʲ����۾���ĩ�ļ�ֵ����ʱҲ��Ϊ�ʲ���ֵ��
	 * @param Life Ϊ�۾����ޣ���ʱҲ�����ʲ���ʹ��������
	 * @param Per Ϊָ���ڼ䣬�䵥λ�� life ��ͬ
	 * @return
	 */
	private Object syd(Object[] result){
		double cost;
		double salvage;
		double life;
		double per;
		for(int i=0;i<=3;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fsyd:" + mm.getMessage("function.paramValNull"));
			}
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fsyd:" + mm.getMessage("function.paramTypeError"));
			}
		}
		cost=Variant.doubleValue(result[0]);
		salvage=Variant.doubleValue(result[1]);
		life=Variant.doubleValue(result[2]);
		per=Variant.doubleValue(result[3]);
		return new Double((cost-salvage)*(life-per+1)*2/(life*(life+1)));
	}

}
