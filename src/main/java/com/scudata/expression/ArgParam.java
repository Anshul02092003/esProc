package com.scudata.expression;

import com.scudata.dm.Context;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;

/**
 * �����ڵ㣬ֵ���ɱ��޸�
 * @author RunQian
 *
 */
public class ArgParam extends Node {
	private Param param;

	public ArgParam(Param param) {
		this.param = param;
	}

	public Object calculate(Context ctx) {
		return param.getValue();
	}

	protected boolean containParam(String name) {
		return name.equals(param.getName());
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		if (resultList.get(param.getName()) == null) {
			resultList.addVariable(param.getName(), param.getValue());
		}
	}
}
