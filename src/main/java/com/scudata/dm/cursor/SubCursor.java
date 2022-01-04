package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * ���α꣬���ڴӸ����α���ȡָ��������
 * @author RunQian
 *
 */
public class SubCursor extends ICursor {
	private ICursor cursor; // Դ�α�
	private int count; // ��ȡ��¼��
	private int total; // ��Ҫȡ���ܼ�¼��
	
	/**
	 * �������α�
	 * @param cursor Դ�α�
	 * @param total Ҫȡ�����ݵ�������
	 */
	public SubCursor(ICursor cursor, int total) {
		this.cursor = cursor;
		this.total = total;
		setDataStruct(cursor.getDataStruct());
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			cursor.resetContext(ctx);
			super.resetContext(ctx);
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (cursor == null || n < 1) return null;

		if (count + n > total) {
			n = total - count;
			if (n == 0) return null;
		}
		
		Sequence seq = cursor.fetch(n);
		if (seq != null) {
			count += seq.length();
		}
		
		return seq;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (cursor == null || n < 1) return 0;

		if (count > total - n) {
			n = total - count;
			if (n == 0) return 0;
		}
		
		n = cursor.skip(n);
		count += n;
		return n;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
}
