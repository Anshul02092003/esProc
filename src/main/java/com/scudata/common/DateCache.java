package com.scudata.common;

import java.sql.Date;
import java.util.Calendar;

final public class DateCache {
	// 1992��֮ǰ�е����ڲ��ܱ�86400000����
	// �����2000/1/1��2030/12/31֮�������
	private static final int DATECOUNT = 11323;
	private static final Date []dates = new Date[DATECOUNT];
	static final long BASEDATE; // 2000/1/1��Ӧ������ֵ
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(java.util.Calendar.MILLISECOND, 0);
		BASEDATE = calendar.getTimeInMillis();
		
		for (int i = 0, len = dates.length; i < len; ++i) {
			dates[i] = new java.sql.Date(BASEDATE + i * 86400000L);
		}
	}
	
	public static Date getDate(int i) {
		if (i < DATECOUNT) {
			return dates[i];
		} else {
			return new java.sql.Date(BASEDATE + i * 86400000L);
		}
	}
}