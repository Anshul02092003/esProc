package com.scudata.vdb;

import com.scudata.util.Variant;

/**
 * Ŀ¼����
 * @author RunQian
 *
 */
abstract class IDir {
	protected Object value; // Ŀ¼ֵ
	protected String name; // Ŀ¼�������ֶ���
		
	public IDir() {
	}
		
	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
	
	abstract public ISection getParent();

	public boolean isEqualValue(Object val) {
		return Variant.isEquals(value, val);
	}
	
	public boolean isEqualName(String str) {
		if (str == null) {
			return name == null;
		} else {
			return str.equals(name);
		}
	}
	
	abstract public void releaseSubSection();
}
