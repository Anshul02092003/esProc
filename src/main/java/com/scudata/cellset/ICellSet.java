package com.scudata.cellset;

import java.io.Externalizable;

import com.scudata.common.*;
import com.scudata.dm.Context;

public interface ICellSet
	extends ICloneable, Externalizable, IRecord {

	/**
	 * ȡ��ͨ��Ԫ��
	 * @param row �к�(��1��ʼ)
	 * @param col �к�(��1��ʼ)
	 * @return INormalCell
	 */
	public INormalCell getCell(int row, int col);

	/**
	 * ȡ��ͨ��Ԫ��
	 * @param id String ��Ԫ���ַ�����ʶ: B2
	 * @return INormalCell
	 */
	public INormalCell getCell(String id);

	/**
	 * ����ͨ��Ԫ��
	 * @param r �к�(��1��ʼ)
	 * @param c �к�(��1��ʼ)
	 * @param cell ��ͨ��Ԫ��
	 */
	public void setCell(int r, int c, INormalCell cell);

	/**
	 * ȡ���׵�Ԫ��
	 * @param r �к�(��1��ʼ)
	 * @return IRCell
	 */
	public IRowCell getRowCell(int r);

	/**
	 * �����׵�Ԫ��
	 * @param r �к�(��1��ʼ)
	 * @param rc ���׵�Ԫ��
	 */
	public void setRowCell(int r, IRowCell rc);

	/**
	 * ȡ���׵�Ԫ��
	 * @param c �к�(��1��ʼ)
	 * @return IColCell
	 */
	public IColCell getColCell(int c);

	/**
	 * �����׵�Ԫ��
	 * @param c �к�(��1��ʼ)
	 * @param cc ���׵�Ԫ��
	 */
	public void setColCell(int c, IColCell cc);

	/**
	 * @return int ���ر�������
	 */
	public int getRowCount();

	/**
	 * @return int ���ر�������
	 */
	public int getColCount();

	/**
	 * ���ص�ǰ���ڼ���ĵ�Ԫ��
	 * @return INormalCell
	 */
	public INormalCell getCurrent();

	/**
	 * �����еĲ�
	 * @param c int �к�
	 * @return int
	 */
	public int getColLevel(int c);

	/**
	 * �����еĲ�
	 * @param r int �к�
	 * @return int
	 */
	public int getRowLevel(int r);

	/**
	 * ���ؼ���������
	 * @return Context
	 */
	public Context getContext();

}
