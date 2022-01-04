package com.scudata.dm.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * ���������̶��ı�׼������Ϣ
 * @author RunQian
 *
 */
public class FixedParamFunInfo extends FunInfo {
	public static final String NONSUPPORT = "N/A";

	private HashMap<Integer, String> infoMap = new HashMap<Integer, String>();

	public FixedParamFunInfo() {
	}

	public FixedParamFunInfo(String name, int pcount) {
		super(name, pcount);
	}

	/*
	 * infoֵ������3������� Ϊnull����ʾ���׼����һ�� ΪN/A(�����ִ�Сд)����ʾ��֧�ִ˺���
	 * �����ʾ���ݿ�SQL���ʽ����?n��ʾ��׼�����ĵ�n��������
	 */

	public String getInfo(int dbType) {
		return infoMap.get(new Integer(dbType));
	}

	public void setInfo(int dbType, String info) {
		infoMap.put(new Integer(dbType), info);
	}

	public Map<Integer, String> getInfos() {
		return infoMap;
	}
}
