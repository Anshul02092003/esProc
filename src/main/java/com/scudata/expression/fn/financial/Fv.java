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
 * Fv(rate,nper,pmt,[pv],[type])  ����δ��ֵ
 * Fv@p(rate,nper,pmt,[fv],[type])  ������ֵ
 * @t �ڳ�����,ʡ��Ϊ��ĩ����
 * @param Rate Ϊÿ�ڵ�����, ����ֵ����������ڼ䱣�ֲ��䡣
 * @param Nper Ϊ�ܸ���������
 * @param pmt  Ϊÿ�ڵ�Ͷ�ʶ�, ����ֵ����������ڼ䱣�ֲ��䡣���ʡ�� pmt���������� pv/fv ������
 * @param Pv Ϊ��ֵ,�൱�ڳ��ڵ�Ͷ�ʡ����ʡ�� PV���������ֵΪ�㣬���ұ������ pmt ������
 * @param Fv Ϊδ��ֵ�������һ��֧����ʣ���Ͷ�ʶ����Ϊ��ĩ����׷�ӵ�Ͷ�ʶ���ʡ�� fv���������ֵΪ���ұ������ pmt ���������磬�����Ҫ�� 18 ��Ͷ���ڽ���ʱͶ�ʣ�50,000���� ��50,000 ����δ��ֵ��

 * @return ��@p����δ��ֵ��@p������ֵ
 * 
 * ���typeΪ0����value�ļ��㹫ʽ���£�=-(pmt*(1+rate)^(nper-1)+ pmt*(1+rate)^(nper-2)+����+pmt*(1+rate)^0+pv*(1+rate)^nper)��
 * ���typeΪ1����=-(pmt*(1+rate)^nper+ pmt*(1+rate)^(nper-1)+����+pmt*(1+rate)+pv*(1+rate)^nper)
 * 
 * ���typeΪ0��value@p�ļ��㹫ʽ���£�-(pmt/(1+rate)^nper+pmt/(1+rate)^(nper-1)+...... +pmt/(1+rate)+fv/(1+rate)^nper)��
 * ���typeΪ1����-(pmt/(1+rate)^(nper-1)+pmt/(1+rate)^(nper-2)+...... +pmt/(1+rate)^0+fv/(1+rate)^nper)
 */
public class Fv extends Function {
                                                                                                                            
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		if(param==null || param.isLeaf() || param.getSubSize()<3){
			throw new RQException("Fv" +
								  mm.getMessage("function.missingParam"));
		}
		int size=param.getSubSize();
		Object[] result=new Object[size];
		for(int i=0;i<size;i++){
			IParam sub = param.getSub(i);
			if (sub != null) {
				result[i] = sub.getLeafExpression().calculate(ctx);
				if (result[i] != null && !(result[i] instanceof Number)) {
					throw new RQException("The "+i+"th param of Fv" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		if(result[2]==null) result[2]=new Integer(0);
		if(result[3]==null) result[3]=new Integer(0);
		
		return value(Variant.doubleValue(result[0]),Variant.longValue(result[1]),Variant.doubleValue(result[2]),Variant.doubleValue(result[3]));
	}
	
	private Double value(double rate,long nper,double pmt,double fpv){
		if(rate<=0) return new Double(0);
		if(nper<=0) return new Double(0);
		if (option == null || option.indexOf('p') == -1) {//����δ��ֵ
			if(option==null || option.indexOf('t')==-1){  //type=0
				double value=fpv*Math.pow(1+rate,nper);
				for(long i=nper-1;i>=0;i--){
					value+=pmt*Math.pow(1+rate, i);
				}
				return new Double(-value);
			}else{ // type=1
				double value=fpv*Math.pow(1+rate,nper);
				for(long i=nper;i>=1;i--){
					value+=pmt*Math.pow(1+rate, i);
				}
				return new Double(-value);
			}
		}
		else{//������ֵ
			if(option==null || option.indexOf('t')==-1){  //type=0
				double value=fpv/Math.pow(1+rate,nper);
				for (long i=nper;i>=1;i--){
					value+=pmt/Math.pow(1+rate,i);
				}
				return new Double(-value);
			}else {  //type=1
				double value=fpv/Math.pow(1+rate,nper);
				for (long i=nper-1;i>=0;i--){
					value+=pmt/Math.pow(1+rate,i);
				}
				return new Double(-value);
			}
		}
	}
}
