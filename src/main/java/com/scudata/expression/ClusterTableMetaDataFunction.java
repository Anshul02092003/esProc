package com.scudata.expression;

import com.scudata.parallel.ClusterTableMetaData;

/**
 * ��Ⱥ���������
 * T.f()
 * @author RunQian
 *
 */
public abstract class ClusterTableMetaDataFunction extends MemberFunction {
	protected ClusterTableMetaData table;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof ClusterTableMetaData;
	}

	public void setDotLeftObject(Object obj) {
		table = (ClusterTableMetaData)obj;
	}
}