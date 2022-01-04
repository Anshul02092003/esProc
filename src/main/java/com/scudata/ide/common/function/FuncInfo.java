package com.scudata.ide.common.function;

import java.util.ArrayList;

import com.scudata.common.ICloneable;
import com.scudata.expression.Expression;

/**
 * ������Ϣ
 *
 */
public class FuncInfo implements ICloneable {
	/**
	 * ��������
	 */
	String name;

	/**
	 * ��������
	 */
	String desc;

	/**
	 * ��׺����������ͬ��������
	 */
	String postfix;

	/**
	 * ����������
	 */
	byte majorType = Expression.TYPE_UNKNOWN;

	/**
	 * ����ֵ����
	 */
	byte returnType = Expression.TYPE_UNKNOWN;

	/**
	 * ����ѡ��
	 */
	ArrayList<FuncOption> options = null;

	/**
	 * ��������
	 */
	ArrayList<FuncParam> params = null;

	/**
	 * ���캯��
	 */
	public FuncInfo() {
	}

	/**
	 * ���ú�������
	 * 
	 * @param name
	 *            ��������
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ȡ��������
	 * 
	 * @return ��������
	 */
	public String getName() {
		return name;
	}

	/**
	 * ���ú�������
	 * 
	 * @param desc
	 *            ��������
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * ȡ��������
	 * 
	 * @return ��������
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * ���ú�׺����������ͬ��������
	 * 
	 * @param postfix
	 */
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	/**
	 * ȡ��׺����������ͬ��������
	 * 
	 * @return
	 */
	public String getPostfix() {
		return postfix;
	}

	/**
	 * ��������������
	 * 
	 * @param type
	 */
	public void setMajorType(byte type) {
		majorType = type;
	}

	/**
	 * ȡ����������
	 * 
	 * @return
	 */
	public byte getMajorType() {
		return majorType;
	}

	/**
	 * ���÷���ֵ����
	 * 
	 * @param type
	 */
	public void setReturnType(byte type) {
		returnType = type;
	}

	/**
	 * ȡ����ֵ����
	 * 
	 * @return
	 */
	public byte getReturnType() {
		return returnType;
	}

	/**
	 * ���ú���ѡ��
	 * 
	 * @param options
	 */
	public void setOptions(ArrayList<FuncOption> options) {
		this.options = options;
	}

	/**
	 * ȡ����ѡ��
	 * 
	 * @return
	 */
	public ArrayList<FuncOption> getOptions() {
		return options;
	}

	/**
	 * ���ò�������
	 * 
	 * @param params
	 */
	public void setParams(ArrayList<FuncParam> params) {
		this.params = params;
	}

	/**
	 * ȡ��������
	 * 
	 * @return
	 */
	public ArrayList<FuncParam> getParams() {
		return params;
	}

	/**
	 * deepClone
	 *
	 * @return Object
	 */
	public Object deepClone() {
		FuncInfo fi = new FuncInfo();
		fi.setName(name);
		fi.setDesc(desc);
		fi.setPostfix(postfix);
		fi.setMajorType(majorType);
		fi.setReturnType(returnType);
		if (options != null) {
			ArrayList<FuncOption> cloneOptions = new ArrayList<FuncOption>(
					options.size());
			for (int i = 0; i < options.size(); i++) {
				FuncOption fo = options.get(i);
				cloneOptions.add((FuncOption) fo.deepClone());
			}
			fi.setOptions(cloneOptions);
		}
		if (params != null) {
			ArrayList<FuncParam> cloneParams = new ArrayList<FuncParam>(
					params.size());
			for (int i = 0; i < params.size(); i++) {
				FuncParam fp = params.get(i);
				cloneParams.add((FuncParam) fp.deepClone());
			}
			fi.setParams(cloneParams);
		}
		return fi;
	}
}
