package com.scudata.dm;

/**
 * �������ڱ�����е���ʼλ�úͽ���λ��
 * @author WangXiaoJun
 *
 */
public class Region {
	public int start; // ��ʼλ�ã�������
	public int end; // ����λ�ã�������
	
	public Region(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
