package com.scudata.common;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

/**
 * �̰߳�ȫ�࣬��ÿ�θ�ʽ���ͷ���ǰ�����õ�ǰ�ĸ�ʽ���������ϴε����û�Ӱ���´εĸ�ʽ���ͷ���
 */
public class DateFactory {
	private static ThreadLocal<DateFactory> local = new ThreadLocal<DateFactory>() {
		protected synchronized DateFactory initialValue() {
			return new DateFactory();
		}
	};

	public static DateFactory get() {
		return (DateFactory) local.get();
	}

	private Calendar calendar = Calendar.getInstance();

	private DateFactory() {
	}

	public Calendar getCalendar() {
		calendar.clear();
		return calendar;
	}

	public Calendar calendar() {
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
		gc.setFirstDayOfWeek(Calendar.SUNDAY);
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}
	
	/**
	 * ����һΪ��һ��
	 * @param date
	 * @return
	 */
	public Date weekBegin1(Date date) {
		Calendar gc = getCalendar();
		gc.setFirstDayOfWeek(Calendar.MONDAY);
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}

	public Date weekEnd(Date date) {
		Calendar gc = getCalendar();
		gc.setFirstDayOfWeek(Calendar.SUNDAY);
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(gc.getTimeInMillis());
	}
	
	/**
	 * ����һΪ��һ��
	 * @param date
	 * @return
	 */
	public Date weekEnd1(Date date) {
		Calendar gc = getCalendar();
		gc.setFirstDayOfWeek(Calendar.MONDAY);
		gc.setTime(date);
		gc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
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
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public int month(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	public int day(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
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
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public int daysInMonth(Date date) {
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public int daysInYear(Date date) {
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
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
	
	/**
	 * �����ڱ�ɻ���1970�������
	 * days@o(date) date(1970+d\384,d\32%12+1,d%32)
	 * @param date
	 * @return int
	 */
	public static int toDays(Date date) {
		Calendar calendar = get().calendar;
		calendar.setTime(date);
		int y = calendar.get(Calendar.YEAR) - 1970;
		int ym = y * 12 + calendar.get(Calendar.MONTH);
		return ym * 32 + calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * �����ڱ�ɻ���1970�������
	 * @param y ��1��ʼ����
	 * @param m ��0��ʼ����
	 * @param d ��1��ʼ����
	 * @return int
	 */
	public static int toDays(int y, int m, int d) {
		return ((y - 1970) * 12 + m) * 32 + d;
	}
	
	/**
	 * �ѻ���1970��������������
	 * @param days
	 * @return Date
	 */
	public static Date toDate(int days) {
		if (days < 0) {
			int y = days / 384 + 1969;
			int m = days / 32 % 12 + 11;
			int d = days % 32 + 32;
			
			Calendar calendar = get().calendar;
			calendar.set(y, m, d, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return new java.sql.Date(calendar.getTimeInMillis());
		} else {
			int y = days / 384 + 1970;
			int m = days / 32 % 12;
			int d = days % 32;
			
			Calendar calendar = get().calendar;
			calendar.set(y, m, d, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return new java.sql.Date(calendar.getTimeInMillis());
		}
	}
	
	/**
	 * �ѻ���1970������������
	 * @param days
	 * @return int
	 */
	public static int toYear(int days) {
		if (days < 0) {
			return days / 384 + 1969;
		} else {
			return days / 384 + 1970;
		}
	}
	
	/**
	 * �ѻ���1970������������
	 * @param days
	 * @return int
	 */
	public static int toMonth(int days) {
		if (days < 0) {
			return days / 32 % 12 + 12;
		} else {
			return days / 32 % 12 + 1;
		}
	}
	
	/**
	 * �ѻ���1970��������������
	 * @param days
	 * @return int
	 */
	public static int toYearMonth(int days) {
		if (days < 0) {
			int y = days / 384 + 1969;
			int m = days / 32 % 12 + 12;
			return y * 100 + m;
		} else {
			int ym = days / 32;
			int y = ym / 12 + 1970;
			return y * 100 + ym % 12 + 1;
		}
	}
	
	/**
	 * �ѻ���1970������������
	 * @param days
	 * @return int
	 */
	public static int toDay(int days) {
		if (days < 0) {
			return days % 32 + 32;
		} else {
			return days % 32;
		}
	}
}
