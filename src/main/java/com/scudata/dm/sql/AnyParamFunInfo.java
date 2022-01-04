package com.scudata.dm.sql;

/**
 * ������������ı�׼������Ϣ
 * @author RunQian
 *
 */
public class AnyParamFunInfo extends FunInfo {
	private String className;

	public AnyParamFunInfo() {
	}

	public AnyParamFunInfo(String name, int pcount, String className) {
		super(name, pcount);
		this.className = className;
	}

	public String getInfo() {
		return className;
	}

	public void setInfo(String info) {
		this.className = info;
	}
}
