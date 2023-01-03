package com.scudata.thread;

import java.util.Date;

/**
 * ����ִ�����������
 * @author RunQian
 *
 */
class DateSortJob extends Job {
	private Date []src;
	private Date []dest;
	
	private int fromIndex; // ��ʼλ�ã�����
	private int toIndex; // ����λ�ã�������
	private int off; // ���������ƫ����
	
	private int threadCount; // �����߳���
	
	public DateSortJob(Date []src, Date []dest, int fromIndex, int toIndex, int off, int threadCount) {
		this.src = src;
		this.dest = dest;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.off = off;
		this.threadCount = threadCount;
	}
	
	public void run() {
		MultithreadUtil.mergeSort(src, dest, fromIndex, toIndex, off, threadCount);
	}
}
