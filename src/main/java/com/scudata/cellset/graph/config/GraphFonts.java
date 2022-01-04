package com.scudata.cellset.graph.config;

import java.io.*;

import com.scudata.common.*;

/**
 * ��װͳ��ͼ�õ���������������
 * 
 * @author Joancy
 *
 */
public class GraphFonts implements ICloneable, Externalizable, IRecord {
	private final static long serialVersionUID = 82857881736578L;
	private byte version = ( byte ) 1;

	/** ͳ��ͼ�������� */
	private GraphFont titleFont;
	/** ͳ��ͼ����������� */
	private GraphFont xTitleFont;
	/** ͳ��ͼ����������� */
	private GraphFont yTitleFont;
	/** ͳ��ͼ�����ǩ���� */
	private GraphFont xLabelFont;
	/** ͳ��ͼ�����ǩ���� */
	private GraphFont yLabelFont;
	/** ͳ��ͼͼ������ */
	private GraphFont legendFont;
	/** ͳ��ͼͼ����ʾ�������� */
	private GraphFont dataFont;

	/**
	 * ȱʡֵ���캯��
	 */
	public GraphFonts() {
		titleFont = new GraphFont();
		xTitleFont = new GraphFont();
		yTitleFont = new GraphFont();
		xLabelFont = new GraphFont();
		yLabelFont = new GraphFont();
		legendFont = new GraphFont();
		dataFont = new GraphFont();
	}

	/**
	 * ȡ��������
	 * @return GraphFont����������
	 */
	public GraphFont getTitleFont() {
		return titleFont;
	}

	/**
	 * ���ñ�������
	 * @param font ��������
	 */
	public void setTitleFont( GraphFont font ) {
		this.titleFont = font;
	}

	/**
	 * ȡ�����������
	 * @return GraphFont�������������
	 */
	public GraphFont getXTitleFont() {
		return xTitleFont;
	}

	/**
	 * ���ú����������
	 * @param font �����������
	 */
	public void setXTitleFont( GraphFont font ) {
		this.xTitleFont = font;
	}

	/**
	 * ȡ�����������
	 * @return GraphFont�������������
	 */
	public GraphFont getYTitleFont() {
		return yTitleFont;
	}

	/**
	 * ���������������
	 * @param font �����������
	 */
	public void setYTitleFont( GraphFont font ) {
		this.yTitleFont = font;
	}

	/**
	 * ȡ�����ǩ����
	 * @return GraphFont�������ǩ����
	 */
	public GraphFont getXLabelFont() {
		return xLabelFont;
	}

	/**
	 * ���ú����ǩ����
	 * @param font �����ǩ����
	 */
	public void setXLabelFont( GraphFont font ) {
		this.xLabelFont = font;
	}

	/**
	 * ȡ�����ǩ����
	 * @return GraphFont�������ǩ����
	 */
	public GraphFont getYLabelFont() {
		return yLabelFont;
	}

	/**
	 * ���������ǩ����
	 * @param font �����ǩ����
	 */
	public void setYLabelFont( GraphFont font ) {
		this.yLabelFont = font;
	}

	/**
	 * ȡͼ������
	 * @return GraphFont��ͼ������
	 */
	public GraphFont getLegendFont() {
		return legendFont;
	}

	/**
	 * ����ͼ������
	 * @param font ͼ������
	 */
	public void setLegendFont( GraphFont font ) {
		this.legendFont = font;
	}

	/**
	 * ȡͼ����ʾ��������
	 * @return GraphFont��ͼ����ʾ��������
	 */
	public GraphFont getDataFont() {
		return dataFont;
	}

	/**
	 * ����ͼ����ʾ��������
	 * @param font ͼ����ʾ��������
	 */
	public void setDataFont( GraphFont font ) {
		this.dataFont = font;
	}

	/**
	 * ��ȿ�¡
	 * @return Object ��¡������弯��
	 */
	public Object deepClone() {
		GraphFonts fonts = new GraphFonts();
		fonts.setTitleFont( ( GraphFont ) titleFont.deepClone() );
		fonts.setXTitleFont( ( GraphFont ) xTitleFont.deepClone() );
		fonts.setYTitleFont( ( GraphFont ) yTitleFont.deepClone() );
		fonts.setXLabelFont( ( GraphFont ) xLabelFont.deepClone() );
		fonts.setYLabelFont( ( GraphFont ) yLabelFont.deepClone() );
		fonts.setLegendFont( ( GraphFont ) legendFont.deepClone() );
		fonts.setDataFont( ( GraphFont ) dataFont.deepClone() );
		return fonts;
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void writeExternal( ObjectOutput out ) throws IOException{
		out.writeByte( version );
		out.writeObject( titleFont );
		out.writeObject( xTitleFont );
		out.writeObject( yTitleFont );
		out.writeObject( xLabelFont );
		out.writeObject( yLabelFont );
		out.writeObject( legendFont );
		out.writeObject( dataFont );
	}

	/**
	 * ʵ�����л��ӿ�
	 */
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException{
		byte ver = in.readByte();
		titleFont = ( GraphFont ) in.readObject();
		xTitleFont = ( GraphFont ) in.readObject();
		yTitleFont = ( GraphFont ) in.readObject();
		xLabelFont = ( GraphFont ) in.readObject();
		yLabelFont = ( GraphFont ) in.readObject();
		legendFont = ( GraphFont ) in.readObject();
		dataFont = ( GraphFont ) in.readObject();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public byte[] serialize() throws IOException{
	  ByteArrayOutputRecord out = new ByteArrayOutputRecord();
	  out.writeRecord( titleFont );
	  out.writeRecord( xTitleFont );
	  out.writeRecord( yTitleFont );
	  out.writeRecord( xLabelFont );
	  out.writeRecord( yLabelFont );
	  out.writeRecord( legendFont );
	  out.writeRecord( dataFont );
	  return out.toByteArray();
	}

	/**
	 * ʵ��IRecord�ӿ�
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
	  ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
	  titleFont = (GraphFont) in.readRecord(new GraphFont());
	  xTitleFont = (GraphFont) in.readRecord(new GraphFont());
	  yTitleFont = (GraphFont) in.readRecord(new GraphFont());
	  xLabelFont = (GraphFont) in.readRecord(new GraphFont());
	  yLabelFont = (GraphFont) in.readRecord(new GraphFont());
	  legendFont = (GraphFont) in.readRecord(new GraphFont());
	  dataFont = (GraphFont) in.readRecord(new GraphFont());
	}

}
