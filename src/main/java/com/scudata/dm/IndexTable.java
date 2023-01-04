package com.scudata.dm;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;

/**
 * �ڴ����������
 * @author WangXiaoJun
 *
 */
abstract public class IndexTable {	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ���˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param key ��
	 * @return
	 */
	abstract public Object find(Object key);

	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ���˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param keys ��ֵ����
	 * @return
	 */
	abstract public Object find(Object []keys);
	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ��λ�ã��˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param key ��
	 * @return
	 */
	abstract public int[] findAllPos(IArray key);

	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ��λ�ã�ֻ����signArray��Ϊtrue�ģ��˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param key ��
	 * @param signArray
	 * @return
	 */
	abstract public int[] findAllPos(IArray key, BoolArray signArray);
	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ��λ�ã��˷�����������Ϊһ���ֶεĹ�ϣ��
	 * @param keys ��ֵ����
	 * @return
	 */
	abstract public int[] findAllPos(IArray []keys);
	
	/**
	 * ���ݼ����Ҷ�Ӧ��λ�ã�ֻ����signArray��Ϊtrue�ģ��˷�����������Ϊ���ֶεĹ�ϣ��
	 * @param keys ��ֵ����
	 * @param signArray
	 * @return
	 */
	abstract public int[] findAllPos(IArray []keys, BoolArray signArray);
	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ��λ�ã��Ҳ�������0
	 * @param key ��
	 * @return
	 */
	abstract public int findPos(Object key);
	
	/**
	 * ���ݼ����Ҷ�Ӧ��ֵ��λ�ã��Ҳ�������0
	 * @param keys ��ֵ����
	 * @return
	 */
	abstract public int findPos(Object []keys);
}
