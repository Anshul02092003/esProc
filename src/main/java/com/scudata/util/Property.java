package com.scudata.util;

/**
 * ���ԣ����ڱ�ʾ�Զ��庯��
 * @author WangXiaoJun
 *
 */
public class Property {
	private String name;
	private String value;
	
	public Property() {
	}
	
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
