package com.scudata.expression;

import com.scudata.parallel.ClusterPhyTable;

/**
 * ��Ⱥ���������
 * T.f()
 * @author RunQian
 *
 */
public abstract class ClusterPhyTableFunction extends MemberFunction {
	protected ClusterPhyTable table;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof ClusterPhyTable;
	}

	public void setDotLeftObject(Object obj) {
		table = (ClusterPhyTable)obj;
	}
}