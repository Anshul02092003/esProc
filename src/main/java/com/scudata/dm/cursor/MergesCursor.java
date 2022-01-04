package com.scudata.dm.cursor;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.util.Variant;

/**
 * ����α�������鲢�����γɵ��α�
 * ÿ���α��ڵ����ݾ��Ѿ����ض����ʽ�ź�˳��
 * ��󣬸��α����ʱ�����ض��Ĺ���鲢�����
 * c----conj    ��·�α����ΰ����ʽ������exps�����������
 * u----union   �����������������ظ����ݡ�ͬһ�α��ڵ������ظ������ظ���
 * i----isect   ��·�α�Ĺ�ͬ���ݲ������
 * d----diff    ��һ�����У���������û�е������
 * @author RunQian
 *
 */
public class MergesCursor extends ICursor {
	/** ��ɶ�·�α�����顣�α��������Ѿ���exps�Ź���(��֧����������) **/
	private ICursor []cursors;
	/** ��·�α�ı��ʽ��������˱��ʽ����(��֧����������)����������α��Ѿ����ɴ˱��ʽ������� **/
	private Expression[] exps;
	/** �����ʱ��� **/
	private Context ctx;
	/** ��·�α꣬�α�鲢���� **/
	private char type = 'c'; // c:conj  u:union  i:isect  d:diff x:xor
	/** ���ݻ������������·�α������ **/
	private Sequence []tables;	// ���ݻ����������ڻ������yo
	/** ��ǰ���ݸ��ݱ��ʽ�ļ����� **/
	private Object [][]values;
	/** ��ǰ����������ڸ��Ի����������� **/
	private int []seqs;	
	/** �������е�ǰԪ�أ���С������������ **/
	private int []items; 		// ���еĵ�ǰԪ�ش�С���������
	
	/** ��ǰԪ�ص����� **/
	private int []ranks; // ��ǰԪ�ص�����0��1��-1��union��isect��diff��xorʹ��
	/** �Ƿ�ȡ����� **/
	private boolean isEnd = false;
	/** null����Сֵ�������ֵ **/
	private boolean isNullMin = true; // null�Ƿ���Сֵ
	private Object NULL = this;

	private Context []ctxs; // ÿ�������exps���Լ��������ģ�ÿ����ȡ�����ݺ���ѹջ
	private Sequence.Current []currents;
	private Expression[][] dupExps;
	
	/**
	 * ��·�α깹�캯��
	 * @param cursors	��ɶ�·�α������	
	 * @param exps		������ʽ��cursors�е�ÿ����Ա�����ݸñ��ʽ�����Ź���
	 * @param ctx		�����ı�����
	 */
	public MergesCursor(ICursor []cursors, Expression[] exps, Context ctx) {
		this(cursors, exps, null, ctx);
	}
	
	/**
	 * 
	 * ��·�α깹�캯��
	 * @param cursors	��ɶ�·�α������	
	 * @param exps		������ʽ��cursors�е�ÿ����Ա�����ݸñ��ʽ�����Ź���
	 * @param opt		�α�鲢��������c��u��i��d�ĸ��������⡣0�����������������棩
	 * 			c----conj    ��·�α����ΰ����ʽ������exps�����������
	 *			u----union   �����������������ظ����ݡ�ͬһ�α��ڵ������ظ������ظ���
	 *			i----isect   �����α궼�е����������
	 *			d----diff    ��һ�����У���������û�е������
	 *			x----xor 	 �����α��������
	 *			0			 ������������һ�������档��0����ʾ������Ϊnullʱ��Ϊ���ֵ������Ϊ��Сֵ��
	 * @param ctx		�����ı�����
	 */
	public MergesCursor(ICursor []cursors, Expression[] exps, String opt, Context ctx) {
		this.cursors = cursors;
		this.exps = exps;
		this.ctx = ctx;
		
		setDataStruct(cursors[0].getDataStruct());
		
		if (opt != null) {
			if (opt.indexOf('u') !=-1) {
				type = 'u';
			} else if (opt.indexOf('i') !=-1) {
				type = 'i';
			} else if (opt.indexOf('d') !=-1) {
				type = 'd';
			} else if (opt.indexOf('x') !=-1) {
				type = 'x';
			}
			
			if (opt.indexOf('0') !=-1) {
				isNullMin = false;
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

	/**
	 * ѡ����һ��Ҫ����ȡ�����ݡ���û�����ݿɶ����򷵻�false;
	 *		�ո�ȡ�����ݵ��α꣬�л���ǰ���ݡ������α껺���е����ݾ�����ȡ�����ٴλ���һ�����ݡ�
	 *		�ոձ��л������ݣ���Ա��ʽ�����㡣
	 *		���ݸ����α굱ǰ���ݶԱ��ʽ�ļ���������������ѡ����һ��Ҫ��ȡ������
	 * 
	 * @return	true	��ǰ������ȡ������
	 * 			false	��ǰ�����ݿ�ȡ
	 */
	private boolean popTop() {
		int []items = this.items;
		int count = items.length;
		int item = items[0];
		Sequence table = tables[item];

		// �ո�ȡ�����ݵ��α꣬�����������ˣ��Ͷ�ȡ�µ����ݡ�
		int next = seqs[item] + 1;
		if (next > table.length()) {
			ComputeStack stack = ctxs[item].getComputeStack();
			stack.pop();
			
			table = cursors[item].fuzzyFetch(FETCHCOUNT_M);
			if (table == null || table.length() == 0) {
				for (int j = 1; j < count; ++j) {
					items[j - 1] = items[j];
				}

				tables[item] = null;
				values[item] = null;
				items[count - 1] = -1;

				return items[0] != -1;
			}

			currents[item] = table.new Current(1);
			stack.push(currents[item]);
			tables[item] = table;
			next = 1;
		}

		// �л��굱ǰ���ݣ���Ա��ʽ�����㡣
		seqs[item] = next;
		currents[item].setCurrent(next);
		calc(dupExps[item], ctxs[item], values[item]);
		
		// ѡ����һ��Ҫ��ȡ�����ݡ�
		topChange(values, items);
		return true;
	}

	/**
	 * �ų���·�α��е��ظ����ݡ�
	 * 		
	 * @return	���ؾ������غ�ȡ�õ�����
	 */
	private Object popRepeated() {
		Sequence []tables = this.tables;
		Object [][]values = this.values;
		int []seqs = this.seqs;
		int []ranks = this.ranks;
		int count = tables.length;
		Object r = NULL;
		
		for (int i = 0; i < count; ++i) {
			if (ranks[i] == 0) {
				if (r == NULL) r = tables[i].getMem(seqs[i]);
				
				int next = seqs[i] + 1;
				if (next > tables[i].length()) {
					ComputeStack stack = ctxs[i].getComputeStack();
					stack.pop();
					
					tables[i] = cursors[i].fuzzyFetch(FETCHCOUNT_M);
					if (tables[i] != null && tables[i].length() > 0) {
						currents[i] = tables[i].new Current(1);
						stack.push(currents[i]);

						calc(dupExps[i], ctxs[i], values[i]);
						seqs[i] = 1;
					} else {
						tables[i] = null;
						values[i] = null;
						ranks[i] = -1;
					}
				} else {
					seqs[i] = next;
					currents[i].setCurrent(next);
					calc(dupExps[i], ctxs[i], values[i]);
				}
			}
		}

		for (int i = 0; i < count; ++i) {
			if (ranks[i] != -1) {
				ranks[i] = 0;
				for (int j = 0; j < i; ++j) {
					if (ranks[j] == 0) {
						int cmp = compareArrays(values[i], values[j]);
						if (cmp < 0) {
							ranks[j] = 1;
							for (++j; j < i; ++j) {
								if (ranks[j] == 0) {
									ranks[j] = 1;
								}
							}
						} else if (cmp > 0) {
							ranks[i] = 1;
						}
	
						break;
					}
				}
			}
		}
		
		return r;
	}
	
	/**
	 * �ų���·�α��������ݡ�
	 * 		
	 * @return	���ؾ������غ�ȡ�õ�����
	 */
	private Object popXor() {
		Sequence []tables = this.tables;
		Object [][]values = this.values;
		int []seqs = this.seqs;
		int []ranks = this.ranks;
		int count = tables.length;
		Object r;
		
		while (true) {
			r = NULL;
			int xorCount = 0;//��Сֵ�ĸ���
			for (int i = 0; i < count; ++i) {
				if (ranks[i] == 0) {
					if (r == NULL) 
						r = tables[i].getMem(seqs[i]);
					xorCount++;
					
					int next = seqs[i] + 1;
					if (next > tables[i].length()) {
						ComputeStack stack = ctxs[i].getComputeStack();
						stack.pop();

						tables[i] = cursors[i].fuzzyFetch(FETCHCOUNT_M);
						if (tables[i] != null && tables[i].length() > 0) {
							currents[i] = tables[i].new Current(1);
							stack.push(currents[i]);

							calc(dupExps[i], ctxs[i], values[i]);
							seqs[i] = 1;
						} else {
							tables[i] = null;
							values[i] = null;
							ranks[i] = -1;
						}
					} else {
						seqs[i] = next;
						currents[i].setCurrent(next);
						calc(dupExps[i], ctxs[i], values[i]);
					}
				}
			}
			if (r == NULL)
				return r;
			//��������
			for (int i = 0; i < count; ++i) {
				if (ranks[i] != -1) {
					ranks[i] = 0;
					for (int j = 0; j < i; ++j) {
						if (ranks[j] == 0) {
							int cmp = compareArrays(values[i], values[j]);
							if (cmp < 0) {
								ranks[j] = 1;
								for (++j; j < i; ++j) {
									if (ranks[j] == 0) {
										ranks[j] = 1;
									}
								}
							} else if (cmp > 0) {
								ranks[i] = 1;
							}
		
							break;
						}
					}
				}
			}
		
			//�����Сֵ���ֵĸ����ǵ���������Է�����
			if (xorCount % 2 == 1 )
				break;
		}
		return r;
	}
	
	/**
	 * ѡ����·�α���һ��Ҫ��ȡ�����ݡ�������
	 * 
	 * @param values	�����α굱ǰ���ݸ��ݱ��ʽ�ļ�����
	 * @param items		�����α굱ǰ���ݣ��ڻ������е�����
	 */
	private void topChange(Object [][]values, int []items) {
		int item = items[0];
		Object []obj = values[item];
		if (items[1] == -1 || compareArrays(obj, values[items[1]]) <= 0) {
			return;
		}

		int low = 2;
		int high = values.length - 1;
		while (low <= high) {
			int mid = (low + high) >> 1;
			if (items[mid] == -1) {
				high = mid - 1;
			} else {
				int cmp = compareArrays(obj, values[items[mid]]);
				if (cmp < 0) {
					high = mid - 1;
				} else if (cmp > 0) {
					low = mid + 1;
				} else {
					System.arraycopy(items, 1, items, 0, mid - 1);
					items[mid - 1] = item;
					return; // key found
				}
			}
		}

		// key not found
		System.arraycopy(items, 1, items, 0, low - 1);
		items[low - 1] = item;
	}
	
	private static void calc(Expression []exps, Context ctx, Object []outValues) {
		for (int i = 0, len = exps.length; i < len; ++i) {
			outValues[i] = exps[i].calculate(ctx);
		}
	}
	
	/**
	 * �������α�Ļ�����
	 * 		������������������ֱ�ӷ��ء�
	 */
	private void getData() {
		if (tables != null) return;

		ICursor []cursors = this.cursors;
		Expression[] exps = this.exps;
		Context ctx = this.ctx;

		int tcount = cursors.length;
		tables = new Sequence[tcount];
		values = new Object[tcount][];
		seqs = new int[tcount];
		
		ctxs = new Context[tcount];
		currents = new Sequence.Current[tcount];
		dupExps = new Expression[tcount][];
		
		if (type == 'c') {
			items = new int[tcount]; // ���еĵ�ǰԪ�ش�С���������
			for (int i = 0; i < tcount; ++i) {
				ctxs[i] = ctx.newComputeContext();
				dupExps[i] = Operation.dupExpressions(exps, ctxs[i]);
				Sequence table = cursors[i].fuzzyFetch(FETCHCOUNT_M);
				if (table != null && table.length() > 0) {
					Object []curValues = new Object[exps.length];
					currents[i] = table.new Current(1);
					ctxs[i].getComputeStack().push(currents[i]);
					calc(dupExps[i], ctxs[i], curValues);
					
					tables[i] = table;
					values[i] = curValues;
					seqs[i] = 1;
					items[i] = i;

					for (int j = 0; j < i; ++j) {
						if (items[j] == -1) {
							items[j] = i;
							items[i] = -1;
							break;
						} else if (compareArrays(curValues, values[items[j]]) < 0) {
							for (int k = i; k > j; --k) {
								items[k] = items[k - 1];
							}

							items[j] = i;
							break;
						}
					}
				} else {
					items[i] = -1;
				}
			}
		} else {
			ranks = new int[tcount]; // ���еĵ�ǰԪ�ص�����
			for (int i = 0; i < tcount; ++i) {
				ctxs[i] = ctx.newComputeContext();
				dupExps[i] = Operation.dupExpressions(exps, ctxs[i]);
				Sequence table = cursors[i].fuzzyFetch(FETCHCOUNT_M);
				if (table != null && table.length() > 0) {
					Object []curValues = new Object[exps.length];
					currents[i] = table.new Current(1);
					ctxs[i].getComputeStack().push(currents[i]);
					calc(dupExps[i], ctxs[i], curValues);

					tables[i] = table;
					values[i] = curValues;
					seqs[i] = 1;
					ranks[i] = 0;

					for (int j = 0; j < i; ++j) {
						if (ranks[j] == 0) {
							int cmp = compareArrays(curValues, values[j]);
							if (cmp < 0) {
								ranks[j] = 1;
								for (++j; j < i; ++j) {
									if (ranks[j] == 0) {
										ranks[j] = 1;
									}
								}
							} else if (cmp > 0) {
								ranks[i] = 1;
							}

							break;
						}
					}
				} else {
					ranks[i] = -1;
				}
			}
		}
	}

	/**
	 * uid����ģʽ�µ�ȡ��
	 * 		
	 * @param n	Ҫȡ��������
	 * @return	����ȡ�����
	 */
	private Sequence get_uid(int n) {
		Sequence table;
		if (n > INITSIZE) {
			table = new Sequence(INITSIZE);
		} else {
			table = new Sequence(n);
		}

		if (type == 'u') {	// u--unionȥ�غϲ���·�α�����
			for (int i = 0; i < n; ++i) {
				// ����ȥ��ȡ���������ų��ظ�����
				Object r = popRepeated();
				if (r != NULL) {
					table.add(r);
				} else {
					break;
				}
			}
		} else if (type == 'i') {	// i--isect�������·�α�Ĺ�ͬ����
			int []ranks = this.ranks;
			int tcount = tables.length;

			Next:
			for (; n != 0;) {
				for (int t = 0; t < tcount; ++t) {
					if (ranks[t] != 0) {
						if (popRepeated() == NULL) {
							break Next;
						} else {
							continue Next;
						}
					}
				}
				
				Object r = popRepeated();
				table.add(r);
				--n;
			}
		} else if (type == 'x') {	// x--xor�����·�α��������Ľ��
			for (int i = 0; i < n; ++i) {
				Object r = popXor();
				if (r != NULL) {
					table.add(r);
				} else {
					break;
				}
			}
		} else { // d--diff	�������һ·�α���е�����
			int []ranks = this.ranks;
			int tcount = tables.length;

			Next:
			for (; n != 0;) {
				if (ranks[0] == 1) {
					if (popRepeated() == NULL) {
						break Next;
					} else {
						continue Next;
					}
				} else if (ranks[0] == -1) {
					break Next;
				}
				
				for (int t = 1; t < tcount; ++t) {
					if (ranks[t] == 0) {
						popRepeated();
						continue Next;
					}
				}
				
				Object r = popRepeated();
				table.add(r);
				--n;
			}			
		}
		
		if (table.length() == 0) {
			return null;
		} else {
			return table;
		}
	}
	
	private long skip_uid(long n) {
		if (type == 'u') {
			for (long i = 0; i < n; ++i) {
				Object r = popRepeated();
				if (r == NULL) {
					return i;
				}
			}
		} else if (type == 'i') {
			int []ranks = this.ranks;
			int tcount = tables.length;
			
			Next:
			for (long i = 0; i < n;) {
				for (int t = 0; t < tcount; ++t) {
					if (ranks[t] != 0) {
						if (popRepeated() == NULL) {
							return i;
						} else {
							continue Next;
						}
					}
				}
				
				popRepeated();
				++i;
			}			
		} else { // 'd'
			int []ranks = this.ranks;
			int tcount = tables.length;

			Next:
			for (long i = 0; i < n;) {
				if (ranks[0] == 1) {
					if (popRepeated() == NULL) {
						return i;
					} else {
						continue Next;
					}
				} else if (ranks[0] == -1) {
					return i;
				}
				
				for (int t = 1; t < tcount; ++t) {
					if (ranks[t] == 0) {
						popRepeated();
						continue Next;
					}
				}
				
				popRepeated();
				++i;
			}			
		}
		
		return n;
	}

	/**
	 * ȡ��ָ������������
	 * 		������������������ж���ȡ���١�
	 * 		uid����ȡ����ʽ����get_ui����ʵ�֡�
	 * 		����������Ҫ����Ϊc��ʽ��ȡ�����̡��������У�������ķ�ʽȡ�����ݡ�
	 * @param	n	Ҫȡ��������
	 */
	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		
		try {
			// ��仺����
			getData();
	
			// uid����ģʽ�µ�ȡ��
			if (type != 'c') return get_uid(n);
			
			int []items = this.items;
			if (items[0] == -1) {
				return null;
			}
	
			// ���������ݻ�����
			Sequence []tables = this.tables;
			int []seqs = this.seqs;
			Sequence table;
			if (n > INITSIZE) {
				table = new Sequence(INITSIZE);
			} else {
				table = new Sequence(n);
			}
	
			// ѭ��ȡ������仺������ѭ�������жԸ�·�α��ȡ�����������鲢��
			for (int i = 0; i < n; ++i) {
				int item = items[0];
				Object r = tables[item].getMem(seqs[item]);
				table.add(r);
	
				if (!popTop()) {
					break;
				}
			}
	
			return table;
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		
		try {
			getData();
	
			if (type != 'c') return skip_uid(n);
	
			int []items = this.items;
			if (items[0] == -1) {
				return 0;
			}
	
			for (long i = 0; i < n; ++i) {
				if (!popTop()) {
					return i + 1;
				}
			}
	
			return n;
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursors != null) {
			for (int i = 0, count = cursors.length; i < count; ++i) {
				cursors[i].close();
			}

			tables = null;
			values = null;
			seqs = null;
			items = null;
			isEnd = true;
		}
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
	
	/**
	 * ��·�α��ڶԱ��������ݵĴ�С
	 * ����isNullMin����ͬ�ıȽ�
	 */
	private int compareArrays(Object []o1, Object []o2) {
		if (isNullMin) {
			return Variant.compareArrays(o1, o2);
		} else {
			return Variant.compareArrays_0(o1, o2);
		}
	}
}
