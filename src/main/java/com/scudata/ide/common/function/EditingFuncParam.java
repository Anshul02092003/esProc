package com.scudata.ide.common.function;

import java.util.ArrayList;

/**
 * ���ڱ༭�ĺ�������
 *
 */
public class EditingFuncParam {
	/**
	 * ������
	 */
	private String paramString;
	/**
	 * �Ӵֵ���ʼ��
	 */
	private int boldStart = 0;
	/**
	 * �ӴֵĽ�����
	 */
	private int boldEnd = 0;

	/**
	 * ���캯��
	 */
	public EditingFuncParam() {
	}

	/**
	 * ���ò�����
	 * 
	 * @param paramString
	 */
	public void setParamString(String paramString) {
		this.paramString = paramString;
	}

	/**
	 * ȡ������
	 * 
	 * @return
	 */
	public String getParamString() {
		return paramString;
	}

	/**
	 * ���üӴֵ���ʼ��ͽ�����
	 * 
	 * @param start
	 *            �Ӵֵ���ʼ��
	 * @param end
	 *            �ӴֵĽ�����
	 */
	public void setBoldPos(int start, int end) {
		this.boldStart = start;
		this.boldEnd = end;
	}

	/**
	 * ת�ַ���
	 */
	public String toString() {
		return paramString;
	}

	/**
	 * ׷�ӱ༭�ı�
	 * 
	 * @param container
	 */
	public void appendEditingText(ArrayList<EditingText> container) {
		if (boldStart == boldEnd) {
			container.add(new EditingText(paramString));
		} else {
			container.add(new EditingText(paramString.substring(0, boldStart)));
			container.add(new EditingText(paramString.substring(boldStart,
					boldEnd), EditingText.STYLE_SELECTED));
			container.add(new EditingText(paramString.substring(boldEnd)));
		}
	}
}
