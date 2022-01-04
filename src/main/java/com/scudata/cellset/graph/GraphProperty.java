package com.scudata.cellset.graph;

import java.io.*;

import com.scudata.common.*;

/**
 * ��������֮�����������
 * @author Joancy
 *
 */
public class GraphProperty extends PublicProperty {
	private final static long serialVersionUID = 82857881736578L;
	/** ���ඨ�� */
	private String category; // ���൥Ԫ��Դ��
	private String series; // ϵ�е�Ԫ�񣬿ɿգ�Դ��
	private String value; // ֵ��Ԫ��Դ��

	private int x, y, w = 400, h = 260;

	/**
	 * ȡͳ��ͼ���ඨ��
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * ֵ��Ԫ��
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * ��ȡϵ�б��ʽ
	 * @return ϵ�б��ʽ 
	 */
	public String getSeries() {
		return series;
	}

	/**
	 * x����
	 * @return ����ֵ
	 */
	public int getX() {
		return x;
	}

	/**
	 * y����
	 * @return ����ֵ
	 */
	public int getY() {
		return y;
	}

	/**
	 * ͼ�ο��
	 * @return ���ֵ
	 */
	public int getW() {
		return w;
	}

	/**
	 * ͼ�θ߶�
	 * @return �߶�ֵ
	 */
	public int getH() {
		return h;
	}

	/**
	 * ����ͳ��ͼ���ඨ��
	 * 
	 * @param categories
	 *            ͳ��ͼ���ඨ��
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * ֵ���ʽ
	 * @param value ���ʽ
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * ����ϵ�б��ʽ
	 * @param series ���ʽ
	 */
	public void setSeries(String series) {
		this.series = series;
	}

	/**
	 * ���ú�����
	 * @param x ����ֵ
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * ����������
	 * @param y ����ֵ
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * ����ͼ�ο��
	 * @param w ���ֵ
	 */
	public void setW(int w) {
		this.w = w;
	}

	/**
	 * ����ͼ�θ߶�
	 * @param h �߶�ֵ
	 */
	public void setH(int h) {
		this.h = h;
	}

	/**
	 * ��������λ��
	 * @param x ������
	 * @param y ������
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * ���ô�С
	 * @param w ���
	 * @param h �߶�
	 */
	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
	}

	/**
	 * ��ȿ�¡
	 * 
	 * @return Object ��¡���ͼ������
	 */
	public Object deepClone() {
		GraphProperty gp = new GraphProperty();
		gp.setCategory(category);
		gp.setValue(value);
		gp.setSeries(series);
		gp.setLocation(getX(),getY());
		gp.setSize(getW(), getH());
		gp.setPublicProperty(this);
		return gp;
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		byte ver = 1;
		out.writeByte(ver);
		out.writeObject(category);
		out.writeObject(value);
		out.writeObject(series);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(w);
		out.writeInt(h);
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		byte ver = in.readByte();
		category = (String) in.readObject();
		value = (String) in.readObject();
		series = (String) in.readObject();

		x = in.readInt();
		y = in.readInt();
		w = in.readInt();
		h = in.readInt();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeBytes(super.serialize());
		
		out.writeString(category);
		out.writeString(value);
		out.writeString(series);

		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(w);
		out.writeInt(h);
		return out.toByteArray();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public void fillRecord(byte[] buf) throws IOException,
			ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		byte[] superRecord = in.readBytes();
		super.fillRecord( superRecord );
		
		category = in.readString();
		value = in.readString();
		series = in.readString();

		x = in.readInt();
		y = in.readInt();
		w = in.readInt();
		h = in.readInt();
	}

}
