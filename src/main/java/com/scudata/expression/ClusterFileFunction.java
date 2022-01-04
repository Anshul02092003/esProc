package com.scudata.expression;

import com.scudata.parallel.ClusterFile;

/**
 * ��Ⱥ�ļ���Ա��������
 * file.f()
 * @author RunQian
 *
 */
public abstract class ClusterFileFunction extends MemberFunction {
	protected ClusterFile file;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof ClusterFile;
	}

	public void setDotLeftObject(Object obj) {
		file = (ClusterFile)obj;
	}
}