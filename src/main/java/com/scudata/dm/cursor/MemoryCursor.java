package com.scudata.dm.cursor;

import com.scudata.dm.Sequence;

/**
 * ���ڴ����й����α�
 * A.cursor() A.cursor(k:n)
 * @author RunQian
 *
 */
public class MemoryCursor extends ICursor {
	private Sequence data; // ����
	private int startSeq; // ��ʼλ�ã�����
	private int endSeq; // ����λ�ã�������
	private int next = 1; // ��һ����¼�����
	
	/**
	 * �����ڴ��α�
	 * @param seq Դ����
	 */
	public MemoryCursor(Sequence seq) {
		if (seq != null) {
			if (seq.length() > 0) {
				data = seq;
				next = 1;
				startSeq = 1;
				endSeq = seq.length();
			} else {
				setDataStruct(seq.dataStruct());
			}
		}
	}

	/**
	 * ��ָ�����乹���ڴ��α�
	 * @param seq Դ����
	 * @param start ��ʼλ�ã�����
	 * @param end ����λ�ã�������
	 */
	public MemoryCursor(Sequence seq, int start, int end) {
		if (seq != null) {
			if (seq.length() > 0 && start < end) {
				int len = seq.length();
				if (start <= len) {
					data = seq;
					next = start;
					startSeq = start;
					endSeq = end - 1;
					if (endSeq > len) {
						endSeq = len;
					}
				}
			} else {
				setDataStruct(seq.dataStruct());
			}
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		int rest = endSeq - next + 1;
		if (rest < 1 || n < 1) {
			return null;
		}
		
		Sequence data = this.data;
		if (rest >= n) {
			int end = next + n;
			Sequence table = new Sequence(n);
			for (int i = next; i < end; ++i) {
				table.add(data.getMem(i));
			}

			this.next = end;
			return table;
		} else {
			if (next == 1 && endSeq == data.length()) {
				next = endSeq + 1;
				//return data;
				
				// ������ܻ��޸�data��������Ҫ�����¶���
				return new Sequence(data);
			}
			
			Sequence table = new Sequence(rest);
			for (int i = next, end = endSeq; i <= end; ++i) {
				table.add(data.getMem(i));
			}

			next = endSeq + 1;
			return table;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		int rest = endSeq - next + 1;
		if (rest < 1 || n < 1) {
			return 0;
		}

		if (rest > n) {
			this.next += n;
			return n;
		} else {
			next = endSeq + 1;
			return rest;
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		next = endSeq + 1;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		next = startSeq;
		return true;
	}
	
	public Sequence fuzzyFetch(int n) {
		return fetch();
	}
}
