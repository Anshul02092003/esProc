package com.scudata.array;

public interface NumberArray extends IArray {
	/**
	 * ȡָ��λ��Ԫ�ص�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ����ֵ
	 */
	//int getInt(int index);

	/**
	 * ȡָ��λ��Ԫ�صĳ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	//long getLong(int index);
	
	/**
	 * ȡָ��λ��Ԫ�صĸ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	double getDouble(int index);
	
	/**
	 * ������������Ӧ�ĳ�Ա���бȽϣ����رȽϽ������
	 * @param rightArray �Ҳ�����
	 * @return IntArray 1������0����ȣ�-1���Ҳ��
	 */
	IntArray memberCompare(NumberArray rightArray);
}
