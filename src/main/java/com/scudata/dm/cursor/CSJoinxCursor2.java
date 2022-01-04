package com.scudata.dm.cursor;

import com.scudata.dm.*;
import com.scudata.dm.op.Operation;
import com.scudata.dw.MemoryTable;
import com.scudata.expression.Expression;
import com.scudata.util.CursorUtil;

/**
 * �α�joinx�࣬�����ǹ鲢��
 * �α���һ���ɷֶμ��ļ���ʵ��T��join���㡣cs����������ʱʹ������ࡣ
 * @author 
 * 
 */
public class CSJoinxCursor2 extends ICursor {
	private ICursor srcCursor;//Դ�α�
	private Object []fileTable;//ά��
	private Expression [][]fields;//��ʵ���ֶ�
	private Expression [][]keys;//ά���ֶ�
	private Expression [][]exps;//�µı��ʽ
	private String option;
	private String fname;
	private String[][] expNames;
	
	private Sequence cache;
	private boolean isEnd;
	private int n;//����������
	
	public CSJoinxCursor2(ICursor cursor, Expression [][]fields, Object []fileTable, 
			Expression[][] keys, Expression[][] exps, String[][] expNames, String fname, Context ctx, int n, String option) {
		srcCursor = cursor;
		this.fileTable = fileTable;
		this.fields = fields;
		this.keys = keys;
		this.exps = exps;
		this.ctx = ctx;
		this.option = option;
		this.fname = fname;
		this.expNames = expNames;
		this.n = n;
		if (this.n < ICursor.FETCHCOUNT) {
			this.n = ICursor.FETCHCOUNT;
		}
		//���newNames����null������newExps���
		for (int i = 0, len = expNames.length; i < len; i++) {
			String[] arr = this.expNames[i];
			for (int j = 0, len2 = arr.length; j < len2; j++) {
				if (arr[j] == null) {
					arr[j] = exps[i][j].getFieldName();
				}
			}
		}
	}

	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			exps = Operation.dupExpressions(exps, ctx);
			super.resetContext(ctx);
		}
	}

	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		Sequence temp, result;
		Sequence cache = this.cache;
		int len = 0;
		if (cache != null) {
			len = cache.length();
			if (len > n) {
				this.cache = cache.split(n + 1);
				return cache;
			} else if (len == n) {
				this.cache = null;
				return cache;
			}
		}
		
		while (true) {
			if (option != null && option.indexOf("z")!=-1) {
				temp = new MemoryTable(srcCursor, this.n);
			} else {
				temp = srcCursor.fetch(this.n);
			}
			if (temp == null || temp.length() == 0) {
				if (cache != null && cache.length() > n) {
					this.cache = cache.split(n + 1);
					return cache;
				} else {
					isEnd = true;
					this.cache = null;
					return cache;
				}
				
			}
			result = CursorUtil.joinx(temp, fields, fileTable, keys, exps, expNames, fname, ctx, option);
			if (result != null && result.length() != 0) {
				if (cache == null) {
					cache = result;
					if (n == result.length()) {
						this.cache = null;
						return result;
					}
				} else {
					cache.addAll(result);
					len = cache.length();
					if (len > n) {
						this.cache = cache.split(n + 1);
						return cache;
					} else if (len == n) {
						this.cache = null;
						return cache;
					}
				}
			}
		}
	}

	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		Sequence seq = get((int) n);
		if (seq != null) {
			return seq.length();
		} else {
			return 0;
		}
	}

	public synchronized void close() {
		super.close();
		srcCursor.close();
		isEnd = true;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		super.close();
		srcCursor.reset();
		isEnd = false;
		return true;
	}
}
