package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * ���ڰѶ�·�α��ɵ�·�α�
 * ��װ��cs.groups�������㲻���ٲ��ö��߳�����
 * @author RunQian
 *
 */
public class SinglepathCursor extends ICursor {
	private ICursor cursor; // ��·�α�

	public SinglepathCursor(ICursor cursor) {
		this.cursor = cursor;
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
		return cursor.fetch(n);
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		return cursor.skip();
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		cursor.close();
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		return cursor.reset();
	}
}
