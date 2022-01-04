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
 * Fddb(cost,salvage,life,period,factor)  ʹ��˫�����ݼ���������ָ������������һ���ʲ��ڸ����ڼ��ڵ��۾�ֵ
 * 
 * @return
 * 
 * 
 */
public class Ddb extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<4){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fddb:" +
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
		return ddb(result);

	}
	

	
	/**
	 * Fddb(cost,salvage,life,period,factor)  ʹ��˫�����ݼ���������ָ������������һ���ʲ��ڸ����ڼ��ڵ��۾�ֵ
	 * @param Cost Ϊ�ʲ�ԭֵ
	 * @param Salvage Ϊ�ʲ����۾���ĩ�ļ�ֵ����ʱҲ��Ϊ�ʲ���ֵ��
	 * @param Life Ϊ�۾����ޣ���ʱҲ�����ʲ���ʹ��������
	 * @param Period Ϊ��Ҫ�����۾�ֵ���ڼ䡣Period ����ʹ���� life ��ͬ�ĵ�λ
	 * @param Factor Ϊ���ݼ����ʡ���� factor ��ʡ�ԣ���ȱʡΪ 2��˫�����ݼ�����
	 * @return
	 */
	private Object ddb(Object[] result){
		double cost;
		double salvage;
		double life;
		double period;
		double factor=2;
		
		for(int i=0;i<=3;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fddb:" + mm.getMessage("function.paramValNull"));
			}
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fddb:" + mm.getMessage("function.paramTypeError"));
			}
		}
		cost=Variant.doubleValue(result[0]);
		salvage=Variant.doubleValue(result[1]);
		life=Variant.doubleValue(result[2]);
		period=Variant.doubleValue(result[3]);
		if(result[4]!=null && result[4] instanceof Number){
			factor=Variant.doubleValue(result[4]);
		}

		double depreciation =0;
		double tmp=0;
		for(int i=1;i<=period;i++){
			tmp=Math.min((cost - depreciation ) * factor/life,cost-salvage-depreciation);
			depreciation+=tmp;
		}
		return new Double(tmp);
	}

}
