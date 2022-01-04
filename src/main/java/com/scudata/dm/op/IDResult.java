package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.util.HashUtil;

/**
 * ���ڶ�������������ȡȥ�غ���ֶ�ֵ
 * @author RunQian
 *
 */
public class IDResult implements IResult {
	private Expression[] exps; // ȥ���ֶα��ʽ����
	private int count; // �����Ľ��������ʡ�Ա�������
	private Context ctx; // ����������
	
	private HashUtil hashUtil; // �ṩ��ϣ����Ĺ�ϣ��
	private ListBase1 [][]allGroups; // ��ϣ��
	private Sequence []outs;

	public IDResult(Expression []exps, int count, Context ctx) {
		this.exps = exps;
		this.count = count;
		this.ctx = ctx;
		
		if (count == Integer.MAX_VALUE) {
			count = Env.getDefaultHashCapacity();
		}
		
		hashUtil = new HashUtil(count);
		int fcount = exps.length;
		outs = new Sequence[fcount];
		allGroups = new ListBase1[fcount][];

		for (int i = 0; i < fcount; ++i) {
			outs[i] = new Sequence(count);
			allGroups[i] = new ListBase1[hashUtil.getCapacity()];
		}
	}

	/**
	 * ȡȥ�غ�Ľ��
	 * @return Sequence
	 */
	public Sequence getResultSequence() {
		if (exps.length == 1) {
			return outs[0];
		} else {
			return new Sequence(outs);
		}
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
		while (true) {
			Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
			if (src == null || src.length() == 0) break;
			
			addGroups(src, ctx);
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
		Sequence.Current current = table.new Current();
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

	/**
	 * ��֧�ֲ�������
	 */
	public Object combineResult(Object []results) {
		throw new RuntimeException();
	}
}
