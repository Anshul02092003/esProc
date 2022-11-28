package com.scudata.dm;

import com.scudata.expression.Expression;
import com.scudata.util.Variant;

/**
 * ��������鲢��������Ҫ���ҵ�ֵ�����������
 * @author RunQian
 *
 */
public class MergeIndexTable extends IndexTable {
	private Sequence code; // ά���������ֶ�����
	private Sequence values; // ά��Ĺ����ֶ�ֵ
	private int currentSeq = 1; // ��ǰ����������ţ�findʱ��ӵ�ǰ��ſ�ʼ������
	
	public MergeIndexTable(Sequence code, Expression []exps, Context ctx) {
		this.code = code;
		if (exps == null || exps.length == 0) {
			values = code;
		} else if (exps.length == 1) {
			values = code.calc(exps[0], ctx);
		} else {
			int fcount = exps.length;
			int len = code.length();
			Sequence sequence = new Sequence(len);
			values = sequence;

			ComputeStack stack = ctx.getComputeStack();
			Sequence.Current current = sequence.new Current();
			stack.push(current);

			try {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					Object []vals = new Object[fcount];
					for (int f = 0; f < fcount; ++f) {
						vals[f] = exps[f].calculate(ctx);
					}

					sequence.add(vals);
				}
			} finally {
				stack.pop();
			}			
		}
	}

	public Object find(Object key) {
		Sequence values = this.values;
		int len = values.length();
		for (int i = currentSeq; i <= len; ++i) {
			int cmp = Variant.compare(values.getMem(i), key);
			if (cmp == 0) {
				// �ҵ���ȵģ�����currentSeqΪ��ǰ��ţ��´β��Ҵ��⿪ʼ�������
				currentSeq = i;
				return code.getMem(i);
			} else if (cmp > 0) {
				// û���ҵ���ȵģ�����currentSeqΪ��ǰ��ţ��´β��Ҵ��⿪ʼ�������
				currentSeq = i;
				return null;
			}
		}
		
		currentSeq = len + 1;
		return null;
	}

	public Object find(Object []keys) {
		if (keys.length == 1) {
			return find(keys[0]);
		}
		
		Sequence values = this.values;
		int len = values.length();
		for (int i = currentSeq; i <= len; ++i) {
			int cmp = Variant.compareArrays((Object [])values.getMem(i), keys);
			if (cmp == 0) {
				// �ҵ���ȵģ�����currentSeqΪ��ǰ��ţ��´β��Ҵ��⿪ʼ�������
				currentSeq = i;
				return code.getMem(i);
			} else if (cmp > 0) {
				// û���ҵ���ȵģ�����currentSeqΪ��ǰ��ţ��´β��Ҵ��⿪ʼ�������
				currentSeq = i;
				return null;
			}
		}
		
		currentSeq = len + 1;
		return null;
	}
}
