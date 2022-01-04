package com.scudata.excel;

import com.scudata.common.Logger;

/**
 * ��ȡPoi5.0.0��Poi3.17�汾�ĵ�������ʵ����
 * �������һ�£������ݰ汾��ͬ������ڲ�ͬ�İ��С�Ϊ�����ֿ����룬�ֱ�ŵ���poi-5.0.0.jar�к�poi-3.17.jar�С�
 * */
public class ExcelVersionCompatibleUtilGetter {
	private static ExcelVersionCompatibleUtilInterface e = null;
	public static String version5 = "com.scudata.excel.ExcelVersionCompatibleUtil5";
	public static String version3 = "com.raqsoft.report.util.ExcelVersionCompatibleUtil3";
	public static String version = null;
	
	public static ExcelVersionCompatibleUtilInterface getInstance(){
		if(version == null || version.length() == 0) {
			loadCompatibleUtilClassName(version5);
		}
		return getInstance(version);
	}
	
	public static void loadCompatibleUtilClassName(String cls) {
		version = cls;
		try {
			e = (ExcelVersionCompatibleUtilInterface) Class.forName(version).newInstance();
		} catch (Exception e1) {
			Logger.debug(e1.getMessage());
			try {
				e = (ExcelVersionCompatibleUtilInterface) Class.forName(version5).newInstance();
			} catch (InstantiationException e2) {
				e2.printStackTrace();
			} catch (IllegalAccessException e2) {
				e2.printStackTrace();
			} catch (ClassNotFoundException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public static ExcelVersionCompatibleUtilInterface getInstance(String cls){
		if(e == null) {
			loadCompatibleUtilClassName(cls);
		}
		return e;
	}
}
