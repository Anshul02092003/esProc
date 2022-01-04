package com.scudata.chart.element;

import java.util.ArrayList;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.dm.*;

import java.awt.Shape;

//ӳ���ᣬֻ��Ϊ�߼�ֵ������ֵ��һ��ӳ�䣬��ʵ�ʻ�ͼ
public class MapAxis extends ObjectElement implements IMapAxis{
	// ������
	public String name;
	// �߼�ֵ����
	public Sequence logicalData;

	// ����ֵ��ͼ����Ŀǰ��֧����ɫ�����͡�����
	public Sequence physicalData;

//	ӳ������������ݱ�����������ӳ������
	public Object getMapValue(Object val,byte mapProperty){
		return getPhyValue(val);
	}

	/**
	 * getName
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	public static Object getMapValue(Sequence s1,Object v1,Object s2) {
		int index = s1.firstIndexOf(v1);
		if (index < 1)
			return v1;
		if(s2 instanceof Sequence ){
			Sequence map = (Sequence)s2;
			int phySize = map.length();
			index = index % phySize;
			if (index == 0) {
				index = phySize;
			}
			return map.get(index);
		}
		Para para = (Para)s2;
		return para.objectValue(index);
	}
	
	public Object getPhyValue(Object lgcValue) {
		return getMapValue(logicalData,lgcValue,physicalData);
	}

	/**
	 * isVisible
	 * 
	 * @return boolean
	 */
	public boolean isVisible() {
		return false;
	}

	public void beforeDraw() {
	}

	/**
	 * draw
	 */
	public void draw() {
	}

	/**
	 * drawBack
	 */
	public void drawBack() {
	}

	/**
	 * drawFore
	 */
	public void drawFore() {
	}

	/**
	 * getEngine
	 * 
	 * @return Engine
	 */
	public Engine getEngine() {
		return null;
	}

	/**
	 * getParamInfoList
	 * 
	 * @return ParamInfoList
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(MapAxis.class, this);
		paramInfos.add(new ParamInfo("name"));
		paramInfos.add(new ParamInfo("logicalData"));
		paramInfos.add(new ParamInfo("physicalData"));
		return paramInfos;
	}

	/**
	 * getShape
	 * 
	 * @return Shape
	 */
	public ArrayList<Shape> getShapes() {
		return null;
	}

	public ArrayList<String> getLinks() {
		return null;
	}

	/**
	 * setEngine
	 * 
	 * @param e
	 *            Engine
	 */
	public void setEngine(Engine e) {
	}

}
