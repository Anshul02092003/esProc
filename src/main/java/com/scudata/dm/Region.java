package com.scudata.dm;

/**
 * �������ڱ�����е���ʼλ�úͽ���λ��
 * @author WangXiaoJun
 *
 */
class Region {
	int start; // ��ʼλ�ã�������
	int end; // ����λ�ã�������
	
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
