package com.scudata.dm;

import java.io.*;

import com.scudata.chart.*;
import com.scudata.common.*;

/**
 * ������
 * 
 * @author Joancy
 *
 */
public class Canvas implements ICloneable, Externalizable, IRecord {
  private String name;
  private transient Sequence chartElements = new Sequence();

  private transient String htmlLinks=null;
  
  /**
   * ����һ��ȱʡ����
   */
  public Canvas() {
  }

  /**
   * ��ȡͼ�������html������
   * @return ������
   */
  public String getHtmlLinks(){
	  //�����ȵ��ü���ͼ�Σ���ΪҪ��ȡ��Ӧ��w��h������û�����ʱҪ����
	  if( htmlLinks==null) throw new RQException("You should call G.draw(w,h) before call G.hlink().");
	  return htmlLinks;
  }
  
  /**
   * ���û�������
   * @param name
   */
  public void setName(String name) {
	this.name = name;
  }

  /**
   * ȡ��������
   * @return
   */
  public String getName() {
	return name;
  }

  /**
   * ����һ��ͼԪ
   * @param elem  ͼԪ�����б�ʾ
   */
  public void addChartElement(Sequence elem) {
	chartElements.add(elem);
  }

  /**
   * ��ȡ����ͼԪ
   * @return ͼԪ���б�ʾ������
   */
  public Sequence getChartElements(){
    return chartElements;
  }

  /**
   * ��������ͼԪ����
   * @param elements ͼԪ����
   */
  public void setChartElements(Sequence elements){
    chartElements=elements;
  }

  /**
   * ���ͼԪ
   */
  public void clear() {
	chartElements.clear();
  }

  private byte[] getImageBytes(int w, int h, byte fmt) {
	Engine e = new Engine(this.getChartElements());
	byte[] bytes = e.calcImageBytes(w, h, fmt);
	htmlLinks = e.getHtmlLinks();
	if( htmlLinks==null ) htmlLinks="";//null��ʾû�м��㣬�ձ�ʾ�����ˣ�����û�ж��峬����
	return bytes;
  }

  /**
   * ����������svg��ʽ����
   * @param w ���
   * @param h �߶�
   * @return svgͼ���ֽ�����
   */
  public byte[] toSVG(int w, int h) { //Utf-8
	return getImageBytes(w, h, Consts.IMAGE_SVG);
  }

  /**
   * ����������jpg��ʽ����
   * @param w ���
   * @param h �߶�
   * @return jpgͼ���ֽ�����
   */
  public byte[] toJpg(int w, int h) {
	return getImageBytes(w, h, Consts.IMAGE_JPG);
  }

  /**
   * ����������png��ʽ����
   * @param w ���
   * @param h �߶�
   * @return pngͼ���ֽ�����
   */
  public byte[] toPng(int w, int h) {
	return getImageBytes(w, h, Consts.IMAGE_PNG);
  }

  /**
   * ����������gif��ʽ����
   * @param w ���
   * @param h �߶�
   * @return gifͼ���ֽ�����
   */
  public byte[] toGif(int w, int h) {
	return getImageBytes(w, h, Consts.IMAGE_GIF);
  }

  /**
   * ʵ��toString���ı�����
   */
  public String toString(){
	  StringBuffer sb = new StringBuffer();
	  if(name!=null) sb.append(name+":");
	  if(chartElements!=null) sb.append( chartElements.length()+" elements.");
	  return sb.toString();
  }
  
  /**
   * ��¡��������
   * @return ��¡�Ļ���
   */
  public Object deepClone(){
	  Canvas canvas = new Canvas();
	  canvas.name = name;
	  return canvas;
  }

  /**
   * д���ݵ���
   * @param out ObjectOutput �����
   * @throws IOException
   */
  public void writeExternal(ObjectOutput out) throws IOException {
	  out.writeByte(1);
	  out.writeObject(name);
  }

  /**
   * �����ж�����
   * @param in ObjectInput ������
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	  in.readByte();
	  name = (String)in.readObject();
  }

  /**
   * ʵ��IRecord�ӿ�
   */
  public byte[] serialize() throws IOException{
	  ByteArrayOutputRecord out = new ByteArrayOutputRecord();
	  out.writeString(name);
	  return out.toByteArray();
  }

  /**
   * ʵ��IRecord�ӿ�
   */
  public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
	  ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
	  name = in.readString();
  }
}
