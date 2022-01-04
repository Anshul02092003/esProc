package com.scudata.chart;

import java.awt.*;
import java.util.ArrayList;

import com.scudata.chart.edit.*;

/**
 * ͼԪ�ӿڣ�ͼԪ�еĲ���ֻ�����������ͣ�
 * 1����������(int,float,double,boolean,String)
 * 2����������(Param)
 * ������Ϊ��������Paramʱ��Ϊ���������д����������null��ʼֵ����Ӧ����� new Param(null)
 */
public interface IElement {
  /**
   * �г�ͼԪ�Ĳ�����Ϣ�б��ṩ���༭��ʹ��
   * @return ArrayList��ÿ����Ա��ΪParamInfo����
   */
  public ParamInfoList getParamInfoList();

  /**
   * ͼ��Ԫ���Ƿ�ɼ�
   * @return �Ƿ���true�����򷵻�false
   */
  public boolean isVisible();
  
  /**
   * ͼԪ���ػ�ǰ��ֵ��ʼ�������ڳ�ʼ��ֵ��ͼԪ���������Ⱥ����ã�����Ҫ�ȳ�ʼ�����ٿ�ʼ��ͼ
   * ��ͼ��������ε���ÿ����ͼԪ�ص�beforeDraw, drawBack,draw, drawFore
   */
  public void beforeDraw();

  /**
   * ���Ʊ�����,ע���ڱ�����Ӧ�������е����ɫ����Ӱ�ȣ��Ų��Ḳ�Ǻ����Ĳ㡣�ò㲻�ܻ��߼�����
   */
  public void drawBack();

  /**
   * �����м�㣬�м�����ͼ�ε����ɫ
   */
  public void draw(); 

  /**
   *  ����ǰ���㣬ǰ�������ͼ�εı��ߣ��Լ����ֵ�
   */
  public void drawFore();

  /**
   *  ȡͼԪ�ı߽���״��������Ӧ����¼�
   * @return �߽���״�б�
   */
  public ArrayList<Shape> getShapes();
  
  /**
   *  ȡͼԪ�ı߽���״����Ӧ������
   * @return �������б�
   */
  public ArrayList<String> getLinks();

  /**
   * ���û�ͼ���棬��ͼ�����൱�ڴ�ܼң���ͼԪ�ؿ���ͨ�������ȡ������Ԫ�ص���Ӧ��Ϣ
   * @param e ��ͼ����
   */
  public void setEngine( Engine e );
  
/**
 * ��ȡ��ͼ����
 * @return ��ͼ����
 */
  public Engine getEngine();

}
