package com.scudata.chart.graph;

import java.awt.Color;

import com.scudata.cellset.graph.PublicProperty;
import com.scudata.cellset.graph.config.GraphTypes;
import com.scudata.chart.*;
import com.scudata.chart.edit.*;

/** 
 * ����ͼ������ͼ
 * 
 */
public class GraphColumn extends GraphElement {

	public int barDistance;

	public byte columnType = GraphTypes.GT_COL; 
	public Color columnBorderColor = null;//��ɫ����Ϊnull��ʾΪ͸��ɫ��

	/**
	 * ȱʡ�������캯��
	 */
	public GraphColumn() {
	}

	protected PublicProperty getPublicProperty() {
		PublicProperty pp = super.getPublicProperty();
		pp.setBarDistance(barDistance);
		pp.setType( columnType );
		pp.setAxisColor(PublicProperty.AXIS_COLBORDER,columnBorderColor );

		return pp;
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(GraphColumn.class, this);
		
		paramInfos.add(new ParamInfo("columnType", Consts.INPUT_COLUMNTYPE));
		paramInfos.add(new ParamInfo("barDistance", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("columnBorderColor", Consts.INPUT_COLOR));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}
	
}
