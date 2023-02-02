package com.scudata.dm.op;

import java.util.Comparator;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.Env;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Sequence;
import com.scudata.dm.comparator.BaseComparator;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.fn.gather.ICount.ICountBitSet;
import com.scudata.expression.fn.gather.ICount.ICountPositionSet;
import com.scudata.util.HashUtil;

/**
 * ���ڶ�������������ȡȥ�غ���ֶ�ֵ
 * @author RunQian
 *
 */
public class IDResult implements IResult {
	
	private Expression[] exps; // ȥ���ֶα��ʽ����
	private int count; // �����Ľ��������ʡ�Ա�������
	private String opt;
	private Context ctx; // ����������
	
	private boolean optB;
	private boolean optN;
	
	private HashUtil hashUtil; // �ṩ��ϣ����Ĺ�ϣ��
	private ListBase1 [][]allGroups; // ��ϣ��
	private Sequence []outs;
	
	private ICountBitSet bitSet;
	private ICountPositionSet posSet;
	
	public IDResult(Expression []exps, int count, String opt, Context ctx) {
		this.exps = exps;
		this.count = count;
		this.opt = opt;
		this.ctx = ctx;
		
		if (opt != null) {
			 optB = opt.indexOf('b') != -1;
			 optN = opt.indexOf('n') != -1;
		}
		
		if (count == Integer.MAX_VALUE) {
			count = Env.getDefaultHashCapacity();
		}
		
		int fcount = exps.length;
		if (optB) {
			bitSet = new ICountBitSet();
		} else if (optN) {
			posSet = new ICountPositionSet();
		} else {
			hashUtil = new HashUtil(count);
			allGroups = new ListBase1[fcount][];

			for (int i = 0; i < fcount; ++i) {
				allGroups[i] = new ListBase1[hashUtil.getCapacity()];
			}
		}

		outs = new Sequence[fcount];
		for (int i = 0; i < fcount; ++i) {
			outs[i] = new Sequence(count);
		}
	}

	/**
	 * ȡȥ�غ�Ľ��
	 * @return Sequence
	 */
	public Sequence getResultSequence() {
		Sequence result;
		if (exps.length == 1) {
			if (opt == null || opt.indexOf('u') == -1) {
				Comparator<Object> comparator = new BaseComparator();
				outs[0].getMems().sort(comparator);
			}
			
			result = outs[0];
		} else {
			result = new Sequence(outs);
		}
		
		if (opt != null && opt.indexOf('0') != -1) {
			result.deleteNull(false);
		}
		return result;
	}
	
	/**
	 * �������ͽ���ʱ����
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
	}
	
	/**
	 * ȡȥ�غ�Ľ��
	 * @return Object
	 */
	public Object result() {
		return getResultSequence();
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		if (table == null || table.length() == 0) return;
		
		addGroups(table, ctx);
	}

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param cursor �α�����
	 */
	public void push(ICursor cursor) {
		Context ctx = this.ctx;
		if (optB) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_b(src, ctx);
			}
		} else if (optN) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_n(src, ctx);
			}
		} else {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups(src, ctx);
			}
		}
	}
	
	private void addGroups(Sequence table, Context ctx) {
		final int INIT_GROUPSIZE = HashUtil.getInitGroupSize();
		HashUtil hashUtil = this.hashUtil;
		ListBase1 [][]allGroups = this.allGroups;
		Sequence []outs = this.outs;
		
		Expression[] exps = this.exps;
		int count = this.count;
		int fcount = exps.length;

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(table);
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean sign = false;
				for (int f = 0; f < fcount; ++f) {
					Sequence out = outs[f];
					if (out.length() < count) {
						sign = true;
						Object val = exps[f].calculate(ctx);
						int hash = hashUtil.hashCode(val);
						ListBase1 []groups = allGroups[f];
						if (groups[hash] == null) {
							groups[hash] = new ListBase1(INIT_GROUPSIZE);
							groups[hash].add(val);
							out.add(val);
						} else {
							int index = groups[hash].binarySearch(val);
							if (index < 1) {
								groups[hash].add(-index, val);
								out.add(val);
							}
						}
					}
				}
				
				if (!sign) {
					break;
				}
			}
		} finally {
			stack.pop();
		}
	}

	private void addGroups_b(Sequence table, Context ctx) {
		ICountBitSet set = this.bitSet;
		Sequence []outs = this.outs;
		
		Expression[] exps = this.exps;
		int count = this.count;
		int fcount = exps.length;

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(table);
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean sign = false;
				for (int f = 0; f < fcount; ++f) {
					Sequence out = outs[f];
					if (out.length() < count) {
						sign = true;
						Object val = exps[f].calculate(ctx);
						if (val instanceof Number && set.add(((Number)val).intValue())) {
							out.add(val);
						}
					}
				}
				
				if (!sign) {
					break;
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	private void addGroups_n(Sequence table, Context ctx) {
		ICountPositionSet set = this.posSet;
		Sequence []outs = this.outs;
		
		Expression[] exps = this.exps;
		int count = this.count;
		int fcount = exps.length;

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(table);
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean sign = false;
				for (int f = 0; f < fcount; ++f) {
					Sequence out = outs[f];
					if (out.length() < count) {
						sign = true;
						Object val = exps[f].calculate(ctx);
						if (val instanceof Number && set.add(((Number)val).intValue())) {
							out.add(val);
						}
					}
				}
				
				if (!sign) {
					break;
				}
			}
		} finally {
			stack.pop();
		}
	}
	/**
	 * ��֧�ֲ�������
	 */
	public Object combineResult(Object []results) {
		throw new RuntimeException();
	}
}
