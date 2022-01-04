package com.scudata.expression.mfn.cluster;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dw.Cuboid;
import com.scudata.expression.ClusterTableMetaDataFunction;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.ParamInfo2;
import com.scudata.resources.EngineMessage;

/**
 * ����Ԥ�������������������
 * T.cgroups(Fi,��;y:Gi,��;w)
 * @author RunQian
 *
 */
public class Cgroups extends ClusterTableMetaDataFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cgroups" + mm.getMessage("function.missingParam"));
		}
		
		IParam sub0;
		IParam sub1 = null;
		IParam sub2 = null;
		Expression w = null;
		boolean hasM = false;
		int n = Env.getParallelNum();
		if (option != null && option.indexOf('m') != -1) {
			hasM = true;
		}
		if (param.getType() == IParam.Semicolon) {
			int size = param.getSubSize();
			if ((size > 3 && !hasM) || (size > 4 && hasM)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cgroups" + mm.getMessage("function.invalidParam"));
			}
			
			sub0 = param.getSub(0);
			
			sub1 = param.getSub(1);
			if (size > 2) {
				sub2 = param.getSub(2);
			}
			w = sub2 == null ? null : sub2.getLeafExpression();
			if (hasM) {
				IParam sub3 = param.getSub(3);
				if (sub3 != null) {
					n = (Integer) sub3.getLeafExpression().calculate(ctx);
				}
			}
		} else {
			sub0 = param;
		}
		
		String []expNames = null;
		String []names = null;
		String []newExpNames = null;
		String []newNames = null;
		
		if (sub0 != null) {
			ParamInfo2 pi0 = ParamInfo2.parse(sub0, "cuboid", true, false);
			names = pi0.getExpressionStrs2();
			expNames = pi0.getExpressionStrs1();
		}
		
		ParamInfo2 pi1 = null;
		if (sub1 != null) {
			pi1 = ParamInfo2.parse(sub1, "cuboid", true, false);
			newExpNames = pi1.getExpressionStrs1();
			newNames = pi1.getExpressionStrs2();
		}
		return Cuboid.cgroups(expNames, names, newExpNames, newNames, 
				table, w, hasM, n, option, ctx);
	}
}
