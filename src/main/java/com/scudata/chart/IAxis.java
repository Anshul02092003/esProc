package com.scudata.chart;

import java.awt.geom.Point2D;
import java.util.*;
/**
 * ������ӿ�
 * @author Administrator
 *
 */
public interface IAxis{
  public Point2D getBasePoint(ICoor coor);//��ͼʱ����ʼ��
  
  public String getName();

  public int getLocation();

  
  //��ͼǰ׼�����������������ĳ�ʼ�������ظ�repaintʱ�����ٵ��ø÷���
  public void prepare(ArrayList<DataElement> dataElements);
}
