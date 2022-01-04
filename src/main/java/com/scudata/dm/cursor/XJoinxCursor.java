package com.scudata.dm.cursor;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.Select;
import com.scudata.expression.Expression;

/**
 * ���ڶ��α������
 * xjoinx(csi:Fi,xi;��)
 * @author RunQian
 *
 */
public class XJoinxCursor extends ICursor {
	private ICursor []cursors; // �α�����
	private Expression []exps; // ���˱��ʽ
	private DataStruct ds; // ��������ݽṹ
	private boolean isLeft = false; // �Ƿ�������
	private boolean isEnd = false; // �Ƿ�ȡ������

	private Sequence []tables; // ÿ���α�ȡ�������ݻ���
	private int []currents; // ÿ���α굱ǰ������������
	private Record record; // ���ڼ���

	/**
	 * ��������α�
	 * @param cursors �α�����
	 * @param exps ���˱��ʽ
	 * @param names �ֶ�������
	 * @param fltOpts ����ѡ��
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public XJoinxCursor(ICursor []cursors, Expression []exps, String []names, String[] fltOpts, String opt, Context ctx) {
		this.cursors = cursors;
		this.exps = exps;
		this.ctx = ctx;
		this.isLeft = opt != null && opt.indexOf('1') != -1;

		int tcount = cursors.length;
		if (names == null) {
			names = new String[tcount];
		}

		ds = new DataStruct(names);
		setDataStruct(ds);
		
		for (int i = 0; i < tcount; ++i) {
			if (exps[i] != null) {
				cursors[i].addOperation(new Select(exps[i], fltOpts[i]), ctx);
			}
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			exps = Operation.dupExpressions(exps, ctx);
			super.resetContext(ctx);
		}
	}

	private void init() {
		if (tables != null) {
			return;
		}
		
		int tcount = cursors.length;
		tables = new Sequence[tcount];
		currents = new int[tcount];
		record = new Record(ds);
		
		ComputeStack stack = ctx.getComputeStack();
		stack.push(record);
		try {
			for (int i = 0; i < tcount; ++i) {
				currents[i] = 1;
				tables[i] = cursors[i].fetch(FETCHCOUNT);
				if (tables[i] == null) {
					break;
				} else if (tables[i].length() == 0) {
					tables[i] = null;
					break;
				} else {
					record.setNormalFieldValue(i, tables[i].getMem(1));
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	private boolean next(int t) {
		Sequence []tables = this.tables;
		int []currents = this.currents;
		if (tables[t].length() < ++currents[t]) {
			tables[t] = cursors[t].fetch(FETCHCOUNT);
			if (tables[t] != null && tables[t].length() > 0) {
				currents[t] = 1;
				record.setNormalFieldValue(t, tables[t].getMem(1));
				return true;
			} else if (t > 0) {
				if (next(t - 1)) {
					cursors[t].reset();
					tables[t] = cursors[t].fetch(FETCHCOUNT);
					if (tables[t] != null && tables[t].length() > 0) {
						currents[t] = 1;
						record.setNormalFieldValue(t, tables[t].getMem(1));
						return true;
					} else {
						tables[t] = null;
						record.setNormalFieldValue(t, null);
						return false;
					}
				} else {
					//record.setNormalFieldValue(t, null);
					return false;
				}
			} else {
				return false;
			}
		} else {
			record.setNormalFieldValue(t, tables[t].getMem(currents[t]));
			return true;
		}
	}
	
	private Record getRecord() {
		Sequence []tables = this.tables;
		if (tables == null) {
			return null;
		}
		
		int []currents = this.currents;
		int last = tables.length - 1;
		
		Next:
		while (true) {
			if (tables[last] != null) {
				record.setNormalFieldValue(last, tables[last].getMem(currents[last]));
				Record r = new Record(ds, record.getFieldValues());
				if (tables[last].length() < ++currents[last]) {
					tables[last] = cursors[last].fetch(FETCHCOUNT);
					if (tables[last] != null && tables[last].length() > 0) {
						currents[last] = 1;
					} else {
						if (next(last - 1)) {
							cursors[last].reset();
							tables[last] = cursors[last].fetch(FETCHCOUNT);
							currents[last] = 1;
						}
					}
				}
				
				return r;
			}
			
			for (int i = last - 1; i >= 0; --i) {
				if (tables[i] != null) {
					if (isLeft) {
						Record r = new Record(ds);
						for (int j = 0; j <= i; ++j) {
							r.setNormalFieldValue(j, record.getNormalFieldValue(j));
						}
						
						if (next(i)) {
							for (int j = i + 1; j <= last; ++j) {
								cursors[j].reset();
								tables[j] = cursors[j].fetch(FETCHCOUNT);
								if (tables[j] != null && tables[j].length() > 0) {
									record.setNormalFieldValue(j, tables[j].getMem(1));
									currents[j] = 1;
								} else {
									tables[j] = null;
									break;
								}
							}
						}
						
						return r;
					} else {
						if (next(i)) {
							for (int j = i + 1; j <= last; ++j) {
								cursors[j].reset();
								tables[j] = cursors[j].fetch(FETCHCOUNT);
								if (tables[j] != null && tables[j].length() > 0) {
									record.setNormalFieldValue(j, tables[j].getMem(1));
									currents[j] = 1;
								} else {
									tables[j] = null;
									break;
								}
							}
						}
						
						continue Next;
					}
				}
			}
			
			return null;
		}
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		
		init();
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}
		
		ListBase1 mems = newTable.getMems();
		ComputeStack stack = ctx.getComputeStack();
		stack.push(record);
		try {
			for (int i = 0; i < n; ++i) {
				Record r = getRecord();
				if (r != null) {
					mems.add(r);
				} else {
					break;
				}
			}
		} finally {
			stack.pop();
		}

		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		
		init();
		
		ComputeStack stack = ctx.getComputeStack();
		stack.push(record);
		try {
			for (int i = 0; i < n; ++i) {
				Record r = getRecord();
				if (r == null) {
					return i;
				}
			}
		} finally {
			stack.pop();
		}

		return n;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		
		for (int i = 0, count = cursors.length; i < count; ++i) {
			cursors[i].close();
		}

		tables = null;
		currents = null;
		record = null;
		isEnd = true;
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
}
