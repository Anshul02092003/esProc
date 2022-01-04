package com.scudata.ide.common.function;

import java.util.List;

/**
 * �������ӿ�
 *
 */
public interface IParamTreeNode {
	/** Ҷ�ӽڵ� */
	public static final char NORMAL = 0;
	/** 1 �ֺţ����ȼ���� */
	public static final char SEMICOLON = ';';
	/** 2 ���� */
	public static final char COMMA = ',';
	/** 3 ð�� */
	public static final char COLON = ':';

	/**
	 * ���ؽڵ������
	 * 
	 * @return char Normal��Semicolon��Comma��Colon
	 */
	char getType();

	/**
	 * ���ؽڵ�����
	 * 
	 * @return String
	 */
	String getContent();

	/**
	 * �����Ƿ���Ҷ�ӽڵ�
	 * 
	 * @return boolean
	 */
	boolean isLeaf();

	/**
	 * �����ӽڵ�����Ҷ�ӽڵ㷵��0
	 * 
	 * @return int
	 */
	int getSubSize();

	/**
	 * ����ĳһ�ӽڵ㣬��0��ʼ����
	 * 
	 * @param index
	 *            int
	 * @return IParam
	 */
	IParamTreeNode getSub(int index);

	/**
	 * �������еĽڵ㣬�����ָ����ڵ�
	 * 
	 * @param list
	 *            List Ԫ����IParamTreeNode
	 */
	void getAllParam(List<IParamTreeNode> list);

	/**
	 * �������е�Ҷ�ӽڵ�
	 * 
	 * @param list
	 *            List Ԫ����IParamTreeNode
	 */
	void getAllLeafParam(List<IParamTreeNode> list);
}
