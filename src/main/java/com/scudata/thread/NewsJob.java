package com.scudata.thread;

import java.util.ArrayList;
import java.util.HashSet;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ����ִ��A.news������
 * @author RunQian
 *
 */
class NewsJob extends Job {
	private Sequence src; // Դ����
	private int start; // ��ʼλ�ã�����
	private int end; // ����λ�ã�������
	
	private Expression gexp;
	private Expression[] exps; // ������ʽ����
	private DataStruct ds; // ��������ݽṹ
	private String opt; // ѡ��
	private Context ctx; // ����������
	
	private Table result; // �����
	
	public NewsJob(Sequence src, int start, int end, 
			Expression gexp, Expression[] exps, DataStruct ds, String opt, Context ctx) {
		this.src = src;
		this.start = start;
		this.end = end;
		this.gexp = gexp;
		this.exps = exps;
		this.ds = ds;
		this.opt = opt;
		this.ctx = ctx;
	}
	
	public void run() {
		int start = this.start;
		int end = this.end;
		Expression gexp = this.gexp;
		Expression[] exps = this.exps;
		DataStruct ds = this.ds;
		Context ctx = this.ctx;
		
		Table result = new Table(ds, (end - start) * 2);
		this.result = result;
		IArray resultMems = result.getMems();
		int fcount = ds.getFieldCount();
		int resultSeq = 1;
		
		boolean isLeft = opt != null && opt.indexOf('1') != -1;
		Sequence ns = null;
		if (isLeft) {
			// ��������������ҳ����ʽ������X���ֶΣ�����һ����ֵ�ļ�¼��XȡֵΪnullʱ��������¼ѹջ
			ArrayList<String> fieldList = new ArrayList<String>();
			for (Expression exp : exps) {
				exp.getUsedFields(ctx, fieldList);
			}
			
			Object obj = src.ifn();
			DataStruct oldDs = null;
			if (obj instanceof BaseRecord) {
				oldDs = ((BaseRecord)obj).dataStruct();
			}
			
			HashSet<String> set = new HashSet<String>();
			for (String name : fieldList) {
				if (oldDs == null || oldDs.getFieldIndex(name) == -1) {
					set.add(name);
				}
			}
			
			ns = new Sequence(1);
			int count = set.size();
			if (count == 0) {
				ns.add(null);
			} else {
				String []names = new String[set.size()];
				set.toArray(names);
				Record nullRecord = new Record(new DataStruct(names));
				ns.add(nullRecord);
			}
		}
		
		// �Ȱ��²��������ѹջ����ֹ���ò���Դ���
		ComputeStack stack = ctx.getComputeStack();
		Current resultCurrent = new Current(result);
		Current current = new Current(src);
		stack.push(resultCurrent);
		stack.push(current);

		try {
			for (; start < end; ++start) {
				current.setCurrent(start);
				Object obj = gexp.calculate(ctx);
				Sequence seq = null;
				
				if (obj instanceof Sequence) {
					seq = (Sequence)obj;
				} else if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n > 0) {
						seq = new Sequence(1, n);
					}
				} else if (obj instanceof BaseRecord) {
					try {
						stack.push((BaseRecord)obj);
						resultCurrent.setCurrent(resultSeq);
						Record r = new Record(ds);
						resultMems.add(r);
						resultSeq++;

						for (int f = 0; f < fcount; ++f) {
							r.setNormalFieldValue(f, exps[f].calculate(ctx));
						}
					} finally {
						stack.pop();
					}
					
					continue;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("news" + mm.getMessage("function.paramTypeError"));
				}
				
				if (seq == null || seq.length() == 0) {
					if (isLeft) {
						seq =ns;
					} else {
						continue;
					}
				}
				
				try {
					Current curCurrent = new Current(seq);
					stack.push(curCurrent);
					int curLen = seq.length();
					
					for (int m = 1; m <= curLen; ++m, ++resultSeq) {
						resultCurrent.setCurrent(resultSeq);
						curCurrent.setCurrent(m);
						Record r = new Record(ds);
						resultMems.add(r);

						for (int f = 0; f < fcount; ++f) {
							r.setNormalFieldValue(f, exps[f].calculate(ctx));
						}
					}
				} finally {
					stack.pop();
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
	}

	public Table getResult() {
		return result;
	}
}
