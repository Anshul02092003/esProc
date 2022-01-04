package com.scudata.cellset.graph.draw;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import com.scudata.cellset.graph.*;
import com.scudata.cellset.graph.config.IGraphProperty;
import com.scudata.chart.Consts;
import com.scudata.chart.Utils;
import com.scudata.common.*;
/**
 * �ѻ���ͼʵ��
 * @author Joancy
 *
 */

public class DrawColStacked extends DrawBase {
	/**
	 * ʵ�ֻ�ͼ����
	 */
	public void draw(StringBuffer htmlLink) {
		drawing(this, htmlLink);
	}

	/**
	 * ���ݻ�ͼ����db��ͼ��������ͼ��ĳ����Ӵ���htmlLink
	 * @param db ����Ļ�ͼ����
	 * @param htmlLink �����ӻ���
	 */
	public static int drawing(DrawBase db, StringBuffer htmlLink) {
		return drawing(db,htmlLink,false);
	}
	
	/**
	 * ���ݻ�ͼ����db��ͼ��������ͼ��ĳ����Ӵ���htmlLink
	 * @param db ����Ļ�ͼ����
	 * @param htmlLink �����ӻ���
	 */
	public static int drawing(DrawBase db, StringBuffer htmlLink, boolean is2Y) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;
		double seriesWidth;
		double coorWidth;
		double categorySpan;
		double dely;
		double x, y;

		gp.maxValue = gp.maxPositive;
		gp.minValue = gp.minNegative;
		gp.coorWidth = 0;

		db.initGraphInset();
		db.createCoorValue();
		db.drawLegend(htmlLink);
		db.drawTitle();
		db.drawLabel();
		db.keepGraphSpace();
		db.adjustCoorInset();
		gp.graphRect = new Rectangle2D.Double(gp.leftInset, gp.topInset, gp.graphWidth
				- gp.leftInset - gp.rightInset, gp.graphHeight - gp.topInset
				- gp.bottomInset);
		if (gp.graphRect.width < 10 || gp.graphRect.height < 10) {
			return -1;
		}

		if (gp.coorWidth < 0 || gp.coorWidth > 10000) {
			gp.coorWidth = 0;
		}
		ArrayList tmpCat = null;
		if ( is2Y ) {
//			���ڶѻ�ͼ������ϵ�У���˫��ͼis2Y������ͬʱʹ����category2�������г�ͻ
//			���is2Y���ƶѻ�ͼʱ����Ҫ����category2���öѻ�������������ϵ��
//			�ѻ�ͼ������ٻ�˫������� 
			tmpCat = egp.category2;
			egp.category2 = null;
		}

		int serNum = 1;
		if (egp.category2 != null) {
			serNum = 2;
		}
		
		if (gp.barDistance > 0) {
			double maxCatSpan = (gp.graphRect.width - serNum * gp.catNum
					* 1.0f)
					/ (gp.catNum + 1.0f);
			if (gp.barDistance <= maxCatSpan) {
				categorySpan = gp.barDistance;
			} else {
				categorySpan = maxCatSpan;
			}
			seriesWidth = (gp.graphRect.width - (gp.catNum + 1) * categorySpan)
					/ (serNum * gp.catNum);
		} else {
			seriesWidth = (gp.graphRect.width / (((gp.catNum + 1)
					* gp.categorySpan / 100.0)
					+ gp.coorWidth / 200.0 + gp.catNum * serNum));

			categorySpan = (seriesWidth * (gp.categorySpan / 100.0));
		}
		
		coorWidth = (seriesWidth * (gp.coorWidth / 200.0));
		dely = (gp.graphRect.height - coorWidth) / gp.tickNum;
		gp.gRect1 = (Rectangle2D.Double)gp.graphRect.clone();
		gp.gRect2 = (Rectangle2D.Double)gp.graphRect.clone();
		/* �������� */
		db.drawGraphRect();
		/* ��Y�� */
		for (int i = 0; i <= gp.tickNum; i++) {
			db.drawGridLine(dely, i);

			Number coory = (Number) gp.coorValue.get(i);
			String scoory = db.getFormattedValue(coory.doubleValue());

			x = gp.gRect1.x - gp.tickLen;
			y = gp.gRect1.y + gp.gRect1.height - i * dely;
			gp.GFV_YLABEL.outText(x, y, scoory);
			// ���û���
			if (coory.doubleValue() == gp.baseValue + gp.minValue) {
				gp.valueBaseLine =  (gp.gRect1.y + gp.gRect1.height - i
						* dely);
			}
		}

		// ��������
		db.drawWarnLine();

		/* ��X�� */
		ArrayList cats = egp.categories;
		int cc = cats.size();
		Color c;
		for (int i = 0; i < cc; i++) {
			ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
			double delx = (i + 1) * categorySpan + i * seriesWidth
					* serNum + seriesWidth * serNum / 2.0;
			
			boolean valvis = (i % (gp.graphXInterval + 1) == 0);//�����Ƿ���ʾֵ����Table�ֿ�
			boolean vis = valvis && !gp.isDrawTable;
			if (vis) {
				c = egp.getAxisColor(GraphProperty.AXIS_BOTTOM);
				Utils.setStroke(g, c, Consts.LINE_SOLID, 1.0f);
				db.drawLine(gp.gRect1.x + delx, gp.gRect1.y + gp.gRect1.height,
						gp.gRect1.x + delx, gp.gRect1.y + gp.gRect1.height
								+ gp.tickLen, c);
				// ����������
				db.drawGridLineCategoryV(gp.gRect1.x + delx);
			}

			String value = egc.getNameString();
			x = gp.gRect1.x + delx;
			y = gp.gRect1.y + gp.gRect1.height + gp.tickLen;
			gp.GFV_XLABEL.outText(x, y, value, vis);
			double positiveBase = gp.valueBaseLine;
			double negativeBase = gp.valueBaseLine;

			double lb;
			if (egp.category2 == null) {
				if (gp.isDrawTable) {
					lb = db.getDataTableX(i) - seriesWidth / 2;
				}else{
					lb = gp.gRect1.x + (i + 1) * categorySpan + i
							* seriesWidth;
				}
				drawSeries(0, gp.serNames,egc,
						dely, db, lb, positiveBase,
						seriesWidth, htmlLink, negativeBase,
						coorWidth, valvis);
			}else{
				if (gp.isDrawTable) {
					 lb = db.getDataTableX(i)- seriesWidth;
				}else{
					lb = gp.gRect1.x + (i + 1) * categorySpan + (i
							* serNum + 0)
							* seriesWidth;
				}
				
				drawSeries(0, gp.serNames,egc,
						dely, db, lb, positiveBase,
						seriesWidth, htmlLink, negativeBase,
						coorWidth, valvis);
				if (gp.isDrawTable) {
					 lb = db.getDataTableX(i);
				}else{
					lb = gp.gRect1.x + (i + 1) * categorySpan + (i
							* serNum + 1)
							* seriesWidth;
				}
				egc = (ExtGraphCategory) egp.category2.get(i);
				drawSeries(gp.serNames.size(), gp.serNames2,egc,
						dely, db, lb, positiveBase,
						seriesWidth, htmlLink, negativeBase,
						coorWidth, valvis);
			}


		}

		db.outLabels();
		/* �ػ�һ�»��� */
		db.drawLine(gp.gRect1.x, gp.valueBaseLine, gp.gRect1.x
				+ gp.gRect1.width, gp.valueBaseLine,
				egp.getAxisColor(GraphProperty.AXIS_BOTTOM));
		db.drawLine(gp.gRect1.x + gp.gRect1.width, gp.valueBaseLine,
				gp.gRect1.x + gp.gRect1.width + coorWidth,
				gp.valueBaseLine - coorWidth,
				egp.getAxisColor(GraphProperty.AXIS_BOTTOM));

		if ( is2Y ) {
			egp.category2 = tmpCat;
		}
		
		return gp.serNum;
	}

	private static void drawSeries(int serNumBase, Vector serNames,ExtGraphCategory egc,
			double dely, DrawBase db, double dlb, double positiveBase,
			double seriesWidth, StringBuffer htmlLink, double negativeBase,
			double coorWidth, boolean vis) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;
		ArrayList<ValueLabel> labelList = db.labelList;
		
		double lb = Math.round(dlb);
		int bs = Consts.LINE_SOLID;
		float bw = 1.0f;
		int serNum = serNames.size();
		for (int j = 0; j < serNum; j++) {
			ExtGraphSery egs = egc.getExtGraphSery(serNames.get(j));
			if (egs.isNull()) {
				continue;
			}
			double val = egs.getValue();
			double tmp = val;
			double len = Math.round(dely * gp.tickNum * (tmp - gp.minValue)
					/ (gp.maxValue * gp.coorScale));

			if (len == 0) {
				continue;
			}

			Color bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);
			Color tmpc = db.getColor(j+serNumBase);
			if (len > 0) {
				Utils.draw2DRect(g, lb, positiveBase - len,
						seriesWidth, len, bc, bs, bw,
						egp.isDrawShade(), egp.isRaisedBorder(),
						db.getTransparent(), db.getChartColor(tmpc), true);

				db.htmlLink(lb, positiveBase - len, seriesWidth, len,
						htmlLink, egc.getNameString(), egs);
			} else {
				Utils.draw2DRect(g, lb, negativeBase, seriesWidth,
						Math.abs(len), bc, bs, bw, egp.isDrawShade(),
						egp.isRaisedBorder(), db.getTransparent(),
						db.getChartColor(tmpc), true);
				db.htmlLink(lb, negativeBase, seriesWidth,
						Math.abs(len), htmlLink, egc.getNameString(), egs);
			}

			if (len > 0) {
				double ptx1[] = { lb, lb + coorWidth,
						lb + coorWidth + seriesWidth,
						lb + seriesWidth};
				double pty1[] = { positiveBase - len,
						positiveBase - len - coorWidth,
						positiveBase - len - coorWidth,
						positiveBase - len};
				g.setColor(db.getColor(j+serNumBase));
				Shape poly = Utils.newPolygon2D(ptx1, pty1);
				g.fill( poly );
				db.drawPolygon(ptx1, pty1, 4,
						egp.getAxisColor(GraphProperty.AXIS_COLBORDER));
			}

			double temp = 0;
			if (len > 0) {
				temp = positiveBase;
			} else {
				temp = negativeBase;
			}
			double ptx2[] = { lb + seriesWidth, lb + seriesWidth,
					lb + seriesWidth + coorWidth,
					lb + seriesWidth + coorWidth };
			double pty2[] = { temp, temp - len, temp - len - coorWidth,
					temp - coorWidth };
			Color clr = db.getColor(j+serNumBase);
			clr = clr.darker();
			g.setColor(clr);
			Shape poly = Utils.newPolygon2D(ptx2, pty2);
			g.fill( poly );
			db.drawPolygon(ptx2, pty2, 4,
					egp.getAxisColor(GraphProperty.AXIS_COLBORDER));

			String percentFmt = null;
			if (vis) {
				if (gp.dispValueType == IGraphProperty.DISPDATA_PERCENTAGE) { // �����ʾ�ٷֱ�
					if (StringUtils.isValidString(gp.dataMarkFormat)) {
						percentFmt = gp.dataMarkFormat;
					} else {
						percentFmt = "0.00%";
					}
				}
			}

			ValueLabel vl = null;

			double x = (lb + seriesWidth / 2);
			if (len > 0) {
				String sval = null;
				if (percentFmt != null) {
					sval = db.getFormattedValue(
							egs.getValue() / egc.getPositiveSumSeries(),
							percentFmt);
				}else{
					sval = db.getDispValue(egc,egs,gp.serNum);
				}

				if (StringUtils.isValidString(sval)) {
					vl = new ValueLabel(sval, new Point2D.Double(x, (positiveBase - len/ 2)), gp.GFV_VALUE.color,
							GraphFontView.TEXT_ON_CENTER);
				}
				positiveBase -= len;
			} else {
				String sval = null;
				if (percentFmt != null) {
					sval = db.getFormattedValue(
							egs.getValue() / egc.getNegativeSumSeries(),
							percentFmt);
				}else{
					sval = db.getDispValue(egc,egs,gp.serNum);
				}
				if (StringUtils.isValidString(sval)) {
					vl = new ValueLabel(sval, new Point2D.Double(x, (negativeBase - len
							/ 2)), gp.GFV_VALUE.color,
							GraphFontView.TEXT_ON_CENTER);
				}

				negativeBase -= len;
			}
			if (vl != null) {
				labelList.add(vl);
			}
		}
		
		// ��������ʾ��ֵ
		if (gp.dispStackSumValue && vis) {
			double val = db
					.getScaledValue(egc.getPositiveSumSeries(), true);
			String sval;
			ValueLabel vl = null;
			double x, y;
			if (val > 0) {
				sval = db.getFormattedValue(val);
				x = (lb + seriesWidth / 2);
				y = positiveBase - 3;
				vl = new ValueLabel(sval, new Point2D.Double(x, y),
						gp.GFV_VALUE.color, gp.GFV_VALUE.textPosition);
			}
			val = db.getScaledValue(egc.getNegativeSumSeries(), true);
			if (val < 0) {
				sval = db.getFormattedValue(val);
				x = (lb + seriesWidth / 2);
				y = negativeBase + 3; 
				vl = new ValueLabel(sval, new Point2D.Double(x, y),
						gp.GFV_VALUE.color, GraphFontView.TEXT_ON_BOTTOM);
			}

			if (vl != null) {
				labelList.add(vl);
			}
		}
		
	}
	
}
