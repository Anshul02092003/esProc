package com.scudata.thread;

import com.scudata.dm.Sequence;

/**
 * ����ִ��A.derive������
 * @author RunQian
 *
 */
class BitsJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������	
	private String opt; // ѡ��
	private Sequence result; // �����
	
	public BitsJob(Sequence src, int start, int end, String opt) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.opt = opt;
	}
	
	public void run() {
		result = src.bits(start, end, opt);
	}

	public Sequence getResult() {
		return result;
	}
}
