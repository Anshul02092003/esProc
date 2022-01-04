package com.scudata.dm.cursor;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.util.Variant;

/**
 * ��������α꣬�α�������Ѱ������ֶ����򣬷���ʱֻ������ڵļ�¼�ȽϷ����ֶ��Ƿ���ͬ
 * ��������ʵ�������麯��cs.groupx()�Ķ��η��飬����������ʱ�������������ֶ�����д������ʱ�ļ���
 * �ٶ���ʱ�ļ��������ֶι鲢�γ������α�
 * @author RunQian
 *
 */
public class GroupmCursor extends ICursor {
	private ICursor cursor; // ���ݰ������ֶ�������α�
	private Expression []exps; // ������ʽ
	private Expression []newExps; // ���ܱ��ʽ
	private Node []gathers; // ���ܱ��ʽ��Ӧ�Ļ��ܺ���
	private DataStruct newDs; // ��������ݽṹ

	private Sequence data; // ���α���ȡ��������
	private int currentIndex; // ��ǰҪ��������ݵ����

	/**
	 * ������������α�
	 * @param cursor ���ݰ������ֶ�������α�
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param newExps ���ܱ��ʽ����
	 * @param newNames �����ֶ�������
	 * @param ctx ����������
	 */
	public GroupmCursor(ICursor cursor, Expression[] exps, String []names,
					   Expression[] newExps, String []newNames, Context ctx) {
		this.cursor = cursor;
		this.exps = exps;
		this.newExps = newExps;
		this.ctx = ctx;

		int count = exps.length;
		int newCount = newExps == null ? 0 : newExps.length;

		// ���ʡ���˽�����ֶ������ݱ��ʽ�Զ�����
		if (names == null) {
			names = new String[count];
		}
		
		for (int i = 0; i < count; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				names[i] = exps[i].getFieldName();
			}
		}

		if (newNames == null) {
			newNames = new String[newCount];
		}
		
		for (int i = 0; i < newCount; ++i) {
			if (newNames[i] == null || newNames[i].length() == 0) {
				newNames[i] = newExps[i].getFieldName();
			}
		}

		String []totalNames = new String[count + newCount];
		System.arraycopy(names, 0, totalNames, 0, count);
		System.arraycopy(newNames, 0, totalNames, count, newCount);
		newDs = new DataStruct(totalNames);
		newDs.setPrimary(names);
		gathers = Sequence.prepareGatherMethods(newExps, ctx);
		
		setDataStruct(newDs);
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			cursor.resetContext(ctx);
			exps = Operation.dupExpressions(exps, ctx);
			newExps = Operation.dupExpressions(newExps, ctx);
			gathers = Sequence.prepareGatherMethods(newExps, ctx);
			super.resetContext(ctx);
		}
	}

	private Sequence getData() {
		if (data != null) {
			return data;
		}

		data = cursor.fetch(FETCHCOUNT);
		if (data == null || data.length() == 0) {
			return null;
		} else {
			currentIndex = 1;
			return data;
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (cursor == null || n < 1) {
			return null;
		}

		Sequence data = getData();
		if (data == null) {
			return null;
		}

		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(newDs, INITSIZE);
		} else {
			newTable = new Table(newDs, n);
		}

		Context ctx = this.ctx;
		Expression[] exps = this.exps;
		Node []gathers = this.gathers;
		int keyCount = exps.length;
		int valCount = gathers == null ? 0 : gathers.length;
		Object []keys = new Object[keyCount];
		Object []nextKeys = new Object[keyCount];

		int index = currentIndex;
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(index++);

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			Record cur = newTable.newLast(keys);
			for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
				Object val = gathers[v].gather(ctx);
				cur.setNormalFieldValue(f, val);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						nextKeys[k] = exps[k].calculate(ctx);
					}

					if (Variant.compareArrays(keys, nextKeys) == 0) {
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(cur.getNormalFieldValue(f), ctx);
							cur.setNormalFieldValue(f, val);
						}
					} else {
						if (newTable.length() == n) {
							this.currentIndex = index;
							break End;
						}

						Object []tmp = keys;
						keys = nextKeys;
						nextKeys = tmp;
						cur = newTable.newLast(keys);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(ctx);
							cur.setNormalFieldValue(f, val);
						}
					}
				}

				this.data = null;
				data = getData();
				if (data == null) break;

				index = 1;
				stack.pop();
				current = data.new Current();
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		newTable.finishGather(gathers);
		return newTable;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (cursor == null || n < 1) return 0;

		Sequence data = getData();
		if (data == null) return 0;

		Context ctx = this.ctx;
		Expression[] exps = this.exps;
		int keyCount = exps.length;
		Object []keys = new Object[keyCount];
		Object []nextKeys = new Object[keyCount];

		long count = 1;
		int index = currentIndex;
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(index++);

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						nextKeys[k] = exps[k].calculate(ctx);
					}

					if (Variant.compareArrays(keys, nextKeys) != 0) {
						if (count == n) {
							this.currentIndex = index;
							break End;
						}

						Object []tmp = keys;
						keys = nextKeys;
						nextKeys = tmp;
						count++;
					}
				}

				this.data = null;
				data = getData();
				if (data == null) break;

				index = 1;
				stack.pop();
				current = data.new Current();
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursor != null) {
			cursor.close();
			cursor = null;
			exps = null;
			gathers = null;
			newDs = null;
			data = null;
		}
	}
}
