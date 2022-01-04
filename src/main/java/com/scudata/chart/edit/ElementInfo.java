package com.scudata.chart.edit;

import java.util.*;

import com.scudata.chart.*;
import com.scudata.common.*;
import com.scudata.expression.*;

/**
 * ͼԪ��Ϣ�Ǽ��࣬ͨ���ǼǺ��ͼԪ���Զ��г����༭����������б�����
 * @author Joancy
 *
 */
public class ElementInfo {
	private String name;
	private String title;
	private Class elementClass;

	private ArrayList chartParams;

	/**
	 * ȱʡ���캯��
	 */
	public ElementInfo() {
	}

	/**
	 * ����ָ��ֵ����ͼԪ��Ϣ
	 * @param name ����
	 * @param title ����
	 * @param elementClass ͼԪ��ʵ����
	 */
	public ElementInfo(String name, String title, Class elementClass) {
		this.name = name;
		this.title = title;
		this.elementClass = elementClass;
	}

	private ParamInfoList listParamInfoList() {
		try {
			IElement element = (IElement) elementClass.newInstance();
			ParamInfoList pil = element.getParamInfoList();
			return pil;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	/**
	 * ʵ����һ��ͼԪ
	 * @return ���ػ���ͼԪ����
	 */
	public ObjectElement getInstance() {
		try {
			return (ObjectElement) elementClass.newInstance();
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	/**
	 * ��plotString���ݶ�ȡ�༭��Ϣ
	 * @param plotString plot��ʽ���ı���
	 */
	public void setPlotString(String plotString) {
		int len = plotString.length();
		String paramString = plotString.substring(5, len - 1);
		ArgumentTokenizer at = new ArgumentTokenizer(paramString, ',');
		name = Escape.removeEscAndQuote(at.next());
		ElementInfo ei = ElementLib.getElementInfo(name);
		this.title = ei.getTitle();
		this.elementClass = ei.getElementClass();

		chartParams = new ArrayList();
		while (at.hasNext()) {
			ChartParam cp = new ChartParam();
			cp.setPlotString(at.next());
			chartParams.add(cp);
		}
	}

	/**
	 * ���༭�ı����ڵĲ����б�����ת��Ϊplot��ʽ�ı���
	 * @param pil �༭�ò����Ĳ�����Ϣ�б�
	 * @return plot��ʽ�ı���
	 */
	public String toPlotString(ParamInfoList pil) {
		StringBuffer sb = new StringBuffer("plot(");
		sb.append("\"" + name + "\"");
		List allParams = pil.getAllParams();
		int size = allParams.size();
		for (int i = 0; i < size; i++) {
			ParamInfo pi = (ParamInfo) allParams.get(i);
			String paramPlot = pi.toPlotString(pi.getDefValue());
			if (paramPlot == null)
				continue;
			sb.append(",");
			sb.append(paramPlot);
		}
		sb.append(")");
		return sb.toString();
	}

	public void setProperties(String elementName, HashMap<String,Object> properties) {
		name = elementName;
		ElementInfo ei = ElementLib.getElementInfo(name);
		this.title = ei.getTitle();
		this.elementClass = ei.getElementClass();

		chartParams = new ArrayList();
		Iterator it = properties.keySet().iterator();
		while (it.hasNext()) {
			String pName = (String)it.next();
			Object pValue = properties.get(pName);
			ChartParam cp = new ChartParam(pName,pValue);
			chartParams.add(cp);
		}
	}
	
	public HashMap<String,Object> getProperties(ParamInfoList pil) {
		HashMap<String,Object> properties = new HashMap<String,Object>();
		List allParams = pil.getAllParams();
		int size = allParams.size();
		for (int i = 0; i < size; i++) {
			ParamInfo pi = (ParamInfo) allParams.get(i);
			String paramPlot = pi.toPlotString(pi.getDefValue());
			if (paramPlot == null)
				continue;
			properties.put(pi.getName(),pi.getValue());
		}
		return properties;
	}

	public void setChartParams(ArrayList chartParams) {
		this.chartParams = chartParams;
	}

	/**
	 * ��ͼԪ�Ĳ�������תΪ�༭�õĲ�����Ϣ�б�
	 * @return �༭�ò�����Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = listParamInfoList();
		if (chartParams == null)
			return paramInfos;
		int chartSize = chartParams.size();
		for (int i = 0; i < chartSize; i++) {
			ChartParam cp = (ChartParam) chartParams.get(i);
			ParamInfo pi = paramInfos.getParamInfoByName(cp.getName());
			if (pi == null) {// �������˵Ĳ���
				continue;
			}
			pi.setChartParam(cp);
		}
		return paramInfos;
	}

	/**
	 * ��ȡͼԪ����
	 * @return ����
	 */
	public String getName() {
		return name;
	}

	/**
	 * ��ȡͼԪ����
	 * @return ����
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * ��ȡͼԪ��ʵ����
	 * @return ͼԪ��
	 */
	public Class getElementClass() {
		return elementClass;
	}

}
