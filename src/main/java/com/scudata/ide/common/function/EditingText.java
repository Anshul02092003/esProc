package com.scudata.ide.common.function;

/**
 * ���ڱ༭���ı�
 *
 */
public class EditingText {

	/** ��ͨ�ı� */
	public static String STYLE_NORMAL = "N";
	/** �����ı� */
	public static String STYLE_HIGHLIGHT = "H";
	/** ѡ���ı� */
	public static String STYLE_SELECTED = "S";

	/**
	 * �ı�
	 */
	private String text;
	/**
	 * ��ʽ
	 */
	private String style = STYLE_NORMAL;

	/**
	 * ���캯��
	 * 
	 * @param text
	 *            �ı�
	 */
	public EditingText(String text) {
		this.text = text;
	}

	/**
	 * ���캯��
	 * 
	 * @param text
	 *            �ı�
	 * @param style
	 *            ��ʽ
	 */
	public EditingText(String text, String style) {
		this.text = text;
		this.style = style;
	}

	/**
	 * ȡ�ı�
	 * 
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * ������ʽ
	 * 
	 * @param style
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * ȡ��ʽ
	 * 
	 * @return
	 */
	public String getStyle() {
		return style;
	}
}
