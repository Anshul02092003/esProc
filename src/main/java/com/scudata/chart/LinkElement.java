package com.scudata.chart;

import java.awt.Shape;
import java.util.ArrayList;

import com.scudata.chart.edit.ParamInfo;
import com.scudata.chart.edit.ParamInfoList;
import com.scudata.common.StringUtils;

/**
 * ������ͼԪ�ĳ�����
 * @author Joancy
 *
 */
public abstract class LinkElement extends ObjectElement{
	//�����Ӳ���ʹ�ú꣬��Ϊ��dfx�����ÿ��data��ͼԪ�Ķ�Ӧ�������У�����ʱ��ѭ��ʹ�ã�
	public Para htmlLink = new Para(null);
	public Para tipTitle = new Para(null);// ��tipTitleû��ָ��ʱ��Ĭ��ʹ��data������
	public Para linkTarget = new Para("_blank");

	private transient ArrayList<Shape> shapes = new ArrayList<Shape>();
	private transient ArrayList<String> links = new ArrayList<String>();
	private transient ArrayList<String> titles = new ArrayList<String>();// ���ָ������ʱ����������ʾ��Ϣ
	private transient ArrayList<String> targets = new ArrayList<String>();
	/**
	 * ��ȡͼԪ�����ӱ߽���״
	 * 
	 * @return Shape �߽���״�б�
	 */
	public ArrayList<Shape> getShapes() {
		return shapes;
	}

	/**
	 * ��ȡ�������б�
	 * @return ������
	 */
	public ArrayList<String> getLinks() {
		return links;
	}

	/**
	 * ��ȡ��ʾ��Ϣ�б�
	 * @return ��ʾ��Ϣ
	 */
	public ArrayList<String> getTitles() {
		return titles;
	}

	/**
	 * ��ȡ������Ŀ��򿪷�ʽ�б�
	 * @return Ŀ��򿪷�ʽ
	 */
	public ArrayList<String> getTargets() {
		return targets;
	}

	protected String getTipTitle(int index) {
		if (tipTitle != null) {
			String t = tipTitle.stringValue(index);
			if (t != null) {
				return t;//.toString();
			}
		}
		return null;
	}

	protected void addLink(Shape shape, String link, String title,String target) {
		shapes.add(shape);
		if (StringUtils.isValidString(link)) {
			links.add(link);
			titles.add(title);
			targets.add(target);
		}
	}

	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		
		ParamInfo.setCurrent(LinkElement.class, this);
		paramInfos.add(new ParamInfo("htmlLink", Consts.INPUT_NORMAL));
		paramInfos.add(new ParamInfo("tipTitle", Consts.INPUT_NORMAL));
		paramInfos.add(new ParamInfo("linkTarget", Consts.INPUT_NORMAL));

		return paramInfos;
	}

}
