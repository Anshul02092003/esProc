package com.scudata.util;

/**
 * �����������ݵĶ�·�鲢���㣬ÿһ·��һ���ڵ�
 * @author RunQian
 *
 */
public interface ILoserTreeNode extends Comparable<ILoserTreeNode> {
	/**
	 * ������ǰ�鲢·�ĵ�ǰԪ��
	 * @return Ԫ��ֵ
	 */
	Object popCurrent();
	
	/**
	 * ���ص�ǰ�鲢·�Ƿ���Ԫ��
	 * @return
	 */
	boolean hasNext();
}
