package com.scudata.common;

import java.util.*;
import java.text.*;

/**
 * �̰߳�ȫ�࣬��ÿ�θ�ʽ���ͷ���ǰ�����õ�ǰ�ĸ�ʽ���������ϴε����û�Ӱ���´εĸ�ʽ���ͷ���
 */
public class DateFactory {
	private static ThreadLocal local = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new DateFactory();
		}
	};

	public static DateFactory get() {
		return (DateFactory) local.get();
	}

	private Calendar calendar;

	private DateFactory() {
	}

	private Calendar getCalendar() {
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}
		calendar.clear();
		return calendar;
	}


	public Date toDate(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}
	
	public Date toDate(long date) {
		Calendar gc = getCalendar();
		gc.setTimeInMillis(date);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	public Date toTime(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.YEAR, 1970);
		gc.set(Calendar.MONTH, Calendar.JANUARY);
		gc.set(Calendar.DAY_OF_MONTH, 1);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Time(gc.getTimeInMillis());
	}

	public Date weekBegin(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, gc.getActualMinimum(Calendar.DAY_OF_WEEK));
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	public Date weekEnd(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, gc.getActualMaximum(Calendar.DAY_OF_WEEK));
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());

	}

	/**
	 * ȡָ�����ڵ���������
	 * @param date ָ��������
	 * @return ��������
	 */
	public Date monthBegin(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_MONTH, 1);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	/**
	 * ȡָ�����ڵ���ĩ����
	 * @param date ָ��������
	 * @return ��ĩ����
	 */
	public Date monthEnd(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());

	}

	/**
	 * ȡָ�����ڵļ�������
	 * @param date ָ��������
	 * @return ��������
	 */
	public Date quaterBegin(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		int month = (gc.get(Calendar.MONTH) / 3) * 3;
		gc.set(Calendar.DAY_OF_MONTH, 1);
		gc.set(Calendar.MONTH, month);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	/**
	 * ȡָ�����ڵļ���ĩ����
	 * @param date ָ��������
	 * @return ����ĩ����
	 */
	public Date quaterEnd(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		int month = (gc.get(Calendar.MONTH) / 3) * 3 + 2;
		gc.set(Calendar.DAY_OF_MONTH, 1);
		gc.set(Calendar.MONTH, month);
		gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}
	
	// ȡָ������������ĵ�һ��
	public Date yearBegin(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.MONTH, Calendar.JANUARY);
		gc.set(Calendar.DAY_OF_MONTH, 1);
		return new java.sql.Date(gc.getTimeInMillis());
	}
	
	// ȡָ����������������һ��
	public Date yearEnd(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.set(Calendar.MONTH, Calendar.DECEMBER);
		gc.set(Calendar.DAY_OF_MONTH, 31);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	/**
	 * ȡָ�����ڵ�����ͬһ�գ�����ͬһ�գ��򷵻��������һ��
	 * @param date ָ��������
	 * @return ����ͬһ��
	 */
	public Date lastMonth(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.add(Calendar.MONTH, -1);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	/**
	 * ȡָ�����ڵ���һ��ͬһ����
	 * @param date ָ��������
	 * @return ��һ��ͬһ����
	 */
	public Date lastYear(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.add(Calendar.YEAR, -1);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	/**
	 * ȡָ�����ڵ�����
	 * @param date ָ��������
	 * @return ����
	 */
	public Date lastDay(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		gc.add(Calendar.DATE, -1);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	public int year(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.YEAR);
	}

	public int month(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.MONTH) + 1;
	}

	public int day(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.DAY_OF_MONTH);
	}

	public int hour(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.HOUR_OF_DAY);
	}

	public int minute(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.MINUTE);
	}

	public int second(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.SECOND);
	}

	public int millisecond(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.MILLISECOND);
	}

	public int week(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.get(Calendar.DAY_OF_WEEK);
	}

	public int daysInMonth(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public int daysInYear(Date date) {
		Calendar gc = getCalendar();
		gc.setTime(date);
		return gc.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	public int daysInYear(int year) {
		Calendar gc = getCalendar();
		gc.set(Calendar.DAY_OF_YEAR, 1);
		gc.set(Calendar.YEAR, year);
		return gc.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * ��������(10λ)
	 * @param data ��Ҫ�������ֽڷ�
	 * @param beginIndex ��ʼ������λ��
	 * @return ������Ч��Date����
	 */
	public static Date parseDate(String data, int beginIndex) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Date(DateFormatFactory.get().getDateFormat().parse(data, new ParsePosition(beginIndex)).getTime());
	}

	/**
	 * ��������(10λ)
	 * @param data ��Ҫ�������ֽڷ�
	 * @return ������Ч��Date����
	 */
	public static Date parseDate(String data) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Date(DateFormatFactory.get().getDateFormat().parse(data).getTime());
	}

	/**
	 * ����ʱ��(8λ)
	 * @param data ��Ҫ�������ֽ�����
	 * @param beginIndex ��ʼ������λ��
	 * @return ʱ����Ч��Time����
	 */
	public static Date parseTime(String data, int beginIndex) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Time(DateFormatFactory.get().getTimeFormat().parse(data, new ParsePosition(beginIndex)).getTime());
	}

	/**
	 * ����ʱ��(8λ)
	 * @param data ��Ҫ�������ֽ�����
	 * @return ʱ����Ч��Time����
	 */
	public static Date parseTime(String data) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Time(DateFormatFactory.get().getTimeFormat().parse(data).getTime());
	}

	/**
	 * ��������ʱ��(19λ)
	 * @param data ��Ҫ�������ֽ�����
	 * @param beginIndex ��ʼ������λ��
	 * @return ������ʱ�����Ч��Date����
	 */
	public static Date parseDateTime(String data, int beginIndex) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Timestamp(DateFormatFactory.get().getDateTimeFormat().parse(data, new ParsePosition(beginIndex)).getTime());
	}

	/**
	 * ��������ʱ��(19λ)
	 * @param data ��Ҫ�������ֽ�����
	 * @return ������ʱ�����Ч��Date����
	 */
	public static Date parseDateTime(String data) throws ParseException {
		if (data == null) {
			return null;
		}
		return new java.sql.Timestamp(DateFormatFactory.get().getDateTimeFormat().parse(data).getTime());
	}
}
