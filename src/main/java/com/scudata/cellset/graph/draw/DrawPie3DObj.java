package com.scudata.cellset.graph.draw;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.scudata.cellset.graph.*;
import com.scudata.chart.ChartColor;
import com.scudata.chart.Consts;
import com.scudata.chart.CubeColor;
import com.scudata.chart.Utils;
import com.scudata.common.MessageManager;
import com.scudata.common.StringUtils;
import com.scudata.resources.EngineMessage;

/**
 * ��ά��ͼʵ��
 * @author Joancy
 *
 */
public class DrawPie3DObj extends DrawBase {
	private static Color bc;
	private static int cutColorLevel = -2;
	
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
		double radiusx = 0;
		double radiusy = 0;
		double dely;

		double tmpInt1, tmpInt2;
		gp.coorWidth = 0;
		bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);

		db.initGraphInset();
		if (gp.minValue < 0) {
			g.setColor(Color.black);
			g.setFont(gp.GFV_TITLE.font);
		   MessageManager mm = EngineMessage.get();
			g.drawString(mm.getMessage("drawpie.negativedata",gp.minValue) ,
						50, 50);
			return;
		}

		if (gp.pieHeight > 100) {
			gp.pieHeight = 100;
		}
		if (gp.pieHeight < 0) {
			gp.pieHeight = 0;

		}
		db.drawLegend(htmlLink);
		db.drawTitle();
		db.drawLabel();
		gp.gRect2 = new Rectangle2D.Double(gp.leftInset, gp.topInset, gp.graphWidth
				- gp.leftInset - gp.rightInset, gp.graphHeight - gp.topInset
				- gp.bottomInset);
		gp.gRect1 = (Rectangle2D.Double)gp.gRect2.clone();

		db.keepGraphSpace();

		gp.graphRect = new Rectangle2D.Double(gp.leftInset, 
				gp.topInset, // + 20,
				gp.graphWidth - gp.leftInset - gp.rightInset,
				gp.graphHeight - gp.topInset - gp.bottomInset);
		if (gp.graphRect.width < 10 || gp.graphRect.height < 10) {
			return;
		}
		if (gp.serNum == 0) {
			radiusx = gp.graphRect.width / 2;
		} else {
			radiusx = gp.graphRect.width / (2 * gp.serNum);
		}
		radiusy = gp.graphRect.height
				/ (2 * gp.serNum + (gp.pieHeight / 100.0));
		if (gp.pieRotation < 10) {
			gp.pieRotation = 10;
		}
		if (gp.pieRotation > 100) {
			gp.pieRotation = 100;
		}
		if (radiusx * (gp.pieRotation / 100.0) > radiusy) {
			radiusx =  (radiusy / (gp.pieRotation / 100.0));
		} else {
			radiusy =  (radiusx * gp.pieRotation / 100.0);
		}
		dely =  (radiusy * (gp.pieHeight / 100.0));

		tmpInt1 = radiusx * (2 * gp.serNum);
		tmpInt2 = radiusy * (2 * gp.serNum) + radiusy * (gp.pieHeight / 100.0);

		gp.graphRect = new Rectangle2D.Double(gp.graphRect.x + (gp.graphRect.width - tmpInt1) / 2, gp.graphRect.y
				+ (gp.graphRect.height - tmpInt2) / 2, tmpInt1, tmpInt2);

		double orgx = gp.graphRect.x + gp.graphRect.width / 2;
		double orgy = gp.graphRect.y + dely + (gp.graphRect.height - dely) / 2;
		boolean cut = gp.serNum == 1 && gp.catNum > 1 && egp.isCutPie();
		/* ��ʼѭ������ */
		if (gp.graphTransparent) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.60F));
		}
		Shape tmpShape;
		Color tmpc;
		for (int z = 0; z < gp.serNum; z++) {
			String serName = (String) gp.serNames.get(z);
			double totAmount = 0.0;
			double totAngle = 0;
			/* �����ǰ���еİ뾶 */
			double radx = (gp.serNum - z) * radiusx;
			double rady = (gp.serNum - z) * radiusy;

			// ������ʱ��Ҫ�õ���һϵ�еİ뾶���Σ���ȷ����״��������
			double rxx = (gp.serNum - z - 1) * radiusx;
			double ryy = (gp.serNum - z - 1) * radiusy;
			double xx, yy;
			double ww = 2 * rxx;
			double hh = 2 * ryy;

			double max = 0;
			double maxi = -1, maxX = 0, maxY = 0; // ������������Ϣ������ۼƵ�����ı�
			double maxAngle = 0;
			ExtGraphCategory maxEgc = null;
			ExtGraphSery maxEgs = null;

			double movex = 0;
			double movey = 0;
			/* ������ܺ� */
			ArrayList cats = egp.categories;
			int cc = cats.size();
			for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				ExtGraphSery egs = egc.getExtGraphSery(serName);
				double amount = egs.getValue();
				if (amount > max) {
					max = amount;
					maxi = i;
				}

				totAmount += amount;
			}

			if (totAmount == 0.0) {
				continue;
			}
			/* �Ȼ��±ߵ�Բ */
			totAngle = 0;
			for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				ExtGraphSery egs = egc.getExtGraphSery(serName);
				double amount = egs.getValue();
				double angle = 0;
				angle = 360.0 * amount / totAmount;
				if (angle == 0) {
					continue;
				}
				if (i == gp.catNum - 1) {
					angle = 360 - totAngle;
				}
				if (cut && maxi == i) {
					movex = getDxOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
					movey = getDyOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
				} else {
					movex = 0;
					movey = 0;
				}
				g.setColor(db.getColor(i));
				tmpShape = new Arc2D.Double(orgx - radx + movex, orgy - rady
						+ movey, 2 * radx, 2 * rady, totAngle, angle, Arc2D.PIE);
				g.fill(tmpShape);
				db.drawShape(tmpShape, bc);
				// ���󵲰�
				if (totAngle < 180) {
					drawBackBaffle(db,totAngle, orgx, radx, movex, orgy, rady,
							movey, dely, angle, i);
				}
				totAngle += angle;
			}
			tmpc = egp.getAxisColor(GraphProperty.AXIS_PIEJOIN);
			db.g.setStroke(new BasicStroke(1.0f));
			db.drawShape(new Line2D.Double(orgx + radx, orgy - dely, orgx + radx,
					orgy), tmpc);
			// ���п�
			totAngle = 0;
			double cumulativeAmount = 0.0;
			double curveY = 0;
			if (egp.isRaisedBorder()) {
				curveY = dely / 5; // �αߵ�ƫ��
			}

			for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				ExtGraphSery egs = egc.getExtGraphSery(serName);
				double amount = egs.getValue();
				double angle = 0;
				angle =  (360.0 * amount / totAmount);
				if (i == gp.catNum - 1) {
					angle = 360 - totAngle;
				}
				if (angle == 0 && totAngle >= 90) {
					continue;
				}
				if (cut && maxi == i) {
					movex = getDxOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
					movey = getDyOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
				} else {
					movex = 0;
					movey = 0;
				}
				totAngle += angle;

				double bx1 = 0, ex1 = 0, by1 = 0, ey1 = 0;
				Arc2D.Double ddd = new Arc2D.Double(orgx - radx + movex, orgy
						- rady + movey, 2 * radx, 2 * rady, (totAngle - angle),
						angle, Arc2D.PIE);
				bx1 = ddd.getStartPoint().getX();
				ex1 = ddd.getEndPoint().getX();
				by1 = ddd.getStartPoint().getY();
				ey1 = ddd.getEndPoint().getY();

				double bx2 = 0, ex2 = 0, by2 = 0, ey2 = 0;
				if (egp.isRaisedBorder()) {
					ddd = new Arc2D.Double(orgx - radx + movex + curveY / 2,
							orgy - rady - dely + movey, 2 * radx - curveY, 2
									* rady - curveY, (totAngle - angle), angle,
							Arc2D.PIE);
					bx2 = ddd.getStartPoint().getX();
					ex2 = ddd.getEndPoint().getX();
					by2 = ddd.getStartPoint().getY();
					ey2 = ddd.getEndPoint().getY();
				}

				if (cut && maxi == i) { // �������
					if (bx1 > orgx + movex && ex1 >= orgx + movex
							&& ey1 >= orgy + movey && by1 <= orgy + movey) {
						drawCut2(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut1(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
					} else if (bx1 > orgx + movex && ex1 >= orgx + movex) { // 4
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut2(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut1(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);

					} else if (bx1 < orgx + movex && ex1 <= orgx + movex) {
						drawCut1(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut2(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);

					} else if (ex1 > orgx + movex && bx1 < orgx + movex) {
						drawCut1(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut2(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
					} else if (ex1 < orgx + movex && bx1 > orgx + movex) {
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut2(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut1(db,bx1, by1, orgx, orgy, movex, movey, dely, i,
								bx2, by2, curveY);
					}
				} else if (gp.graphTransparent
						&& gp.catNum > 1
						&& (!cut || (i != maxi - 1 && !(i == gp.catNum - 1 && maxi == 0)))) { // ���������
					if (ex1 > orgx + movex) {
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
					} else {
						drawCut3(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
						drawCut4(db,ex1, ey1, orgx, orgy, movex, movey, dely, i,
								ex2, ey2, curveY);
					}
				}
			}

			// �����浲��
			totAngle = 0; // **************������

			Arc2D.Double topOval;
			Color bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);
			int bs = Consts.LINE_SOLID;
			float bw = 1.0f;
			for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				ExtGraphSery egs = egc.getExtGraphSery(serName);
				double amount = egs.getValue();
				double angle = 0;
				angle = 360.0 * amount / totAmount;
				if (i == gp.catNum - 1) {
					angle = 360 - totAngle;
				}
				if (cut && maxi == i) {
					movex = getDxOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
					movey = getDyOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
				} else {
					movex = 0;
					movey = 0;
				}

				// ��ǰ����
				if (totAngle + angle > 180) {
					drawFrontBaffle(db,totAngle, orgx, radx, movex, orgy, rady,
							movey, dely, angle, i, false);
				}
				Color c = db.getColor(i);
				ChartColor chartColor = db.getChartColor(c);
				if (egp.isRaisedBorder()) {
					if (totAngle + angle > 150 || totAngle == 0
							|| totAngle + angle < 45) {
						// ���αߵ�ǰ����
						drawFrontBaffle(db,totAngle, orgx, radx, movex, orgy
								- dely, rady, movey, curveY / 2, angle, i, true);
					}
					// �������԰
					topOval = new Arc2D.Double(
							orgx - radx + movex + curveY / 2, orgy - rady
									- dely + movey, 2 * radx - curveY, 2 * rady
									- curveY, totAngle, angle, Arc2D.PIE);

				} else {
					topOval = new Arc2D.Double(orgx - radx + movex, orgy - rady
							- dely + movey, 2 * radx, 2 * rady, totAngle,
							angle, Arc2D.PIE);
					// �������԰
				}

				Utils.drawCylinderTop(g, topOval, bc, bs, bw, db.getTransparent(),
						chartColor, true);

				Arc2D.Double ddd1;
				if (egp.isRaisedBorder()) {
					ddd1 = new Arc2D.Double(orgx - radx + movex + curveY / 2,
							orgy - rady - dely + movey, 2 * radx - curveY, 2
									* rady - curveY, totAngle, angle, Arc2D.PIE);
				} else {
					ddd1 = new Arc2D.Double(orgx - radx + movex, orgy - rady
							- dely + movey, 2 * radx, 2 * rady, totAngle,
							angle, Arc2D.PIE);
				}

				if (egp.isRaisedBorder()) {
					if (gp.catNum > 1) {
						tmpShape = new Line2D.Double(orgx + movex, orgy - dely
								+ movey - curveY / 2, ddd1.getStartPoint()
								.getX(), ddd1.getStartPoint().getY());
						db.drawShape(tmpShape, bc);
					}
					tmpShape = new Arc2D.Double(orgx - radx + movex + curveY
							/ 2, orgy - rady - dely + movey, 2 * radx - curveY,
							2 * rady - curveY, totAngle, angle, Arc2D.OPEN);
					db.drawShape(tmpShape, bc);// ���������⣬�߿���ɫ�Լ��Ƿ�ΪOPEN
				} else {
					if (gp.catNum > 1) {
						tmpShape = new Line2D.Double(orgx + movex, orgy - dely
								+ movey, ddd1.getStartPoint().getX(), ddd1
								.getStartPoint().getY());
						db.drawShape(tmpShape, bc);
					}
					tmpShape = new Arc2D.Double(orgx - radx + movex, orgy - rady
							- dely + movey, 2 * radx, 2 * rady, totAngle,
							angle, Arc2D.OPEN);
					db.drawShape(tmpShape, bc);
				}

				xx = orgx - rxx + movex;
				yy = orgy - ryy - dely + movey;
				Arc2D.Double ddd2 = null;
				if (ww != 0) {
					ddd2 = new Arc2D.Double(xx, yy, ww, hh, totAngle, angle,
							Arc2D.PIE);
				}
				db.htmlLink(ddd1, htmlLink, egc.getNameString(), egs, ddd2);

				totAngle += angle;
			}

			totAngle = 0; //����������
			for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				ExtGraphSery egs = egc.getExtGraphSery(serName);
				double amount = egs.getValue();
				double angle = 0;
				angle = 360.0 * amount / totAmount;
				if (i == gp.catNum - 1) {
					angle = 360 - totAngle;
				}
				if (cut && maxi == i) {
					movex = getDxOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
					movey = getDyOnAngle(radiusx, radiusy, dely * 0.67f,
							totAngle + angle / 2);
					// ��������ػ�һ�Σ��Է������˸�ס
					// ��ǰ����
					if (totAngle + angle > 180) {
						drawFrontBaffle(db,totAngle, orgx, radx, movex, orgy,
								rady, movey, dely, angle, i, false);
					}

					if (egp.isRaisedBorder()) {
						if (totAngle + angle > 150 || totAngle == 0
								|| totAngle + angle < 45) {
							// ���αߵ�ǰ����
							drawFrontBaffle(db,totAngle, orgx, radx, movex, orgy
									- dely, rady, movey, curveY / 2, angle, i,
									true);
						}
						// �������԰
						topOval = new Arc2D.Double(orgx - radx + movex + curveY
								/ 2, orgy - rady - dely + movey, 2 * radx
								- curveY, 2 * rady - curveY, totAngle, angle,
								Arc2D.PIE);
					} else {
						topOval = new Arc2D.Double(orgx - radx + movex, orgy
								- rady - dely + movey, 2 * radx, 2 * rady,
								totAngle, angle, Arc2D.PIE);
						// �������԰
					}

					Color c = db.getColor(i);
					ChartColor chartColor = db.getChartColor(c);
					Utils.drawCylinderTop(g, topOval, bc, bs, bw,
							db.getTransparent(), chartColor, true);
				} else {
					movex = 0;
					movey = 0;
				}

				if (gp.serNum == 1) { // ���ٷֱȱ�ʶ
					double dLen = radx;
					if(egp.isRaisedBorder()){
						dLen = radx-5;
					}

					double x1 = orgx
							+ getDxOnAngle(radx, rady, dLen, totAngle + angle
									/ 2) + movex;
					double y1 = orgy
							+ getDyOnAngle(radx, rady, dLen, totAngle + angle
									/ 2) - dely - curveY / 2 + movey;
					double depthR = radx * (gp.pieLine/100f);
					if ((totAngle + angle / 2) > 180) {
						depthR += Math
								.abs(dely
										* Math.sin(Math.toRadians(totAngle
												+ angle / 2)));
					}
					double x2 = orgx
							+ Math.round((radx + depthR)
									* Math.cos(Math.toRadians(totAngle + angle
											/ 2))) + movex;
					double y2 = orgy
							- Math.round((rady + curveY / 2 + depthR)
									* Math.sin(Math.toRadians(totAngle + angle
											/ 2))) + movey - dely - curveY / 2;

					g.setColor(gp.coorColor);

					String fmt;
					String text = "";
					double tmpAngle = (totAngle + angle / 2); // 360 -
					// ��ʾ��ֵ��ʾ
					switch (gp.dispValueType) {
					case GraphProperty.DISPDATA_NONE:
						text = "";
						break;
					case GraphProperty.DISPDATA_VALUE:
					case GraphProperty.DISPDATA_NAME_VALUE:
						if (StringUtils.isValidString(gp.dataMarkFormat)) {
							fmt = gp.dataMarkFormat;
						} else {
							fmt = "";
						}
						tmpc = egp.getAxisColor(GraphProperty.AXIS_PIEJOIN);
						tmpShape = new Line2D.Double(x1, y1, x2, y2);
						db.g.setStroke(new BasicStroke(1.0f));
						db.drawShape(tmpShape, tmpc);
						text = db.getFormattedValue(amount, fmt);
						if(gp.dispValueType==GraphProperty.DISPDATA_NAME_VALUE){
							text = getDispName(egc,egs,gp.serNum)+","+text;
						}
						break;
					case GraphProperty.DISPDATA_TITLE: // ����
						tmpc = egp.getAxisColor(GraphProperty.AXIS_PIEJOIN);
						tmpShape = new Line2D.Double(x1, y1, x2, y2);
						db.g.setStroke(new BasicStroke(1.0f));
						db.drawShape(tmpShape, tmpc);
						text = egs.getTips();
						break;
					default: // GraphProperty.DISPDATA_PERCENTAGE
						if (StringUtils.isValidString(gp.dataMarkFormat)) {
							fmt = gp.dataMarkFormat;
						} else {
							fmt = "0.00%";
						}
						tmpc = egp.getAxisColor(GraphProperty.AXIS_PIEJOIN);
						tmpShape = new Line2D.Double(x1, y1, x2, y2);
						db.g.setStroke(new BasicStroke(1.0f));
						db.drawShape(tmpShape, tmpc);
						if (i != maxi) {
							text = db.getFormattedValue(amount / totAmount, fmt);
							String tmp = text.substring(0, text.length() - 1);
							if (tmp.equals(".")
									|| !StringUtils.isValidString(tmp)) {
								tmp = "0";
							}
							cumulativeAmount += java.lang.Double
									.parseDouble(tmp);
							if(gp.dispValueType==GraphProperty.DISPDATA_NAME_PERCENTAGE){
								text = getDispName(egc,egs,gp.serNum)+","+text;
							}
						} else {
							maxAngle = tmpAngle;
							maxEgc = egc;
							maxEgs = egs;
							maxX = x2;
							maxY = y2;
						}
						break;
					}
					db.drawOutCircleText(gp.GFV_VALUE, text, tmpAngle,  x2,
							 y2);
				}
				totAngle += angle;
			}

			/* ����1��ϵ�е�ʱ��ֻ��ע��������� */
			if (gp.serNum > 1) {
				int angle = Math.round(360 / gp.serNum) * z;
				g.setColor(gp.coorColor);
				double x1 = orgx
						+ Math.round((radx - radiusx / 2)
								* Math.cos(Math.toRadians(angle)));
				double y1 = orgy
						- Math.round((rady - radiusy / 2)
								* Math.sin(Math.toRadians(angle)));
				double x2 = orgx
						+ Math.round((radiusx * gp.serNum + 5)
								* Math.cos(Math.toRadians(angle)));
				double y2 = orgy
						- Math.round((radiusy * gp.serNum + 5)
								* Math.sin(Math.toRadians(angle)));
				tmpc = egp.getAxisColor(GraphProperty.AXIS_PIEJOIN);
				tmpShape = new Line2D.Double(x1, y1, x2, y2);
				db.g.setStroke(new BasicStroke(1.0f));
				db.drawShape(tmpShape, tmpc);
				db.drawOutCircleText(gp.GFV_XLABEL, serName, angle,  x2,
						 y2);
			} else {
				// ѭ���������������������ֵ
				if (gp.dispValueType == GraphProperty.DISPDATA_PERCENTAGE
						|| gp.dispValueType == GraphProperty.DISPDATA_NAME_PERCENTAGE) {
					String fmt;
					if (StringUtils.isValidString(gp.dataMarkFormat)) {
						fmt = gp.dataMarkFormat;
					} else {
						fmt = "0.00%";
					}
					String text = db.getFormattedValue(
							(100.0 - cumulativeAmount) / 100, fmt);
					if(gp.dispValueType==GraphProperty.DISPDATA_NAME_PERCENTAGE){
						text = getDispName(maxEgc,maxEgs,gp.serNum)+","+text;
					}
					db.drawOutCircleText(gp.GFV_VALUE, text, maxAngle,  maxX,
							 maxY);
				}
			}
			orgy -= dely;
		}
		db.outLabels();

	}

	/**
	 * ����ڽǶ�angle�����ϣ�����ԭ�㳤��Ϊlen��x����ƫ����
	 * 
	 * @param radx
	 *            int��x��뾶
	 * @param rady
	 *            int��y��뾶
	 * @param len
	 *            int������
	 * @param angle
	 *            int���Ƕ�
	 * @return int��ƫ��ֵ
	 */
	private static double getDxOnAngle(double radx, double rady, double len,
			double angle) {
		return Math.round(len * Math.cos(Math.toRadians(angle)));
	}

	private static double getDyOnAngle(double radx, double rady, double len,
			double angle) {
		return -Math.round(len * rady / radx * Math.sin(Math.toRadians(angle)));
	}

	public ChartColor getChartColor(Color c) {
		ChartColor cc = new ChartColor(c);
		if (isGradientColor(egp)) {// ����ǽ���ɫ������ΪChartColor����ģʽ
			cc.setColor1(c);
			cc.setColor2(c);
			cc.setGradient(true);
		} else {
			cc.setGradient(false);
		}
		return cc;
	}

	// ����͹��Ĭ�Ͻ�ֹ������ɫ���У�
	// ��ͼ���⴦��Ϊ�����䣬Ϊ������
	private static boolean isGradientColor(ExtGraphProperty egp) {
		return (egp.isRaisedBorder() && egp.getImageFormat() != GraphProperty.IMAGE_GIF)
				|| egp.isGradientColor();
	}

	private static Shape getPath2DShape(double[] x, double[] y) {
		return Utils.newPolygon2DShape(x, y);
	}

	// isCurve:�Ƿ���ϱ�
	private static void drawFrontBaffle(DrawBase db,double totAngle, double orgx, double radx,
			double movex, double orgy, double rady, double movey, double dely,
			double angle, int i, boolean isCurve) {
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		double ex1 = 0;
		double bx1 = 0;
		double ey1 = 0;
		double by1 = 0;
		double totAngle1 = totAngle;
		if (!isCurve && totAngle < 180) {
			totAngle1 = 180;
		}
		Rectangle2D.Double shinningRange = new Rectangle2D.Double(orgx - radx
				+ movex, orgy - rady + movey, 2 * radx, 2 * rady);

		Arc2D.Double ddd = new Arc2D.Double(orgx - radx + movex, orgy - rady
				+ movey, 2 * radx, 2 * rady, totAngle1, totAngle + angle
				- totAngle1, Arc2D.PIE);
		Arc2D.Double dddd = new Arc2D.Double(orgx - radx + movex, orgy - rady
				+ movey, 2 * radx, 2 * rady, totAngle1 - 1, totAngle + angle
				- totAngle1 + 2, Arc2D.PIE);
		Arc2D.Double ddd1 = null;
		if (isCurve) {
			double curveY = dely;
			ddd1 = new Arc2D.Double(orgx - radx + movex + curveY, orgy - rady
					+ movey - dely + curveY, 2 * (radx - curveY) - 1,
					2 * (rady - curveY) - 1, totAngle1, totAngle + angle
							- totAngle1, Arc2D.PIE);
		} else {
			ddd1 = new Arc2D.Double(orgx - radx + movex, orgy - rady + movey
					- dely, 2 * radx, 2 * rady, totAngle1, totAngle + angle
					- totAngle1, Arc2D.PIE);
		}

		ex1 = ddd.getEndPoint().getX();
		bx1 = ddd.getStartPoint().getX();
		ey1 = ddd.getEndPoint().getY();
		by1 = ddd.getStartPoint().getY();
		double aa = dddd.getEndPoint().getX();
		double aa1 = dddd.getStartPoint().getX();
		double bb = dddd.getEndPoint().getY();
		double bb1 = dddd.getStartPoint().getY();
		double ptx1[] = { ddd1.getStartPoint().getX(), 
				bx1, ex1, ddd1.getEndPoint().getX(), 
		};
		double pty1[] = { ddd1.getStartPoint().getY(),
				by1, ey1, ddd1.getEndPoint().getY(),
		};
		Shape pp1 = getPath2DShape(ptx1, pty1);
		double ptx2[] = { orgx + movex, aa1, aa, };
		double pty2[] = { orgy + movey, bb1, bb, };
		Shape pp2 = getPath2DShape(ptx2, pty2);
		final Area area = new Area(ddd);
		Area area1 = new Area(ddd1);
		Area area2 = new Area(pp1);
		Area area3 = new Area(pp2);

		area.subtract(area3);
		area.add(area1);
		area.add(area2);
		area.subtract(area1);

		Color c = db.getColor(i);
		ChartColor chartColor = db.getChartColor(c);
		if( !chartColor.isGradient() ){//������ǽ���ɫ����������һ�����ɫ
			c=new CubeColor(c).getF2();
			chartColor = db.getChartColor(c);
		}

		Color bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);
		int bs = Consts.LINE_SOLID;
		float bw = 1.0f;
		if (bc == null)
			bw = 0;// ͸��ɫ�����߿򣬵���Utils��bcΪnull��ʾ��ǰ��ɫ
		Utils.drawCylinderFront(g, area, bc, bs, bw, db.getTransparent(),
				chartColor, true, shinningRange, isCurve);
	}

	/**
	 * ���󵲰�
	 * */
	private static void drawBackBaffle(DrawBase db,double totAngle, double orgx, double radx,
			double movex, double orgy, double rady, double movey, double dely,
			double angle, int i) {
		Graphics2D g = db.g;

		double ex1 = 0;
		double bx1 = 0;
		double ey1 = 0;
		double by1 = 0;
		Arc2D.Double ddd = null;
		Arc2D.Double ddd1 = null;
		double totAngle1 = totAngle + angle;
		if (totAngle + angle > 180) {
			totAngle1 = 180;
		}
		ddd = new Arc2D.Double(orgx - radx + movex, orgy - rady + movey,
				2 * radx, 2 * rady, totAngle, totAngle1 - totAngle, Arc2D.PIE);
		ddd1 = new Arc2D.Double(orgx - radx + movex,
				orgy - rady + movey - dely, 2 * radx, 2 * rady, totAngle,
				totAngle1 - totAngle, Arc2D.PIE);
		ex1 =  ddd.getEndPoint().getX();
		bx1 =  ddd.getStartPoint().getX();
		ey1 =  ddd.getEndPoint().getY();
		by1 =  ddd.getStartPoint().getY();
		double ptx1[] = { bx1, bx1, ex1, ex1 };
		double pty1[] = { -dely + by1, by1, ey1, -dely + ey1 };
		// Polygon pp1 = new Polygon(ptx1, pty1, 4);
		Shape pp1 = getPath2DShape(ptx1, pty1);
		double ptx2[] = { orgx + movex, bx1, ex1, };
		double pty2[] = { orgy + movey - dely, by1 - dely, ey1 - dely, };
		// Polygon pp2 = new Polygon(ptx2, pty2, 3);
		Shape pp2 = getPath2DShape(ptx2, pty2);
		Area area = new Area(ddd); // ���������
		Area area1 = new Area(ddd1); // ���������
		Area area2 = new Area(pp1); // �ı���
		Area area3 = new Area(pp2); // �����������
		area1.subtract(area3);
		area1.add(area);
		area1.add(area2);
		area1.subtract(area);
		g.setColor(db.getColor(i));
		g.fill(area1);
		db.drawShape(area1, bc);
	}

	private static void drawCut1(DrawBase db,double bx1, double by1, double orgx, double orgy,
			double movex, double movey, double dely, int i, double bx2,
			double by2, double curveY) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		double ptx1[], pty1[];
		curveY = Math.ceil(curveY / 2.0f);
		if (egp.isRaisedBorder()) {
			ptx1 = new double[] { bx1 - movex, bx1 - movex, bx2 - movex, orgx,
					orgx };
			pty1 = new double[] { by1 - movey, -dely + by1 - movey,
					by2 - movey, -dely - curveY + orgy, orgy };
		} else {
			ptx1 = new double[] { bx1 - movex, bx1 - movex, orgx, orgx };
			pty1 = new double[] { by1 - movey, -dely + by1 - movey,
					-dely + orgy, orgy };
		}
		Color c;
		if (i == 0) {
			c = db.getColor(gp.catNum - 1);
		} else {
			c = db.getColor(i - 1);
		}
		CubeColor ccr = new CubeColor(c);
		g.setColor(ccr.getRelativeDarker("F2", cutColorLevel));// ��F2��һ������ɫ
		Shape s = getPath2DShape(ptx1, pty1);
		g.fill(s);
		db.drawShape(s, bc);
	}

	private static void drawCut2(DrawBase db,double bx1, double by1, double orgx, double orgy,
			double movex, double movey, double dely, int i, double bx2,
			double by2, double curveY) {
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		curveY = curveY / 2;
		double ptx1[], pty1[];
		if (egp.isRaisedBorder()) {
			ptx1 = new double[] { bx1, bx1, bx2, orgx + movex, orgx + movex };
			pty1 = new double[] { by1, -dely + by1, by2,
					-dely - curveY + orgy + movey, orgy + movey };
		} else {
			ptx1 = new double[] { bx1, bx1, orgx + movex, orgx + movex };
			pty1 = new double[] { by1, -dely + by1, -dely + orgy + movey,
					orgy + movey };
		}
		Color c = db.getColor(i);
		CubeColor ccr = new CubeColor(c);
		g.setColor(ccr.getRelativeDarker("F2", cutColorLevel));

		Shape s = getPath2DShape(ptx1, pty1);
		g.fill(s);
		db.drawShape(s, bc);
	}

	private static void drawCut3(DrawBase db,double ex1, double ey1, double orgx, double orgy,
			double movex, double movey, double dely, int i, double ex2,
			double ey2, double curveY) {
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		curveY = curveY / 2;
		double ptx1[], pty1[];
		if (egp.isRaisedBorder()) {
			ptx1 = new double[] { ex1, ex1, ex2, orgx + movex, orgx + movex };
			pty1 = new double[] { ey1, -dely + ey1, ey2,
					-dely - curveY + orgy + movey, orgy + movey };
			g.setColor(db.getColor(i).darker().darker());
		} else {
			ptx1 = new double[] { ex1, ex1, orgx + movex, orgx + movex };
			pty1 = new double[] { ey1, -dely + ey1, -dely + orgy + movey,
					orgy + movey };
			g.setColor(db.getColor(i).darker());
		}
		Color c = db.getColor(i);
		CubeColor ccr = new CubeColor(c);
		g.setColor(ccr.getRelativeDarker("F2", cutColorLevel));

		Shape s = getPath2DShape(ptx1, pty1);
		g.fill(s);
		db.drawShape(s, bc);
	}

	private static void drawCut4(DrawBase db,double ex1, double ey1, double orgx, double orgy,
			double movex, double movey, double dely, int i, double ex2,
			double ey2, double curveY) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		curveY = curveY / 2;
		double ptx1[], pty1[];
		if (egp.isRaisedBorder()) {
			ptx1 = new double[] { ex1 - movex, ex1 - movex, ex2 - movex, orgx,
					orgx };
			pty1 = new double[] { ey1 - movey, -dely + ey1 - movey,
					ey2 - movey, -dely - curveY + orgy, orgy };
		} else {
			ptx1 = new double[] { ex1 - movex, ex1 - movex, orgx, orgx };
			pty1 = new double[] { ey1 - movey, -dely + ey1 - movey,
					-dely + orgy, orgy };
		}
		Color c;
		if (i + 1 == gp.catNum) {
			c = db.getColor(0);
		} else {
			c = db.getColor(i + 1);
		}
		CubeColor ccr = new CubeColor(c);
		Shape s = getPath2DShape(ptx1, pty1);
		g.setColor(ccr.getRelativeDarker("F2", cutColorLevel));
		g.fill(s);
		db.drawShape(s, bc);
	}
}
