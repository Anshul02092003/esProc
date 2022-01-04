package com.scudata.dm;

import java.util.*;

import com.scudata.common.RQException;

//�ռ������
//�ݲ�����ʱ�Զ����
public class JobSpaceManager {
	private static ArrayList<JobSpace> spaces = new ArrayList<JobSpace>();

	public static ArrayList<JobSpace> currentSpaces(){
		return spaces;
	}
	
	public static synchronized JobSpace getSpace(String spaceId) {
		if( spaceId==null ) throw new RQException("Space id can not be null!");
		JobSpace js;
		for (int i = spaces.size()-1; i >=0; i--) {
			js = spaces.get(i);
//			if( js==null ){//|| js.getID()==null){//ԭ���ϲ�Ӧ�ó���jsΪnull�Ķ��󣬵�Ŀǰ����ԭ����null�쳣
//				spaces.remove(i);
//				continue;
//			}
//			���ڲ����ܳ��ֵ�������Ⱥ��ԣ�����Ӱ�����ܣ���������������ʱ����������Ը��ԭ��2015.6.24
			if (js.getID().equals(spaceId))
				return js;
		}

		js = new JobSpace(spaceId);
		spaces.add(js);
		return js;
	}

	public static synchronized void closeSpace(String spaceId) {
//		Logger.debug("before closeSpace "+spaceId);
		if( spaceId==null ) throw new RQException("Space id can not be null!");
		JobSpace js = null;
		for (int i = spaces.size()-1; i >=0; i--) {
			js = spaces.get(i);
//			if( js==null ){//|| js.getID()==null){
//				spaces.remove(i);
//				continue;
//			}
			if (js.getID().equals(spaceId)) {
				js.close();
				spaces.remove(i);
//				Logger.debug("Space "+spaceId+" is removed. Current spaces:"+spaces.size());
				break;
			}
		}
	}

	public static synchronized HashMap<String, Param[]> listSpaceParams() {
		HashMap<String, Param[]> hm = new HashMap<String, Param[]>();
		JobSpace js;// = JobSpaceManager.getDefSpace();
		// hm.put( js.getID(), js.getAllParams());
		ArrayList<JobSpace> al = spaces;
		if (al != null) {
			for (int i = 0; i < al.size(); i++) {
				js = al.get(i);
				hm.put(js.getID(), js.getAllParams());
			}
		}
		return hm;
	}

	/**
	 * checkTimeOut
	 */
	public synchronized static void checkTimeOut(int timeOut) {
		// ������룬timeOut��λΪ��
		for (int i = spaces.size() - 1; i >= 0; i--) {
			JobSpace js = spaces.get(i);
			if (js.checkTimeOut(timeOut)) {
				closeSpace(js.getID());
			}
		}
	}

	public static void main(String[] args) {
		String spaceId = null;//"11";
		JobSpace js = JobSpaceManager.getSpace(spaceId);
		js.setParamValue("a1", new Integer(5));

		// JobSpaceManager.removeSpace(spaceId);
		js = JobSpaceManager.getSpace(spaceId);
		Object o = js.getParam("a1").getValue();
		System.out.println(o);

	}
}
