package com.scudata.expression;

import com.scudata.dm.Context;
import com.scudata.dm.ParamList;

/**
 * ���������ڵ㣬ֵ���ɱ��޸�
 * @author RunQian
 *
 */
public class ConstParam extends Node {
	private String name;
	private Object value;

	public ConstParam(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public Object calculate(Context ctx) {
		return value;
	}

	protected boolean containParam(String name) {
		return name.equals(this.name);
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		if (resultList.get(name) == null) {
			resultList.addVariable(name, value);
		}
	}
}
