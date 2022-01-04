package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.TotalResult;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.thread.GroupsJob;
import com.scudata.thread.ThreadPool;
import com.scudata.thread.TotalJob;
import com.scudata.util.CursorUtil;

/**
 * ��·�α꣬���ڶ��̼߳���
 * @author WangXiaoJun
 *
 */
public class MultipathCursors extends ICursor implements IMultipath {
	private ICursor []cursors; // ÿһ·���α깹�ɵ�����
	
	// ���³�Ա�����α��fetch������ͨ����·�α��fetch�ǲ��ᱻ���õ�
	private Sequence table; // �����ļ�¼����
	private CursorReader []readers; // ÿһ·�α��ȡ�����񣬲��ö��߳�
	private boolean isEnd = false; // �Ƿ�ȡ������
	
	/**
	 * ������·�α�
	 * @param cursors �α�����
	 * @param ctx ����������
	 */
	public MultipathCursors(ICursor []cursors, Context ctx) {
		setDataStruct(cursors[0].getDataStruct());
		
		if (hasSame(cursors)) {
			int len = cursors.length;
			for (int i = 0; i < len; ++i) {
				cursors[i] = new SyncCursor(cursors[i]);
				cursors[i].resetContext(ctx.newComputeContext());
			}
		} else {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx.newComputeContext());
			}
		}
		
		this.cursors = cursors;
	}

	/**
	 * ��������·�α���ɵ�����
	 * @return �α�����
	 */
	public ICursor[] getCursors() {
		return cursors;
	}
	
	/**
	 * ȡ��·�α�·��
	 * @return ·��
	 */
	public int getPathCount() {
		return cursors.length;
	}
	
	private boolean hasSame(ICursor []cursors) {
		int len = cursors.length;
		for (int i = 0; i < len; ++i) {
			ICursor cursor = cursors[i];
			for (int j = i + 1; j < len; ++j) {
				if (cursor == cursors[j]) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Ϊ�α긽������
	 * @param op ����
	 * @param ctx ����������
	 */
	public Operable addOperation(Operation op, Context ctx) {
		for (ICursor cursor : cursors) {
			ctx = cursor.getContext();
			Operation dup = op.duplicate(ctx);
			cursor.addOperation(dup, ctx);
		}
		
		return this;
	}
	
	/**
	 * ��������·�α���ɵ�����
	 * @return �α�����
	 */
	public ICursor[] getParallelCursors() {
		if (readers != null) {
			int len = cursors.length;
			for (int i = 0; i < len; ++i) {
				Sequence seq = readers[i].getCatch();
				if (cache != null) {
					cache.addAll(seq);
					seq = cache;
					cache = null;
				}
				
				if (cursors[i].cache == null) {
					cursors[i].cache = seq;
				} else {
					cursors[i].cache.addAll(seq);
				}
			}
			
			readers = null;
		}
		
		return cursors;
	}
		
	private Sequence getData() {
		if (table != null) return table;

		CursorReader []readers = this.readers;
		int tcount = readers.length;
				
		for (int i = 0; i < tcount; ++i) {
			if (readers[i] != null) {
				Sequence cur = readers[i].getTable();
				if (cur != null) {
					if (table == null) {
						table = cur;
					} else {
						table = append(table, cur);
					}
				} else {
					readers[i] = null;
				}
			}
		}
		
		return table;
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		
		if (readers == null) {
			ICursor []cursors = this.cursors;
			int tcount = cursors.length;
			CursorReader []readers = new CursorReader[tcount];
			this.readers = readers;
			ThreadPool threadPool = ThreadPool.instance();
			
			int avg;
			if (n == ICursor.MAXSIZE) {
				avg = n;
			} else {
				avg = n / tcount;
				if (avg < FETCHCOUNT) {
					avg = FETCHCOUNT;
				} else if (n % tcount != 0) {
					avg++;
				}
			}
			
			for (int i = 0; i < tcount; ++i) {
				readers[i] = new CursorReader(threadPool, cursors[i], avg);
			}
		}
		
		Sequence result = getData();
		if (result == null) {
			return null;
		}
		
		int len = result.length();
		if (len > n) {
			return result.split(1, n);
		} else if (len == n) {
			this.table = null;
			return result;
		}
		
		this.table = null;
		while (true) {
			Sequence cur = getData();
			if (cur == null || cur.length() == 0) {
				return result;
			}
			
			int curLen = cur.length();
			if (len + curLen > n) {
				return append(result, cur.split(1, n - len));
			} else if (len + curLen == n) {
				this.table = null;
				return append(result, cur);
			} else {
				this.table = null;
				result = append(result, cur);
				len += curLen;
			}
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		
		if (readers == null) {
			if (n == MAXSKIPSIZE) {
				ICursor []cursors = this.cursors;
				int tcount = cursors.length;
				CursorSkipper []skipper = new CursorSkipper[tcount];
				ThreadPool threadPool = ThreadPool.instance();
							
				for (int i = 0; i < tcount; ++i) {
					skipper[i] = new CursorSkipper(threadPool, cursors[i], MAXSKIPSIZE);
				}
				
				long total = 0;
				for (int i = 0; i < tcount; ++i) {
					total += skipper[i].getActualSkipCount();
				}
				
				return total;
			}
			
			ICursor []cursors = this.cursors;
			int tcount = cursors.length;
			CursorReader []readers = new CursorReader[tcount];
			this.readers = readers;
			ThreadPool threadPool = ThreadPool.instance();
						
			for (int i = 0; i < tcount; ++i) {
				readers[i] = new CursorReader(threadPool, cursors[i], FETCHCOUNT);
			}
		}

		Sequence result = getData();
		if (result == null) {
			return 0;
		}
		
		long len = result.length();
		if (len > n) {
			result.split(1, (int)n);
			return n;
		} else if (len == n) {
			this.table = null;
			return n;
		}
		
		this.table = null;
		while (true) {
			Sequence cur = getData();
			if (cur == null || cur.length() == 0) {
				return len;
			}
			
			int curLen = cur.length();
			if (len + curLen > n) {
				cur.split(1, (int)(n - len));
				return n;
			} else if (len + curLen == n) {
				this.table = null;
				return n;
			} else {
				this.table = null;
				len += curLen;
			}
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursors != null) {
			for (int i = 0, count = cursors.length; i < count; ++i) {
				cursors[i].close();
			}

			//cursors = null;
			table = null;
			readers = null;
			isEnd = true;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		for (int i = 0, count = cursors.length; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		isEnd = false;
		return true;
	}

	private static Table groups(ICursor []cursors, Expression[] exps, String[] names, 
			Expression[] calcExps, String[] calcNames, String opt, Context ctx, int groupCount) {
		int cursorCount = cursors.length;		
		int keyCount = exps == null ? 0 : exps.length;
		int valCount = calcExps == null ? 0 : calcExps.length;
		String option = opt == null ? "u" : opt + "u";
		
		if (valCount > 0) {			
			// ���ɽ���������ֶ��ֶ���
			if (calcNames == null) {
				calcNames = new String[valCount];
			}
			
			for (int i = 0; i < valCount; ++i) {
				if (calcNames[i] == null || calcNames[i].length() == 0) {
					calcNames[i] = calcExps[i].getFieldName();
				}
			}
		}
		
		// ���ɷ��������ύ���̳߳�
		ThreadPool pool = ThreadPool.newInstance(cursorCount);
		Table result = null;

		try {
			GroupsJob []jobs = new GroupsJob[cursorCount];
			for (int i = 0; i < cursorCount; ++i) {
				Context tmpCtx = ctx.newComputeContext();
				Expression []tmpExps = Operation.dupExpressions(exps, tmpCtx);
				Expression []tmpCalcExps = Operation.dupExpressions(calcExps, tmpCtx);
				
				jobs[i] = new GroupsJob(cursors[i], tmpExps, names, tmpCalcExps, calcNames, option, tmpCtx);
				if (groupCount > 1) {
					jobs[i].setGroupCount(groupCount);
				}
				
				pool.submit(jobs[i]);
			}
			
			// �ȴ���������ִ����ϣ����ѽ����ӵ�һ�����
			for (int i = 0; i < cursorCount; ++i) {
				jobs[i].join();
				if (result == null) {
					result = jobs[i].getResult();
				} else {
					result.addAll(jobs[i].getResult());
				}
			}
		} finally {
			pool.shutdown();
		}
		
		if (result == null || result.length() == 0) {
			return result;
		}
		
		// ���ɶ��η��������ʽ
		Expression []keyExps = null;
		if (keyCount > 0) {
			keyExps = new Expression[keyCount];
			for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
				keyExps[i] = new Expression(ctx, "#" + q);
			}
		}

		// ���ɶ��η�����ܱ��ʽ
		Expression []valExps = null;
		if (valCount > 0) {
			valExps = new Expression[valCount];
			for (int i = 0, q = keyCount + 1; i < valCount; ++i, ++q) {
				Node gather = calcExps[i].getHome();
				gather.prepare(ctx);
				valExps[i] = gather.getRegatherExpression(q);
			}
		}

		// ���ж��η���
		return result.groups(keyExps, names, valExps, calcNames, opt, ctx);
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx) {
		if (cursors.length == 1 || Env.getParallelNum() == 1) {
			return super.groups(exps, names, calcExps, calcNames, opt, ctx);
		} else {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, -1);
		}
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param groupCount ���������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx, int groupCount) {
		if (cursors.length == 1 || Env.getParallelNum() == 1) {
			return super.groups(exps, names, calcExps, calcNames, opt, ctx, groupCount);
		} else if (groupCount < 1 || exps == null || exps.length == 0) {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, -1);
		} else if (opt != null && opt.indexOf('n') != -1) {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		} else {
			return CursorUtil.fuzzyGroups(this, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		}
	}

	/**
	 * ���α���л���
	 * @param calcExps ���ܱ��ʽ����
	 * @param ctx ����������
	 * @return ���ֻ��һ�����ܱ��ʽ���ػ��ܽ�������򷵻ػ��ܽ�����ɵ�����
	 */
	public Object total(Expression[] calcExps, Context ctx) {
		if (cursors.length == 1 || Env.getParallelNum() == 1) {
			return super.total(calcExps, ctx);
		}

		int cursorCount = cursors.length;		
		int valCount = calcExps.length;
		
		// ���ɻ��������ύ���̳߳�
		Table result;
		ThreadPool pool = ThreadPool.newInstance(cursorCount);

		try {
			TotalJob []jobs = new TotalJob[cursorCount];
			for (int i = 0; i < cursorCount; ++i) {
				Context tmpCtx = ctx.newComputeContext();
				Expression []tmpCalcExps = Operation.dupExpressions(calcExps, tmpCtx);
				
				jobs[i] = new TotalJob(cursors[i], tmpCalcExps, tmpCtx);
				pool.submit(jobs[i]);
			}
			
			// �ȴ���������ִ����ϣ����ѽ����ӵ�һ�����
			if (valCount == 1) {
				String []fnames = new String[]{"_1"};
				result = new Table(fnames, cursorCount);
				for (int i = 0; i < cursorCount; ++i) {
					jobs[i].join();
					Record r = result.newLast();
					r.setNormalFieldValue(0, jobs[i].getResult());
				}
			} else {
				String []fnames = new String[valCount];
				for (int i = 1; i < valCount; ++i) {
					fnames[i - 1] = "_" + i;
				}
				
				result = new Table(fnames, cursorCount);
				for (int i = 0; i < cursorCount; ++i) {
					jobs[i].join();
					Sequence seq = (Sequence)jobs[i].getResult();
					result.newLast(seq.toArray());
				}
			}
		} finally {
			pool.shutdown();
		}
		
		// ���ɶ��λ��ܱ��ʽ
		Expression []valExps = new Expression[valCount];
		for (int i = 0; i < valCount; ++i) {
			Node gather = calcExps[i].getHome();
			gather.prepare(ctx);
			valExps[i] = gather.getRegatherExpression(i + 1);
		}
		
		// ���ж��λ���
		TotalResult total = new TotalResult(valExps, ctx);
		total.push(result, ctx);
		return total.result();
	}
}
