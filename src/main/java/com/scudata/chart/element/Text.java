package com.scudata.chart.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.scudata.app.common.StringUtils2;
import com.scudata.chart.Consts;
import com.scudata.chart.DataElement;
import com.scudata.chart.Para;
import com.scudata.chart.Utils;
import com.scudata.chart.edit.ParamInfo;
import com.scudata.chart.edit.ParamInfoList;
/**
 * �ı�ͼԪ
 * �ı�ͼԪ��������ı���ʽչ�֣���������������չʾ
 * @author Joancy
 *
 */
public class Text extends DataElement{
	//�����ı�ʱ�������Ч����������ʵ�ʿ�����ģ�
//	�����ı�ʱ�� ������ߵĽ��ص�
//	����ʱ���ÿ�������� ����ߴ�
	public Para width=new Para(new Double(0));
	public Para height=new Para(new Double(0));//����ʱ������ݿ������

	public Para text = new Para(null);
	public Para textFont = new Para("Dialog");//"����"
	public Para textStyle = new Para(new Integer(0));
	public Para textSize = new Para(new Integer(14));
	public Para textColor = new Para(Color.black);
	public Para backColor = new Para(null);
	
//	�Ƿ����ʽ�ı��������ı�ʱ��������width��height��x��yΪ�ı����ĵ㣬����Ҳ�������б���
//	�����ı�ʱ��x,yΪ���Ͻǣ����ÿ�͸ߣ�������ת���������ԣ�����Ϊw��h�޶��ľ���
	public boolean isMulti = false;

	// ��ת�Ƕ�
	public int textAngle = 0;

	// �������
	public int hAlign = Consts.HALIGN_CENTER;

	// �������
	public int vAlign = Consts.VALIGN_MIDDLE;
	
	// �������������
	public int barType = Consts.TYPE_NONE;

	// �ַ��� 
	public String charSet = "UTF-8";
	
	// ������ʱ��ʾ����
	public boolean dispText = false;

	// �ݴ���
	public String recError = "M";
	
	//logoͼ����
	public Para logoValue = new Para();
	
	//logoռͼ�εİٷֱ���
	public int logoSize=15;

	// logo������
	public boolean logoFrame = true;
	
	/**
	 * �Ƿ�Ϊ�����ı�
	 * @return ����Ƕ����ı�����true�����򷵻�false
	 */
	public boolean isMulti() {
		return isMulti;
	}

	/**
	 * ��ͼǰ׼����������ǰ���Ըú���
	 */
	public void beforeDraw() {
	}

	/**
	 * �����м�㣬���ԣ�������
	 */
	public void draw() {
	}

	/**
	 * ���Ʊ����㣬����
	 */
	public void drawBack() {
	}

	/**
	 * ��ȡ���λ�õ��ı���ռ���
	 * @param index ���
	 * @return ���ؿ��
	 */
	public int getWidth(int index){
		return (int)e.getXPixel(width.doubleValue(index));
	}
	
	/**
	 * ��ȡ���λ�õ��ı���ռ���
	 * @param index ���
	 * @return ���ظ߶�
	 */
	public int getHeight(int index){
		return (int)e.getYPixel(height.doubleValue(index));
	}
	
	/**
	 * ����ǰ����
	 */
	public void drawFore() {
		if (!isVisible()) {
			return;
		}
		drawTexts();
	}
	
	private void drawTexts() {
		// ����
		int size = pointSize();
		for (int i = 1; i <= size; i++) {
			Point2D p = getScreenPoint(i);
			Shape shape = drawAText(i, p);
			if(shape!=null){
				String title = getTipTitle(i);
				addLink(shape, htmlLink.stringValue(i), title,linkTarget.stringValue(i));
			}
		}
	}
	
	private Shape drawAText(int index, Point2D p) {
		double px = p.getX();
		double py = p.getY();
		String tf = textFont.stringValue(index);
		int ts = textStyle.intValue(index);
		int tsize = textSize.intValue(index);
		
		Font font = Utils.getFont(tf, ts, tsize);
		Color c = textColor.colorValue(index);
		Color backC = backColor.colorValue(index);
		String aText = text.stringValue(index);
		
		if(barType==Consts.TYPE_NONE){
			Rectangle shape;
			Graphics g = e.getGraphics();
			if(!isMulti){
//				�����ı�ʱ�������ÿ�ߣ�ָ����x��yΪ�ı����ĵ�
				Utils.drawText(e, aText, px, py, font, c, backC, ts, textAngle, hAlign
						+ vAlign, true);
				shape = Utils.getTextSize(aText, g, ts, textAngle, font);
				shape.setLocation((int)px, (int)py);
			}else{
				FontMetrics fm = g.getFontMetrics(font);
				int ascent = fm.getAscent();
				int fheight = fm.getHeight();
				int w = getWidth(index);
				if(w<1){
					w = 60;//δ����ʱȱʡ���
				}
				ArrayList<String> wrapedString = StringUtils2.wrapString(aText, fm, w, false, -1);
				//	����ʱ�����Դ�ֱ���֣���ת������״̬
				int h = getHeight(index);
				int lineH = StringUtils2.getTextRowHeight(fm);
				int lines = wrapedString.size();
				if(h<1){
					h = lineH*lines;//δ����ʱȱʡ���
				}
				shape = new Rectangle((int)px, (int)py, w, h);
				if(backC!=null){
					g.setColor(backC);
					g.fillRect((int)px, (int)py, w, h);
				}

				
				int yy = (int)py;
				if (vAlign == Consts.VALIGN_MIDDLE) {
					yy = (int)py + (h - lineH * lines) / 2;
				} else if (vAlign == Consts.VALIGN_BOTTOM) {
					yy = (int)py + h - lineH * lines;
				}
				if (yy < py) {
					yy = (int)py;
				}
				for (int i = 0; i < lines; i++) {
					if (i > 0 && yy + lineH > py + h) { // ��һ�����ǻ��ƣ�����������ڿ��ⲻ���������ڸǱ�ĸ�������
						break;
					}

					String wrapedText = (String) wrapedString.get(i);
					int fw = stringWidth(fm, wrapedText,g);
					int x1 = (int)px;
					if (hAlign == Consts.HALIGN_CENTER) {
						x1 = (int)px + (w - fw) / 2;
					} else if (hAlign == Consts.HALIGN_RIGHT) {
						x1 = (int)px + w - fw;
					}
					int y1 = yy + fheight-ascent;//+ ascent;// + 
					Utils.drawText(e, wrapedText, x1, y1, font, c, null, ts, 0,
							Consts.HALIGN_LEFT+Consts.VALIGN_MIDDLE
							, true);
					yy += lineH;
				}
			}
			return shape;
		}else{
			Graphics2D g = e.getGraphics();
			BufferedImage barcodeImg = Utils.calcBarcodeImage(this,index,c,backC);
			Rectangle posDesc = new Rectangle();
			posDesc.setBounds((int)px, (int)py, barcodeImg.getWidth(), barcodeImg.getHeight());
			Point drawPoint = Utils.getRealDrawPoint(posDesc, hAlign + vAlign,true);
			g.drawImage(barcodeImg, drawPoint.x, drawPoint.y, null);
//			g.drawImage(barcodeImg, (int)px, (int)py,null);����ע�͹����������ͼƬ������Ҫ���ݶ��뷽ʽ����
			return posDesc;
		}
	}

	/**
	 * ����ָ�������µ��ı�ռ�ÿ��
	 * @param fm �������
	 * @param text �ı�ֵ
	 * @param g ͼ���豸
	 * @return ռ�ÿ�ȣ���λΪ����
	 */
	public static int stringWidth(FontMetrics fm, String text, Graphics g) {
		Graphics displayG = g;
		FontMetrics dispFm = displayG.getFontMetrics(fm.getFont());
		return dispFm.stringWidth(text);
	}
	
	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(Text.class, this);

		paramInfos.add(new ParamInfo("text"));
		paramInfos.add(new ParamInfo("isMulti", Consts.INPUT_CHECKBOX));

		String group = "size";
		paramInfos.add(group, new ParamInfo("width", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("height", Consts.INPUT_DOUBLE));

		group = "text";
		paramInfos.add(group, new ParamInfo("textFont", Consts.INPUT_FONT));
		paramInfos.add(group,
				new ParamInfo("textStyle", Consts.INPUT_FONTSTYLE));
		paramInfos.add(group, new ParamInfo("textSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group, new ParamInfo("textColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("backColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("textAngle", Consts.INPUT_INTEGER));

		group = "align";
		paramInfos.add(group, new ParamInfo("hAlign", Consts.INPUT_HALIGN));
		paramInfos.add(group, new ParamInfo("vAlign", Consts.INPUT_VALIGN));
		
		group = "barcode";
		paramInfos.add(group, new ParamInfo("barType", Consts.INPUT_BARTYPE));
		paramInfos.add(group, new ParamInfo("charSet", Consts.INPUT_CHARSET));
		paramInfos.add(group, new ParamInfo("dispText", Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("recError", Consts.INPUT_RECERROR));
		paramInfos.add(group, new ParamInfo("logoValue"));
		paramInfos.add(group, new ParamInfo("logoSize", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("logoFrame", Consts.INPUT_CHECKBOX));
		
		ParamInfoList superParams = super.getParamInfoList();
		superParams.delete("data","axisTime");
		superParams.delete("data","dataTime");
		paramInfos.addAll( superParams);
		
		return paramInfos;
	}

	/**
	 * ��ȡָ������µ���ʾ�ı�
	 * @param index ���
	 * @return �ı�����
	 */
	public String getDispText(int index) {
		return text.stringValue(index);
	}
	
	protected String getText(int index) {
//		����ʱ�� tipΪ��ǰ����
		if(barType!=Consts.TYPE_NONE){
			return text.stringValue(index);
		}
//		�ı�ʱ���˴������أ� �ײ㷵��������Ϣ
		return null;
	}

	/**
	 * �Ƿ����������ɫ
	 * �ú��������壬����false
	 */
	public boolean hasGradientColor() {
		return false;
	}

	/**
	 * ��¡�ı�����
	 * @param t ��һ���ı�����
	 */
	public void clone(Text t){
		super.clone(t);
	}
	
	/**
	 * ��ȿ�¡һ���ı�ͼԪ
	 * @return ��¡����ı�ͼԪ
	 */
	public Object deepClone() {
		Text t = new Text();
		clone(t);
		return t;
	}
}
