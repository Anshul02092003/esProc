package com.scudata.chart.graph;

import java.awt.Color;

import com.scudata.cellset.graph.PublicProperty;
import com.scudata.cellset.graph.config.GraphTypes;
import com.scudata.chart.*;
import com.scudata.chart.edit.*;

/**
 * ��ͼ
 * 
 * @author Joancy
 *
 */
public class GraphPie extends GraphBase {
	/** ��ͼ���Ƿ�����һ����ʾ */
	public boolean pieSpacing = true;
	public int pieRotation = 50; /* ����ռ����ĳ��Ȱٷֱ� */
	public int pieHeight = 70; /* ����ͼ�ĸ߶�ռ�뾶�İٷֱ�<=100 */
	
	public byte pieType = GraphTypes.GT_PIE; 
	public Color pieJoinLineColor = Color.lightGray;

	/**
	 * ȱʡ�������캯��
	 */
	public GraphPie() {
	}

	protected PublicProperty getPublicProperty() {
		PublicProperty pp = super.getPublicProperty();
		pp.setPieSpacing(pieSpacing);
		pp.setPieRotation(pieRotation);
		pp.setPieHeight(pieHeight);
		pp.setAxisColor(PublicProperty.AXIS_PIEJOIN,pieJoinLineColor );

		pp.setType( pieType );
		return pp;
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();

		ParamInfo.setCurrent(GraphPie.class, this);
		paramInfos.add(new ParamInfo("pieType", Consts.INPUT_PIETYPE));
		
		paramInfos.add(new ParamInfo("pieSpacing", Consts.INPUT_CHECKBOX));
		paramInfos.add(new ParamInfo("pieRotation", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("pieHeight", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("pieJoinLineColor", Consts.INPUT_COLOR));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}
	
}
