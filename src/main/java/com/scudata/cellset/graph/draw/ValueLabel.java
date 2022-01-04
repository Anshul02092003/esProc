package com.scudata.cellset.graph.draw;

import java.awt.*;
import java.awt.geom.Point2D;
/**
 * �ı���ǩ�ķ�װ��
 * ����ͼ��ʱ���ı�ֵ��Ҫ�����ͳһ�����Է�ֹ������
 * @author Joancy
 *
 */
public class ValueLabel {
  public String text;
  public Point2D.Double p;
  public Color c;
  public byte direction=GraphFontView.TEXT_ON_TOP;

  /**
   * ����һ���ı���ǩ����
   * @param text �ı�ֵ
   * @param p ����
   * @param c ��ɫ
   */
  public ValueLabel(String text, Point2D.Double p, Color c) {
	this.text = text;
	this.p = p;
	this.c = c;
  }

  /**
   * ����һ���ı���ǩ����
   * @param text �ı�ֵ
   * @param p ����
   * @param c ��ɫ
   * @param textDirection ��λ
   */
  public ValueLabel(String text, Point2D.Double p, Color c,byte textDirection) {
	this.text = text;
	this.p = p;
	this.c = c;
	this.direction = textDirection;
  }
}
