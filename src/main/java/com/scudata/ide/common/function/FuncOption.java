package com.scudata.ide.common.function;

import com.scudata.common.ICloneable;

/**
 * ����ѡ��
 *
 */
public class FuncOption implements ICloneable {
	/**
	 * ѡ���ַ�
	 */
	String optionChar;
	/**
	 * ����
	 */
	String description;
	/**
	 * �Ƿ�ȱʡѡ��
	 */
	boolean defaultSelect;

	/**
	 * �Ƿ�ѡ��
	 */
	transient boolean select;

	/**
	 * ���캯��
	 */
	public FuncOption() {
	}

	/**
	 * ����ѡ���ַ�
	 * 
	 * @param c
	 */
	public void setOptionChar(String c) {
		optionChar = c;
	}

	/**
	 * ȡѡ���ַ�
	 * 
	 * @return
	 */
	public String getOptionChar() {
		return optionChar;
	}

	/**
	 * ��������
	 * 
	 * @param desc
	 */
	public void setDescription(String desc) {
		description = desc;
	}

	/**
	 * ȡ����
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * ����ȱʡѡ��
	 * 
	 * @param select
	 */
	public void setDefaultSelect(boolean select) {
		this.defaultSelect = select;
	}

	/**
	 * ȡȱʡѡ��
	 * 
	 * @return
	 */
	public boolean isDefaultSelect() {
		return defaultSelect;
	}

	/**
	 * �����Ƿ�ѡ��
	 * 
	 * @param select
	 */
	public void setSelect(boolean select) {
		this.select = select;
	}

	/**
	 * ȡ�Ƿ�ѡ��
	 * 
	 * @return
	 */
	public boolean isSelect() {
		return select;
	}

	/**
	 * deepClone
	 * 
	 * @return Object
	 */
	public Object deepClone() {
		FuncOption fo = new FuncOption();
		fo.setOptionChar(optionChar);
		fo.setDescription(description);
		fo.setDefaultSelect(defaultSelect);
		return fo;
	}
}
