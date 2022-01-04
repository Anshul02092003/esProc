package com.scudata.chart.edit;

import java.util.*;

import com.scudata.chart.resources.*;
import com.scudata.common.MessageManager;

import java.io.*;

/**
 * ͼԪ������࣬ÿһ����ʵ�ֵ�ͼԪ����Ҫ��loadSystemElements()�еǼ�
 * �ǼǺ��ͼԪ�Ż��ڽ����Զ��г����Լ��Բ������б༭
 * @author Joancy
 *
 */
public class ElementLib {
	private static ArrayList<String> groupList = new ArrayList<String>(20);
	private static ArrayList<ArrayList<ElementInfo>> elementList = new ArrayList<ArrayList<ElementInfo>>(
			20);
	private static MessageManager mm = ChartMessage.get();

	static {
		loadSystemElements();
	}

	private static int indexof(ArrayList<ElementInfo> al, String name) {
		int size = al.size();
		for (int i = 0; i < size; i++) {
			ElementInfo ei = al.get(i);
			if (ei.getName().equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}

	private static ArrayList<ElementInfo> getElementList(String group) {
		int size = groupList.size();
		for (int i = 0; i < size; i++) {
			String groupTitle = (String) groupList.get(i);
			if (groupTitle.equalsIgnoreCase(group))
				return elementList.get(i);
		}
		ArrayList<ElementInfo> newElement = new ArrayList<ElementInfo>();
		groupList.add(group);
		elementList.add(newElement);
		return newElement;
	}

	/**
	 * ����ͼԪ���ƻ�ȡ��Ӧ��ͼԪ��Ϣ��
	 * @param name ����
	 * @return ͼԪ��Ϣ��
	 */
	public static ElementInfo getElementInfo(String name) {
		for (int i = 0; i < elementList.size(); i++) {
			ArrayList<ElementInfo> al = elementList.get(i);
			ElementInfo ei = getElementInfo(al, name);
			if (ei != null)
				return ei;
		}
		throw new RuntimeException(mm.getMessage("ElementLib.badelement",name));
	}

	/**
	 * ��ָ����ͼԪ��Ϣ�б��в���ͼԪ��Ϣ
	 * @param al ͼԪ��Ϣ�б�
	 * @param name ͼԪ����
	 * @return ͼԪ��Ϣ��
	 */
	public static ElementInfo getElementInfo(ArrayList<ElementInfo> al,
			String name) {
		int i = indexof(al, name);
		if (i >= 0)
			return al.get(i);
		return null;
	}

	/**
	 * �ڳ��������һ��ͼԪ��Ϣ
	 * @param group ͼԪ�����ķ���
	 * @param name ����
	 * @param className ���ȫ·������
	 */
	public static void addElement(String group, String name, String className) {
		try {
			String groupTitle = mm.getMessage(group);
			ArrayList<ElementInfo> al = getElementList(groupTitle);

			Class elemClass = Class.forName(className);
			String title = mm.getMessage(name);

			ElementInfo ei = new ElementInfo(name, title, elemClass);
			int i = indexof(al, name);
			if (i >= 0) {
				al.add(i, ei);
			} else {
				al.add(ei);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * ��ȡ��ǰ�����µ�ȫ��ͼԪ��Ϣ�б�
	 * @return ͼԪ��Ϣ�б�
	 */
	public static ArrayList<ArrayList<ElementInfo>> getElementInfoList() {
		return elementList;
	}

	/**
	 * ��ȡ��ǰ������ȫ��ͼԪ�����
	 * @return ȫ��������б�
	 */
	public static ArrayList<String> getElementTitleList() {
		return groupList;
	}

	/**
	 * �Ǽ�ȫ��ʵ�ֵ�ϵͳͼԪ
	 */
	public static void loadSystemElements() {
		String group = "axis";
		addElement(group, "MapAxis", "com.scudata.chart.element.MapAxis");
		addElement(group, "NumericAxis",
				"com.scudata.chart.element.NumericAxis");
		addElement(group, "EnumAxis", "com.scudata.chart.element.EnumAxis");
		addElement(group, "DateAxis", "com.scudata.chart.element.DateAxis");
		addElement(group, "TimeAxis", "com.scudata.chart.element.TimeAxis");

		group = "element";
		addElement(group, "Dot", "com.scudata.chart.element.Dot");
		addElement(group, "Line", "com.scudata.chart.element.Line");
		addElement(group, "Column", "com.scudata.chart.element.Column");
		// addElement(group,"Polygon","com.scudata.chart.element.Polygon");
		addElement(group, "Sector", "com.scudata.chart.element.Sector");
		addElement(group, "Text", "com.scudata.chart.element.Text");

		group = "Graph";
		addElement(group, "GraphColumn", "com.scudata.chart.graph.GraphColumn");
		addElement(group, "GraphLine", "com.scudata.chart.graph.GraphLine");
		addElement(group, "GraphPie", "com.scudata.chart.graph.GraphPie");
		addElement(group, "Graph2Axis", "com.scudata.chart.graph.Graph2Axis");

		group = "other";
		addElement(group, "BackGround", "com.scudata.chart.element.BackGround");
		addElement(group, "Legend", "com.scudata.chart.element.Legend");
	}

	/**
	 * �����Զ���ͼԪ��Ϣ
	 * @param is �Զ�����Ϣ�������ļ���
	 */
	public static void loadCustomElements(InputStream is) {
		try {
			Properties pt = new Properties();
			pt.load(is);
			int c = 0;

			for (Enumeration e = pt.propertyNames(); e.hasMoreElements();) {
				Object key = e.nextElement();
				String value = (String) pt.get(key);
				value = new String(value.getBytes("ISO-8859-1"), "gbk");
				String[] items = value.split(",");
				int pos = value.indexOf(',');
				String type = items[0];
				String title = items[1];
				String cls = items[2];
			} // for
		} catch (IOException e) {
			// Logger.error(e.getMessage());
		}
	}

}
