package com.scudata.dm.sql;

/**
 * ��׼������Ϣ(��׼������+��������Ψһ)
 * @author RunQian
 *
 */
public class FunInfo implements Comparable<FunInfo> {
	private String name; // ��׼������
	private int pcount; // ����������-1��ʾ������������

	public FunInfo() {
	}

	public FunInfo(String name, int pcount) {
		this.name = name;
		this.pcount = pcount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getParamCount() {
		return pcount;
	}

	public void setParamCount(int pc) {
		this.pcount = pc;
	}

	public int hashCode() {
		return (name.hashCode() << 24) + pcount;
	}

	public boolean equals(Object o) {
		if (o instanceof FunInfo) {
			return compareTo((FunInfo)o) == 0;
		} else {
			return false;
		}
	}

	// ����map��key
	public int compareTo(FunInfo o) {
		FunInfo funInfo = (FunInfo)o;
		int cmp = name.compareToIgnoreCase(funInfo.name);
		if (cmp == 0) {
			return pcount - funInfo.pcount;
		} else {
			return cmp;
		}
	}
}
