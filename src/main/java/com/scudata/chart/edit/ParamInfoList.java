package com.scudata.chart.edit;

import java.util.*;

import com.scudata.chart.resources.*;
import com.scudata.common.*;

/**
 * ������Ϣ�б��࣬���ڱ༭������г�ͼԪ�Ŀɱ༭������Ϣ
 * @author Joancy
 *
 */
public class ParamInfoList {
	ArrayList<ArrayList<ParamInfo>> paramGroups = new ArrayList<ArrayList<ParamInfo>>();
	ArrayList<String> groupNames = new ArrayList<String>();

	ArrayList<ParamInfo> rootList = new ArrayList<ParamInfo>();
	private final static String ROOTGROUP = "RootGroup";
	private MessageManager mm = ChartMessage.get();

	/**
	 * ȱʡ�������캯��
	 */
	public ParamInfoList() {
	}

	private ArrayList<ArrayList<ParamInfo>> getParamGroups() {
		return paramGroups;
	}

	private ArrayList<ParamInfo> getrootList() {
		return rootList;
	}

	/**
	 * ����һ��������Ϣ�б������ȫ����ӵ���ǰ�б�
	 * @param pil ������Ϣ�б�
	 */
	public void addAll(ParamInfoList pil) {
		//pil�з�������Ҫ��������������ͬ���ƺϲ����ĳ����´���
		ArrayList<String> groupNames = pil.getGroupNames();
		for( int i=0; i<groupNames.size(); i++){
			String grpName = groupNames.get(i);
			ArrayList<ParamInfo> grpParams = pil.getParams(grpName);
			for( int n=0;n<grpParams.size(); n++){
				add(grpName,grpParams.get(n));
			}
		}
		rootList.addAll(pil.getrootList());
	}

	/**
	 * ��һ��������Ϣpi��ӵ�����group��
	 * @param group ������
	 * @param pi ������Ϣ
	 */
	public void add(String group, ParamInfo pi) {
		ArrayList<ParamInfo> pis = null;
		if (group == null || ROOTGROUP.equalsIgnoreCase(group)) {
			pis = rootList;
		} else {
			group = mm.getMessage(group);
			int index = groupNames.indexOf(group);
			if (index < 0) {
				groupNames.add(group);
				pis = new ArrayList<ParamInfo>();
				paramGroups.add(pis);
			} else {
				pis = paramGroups.get(index);
			}
		}
		if (pis == null) {
			pis = rootList;
		}
		pis.add(pi);
	}
	
	/**
	 * ĳЩ���಻��Ҫ�����ж���õĲ�����Ϣʱ������ɾ����
	 * �ú���ɾ���������µ���Ϣ
	 * @param group ��������
	 */
	public void deleteGroup(String group){
		group = mm.getMessage(group);
		int index = groupNames.indexOf(group);
		if (index >=0 ) {
			groupNames.remove(index);
			paramGroups.remove(index);
		}
	}

	/**
	 * ɾ������group�µ�һ��������Ϣ
	 * @param group ������
	 * @param name ������Ϣ������
	 */
	public void delete(String group, String name){
		group = mm.getMessage(group);
		delete(getParams(group),name);
	}
	
	/**
	 * ����·���£�Ҳ���������κη��飬����һ��������Ϣ
	 * @param pi ������Ϣ
	 */
	public void add(ParamInfo pi) {
		rootList.add(pi);
	}
	
	/**
	 * ɾ����Ŀ¼�µĲ�����Ϣ
	 * @param name ��������
	 */
	public void delete(String name){
		delete(rootList,name);
	}
	
	/**
	 * ��ָ���Ĳ�����Ϣ�б���ɾ��һ��������Ϣ
	 * @param list ������Ϣ�б�
	 * @param name Ҫɾ���Ĳ�������
	 */
	public void delete(ArrayList<ParamInfo> list,String name){
		for(int i=0; i<list.size();i++){
			ParamInfo pi = list.get(i);
			if(pi.getName().equalsIgnoreCase(name)){
				list.remove(i);
				return;
			}
		}
	}

	/**
	 * �г���ǰ��Ϣ�б��е�ȫ����������
	 * @return ���������б�
	 */
	public ArrayList<String> getGroupNames() {
		return groupNames;
	}

	/**
	 * ��ȡһ����������в�����Ϣ�б�
	 * @param groupName ��������
	 * @return ������Ϣ�б�
	 */
	public ArrayList<ParamInfo> getParams(String groupName) {
		ArrayList<ParamInfo> pis = null;
		int index = groupNames.indexOf(groupName);
		if (index >= 0) {
			pis = paramGroups.get(index);
		}
		if (pis != null)
			return pis;
		return rootList;
	}

	/**
	 * ��ȡ��Ŀ¼�µ�ȫ��������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ArrayList<ParamInfo> getRootParams() {
		return rootList;
	}

	/**
	 * �����в�����Ϣ���ҵ���Ӧ�Ĳ�����Ϣ
	 * @param name ��������
	 * @return ������Ϣ
	 */
	public ParamInfo getParamInfoByName(String name) {
		ArrayList<ParamInfo> aps = getAllParams();
		int infoSize = aps.size();
		for (int i = 0; i < infoSize; i++) {
			ParamInfo pi = (ParamInfo) aps.get(i);
			if (pi.getName().equalsIgnoreCase(name)) {
				return pi;
			}
		}
		return null;
	}

	/**
	 * �г����в�����Ϣ���ۺ��б�������Ŀ¼�Լ��������з���
	 * @return ȫ��������Ϣ�б�
	 */
	public ArrayList<ParamInfo> getAllParams() {
		ArrayList<ParamInfo> aps = new ArrayList<ParamInfo>();
		int size = rootList == null ? 0 : rootList.size();
		for (int i = 0; i < size; i++) {
			aps.add(rootList.get(i));
		}
		size = paramGroups == null ? 0 : paramGroups.size();
		for (int i = 0; i < size; i++) {
			ArrayList<ParamInfo> pis = paramGroups.get(i);
			int ps = pis == null ? 0 : pis.size();
			for (int j = 0; j < ps; j++) {
				aps.add(pis.get(j));
			}
		}
		return aps;
	}

}
