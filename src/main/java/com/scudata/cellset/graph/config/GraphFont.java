package com.scudata.cellset.graph.config;

import java.io.*;

import com.scudata.common.*;

import java.awt.*;

/**
 *  ͳ��ͼ�е������ඨ��
 * @author Joancy
 *
 */
public class GraphFont implements ICloneable, Externalizable, IRecord {
	private final static long serialVersionUID = 82857881736578L;
	private byte version = ( byte ) 1;

	/** �������� */
	private String family;
	/** �����С */
	private int size = 12;
	/** �Ƿ��Զ�������С */
	private boolean autoResize = false;
	/** �Ƿ���� */
	private boolean bold = false;
	/** ������ɫ */
	private int color = Color.black.getRGB();
	/** �Ƿ��������� */
	private boolean verticalText = false;
	/** ��ת�Ƕ� */
	private int angle;

	/**
	 * ������������
	 * @param family ��������
	 */
	public void setFamily( String family ) {
		this.family = family;
	}

	/**
	 * ���������С
	 * @param size �����С
	 */
	public void setSize( int size ) {
		this.size = size;
	}

	/**
	 * �����Ƿ��Զ����������С
	 * @param b �Ƿ��Զ�����
	 */
	public void setAutoResize( boolean b ) {
		this.autoResize = b;
	}

	/**
	 * �����Ƿ����
	 * @param b �Ƿ����
	 */
	public void setBold( boolean b ) {
		this.bold = b;
	}

	/**
	 * ����������ɫ
	 * @param c ��ɫֵ
	 */
	public void setColor( int c ) {
		this.color = c;
	}

	/**
	 * �����Ƿ���������
	 * @param b �Ƿ���������
	 */
	public void setVerticalText( boolean b ) {
		this.verticalText = b;
	}

	/**
	 * ������ת�Ƕ�
	 * @param angle �Ƕ�ֵ
	 */
	public void setAngle( int angle ) {
		this.angle = angle;
	}

	/**
	 * ȡ��������
	 * @return String����������
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * ȡ�����С
	 * @return int�������С
	 */
	public int getSize() {
		return size;
	}

	/**
	 * �Ƿ��Զ�������С
	 * @return boolean �Զ�����ʱ����true�����򷵻�false
	 */
	public boolean isAutoResize() {
		return autoResize;
	}

	/**
	 * �Ƿ����
	 * @return boolean ���巵��true�����򷵻�false
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * ȡ������ɫ
	 * @return int��������ɫ
	 */
	public int getColor() {
		return color;
	}

	/**
	 * �Ƿ���������
	 * @return boolean �������ַ���true�����򷵻�false
	 */
	public boolean isVerticalText() {
		return verticalText;
	}

	/**
	 * ȡ��ת�Ƕ�
	 * @return int����ת�Ƕ�
	 */
	public int getAngle() {
		return angle;
	}

	/**
	 * ��ȿ�¡
	 * @return Object ��¡����������
	 */
	public Object deepClone() {
		GraphFont font = new GraphFont();
		font.setFamily( family );
		font.setSize( size );
		font.setColor( color );
		font.setBold( bold );
		font.setAngle( angle );
		font.setAutoResize( autoResize );
		font.setVerticalText( verticalText );
		return font;
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void writeExternal( ObjectOutput out ) throws IOException{
		out.writeByte( version );
		out.writeObject( family );
		out.writeInt( size );
		out.writeInt( color );
		out.writeBoolean( bold );
		out.writeInt( angle );
		out.writeBoolean( autoResize );
		out.writeBoolean( verticalText );
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException{
		byte ver = in.readByte();
		family = ( String ) in.readObject();
		size = in.readInt();
		color = in.readInt();
		bold = in.readBoolean();
		angle = in.readInt();
		autoResize = in.readBoolean();
		verticalText = in.readBoolean();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public byte[] serialize() throws IOException{
	  ByteArrayOutputRecord out = new ByteArrayOutputRecord();
	  out.writeString( family );
	  out.writeInt( size );
	  out.writeInt( color );
	  out.writeBoolean( bold );
	  out.writeInt( angle );
	  out.writeBoolean( autoResize );
	  out.writeBoolean( verticalText );
	  return out.toByteArray();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
	  ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
	  family = in.readString();
	  size = in.readInt();
	  color = in.readInt();
	  bold = in.readBoolean();
	  angle = in.readInt();
	  autoResize = in.readBoolean();
	  verticalText = in.readBoolean();
	}

}
