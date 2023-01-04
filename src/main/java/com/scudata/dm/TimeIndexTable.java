package com.scudata.dm;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * ����ʱ����ֶεĹ�ϣ�����������һ���ֶ�Ϊʱ�����ʱ�����������Ƚ��бȽϣ�����ȡǰ�������
 * @author WangXiaoJun
 *
 */
public class TimeIndexTable extends IndexTable {
	private Sequence code; // Դ����ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	private HashUtil hashUtil; // ���ڼ����ϣֵ
	
	// ������ʱ���֮�����������hashֵ���飬��ϣֵ��ͬ�İ����н���������
	// �б��е�ֵΪ���飬��ŵ��ǣ���ֵ+ʱ���ֵ+��¼���
	private ListBase1[] entries;
	private int totalKeyCount;
	
	/**
	 * ������ϣ��
	 * @param code Sequence ά��
	 * @param fields int[] �����ֶ����������һ���ֶ�Ϊʱ���
	 * @param capacity ��ϣ������
	 */
	public TimeIndexTable(Sequence code, int []fields, int capacity) {
		HashUtil hashUtil = new HashUtil(capacity);
		this.code = code;
		this.hashUtil = hashUtil;
		
		ListBase1 []groups = new ListBase1[hashUtil.getCapacity()];
		this.entries = groups;
		final int INIT_GROUPSIZE = HashUtil.getInitGroupSize();

		int totalKeyCount = fields.length;
		int keyCount = totalKeyCount - 1;
		this.totalKeyCount = totalKeyCount;
		int count = totalKeyCount + 1;

		for (int i = 1, len = code.length(); i <= len; ++i) {
			BaseRecord r = (BaseRecord)code.getMem(i);
			Object []keys = new Object[count];
			for (int c = 0; c < totalKeyCount; ++c) {
				keys[c] = r.getNormalFieldValue(fields[c]);
			}
			
			keys[totalKeyCount] = i;
			int hash = hashUtil.hashCode(keys, keyCount);
			if (groups[hash] == null) {
				groups[hash] = new ListBase1(INIT_GROUPSIZE);
				groups[hash].add(keys);
			} else {
				int index = HashUtil.bsearch_a(groups[hash], keys, totalKeyCount);
				if (index < 1) {
					groups[hash].add(-index, keys);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.dupKeys"));
				}
			}
		}
	}
	
	private TimeIndexTable(Sequence code, HashUtil hashUtil, ListBase1[] entries, int totalKeyCount) {
		this.code = code;
		this.hashUtil = hashUtil;
		this.entries = entries;
		this.totalKeyCount = totalKeyCount;
	}
	
	/**
	 * ȡ��ϣ������
	 * @return
	 */
	public int getCapacity() {
		return hashUtil.getCapacity();
	}
	
	public Object find(Object key) {
		// ����ʱû�ṩʱ���ֶΣ�ȡ���µ�
		int hash = hashUtil.hashCode(key);
		ListBase1 table = entries[hash];
		if (table == null) {
			return null;
		}
		
		int index = HashUtil.bsearch_a(table, key);
		if (index > 0) {
			for (int i = index + 1, size = table.size; i <= size; ++i) {
				Object []r = (Object[])table.get(i);
				if (Variant.isEquals(r[0], key)) {
					index = i;
				} else {
					break;
				}
			}
			
			Object []r = (Object[])table.get(index);
			return code.getMem((Integer)r[totalKeyCount]);
		} else {
			return null;
		}
	}

	/**
	 * �ɼ�����Ԫ�أ��Ҳ������ؿ�
	 * @param keys ��ֵ����
	 */
	public Object find(Object []keys) {
		int count = keys.length;
		if (count == totalKeyCount) {
			int hash = hashUtil.hashCode(keys, count - 1);
			ListBase1 table = entries[hash];
			if (table == null) {
				return null;
			}

			int index = HashUtil.bsearch_a(table, keys, count);
			if (index > 0) {
				Object []r = (Object[])table.get(index);
				return code.getMem((Integer)r[count]);
			} else {
				index = -index - 1;
				if (index > 0) {
					// ���ʱ���û����ȵģ���ȡǰ�������
					Object []r = (Object[])table.get(index);
					if (Variant.compareArrays(r, keys, count - 1) == 0) {
						return code.getMem((Integer)r[count]);
					}
				}
				
				return null; // key not found
			}
		} else {
			// ����ʱû�ṩʱ���ֶΣ�ȡ���µ�
			int hash = hashUtil.hashCode(keys, count);
			ListBase1 table = entries[hash];
			if (table == null) {
				return null;
			}
			
			int index = HashUtil.bsearch_a(table, keys, count);
			if (index > 0) {
				for (int i = index + 1, size = table.size; i <= size; ++i) {
					Object []r = (Object[])table.get(i);
					if (Variant.compareArrays(r, keys, count) == 0) {
						index = i;
					} else {
						break;
					}
				}
				
				Object []r = (Object[])table.get(index);
				return code.getMem((Integer)r[totalKeyCount]);
			} else {
				return null;
			}
		}
	}

	/**
	 * ���������й��������������������
	 * @param exp ���˱��ʽ
	 * @param ctx
	 * @return Table ���������ļ�¼���ɵ������
	 */
	public Table select(Expression exp, Context ctx) {
		Sequence code = this.code;
		ListBase1 []entries = this.entries;
		int len = code.length();
		
		int capacity = entries.length;
		ListBase1 []resultEntries = new ListBase1[capacity];
		Table result = new Table(code.dataStruct(), len);
		IArray mems = result.getMems();
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(code);
		stack.push(current);
		
		try {
			for (int i = 0; i < capacity; ++i) {
				ListBase1 entry = entries[i];
				if (entry == null) {
					continue;
				}
				
				int size = entry.size();
				ListBase1 resultEntry = new ListBase1(size);
				
				for (int j = 1; j <= size; ++j) {
					Object []r = (Object[])entry.get(j);
					current.setCurrent((Integer)r[totalKeyCount]);
					
					Object b = exp.calculate(ctx);
					if (Variant.isTrue(b)) {
						mems.add(current.getCurrent());
						resultEntry.add(r);
					}
				}
				
				if (resultEntry.size > 0) {
					resultEntries[i] = resultEntry;
				}
			}
		} finally {
			stack.pop();
		}
		
		result.trimToSize();
		TimeIndexTable indexTable = new TimeIndexTable(result, hashUtil, resultEntries, totalKeyCount);
		result.setIndexTable(indexTable);
		return result;
	}
	
	public int findPos(Object key) {
		// ����ʱû�ṩʱ���ֶΣ�ȡ���µ�
		int hash = hashUtil.hashCode(key);
		ListBase1 table = entries[hash];
		if (table == null) {
			return 0;
		}
		
		int index = HashUtil.bsearch_a(table, key);
		if (index > 0) {
			for (int i = index + 1, size = table.size; i <= size; ++i) {
				Object []r = (Object[])table.get(i);
				if (Variant.isEquals(r[0], key)) {
					index = i;
				} else {
					break;
				}
			}
			
			Object []r = (Object[])table.get(index);
			return (Integer)r[totalKeyCount];
		} else {
			return 0;
		}
	}

	public int findPos(Object []keys) {
		int count = keys.length;
		if (count == totalKeyCount) {
			int hash = hashUtil.hashCode(keys, count - 1);
			ListBase1 table = entries[hash];
			if (table == null) {
				return 0;
			}

			int index = HashUtil.bsearch_a(table, keys, count);
			if (index > 0) {
				Object []r = (Object[])table.get(index);
				return (Integer)r[count];
			} else {
				index = -index - 1;
				if (index > 0) {
					// ���ʱ���û����ȵģ���ȡǰ�������
					Object []r = (Object[])table.get(index);
					if (Variant.compareArrays(r, keys, count - 1) == 0) {
						return (Integer)r[count];
					}
				}
				
				return 0; // key not found
			}
		} else {
			// ����ʱû�ṩʱ���ֶΣ�ȡ���µ�
			int hash = hashUtil.hashCode(keys, count);
			ListBase1 table = entries[hash];
			if (table == null) {
				return 0;
			}
			
			int index = HashUtil.bsearch_a(table, keys, count);
			if (index > 0) {
				for (int i = index + 1, size = table.size; i <= size; ++i) {
					Object []r = (Object[])table.get(i);
					if (Variant.compareArrays(r, keys, count) == 0) {
						index = i;
					} else {
						break;
					}
				}
				
				Object []r = (Object[])table.get(index);
				return (Integer)r[totalKeyCount];
			} else {
				return 0;
			}
		}
	}
	
	public int[] findAllPos(IArray key) {
		if (key == null) {
			return null;
		}
		int len = key.size();
		int[] pos = new int[len + 1];
		for (int i = 1; i <= len; i++) {
			Object obj = key.get(i);
			pos[i] = findPos(obj);
		}
		return pos;
	}

	public int[] findAllPos(IArray[] keys) {
		if (keys == null) {
			return null;
		}
		
		int keyCount = keys.length;
		int len = keys[0].size();
		int[] pos = new int[len + 1];
		Object[] objs = new Object[keyCount];
		for (int i = 1; i <= len; i++) {
			for (int c = 0; c < keyCount; c++) {
				objs[c] = keys[c].get(i);
			}
			pos[i] = findPos(objs);
		}
		return pos;
	}

	public int[] findAllPos(IArray key, BoolArray signArray) {
		if (key == null) {
			return null;
		}
		int len = key.size();
		int[] pos = new int[len + 1];
		for (int i = 1; i <= len; i++) {
			if (signArray.isFalse(i)) {
				continue;
			}
			Object obj = key.get(i);
			pos[i] = findPos(obj);
		}
		return pos;
	}

	public int[] findAllPos(IArray[] keys, BoolArray signArray) {
		if (keys == null) {
			return null;
		}
		
		int keyCount = keys.length;
		int len = keys[0].size();
		int[] pos = new int[len + 1];
		Object[] objs = new Object[keyCount];
		for (int i = 1; i <= len; i++) {
			if (signArray.isFalse(i)) {
				continue;
			}
			for (int c = 0; c < keyCount; c++) {
				objs[c] = keys[c].get(i);
			}
			pos[i] = findPos(objs);
		}
		return pos;
	}
}
