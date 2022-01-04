package com.scudata.cellset;

import java.io.Externalizable;

import com.scudata.common.*;

public interface IColCell
	extends ICloneable, Externalizable, IRecord {

	/**
	 * �����к�
	 * @return int
	 */
	public int getCol();

	/**
	 * �����к�
	 * @param col int
	 */
	public void setCol(int col);

	/**
	 * �����п�
	 * @return float
	 */
	public float getWidth();

	/**
	 * �����п�
	 * @param w float
	 */
	public void setWidth(float w);

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
