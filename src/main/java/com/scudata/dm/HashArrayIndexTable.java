package com.scudata.dm;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.Job;
import com.scudata.thread.MultithreadUtil;
import com.scudata.thread.ThreadPool;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * �ɶ��ֶ����������Ĺ�ϣ��
 * @author WangXiaoJun
 *
 */
class HashArrayIndexTable extends IndexTable {
	// ���ڴ�Ź�ϣ�����Ԫ�أ���ϣֵ��ͬ��Ԫ��������洢
	private static class Entry {
		Object []keys;
		int seq; // ��Ӧ�ļ�¼������е����
		Entry next;
		
		public Entry(Object []keys, int seq) {
			this.keys = keys;
			this.seq = seq;
		}
		
		public Entry(Object []keys, int seq, Entry next) {
			this.keys = keys;
			this.seq = seq;
			this.next = next;
		}
	}
	
	// ���ڶ��̴߳�����ϣ��
	private static class CreateJob extends Job {
		private HashUtil hashUtil;
		private Sequence code;
		private int []fields;
		private int start; // ��ʼλ�ã�����
		private int end; // ����λ�ã�������
		private boolean checkDupKey;// �����ֵ
		
		Entry []entries; // ��hashֵ����
		
		public CreateJob(HashUtil hashUtil, Sequence code, int []fields, int start, int end, boolean checkDupKey) {
			this.hashUtil = hashUtil;
			this.code = code;
			this.fields = fields;
			this.start = start;
			this.end = end;
			this.checkDupKey = checkDupKey;
		}

		public void run() {
			HashUtil hashUtil = this.hashUtil;
			Sequence code = this.code;
			int []fields = this.fields;
			Entry []groups = new Entry[hashUtil.getCapacity()];
			this.entries = groups;

			Object []keys;
			int keyCount = fields.length;
			BaseRecord r;

			for (int i = start, end = this.end; i < end; ++i) {
				r = (BaseRecord)code.getMem(i);
				keys = new Object[keyCount];
				for (int c = 0; c < keyCount; ++c) {
					keys[c] = r.getNormalFieldValue(fields[c]);;
				}

				int hash = hashUtil.hashCode(keys, keyCount);
				for (Entry entry = groups[hash]; entry != null; entry = entry.next) {
					if (checkDupKey && Variant.compareArrays(entry.keys, keys, keyCount) == 0) {
						MessageManager mm = EngineMessage.get();
						String str = "[";
						for (int k = 0; k < keyCount; ++k) {
							if (k != 0) {
								str += ",";
							}
							str += Variant.toString(keys[k]);
						}
						
						str += "]";
						throw new RQException(str + mm.getMessage("engine.dupKeys"));
					}
				}
				
				groups[hash] = new Entry(keys, i, groups[hash]);
			}
		}
	}
	
	private Sequence code; // Դ����ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	protected HashUtil hashUtil; // ���ڼ����ϣֵ
	protected Entry[] entries; // ��hashֵ����
	private boolean useMultithread; // ʹ�ö��̴߳�������
	
	public HashArrayIndexTable(int capacity) {
		hashUtil = new HashUtil(capacity);
	}
	
	/**
	 * ������ϣ��
	 * @param capacity ��ϣ������
	 * @param opt ѡ�m���ö��̴߳�����ϣ��
	 */
	public HashArrayIndexTable(int capacity, String opt) {
		hashUtil = new HashUtil(capacity);
		useMultithread = opt != null && opt.indexOf('m') != -1;
	}
	
	private HashArrayIndexTable(Sequence code, HashUtil hashUtil, Entry []entries) {
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
	 * @param exps �ֶα��ʽ����
	 * @param ctx ����������
	 */
	public void create(Sequence code, Expression []exps, Context ctx) {
		this.code = code;
		HashUtil hashUtil = this.hashUtil;
		Entry []groups = new Entry[hashUtil.getCapacity()];
		this.entries = groups;
		Object []keys;
		int keyCount = exps.length;

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(code);
		stack.push(current);

		try {
			for (int i = 1, len = code.length(); i <= len; ++i) {
				current.setCurrent(i);
				keys = new Object[keyCount];
				for (int c = 0; c < keyCount; ++c) {
					keys[c] = exps[c].calculate(ctx);
				}

				int hash = hashUtil.hashCode(keys, keyCount);
				for (Entry entry = groups[hash]; entry != null; entry = entry.next) {
					if (Variant.compareArrays(entry.keys, keys, keyCount) == 0) {
						MessageManager mm = EngineMessage.get();
						String str = "[";
						for (int k = 0; k < keyCount; ++k) {
							if (k != 0) {
								str += ",";
							}
							str += Variant.toString(keys[k]);
						}
						
						str += "]";
						throw new RQException(str + mm.getMessage("engine.dupKeys"));
					}
				}
				
				groups[hash] = new Entry(keys, i, groups[hash]);
			}
		} finally {
			stack.pop();
		}
	}
	
	// �ϲ���ϣ��
	private static void combineHashGroups(Entry []result, Entry []entries, boolean checkDupKey) {
		int len = result.length;
		for (int i = 0; i < len; ++i) {
			if (result[i] == null) {
				result[i] = entries[i];
			} else if (entries[i] != null) {
				Entry entry = entries[i];
				while (true) {
					// �ȽϹ�ϣֵ��ͬ��Ԫ���Ƿ�ֵҲ��ͬ
					for (Entry resultEntry = result[i]; resultEntry != null; resultEntry = resultEntry.next) {
						if (Variant.compareArrays(entry.keys, resultEntry.keys) == 0) {
							Object []keys = entry.keys;
							MessageManager mm = EngineMessage.get();
							String str = "[";
							for (int k = 0; k < keys.length; ++k) {
								if (k != 0) {
									str += ",";
								}
								str += Variant.toString(keys[k]);
							}
							
							str += "]";
							throw new RQException(str + mm.getMessage("engine.dupKeys"));
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
	 * @param fields �ֶ�������ɵ�����
	 */
	public void create(Sequence code, int []fields) {
		create(code , fields, true);
	}
	
	/**
	 * �����е�ָ���ֶ�Ϊ��������ϣ��
	 * @param code Դ����
	 * @param fields �ֶ�������ɵ�����
	 * @param checkDupKey ������ֵ
	 */
	public void create(Sequence code, int []fields, boolean checkDupKey) {
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
						jobs[i] = new CreateJob(hashUtil, code, fields, start, len + 1, checkDupKey);
					} else {
						jobs[i] = new CreateJob(hashUtil, code, fields, start, start + singleCount, checkDupKey);
						start += singleCount;
					}
					
					pool.submit(jobs[i]);
				}
				
				for (int i = 0; i < threadCount; ++i) {
					jobs[i].join();
					
					if (entries == null) {
						entries = jobs[i].entries;
					} else {
						combineHashGroups(entries, jobs[i].entries, checkDupKey);
					}
				}
			} finally {
				pool.shutdown();
			}
		} else {
			CreateJob job = new CreateJob(hashUtil, code, fields, 1, len + 1, checkDupKey);
			job.run();
			entries = job.entries;
		}
	}

	public Object find(Object key) {
		throw new RuntimeException();
	}

	/**
	 * �ɼ�����Ԫ�أ��Ҳ������ؿ�
	 * @param keys ��ֵ����
	 */
	public Object find(Object []keys) {
		int count = keys.length;
		int hash = hashUtil.hashCode(keys, count);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compareArrays(entry.keys, keys) == 0) {
				return code.getMem(entry.seq);
			}
		}

		return null; // key not found
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
		IArray mems = result.getMems();
		int newLen = 0;
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(code);
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
						Entry prev = new Entry(entry.keys, newLen);
						resultEntries[i] = prev;
						
						for (entry = entry.next; entry != null; entry = entry.next) {
							current.setCurrent(entry.seq);
							b = exp.calculate(ctx);
							if (Variant.isTrue(b)) {
								newLen++;
								mems.add(current.getCurrent());
								prev.next = new Entry(entry.keys, newLen);
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
		HashArrayIndexTable indexTable = new HashArrayIndexTable(result, hashUtil, resultEntries);
		result.setIndexTable(indexTable);
		return result;
	}
	
	public int findPos(Object key) {
		throw new RuntimeException();
	}
	
	/**
	 * �ɼ�����Ԫ��λ��
	 * @param keys ��ֵ����
	 */
	public int findPos(Object []keys) {
		int count = keys.length;
		int hash = hashUtil.hashCode(keys, count);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compareArrays(entry.keys, keys) == 0) {
				return entry.seq;
			}
		}

		return 0; // key not found
	}
	
	public int[] findAllPos(IArray key) {
		throw new RuntimeException();
	}

	public int[] findAllPos(IArray[] keys) {
		Entry []entries = this.entries;
		HashUtil hashUtil = this.hashUtil;
		int keyCount = keys.length;
		int len = keys[0].size();
		int []pos = new int[len + 1];
		
		for (int i = 1; i <= len; ++i) {
			int hash = hashUtil.hashCode(keys, i, keyCount);
			
			Next:
			for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
				Object []keyValues = entry.keys;
				for (int k = 0; k < keyCount; ++k) {
					if (!keys[k].isEquals(i, keyValues[k])) {
						continue Next;
					}
				}
				
				pos[i] =  entry.seq;
				break;
			}
		}
		
		return pos;
	}

	public int[] findAllPos(IArray key, BoolArray signArray) {
		throw new RuntimeException();
	}

	public int[] findAllPos(IArray[] keys, BoolArray signArray) {
		Entry []entries = this.entries;
		HashUtil hashUtil = this.hashUtil;
		int keyCount = keys.length;
		int len = keys[0].size();
		int []pos = new int[len + 1];
		
		for (int i = 1; i <= len; ++i) {
			if (signArray.isFalse(i)) {
				continue;
			}
			int hash = hashUtil.hashCode(keys, i, keyCount);
			
			Next:
			for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
				Object []keyValues = entry.keys;
				for (int k = 0; k < keyCount; ++k) {
					if (!keys[k].isEquals(i, keyValues[k])) {
						continue Next;
					}
				}
				
				pos[i] =  entry.seq;
				break;
			}
		}
		
		return pos;
	}
}
