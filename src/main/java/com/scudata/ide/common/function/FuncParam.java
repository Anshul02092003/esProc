package com.scudata.ide.common.function;

import java.util.ArrayList;

import com.scudata.common.ICloneable;

/**
 * ��������
 *
 */
public class FuncParam implements ICloneable {
	/**
	 * ˵��
	 */
	String desc;
	/**
	 * ǰ����
	 */
	char preSign;
	/**
	 * �Ƿ��Ӳ���
	 */
	boolean isSubParam;
	/**
	 * �Ƿ���ظ�
	 */
	boolean isRepeatable;
	/**
	 * �Ƿ��ʶ��
	 */
	boolean isIdentifierOnly;
	/**
	 * ѡ��ָ������
	 */
	byte filterType = FuncConst.FILTER_NULL;
	/**
	 * ѡ���б�
	 */
	ArrayList<FuncOption> options = null;

	/**
	 * ����ֵ
	 */
	transient String paramValue = "";

	/**
	 * ���캯��
	 */
	public FuncParam() {
	}

	/**
	 * ����˵��
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * ȡ˵��
	 * 
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * ����ǰ����
	 * 
	 * @param sign
	 */
	public void setPreSign(char sign) {
		preSign = sign;
	}

	/**
	 * ȡǰ����
	 * 
	 * @return
	 */
	public char getPreSign() {
		return preSign;
	}

	/**
	 * �����Ƿ��Ӳ���
	 * 
	 * @param isSub
	 */
	public void setSubParam(boolean isSub) {
		isSubParam = isSub;
	}

	/**
	 * ȡ�Ƿ��Ӳ���
	 * 
	 * @return
	 */
	public boolean isSubParam() {
		return isSubParam;
	}

	/**
	 * �����Ƿ���ظ�
	 * 
	 * @param repeatable
	 */
	public void setRepeatable(boolean repeatable) {
		isRepeatable = repeatable;
	}

	/**
	 * ȡ�Ƿ���ظ�
	 */
	public boolean isRepeatable() {
		return isRepeatable;
	}

	/**
	 * �����Ƿ��ʶ��
	 * 
	 * @param identifierOnly
	 */
	public void setIdentifierOnly(boolean identifierOnly) {
		isIdentifierOnly = identifierOnly;
	}

	/**
	 * ȡ�Ƿ��ʶ��
	 * 
	 * @return
	 */
	public boolean isIdentifierOnly() {
		return isIdentifierOnly;
	}

	/**
	 * ����ѡ��ָ������
	 * 
	 * @param type
	 */
	public void setFilterType(byte type) {
		filterType = type;
	}

	/**
	 * ȡѡ��ָ������
	 * 
	 * @return
	 */
	public byte getFilterType() {
		return filterType;
	}

	/**
	 * ���ò���ֵ
	 * 
	 * @param value
	 */
	public void setParamValue(String value) {
		paramValue = value;
	}

	/**
	 * ȡ����ֵ
	 * 
	 * @return
	 */
	public String getParamValue() {
		return paramValue;
	}

	/**
	 * ����ѡ���б�
	 * 
	 * @param options
	 */
	public void setOptions(ArrayList<FuncOption> options) {
		this.options = options;
	}

	/**
	 * ȡѡ���б�
	 * 
	 * @return
	 */
	public ArrayList<FuncOption> getOptions() {
		return options;
	}

	/**
	 * ��¡
	 */
	public Object deepClone() {
		FuncParam fp = new FuncParam();
		fp.setDesc(desc);
		fp.setPreSign(preSign);
		fp.setSubParam(isSubParam);
		fp.setRepeatable(isRepeatable);
		fp.setIdentifierOnly(isIdentifierOnly);
		fp.setFilterType(filterType);
		if (options != null) {
			ArrayList<FuncOption> cloneOptions = new ArrayList<FuncOption>();
			for (int i = 0; i < options.size(); i++) {
				cloneOptions.add((FuncOption) options.get(i).deepClone());
			}
			fp.setOptions(cloneOptions);
		}
		return fp;
	}
}
