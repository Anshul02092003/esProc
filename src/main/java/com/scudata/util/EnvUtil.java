package com.scudata.util;

import com.scudata.common.ISessionFactory;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.JobSpace;
import com.scudata.dm.Param;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;

/**
 * ������صĹ�����
 * @author RunQian
 *
 */
public class EnvUtil {
	private static final long G = 1024 * 1024 * 1024; // 1G�Ĵ�С
	private static final int FIELDSIZE = 50; // �����ڴ�ʱÿ���ֶ�ռ�õĿռ��С
	private static final int MAXRECORDCOUNT = 20000000; // �ڴ��б��������¼����
	
	// ȡ��ǰ�����ڴ���Դ�Լ�����ɶ�������¼
	/**
	 * ȡ��ǰ�����ڴ��Լ���Դ�Ŷ�������¼
	 * @param fcount �ֶ���
	 * @return ��¼����
	 */
	public static int getCapacity(int fcount) {
		Runtime rt = Runtime.getRuntime();
		runGC(rt);
	
		long freeMemory = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		long recordCount = freeMemory / fcount / FIELDSIZE / 3;
		if (recordCount > MAXRECORDCOUNT) {
			return MAXRECORDCOUNT;
		} else {
			return (int)recordCount;
		}
	}
	
	/**
	 * �����Ƿ��п����ڴ�������α����
	 * @param rt Runtime
	 * @param table �Ѷ�������
	 * @return true�����Լ�������false���������ٶ���
	 */
	public static boolean memoryTest(Runtime rt, Sequence table) {
		int len = table.length();
		if (len >= MAXRECORDCOUNT) return false;
		
		long freeMemory = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		if (freeMemory < 200000000L) { // 200m
			return false;
		}
		
		int fcount = 1;
		Object obj = table.get(1);
		if (obj instanceof Record) {
			fcount = ((Record)obj).getFieldCount();
		}
		
		int recordCount = (int)G / (fcount * FIELDSIZE); // 1g�ڴ��Լ�ܹ����ɵļ�¼��
		
		if (freeMemory < 300000000L) { // 300m
			return len < recordCount / 10;
		} else if (freeMemory < 500000000L) { // 500m
			return len < recordCount / 5;
		} else if (freeMemory < 700000000L) { // 700m
			return len < recordCount / 3;
		} else if (freeMemory < G){ // 1g
			return len < recordCount / 2;
		} else {
			return len < recordCount * (freeMemory / G);
		}
	}
	
	/**
	 * ִ�������ռ�
	 * @param rt Runtime
	 */
	public static void runGC(Runtime rt) {
		for (int i = 0; i < 4; ++i) {
			rt.runFinalization();
			rt.gc();
			Thread.yield();
		}
	}

	/**
	 * ���ұ����������������еı���������Session�еģ������Env�е�ȫ�ֱ�����
	 * @param varName String ������
	 * @param ctx Context ����������
	 * @return Param
	 */
	public static Param getParam(String varName, Context ctx) {
		if (ctx != null) {
			Param p = ctx.getParam(varName);
			if (p != null)return p;

			JobSpace js = ctx.getJobSpace();
			if (js != null) {
				p = js.getParam(varName);
				if (p != null)return p;
			}
		}

		return Env.getParam(varName);
	}

	/**
	 * ɾ������
	 * @param varName ������
	 * @param ctx ����������
	 * @return Param ɾ���ı�����û�ҵ��򷵻ؿ�
	 */
	public static Param removeParam(String varName, Context ctx) {
		if (ctx != null) {
			Param p = ctx.removeParam(varName);
			if (p != null) {
				return p;
			}

			JobSpace js = ctx.getJobSpace();
			if (js != null) {
				p = js.removeParam(varName);
				if (p != null) {
					return p;
				}
			}
		}

		return Env.removeParam(varName);
	}

	/**
	 * ȡ���ݿ����ӹ���
	 * @param dbName ���ݿ���
	 * @param ctx ����������
	 * @return ISessionFactory
	 */
	public static ISessionFactory getDBSessionFactory(String dbName, Context ctx) {
		if (ctx == null) {
			return Env.getDBSessionFactory(dbName);
		} else {
			ISessionFactory dbsf = ctx.getDBSessionFactory(dbName);
			return dbsf == null ? Env.getDBSessionFactory(dbName) : dbsf;
		}
	}
}
