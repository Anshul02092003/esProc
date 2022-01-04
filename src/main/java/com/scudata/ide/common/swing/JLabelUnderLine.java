package com.scudata.ide.common.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * ֧���»��ߵı�ǩ�ؼ�
 *
 */
public class JLabelUnderLine extends JLabel {
	private static final long serialVersionUID = 1L;

	/**
	 * �»�����ɫ
	 */
	private Color underLineColor = Color.BLUE;
	/**
	 * ��ʾֵ
	 */
	private Object value;

	/**
	 * ���캯��
	 */
	public JLabelUnderLine() {
		super("");
		setBackground(new JTextField().getBackground());
		setBorder(null);
	}

	/**
	 * ������ʾֵ
	 * 
	 * @param value
	 *            ��ʾֵ
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * ȡ�»�����ɫ
	 * 
	 * @return
	 */
	public Color getUnderLineColor() {
		return underLineColor;
	}

	/**
	 * �����»�����ɫ
	 * 
	 * @param pUnderLineColor
	 */
	public void setUnderLineColor(Color pUnderLineColor) {
		underLineColor = pUnderLineColor;
	}

	/**
	 * ����
	 */
	public void paint(Graphics g) {
		super.paint(g);

		if (null == value || !(value instanceof String) || "".equals(value))
			return;

		FontMetrics fm = getFontMetrics(getFont());
		Rectangle r = g.getClipBounds();
		int xoffset = 0, yoffset = 0, pointX = 0, pointY = 0, point2X = 0, point2Y = 0;
		int xoffset1 = getWidth();
		int topGap = 0;
		Insets inserts = getInsets();
		if (inserts != null) {
			xoffset = inserts.left;
			yoffset = inserts.bottom;
			xoffset1 -= inserts.right;
			topGap = inserts.top;
		}

		if (null != this.getBorder()
				&& null != this.getBorder().getBorderInsets(this)) {
			inserts = this.getBorder().getBorderInsets(this);
			xoffset = inserts.left;
			yoffset = inserts.bottom;
			xoffset1 -= inserts.right;
			topGap = inserts.top;
		}
		pointY = point2Y = r.height - yoffset - fm.getDescent();
		if (pointY < topGap + fm.getHeight() - fm.getDescent()) {
			return;
		}
		final int GAP = getFontMetrics(getFont()).stringWidth(" ");
		String dispText = getText();
		String codeText = value.toString();
		if (dispText.length() == codeText.length()) {
			dispText = " " + dispText;
		}
		int stringWidth = getFontMetrics(getFont()).stringWidth(dispText) - GAP;
		int halign = this.getHorizontalAlignment();
		// ������Ǵ����Ե������Ϊ�Ӹ����
		switch (halign) {
		case JLabel.LEFT:
			pointX = xoffset + GAP;
			break;
		case JLabel.RIGHT:
			pointX = xoffset1 - stringWidth - GAP;
			break;
		case JLabel.CENTER:
			pointX = (getWidth() - stringWidth) / 2;
			break;
		default:
			return;
		}
		point2X = pointX + stringWidth;

		if (null != underLineColor) {
			g.setColor(underLineColor);
		}

		g.drawLine(pointX, pointY, point2X, point2Y);
	}

}