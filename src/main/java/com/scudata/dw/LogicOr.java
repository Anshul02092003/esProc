package com.scudata.dw;

/**
 * ������������
 * @author runqian
 *
 */
class LogicOr extends IFilter {
	private IFilter left;
	private IFilter right;
	
	public LogicOr(IFilter left, IFilter right) {
		super(left.column, left.priority);
		this.left = left;
		this.right = right;
	}
	
	public LogicOr(IFilter left, IFilter right, String columnName) {
		this.columnName = columnName;
		priority = left.priority;
		this.left = left;
		this.right = right;
	}
	
	public boolean match(Object value) {
		return left.match(value) || right.match(value);
	}
	
	public boolean match(Object minValue, Object maxValue) {
		return left.match(minValue, maxValue) || right.match(minValue, maxValue);
	}
}