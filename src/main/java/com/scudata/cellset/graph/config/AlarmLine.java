package com.scudata.cellset.graph.config;

import java.io.*;

import com.scudata.common.*;

import java.awt.*;

/**
 * ͳ��ͼ�еľ�����������
 */
public class AlarmLine implements ICloneable, Externalizable, IRecord {
	private final static long serialVersionUID = 82857881736578L;
	private byte version = (byte) 3;
	// �汾3������ ��ʶ����ֵ

	/** �����������ơ� */
	private String name;
	/** ������ֵ�� */
	private String alarmValue;
	/** �����������͡� */
	private byte lineType = IGraphProperty.LINE_SOLID;
	/** ��������ɫ */
	private int color = Color.red.getRGB();

	private byte lineThick = 1; /* ����ͼ�Ĵ�ϸ�� */
	private boolean isDrawAlarmValue = true; /* ��ʶ����ֵ */

	/**
	 * ��������
	 * 
	 * @param name
	 *            ����
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ���þ���ֵ
	 * 
	 * @param value
	 *            ����ֵ
	 */
	public void setAlarmValue(String value) {
		this.alarmValue = value;
	}

	/**
	 * �����Ƿ���ƾ���ֵ
	 * 
	 * @param isDrawAlarmValue
	 *            �Ƿ����
	 */
	public void setDrawAlarmValue(boolean isDrawAlarmValue) {
		this.isDrawAlarmValue = isDrawAlarmValue;
	}

	/**
	 * ȡ�Ƿ���ƾ���ֵ
	 * 
	 * @return ���Ʒ���true�����򷵻�false
	 */
	public boolean isDrawAlarmValue() {
		return isDrawAlarmValue;
	}

	/**
	 * ���þ���������
	 * 
	 * @param type
	 *            ���������ͣ�ȡֵΪGraphProperty.LINE_NONE, LINE_SOLID, LINE_LONG_DASH,
	 *            LINE_SHORT_DASH, LINE_DOT_DASH, LINE_2DOT_DASH
	 */
	public void setLineType(byte type) {
		this.lineType = type;
	}

	/**
	 * ������ɫ
	 * 
	 * @param color
	 *            ��ɫֵ
	 */
	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * ȡ����
	 * 
	 * @return String������
	 */
	public String getName() {
		return name;
	}

	/**
	 * ȡ����ֵ
	 * 
	 * @return String������ֵ
	 */
	public String getAlarmValue() {
		return alarmValue;
	}

	/**
	 * ȡ����������
	 * 
	 * @return byte�����������ͣ�ֵΪGraphProperty.LINE_NONE, LINE_SOLID, LINE_LONG_DASH,
	 *         LINE_SHORT_DASH, LINE_DOT_DASH, LINE_2DOT_DASH
	 */
	public byte getLineType() {
		return lineType;
	}

	/**
	 * ȡ��ɫ
	 * 
	 * @return int����ɫ
	 */
	public int getColor() {
		return color;
	}

	/**
	 * �����ߴ�ϸ��
	 * 
	 * @return byte �ֶ�
	 */
	public byte getLineThick() {
		return lineThick;
	}

	/**
	 * �����ߵĴֶ�
	 * 
	 * @param thick
	 *            �ֶ�ֵ
	 */
	public void setLineThick(byte thick) {
		if (thick < 0 || thick > 10) {
			thick = 10;
		}
		lineThick = thick;
	}

	/**
	 * ��ȿ�¡
	 * 
	 * @return Object ��¡�ľ����߶���
	 */
	public Object deepClone() {
		AlarmLine line = new AlarmLine();
		line.setName(name);
		line.setAlarmValue(alarmValue);
		line.setColor(color);
		line.setLineType(lineType);
		line.setLineThick(lineThick);
		line.setDrawAlarmValue(isDrawAlarmValue);
		return line;
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(version);
		out.writeObject(name);
		out.writeObject(alarmValue);
		out.writeByte(lineType);
		out.writeInt(color);
		out.writeByte(lineThick);
		out.writeBoolean(isDrawAlarmValue);
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		byte ver = in.readByte();
		name = (String) in.readObject();
		alarmValue = (String) in.readObject();
		lineType = in.readByte();
		color = in.readInt();
		if (ver > 1) {
			lineThick = in.readByte();
		}
		if (ver > 2) {
			isDrawAlarmValue = in.readBoolean();
		}
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeString(name);
		out.writeString(alarmValue);
		out.writeByte(lineType);
		out.writeInt(color);
		out.writeByte(lineThick);
		out.writeBoolean(isDrawAlarmValue);
		return out.toByteArray();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public void fillRecord(byte[] buf) throws IOException,
			ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		name = in.readString();
		alarmValue = in.readString();
		lineType = in.readByte();
		color = in.readInt();
		if (in.available() > 0) {
			lineThick = in.readByte();
		}
		if (in.available() > 0) {
			isDrawAlarmValue = in.readBoolean();
		}
	}

}
