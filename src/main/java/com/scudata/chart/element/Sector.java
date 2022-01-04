package com.scudata.chart.element;

import java.awt.*;
import java.awt.geom.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.util.Variant;
/**
 * ����ͼ��ֻ��Ӧ���ڼ�����ϵ
 * ͨ������Ϊ��ͼ
 * @author Joancy
 *
 */
public class Sector extends Ring {
	// ����������
	public Para textLineStyle = new Para(new Integer(Consts.LINE_SOLID));
	public Para textLineWeight = new Para(new Float(1));
	public Para textLineColor = new Para(Color.lightGray);

	// �б�ʾ��������ʱ�� ���ߵ����߽�
	private transient double outerRadius = 0;

	/**
	 * ȱʡ�����Ĺ��캯��
	 */
	public Sector() {
		stackType = Consts.STACK_PERCENT;
	}

	/**
	 * ���Ʊ�����
	 */
	public void drawBack() {
		super.drawBack();
		// �б�ʾ��������ʱ�� ���ߵ����߽�
		ICoor coor = getCoor();
		if (coor.isPolarCoor()) {
			PolarCoor pc = (PolarCoor) coor;
			TickAxis ta = (TickAxis) pc.getPolarAxis();
			double outLen = ta.getAxisLength() / 10;
			outerRadius = ta.getAxisLength() + outLen;
		}
	}

	/**
	 * �����м��
	 */
	public void draw() {
		ICoor coor = getCoor();
		PolarCoor pc = (PolarCoor) coor;
		EnumAxis ea = coor.getEnumAxis();
		Point2D p;
		if (categories.length() == 1) {
			int serCount = series.length();
			Point2D lastPoint = null;
			String catName = categories.get(1).toString();
			for (int s = 1; s <= serCount; s++) {
				String serName = (String) series.get(s);
				int index = Utils.indexOf(data1, catName, serName);
				if (index == 0) { // ĳ�������ϵ�е���ֵȱ��
					continue;
				}
				Object val1 = discardSeries(data1.get(index));
				Object val2 = discardSeries(data2.get(index));
				if (stackType == Consts.STACK_PERCENT) {
					if(val2 instanceof Number){
						val2 = getPercentValue(val1,val2,data1,data2);
					}else{
						val1 = getPercentValue(val2,val1,data2,data1);
					}
				}
				p = pc.getPolarPoint(val1, val2);
				TickAxis angleAxis = pc.getAngleAxis();
				double start, extent;
				if (lastPoint == null) {
					start = angleAxis.startAngle;
				} else {
					start = lastPoint.getY();
				}
				extent = p.getY();
				double angle = start + extent / 2;
				Point2D txtP = new Point2D.Double(p.getX(), angle);
				drawCategoryAndLine(s,txtP);
				lastPoint = new Point2D.Double(p.getX(), start + extent);
			}
		} else {
			int catCount = categories.length();
			TickAxis axisP = pc.getPolarAxis();
			TickAxis axisA = pc.getAngleAxis();
			double angleRange = axisA.getAxisLength();
			for( int i=1; i<=catCount; i++){
				Object cat = categories.get(i);
				double pR = axisP.getValueLen(cat);
				double pA = axisA.startAngle + angleRange*i/(catCount+1);
				p = new Point2D.Double(pR, pA);
				drawCategoryAndLine(i,p);
			}
		}
	}

	/**
	 * ��ȡ����
	 * @param ia �̶���
	 * @param index ��ֵ���
	 * @return ʵ�����ȵ�����
	 */
	public double getColumnWidth(TickAxis ia, int index) {
		ICoor coor = getCoor();
		EnumAxis ea = coor.getEnumAxis();
		double colWidth = series.length();
		if (colWidth == 0)
			colWidth = 1;
		double tmp = ia.getValueRadius(colWidth);
		return tmp;

	}

	protected boolean isFillPie() {
		return true;
	}

	// �������Լ�����
	protected void drawCategoryAndLine(int index, Point2D polarIn) {
		PolarCoor pc = (PolarCoor) getCoor();
		EnumAxis ea = pc.getEnumAxis();
		String txt;
		if (categories.length() == 1) {
			txt = Variant.toString(series.get(index));
		} else {
			txt = Variant.toString(categories.get(index));
		}
		if (!StringUtils.isValidString(txt)) {
			return;
		}
		// ��������
		Graphics2D g = e.getGraphics();
		int style = textLineStyle.intValue(index);
		float weight = textLineWeight.intValue(index);
		Point2D pIn = pc.getScreenPoint(polarIn);
		Point2D polarOut = new Point2D.Double(outerRadius, polarIn.getY());
		Point2D pOut = pc.getScreenPoint(polarOut);
		if (Utils.setStroke(g, textLineColor.colorValue(index), style, weight)) {
			Utils.drawLine(g, pIn, pOut);
		}

		String fontName = textFont.stringValue(index);
		int fontStyle = textStyle.intValue(index);
		int fontSize = textSize.intValue(index);
		Color c = textColor.colorValue(index);
		Utils.drawPolarPointText(e, txt, pc, polarOut, fontName, fontStyle,
				fontSize, c, textOverlapping);
	}

	/**
	 * ��ȡ������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(Sector.class, this);

		String group = "textline";
		paramInfos.add(group, new ParamInfo("textLineStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("textLineWeight",
				Consts.INPUT_INTEGER));
		paramInfos.add(group,
				new ParamInfo("textLineColor", Consts.INPUT_COLOR));

		paramInfos.addAll(super.getParamInfoList());

		//		ȥ�����ε�������������
		ParamInfo pi = paramInfos.getParamInfoByName("data1");
		String tmp = pi.getTitle();
		int i = tmp.indexOf('/');
		if(i>0){
			tmp = tmp.substring(0,i);
		}
		pi.setTitle( tmp );

		pi = paramInfos.getParamInfoByName("data2");
		tmp = pi.getTitle();
		i = tmp.indexOf('/');
		if(i>0){
			tmp = tmp.substring(0,i);
		}
		pi.setTitle( tmp );
		return paramInfos;
	}

	/**
	 * ��ͼǰ�ļ���׼�������ݺϷ��Լ��
	 */
	public void prepare() {
		super.prepare();
		ICoor coor = getCoor();
		if (coor.isCartesianCoor()) {
			throw new RuntimeException(
					"Sector graph does not support cartesian coordinate system.");
		}

		if (!isStacked()) { // ��ͼҪ��ѻ�����
			throw new RuntimeException(
					"Sector graph must be stacked by value or percent.");
		}
		EnumAxis ea = coor.getEnumAxis();
		if (ea.getLocation() != Consts.AXIS_LOC_POLAR) {
			throw new RuntimeException(
					"Sector graph must specify an enumeration axis as polar axis.");
		}

		// ����["����","����","����"]��ö�����ݣ����������ǰ��շ��������ͳ��ȣ����ڱ�ͼ��˵��Ӧ����ͬһ��Ȧ��Ӧ������ͬһ��
		// ���࣬�ʵ�������Ϊ: [" ,����"," ,����"," ,����"]�������� �� ���ࡣ��ͼ���ݾ�ͳһ�ˣ��շ��಻Ӱ���ǩ����
		String eaName = ea.getName();
		Sequence enumData = getAxisData(eaName);
		Sequence data = null;
		int size = enumData.length();
		for (int i = 1; i <= size; i++) {
			Object val = enumData.get(i);
			Object series = Utils.parseSeries(val);
			if (series == null) {
				if (data == null) {
					data = new Sequence();
				}
				data.set(i, " ," + val);
			} else {
				break;
			}
		}
		setAxisData(eaName, data);
		ea.gapRatio = 0;// ����ͼ��gap����Ϊ0
//		������׼������ʱ�Ե�ǰcategories��ֵ��sector�����˷���ֵ����Ҫ����prepare
		super.prepare();
	}
	
	/**
	 * ��¡����ֵ
	 * @param s ��һ������
	 */
	public void clone(Sector s){
		super.clone(s);
	}
	
	/**
	 * ��ȿ�¡��ͼԪ
	 * @return ��¡�����ͼԪ
	 */
	public Object deepClone() {
		Sector s = new Sector();
		clone(s);
		return s;
	}

}
