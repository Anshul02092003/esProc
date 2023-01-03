package com.scudata.thread;


/**
 * ����ִ�����������
 * @author RunQian
 *
 */
class StringSortJob extends Job {
	private String []src;
	private String []dest;
	
	private int fromIndex; // ��ʼλ�ã�����
	private int toIndex; // ����λ�ã�������
	private int off; // ���������ƫ����
	
	private int threadCount; // �����߳���
	
	public StringSortJob(String []src, String []dest, int fromIndex, int toIndex, int off, int threadCount) {
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
