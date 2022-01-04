package com.scudata.expression.fn.math;

import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����г�Ա�������������Լ��,����ֵ��Ա�����ԣ���ֵ��Ա���Զ�ȡ��,����С��0�ĳ�Ա���ش���ֵ0
 * @author yanjing
 *
 */
public class Gcd extends Function {

	public Object calculate(Context ctx) {
		ArrayList<Number> num=new ArrayList<Number>();
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("gcd" + mm.getMessage("function.missingParam"));
		} else if(param.isLeaf()) {
			Object result = param.getLeafExpression().calculate(ctx);
			if (result != null && result instanceof Number) {
				num.add((Number)result);
			} else if (result != null && result instanceof Sequence) {
				int n=((Sequence)result).length();
				for (int i=1;i<=n;i++) {
					Object tmp=((Sequence)result).get(i);
					if (tmp!=null && tmp instanceof Number) {
						num.add((Number)tmp);
					}
				}
			}
		} else {
			int size = param.getSubSize();
			
			for (int j=0; j<size; j++) {
				IParam subj = param.getSub(j);
				if (subj != null) {
					Object result = subj.getLeafExpression().calculate(ctx);
					if (result != null && result instanceof Number) {
						num.add((Number)result);
					} else if(result != null && result instanceof Sequence) {
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
		}
		
		int k=num.size();
		Number[] nums=new Number[k];
		num.toArray(nums);
		return new Long(gcd(nums,k));
	}
	
	/**
	 * ����������Լ������Ϊ��С����������Ҫ�õ������������Ե����ó���
	 * @param num
	 * @param k
	 * @return
	 */
	public static long gcd(Number[] num,int k){
		Arrays.sort(num);//��С��������
		
		//����С������ʼ���������������Լ��
		long d = Variant.longValue(num[0]);
		if (d<0) {
			return 0;
		}
		
		for (int i = 1; i < k; i++) {
			long d1=Variant.longValue(num[i]);
			if(d1<0) return 0;
			d=gcd(d1,d);
		}
		
		return d;
	}
	
	/**
	 * �������������Լ������ȡŷ����¶���f(a,b)=f(b,a%b)
	 * @param max
	 * @param min
	 * @return
	 */
	private static long gcd(long max,long min){
		if (max == min) {
			return max;
		}
		
		if (min > max) {
			long tmp=min;
			min=max;
			max=tmp;
		}
		
		if(min==0) {
			return max;//��Ϊ0�ܱ��κ������������ݰٶȰٿ�
		} else {
			return gcd(min,max%min);
		}
	}
}
