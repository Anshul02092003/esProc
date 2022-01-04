package com.scudata.expression.fn.parallel;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Machines;
import com.scudata.dm.ZoneLoader;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * hosts(;j)	ȡ�����ֻ�������j���ڴ����ţ�j��ʡ��
 * hosts(i;j)	���ñ��ֻ�������j���ڴ����ţ�i=0��ʾ�������j���ڴ�����
 * hosts(n,hs;j)	��hs���ҳ�hosts(;j)����ֵΪ1,��,n�Ŀ��÷ֻ����С���ȱʧ������hosts()Ϊ�յķֻ�����Ӧȱʧִֵ�г�ʼ��(init.dfx)���Ҳ����㹻��ֻ����ؿ�
 * n==0ʱ���ؿ��÷ֻ��������÷ֻ���λ�����null

 * @author Joancy
 */
public class Hosts extends Function {
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}
		return this;
	}

	public Object calculate(Context ctx) {
		Integer I=null,N=null;
		String J=null;
		Machines hs=null;
		
		if (param != null) {
			IParam leftParam;
			if (param.isLeaf()) {
				Object obj = param.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("hosts" + mm.getMessage("function.paramTypeError"));
				}
				I = ((Number)obj).intValue();
			} else {
				if (param.getSubSize() > 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("hosts" + mm.getMessage("function.invalidParam"));
				}
				
				leftParam = param.getSub(0);
				if (leftParam != null) {
					if(leftParam.isLeaf()){
						I = (Integer) leftParam.getLeafExpression().calculate(ctx);
					}else{
						if (leftParam.getSubSize() > 2) {
							MessageManager mm = EngineMessage.get();
							throw new RQException("hosts" + mm.getMessage("function.invalidParam"));
						}
						IParam nParam = leftParam.getSub(0);
						if(nParam!=null){
							N = (Integer) nParam.getLeafExpression().calculate(ctx);
						}
						IParam hsParam = leftParam.getSub(1);
						if(hsParam==null){
							MessageManager mm = EngineMessage.get();
							throw new RQException("hosts" + mm.getMessage("function.invalidParam"));
						}else{
							Object hostObj = hsParam.getLeafExpression().calculate(ctx);
							hs = new Machines();
							if (!hs.set(hostObj)) {
								MessageManager mm = EngineMessage.get();
								throw new RQException("hosts" + mm.getMessage("function.invalidParam"));
							}
						}
						
					}
				}

				IParam rightParam = param.getSub(1);
				if (rightParam != null && rightParam.isLeaf()) {
					Object obj = rightParam.getLeafExpression().calculate(ctx);
					if (!(obj instanceof String)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("hosts" + mm.getMessage("function.paramTypeError"));
					}
					J = (String)obj;
				}
				
			}
		}
				
		ZoneLoader zl = new ZoneLoader();
		if(hs==null){
			zl.setArgs(I, J);
		}else{
			zl.setArgs(N, hs, J);
		}
		return zl.execute();
	}
}
