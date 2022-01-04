package com.scudata.cellset.graph.draw;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import com.scudata.cellset.graph.*;
import com.scudata.chart.ChartColor;
import com.scudata.chart.Consts;
import com.scudata.chart.Utils;
/**
 * ��ά��ͼʵ��
 * @author Joancy
 *
 */

public class DrawDot3D extends DrawBase {
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
	public static void drawing(DrawBase db,StringBuffer htmlLink) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;
		ArrayList<ValueLabel> labelList = db.labelList;
		double seriesWidth;
		double seriesDeep;
		double coorWidth;
		double categorySpan;
		double seriesSpan;
		double dely = 0;
		double tmpInt;

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
			return;
		}

		gp.gRect1 = (Rectangle2D.Double)gp.graphRect.clone();
		gp.gRect2 = (Rectangle2D.Double)gp.graphRect.clone();

		coorWidth = (Math.min(gp.graphRect.width, gp.graphRect.height)) / 2;
		gp.gRect1.y += coorWidth;
		gp.gRect1.width -= coorWidth;
		gp.gRect1.height -= coorWidth;

		dely = gp.gRect1.height / gp.tickNum;
		gp.gRect1.y += (gp.gRect1.height - dely * gp.tickNum) / 2;
		gp.gRect1.height =  (dely * gp.tickNum);

		seriesDeep = (coorWidth / (((gp.serNum + 1) * gp.seriesSpan / 100.0) + gp.serNum));
		seriesSpan = (seriesDeep * (gp.seriesSpan / 100.0));

		tmpInt =  ((gp.serNum + 1) * seriesSpan + gp.serNum * seriesDeep);
		tmpInt =  ((coorWidth - tmpInt) / 2);
		gp.gRect1.y += tmpInt;

		if (gp.barDistance > 0) {
			double maxCatSpan = (gp.gRect1.width - gp.catNum * 1.0f)
					/ (gp.catNum + 1.0f);
			if (gp.barDistance <= maxCatSpan) {
				categorySpan = gp.barDistance;
			} else {
				categorySpan = maxCatSpan;
			}
			seriesWidth = (gp.gRect1.width - (gp.catNum + 1) * categorySpan)
					/ gp.catNum;
		} else {
			seriesWidth = (gp.gRect1.width / (((gp.catNum + 1)
					* gp.categorySpan / 100.0)
					+ gp.coorWidth / 200.0 + gp.catNum));

			categorySpan = (seriesWidth * (gp.categorySpan / 100.0));
		}
		gp.gRect2.x =  (gp.gRect1.x + coorWidth);
		gp.gRect2.width = gp.gRect1.width;
		gp.gRect2.y =  (gp.gRect1.y - coorWidth);
		gp.gRect2.height = gp.gRect1.height;

		/* �������� */
		db.drawGraphRect();
		Point2D.Double p;
		/* ��Y�� */
		for (int i = 0; i <= gp.tickNum; i++) {
			db.drawGridLine(dely, i);

			Number coory = (Number) gp.coorValue.get(i);
			String scoory = db.getFormattedValue(coory.doubleValue());

			p = db.getVTickPoint(i*dely);
			gp.GFV_YLABEL.outText(p.x-gp.tickLen, p.y, scoory);
			// ���û���
			if (coory.doubleValue() == gp.baseValue + gp.minValue) {
				gp.valueBaseLine =  (gp.gRect1.y + gp.gRect1.height - i
						* dely);
			}
		}
		// ��������
		db.drawWarnLine();
		if (gp.graphTransparent) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.60F));
		}
		ArrayList cats = egp.categories;
		int cc = cats.size();
		Color c;
		
//		��x��ǩ�Լ�������
		for (int i = 0; i < cc; i++) {
			ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
			double delx = (i + 1) * categorySpan + i * seriesWidth + seriesWidth / 2.0;

			g.setColor(gp.gridColor);
			float dashes[] = { 2 };
			g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 1, dashes, 0));
			Utils.drawLine(g,gp.gRect1.x + delx, gp.valueBaseLine,  (gp.gRect1.x
					+ delx + coorWidth),  (gp.valueBaseLine - coorWidth));

			boolean vis = (i % (gp.graphXInterval + 1) == 0);
			p = db.getHTickPoint(delx);
			if (vis) {
				c = egp.getAxisColor(GraphProperty.AXIS_BOTTOM);
				Utils.setStroke(g, c, Consts.LINE_SOLID, 1.0f);
				db.drawLine(p.x,p.y,p.x,p.y+gp.tickLen,c);
				// ����������
				db.drawGridLineCategoryV(gp.gRect2.x + delx);
			}
			String value = egc.getNameString();
			gp.GFV_XLABEL.outText(p.x, p.y+gp.tickLen, value, vis);
			
		}
		
		/* �������� */
		 drawDot(cc,cats,categorySpan,seriesWidth,
					g,gp, coorWidth, dely, seriesSpan,
					seriesDeep, db, labelList,false,htmlLink);		
		
//		������������������������ƽ�治͸������Ӧ��ס����������ֵ
		db.outLabels();
		// ���ƻ���͸��ƽ�棬
		if (gp.valueBaseLine != gp.gRect1.y + gp.gRect1.height) {
			double xx[] = { gp.gRect1.x,  (gp.gRect1.x + coorWidth),
					 (gp.gRect1.x + gp.gRect1.width + coorWidth),
					 (gp.gRect1.x + gp.gRect1.width) };
			double yy[] = { gp.valueBaseLine,
					 (gp.valueBaseLine - coorWidth),
					 (gp.valueBaseLine - coorWidth), gp.valueBaseLine };
			Shape poly = Utils.newPolygon2D(xx, yy);

			Color ccc = egp.getAxisColor(GraphProperty.AXIS_BOTTOM);
			if (ccc == null) {// ����ױ�Ϊ͸��ɫʱ��ʹ��ȱʡ��
				ccc = Color.lightGray;
			}
			float trans = 1.0f;
			if (gp.graphTransparent) {
				trans = 0.4f;
			}

			Utils.fill(g, poly, trans, ccc);
		}

		/* �������� */
		 drawDot(cc,cats,categorySpan,seriesWidth,
					g,gp, coorWidth, dely, seriesSpan,
					seriesDeep, db, labelList,true,htmlLink);		

		if (gp.graphTransparent) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					1.00F));
		}
		db.outLabels();
		/* ��һ�»��� */
		if (gp.valueBaseLine != gp.gRect1.y + gp.gRect1.height) {
			db.drawLine(gp.gRect1.x, gp.valueBaseLine, gp.gRect1.x
					+ gp.gRect1.width, gp.valueBaseLine,
					egp.getAxisColor(GraphProperty.AXIS_BOTTOM));
			db.drawLine(gp.gRect1.x + gp.gRect1.width, gp.valueBaseLine,
					 (gp.gRect1.x + gp.gRect1.width + coorWidth),
					 (gp.valueBaseLine - coorWidth),
					egp.getAxisColor(GraphProperty.AXIS_BOTTOM));
		}
	}
	
	private static void drawDot(int cc,ArrayList cats,double categorySpan,double seriesWidth,
			Graphics2D g,GraphParam gp,double coorWidth,double dely,double seriesSpan,
			double seriesDeep,DrawBase db,ArrayList<ValueLabel> labelList,boolean drawPositive,StringBuffer htmlLink){
		/* ������������ */
		for (int i = 0; i < cc; i++) {
			ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
			boolean vis = (i % (gp.graphXInterval + 1) == 0);

			for (int j = gp.serNum - 1; j >= 0; j--) {
				ExtGraphSery egs = egc.getExtGraphSery(gp.serNames.get(j));
				if (egs.isNull()) {
					continue;
				}
				double val = egs.getValue();
				double tmp = val - gp.baseValue;
				double len =  Math.round(dely * gp.tickNum
						* (tmp - gp.minValue) / (gp.maxValue * gp.coorScale));
				if (drawPositive) {
					 if(len < 0) continue;
				}else{
					 if(len > 0) continue;
				}

				double lb =  Math.round(gp.gRect1.x + (i + 1) * categorySpan
						+ i * seriesWidth);
				double br =  Math
						.round((j + 1) * seriesSpan + j * seriesDeep);

				int cIndex;
				if (!gp.isMultiSeries) {
					cIndex = i;
				} else {
					cIndex = j;
				}

				double xx,yy;
				xx = lb + br + seriesWidth/2;
				yy = gp.valueBaseLine - len - br;
				
				Point2D.Double p2 = new Point2D.Double(xx,yy);
				Color foreColor = db.getColor(cIndex);
				ChartColor backColor = new ChartColor(foreColor);
				Shape s = Utils.drawCartesianPoint2(g, p2, Consts.PT_CIRCLE, 0, 0, 0,
						Consts.LINE_SOLID, db.gp.getLineThick(), backColor, foreColor, 1);//gp.getLineThick()
				if(s!=null){
					Rectangle rect = s.getBounds();
					db.htmlLink(rect.x, rect.y,rect.width, rect.height, htmlLink,
							egc.getNameString(), egs);
				}

				
				// ���������֣�����ᱻͼ�θ���
				if (gp.dispValueOntop && !egs.isNull() && vis) {
					String sval = db.getDispValue(egc,egs,gp.serNum);
					double x = lb + br +  seriesWidth / 2;// - TR.width / 2;
					double y = gp.valueBaseLine - len - br;
					Color c;
					if (!gp.isMultiSeries) {
						c = db.getColor(i);
					} else {
						c = db.getColor(j);
					}
					ValueLabel vl;
					if (len < 0) {
						vl = new ValueLabel(sval, new Point2D.Double(x, y), c,
								GraphFontView.TEXT_ON_BOTTOM);
					} else {
						vl = new ValueLabel(sval, new Point2D.Double(x, y), c,
								GraphFontView.TEXT_ON_TOP);
					}
					labelList.add(vl);
				}
			}
		}
	}
	
}
