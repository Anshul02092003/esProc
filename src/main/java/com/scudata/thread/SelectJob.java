package com.scudata.thread;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Sequence;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;
import com.scudata.util.Variant;

/**
 * ����ִ��A.select������
 * @author RunQian
 *
 */
class SelectJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������
	
	private Expression exp; // ���˱��ʽ
	
	private Expression[] fltExps; // ������ʽ����
	private Object[] vals; // ֵ����
	
	private Context ctx; // ����������
	private Sequence result; // �����
	
	public SelectJob(Sequence src, int start, int end, Expression exp, Context ctx) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.exp = exp;
		this.ctx = ctx;
	}
	
	public SelectJob(Sequence src, int start, int end, Expression[] fltExps, Object[] vals, Context ctx) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.fltExps = fltExps;
		this.vals = vals;
		this.ctx = ctx;
	}
	
	public void run() {
		if (fltExps == null) {
			run1();
		} else {
			run2();
		}
	}
	
	private void run1() {
		Sequence src = this.src;
		int end = this.end;
		Expression exp = this.exp;
		Context ctx = this.ctx;

		Sequence result = new Sequence();
		this.result = result;
		ListBase1 mems = src.getMems();
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = src.new Current();
		stack.push(current);

		try {
			for (int i = start; i < end; ++i) {
				current.setCurrent(i);
				Object obj = exp.calculate(ctx);
				if (Variant.isTrue(obj)) {
					result.add(mems.get(i));
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	private void run2() {
		Sequence src = this.src;
		int end = this.end;
		Expression[] fltExps = this.fltExps;
		Object[] vals = this.vals;
		Context ctx = this.ctx;
		int colCount = fltExps.length;
		
		Sequence result = new Sequence();
		this.result = result;
		ListBase1 mems = src.getMems();
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = src.new Current();
		stack.push(current);

		try {
			Next:
			for (int i = start; i < end; ++i) {
				current.setCurrent(i);
				for (int c = 0; c < colCount; ++c) {
					Object flt = fltExps[c].calculate(ctx);
					if (!Variant.isEquals(flt, vals[c])) {
						continue Next;
					}
				}

				result.add(mems.get(i));
			}
		} finally {
			stack.pop();
		}
	}

	public void getResult(Sequence seq) {
		seq.addAll(result);
	}
}
