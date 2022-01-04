package com.scudata.cellset.graph.draw;

import java.awt.Color;

import com.scudata.cellset.graph.*;

/**
 * ͳ��ͼ�еľ�����������
 */
public class ExtAlarmLine {
	/**�����������ơ�*/
	private String name = null;
	/**������ֵ��*/
	private double value = 0;
	/**�����������͡�*/
	private byte lineType = GraphProperty.LINE_SOLID;
	/**  ��������ɫ */
	private int color = Color.red.getRGB();
	/* ֱ�ߵĴֶ� */
	private float lineThick;
	private boolean isDrawAlarmValue = true;

	/**
	 * ȡ����
	 * @return String������
	 */
	public String getName() {
		return name;
	}

	/**
	 * ȡ����ֵ
	 * @return String������ֵ
	 */
	public double getAlarmValue() {
		return value;
	}

	/**
	 * ȡ����������
	 * @return byte�����������ͣ�ֵΪGraphProperty.LINE_NONE, LINE_SOLID, LINE_LONG_DASH, LINE_SHORT_DASH, LINE_DOT_DASH, LINE_2DOT_DASH
	 */
	public byte getLineType() {
		return lineType;
	}

	/**
	 * ȡ��ɫ
	 * @return int����ɫ
	 */
	public int getColor() {
		return color;
	}

	/**
	 * ��������
	 * @param name������
	 */
	public void setName(String name) {
		this.name= name;
	}

	/**
	 * ���þ���ֵ
	 * @param value������ֵ
	 */
	public void setAlarmValue(double value) {
		this.value= value;
	}

	/**
	 * ���þ���������
	 * @param type�����������ͣ�ֵΪGraphProperty.LINE_NONE, LINE_SOLID, LINE_LONG_DASH, LINE_SHORT_DASH, LINE_DOT_DASH, LINE_2DOT_DASH
	 */
	public void setLineType(byte type) {
		lineType=type;
	}

	/**
	 * ������ɫ
	 * @param color����ɫ
	 */
	public void setColor(int color) {
		this.color= color;
	}

	/**
	 * ���ôֶ�
	 * @param thick �ֶ�
	 */
	public void setLineThick( float thick ){
	  this.lineThick = thick;
	}
	/**
	 * ȡ�ߴֶ�
	 * @return �ֶ�
	 */
	public float getLineThick(){
	  return lineThick;
	}

	/**
	 * �����Ƿ���ƾ���ֵ
	 * @param isDrawAlarmValue �Ƿ���ƾ���ֵ
	 */
	public void setDrawAlarmValue( boolean isDrawAlarmValue ){
		  this.isDrawAlarmValue = isDrawAlarmValue;
	}
	/**
	 * ȡ�Ƿ���ƾ���ֵ
	 * @return ���Ʒ���true������false
	 */
	public boolean isDrawAlarmValue(){
	  return isDrawAlarmValue;
	}

}
