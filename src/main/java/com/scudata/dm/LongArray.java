package com.scudata.dm;

/**
 * long����
 * @author WangXiaoJun
 *
 */
public class LongArray {
	private long []datas; // Ԫ��ֵ����
	private int size; // Ԫ����

	/**
	 * ����long����
	 */
	public LongArray() {
		this(8);
	}

	/**
	 * ����long����
	 * @param capacity ��ʼ����
	 */
	public LongArray(int capacity) {
		datas = new long[capacity];
	}

	/**
	 * ȡԪ����
	 * @return
	 */
	public int size() {
		return size;
	}

	/**
	 * ����Ԫ��ֵ��ɵ�����
	 * @return
	 */
	public long[] toArray() {
		if (datas.length == size) {
			return datas;
		}
		
		long []tmp = new long[size];
		System.arraycopy(datas, 0, tmp, 0, size);
		return tmp;
	}

	/**
	 * ȡָ��λ�õ�longֵ
	 * @param index λ�ã���0��ʼ����
	 * @return long
	 */
	public long get(int index) {
		return datas[index];
	}

	/**
	 * ���һ��longֵ��������
	 * @param value
	 */
	public void add(long value) {
		ensureCapacity(size+1);
		datas[size++] = value;
	}

	/**
	 * ȷ��������С��ָ����С
	 * @param mincap ����
	 */
	public void ensureCapacity(int mincap) {
		if(mincap > datas.length) {
			int newcap = (datas.length * 3)/2 + 1;
			long []olddata = datas;
			datas = new long[newcap < mincap ? mincap : newcap];
			System.arraycopy(olddata, 0, datas, 0, size);
		}
	}
}
