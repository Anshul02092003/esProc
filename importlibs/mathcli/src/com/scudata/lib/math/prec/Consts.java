package com.scudata.lib.math.prec;

import com.scudata.dm.DataStruct;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Table;

/**
 * ����
 *
 */
public class Consts {
	/**
	 * ��������
	 */

	/** ȱʡ���Զ���� **/
	public static final byte F_DEFAULT = 0;
	/** ��ֵ����������ȡֵ��{0,1}�ڣ���ֻ�����ֿ����� **/
	public static final byte F_TWO_VALUE = 1;
	/** ��ֵ���� **/
	public static final byte F_SINGLE_VALUE = 2;
	/** ö�ٱ��������Ƕ�ֵ��������������ɢ���� **/
	public static final byte F_ENUM = 3;

	/** ��ֵ����������ȡֵΪʵ�� **/
	public static final byte F_NUMBER = 11;
	/** ��������������ȡֵΪ������Ҳ������ֵ���� **/
	public static final byte F_COUNT = 12;
	/** ���� **/
	public static final byte F_DATE = 13;
	/** ����Ϊ��ֵ������������������ **/
	public static final byte F_CONTINUITY = 20;
	/** ���ı����ͣ������뽨ģ **/
	public static final byte F_LONG_TEXT = 21;

	/** �ñ�����Ԥ���������ڿ�ֵ�������ö��ֵ���౻ɾ��������������BI,MI�� **/
	public static final byte F_DEL_I = 31;
	/** �ñ����ڶ�ֵ����ѡ���б�ɾ������MVP�޹� **/
	public static final byte F_DEL_B = 32;
	/** �ñ�������ɢ����ѡ���б�ɾ�������Բ���ִ��Normalize�Լ�Standardize **/
	public static final byte F_DEL_N = 33;

	/** t_type: target type. 0 binary, 1 categorical 2 numerical **/
	public static final byte TARGET_TYPE_BINARY = 0;
	public static final byte TARGET_TYPE_CATEGORICAL = 1;
	public static final byte TARGET_TYPE_NUMERICAL = 2;

	public static final String PARAM_NULL = "None";

	public static final byte DCT_DATETIME = 1;
	public static final byte DCT_DATE = 2;
	public static final byte DCT_TIME = 3;
	public static final byte DCT_UDATE = 4;
	
	/** �����ھ��б任��¼��P�����ݽṹ **/
	public final static DataStruct DSP = new DataStruct(new String[] { "TYPE",
			"MERGE", "ITEM", "FREQ", "AVG", "SE", "MEDIAN", "UP", "LOW", "MIN",
			"PT", "VALUE", "OP", "MIMA", "IST", "NEWNAME", "MIINDEX", "BI",
			"MVP", "DTYPE" });

	/** �仯��P�б��������ֶκ� **/
	public static final int P_TYPE = 0;
	public static final int P_MERGE = 1;
	public static final int P_ITEM = 2;
	public static final int P_FREQ = 3;
	public static final int P_AVG = 4;
	public static final int P_SE = 5;
	public static final int P_MEDIAN = 6;
	public static final int P_UP = 7;
	public static final int P_LOW = 8;
	public static final int P_MIN = 9;
	public static final int P_PT = 10;
	public static final int P_VALUE = 11;
	public static final int P_OP = 12;
	public static final int P_MIMA = 13;
	public static final int P_IST = 14;
	public static final int P_NEWNAME = 15;
	public static final int P_MIINDEX = 16;
	public static final int P_BI = 17;
	public static final int P_MVP = 18;
	public static final int P_DATETYPE = 19;
	// �����������ֶΣ�����ģʽ�����������ֶι��̲�һ�£����Ǹ���ǰ��ļ�¼λ�ã�ʹ��ʱ���ж�����DTYPE����
	public static final int P_DATE_HOUR = 1;
	public static final int P_DATE_AM = 2;
	public static final int P_DATE_NIGHT = 3;
	public static final int P_DATE_MONTH = 4;
	public static final int P_DATE_SEASON = 5;
	public static final int P_DATE_WEEK = 6;
	public static final int P_DATE_TOTODAY = 7;
	public static final int P_DATE_REL = 8;

	public static final Integer CONST_YES = new Integer(1);
	public static final Integer CONST_NO = new Integer(0);

	public static final String CONST_OTHERS = "others";
	public static final String CONST_NULL = "missing";
	public static final Integer CONST_OTHERNUMS = Integer.MAX_VALUE;
	public static final Integer CONST_NULLNUM = new Integer(Integer.MAX_VALUE - 1);

	public static Table ptt(int cols) {
		Table P = new Table(DSP);
		for (int i = 0; i < cols; i++) {
			P.newLast();
		}
		return P;
	}

	public static synchronized void set(Table P, int ci, int prop, Object pv) {
		if (P == null) {
			return;
		}
		BaseRecord r = P.getRecord(ci + 1);
		r.set(prop, pv);
	}

	public static void set(BaseRecord r, int prop, Object pv) {
		r.set(prop, pv);
	}

	public static Object get(Table P, int ci, int prop) {
		if (P == null) {
			return null;
		}
		BaseRecord r = P.getRecord(ci + 1);
		return r.getFieldValue(prop);
	}

	public static Object get(BaseRecord r, int prop) {
		return r.getFieldValue(prop);
	}

	public static boolean isDispersed(byte type) {
		return (type == F_TWO_VALUE || type == F_ENUM);
	}

	public static boolean isContinuous(byte type) {
		return (type == F_NUMBER || type == F_COUNT);
	}

	public static double getPLevel(int size) {
		if (size > 256000) {
			return 0.005;
		} else if (size > 64000) {
			return 0.01;
		} else if (size > 16000) {
			return 0.02;
		} else if (size > 4000) {
			return 0.03;
		} else if (size > 1000) {
			return 0.04;
		} else {
			return 0.05;
		}
	}

	public static double getSmoothFactor(int size) {
		if (size >= 2000000) {
			return 50d;
		} else if (size > 1000000) {
			return size * 0.000025;
		} else if (size >= 50000) {
			return 25d;
		} else if (size > 10000) {
			return size * 0.0005;
		} else {
			return 5d;
		}
	}
}
