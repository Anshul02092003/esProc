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
 * Fvdb(cost,salvage,life,start_period,end_period,factor)  ʹ��˫�����ݼ���������ָ���ķ���������ָ�����κ��ڼ��ڣ����������ڼ䣩���ʲ��۾�ֵ������ db@v ����ɱ����ݼ���
 * @s  no_switch=true
 * @return
 * 
 * 
 */
public class Vdb extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		if(param==null || param.isLeaf() || param.getSubSize()<4){
			MessageManager mm = EngineMessage.get();
			throw new RQException("Fvdb:" +
									  mm.getMessage("function.missingParam"));
		}
		int size=param.getSubSize();
		Object[] result=new Object[7];
		size=Math.min(size,7);
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
			}
		}

		if(option!=null && option.indexOf('s')>=0) result[6]=new Boolean(true);
		return vdb(result);
	}
	
	
	/**
	 * Fvdb(cost,salvage,life,start_period,end_period,factor)
	 * @s  no_switch=true  
	 * ʹ��˫�����ݼ���������ָ���ķ���������ָ�����κ��ڼ��ڣ����������ڼ䣩���ʲ��۾�ֵ��
	 * ���� Fvdb ����ɱ����ݼ���
	 * @param Cost Ϊ�ʲ�ԭֵ
	 * @param Salvage Ϊ�ʲ����۾���ĩ�ļ�ֵ����ʱҲ��Ϊ�ʲ���ֵ��
	 * @param Life Ϊ�۾����ޣ���ʱҲ�����ʲ���ʹ��������
	 * @param Start_period Ϊ�����۾ɼ������ʼ�ڼ�,������ life �ĵ�λ��ͬ
	 * @param End_period Ϊ�����۾ɼ���Ľ�ֹ�ڼ�,������ life �ĵ�λ��ͬ
	 * @param Factor Ϊ���ݼ����ʣ��۾����ӣ������ factor ��ʡ�ԣ���ȱʡΪ 2��˫�����ݼ�����
	 * @param No_switch Ϊһ�߼�ֵ��ָ�����۾�ֵ�������ݼ�����ֵʱ���Ƿ�ת��ֱ���۾ɷ���
	 * @param ��� no_switch Ϊ TRUE����ʹ�۾�ֵ�������ݼ�����ֵ��Ҳ��ת��ֱ���۾ɷ��� 
	 * @param ��� no_switch Ϊ FALSE �򱻺��ԣ����۾�ֵ�������ݼ�����ֵʱ����ת�������۾ɷ���

	 * @return
	 */
	private Object vdb(Object[] result){
		double cost;
		double salvage;
		double life;
		double start_period;
		double end_period;
		double factor=2;
		boolean no_switch=false;
		
		for(int i=0;i<=3;i++){
			if(result[i]==null){
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fvdb:" + mm.getMessage("function.paramValNull"));
			}
			if (!(result[i] instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("The "+i+"th param of Fvdb:" + mm.getMessage("function.paramTypeError"));
			}
		}
		cost=Variant.doubleValue(result[0]);
		salvage=Variant.doubleValue(result[1]);
		life=Variant.doubleValue(result[2]);
		start_period=Variant.doubleValue(result[3]);
		end_period=start_period;
		if(result[4]!=null && result[4] instanceof Number){
			end_period=Variant.doubleValue(result[4]);
		}
		if(result[5]!=null && result[5] instanceof Number){
			factor=Variant.doubleValue(result[5]);
		}
		if(result[6]!=null && result[6] instanceof Boolean){
			no_switch=((Boolean)result[6]).booleanValue();
		}

		double depreciation =0;
		double value=0;
		for(int i=1;i<=end_period;i++){
			double tmp=(cost - depreciation ) * factor/life;
			double tmp1=cost-salvage-depreciation;
			if(!no_switch && tmp1/(life-i+1)>tmp){
				tmp=tmp1/(life-i+1);
			}else tmp=Math.min(tmp, tmp1);
			depreciation+=tmp;
			if(i>start_period) value+=tmp;
		}
		return new Double(value);
	}
}
