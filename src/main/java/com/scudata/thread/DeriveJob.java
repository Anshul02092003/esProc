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
 * ����ִ��A.derive������
 * @author RunQian
 *
 */
class DeriveJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������
	
	private DataStruct newDs; // ��������ݽṹ
	private Expression[] exps; // ������ʽ����
	private String opt; // ѡ��
	private Context ctx; // ����������
	
	private Table result; // �����
	
	public DeriveJob(Sequence src, int start, int end, DataStruct newDs, 
			Expression[] exps, String opt, Context ctx) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.newDs = newDs;
		this.exps = exps;
		this.opt = opt;
		this.ctx = ctx;
	}
	
	public void run() {
		Sequence src = this.src;
		int start = this.start;
		int end = this.end;
		DataStruct newDs = this.newDs;
		Context ctx = this.ctx;
		
		Table table = new Table(newDs, end - start);
		this.result = table;
		
		int colCount = exps.length;
		int newColCount = newDs.getFieldCount();
		int oldColCount = newColCount - colCount;
		
		ListBase1 srcMems = src.getMems();
		ListBase1 mems = table.getMems();

		ComputeStack stack = ctx.getComputeStack();
		Current newCurrent = table.new Current();
		stack.push(newCurrent);
		Current current = src.new Current();
		stack.push(current);

		try {
			if (opt == null || opt.indexOf('i') == -1) {
				for (int i = 1; start < end; ++start, ++i) {
					Record r = new Record(newDs);
					mems.add(r);
					r.set((Record)srcMems.get(start));

					newCurrent.setCurrent(i);
					current.setCurrent(start);
					for (int c = 0; c < colCount; ++c) {
						r.setNormalFieldValue(c + oldColCount, exps[c].calculate(ctx));
					}
				}
			} else {
				Next:
				for (int i = 1; start < end; ++start) {
					Record r = new Record(newDs);
					mems.add(r);
					r.set((Record)srcMems.get(start));

					newCurrent.setCurrent(i);
					current.setCurrent(start);
					for (int c = 0; c < colCount; ++c) {
						Object obj = exps[c].calculate(ctx);
						if (obj != null) {
							r.setNormalFieldValue(c + oldColCount, obj);
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
