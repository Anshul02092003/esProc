package com.scudata.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * �����࣬��ʾһ������
 */
public class Area implements ICloneable, Externalizable, Cloneable,
		Comparable<Area>, IRecord {
	private static final long serialVersionUID = 1l;
	/** ��������short�ĳ���int */
	private static byte version = (byte) 2;

	/** ��ʼ�кţ������к� */
	private int r1 = -1, r2 = -1;
	/** ��ʼ�кţ������к� */
	private int c1 = -1, c2 = -1;

	public Area() {
	}

	/**
	 * ���캯��
	 * 
	 * @param r1
	 *            int ��ʼ�к�
	 * @param r2
	 *            int �����к�
	 */
	public Area(int r1, int r2) {
		this.r1 = r1;
		this.r2 = r2;
	}

	/**
	 * ���캯��
	 * 
	 * @param r1
	 *            int ��ʼ�к�
	 * @param c1
	 *            int ��ʼ�к�
	 * @param r2
	 *            int �����к�
	 * @param c2
	 *            int �����к�
	 */
	public Area(int r1, int c1, int r2, int c2) {
		this.r1 = r1;
		this.r2 = r2;

		this.c1 = c1;
		this.c2 = c2;
	}

	/**
	 * ȡ����ʼ�к�
	 * 
	 * @return int
	 */
	public int getBeginRow() {
		return r1;
	}

	/**
	 * ������ʼ�к�
	 * 
	 * @param r
	 *            int �к�
	 */
	public void setBeginRow(int r) {
		this.r1 = r;
	}

	/**
	 * �õ������к�
	 * 
	 * @return int
	 */
	public int getEndRow() {
		return r2;
	}

	/**
	 * ���ý����к�
	 * 
	 * @param r
	 *            int
	 */
	public void setEndRow(int r) {
		this.r2 = r;
	}

	/**
	 * ȡ����ʼ�к�
	 * 
	 * @return int
	 */
	public int getBeginCol() {
		return this.c1;
	}

	/**
	 * ������ʼ�к�
	 * 
	 * @param c
	 *            int
	 */
	public void setBeginCol(int c) {
		this.c1 = c;
	}

	/**
	 * ȡ�ý����к�
	 * 
	 * @return int
	 */
	public int getEndCol() {
		return this.c2;
	}

	/**
	 * ���ý����к�
	 * 
	 * @param c
	 *            int
	 */
	public void setEndCol(int c) {
		this.c2 = c;
	}

	public void setArea(int br, int bc, int er, int ec) {
		this.r1 = br;
		this.r2 = er;
		this.c1 = bc;
		this.c2 = ec;
	}

	/**
	 * ����һ�����������бȽϣ����αȽ���ʼ�С���ʼ�С������С������У� ���ȽϹ����д��ڲ�����򷵻������򷵻�0
	 * 
	 * @param other
	 *            Area
	 * @return int
	 */
	public int compareTo(Area other) {
		int x = r1 - other.r1;
		if (x != 0) {
			return x;
		}
		x = c1 - other.c1;
		if (x != 0) {
			return x;
		}
		x = r2 - other.r2;
		if (x != 0) {
			return x;
		}
		return c2 - other.c2;
	}

	/**
	 * �ж�row,col�Ƿ��ڵ�ǰ������
	 * 
	 * @param row
	 *            int
	 * @param col
	 *            int
	 * @return boolean
	 */
	public boolean contains(int row, int col) {
		return (r1 <= row && r2 >= row && c1 <= col && c2 >= col);
	}

	/**
	 * �Ƿ������һ����
	 * 
	 * @param a
	 *            Area
	 * @return boolean
	 */
	public boolean contains(Area a) {
		return contains(a.getBeginRow(), a.getBeginCol())
				&& contains(a.getEndRow(), a.getEndCol())
				&& contains(a.getBeginRow(), a.getEndCol())
				&& contains(a.getEndRow(), a.getBeginCol());
	}

	/**
	 * ���л��������
	 * 
	 * @param out
	 *            ObjectOutput
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(version);
		out.writeInt(r1);
		out.writeInt(r2);
		out.writeInt(c1);
		out.writeInt(c2);
	}

	/**
	 * ���л����뱾��
	 * 
	 * @param in
	 *            ObjectInput
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		in.readByte();
		r1 = in.readInt();
		r2 = in.readInt();
		c1 = in.readInt();
		c2 = in.readInt();
	}

	/**
	 * ���л��������
	 * 
	 * @return ObjectOutput
	 * @throws IOException
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeInt(r1);
		out.writeInt(r2);
		out.writeInt(c1);
		out.writeInt(c2);
		return out.toByteArray();
	}

	/**
	 * ���л����뱾��
	 * 
	 * @param buf
	 *            byte[]
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void fillRecord(byte[] buf) throws IOException,
			ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		r1 = in.readInt();
		r2 = in.readInt();
		c1 = in.readInt();
		c2 = in.readInt();
	}

	/**
	 * ��¡����
	 * 
	 * @return Object
	 */
	public Object deepClone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Error when clone Area");
		}
	}
}
