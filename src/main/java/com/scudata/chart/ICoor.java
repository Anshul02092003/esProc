package com.scudata.chart;

import java.awt.geom.Point2D;

import com.scudata.chart.element.*;

/**
 * ����ϵ�ӿ�
 * @author Joancy
 *
 */
public interface ICoor {
	/**
	 * ���ù�������ϵ�Ŀ̶���1
	 * 
	 * @param a �̶���
	 */
  public void setAxis1(TickAxis a);

	/**
	 * ���ù�������ϵ�Ŀ̶���2
	 * 
	 * @param a �̶���
	 */
  public void setAxis2(TickAxis a);

/**
 * ��ȡ�̶���1
 * @return �̶���
 */
  public TickAxis getAxis1();

  /**
   * ��ȡ�̶���2
   * @return �̶���
   */
  public TickAxis getAxis2();

  /**
   * ��ȡ�߼�ֵval1��val2�ڸ�����ϵ�µľ�����������
   * @param val1 Object ��Ӧ��1���߼�����
   * @param val2 Object ��Ӧ��2���߼�����
   * @return Point ʵ�����ȵ���������
   */
  public Point2D getScreenPoint(Object val1, Object val2);

  /**
   * ��ȡ��ֵ�ᣬ����еĻ���һ��������ֵ���ö������ϵ�����ϵʱ
   * @return ��ֵ��
   */
  public NumericAxis getNumericAxis();
  
  /**
   * ��ȡ����ϵ�����ö����
   * @return ö����
   */
  public EnumAxis getEnumAxis();
  
  /**
   * �жϵ�ǰ����ϵ�Ƿ�Ϊ������ϵ
   * @return �Ƿ���true�����򷵻�false
   */
  public boolean isPolarCoor();
  
  /**
   * �жϵ�ǰ����ϵ�Ƿ�Ϊֱ������ϵ
   * @return �Ƿ���true�����򷵻�false
   */
  public boolean isCartesianCoor();
  
	/**
	 * ����ͼԪ�������жϵ�ǰ����ϵ�Ƿ�����һ��ö����
	 * ֻ�д�����ö�������ֵ���ܶѻ�
	 * @return �Ƿ���true�����򷵻�false
	 */
  public boolean isEnumBased();
}
