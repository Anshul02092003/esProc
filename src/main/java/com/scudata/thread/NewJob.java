package com.scudata.thread;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;

/**
 * ����ִ��A.new������
 * @author RunQian
 *
 */
class NewJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������
	
	private DataStruct ds; // ��������ݽṹ
	private Expression[] exps; // ������ʽ����
	private String opt; // ѡ��
	private Context ctx; // ����������
	
	private Table result; // �����
	
	public NewJob(Sequence src, int start, int end, DataStruct ds, Expression[] exps, String opt, Context ctx) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.ds = ds;
		this.exps = exps;
		this.opt = opt;
		this.ctx = ctx;
	}
	
	public void run() {
		int start = this.start;
		int end = this.end;
		DataStruct ds = this.ds;
		Context ctx = this.ctx;
		
		Table table = new Table(ds, end - start);
		this.result = table;
		
		int colCount = ds.getFieldCount();
		ListBase1 mems = table.getMems();

		ComputeStack stack = ctx.getComputeStack();
		Current newCurrent = table.new Current();
		stack.push(newCurrent);
		Current current = src.new Current();
		stack.push(current);

		try {
			if (opt == null || opt.indexOf('i') == -1) {
				for (int i = 1; start < end; ++start, ++i) {
					Record r = new Record(ds);
					mems.add(r);

					newCurrent.setCurrent(i);
					current.setCurrent(start);
					for (int c = 0; c < colCount; ++c) {
						r.setNormalFieldValue(c, exps[c].calculate(ctx));
					}
				}
			} else {
				Next:
				for (int i = 1; start < end; ++start) {
					Record r = new Record(ds);
					mems.add(r);
					
					newCurrent.setCurrent(i);
					current.setCurrent(start);
					for (int c = 0; c < colCount; ++c) {
						Object obj = exps[c].calculate(ctx);
						if (obj != null) {
							r.setNormalFieldValue(c, obj);
						} else {
							mems.remove(i); // ����exps�����������²����ļ�¼
							continue Next;
						}
					}
					
					++i;
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
	}

	public void getResult(Table table) {
		table.getMems().addAll(result.getMems());
	}
}
