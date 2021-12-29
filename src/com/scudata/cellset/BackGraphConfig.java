package com.scudata.cellset;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.ImageIcon;

import com.scudata.chart.Utils;
import com.scudata.common.*;
/**
 * ����ͼ����
 * 
 * @author Joancy
 *
 */
public class BackGraphConfig implements Externalizable, ICloneable, Cloneable,
		IRecord {
	private static final long serialVersionUID = 1L;

	/** ����ͼ�������ͣ�URL */
	public final static byte TYPE_URL = (byte) 0;
	/** ����ͼ�������ͣ����ʽ */
	public final static byte TYPE_EXP = (byte) 1;

	/** ����ͼ��ʾ��ʽ����ʾ */
	public final static byte DISP_NONE = (byte) 10;
	/** ����ͼ��ʾ��ʽ��ÿҳ��ʾ */
	public final static byte DISP_PER_PAGE = (byte) 11;

	public static final byte SOURCE_NONE = 0; // ��
	public static final byte SOURCE_PICTURE = 1; // ͼƬ
	public static final byte SOURCE_TEXT = 2; // ����,ˮӡ����ͼ

	public static final byte MODE_NONE = 0; // ȱʡ
	public static final byte MODE_FILL = 1; // ���
	public static final byte MODE_TILE = 2; // ƽ��

	public static final byte TEXT_NORMAL = 0; // ����
	public static final byte TEXT_TILT = 1; // ��б

	private byte type = TYPE_URL;
	private String value;
	private byte disp = DISP_PER_PAGE;

	// ����ͼƬ�ķ�ʽ
	private byte imgSource = SOURCE_PICTURE;
	// ����
	private byte mode = MODE_NONE;
	private String fontName = "Dialog";
	private int fontSize = 12;
	private int textColor = Color.LIGHT_GRAY.getRGB();
	private int textGap = 40;
	private int textTransparency = 30;

	private byte[] imageBytes = null;

	public transient byte[] tmpImageBytes = null;
	private transient String waterMark = null;

	/**
	 * ȱʡ���캯��
	 */
	public BackGraphConfig() {
	}

	/**
	 * ���캯��
	 *
	 * @param type
	 *            ָ���������ͣ���ȡֵΪTYPE_URL��TYPE_EXP
	 * @param value
	 *            ����typeΪTYPE_URLʱ�˲�����ʾURL�� ΪTYPE_EXPʱ�˲�����ʾ���ʽ��
	 * @param dispMode
	 *            ��ʾģʽ����ȡֵΪDISP_DEFAULT��DISP_PER_PAGE
	 */
	public BackGraphConfig(byte type, String value, byte dispMode) {
		this.type = type;
		this.value = value;
		this.disp = dispMode;
	}

	/**
	 * ���ñ���ͼƬ���� TYPE_URL��TYPE_EXP
	 * 
	 * @param type
	 *            byte
	 */
	public void setType(byte type) {
		this.type = type;
	}

	/**
	 * ��ñ���ͼƬ����
	 * 
	 * @return byte
	 */
	public byte getType() {
		return this.type;
	}

	/**
	 * ����ULR����ʽ��������typeΪTYPE_URLʱ�˲�����ʾURL��ΪTYPE_EXPʱ�˲�����ʾ���ʽ��
	 * 
	 * @param urlOrClassName
	 *            String
	 */
	public void setValue(String value) {
		this.value = value;
		this.tmpImageBytes = null;
	}

	/**
	 * ȡ��ULR����ʽ��
	 * 
	 * @return String
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * ������ʾģʽ DISP_DEFAULT��DISP_PER_PAGE
	 * 
	 * @param dispMode
	 *            byte
	 */
	public void setDispMode(byte dispMode) {
		this.disp = dispMode;
	}

	/**
	 * ȡ����ʾ��ʽ
	 * 
	 * @return byte
	 */
	public byte getDispMode() {
		return this.disp;
	}

	/**
	 * ȡ����ͼ
	 */
	public byte[] getImageBytes() {
		return this.imageBytes;
	}

	/**
	 * ���ݿ�ߣ����ձ���ͼ���ã����ɱ���ͼ
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	public Image getBackImage(int w, int h) {
		return getBackImage(w,h,1.0f);
	}
	
	/**
	 * �÷�������ֱ�ӽ�����ͼ��g������Ӷ��ı���������
	 * @param g ����PDF���ߴ�ӡ��ͼ���豸
	 * @param w ���
	 * @param h �߶�
	 * @param scale ���Ʊ���
	 */
	public void drawImage(Graphics g, int w, int h, float scale){
		switch (imgSource) {
		case SOURCE_PICTURE:
			Image image = new ImageIcon(imageBytes).getImage();
			int iw = image.getWidth(null);
			int ih = image.getHeight(null);
			if(iw*ih<=0){
				return;
			}
			float nw = iw*scale;
			float nh = ih*scale;
			if(scale!=1.0f){
				image = image.getScaledInstance((int)nw, (int)nh, java.awt.Image.SCALE_SMOOTH);
				image = new ImageIcon(image).getImage();				
			}

			switch (mode) {
			case MODE_NONE:
				ImageUtils.drawFixImage(g, image, 0, 0, w, h);
				break;
			case MODE_FILL:
				g.drawImage(image, 0, 0, w, h, null);
				break;
			case MODE_TILE:
				iw = image.getWidth(null);
				ih = image.getHeight(null);
				int x = 0,
				y = 0;
				while (x < w) {
					y = 0;
					while (y < h) {
						ImageUtils.drawFixImage(g, image, x, y, w, h);
						y += ih;
					}
					x += iw;
				}
				break;
			}
			break;
		case SOURCE_TEXT:
			if (!StringUtils.isValidString(waterMark))
				return;
//			������֢����֪����ԭ��������setComposite�����ᵼ��
//			1����ӡ����ȱʧ
//			2��itextû��ʵ�֣�����PDFʱ��û�в�͸��״̬
//			����취����͸������Ϊ100��Ҳ����͸������ʱ������setComposite��ͬʱ����������ɫΪǳɫ
			
			Composite old = setTransparent((Graphics2D) g, textTransparency / 100f);
			Color c1 = new Color(textColor);
			int textAngle = 0;
			if (mode == TEXT_TILT) {
				textAngle = -45;
			}
			int fSize = StringUtils.getScaledFontSize(fontSize,scale);
			Rectangle textRect = getTextRect(fontName, fSize, waterMark);
			iw = textRect.width;
			ih = textRect.height;
			int x = 0,
			y = 0;
			Font font = getFont(fontName, 0, fSize);
			Color c = c1;

			int row = 0,
			col = 0;
			while (x < w) {
				y = 0;
				row = 0;
				while (y < h) {
					row++;
					int mod = (row + col) % 2;
					if (mod == 1) {
						drawText(waterMark, x + iw / 2, y + ih / 2, font, c,
								textAngle, (Graphics2D) g);
					}
					y += ih + textGap;
				}
				x += iw + textGap;
				col++;
			}
			if(old!=null){
				((Graphics2D)g).setComposite(old);
			}
		}
	}
	
	/**
	 * ����ǰ����ͼ����ΪͼƬ����
	 * @param w ���
	 * @param h �߶�
	 * @param scale ���Ʊ���
	 * @return ͼ�����
	 */
	public Image getBackImage(int w, int h, float scale) {
		if (imgSource == SOURCE_NONE) {
			return null;
		}
		if (imgSource == SOURCE_PICTURE && imageBytes == null) {
			return null;
		}
		if (imgSource == SOURCE_TEXT && waterMark == null) {
			return null;
		}
		BufferedImage bimage = null;
		bimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g;
		g = bimage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		drawImage(g,w,h,scale);
		return bimage;
	}

	/**
	 * ���ñ���ͼ���ݣ�һ������ͼƬ��Ҫ����ˢ�£�����û��Ҫÿ�ζ��ػ�ʱ
	 * ������߽����������
	 * @param b ͼ������
	 */
	public void setImageBytes(byte[] b) {
		this.imageBytes = b;
		waterMark = null;
	}

	/**
	 * ��ȡ��������ˮӡ����
	 * @return ˮӡ����
	 */
	public String getWaterMark() {
		return waterMark;
	}

	/**
	 * ���ü�����ˮӡ���֣�������߻�ͼ����
	 * @param wm ˮӡ����
	 */
	public void setWaterMark(String wm) {
		this.waterMark = wm;
		imageBytes = null;
	}

	/**
	 * ��ȡˮӡ���߱���ͼ�������ʽ
	 * ���ַ�ʽ�����Ͻǣ� ������䣬ƽ��
	 * ֵΪ�� BackGraphConfig.MODE_???
	 * @return �����ʽ
	 */
	public byte getMode() {
		return mode;
	}

	/**
	 * ���������ʽ
	 * @param m ��ʽ
	 */
	public void setMode(byte m) {
		mode = m;
	}

	/**
	 * ��ȡˮӡ�ı�ƽ��ʱ���ı����
	 * @return ���ֵ
	 */
	public int getTextGap() {
		return textGap;
	}

	/**
	 * ����ˮӡ�ı���ƽ��ʱ���ı����
	 * @param g ���ֵ
	 */
	public void setTextGap(int g) {
		textGap = g;
	}

	/**
	 * ����ˮӡ�ı���͸���ȣ�ֵΪ0��100
	 * @param tran ͸��ֵ
	 */
	public void setTransparency(int tran) {
		textTransparency = tran;
	}

	/**
	 * ��ȡˮӡ�ı���͸����
	 * @return ͸��ֵ
	 */
	public int getTransparency() {
		return textTransparency;
	}

	/**
	 * ��ȡ����ͼ��������Դ
	 * ��������Դ����ͼ�����ɣ��Լ�ˮӡ��������
	 * ֵΪ�� BackGraphConfig.SOURCE_???
	 * @return ��Դ��ʽ
	 */
	public byte getImageSource() {
		return imgSource;
	}

	/**
	 * ���ñ���ͼ����Դ��ʽ
	 * @param src ��ʽֵ
	 */
	public void setImageSource(byte src) {
		imgSource = src;
	}

	/**
	 * ��ȡˮӡ���ֵ���������
	 * @return ������
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * ����ˮӡ���ֵ�������
	 * @param fn ������
	 */
	public void setFontName(String fn) {
		fontName = fn;
	}

	/**
	 * ��ȡˮӡ���ֵ��ֺ�
	 * @return �ֺ�
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * ����ˮӡ���ֵ��ֺ�
	 * @param size �ֺ�
	 */
	public void setFontSize(int size) {
		fontSize = size;
	}

	/**
	 * ��ȡˮӡ���ֵ���ɫֵ
	 * @return ��ɫֵ
	 */
	public int getTextColor() {
		return textColor;
	}

	/**
	 * ����ˮӡ���ֵ���ɫ
	 * @param c ��ɫֵ
	 */
	public void setTextColor(int c) {
		textColor = c;
	}

	/**
	 * ���л��������
	 * 
	 * @param out ���������
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(3); // version
		out.writeByte(type);
		out.writeObject(value);
		out.writeByte(disp);
		out.writeObject(imageBytes);
		// �汾2�� ���Ӳ���ģʽ
		out.writeByte(imgSource);
		out.writeByte(mode);
		out.writeObject(fontName);
		out.writeInt(fontSize);
		out.writeInt(textColor);
		out.writeInt(textGap);
		out.writeInt(textTransparency);
	}

	/**
	 * ���л����뱾��
	 * 
	 * @param in ����������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		byte version = in.readByte();
		type = in.readByte();
		value = (String) in.readObject();
		disp = in.readByte();
		imageBytes = (byte[]) in.readObject();
		if (version > 1) {
			imgSource = in.readByte();
			mode = in.readByte();
			fontName = (String) in.readObject();
			fontSize = in.readInt();
			textColor = in.readInt();
			textGap = in.readInt();
		}
		if (version > 2) {
			textTransparency = in.readInt();
		}
	}

	/**
	 * ���л��������
	 * 
	 * @throws IOException
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeByte(type);
		out.writeString(value);
		out.writeByte(disp);
		out.writeBytes(imageBytes);

		out.writeByte(imgSource);
		out.writeByte(mode);
		out.writeString(fontName);
		out.writeInt(fontSize);
		out.writeInt(textColor);
		out.writeInt(textGap);
		out.writeInt(textTransparency);

		return out.toByteArray();
	}

	/**
	 * ���л����뱾��
	 * 
	 * @param in
	 *            byte[]input
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void fillRecord(byte[] buf) throws IOException,
			ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		type = in.readByte();
		value = in.readString();
		disp = in.readByte();
		imageBytes = in.readBytes();
		if (in.available() > 0) {
			imgSource = in.readByte();
			mode = in.readByte();
			fontName = in.readString();
			fontSize = in.readInt();
			textColor = in.readInt();
			textGap = in.readInt();
		}
		if (in.available() > 0) {
			textTransparency = in.readInt();
		}
	}

	/**
	 * ��¡����
	 * 
	 * @return Object
	 */
	public Object deepClone() {
		BackGraphConfig bgc = new BackGraphConfig();
		bgc.setType(type);
		bgc.setValue(value);
		bgc.setDispMode(disp);
		bgc.setImageBytes(imageBytes);

		bgc.setImageSource(imgSource);
		bgc.setMode(mode);
		bgc.setFontName(fontName);
		bgc.setFontSize(fontSize);
		bgc.setTextColor(textColor);
		bgc.setTextGap(textGap);
		bgc.setTransparency(textTransparency);

		return bgc;
	}

	/**
	 * ����ͼ��͸����
	 * @param g ͼ���豸
	 * @param transparent ͸��ͼ����Χ0��100�� 100ʱ��͸����0Ϊȫ͸��
	 * @return �ɵ�Composite�����ڵ��ø÷�����Ļָ�
	 */
	public static Composite setTransparent(Graphics2D g, float transparent) {
		if (transparent >= 1) {
			return null;
		} else if (transparent < 0) {
			transparent = 0f;
		}
		Composite old = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				transparent));
		return old;
	}

	/**
	 * ��ȡ�ı����Ƶ�ռ�ÿռ�
	 * @param textFont ��������
	 * @param textSize ���ִ�С
	 * @param text �ı�ֵ
	 * @return ����������ı��ռ�
	 */
	public static Rectangle getTextRect(String textFont, int textSize,
			String text) {
		Font font = new Font(textFont, Font.PLAIN, textSize);
		Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
				.createGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		int txtHeight = fm.getHeight();
		g.dispose();
		return new Rectangle(0, 0, fm.stringWidth(text), txtHeight);
	}

	/**
	 * �����������
	 * @param fontName ��������
	 * @param fontStyle �ı����
	 * @param fontSize �ֺ�
	 * @return �������
	 */
	public synchronized static Font getFont(String fontName, int fontStyle,
			int fontSize) {
		if (fontName == null || fontName.trim().length() < 1) {
			fontName = "dialog";
		}
		
		Font f = new Font(fontName, fontStyle, fontSize);
		return f;
	}

	/**
	 * ��ȡ�ı���ˮƽ�������ʱ�Ŀռ�
	 * @param text �ı�ֵ
	 * @param g ͼ���豸
	 * @param font ��
	 * @return ռ�ÿռ�
	 */
	public static Rectangle getHorizonArea(String text, java.awt.Graphics g,
			Font font) {
		Rectangle area = new Rectangle();
		FontMetrics fm = g.getFontMetrics(font);
		int hw = fm.stringWidth(text);
		int hh = fm.getAscent() + fm.getDescent(); // .getAscent();
		area.width = hw;
		area.height = hh;
		return area;
	}

	/**
	 * ��ȡ�ı���ת������ռ�ÿռ�
	 * @param text �ı�
	 * @param g ͼ���豸
	 * @param angle ��ת�Ƕ�
	 * @param font ����
	 * @return ռ�ÿռ�
	 */
	public static Rectangle getRotationArea(String text, java.awt.Graphics g,
			int angle, Font font) {
		Rectangle area = new Rectangle();
		angle = angle % 360;
		if (angle < 0) {
			angle += 360;
		}
		Rectangle area0 = getTextSize(text, g, 0, font);
		double sin = Math.sin(angle * Math.PI / 180);
		double cos = Math.cos(angle * Math.PI / 180);
		if (sin < 0) {
			sin = -sin;
		}
		if (cos < 0) {
			cos = -cos;
		}
		int aw = (int) (area0.height * sin + area0.width * cos);
		int ah = (int) (area0.width * sin + area0.height * cos);
		area.width = aw;
		area.height = ah;
		return area;
	}

	/**
	 * �Զ�������ת�Ƕȣ�������ط��������ı���ռ�ÿռ�
	 * @param text �ı�
	 * @param g ͼ���豸
	 * @param angle ��ת�Ƕ�
	 * @param font ����
	 * @return ռ�ÿռ�
	 */
	public static Rectangle getTextSize(String text, java.awt.Graphics g,
			int angle, Font font) {
		if (text == null) {
			return new Rectangle();
		}
		Rectangle rect = null;
		if (angle == 0) {
			rect = getHorizonArea(text, g, font);
		} else {
			rect = getRotationArea(text, g, angle, font);
		}
		if (rect.width < 0) {
			rect.width = -rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
		}
		return rect;
	}

	/**
	 * isImageʱ��Ҫ�任Ϊ���Ͻǣ� �ı�ʱ�任Ϊ���½ǣ���Ϊg�����½ǻ����ı��� ���Ͻǻ���ͼ�Ρ�
	 * @param posDesc
	 * @return
	 */
	public static Point getRealDrawPoint(Rectangle posDesc) {
		Rectangle rect = posDesc;
		// ��ͼ���ĵ�
		int xloc = rect.x;
		int yloc = rect.y;

		yloc += rect.height / 2;
		// �����ο������м䣬��Ҫ�����½�x����
		xloc -= rect.width / 2;
		return new Point(xloc, yloc);
	}

//	protected static void setGraphAntiAliasingOff(Graphics2D g) {
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_OFF);
//	}
//
//	protected static void setGraphAntiAliasingOn(Graphics2D g) {
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//	}

	/**
	 * ��ָ��λ�ô�����ı�
	 * @param txt �ı�
	 * @param dx ������
	 * @param dy ������
	 * @param font ����
	 * @param c ��ɫ
	 * @param angle ��ת�Ƕ�
	 * @param g ͼ���豸
	 */
	public static void drawText(String txt, double dx, double dy, Font font,
			Color c, int angle, Graphics2D g) {
		if (txt == null || txt.trim().length() < 1 || font.getSize() == 0) {
			return;
		}
		int x = (int) dx;
		int y = (int) dy;

		// ���ֲ��ص�
		Rectangle rect = getTextSize(txt, g, angle, font);
		rect.x = x;
		rect.y = y;

		g.setFont(font);
		g.setColor(c);

		Point drawPoint = getRealDrawPoint(rect);
		int xloc = drawPoint.x;
		int yloc = drawPoint.y;

		Utils.setGraphAntiAliasingOff(g);

		// ����������
		if (angle != 0) {
			AffineTransform at = g.getTransform();
			Rectangle rect2 = getTextSize(txt, g, 0, font);
			rect2.setLocation(xloc, yloc - rect2.height);
			int delx = 0, dely = 0;
			angle = angle % 360;
			if (angle < 0) {
				angle += 360;
			}
			if (angle >= 0 && angle < 90) {
				delx = 0;
				dely = (int) (rect2.width * Math.sin(angle * Math.PI / 180));
			} else if (angle < 180) {
				dely = rect.height;
				delx = (int) (rect2.width * Math.cos(angle * Math.PI / 180));
			} else if (angle < 270) {
				delx = -rect.width;
				dely = (int) (-rect2.height * Math.sin(angle * Math.PI / 180));
			} else {
				dely = 0;
				delx = (int) (rect2.height * Math.sin(angle * Math.PI / 180));
			}
			AffineTransform at1 = AffineTransform.getRotateInstance(angle
					* Math.PI / 180, xloc - delx, yloc - dely);
			g.transform(at1);

			g.setColor(c);
			g.drawString(txt, xloc - delx, yloc - dely);

			g.setTransform(at);
		} else {
			g.setColor(c);
			g.drawString(txt, xloc, yloc);
		}
		Utils.setGraphAntiAliasingOn(g);
	}

}
