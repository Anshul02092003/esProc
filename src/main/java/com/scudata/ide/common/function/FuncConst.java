package com.scudata.ide.common.function;

/**
 * ��������
 */
public class FuncConst {

	/** δ֪�����㲻��ʱ����������ֵ��ȷ���� */
	public static final byte FILTER_NULL = 0;
	/** ��������ֶ� */
	public static final byte FILTER_MAJOR_FIELD = 1;
	/** ��һ���������ֶ� */
	public static final byte FILTER_FIRSTPARA_FIELD = 2;
	/** ������ */
	public static final byte FILTER_SORT = 3;

	/**
	 * ѡ��ָ�����ͣ�ֵΪ���ϳ���
	 */
	private byte funcFilterId;
	/**
	 * ֵ
	 */
	private String value;
	/**
	 * ����
	 */
	private String title;

	/**
	 * ���캯��
	 * 
	 * @param funcFilterId
	 *            ѡ��ָ������
	 * @param value
	 *            ֵ
	 * @param title
	 *            ����
	 */
	public FuncConst(byte funcFilterId, String value, String title) {
		this.funcFilterId = funcFilterId;
		this.value = value;
		this.title = title;
	}

	/**
	 * ȡѡ��ָ������
	 * 
	 * @return
	 */
	public byte getFuncFilterId() {
		return funcFilterId;
	}

	/**
	 * ȡֵ
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * ȡ����
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * ȡ������
	 * 
	 * @return
	 */
	public static FuncConst[] listAllConsts() {
		return null;
	}
}
