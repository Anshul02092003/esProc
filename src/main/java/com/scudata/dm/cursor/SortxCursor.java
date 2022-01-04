package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;

/**
 * ��ÿ���ļ����е�����������������Ϊһ���α�
 * ����cs.sortx@n(...;n) �����������
 * @author RunQian
 *
 */
public class SortxCursor extends ICursor {
	private FileObject []files; // ��ʱ���ļ�����
	private int fileIndex = -1; // ��ǰ�������ļ������
	private Expression[] sortExps; // ������ʽ
	
	private MemoryCursor cursor; // ��ǰ�ļ�����󴴽����ڴ��α�
	
	/**
	 * ��ÿ���ļ����е�����������������Ϊһ���α�
	 * @param files ��ʱ���ļ�����
	 * @param sortExps ������ʽ����
	 * @param ds ��������ݽṹ
	 * @param ctx ����������
	 */
	public SortxCursor(FileObject []files, Expression[] sortExps, DataStruct ds, Context ctx) {
		this.files = files;
		this.sortExps = sortExps;
		this.ctx = ctx;
		setDataStruct(ds);
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (files == null || n < 1) return null;
		
		if (fileIndex == -1) {
			fileIndex++;
			BFileCursor cs = new BFileCursor(files[0], null, "x", ctx);
			Sequence seq = cs.fetch();
			seq.sort(sortExps, null, "o", ctx);
			cursor = new MemoryCursor(seq);
		}
		
		Sequence table = cursor.fetch(n);
		if (table == null || table.length() < n) {
			fileIndex++;
			if (fileIndex < files.length) {
				BFileCursor cs = new BFileCursor(files[fileIndex], null, "x", ctx);
				Sequence seq = cs.fetch();
				seq.sort(sortExps, null, "o", ctx);
				cursor = new MemoryCursor(seq);
				
				if (table == null) {
					return get(n);
				} else {
					Sequence rest;
					if (n == MAXSIZE) {
						rest = get(n);
					} else {
						rest = get(n - table.length());
					}
					
					table = append(table, rest);
				}
			} else {
				files = null;
				cursor = null;
			}
		}

		return table;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (files == null || n < 1) return 0;
		
		if (fileIndex == -1) {
			fileIndex++;
			BFileCursor cs = new BFileCursor(files[0], null, "x", ctx);
			Sequence seq = cs.fetch();
			seq.sort(sortExps, null, "o", ctx);
			cursor = new MemoryCursor(seq);
		}

		long count = cursor.skip(n);
		if (count < n) {
			fileIndex++;
			if (fileIndex < files.length) {
				BFileCursor cs = new BFileCursor(files[fileIndex], null, "x", ctx);
				Sequence seq = cs.fetch();
				seq.sort(sortExps, null, "o", ctx);
				cursor = new MemoryCursor(seq);
				
				count += skipOver(n - count);
			}
		}

		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		
		if (files != null) {
			for (FileObject file : files) {
				if (file != null) {
					file.delete();
				}
			}
			
			files = null;
			cursor = null;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		return false;
	}
}
