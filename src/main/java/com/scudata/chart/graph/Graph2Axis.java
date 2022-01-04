package com.scudata.chart.graph;

import com.scudata.cellset.graph.PublicProperty;
import com.scudata.cellset.graph.config.GraphTypes;
import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.dm.Sequence;

/**
 * ˫������ͼ
 * @author Joancy
 *
 */
public class Graph2Axis extends GraphLine {
	public int yLabelInterval2 = 0;
	public double yStartValue2 = 0;
	public double yEndValue2 = 0;
	
	public Para leftSeries = null;
	public Para rightSeries = null;


	public byte type = GraphTypes.GT_2YCOLLINE;

	/**
	 * ȱʡ���캯��
	 */
	public Graph2Axis() {
	}

	protected PublicProperty getPublicProperty() {
		PublicProperty pp = super.getPublicProperty();
		if (yLabelInterval + yLabelInterval2 != 0) {
			pp.setYInterval(yLabelInterval + ";" + yLabelInterval2);
		}
		if (yStartValue + yStartValue2 != 0) {
			pp.setYStartValue(yStartValue + ";" + yStartValue2);
		}
		if (yEndValue + yEndValue2 != 0) {
			pp.setYEndValue(yEndValue + ";" + yEndValue2);
		}

		pp.setType(type);
		return pp;
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = super.getParamInfoList();//new ParamInfoList();
		paramInfos.delete("lineType");//���ߵ����ͣ�˫�����治֧��
		paramInfos.delete("line","drawLineTrend");

		ParamInfo.setCurrent(Graph2Axis.class, this);
		paramInfos.add(new ParamInfo("type", Consts.INPUT_2AXISTYPE));
		paramInfos.add(new ParamInfo("leftSeries", Consts.INPUT_NORMAL));
		paramInfos.add(new ParamInfo("rightSeries", Consts.INPUT_NORMAL));

		String group = "YAxisLabels";
		paramInfos.add(group,new ParamInfo("yLabelInterval2", Consts.INPUT_INTEGER));
		paramInfos.add(group,new ParamInfo("yStartValue2", Consts.INPUT_DOUBLE));
		paramInfos.add(group,new ParamInfo("yEndValue2", Consts.INPUT_DOUBLE));

		return paramInfos;
	}

//	�Ƿ�ָ����ϵ��������
	protected boolean isSplitByAxis(){
		return leftSeries!=null || rightSeries!=null;
	}

	private boolean containsName(Para series, String serName){
		Object val = series.getValue();
		if(val instanceof String){
			String str = ","+(String)val+",";
			if(str.indexOf(serName)>0){
				return true;
			}
			return false;
		}else if(val instanceof Sequence){
			Sequence seq = series.sequenceValue();
			Object pos = seq.pos(serName, null);
			if (pos==null){
				return false;
			}else{
				return true;
			}
		}
		throw new RuntimeException("Invalid series name:"+series);
	}
	
	
//	����Ϊ1�� ����Ϊ2 ˫��ͼ���Ǹ���÷�����ָ��ϵ�е���ȷ����
//	ͨ��ϵ��ֻ��ָ��һ�����������ʣ�µľ�ȫ����һ���ᣬ���߶�ָ��ʱ���൱������û���壬ȫ�����������жϣ����������Ϊ1�������Ķ�Ϊ2�����Ƿ�������Ķ����޹�
	protected byte getSeriesAxis(String serName){
		if(leftSeries!=null){
			if(containsName(leftSeries,serName)){
				return Consts.AXIS_LEFT;
			}else{
				return Consts.AXIS_RIGHT;
			}
		}

		if(rightSeries!=null){
			if(!containsName(rightSeries,serName)){
				return Consts.AXIS_LEFT;
			}else{
				return Consts.AXIS_RIGHT;
			}
		}
//		һ���ᶼûָ��ʱ��ʹ��ȱʡ������ֵ
		return Consts.AXIS_LEFT;
	}

}
