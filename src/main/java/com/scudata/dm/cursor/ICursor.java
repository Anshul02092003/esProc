package com.scudata.dm.cursor;

import java.util.ArrayList;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IResource;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Param;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.Sequence.Current;
import com.scudata.dm.op.GroupxResult;
import com.scudata.dm.op.IDResult;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.TotalResult;
import com.scudata.expression.Expression;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * �α���࣬������Ҫʵ��get��skipOver����
 * @author WangXiaoJun
 *
 */
abstract public class ICursor implements IResource, Operable {
	public static final int MAXSIZE = Integer.MAX_VALUE - 1; // ���fetch�������ڴ�ֵ��ʾȡ����
	public static final long MAXSKIPSIZE = Long.MAX_VALUE; // ���skip�Ĳ������ڴ�ֵ��ʾ��������
	
	public static int INITSIZE = 99999; // ȡ��������ʱ���������л����ĳ�ʼ��С
	public static int FETCHCOUNT = 9999; // �������ÿ�δ��α��ȡ���ݵ�����
	public static final int FETCHCOUNT_M = 999; // ��·�α겢�м���ʱÿһ·��ȡ���ݵ�����

	protected Sequence cache; // �и��Ӳ������ߵ�����peek����˳�Ա��Ž�����Ҫ����ȡ�Ĳ�������
	protected ArrayList<Operation> opList; // ���Ӳ����б�
	protected Context ctx; // �ö��߳��α�ȡ��ʱ��Ҫ���������Ĳ����½������ʽ
	
	protected DataStruct dataStruct; // ��������ݽṹ
	private boolean isDecrease = false; // ���ӵ������Ƿ��ʹ���ݱ��٣�����select
	
	/**
	 * ȡ�α��Ĭ��ȡ����С
	 * @return
	 */
	public static int getFetchCount() {
		return FETCHCOUNT;
	}

	/**
	 * �����α��Ĭ��ȡ����С
	 * @param count
	 */
	public static void setFetchCount(int count) {
		FETCHCOUNT = count;
	}

	
	/**
	 * ���м���ʱ��Ҫ�ı�������
	 * ��������õ��˱��ʽ����Ҫ�������������½������ʽ
	 * �������ش˷���ʱ��Ҫ����һ�¸���ķ���
	 * @param ctx
	 */
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			this.ctx = ctx;
			opList = duplicateOperations(ctx);
		}
	}
	
	/**
	 * ȡ����������
	 * @return
	 */
	public Context getContext() {
		return ctx;
	}
	
	private ArrayList<Operation> duplicateOperations(Context ctx) {
		ArrayList<Operation> opList = this.opList;
		if (opList == null) return null;
				
		ArrayList<Operation> newList = new ArrayList<Operation>(opList.size());
		for (Operation op : opList) {
			newList.add(op.duplicate(ctx));
		}
		
		return newList;
	}
	
	/**
	 * �����α��������
	 * ��·�α���߳�����ʱ��Ҫ����������
	 * @param ctx
	 */
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Ϊ�α긽������
	 * @param op ����
	 * @param ctx ����������
	 */
	public Operable addOperation(Operation op, Context ctx) {
		if (opList == null) {
			opList = new ArrayList<Operation>();
		}
		
		opList.add(op);
		if (op.isDecrease()) {
			isDecrease = true;
		}
		
		if (this.ctx == null) {
			this.ctx = ctx;
		}
		
		// �ֶβ����α�ʱ���ڶ����α��ʱ����ܻ�����������ݻ�����
		if (cache != null) {
			cache = op.process(cache, ctx);
		}
		
		return this;
	}
	
	/**
	 * ���������кϲ���һ�����У����ݽṹ�Ƿ�����������Ƿ����������
	 * ���ںϲ��α���ȡ���õ��Ľ��
	 * @param dest ����
	 * @param src ����
	 * @return Sequence
	 */
	public static Sequence append(Sequence dest, Sequence src) {
		if (src == null || src.length() == 0) return dest;
		
		if (dest instanceof Table) {
			DataStruct ds1 = dest.dataStruct();
			DataStruct ds2 = src.dataStruct();
			if (ds1 == ds2) {
				dest.getMems().addAll(src.getMems());
			} else if (ds1.isCompatible(ds2)) {
				if (ds1 != ds2) {
					for (int i = 1, len = src.length(); i <= len; ++i) {
						Record r = (Record)src.getMem(i);
						r.setDataStruct(ds1);
					}
				}
				
				dest.getMems().addAll(src.getMems());
			} else {
				Sequence seq = new Sequence(dest.length() + src.length());
				seq.getMems().addAll(dest.getMems());
				seq.getMems().addAll(src.getMems());
				return seq;
			}
		} else {
			dest.getMems().addAll(src.getMems());
		}
		
		return dest;
	}
	
	private static Sequence doOperation(Sequence result, ArrayList<Operation> opList, Context ctx) {
		for (Operation op : opList) {
			if (result == null || result.length() == 0) {
				return null;
			}
			
			result = op.process(result, ctx);
		}
		
		return result;
	}
	
	private static Sequence finish(ArrayList<Operation> opList, Context ctx) {
		Sequence result = null;
		for (Operation op : opList) {
			if (result == null || result.length() == 0) {
				result = op.finish(ctx);
			} else {
				result = op.process(result, ctx);
				Sequence tmp = op.finish(ctx);
				if (tmp != null) {
					if (result != null) {
						result = append(result, tmp);
					} else {
						result = tmp;
					}
				}
			}
		}
		
		return result;
	}
	
	public synchronized Sequence peek(int n) {
		ArrayList<Operation> opList = this.opList;
		if (opList == null) {
			if (cache == null) {
				cache = get(n);
			} else if (cache.length() < n) {
				cache = append(cache, get(n - cache.length()));
			} else if (cache.length() > n) {
				return cache.get(1, n + 1);
			}
			
			return cache;
		}
		
		int size;
		if (n > FETCHCOUNT && n < MAXSIZE) {
			size = n;
		} else {
			size = FETCHCOUNT;
		}
		
		while (cache == null || cache.length() < n) {
			Sequence cur = get(size);
			if (cur == null || cur.length() == 0) {
				Sequence tmp = finish(opList, ctx);
				if (tmp != null) {
					if (cache == null) {
						cache = tmp;
					} else {
						cache = append(cache, tmp);
					}
				}

				return cache;
			} else {
				cur = doOperation(cur, opList, ctx);
				if (cache == null) {
					cache = cur;
				} else if (cur != null) {
					cache = append(cache, cur);
				}
			}
		}
		
		if (cache.length() == n) {
			return cache;
		} else {
			if (cache instanceof Table) {
				Table table = new Table(cache.dataStruct(), n);
				table.getMems().addAll(cache.getMems(), n);
				return table;
			} else {
				Sequence seq = new Sequence(n);
				seq.getMems().addAll(cache.getMems(), n);
				return seq;
			}
		}
	}

	/**
	 * ���ȡָ�������ļ�¼��ȡ�ļ�¼�����ܲ�����n
	 * @param n ����
	 * @return Sequence
	 */
	public Sequence fuzzyFetch(int n) {
		if (cache == null) {
			Sequence result = null;
			ArrayList<Operation> opList = this.opList;
			
			do {
				Sequence cur = get(n);
				if (cur != null) {
					int len = cur.length();
					if (opList != null) {
						cur = doOperation(cur, opList, ctx);
						if (result == null) {
							result = cur;
						} else if (cur != null) {
							result = append(result, cur);
						}
						
						if (len < n) {
							Sequence tmp = finish(opList, ctx);
							if (tmp != null) {
								if (result == null) {
									result = tmp;
								} else {
									result = append(result, tmp);
								}
							}
							
							close();
						}
					} else {
						if (result == null) {
							result = cur;
						} else {
							result = append(result, cur);
						}
						
						if (len < n) {
							close();
						}
					}
				} else {
					if (opList != null) {
						Sequence tmp = finish(opList, ctx);
						if (tmp != null) {
							if (result == null) {
								result = tmp;
							} else {
								result = append(result, tmp);
							}
						}
					}

					close();
					return result;
				}
			} while (result == null || result.length() < n);
			
			return result;
		} else {
			Sequence result = cache;
			cache = null;
			return result;
		}
	}
	
	/**
	 * ����ʣ��ļ�¼���ر��α�
	 * @return Sequence
	 */
	public Sequence fetch() {
		return fetch(MAXSIZE);
	}

	/**
	 * ȡָ�������ļ�¼
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	public synchronized Sequence fetch(int n) {
		if (n < 1) {
			return null;
		}
		
		ArrayList<Operation> opList = this.opList;
		Sequence result = cache;
		if (opList == null) {
			if (result == null) {
				result = get(n);
				if (result == null || result.length() < n) {
					close();
				}
				
				return result;
			} else if (result.length() > n) {
				return result.split(1, n);
			} else if (result.length() == n) {
				cache = null;
				return result;
			} else {
				cache = null;
				result = append(result, get(n - result.length()));
				if (result == null || result.length() < n) {
					close();
				}
				
				return result;
			}
		}
		
		// ����������˵���¼�ֲ���ȡ��������ʵ����ȡ
		int size;
		if ((n > FETCHCOUNT || !isDecrease) && n < MAXSIZE) {
			size = n;
		} else {
			size = FETCHCOUNT;
		}
		
		while (result == null || result.length() < n) {
			Sequence cur = get(size);
			if (cur == null) {
				Sequence tmp = finish(opList, ctx);
				if (tmp != null) {
					if (result == null) {
						result = tmp;
					} else {
						result = append(result, tmp);
					}
				}
				
				close();
				return result;
			} else {
				int len = cur.length();
				cur = doOperation(cur, opList, ctx);
				if (result == null) {
					result = cur;
				} else if (cur != null) {
					result = append(result, cur);
				}
				
				if (len < size) {
					Sequence tmp = finish(opList, ctx);
					if (tmp != null) {
						if (result == null) {
							result = tmp;
						} else {
							result = append(result, tmp);
						}
					}
					
					if (result == null || result.length() < n) {
						close();
						return result;
					}
				}
			}
		}
		
		if (result.length() == n) {
			cache = null;
			return result;
		} else {
			cache = result.split(n + 1);
			return result;
		}
	}
	
	/**
	 * ��ָ�����ʽȡn������
	 * @param exps ���ʽ����
	 * @param n ����
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression[] exps, int n, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		int keyCount = exps.length; 
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(1);
		int index = 2;
		int count = 0;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							if (count + index >= n) {
								break End;
							} else {
								for (int j = 0; j < keyCount; ++j) {
									keys[j] = exps[j].calculate(ctx);
								}
							}
						}
					}
				}

				if (newTable == null) {
					newTable = data;
				} else {
					newTable.getMems().addAll(data.getMems());
				}
				count = newTable.length();
				
				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = data.new Current();
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}

		return newTable;
	}

	/**
	 * ��ָ�����ʽȡһ������
	 * @param exps ���ʽ����
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression[] exps, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		int keyCount = exps.length; 
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(1);
		int index = 2;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							break End;
						}
					}
				}

				if (newTable == null) {
					newTable = data;
				} else {
					newTable.getMems().addAll(data.getMems());
				}

				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = data.new Current();
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}

		return newTable;
	}

	/**
	 * ��ָ�����ʽȡһ������
	 * @param exp ���ʽ
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression exp, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(1);
		int index = 2;

		try {
			Object key = exp.calculate(ctx);
			if (key instanceof Boolean) {
				End:
				while (true) {
					for (int len = data.length(); index <= len; ++index) {
						current.setCurrent(index);
						if (Variant.isTrue(exp.calculate(ctx))) {
							break End;
						}
					}

					if (newTable == null) {
						newTable = data;
					} else {
						newTable.getMems().addAll(data.getMems());
					}

					data = fuzzyFetch(FETCHCOUNT);
					if (data == null) break;

					index = 1;
					stack.pop();
					current = data.new Current();
					stack.push(current);
				}
			} else {
				End:
				while (true) {
					for (int len = data.length(); index <= len; ++index) {
						current.setCurrent(index);
						if (!Variant.isEquals(key, exp.calculate(ctx))) {
							break End;
						}
					}

					if (newTable == null) {
						newTable = data;
					} else {
						newTable.getMems().addAll(data.getMems());
					}

					data = fuzzyFetch(FETCHCOUNT);
					if (data == null) break;

					index = 1;
					stack.pop();
					current = data.new Current();
					stack.push(current);
				}
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}
		
		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ�������̵߳���
	 * @param field �ֶ����
	 * @return Sequence
	 */
	public Sequence fetchGroup(int field) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		ListBase1 mems = data.getMems();
		Record r = (Record)mems.get(1);
		Sequence newTable = null;
		Object key = r.getNormalFieldValue(field);
		int index = 2;

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (Record)mems.get(index);;
				if (!Variant.isEquals(key, r.getNormalFieldValue(field))) {
					break End;
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ�������̵߳���
	 * @param fields �ֶ��������
	 * @return Sequence
	 */
	public Sequence fetchGroup(int []fields) {
		int keyCount = fields.length;
		if (keyCount == 1) {
			return fetchGroup(fields[0]);
		}
		
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		ListBase1 mems = data.getMems();
		Record r = (Record)mems.get(1);
		int index = 2;
		
		Object []keys = new Object[keyCount];
		for (int i = 0; i < keyCount; ++i) {
			keys[i] = r.getNormalFieldValue(fields[i]);
		}

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (Record)mems.get(index);
				for (int i = 0; i < keyCount; ++i) {
					if (!Variant.isEquals(keys[i], r.getNormalFieldValue(fields[i]))) {
						break End;
					}
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ���������limit��ȡ����һ��Ҳ�᷵�أ�������̵߳���
	 * @param fields �ֶ��������
	 * @param limit ����¼��
	 * @return Sequence
	 */
	public Sequence fetchGroup(int []fields, int limit) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		int keyCount = fields.length;
		Sequence newTable = null;
		ListBase1 mems = data.getMems();
		Record r = (Record)mems.get(1);
		int index = 2;
		int count = 0;
		
		Object []keys = new Object[keyCount];
		for (int i = 0; i < keyCount; ++i) {
			keys[i] = r.getNormalFieldValue(fields[i]);
		}

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (Record)mems.get(index);
				for (int i = 0; i < keyCount; ++i) {
					if (!Variant.isEquals(keys[i], r.getNormalFieldValue(fields[i]))) {
						break End;
					}
				}
				if (count + index >= limit + 1) {
					break End;
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			count = newTable.length();
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}
	
	/**
	 * ����һ������
	 * @param exps
	 * @param ctx
	 * @return
	 */
	public synchronized int skipGroup(Expression[] exps, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return 0;
		}

		int keyCount = exps.length;
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = data.new Current();
		stack.push(current);
		current.setCurrent(1);

		int count = 1;
		int index = 2;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index, ++count) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							break End;
						}
					}
				}

				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = data.new Current();
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() > index) {
			cache = data.split(index);
		}

		return count;
	}

	/**
	 * �������м�¼
	 * @return ʵ�������ļ�¼��
	 */
	public long skip() {
		return skip(MAXSKIPSIZE);
	}
	
	/**
	 * ����ָ����¼��
	 * @param n ��¼��
	 * @return long ʵ�������ļ�¼��
	 */
	public synchronized long skip(long n) {
		if (opList == null) {
			if (cache == null) {
				long count = skipOver(n);
				if (count < n) {
					close();
				}
				
				return count;
			} else {
				int len = cache.length();
				if (len == n) {
					cache = null;
					return n;
				} else if (len > n) {
					cache.split(1, (int)n);
					return n;
				} else {
					cache = null;
					long count = n + skipOver(n - len);
					if (count < n) {
						close();
					}
					
					return count;
				}
			}
		} else {
			long total = 0;
			while (n > 0) {
				Sequence seq;
				if (n > FETCHCOUNT) {
					seq = fetch(FETCHCOUNT);
				} else {
					seq = fetch((int)n);
				}
				
				if (seq == null || seq.length() == 0) {
					close();
					break;
				}
				
				total += seq.length();
				n -= seq.length();
			}
			
			return total;
		}
	}

	/**
	 * �ر��α�
	 */
	public void close() {
		cache = null;
		
		if (opList != null) {
			finish(opList, ctx);
		}
	}
	
	/**
	 * ȡ��¼��������Ҫʵ�ִ˷���
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	protected abstract Sequence get(int n);

	/**
	 * ����ָ����¼����������Ҫʵ�ִ˷���
	 * @param n ��¼��
	 * @return long
	 */
	protected abstract long skipOver(long n);
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		return false;
	}
	
	/**
	 * ���ؽ�������ݽṹ
	 * @return DataStruct
	 */
	public DataStruct getDataStruct() {
		return dataStruct;
	}
	
	/**
	 * ���ý�������ݽṹ
	 * @param ds ���ݽṹ
	 */
	public void setDataStruct(DataStruct ds) {
		dataStruct = ds;
	}
	
	// �����α�������ֶΣ���������򷵻�null
	public String[] getSortFields() {
		return null;
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx) {
		IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
		groups.push(this);
		return groups.getResultTable();
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param groupCount ���������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx, int groupCount) {
		if (groupCount < 1 || exps == null || exps.length == 0) {
			return groups(exps, names, calcExps, calcNames, opt, ctx);
		} else if (opt != null && opt.indexOf('n') != -1) {
			IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
			groups.setGroupCount(groupCount);
			groups.push(this);
			return groups.getResultTable();
		} else {
			return CursorUtil.fuzzyGroups(this, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		}
	}

	/**
	 * ���α�������������
	 * @param exps ������ʽ����
	 * @param names	�����ֶ�������
	 * @param calcExps ���ܱ��ʽ	����
	 * @param calcNames	�����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity	�ڴ��б��������������
	 * @return ICursor �������α�
	 */
	public ICursor groupx(Expression[] exps, String []names, 
			Expression[] calcExps, String []calcNames, String opt, Context ctx, int capacity) {
		if (opt != null && opt.indexOf('n') != -1) {
			return CursorUtil.groupx_n(this, exps, names, calcExps, calcNames, ctx, capacity);
		}
		
		GroupxResult groupx = new GroupxResult(exps, names, calcExps, calcNames, opt, ctx, capacity);
		while (true) {
			Sequence src = fetch(INITSIZE);
			if (src == null || src.length() == 0) break;
			
			groupx.push(src, ctx);
		}
		
		return groupx.getResultCursor();
	}

	/**
	 * ��ÿ�����ʽ���й�ϣȥ�أ�����count����ֵͬ
	 * @param exps ���ʽ����
	 * @param count ����
	 * @param ctx ����������
	 * @return Sequence �������е����У����countΪ1��������
	 */
	public Sequence id(Expression []exps, int count, Context ctx) {
		IDResult id = new IDResult(exps, count, ctx);
		id.push(this);
		return id.getResultSequence();
	}

	/**
	 * ���������α�
	 * @param exp �������ʽ
	 * @param initVal ��ʼֵ
	 * @param c �������ʽ��Ϊtrue��ֹͣ
	 * @param ctx ����������
	 * @return �������
	 */
	public Object iterator(Expression exp, Object initVal, Expression c, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Param param = ctx.getIterateParam();
		Object oldVal = param.getValue();
		param.setValue(initVal);
		
		try {
			while (true) {
				// ���α���ȡ��һ�����ݡ�
				Sequence src = fuzzyFetch(FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				Current current = src.new Current();
				stack.push(current);
				try {
					if (c == null) {
						for (int i = 1, size = src.length(); i <= size; ++i) {
							current.setCurrent(i);
							initVal = exp.calculate(ctx);
							param.setValue(initVal);
						}
					} else {
						for (int i = 1, size = src.length(); i <= size; ++i) {
							current.setCurrent(i);
							Object obj = c.calculate(ctx);
							
							// �������Ϊ���򷵻�
							if (obj instanceof Boolean && ((Boolean)obj).booleanValue()) {
								return initVal;
							}
							
							initVal = exp.calculate(ctx);
							param.setValue(initVal);
						}
					}
				} finally {
					stack.pop();
				}
			}
		} finally {
			param.setValue(oldVal);
		}
		
		return initVal;
	}

	/**
	 * ���α�����������
	 * @param cursor �α�
	 * @param exps �����ֶα��ʽ����
	 * @param ctx ����������
	 * @param capacity �ڴ����ܹ�����ļ�¼�������û���������Զ�����һ��
	 * @param opt ѡ�� 0��null�����
	 * @return �ź�����α�
	 */
	public ICursor sortx(Expression[] exps, Context ctx, int capacity, String opt) {
		return CursorUtil.sortx(this, exps, ctx, capacity, opt);
	}

	/**
	 * ������������ֶ�ֵ��ͬ�ļ�¼��ֵ��ͬ��ͬ��
	 * ��ֵ��ͬ�ļ�¼���浽һ����ʱ�ļ���Ȼ��ÿ����ʱ�ļ���������
	 * @param exps ������ʽ
	 * @param gexp ����ʽ
	 * @param ctx ����������
	 * @param opt ѡ��
	 * @return �ź�����α�
	 */
	public ICursor sortx(Expression[] exps, Expression gexp, Context ctx, String opt) {
		return CursorUtil.sortx(this, exps, gexp, ctx, opt);
	}

	/**
	 * ���α���л���
	 * @param calcExps ���ܱ��ʽ����
	 * @param ctx ����������
	 * @return ���ֻ��һ�����ܱ��ʽ���ػ��ܽ�������򷵻ػ��ܽ�����ɵ�����
	 */
	public Object total(Expression[] calcExps, Context ctx) {
		TotalResult total = new TotalResult(calcExps, ctx);
		total.push(this);
		return total.result();
	}
}
