package com.scudata.thread;

import java.util.Comparator;

import com.scudata.common.ICloneable;


/**
 * ����ִ�����������
 * @author RunQian
 *
 */
class SortJob extends Job {
	private Object []src;
	private Object[] dest;
	
	private int fromIndex; // ��ʼλ�ã�����
	private int toIndex; // ����λ�ã�������
	private int off; // ���������ƫ����
	
	private Comparator<Object> c; // �Ƚ���
	private int threadCount; // �����߳���
	
	public SortJob(Object []src, Object[] dest, int fromIndex, int toIndex, int off, Comparator<Object> c, int threadCount) {
		this.src = src;
		this.dest = dest;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.off = off;
		this.threadCount = threadCount;
		
		if (c instanceof ICloneable) {
			this.c = (Comparator<Object>)((ICloneable)c).deepClone();
		} else {
			this.c = c;
		}
	}
	
	public void run() {
		MultithreadUtil.mergeSort(src, dest, fromIndex, toIndex, off, c, threadCount);
	}
}
