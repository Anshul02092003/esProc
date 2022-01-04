package com.scudata.chart.element;

import java.awt.*;
import java.awt.geom.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.*;
import com.scudata.dm.*;

/**
 * ��ͼԪ
 * ��ͼԪ����չ��Ϊ������״������ֱ��ͼ������ֱ��ͼ��Բ��ͼ
 * ���ö����������ʱ�����Ϊϵ������ͼ
 * �ر�أ���ͼԪ�ڼ�����ϵ�±���Ϊһ�������Ļ�
 * @author Joancy
 *
 */
public class Column extends Ring {
	// ���Ӹ߶������һ���߼����꣬��ʽ[w1,w2,...,wn]��w
	public Sequence data3 = null;
	// ���ӿ��,�����ö�����ϣ�<=1��ʾ��ϵ��ռ��ı�����>1��ʾ�������ؿ�ȡ���������ڻ�����ֵ��ʱ��<=1��ʾ���᳤�ı�����>1��ʾ�������ؿ��
	public Para columnWidth = new Para(new Double(0.9));

	// ��������
	public Para columnShape = new Para(new Integer(Consts.COL_COBOID));

	// ��ʾ���ֺ������
	public Para horizontalAlign = new Para(new Integer(Consts.HALIGN_CENTER));

	// ��ʾ�����������
	public Para verticalAlign = new Para(new Integer(Consts.VALIGN_TOP));

	// �Ƿ�����Ӱ
	public boolean shadow = true;

	// ͻ���߿�
	public boolean convexEdge = false;

	/**
	 * ȱʡ�����Ĺ��캯��
	 */
	public Column() {
	}

	/**
	 * ͼԪ����ǰ׼�����������������
	 * ���ݲ���������ƥ��ʱ�ӳ��쳣
	 */
	public void prepare() {
		super.prepare();
		if (data3 != null && data1.length() != data3.length()) {
			throw new RuntimeException(
					"Column property 'data3' is not match to 'data1': data1 length="
							+ data1.length() + " data3 length="
							+ data3.length());
		}
	}

	/**
	 * ��ȡ��index�����ӵĿ��
	 * ö��ֵ���������ϵ�п�ȣ�
	 * ���ڻ�����ֵ��ֱ�Ӱ������ط���
	 * @param ia �̶���
	 * @param index �������
	 * @return ���ذ����ص����ӿ��
	 */
	public double getColumnWidth(TickAxis ia, int index) {
		double colWidth = columnWidth.doubleValue(index);
		if (ia instanceof EnumAxis) {
			colWidth = ia.getValueRadius(colWidth);
		}
//		�������Ϊ0��0���أ�������С��1���أ�Ҫ��ͼ�λ��Ʋ�����
		if(colWidth==0){
			return 0;
		}else if(colWidth<1){
			colWidth=1;
		}
		return colWidth;
	}

	/**
	 * ���ӿ��Ի��ƴ�����3����ֵ���ݵ�һ��������
	 * ����������ʱʹ��data3
	 * @return �߼�����3������
	 */
	public Sequence getData3() {
		return data3;
	}

	protected Shape drawFreeColumn(int index, Point2D p1, Point2D p2, int step,
			boolean isVertical, int seriesIndex) {
		// ֻ�е�һ������ʱ�Ż᷵���������ӵ���״
		Shape linkShape = null;

		double leftX = Math.min(p1.getX(), p2.getX());
		double rightX = Math.max(p1.getX(), p2.getX());
		double topY = Math.min(p1.getY(), p2.getY());
		double bottomY = Math.max(p1.getY(), p2.getY());

		int x = (int) leftX;
		int y = (int) topY;
		int w = (int) (rightX) - x;
		int h = (int) (bottomY) - y;

		// Graphics2D g = e.getGraphics();
		switch (step) {
		case 1: // ������
			int style = columnShape.intValue();
			switch (style) {
			case Consts.COL_CUBE:
				if (isPhysicalCoor()) {
					throw new RuntimeException(
							"Physical coordinates do not support 3D column.");
				}
				linkShape = draw3DColumn(x, y, w, h, seriesIndex, isVertical);
				break;
			case Consts.COL_CYLINDER:
				if (isPhysicalCoor()) {
					throw new RuntimeException(
							"Physical coordinates do not support cylinder column.");
				}
				linkShape = drawCylinder(x, y, w, h, seriesIndex, isVertical);
				break;
			default:
				linkShape = draw2DColumn(x, y, w, h, seriesIndex, isVertical);
			}
			break;
		case 2: // ����������
			String txt = text.stringValue(index);
			if (!StringUtils.isValidString(txt)) {
				return null;
			}

			String fontName = textFont.stringValue(index);
			int fontStyle = textStyle.intValue(index);
			int fontSize = textSize.intValue(index);
			Font font = Utils.getFont(fontName, fontStyle, fontSize);
			Color tc = textColor.colorValue(index);
			int hAlign = horizontalAlign.intValue(index);
			int vAlign = verticalAlign.intValue(index);
			switch (hAlign) {
			case Consts.HALIGN_LEFT:
				break;
			case Consts.HALIGN_CENTER:
				x = x + w / 2;
				break;
			case Consts.HALIGN_RIGHT:
				x = x + w;
				break;
			}
			switch (vAlign) {
			case Consts.VALIGN_TOP:
				break;
			case Consts.VALIGN_MIDDLE:
				y = y + h / 2;
				break;
			case Consts.VALIGN_BOTTOM:
				y = y + h;
				break;
			}
			int location = hAlign + vAlign;
			int coorShift = 0;
			if( !isPhysicalCoor() ){
				style = columnShape.intValue();
				switch (style) {
				case Consts.COL_CYLINDER:
					ICoor coor = getCoor();
					coorShift = ((CartesianCoor) coor).get3DShift() / 2;
					break;
				default:
				}
			}
			
			Utils.drawText(e, txt, x + coorShift, y - coorShift, font, tc,
					fontStyle, 0, location, textOverlapping);
			break;
		}
		return linkShape;
	}

	private Shape draw2DColumn(int x, int y, int w, int h, int index,
			boolean isVertical) {
		Graphics2D g = e.getGraphics();
		ChartColor cc = fillColor.chartColorValue(index);
		Color bc = borderColor.colorValue(index);
		int bs = borderStyle.intValue(index);
		float bw = borderWeight.floatValue(index);

		Utils.draw2DRect(g, x, y, w, h, bc, bs, bw, shadow, convexEdge,
				transparent, cc, isVertical);
		return new java.awt.Rectangle(x, y, w, h);
	}

	private Shape draw3DColumn(int x, int y, int w, int h, int index,
			boolean isVertical) {
		Graphics2D g = e.getGraphics();
		CartesianCoor cc = (CartesianCoor) getCoor();
		int coorShift = cc.get3DShift();
		Color c = borderColor.colorValue(index);
		int style = borderStyle.intValue(index);
		float weight = borderWeight.floatValue(index);
		ChartColor chartColor = fillColor.chartColorValue(index);

		Utils.draw3DRect(g, x, y, w, h, c, style, weight, shadow, convexEdge,
				transparent, chartColor, isVertical, coorShift);

		int[] shapeX = new int[] { x, x + coorShift, x + coorShift + w,
				x + coorShift + w, x + w, x };
		int[] shapeY = new int[] { y, y - coorShift, y - coorShift,
				y - coorShift + h, y + h, y + h };

		return new java.awt.Polygon(shapeX, shapeY, shapeX.length);
	}

	private Shape drawCylinder(int x, int y, int width, int height, int index,
			boolean isVertical) {
		Graphics2D g = e.getGraphics();
		CartesianCoor cc = (CartesianCoor) getCoor();
		int coorShift = cc.get3DShift();
		double halfShift = coorShift / 2;
		ChartColor chartColor = fillColor.chartColorValue(index);

		double ovalRate = 0.5;
		double xOval;
		double yOval;

		if (isVertical) {
			xOval = x + halfShift;
			yOval = y - halfShift + height - width * ovalRate / 2;
		} else {
			xOval = x + halfShift - height * ovalRate / 2;
			yOval = y - halfShift;
		}
		Color bc = borderColor.colorValue(index);
		int bs = borderStyle.intValue(index);
		float bw = borderWeight.floatValue(index);

		Arc2D.Double bottomOval;
		if (isVertical) {
			bottomOval = new Arc2D.Double(xOval, yOval, width,
					width * ovalRate, 0, 360, Arc2D.OPEN);
		} else {
			bottomOval = new Arc2D.Double(xOval, yOval, height * ovalRate,
					height, 0, 360, Arc2D.OPEN);
		}
		if (transparent < 1) {
			// g.setColor(chartColor.getColor1()); // ����Բ
			Utils.fill(g, bottomOval, transparent, chartColor.getColor1());
		}
		if (Utils.setStroke(g, bc, bs, bw)) {
			g.draw(bottomOval);
		}

		Arc2D.Double topOval;
		if (isVertical) {
			topOval = new Arc2D.Double(xOval, yOval - height, width, width
					* ovalRate, 0, 360, Arc2D.OPEN);
		} else {
			topOval = new Arc2D.Double(xOval + width, yOval, height * ovalRate,
					height, 0, 360, Arc2D.OPEN);
		}
		Utils.drawCylinderTop(g, topOval, bc, bs, bw, transparent, chartColor,
				isVertical);

		double xRect, yRect;
		java.awt.geom.Area sc, sc1, sc2;
		if (isVertical) {
			xRect = x + halfShift;
			yRect = y - halfShift;
			sc = new java.awt.geom.Area(new Rectangle2D.Double(xRect, yRect,
					width, height + coorShift - 2));
			sc1 = new java.awt.geom.Area(new Rectangle2D.Double(xRect, yRect,
					width, height + coorShift - 2));
			sc2 = new java.awt.geom.Area(new Rectangle2D.Double(xRect, yRect,
					width, height));
		} else {
			xRect = x - 2;
			yRect = y - halfShift;
			sc = new java.awt.geom.Area(new Rectangle2D.Double(xRect, yRect,
					width + halfShift, height));
			sc1 = new java.awt.geom.Area(new Rectangle2D.Double(xRect, yRect,
					width + halfShift, height));
			sc2 = new java.awt.geom.Area(new Rectangle2D.Double(xRect
					+ halfShift, yRect, width, height));
		}
		java.awt.geom.Area or1 = new java.awt.geom.Area(bottomOval);
		java.awt.geom.Area or2 = new java.awt.geom.Area(topOval);
		sc2.subtract(or1);
		sc1.subtract(sc2);
		sc1.subtract(or1);
		sc.subtract(sc1);
		sc.subtract(or2);

		Utils.drawCylinderFront(g, sc, bc, bs, bw, transparent, chartColor,
				isVertical);

		java.awt.geom.Area outLine = new java.awt.geom.Area(sc);
		outLine.add(or2);
		return outLine;
	}

	/**
	 * ��ȡ��ͼԪ�ı༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();

		ParamInfo.setCurrent(Column.class, this);

		paramInfos.add(new ParamInfo("shadow", Consts.INPUT_CHECKBOX));
		paramInfos.add(new ParamInfo("convexEdge", Consts.INPUT_CHECKBOX));
		paramInfos.add(new ParamInfo("data3", Consts.INPUT_NORMAL));

		String group = "appearance";
		paramInfos
				.add(group, new ParamInfo("columnWidth", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("columnShape",
				Consts.INPUT_COLUMNSTYLE));

		group = "text";
		paramInfos.add(group, new ParamInfo("horizontalAlign",
				Consts.INPUT_HALIGN));
		paramInfos.add(group, new ParamInfo("verticalAlign",
				Consts.INPUT_VALIGN));

		paramInfos.addAll(super.getParamInfoList());// ֮���ԴӸ��������أ��Ǳ�����������Կ�ǰ��˳��
		return paramInfos;
	}

	/**
	 * ��¡��ͼԪ������ֵ
	 * @param c
	 */
	public void clone(Column c){
		super.clone(c);
	}
	
	/**
	 * ��ȿ�¡��ͼԪ
	 * @return ��¡��ͼԪ
	 */
	public Object deepClone() {
		Column c = new Column();
		clone(c);
		return c;
	}
}
