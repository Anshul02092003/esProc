package com.scudata.expression.fn.math;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����г�Ա����С������,����ֵ��Ա�����ԣ���ֵ��Ա���Զ�ȡ��������С�ڵ���0�ĳ�Ա���ش���ֵ0
 * @author yanjing
 *
 */
public class Lcm extends Function {

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("lcm" +
								  mm.getMessage("function.missingParam"));
		}else{
			int size = param.getSubSize();
			ArrayList<Number> num=new ArrayList<Number>();
			for(int j=0;j<size;j++){
				IParam subj = param.getSub(j);
				if (subj != null) {
					Object result = subj.getLeafExpression().calculate(ctx);
					if (result != null && result instanceof Number) {
						num.add((Number)result);
					}else if(result != null && result instanceof Sequence){
						int n=((Sequence)result).length();
						for(int i=1;i<=n;i++){
							Object tmp=((Sequence)result).get(i);
							if (tmp!=null && tmp instanceof Number) {
								num.add((Number)tmp);
							}
						}
					}
				}
			}
			int k=num.size();
			Number[] nums=new Number[k];
			num.toArray(nums);
			return new Long(lcm(nums));
		}
	}
	
	/**��С�������㷨������
	 * ��[a1,a2,..,an] ��ʾa1,a2,..,an����С��������(a1,a2,..,an)��ʾa1,a2,..,an�����Լ��
	 * MΪa1,a2,..,an�ĳ˻�
	 * ��[a1,a2,..,an]=M/(M/a1,M/a2,..,M/an)
	 * @return
	 */
	private long lcm(Number[] num){
		
		int k=num.length;
		long M=1;
		for(int i=0;i<k;i++){
			M*=Variant.longValue(num[i]);
			if(M<=0) return 0;
		}
		Number[] num1=new Number[k];//Ϊ��ɾ��Ϊ�յ������Ա��˳����M/a1,M/a2,..,M/an����
		for(int i=0;i<k;i++){
			num1[i]=new Long(M/Variant.longValue(num[i]));
		}
		
		return M/Gcd.gcd(num1, k);
	}
}
