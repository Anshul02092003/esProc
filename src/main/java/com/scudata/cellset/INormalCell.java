package com.scudata.cellset;

import java.io.Externalizable;

import com.scudata.common.*;
import com.scudata.dm.Context;

public interface INormalCell
	extends ICloneable, Externalizable, IRecord {

	/**
	 * ȡ�õ�Ԫ����к�
	 * @return int
	 */
	public int getRow();

	/**
	 * ���õ�Ԫ����к�
	 * @param r int
	 */
	public void setRow(int r);

	/**
	 * ȡ�õ�Ԫ����к�
	 * @return int
	 */
	public int getCol();

	/**
	 * ���õ�Ԫ����к�
	 * @param c int
	 */
	public void setCol(int c);

	/**
	 * ���ص�Ԫ���ʶ
	 * @return String
	 */
	public String getCellId();

	/**
	 * ���ص�Ԫ������������
	 * @return ICellSet
	 */
	public ICellSet getCellSet();

	/**
	 * ���õ�Ԫ������������
	 * @param cs ICellSet
	 */
	public void setCellSet(ICellSet cs);

	/**
	 * @return String ���ص�Ԫ����ʽ
	 */
	public String getExpString();

	/**
	 * ���õ�Ԫ����ʽ
	 * @param exp String
	 */
	public void setExpString(String exp);

	/**
	 * ���ص�Ԫ��ı��ʽ
	 * @return Expression
	 */
	//public Expression getExpression();

	/**
	 * ���㵥Ԫ����ʽ�ķ���ֵ����
	 * @param ctx Context
	 * @return byte
	 */
	public byte calcExpValueType(Context ctx);

	/**
	 * ���ص�Ԫ��ֵ��û�м����򷵻ؿ�
	 * @return Object
	 */
	public Object getValue();

	/**
	 * ���ص�Ԫ��ֵ��û�м����������
	 * @param doCalc boolean
	 * @return Object
	 */
	public Object getValue(boolean doCalc);

	/**
	 * ���õ�Ԫ��ֵ
	 * @param value Object
	 */
	public void setValue(Object value);
}
