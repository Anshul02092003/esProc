package com.scudata.cellset.graph.draw;

import com.scudata.chart.Consts;

/**
 * ��չͼ��ϵ�����Զ���
 * @author Joancy
 *
 */
public class ExtGraphSery implements Comparable{
  /** ϵ������  */
  private String name;

  /** ϵ�б��ʽ  */
  private Number value = null;

  /** ϵ��Tip  */
  private String tips = null;
  
  private byte axis = Consts.AXIS_LEFT;//Left
  /**
   * ����ϵ������
   * param  name ϵ������
   */
  public void setName(String name) {
	this.name = name;
  }

  /**
   * ����ϵ��ֵ
   * @param  value ϵ��ֵ
   */
  public void setValue(Number value) {
	this.value = value;
  }

  /**
   * ���ϵ������
   * @return  String ϵ������
   */
  public String getName() {
	return name;
  }

  /**
   * ���ϵ��ֵ
   * @return  Object ϵ��ֵ
   */
  public double getValue() {
	if (value == null) {
	  return 0;
	}
	//�����Ͳ�һ�£�����floatת��double��ֱ��ת������ɾ��Ȳ������ᷢ���洢ֵ�����Դ˴������ַ�������һ��
	if(value instanceof Float){
		double d = Double.parseDouble(""+value);
		return d;
	}
	return value.doubleValue();
  }

  /**
   * ��ȡϵ��ֵ
   * @return ֵ
   */
  public Number getValueObject() {
	return value;
  }

  /**
   * �ж��Ƿ�Ϊ��ֵ
   * @return ��ֵ����true�����򷵻�false
   */
  public boolean isNull() {
	return value == null;
  }

  /**
   * ȡ���ɳ����ӵ�tip
   * @return tip��
   */
  public String getTips(){
	return tips;
  }
  
  /**
   * �������ɳ����ӵ�tip��
   * @param tip ��ֵ
   */
  public void setTips( String tip ){
	tips = tip;
  }
  
  /**
   * ȡ��ǰϵ�ж�Ӧ���ᣬ˫��ͼʱ��ϵ�л�����Ӧ���ᣬ��Ϊ������
   * @return
   */
  public byte getAxis(){
	return axis;
  }
  public void setAxis( byte axis ){
	this.axis = axis;
  }
  /**
   * ʵ�ֱȽϽӿ�
   */
  public int compareTo(Object o) {
	ExtGraphSery other = (ExtGraphSery)o;
	return new Double(getValue()).compareTo( new Double(other.getValue()));
  }
}
