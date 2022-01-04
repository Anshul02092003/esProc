package com.scudata.dm.cursor;

import com.scudata.thread.Job;
import com.scudata.thread.ThreadPool;

/**
 * ���ڶ��̴߳��α��������ݵ����񣬽����ȡ�ߺ������ᱻ���뵽�̳߳��У�������������
 * @author WangXiaoJun
 *
 */
class CursorSkipper extends Job {
	private ThreadPool threadPool; // �̳߳�
	private ICursor cursor; // Ҫȡ�����α�
	private long skipCount; // ÿ�������ļ�¼��
	private long actualSkipCount; // ʵ�������ļ�¼��

	/**
	 * �������α�����ָ����¼��������ʹ��getActualSkipCount�õ�ʵ�������ļ�¼��
	 * @param threadPool �̳߳�
	 * @param cursor �α�
	 * @param count ÿ�������ļ�¼��
	 */
	public CursorSkipper(ThreadPool threadPool, ICursor cursor, long count) {
		this.threadPool = threadPool;
		this.cursor = cursor;
		this.skipCount = count;
		threadPool.submit(this);
	}
	
	/**
	 * ȡʵ�������ļ�¼��
	 * @return long
	 */
	public long getActualSkipCount() {
		join();
		long count = actualSkipCount;
		if (count < skipCount) {
			threadPool.submit(this);
		}
		
		return count;
	}

	/**
	 * ���̳߳�����̵߳��ã������α�����
	 */
	public void run() {
		actualSkipCount = cursor.skip(skipCount);
	}
}
