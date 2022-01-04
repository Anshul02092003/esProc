package com.scudata.cellset;

import java.io.Externalizable;

import com.scudata.common.*;

public interface IRowCell
	extends ICloneable, Externalizable, IRecord {

	/**
	 * �����к�
	 * @return int
	 */
	public int getRow();

	/**
	 * �����к�
	 * @param row int
	 */
	public void setRow(int row);

	/**
	 * �����и�
	 * @return float
	 */
	public float getHeight();

	/**
	 * �����и�
	 * @param h float
	 */
	public void setHeight(float h);

	/**
	 * ���ز��
	 * @return int
	 */
	public int getLevel();

	/**
	 * ���ò��
	 * @param level int
	 */
	public void setLevel(int level);
}
