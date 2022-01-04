package com.scudata.chart.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.lang.Math;
import java.util.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.util.Variant;

/**
 * ��ֵ��
 * @author Joancy
 *
 */
public class NumericAxis extends TickAxis {
	// �Զ��������Сֵ�ķ�Χ
	public boolean autoCalcValueRange = true;
	// �Զ���Χʱ��0��ʼֵ
	public boolean autoRangeFromZero = true;

	// ֵ�����ԣ����ֵ
	public double maxValue = 10;

	// ֵ�����ԣ���Сֵ
	public double minValue = 0;

	// ֵ�����ԣ��̶���
	public int scaleNum = 5;

	// ֵ�����ԣ��̶���ʾ��ʽ
	public String format = "#.##";

	// ��λ����
	public String unitFont;// = "����";

	// ��ɫ
	public Color unitColor = Color.blue.darker();

	// ��ʽ
	public int unitStyle;

	// ��ת
	public int unitAngle = 0;

	// ��С
	public int unitSize = 12;

	// �任
	public int transform = 0; // 0�ޱ任��1������2������3ָ������Consts�ж��峣����

	// ����Ǳ����任�������߶�, 1:scale
	public double scale = 1;

	// ����Ƕ����任�������ĵ���
	public double logBase = 10;

	// �����ָ���任��ָ���ĵ���
	public double powerExponent = Math.E;

	/* �����߶��� */
	public Para warnLineStyle = new Para(new Integer(Consts.LINE_DASHED));
	public Para warnLineWeight = new Para(new Float(1));
	public Para warnLineColor = new Para(Color.red);
	public Sequence warnLineData = null;

	/**
	 * ȱʡ�������캯��
	 */
	public NumericAxis() {
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(NumericAxis.class, this);

		String group = "numericaxis";
		paramInfos.add(group, new ParamInfo("autoCalcValueRange",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("autoRangeFromZero",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("maxValue", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("minValue", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("scaleNum", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("format"));

		group = "transform";
		paramInfos.add(group,
				new ParamInfo("transform", Consts.INPUT_TRANSFORM));
		paramInfos.add(group, new ParamInfo("scale", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("logBase", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("powerExponent",
				Consts.INPUT_DOUBLE));

		group = "warnlines";
		paramInfos.add(group, new ParamInfo("warnLineStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("warnLineWeight",
				Consts.INPUT_DOUBLE));
		paramInfos.add(group,
				new ParamInfo("warnLineColor", Consts.INPUT_COLOR));
		paramInfos.add(group,
				new ParamInfo("warnLineData", Consts.INPUT_NORMAL));

		group = "unit";
		paramInfos.add(group, new ParamInfo("unitFont", Consts.INPUT_FONT));
		paramInfos.add(group,
				new ParamInfo("unitStyle", Consts.INPUT_FONTSTYLE));
		paramInfos.add(group, new ParamInfo("unitSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group, new ParamInfo("unitAngle", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("unitColor", Consts.INPUT_COLOR));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}
	
	/**
	 * ��ȡ���ݵ�ʵ��ֵ�������ı���д��
	 * @param val ����ֵ
	 * @return ��Ӧ��ʵ��
	 */
	public double getNumber(Object val){
		double tmp;
		if (val instanceof Number) {
			tmp = ((Number) val).doubleValue();
		} else {
			tmp = Double.parseDouble(val.toString());
		}
		return tmp;
	}

	double getValueLength(Object val, boolean isAbsolute) {
		double axisLen = getAxisLength();
		double tmp = getNumber(val);
		double len = 0;
		if (isAbsolute) {
			Number nMax = recoverTickValue(t_maxValue);
			Number nMin = recoverTickValue(t_minValue);
			len = axisLen * (tmp / (nMax.doubleValue() - nMin.doubleValue()));
		} else {
			tmp = transform(tmp);
			len = axisLen * (tmp - t_minValue) / (t_maxValue - t_minValue);
		}
		return len;
	}

	/*
	 * �̶�ֵ��������ƽ�����䣬�Ҹ����λ���й�
	 */
	protected double getTickPosition(Object tickVal) {
		double val = ((Number) tickVal).doubleValue();
		double axisLen = getAxisLength();
		double len = axisLen * (val - t_minValue) / (t_maxValue - t_minValue);

		double pos = 0;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			pos = getLeftX() + len;
			break;
		case Consts.AXIS_LOC_V:
			pos = getBottomY() - len;
			break;
		case Consts.AXIS_LOC_ANGLE:
			pos = startAngle + len;
			break;
		}

		return pos;
	}

	/**
	 * ��ȡ��ֵ�������
	 * @return Point �����
	 */
	public Point2D getBasePoint() {
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			return new Point2D.Double(t_baseValueLine, getBottomY());
		case Consts.AXIS_LOC_V:
			return new Point2D.Double(getLeftX(), t_baseValueLine);
		}
		return null;
	}

	/**
	 * ��ȡ�����ӵ���״����
	 * 
	 * @return Shape �����壬����null
	 */
	public Shape getShape() {
		return null;
	}

	private double max(double v, Object d) {
		double max = Double.NEGATIVE_INFINITY;
		if (d instanceof Sequence) {
			Sequence al = (Sequence) d;
			max = ((Number) al.max()).doubleValue();
		} else {
			max = getNumber(d);
		}
		return Math.max(max, v);
	}
	
	private double min(ArrayList dataElements) {
		double minValue = Double.POSITIVE_INFINITY;
		for (int i = 0; i < dataElements.size(); i++) {
			DataElement de = (DataElement) dataElements.get(i);
			if(de.isPhysicalCoor()){
				continue;
			}
			Sequence data = de.getAxisData(name);
			minValue = Math.min(minValue,
					((Number) data.min()).doubleValue());
			if(de instanceof Column){
				Column col = (Column)de;
				Sequence data3 = col.getData3();
				if(data3!=null){
					minValue = Math.min(minValue,((Number) data3.min()).doubleValue());					
				}
			}
		}
		return minValue;
	}

	/**
	 * ��ͼǰ������׼��
	 */
	public void prepare(ArrayList<DataElement> dataElements) {
		super.prepare(dataElements);
		// ��һ��ֵ���ϵ�ͼԪ�в�ͬ�Ķѻ�����ʱ��ȡ���Ķѻ�����Ϊ��������
		int stackType = Consts.STACK_NONE;
		for (int i = 0; i < dataElements.size(); i++) {
			DataElement de = dataElements.get(i);
			if(de.isPhysicalCoor()){
				continue;
			}
			
			int tmp;
			if (de instanceof Column) {
				tmp = ((Column) de).stackType;
				if (tmp > stackType) {
					stackType = tmp;
				}
			}
			if (de instanceof Sector) {
				tmp = ((Sector) de).stackType;
				if (tmp > stackType) {
					stackType = tmp;
				}
			}
			if (de instanceof Line) {
				tmp = ((Line) de).stackType;
				if (tmp > stackType) {
					stackType = tmp;
				}
			}
			if (stackType >= Consts.STACK_VALUE)
				break;
		}

		// ����Ҳ�����Զ�����
		if (autoCalcValueRange){
			maxValue = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < dataElements.size(); i++) {
				DataElement de = (DataElement) dataElements.get(i);
				if(de.isPhysicalCoor()){
					continue;
				}
				de.parseNumericAxisData(name);

				Object data;
				if (de instanceof Column) {
					Column co = (Column) de;
					co.stackType = stackType;// ���µ���ͼԪ�Ķѻ�����
					data = Column.getMaxValue(de, name);
				} else if (de instanceof Line) {
					Line li = (Line) de;
					li.stackType = stackType;
					data = Column.getMaxValue(de, name);
				} else if (de instanceof Sector) {
					Sector se = (Sector) de;
					se.stackType = stackType;// ���µ���ͼԪ�Ķѻ�����
					data = Column.getMaxValue(de, name);
				} else {
					data = de.getAxisData(name);
				}
				maxValue = max(maxValue, data);
			}

			if (stackType == Consts.STACK_PERCENT || autoRangeFromZero) {
				if(maxValue>0){
					minValue = 0;	
				}else{
					maxValue=0;
					minValue = min(dataElements);
				}
			} else{
				minValue = min(dataElements);
			}
			
			t_maxValue = transform(Math.max(maxValue, minValue));
			t_minValue = transform(Math.min(maxValue, minValue));

			double absMax = Math
					.max(Math.abs(t_maxValue), Math.abs(t_minValue));
			double tmpScale = 1;
			while (absMax < scaleNum) { // ȥ��С��
				tmpScale *= 0.1;
				absMax *= 10;
			}
			absMax = Math.ceil(absMax);
			if (scaleNum < 1) {// �̶���ĿС��1ʱ���Զ������һ���պó������ֵ����Ŀ
				int tmp = 5;
				double leave = absMax % tmp;
				while (leave > 0) {
					tmp--;
					leave = absMax % tmp;
				}
				scaleNum = tmp;
			}

				double delta = 0;
				if (stackType == Consts.STACK_NONE){
//					���������ֵ��ͷ�ı�ǩ����10%����ֹ��ֵ�պ�����������
					delta = (t_maxValue - t_minValue)*0.1; // ������ֵ��϶
				}
				if (t_minValue >= 0) { // ȫΪ����
					t_maxValue += delta;
					if(t_minValue>0){
						t_minValue -= delta;	
					}
				} else if (t_maxValue <= 0) { // ȫΪ����
					t_minValue -= delta;
					if(t_maxValue<0){
						t_maxValue += delta;	
					}
				} else { // �����и�
					t_maxValue += delta;
					t_minValue -= delta;
				}
		} else {
			t_maxValue = transform(Math.max(maxValue, minValue));
			t_minValue = transform(Math.min(maxValue, minValue));
		}

		switch (transform) {
		case Consts.TRANSFORM_SCALE:
			scale = Math.abs(scale);
			if (scale > 1) {
				t_unitText = "1:" + Utils.format(scale, "#,###");
			} else {
				t_unitText = "1:" + scale;
			}
			break;
		case Consts.TRANSFORM_LOG:
			// ������ָ�����Ѿ�ʹ��ԭֵ��Ϊ����ֵ��������д�ϱ任��λ
			break;
		case Consts.TRANSFORM_EXP:
			break;
		}

		if (!(autoCalcValueRange || stackType > Consts.STACK_NONE)) {
			// �Զ������ѻ�ͼ���߽��ᣬ�������û�ֵ
			t_baseValue = t_minValue;
		}

		createCoorValue();
	}

	/**
	 * �����
	 * @param base ��
	 * @param d ��ֵ
	 * @return ����ֵ
	 */
	public static double log(double base, double d) {
		if (base < 0 || d <= 0 || base == 1) {
			return 0;
		}
		return Math.round(Math.log(d) / Math.log(base) * 1000000d) / 1000000d;
	}

	/**
	 * ��ָ��
	 * @param base ��
	 * @param exp ָ��
	 * @return ָ��ֵ
	 */
	public static double power(double base, double exp) {
		if (base < 0 || exp < 0 || base == 1) {
			return 0;
		}
		return Math.round(Math.exp(exp * Math.log(base)) * 1000000d) / 1000000d;
	}

	/*
	 * ������ָ���任ʱ�������굥λ��ԭ
	 */
	private double recoverTickValue(double value) {
		switch (transform) {
		case Consts.TRANSFORM_LOG:
			return power(logBase, value);
		case Consts.TRANSFORM_EXP:
			return log(powerExponent, value);
		default:
			return value;
		}
	}

	private double transform(double value) {
		switch (transform) {
		case Consts.TRANSFORM_SCALE:
			return value / scale;
		case Consts.TRANSFORM_LOG:
			return log(logBase, value);
		case Consts.TRANSFORM_EXP:
			return power(powerExponent, value);
		case Consts.TRANSFORM_NONE:
		default:
			return value;
		}
	}

	private void createCoorValue() {
		double tmp, delta;
		if (t_minValue >= 0.0 || t_maxValue <= 0) { // ************************************ȫΪ��������ȫΪ����
			double dCoor = (t_maxValue - t_minValue) / scaleNum;
			delta = Math.ceil(dCoor);
			double cha = delta - dCoor;
			boolean isLeagalRange = cha <= dCoor * 0.2;// ����������ֵ���ܳ���ԭֵ��20%;

			if (autoCalcValueRange && isLeagalRange) {// ֵ�ķ�Χ����1����һ�����ֵ���� //t_maxValue-t_minValue > 1
								// && delta - dCoor<dCoor
				t_maxValue = Math.ceil(t_maxValue);// ȡ��
				t_minValue = Math.floor(t_minValue);
				delta = Math.ceil((t_maxValue - t_minValue) / scaleNum);// �����ֵȡ��
				for (int i = 0; i <= scaleNum; i++) {
					tmp = t_minValue + i * delta;
					t_coorValue.add(new Double(tmp));
				}
				t_maxValue = t_minValue + scaleNum * delta;// �����������ֵ
			} else {
				delta = (t_maxValue - t_minValue) / scaleNum;
				for (int i = 0; i <= scaleNum; i++) {
					tmp = t_minValue + i * delta;
					t_coorValue.add(new Double(tmp));
				}
			}
		} else { // ************************************�����и�
			double absMax = t_maxValue;
			double absMin = Math.abs(t_minValue);
			delta = Math.max(absMax, absMin) / scaleNum;

			t_coorValue.add(new Double(0));
			boolean positiveFull = false;
			boolean negativeFull = false;
			for (int i = 1; i <= scaleNum; i++) {
				tmp = i * delta;
				if (!positiveFull) {
					if (tmp <= absMax) {
						t_coorValue.add(new Double(tmp));
					} else {
						t_maxValue = tmp;
						positiveFull = true;
						t_coorValue.add(new Double(tmp));
					}
				}

				if (!negativeFull) {
					if (tmp <= absMin) {
						t_coorValue.add(new Double(-tmp));
					} else {
						t_minValue = -tmp;
						negativeFull = true;
						t_coorValue.add(new Double(-tmp));
					}
				}
			}
			t_coorValue.sort("o");
		}

	}

	String getCoorText(Object coorValue) {
		Number coory = (Number) coorValue;
		Number d = recoverTickValue(((Number) coorValue).doubleValue());
		return Utils.format(d, format);
	}

	/**
	 * �趨������ݷ�Χ
	 * ��ͼ�Ǹ�����ͼ�Σ������ͼ�Ĺ����У�Ҫ���ݲ�ͬ���ද̬����ֵ��Χ
	 * ��ʱ�������Ѿ�û�����壬һ������£�����ϵҲû�л������ı�Ҫ
	 * @param max ���ֵ
	 * @param min ��Сֵ
	 */
	public void setValueRange(double max, double min) {
		t_maxValue = max;
		t_minValue = min;
	}

	/**
	 * ��ͼǰ������׼������
	 */
	public void beforeDraw() {
		double length = getAxisLength();

		double dLen = length * (t_baseValue - t_minValue)
				/ (t_maxValue - t_minValue);
		double end;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			end = getLeftX();
			t_baseValueLine = (int) (end + dLen);
			break;
		case Consts.AXIS_LOC_V:
			end = getBottomY();
			t_baseValueLine = (int) (end - dLen);
			break;
		case Consts.AXIS_LOC_ANGLE:

			// nothing to do
			break;
		}
	}

	/**
	 * ����ǰ����
	 */
	public void drawFore() {
		if (!isVisible()) {
			return;
		}
		super.drawFore();

		// Draw axis UnitText
		if (!StringUtils.isValidString(t_unitText)) {
			return;
		}
		Font font = Utils.getFont(unitFont, unitStyle, unitSize);
		Graphics2D g = e.getGraphics();
		double x, y;

		int locationType = 0;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			x = getRightX() + labelIndent;// + textSize.width;
			y = getBottomY();
			Utils.drawText(e, t_unitText, x, y, font, unitColor, unitStyle,
					unitAngle, Consts.LOCATION_LM, true);
			break;
		case Consts.AXIS_LOC_V:
			x = getLeftX();
			y = getTopY() - labelIndent;// - textSize.height;
			Utils.drawText(e, t_unitText, x, y, font, unitColor, unitStyle,
					unitAngle, Consts.LOCATION_CB, true);
			break;
		case Consts.AXIS_LOC_ANGLE:
			// ���᲻����
			break;
		}

	}

	private Number warnLineData(int index) {
		Object val = warnLineData.get(index);
		if (val == null) {
			return new Double(0);
		}
		if (val instanceof Number) {
			return (Number) val;
		}
		Number n = Variant.parseNumber(val.toString());
		if (n == null) {
			throw new RQException("Warn line data: [ " + val
					+ " ] is not a number!");
		}
		return n;
	}

	private void drawWarnLine(int index, Point2D p, double x1, double y1,
			double x2, double y2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		drawWarnShape(index, p, line, null);
	}

	private void drawWarnShape(int index, Point2D p, Shape shape,
			Point2D screenPoint) {
		Color wc = warnLineColor.colorValueNullAsDef(index);
		int ws = warnLineStyle.intValue(index);
		float ww = warnLineWeight.floatValue(index);
		Graphics2D g = e.getGraphics();
		if (Utils.setStroke(g, wc, ws, ww)) {
			g.draw(shape);
			String txt = getCoorText(warnLineData(index));
			int locationType = adjustLabelPosition(p);
			Font font = Utils.getFont(labelFont, labelStyle, labelSize);
			double x, y;
			if (screenPoint == null) {
				x = p.getX();
				y = p.getY();
			} else {
				x = screenPoint.getX();
				y = screenPoint.getY();
			}
			Utils.drawText(e, txt, x, y, font, wc, labelStyle, labelAngle,
					locationType, labelOverlapping);
		}
	}

	/**
	 * �����м��
	 */
	public void draw() {
		super.draw();
		if (warnLineData == null) {
			return;
		}

		ArrayList<ICoor> coorList = e.getCoorList();
		Shape warnShape;
		double x, y;
		Point2D p;
		Graphics2D g = e.getGraphics();
		int ws, locationType;
		float ww;
		Color wc;
		int tCount = warnLineData.length();

		switch (location) {
		case Consts.AXIS_LOC_H:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isPolarCoor()) {
					continue;
				}
				CartesianCoor cc = (CartesianCoor) coor;
				if (cc.getXAxis() != this) {
					continue;
				}
				int coorShift = cc.get3DShift();
				TickAxis yAxis = (TickAxis) cc.getYAxis();
				for (int t = 1; t <= tCount; t++) {
					Object tickVal = warnLineData(t);
					x = getTickPosition(tickVal);
					y = getBottomY() + coorThick;
					p = new Point2D.Double(x, y);

					drawWarnLine(t, p, x + coorShift, yAxis.getBottomY()
							- coorShift, x + coorShift, yAxis.getTopY()
							- coorShift);
				}
			}
			break;
		case Consts.AXIS_LOC_V:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isPolarCoor()) {
					continue;
				}
				CartesianCoor cc = (CartesianCoor) coor;
				if (cc.getYAxis() != this) {
					continue;
				}
				int coorShift = cc.get3DShift();
				TickAxis xAxis = (TickAxis) cc.getXAxis();
				for (int t = 1; t <= tCount; t++) {
					Object tickVal = warnLineData(t);
					x = getLeftX() - coorThick;
					y = getTickPosition(tickVal);
					p = new Point2D.Double(x, y);
					drawWarnLine(t, p, xAxis.getLeftX() + coorShift, y
							- coorShift, xAxis.getRightX() + coorShift, y
							- coorShift);
				}
			}
			break;
		case Consts.AXIS_LOC_POLAR:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isCartesianCoor()) {
					continue;
				}
				PolarCoor pc = (PolarCoor) coor;
				if (pc.getPolarAxis() != this) {
					continue;
				}
				TickAxis angleAxis = (TickAxis) pc.getAngleAxis();
				Point2D orginalPoint = new Point2D.Double(getLeftX(),
						getBottomY()); // ԭ��
				for (int t = 1; t <= tCount; t++) {
					Object tickVal = warnLineData(t);
					ArrayList<Point2D> points = new ArrayList<Point2D>();
					x = getTickPosition(tickVal);
					y = getBottomY() + coorThick;
					p = new Point2D.Double(x, y);

					if (isPolygonalRegion) {
						double polarLen = getTickPosition(tickVal) - getLeftX();
						for (int n = 1; n <= angleAxis.t_coorValue.length(); n++) {
							Object angleTick = angleAxis.t_coorValue.get(n);
							double angle = angleAxis.getTickPosition(angleTick);
							Point2D polarPoint = new Point2D.Double(polarLen,
									angle);
							points.add(pc.getScreenPoint(polarPoint));
						}
						warnShape = Utils.getPath2D(points, isCircleAngle());
					} else { // ����
						double w, h, tmpLen;
						tmpLen = x - orginalPoint.getX();
						x = orginalPoint.getX() - tmpLen;
						y = orginalPoint.getY() - tmpLen;
						w = tmpLen * 2;
						h = w;

						warnShape = new Arc2D.Double(x, y, w, h,
								angleAxis.startAngle, angleAxis.endAngle
										- angleAxis.startAngle,
								java.awt.geom.Arc2D.OPEN);
					}
					drawWarnShape(t, p, warnShape, null);
				}
			}
			break;
		case Consts.AXIS_LOC_ANGLE:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isCartesianCoor()) {
					continue;
				}
				PolarCoor pc = (PolarCoor) coor;
				if (pc.getAngleAxis() != this) {
					continue;
				}
				TickAxis polarAxis = (TickAxis) pc.getPolarAxis();
				Point2D orginalPoint = new Point2D.Double(polarAxis.getLeftX(),
						polarAxis.getBottomY()); // ԭ��
				double polarLen = polarAxis.getAxisLength();
				for (int t = 1; t <= tCount; t++) {
					Object tickVal = warnLineData(t);
					double angle = getValueLen(tickVal);

					if (isPolygonalRegion) {
					} else { // ����
					}
					p = new Point2D.Double(polarLen, angle);
					warnShape = new Line2D.Double(orginalPoint,
							pc.getScreenPoint(p));
					drawWarnShape(t, p, warnShape, pc.getScreenPoint(p));
				}
			}
			break;
		}

	}

	/**
	 * �Ƿ�ö����
	 * @return false
	 */
	public boolean isEnumAxis() {
		return false;
	}

	/**
	 * �Ƿ�������
	 * @return false
	 */
	public boolean isDateAxis() {
		return false;
	}

	/**
	 * �Ƿ���ֵ��
	 * @return true
	 */
	public boolean isNumericAxis() {
		return true;
	}

	// ���л���ͼ����ص��м�����������transient�޶���ָ�������ұ�������t_��Ϊǰ׺
	private transient double t_maxValue=Double.MIN_VALUE, t_minValue=Double.MAX_VALUE;

	// ��ֵ,ͨ��Ϊ0������û�ָ������Сֵ�����ֵΪ��Сֵ
	private transient double t_baseValue = 0;
	private transient int t_baseValueLine = 0;
	// ��λ˵��
	private transient String t_unitText = "";

	public static void main(String[] args) {
		double d = 1000;
		double tmp = NumericAxis.log(10, d);
		System.out.println("1:" + tmp);
		tmp = NumericAxis.power(10, tmp);
		System.out.println("2:" + tmp);
	}

	public void checkDataMatch(Sequence data){
		if(data!=null && data.length()>1){
			Object one = data.get(1);
			getNumber(one);
		}
	}


	public double animateDoubleValue(Object val){
		return getNumber( val );
	}
	
}
