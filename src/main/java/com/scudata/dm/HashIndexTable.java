package com.scudata.dm;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.Job;
import com.scudata.thread.MultithreadUtil;
import com.scudata.thread.ThreadPool;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * ���ֶ���������
 * @author WangXiaoJun
 *
 */
public class HashIndexTable extends IndexTable {
	// ���ڴ�Ź�ϣ�����Ԫ�أ���ϣֵ��ͬ��Ԫ��������洢
	private static class Entry {
		Object key;
		int seq; // ��Ӧ�ļ�¼������е����
		Entry next;
		
		public Entry(Object key, int seq) {
			this.key = key;
			this.seq = seq;
		}
		
		public Entry(Object key, int seq, Entry next) {
			this.key = key;
			this.seq = seq;
			this.next = next;
		}
	}

	// ���ڶ��̴߳�����ϣ��
	private static class CreateJob extends Job {
		private HashUtil hashUtil;
		private Sequence code;
		private int field;
		private int start; // ��ʼλ�ã�����
		private int end; // ����λ�ã�������
		
		Entry []entries; // ��hashֵ����
		
		public CreateJob(HashUtil hashUtil, Sequence code, int field, int start, int end) {
			this.hashUtil = hashUtil;
			this.code = code;
			this.field = field;
			this.start = start;
			this.end = end;
		}

		public void run() {
			HashUtil hashUtil = this.hashUtil;
			Sequence code = this.code;
			int field = this.field;
			Entry []groups = new Entry[hashUtil.getCapacity()];
			this.entries = groups;
			Object key;
			Record r;

			for (int i = start, end = this.end; i < end; ++i) {
				r = (Record)code.getMem(i);
				key = r.getNormalFieldValue(field);

				int hash = hashUtil.hashCode(key);
				for (Entry entry = groups[hash]; entry != null; entry = entry.next) {
					if (Variant.isEquals(entry.key, key)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(Variant.toString(key) + mm.getMessage("engine.dupKeys"));
					}
				}
				
				groups[hash] = new Entry(key, i, groups[hash]);
			}
		}
	}
	
	private Sequence code; // Դ����ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	protected HashUtil hashUtil; // ���ڼ����ϣֵ
	protected Entry []entries; // ��hashֵ����
	private boolean useMultithread; // ʹ�ö��̴߳�������
	
	public HashIndexTable(int capacity) {
		hashUtil = new HashUtil(capacity);
	}
	
	/**
	 * ������ϣ��
	 * @param capacity ��ϣ������
	 * @param opt ѡ�m���ö��̴߳�����ϣ��
	 */
	public HashIndexTable(int capacity, String opt) {
		hashUtil = new HashUtil(capacity);
		useMultithread = opt != null && opt.indexOf('m') != -1;
	}
	
	private HashIndexTable(Sequence code, HashUtil hashUtil, Entry []entries) {
		this.code = code;
		this.hashUtil = hashUtil;
		this.entries = entries;
	}
	
	/**
	 * ȡ��ϣ������
	 * @return
	 */
	public int getCapacity() {
		return hashUtil.getCapacity();
	}

	/**
	 * ������ϣ��
	 * @param code Դ����
	 */
	public void create(Sequence code) {
		this.code = code;
		HashUtil hashUtil = this.hashUtil;
		Entry []groups = new Entry[hashUtil.getCapacity()];
		this.entries = groups;
		Object key;
		Object r;
		
		for (int i = 1, len = code.length(); i <= len; ++i) {
			r = code.getMem(i);
			if (r instanceof Record) {
				key = ((Record)r).getPKValue();
			} else {
				key = r;
			}

			int hash = hashUtil.hashCode(key);
			for (Entry entry = groups[hash]; entry != null; entry = entry.next) {
				if (Variant.compare(entry.key, key, true) == 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(Variant.toString(key) + mm.getMessage("engine.dupKeys"));
				}
			}
			
			groups[hash] = new Entry(key, i, groups[hash]);
		}
	}

	/**
	 * �ñ��ʽ�ļ���ֵ����������ϣ��
	 * @param code Դ����
	 * @param exp ���ʽ
	 * @param ctx
	 */
	public void create(Sequence code, Expression exp, Context ctx) {
		if (exp == null) {
			create(code);
			return;
		}

		if (code instanceof Table) {
			Table table = (Table)code;
			int f = table.dataStruct().getFieldIndex(exp.toString());
			if (f != -1) {
				create(table, f);
				return;
			}
		}
		
		this.code = code;
		HashUtil hashUtil = this.hashUtil;
		Entry []groups = new Entry[hashUtil.getCapacity()];
		this.entries = groups;
		Object key;

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = code.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = code.length(); i <= len; ++i) {
				current.setCurrent(i);
				key = exp.calculate(ctx);

				int hash = hashUtil.hashCode(key);
				for (Entry entry = groups[hash]; entry != null; entry = entry.next) {
					if (Variant.compare(entry.key, key, true) == 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(Variant.toString(key) + mm.getMessage("engine.dupKeys"));
					}
				}
				
				groups[hash] = new Entry(key, i, groups[hash]);
			}
		} finally {
			stack.pop();
		}
	}
	
	// �ϲ���ϣ��
	private static void combineHashGroups(Entry []result, Entry []entries) {
		int len = result.length;
		for (int i = 0; i < len; ++i) {
			if (result[i] == null) {
				result[i] = entries[i];
			} else if (entries[i] != null) {
				Entry entry = entries[i];
				while (true) {
					// �ȽϹ�ϣֵ��ͬ��Ԫ���Ƿ�ֵҲ��ͬ
					for (Entry resultEntry = result[i]; resultEntry != null; resultEntry = resultEntry.next) {
						if (Variant.isEquals(entry.key, resultEntry.key)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(Variant.toString(entry.key) + mm.getMessage("engine.dupKeys"));
						}
					}
					
					if (entry.next == null) {
						entry.next = result[i];
						result[i] = entries[i];
						break;
					} else {
						entry = entry.next;
					}
				}
			}
		}
	}

	/**
	 * �����е�ָ���ֶ�Ϊ��������ϣ��
	 * @param code Դ����
	 * @param field �ֶ�����
	 */
	public void create(Sequence code, int field) {
		this.code = code;
		int len = code.length();
		if (useMultithread && len > MultithreadUtil.SINGLE_PROSS_COUNT && Env.getParallelNum() > 1) {
			int threadCount = Env.getParallelNum();
			int singleCount = len / threadCount;
			CreateJob []jobs = new CreateJob[threadCount];
			ThreadPool pool = ThreadPool.newInstance(threadCount);

			try {
				for (int i = 0, start = 1; i < threadCount; ++i) {
					if (i + 1 == threadCount) {
						jobs[i] = new CreateJob(hashUtil, code, field, start, len + 1);
					} else {
						jobs[i] = new CreateJob(hashUtil, code, field, start, start + singleCount);
						start += singleCount;
					}
					
					pool.submit(jobs[i]);
				}
				
				for (int i = 0; i < threadCount; ++i) {
					jobs[i].join();
					
					if (entries == null) {
						entries = jobs[i].entries;
					} else {
						combineHashGroups(entries, jobs[i].entries);
					}
				}
			} finally {
				pool.shutdown();
			}
		} else {
			CreateJob job = new CreateJob(hashUtil, code, field, 1, len + 1);
			job.run();
			entries = job.entries;
		}
	}

	/**
	 * �ɼ�����Ԫ�أ��Ҳ������ؿ�
	 * @param key ��ֵ
	 */
	public Object find(Object key) {
		int hash = hashUtil.hashCode(key);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compare(entry.key, key, true) == 0) {
				return code.getMem(entry.seq);
			}
		}
		
		return null; // key not found
	}

	/**
	 * �ɼ�����Ԫ�أ��Ҳ������ؿ�
	 * @param keys ����Ϊ1�ļ�ֵ����
	 */
	public Object find(Object []keys) {
		int hash = hashUtil.hashCode(keys[0]);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compare(entry.key, keys[0], true) == 0) {
				return code.getMem(entry.seq);
			}
		}

		return null; // key not found
	}
	
	/**
	 * �ɼ�����Ԫ����ţ��Ҳ�������-1
	 * @param key ��ֵ
	 */
	public int findSeq(Object key) {
		int hash = hashUtil.hashCode(key);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compare(entry.key, key, true) == 0) {
				return entry.seq;
			}
		}
		
		return -1; // key not found
	}
	
	/**
	 * ���������й��������������������
	 * @param exp ���˱��ʽ
	 * @param ctx
	 * @return Table ���������ļ�¼���ɵ������
	 */
	public Table select(Expression exp, Context ctx) {
		Sequence code = this.code;
		Entry []entries = this.entries;
		int len = code.length();
		
		int capacity = entries.length;
		Entry []resultEntries = new Entry[capacity];
		Table result = new Table(code.dataStruct(), len);
		ListBase1 mems = result.getMems();
		int newLen = 0;
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = code.new Current();
		stack.push(current);
		
		try {
			for (int i = 0; i < capacity; ++i) {
				Entry entry = entries[i];
				while (entry != null) {
					current.setCurrent(entry.seq);
					Object b = exp.calculate(ctx);
					if (Variant.isTrue(b)) {
						mems.add(current.getCurrent());
						newLen++;
						Entry prev = new Entry(entry.key, newLen);
						resultEntries[i] = prev;
						
						for (entry = entry.next; entry != null; entry = entry.next) {
							current.setCurrent(entry.seq);
							b = exp.calculate(ctx);
							if (Variant.isTrue(b)) {
								newLen++;
								mems.add(current.getCurrent());
								prev.next = new Entry(entry.key, newLen);
								prev = prev.next;
							}								
						}
						
						break;
					} else {
						entry = entry.next;
					}
				}
			}
		} finally {
			stack.pop();
		}
		
		result.trimToSize();
		HashIndexTable indexTable = new HashIndexTable(result, hashUtil, resultEntries);
		result.setIndexTable(indexTable);
		return result;
	}
}
