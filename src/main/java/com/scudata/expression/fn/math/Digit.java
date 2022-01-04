package com.scudata.expression.fn.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * digit(xi,��)	sum(2i-1*xi)��xi���ַ���ʱ��ת������
	@h	sum(16i-1*xi)��xi���ַ���ʱ�Ȱ�ʮ��������λ����ת����
	@d	sum(10i-1*xi)��xi���ַ���ʱ��ת������
	@n	sum(2i-1*if(xi,1,0))

 * @author yanjing
 *
 */
public class Digit extends Function {

	public Object calculate(Context ctx) {
		if (param == null || param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("digit" + mm.getMessage("function.missingParam"));
		}else{
			int size=param.getSubSize();
		
			Object[] result1=new Object[size];
			for(int j=0;j<size;j++){
				IParam subj = param.getSub(j);
				if (subj != null) {
					result1[j] = subj.getLeafExpression().calculate(ctx);
				}
			}
		
			long result=0;
			int l=0;
			if(option!=null && option.indexOf("h")>=0){//ʮ������
				for(int j=0;j<size;j++){
					if(result1[j]!=null && result1[j] instanceof String){
						result+=Math.pow(16, j+l)*Long.valueOf((String)result1[j],16).longValue();
					}else if(result1[j]!=null && result1[j] instanceof Number){
						result+=Math.pow(16, j+l)*((Number)result1[j]).longValue();
					}else if(result1[j] != null && result1[j] instanceof Sequence){
						int n=((Sequence)result1[j]).length();
						for(int i=1;i<=n;i++){
							Object tmp=((Sequence)result1[j]).get(i);
							if (tmp!=null && tmp instanceof Number) {
								result+=Math.pow(16, j+l+i-1)*((Number)tmp).longValue();
							}else if (tmp!=null && tmp instanceof String) {
								result+=Math.pow(16, j+l+i-1)*Long.valueOf((String)tmp,16).longValue();
							}
						}
						l+=n-1;
					}
				}
			}else if(option!=null && option.indexOf("d")>=0){//10����
				for(int j=0;j<size;j++){
					if(result1[j]!=null && result1[j] instanceof String){
						result+=Math.pow(10, j+l)*Variant.parseNumber((String)result1[j]).longValue();
					}else if(result1[j]!=null && result1[j] instanceof Number){
						result+=Math.pow(10, j+l)*((Number)result1[j]).longValue();
					}else if(result1[j] != null && result1[j] instanceof Sequence){
						int n=((Sequence)result1[j]).length();
						for(int i=1;i<=n;i++){
							Object tmp=((Sequence)result1[j]).get(i);
							if (tmp!=null && tmp instanceof Number) {
								result+=Math.pow(10, j+l+i-1)*((Number)tmp).longValue();
							}else if (tmp!=null && tmp instanceof String) {
								result+=Math.pow(10, j+l+i-1)*Variant.parseNumber((String)tmp).longValue();
							}
						}
						l+=n-1;
					}
				}	
			}else if(option!=null && option.indexOf("n")>=0){//��1�ǿ�0
				for(int j=0;j<size;j++){
					if(result1[j] != null && result1[j] instanceof Sequence){
						int n=((Sequence)result1[j]).length();
						for(int i=1;i<=n;i++){
							Object tmp=((Sequence)result1[j]).get(i);
							if (tmp!=null){
								result+=Math.pow(2, j+l+i-1);
							}
						}
						l+=n-1;
					}else if(result1[j]!=null){
						result+=Math.pow(2, j+l);
					}
				}
			}else{//������
				for(int j=0;j<size;j++){
					if(result1[j]!=null && result1[j] instanceof String){
						result+=Math.pow(2, j+l)*Variant.parseNumber((String)result1[j]).longValue();
					}else if(result1[j]!=null && result1[j] instanceof Number){
						result+=Math.pow(2, j+l)*((Number)result1[j]).longValue();
					}else if(result1[j] != null && result1[j] instanceof Sequence){
						int n=((Sequence)result1[j]).length();
						for(int i=1;i<=n;i++){
							Object tmp=((Sequence)result1[j]).get(i);
							if (tmp!=null && tmp instanceof Number) {
								result+=Math.pow(2, j+l+i-1)*((Number)tmp).longValue();
							}else if (tmp!=null && tmp instanceof String) {
								result+=Math.pow(2, j+l+i-1)*Variant.parseNumber((String)tmp).longValue();
							}
						}
						l+=n-1;
					}
				}	
			}
			return new Long(result);
		}
	}
}
