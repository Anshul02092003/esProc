package com.scudata.util;

/**
 * ����ȡ����n��ֵ�Ķ�
 * @author WangXiaoJun
 *
 */
public class MaxHeap {
	// С����(�Ѷ�Ԫ����С)������Ԫ�ؽ���ʱ�ȸ��Ѷ����бȽϣ�����ȶѶ�С����
	private Object []heap; // ���ڱ�������������ڵ��ֵ������0��λ�ÿ���
	private int maxSize; // ��ౣ����ֵ������
	private int currentSize; // ��ǰ���е�ֵ������

	/**
	 * ����ȡmaxSize�����ֵ�Ķ�
	 * @param maxSize ����
	 */
	public MaxHeap(int maxSize) {
		this.heap = new Object[maxSize + 1];
		this.maxSize = maxSize;
		this.currentSize = 0;
	}

	/**
	 * ���ص�ǰ��ֵ����
	 * @return ����
	 */
	public int size() {
		return currentSize;
	}

	/**
	 * ������ֵ
	 * @param o ֵ
	 * @return true����ǰֵ��ʱ������maxSize��ֵ��Χ�ڣ�false����ǰֵ̫С������
	 */
	public boolean insert(Object o) {
		Object []heap = this.heap;
		if (currentSize == maxSize) {
			if (Variant.compare(o, heap[1], true) <= 0) {
				return false;
			} else {
				deleteRoot();
				return insert(o);
			}
		} else {
			int i = ++currentSize;
			while (i != 1 && Variant.compare(o, heap[i/2], true) < 0) {
				heap[i] = heap[i/2]; // ��Ԫ������
				i /= 2;              // ���򸸽ڵ�
			}

			heap[i] = o;
			return true;
		}
	}

	/**
	 * ɾ�����ڵ�
	 */
	private void deleteRoot() {
		// �����һ��Ԫ�ط��ڶѶ���Ȼ���Զ����µ���
		Object []heap = this.heap;
		int currentSize = this.currentSize;
		Object o = heap[currentSize];

		int i = 1;
		int c = 2; // �ӽڵ�
		while(c < currentSize) {
			// �ҳ���С���ӽڵ�
			int rc = c + 1;  // ���ӽڵ�
			if (rc < currentSize && Variant.compare(heap[rc], heap[c], true) < 0) {
				c = rc;
			}

			if (Variant.compare(o, heap[c], true) > 0) {
				heap[i] = heap[c];
				i = c;
				c *= 2;
			} else {
				break;
			}
		}

		heap[i] = o;
		heap[currentSize] = null;
		this.currentSize--;
	}

	/**
	 * ��������Ԫ��
	 * @return Ԫ������
	 */
	public Object[] toArray() {
		Object []objs = new Object[currentSize];
		System.arraycopy(heap, 1, objs, 0, currentSize);
		//Arrays.sort(objs);
		return objs;
	}
}
