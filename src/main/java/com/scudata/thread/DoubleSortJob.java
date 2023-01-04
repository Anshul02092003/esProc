package com.scudata.thread;


/**
 * ����ִ�����������
 * @author RunQian
 *
 */
class DoubleSortJob extends Job {
	private double []src;
	private double []dest;
	
	private int fromIndex; // ��ʼλ�ã�����
	private int toIndex; // ����λ�ã�������
	private int off; // ���������ƫ����
	
	private int threadCount; // �����߳���
	
	public DoubleSortJob(double []src, double []dest, int fromIndex, int toIndex, int off, int threadCount) {
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
