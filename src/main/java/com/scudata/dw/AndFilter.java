package com.scudata.dw;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.scudata.util.Variant;

/**
 * ������������й�����
 * @author runqian
 *
 */
public class AndFilter extends IFilter {
	private Number andValue;
	private Number rightValue;
	private long longValue = 0; // ���andValue����rightValue���Ҳ��Ǵ���������longValue�Ż�
	private int operator;

	/**
	 * Constructs a AndFilter for a column-stored table
	 * @param column
	 * @param priority
	 * @param operator
	 * @param andValue
	 * @param rightValue
	 */
	public AndFilter(ColumnMetaData column, int priority, int operator, Object andValue, Object rightValue) {
		super(column, priority);
		this.operator = operator;
		this.andValue = (Number) andValue;
		this.rightValue = (Number) rightValue;
		
		optimize();
	}
	
	/**
	 * Constructs a AndFilter for a row-stored table
	 * @param columnName
	 * @param priority
	 * @param operator
	 * @param andValue
	 * @param rightValue
	 */
	public AndFilter(String columnName, int priority, int operator,  Object andValue, Object rightValue) {
		this.columnName = columnName;
		this.priority = priority;
		this.operator = operator;
		this.andValue = (Number) andValue;
		this.rightValue = (Number) rightValue;
		
		optimize();
	}
	
	private void optimize() {
		if (!(andValue instanceof BigDecimal) && !(andValue instanceof BigInteger) && 
				!(rightValue instanceof BigDecimal) && !(rightValue instanceof BigInteger) && 
				andValue.longValue() == rightValue.longValue()) {
			longValue = andValue.longValue();
		}
	}
	
	/**
	 * Returns {@code true} if the value matchs the filter
	 * @param value
	 */
	public boolean match(Object value) {
		if (longValue != 0) {
			long result = ((Number)value).longValue() & longValue;
			switch (operator) {
			case EQUAL:
				return result == longValue;
			case NOT_EQUAL:
				return result != longValue;
			case GREATER:
				return result > longValue;
			case GREATER_EQUAL:
				return result >= longValue;
			case LESS:
				return result < longValue;
			default: //LESS_EQUAL:
				return result <= longValue;
			}
		} else {
			Number val = Variant.and((Number)value, andValue);
			switch (operator) {
			case EQUAL:
				return Variant.isEquals(val, rightValue);
			case GREATER:
				return Variant.compare(val, rightValue, true) > 0;
			case GREATER_EQUAL:
				return Variant.compare(val, rightValue, true) >= 0;
			case LESS:
				return Variant.compare(val, rightValue, true) < 0;
			case LESS_EQUAL:
				return Variant.compare(val, rightValue, true) <= 0;
			default: //NOT_EQUAL:
				return !Variant.isEquals(val, rightValue);
			}
		}
	}

	/**
	 * ����һ�����ݵ���Сֵ�����ֵ�ж�����������Ƿ�����з���filter������
	 * ע�ⷵ��trueҲֻ�Ǳ�ʾ�����з��ϵ�����
	 */
	public boolean match(Object minValue, Object maxValue) {
		return true;
	}

}
