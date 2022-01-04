package com.scudata.thread;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Sequence;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;

/**
 * ����ִ��A.()������
 * @author RunQian
 *
 */
class CalcJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������
	
	private Expression exp; // ������ʽ
	private Context ctx; // ����������
	private Sequence result; // ���ڴ�Ž����
	
	public CalcJob(Sequence src, int start, int end, Expression exp, Context ctx, Sequence result) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.exp = exp;
		this.ctx = ctx;
		this.result = result;
	}
	
	public void run() {
		int end = this.end;
		Expression exp = this.exp;
		Context ctx = this.ctx;
		
		ListBase1 resultMems = result.getMems();
		ComputeStack stack = ctx.getComputeStack();
		Current current = src.new Current();
		stack.push(current);

		try {
			for (int i = start; i < end; ++i) {
				current.setCurrent(i);
				resultMems.set(i, exp.calculate(ctx));
			}
		} finally {
			stack.pop();
		}
	}
}
