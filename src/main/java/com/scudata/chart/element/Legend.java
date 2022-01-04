package com.scudata.chart.element;

import java.util.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.ParamInfo;
import com.scudata.chart.edit.ParamInfoList;
import com.scudata.chart.resources.ChartMessage;
import com.scudata.common.*;
import com.scudata.dm.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * ͼ��ͼԪ
 * @author Joancy
 *
 */
public class Legend extends LinkElement implements IMapAxis{
	// ������
	public String name;
	// ��������,С��1��ʾ����������1��ʾ����
	public double x = 0.82;
	public double y = 0.2;
	public double width = 0;
	public double height = 0;
	public boolean visible = true;
	public float transparent = 1f;
	public int iconWidth = 20;
	public int edgeIndent = 5;

	public Color backColor = new Color(255, 255, 255, 0);
	public int borderStyle = Consts.LINE_SOLID;
	public int borderWeight = 1;
	public Color borderColor = Color.black;

	// ͼ������
	public Para legendText=new Para();

	// ��������
	public String textFont;// = "����";

	// ������ɫ
	public Color textColor = Color.black;

	// ��������
	public int textStyle;

	// �����ֺ�
	public int textSize = 12;

	// ͼ��������
	public int columns = 1;

	public Para legendType = new Para(new Integer(Consts.LEGEND_RECT));
	public Para legendLineStyle = new Para(new Integer(Consts.LINE_SOLID));
	public Para legendLineWeight = new Para(new Float(1));
	public Para legendLineColor = new Para(Color.lightGray);
	public Para legendFillColor = new Para();
	public Para legendMarkerShape = new Para(new Integer(Consts.PT_CIRCLE));

	private transient Engine e;
	private transient double px, py, pw, ph;


	/**
	 * ��ͼǰ׼��
	 * ��ǰ����
	 */
	public void beforeDraw() {
	}

	/**
	 * ��ȡͼ�����ı�����
	 * ����ͼ������ȱʡ�����ã���ʱ�ӵ�ǰͼԪ���ҳ�ͼ�����������Զ�����
	 * @return ͼ�����ı�����
	 */
	public Sequence getLegendText(){
		return legendText.sequenceValue();
	}
	
	/**
	 * ����ͼ�����ı�
	 * @param seq �ı�����
	 */
	public void setLegendText(Sequence seq){
		this.legendText.setValue(seq);
	}
	
	/**
	 * ���Ʊ�����
	 */
	public void drawBack() {
		if (!isVisible()) {
			return;
		}
		px = e.getXPixel(x);
		py = e.getYPixel(y);

		if (width <= 0) {
			pw = autoWidth();
		} else {
			pw = e.getXPixel(width);
		}

		if (height <= 0) {
			ph = drawLegend(false) + edgeIndent;
		} else {
			ph = e.getYPixel(height);
		}
		Graphics2D g = e.getGraphics();
//			g.setColor(backColor);
		Rectangle2D.Double rect = new Rectangle2D.Double(px, py, pw, ph);
		Utils.fill(g, rect, transparent,backColor);
	}

	/**
	 * �����м��
	 */
	public void draw() {
		if (!isVisible()) {
			return;
		}
		Graphics2D g = e.getGraphics();
		if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)) {
			g.drawRect((int) px, (int) py, (int) pw, (int) ph);
		}
	}

	private Shape drawIcon(int x, int y, int pos) {
		int type = legendType.intValue(pos);
		Graphics2D g = e.getGraphics();
		Color fColor = legendLineColor.colorValue(pos);
		if (fColor == null) {
			fColor = Para.defColorValue(pos);
		}
		int style = legendLineStyle.intValue(pos);
		float weight = legendLineWeight.floatValue(pos);
		ChartColor bColor = legendFillColor.chartColorValue(pos);
		int r = iconWidth / 3;
		int hr = iconWidth / 2;
		int cx = x + hr;
		int cy = y + hr;
		int shape = legendMarkerShape.intValue(pos);
		Rectangle rect = new Rectangle(x, y, iconWidth, iconWidth);
		Point point;
		switch (type) {
		case Consts.LEGEND_RECT:
			ChartColor backColor = bColor;
			Color foreColor = fColor;
			point = new Point(cx,cy);
			Utils.drawCartesianPoint2(g, point, 2, hr, hr, 0,
					style, weight, backColor, foreColor, transparent);
			break;
		case Consts.LEGEND_POINT:
			point = new Point(cx,cy);
			Utils.drawCartesianPoint2(g, point, shape, r, r, r, style, weight, bColor, fColor, 1f);
			break;
		case Consts.LEGEND_LINE:
			if (Utils.setStroke(g, bColor.getColor1(), style, weight)) {
				g.drawLine(x, cy, x + iconWidth, cy);
			}
			break;
		case Consts.LEGEND_LINEPOINT:
			r = iconWidth / 4;
			if (Utils.setStroke(g, fColor, style, weight)) {
				g.drawLine(x, cy, x + iconWidth, cy);
			}
			Point p = new Point(cx,cy);
			Utils.drawCartesianPoint2(g, p, shape, r, r, r, style, weight, bColor, fColor, 1f);
			break;
		case Consts.LEGEND_NONE:
			break;
		}
		return rect;
	}

	private double autoWidth() {
		if(legendText==null){
			String msg = ChartMessage.get().getMessage("legendTextEmpty");
			throw new RQException( msg );
		}
		Font font = Utils.getFont(textFont, textStyle, textSize);
		Graphics2D g = e.getGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		double maxW = 0;

		int size = legendText.getLength();
		for (int i = 1; i <= size; i++) {
			String text = legendText(i);
			int w = fm.stringWidth(text);
			if (w > maxW) {
				maxW = w;
			}
		}
		if (columns < 1) {
			columns = 1;
		}

		return (maxW + edgeIndent * 3 + iconWidth) * columns;
	}

	private int drawLegend(ArrayList wrapText, int x, int y, int width,
			int txtHeight, int index, Font font, boolean reallyDraw) {
		int iconX = x + width - iconWidth - edgeIndent;
		int iconY = y + edgeIndent;
		int legendHeight = edgeIndent;
		StringBuffer linkBuf = new StringBuffer();
		if (wrapText.isEmpty()) {
			legendHeight += txtHeight;
		} else {
			int txtRows = wrapText.size();
			Graphics2D g = e.getGraphics();
			int sx = x + edgeIndent;
			int sy = y + edgeIndent;
			for (int i = 0; i < txtRows; i++) {
				String str = (String) wrapText.get(i);
				if (reallyDraw) {
					linkBuf.append(str);
					Utils.drawText(g, str, sx, sy, font, textColor, textStyle,
							0, Consts.LOCATION_LT);
				}
				legendHeight += txtHeight;
				sy = y + legendHeight;
			}
		}
		if (reallyDraw) {
			Shape linkShape = drawIcon(iconX, iconY, index);
			if(linkShape!=null){
				String title = getTipTitle(index);
				addLink(linkShape, htmlLink.stringValue(index), title,linkTarget.stringValue(index));
			}
		}
		return Math.max(legendHeight, iconWidth + edgeIndent);
	}

	/**
	 * ����ǰ���㣬ͼ�����Ʋ��֣� (��߿�+GAP+����+ͼ��+GAP) = columnWidth
	 */
	public void drawFore() {
		drawLegend(true);
	}
	
	private String legendText(int index){
		return legendText.stringValue(index);
	}
	
	private double drawLegend(boolean reallyDraw) {
		if (!isVisible()) {
			return 0;
		}
		if (columns < 1) {
			columns = 1;
		}
		double columnWidth = pw / columns;

		if ((columnWidth - 2 * edgeIndent) < iconWidth) {
			return 0;
		}
		Font font = Utils.getFont(textFont, textStyle, textSize);
		Graphics2D g = e.getGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		char aChar = '��';
		int textHeight = fm.getHeight();
		int aCharWidth = fm.charWidth(aChar);
		double textWidth = columnWidth - 2 * edgeIndent - iconWidth;

		int size = legendText.getLength();
		double yy = py, autoH = 0;
		for (int r = 0;; r++) {
			int tmpRowH = 0;
			for (int c = 0; c < columns; c++) {
				int index = r * columns + c + 1;
				if (index > size) {
					return autoH + tmpRowH;
				}
				String text = legendText(index);
				double xx = px + columnWidth * c;
				ArrayList wrapText;
				if (textWidth < aCharWidth) { // �������ֵĿ�Ȳ���һ���ַ�ʱ����������
					wrapText = new ArrayList();
				} else {
					wrapText = StringUtils.wrapString(text, fm,
							(float) textWidth);
				}

				g.setFont(font);
				g.setColor(textColor);
				int h = drawLegend(wrapText, (int) xx, (int) yy,
						(int) columnWidth, textHeight, index, font, reallyDraw);
				if (h > tmpRowH) {
					tmpRowH = h;
				}
			}
			yy += tmpRowH;
			autoH += tmpRowH;
		}
	}

	/**
	 * ��ȡ����
	 * @return ��ͼ����
	 */
	public Engine getEngine() {
		return e;
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ParamInfoList ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(Legend.class, this);

		paramInfos.add(new ParamInfo("name"));
		paramInfos.add(new ParamInfo("legendText"));
		paramInfos.add(new ParamInfo("backColor", Consts.INPUT_COLOR));
		paramInfos.add(new ParamInfo("columns", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("transparent", Consts.INPUT_DOUBLE));
		paramInfos.add(new ParamInfo("iconWidth", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("edgeIndent", Consts.INPUT_INTEGER));
		paramInfos.add(new ParamInfo("visible", Consts.INPUT_CHECKBOX));

		String group = "coordinates";
		paramInfos.add(group, new ParamInfo("x", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("y", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("width", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("height", Consts.INPUT_DOUBLE));

		group = "border";
		paramInfos.add(group, new ParamInfo("borderStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("borderWeight",
				Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("borderColor", Consts.INPUT_COLOR));

		group = "text";
		paramInfos.add(group, new ParamInfo("textFont", Consts.INPUT_FONT));
		paramInfos.add(group,
				new ParamInfo("textStyle", Consts.INPUT_FONTSTYLE));
		paramInfos.add(group, new ParamInfo("textSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group, new ParamInfo("textColor", Consts.INPUT_COLOR));

		group = "Legend";// ��ͼ��ͼԪͳһ����Ϊ��д��ͷ����Դ�ļ�Ϊ��Сд����
		paramInfos.add(group, new ParamInfo("legendType",
				Consts.INPUT_LEGENDICON));
		paramInfos.add(group, new ParamInfo("legendLineStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("legendLineWeight",
				Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("legendLineColor",
				Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("legendFillColor",
				Consts.INPUT_CHARTCOLOR));
		paramInfos.add(group, new ParamInfo("legendMarkerShape",
				Consts.INPUT_POINTSTYLE));
		
		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}

	/**
	 * ͼ���Ƿ�ɼ�
	 * @return boolean �ɼ�����true�����򷵻�false
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * ���û�ͼ����
	 * @param e ��ͼ����
	 */
	public void setEngine(Engine e) {
		this.e = e;
		Utils.setParamsEngine(this);
	}

	/**
	 * ����ͼ������
	 */
	public String getName() {
		return name;
	}

	/**
	 * ��ȡ�߼�ֵval��ӳ������mapProperty�ϵ�ӳ������ֵ
	 * @param val �߼�����ֵ
	 * @param mapProperty ӳ�����ԣ�Ŀǰ��֧���������ԣ�
	 * Consts.LEGEND_P_FILLCOLOR: �����ɫ
	 * Consts.LEGEND_P_LINECOLOR: �߿����������ɫ
	 * Consts.LEGEND_P_MARKERSTYLE: ����
	 */
	public Object getMapValue(Object val, byte mapProperty) {
		if(legendText==null){
			String msg = ChartMessage.get().getMessage("legendTextEmpty");
			throw new RQException( msg );
		}
		switch(mapProperty){
		case Consts.LEGEND_P_FILLCOLOR:
			return MapAxis.getMapValue(legendText.sequenceValue(), val, legendFillColor);
		case Consts.LEGEND_P_LINECOLOR:
			return MapAxis.getMapValue(legendText.sequenceValue(), val, legendLineColor);
		case Consts.LEGEND_P_MARKERSTYLE:
			return MapAxis.getMapValue(legendText.sequenceValue(), val, legendMarkerShape);
		}
		return null;
	}

}
