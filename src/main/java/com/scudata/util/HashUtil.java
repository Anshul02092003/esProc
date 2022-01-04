package com.scudata.util;

import com.scudata.dm.Env;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;

/**
 * Ϊָ����ϣ���������һ����ϣֵ
 * @author WangXiaoJun
 *
 */
public final class HashUtil {
	private final int capacity; // ��ϣ������

	// ������Ϊ��ϣ��������һЩֵ����ϣ������Ϊ����ʱ��ϣֵ�ظ��ʽϵ�
	private static final int []PRIMES = new int []{
		13, 19, 29, 41, 59, 79, 107, 149, 197, 263, 347, 457, 599, 787, 1031,
		1361, 1777, 2333, 3037, 3967, 5167, 6719, 8737, 11369, 14783,
		19219, 24989, 32491, 42257, 54941, 71429, 92861, 120721, 156941,
		204047, 265271, 344857, 448321, 582821, 757693, 985003, 1280519,
		1664681, 2164111, 2813353, 3657361, 4754591, 6180989, 8035301,
		10445899, 13579681, 17653589, 22949669, 29834603, 38784989,
		50420551, 65546729, 85210757, 110774011, 144006217, 187208107,
		243370577, 316381771, 411296309, 534685237, 695090819, 903618083,
		1174703521, 1527114613, 1837299131, 2147483647
	};

	private static int getNearCapacity(int c) {
		if (c > Env.MAX_HASHCAPACITY) c = Env.MAX_HASHCAPACITY;

		for (int i = 0, len = PRIMES.length; i < len; ++i) {
			if (PRIMES[i] == c) {
				return PRIMES[i];
			} else if (PRIMES[i] > c) {
				return PRIMES[i] > Env.MAX_HASHCAPACITY ? PRIMES[i - 1] : PRIMES[i];
			}
		}

		throw new RuntimeException();
	}

	public static int getInitGroupSize() {
		return 3;
	}
	
	/**
	 * ������ϣֵ���ɹ��ߣ���ϣ����������Env.getDefaultHashCapacity()�ﶨ���
	 */
	public HashUtil() {
		this.capacity = Env.getDefaultHashCapacity();
	}

	/**
	 * ������ϣֵ���ɹ���
	 * @param capacity ��ϣ���������������ڲ������Ϊһ��ʹ��ϣֵ���ظ��ȵ͵�������
	 * ����getCapacity��ȡ�����������
	 */
	public HashUtil(int capacity) {
		this.capacity = getNearCapacity(capacity);
	}

	/**
	 * ������ϣֵ���ɹ���
	 * @param capacity ��ϣ������
	 * @param doAdjust �Ƿ��������������true��������false��������
	 */
	public HashUtil(int capacity, boolean doAdjust) {
		if (doAdjust) {
			this.capacity = getNearCapacity(capacity);
		} else {
			this.capacity = capacity;
		}
	}
	
	/**
	 * ȡ��ϣ������
	 * @return int
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * �����������Ĺ�ϣֵ
	 * @param vals ��������
	 * @return ��ϣֵ
	 */
	public int hashCode(Object []vals) {
		return hashCode(vals, vals.length);
	}

	/**
	 * �����������Ĺ�ϣֵ
	 * @param vals ��������
	 * @param count ����
	 * @return ��ϣֵ
	 */
	public int hashCode(Object []vals, int count) {
		int hash = vals[0] != null ? vals[0].hashCode() : 0;
		for (int i = 1; i < count; ++i) {
			if (vals[i] != null) {
				hash = 31 * hash + vals[i].hashCode();
			} else {
				hash = 31 * hash;
			}
		}

		return hash(hash);
	}

	/**
	 * �������Ĺ�ϣֵ
	 * @param val ����
	 * @return ��ϣֵ
	 */
	public int hashCode(Object val) {
		if (val != null) {
			return hash(val.hashCode());
		} else {
			return 0;
		}
	}
	
	/**
	 * ���������ָ����ϣ�������µĹ�ϣֵ
	 * @param val ����
	 * @param capacity ��ϣ������
	 * @return ��ϣֵ
	 */
	public static int hashCode(Object val, int capacity) {
		if (val != null) {
			int h = val.hashCode();
			h = (h + (h >> 16)) % capacity;
			if (h > 0) {
				return h;
			} else {
				return -h;
			}
		} else {
			return 0;
		}
	}

	/**
	 * ����long�Ĺ�ϣֵ
	 * @param value
	 * @return
	 */
    public static int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

	private int hash(int h) {
		h = (h + (h >> 16)) % capacity;
		if (h > 0) {
			return h;
		} else {
			return -h;
		}
	}

	/**
	 * �����ֶ�����ֵ���Ҽ�¼
	 * @param table ��ԱΪ��¼
	 * @param keys ����ֵ����
	 * @return λ�ã��Ҳ����򷵻ظ�����λ��
	 */
	public static int bsearch_r(ListBase1 table, Object []keys) {
		int colCount = keys.length;
		int low = 1, high = table.size();
		int cmp = 0;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Record r = (Record)table.get(mid);

			for (int c = 0; c < colCount; ++c) {
				cmp = Variant.compare(r.getNormalFieldValue(c), keys[c], true);
				if (cmp != 0) break;
			}

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}
	
	/**
	 * ������ֵ���Ҽ�¼
	 * @param table ��ԱΪ��¼
	 * @param key ����ֵ
	 * @return λ�ã��Ҳ����򷵻ظ�����λ��
	 */
	public static int bsearch_r(ListBase1 table, Object key) {
		int low = 1, high = table.size();
		int cmp = 0;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Record r = (Record)table.get(mid);
			cmp = Variant.compare(r.getNormalFieldValue(0), key, true);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}

	/**
	 * ������ֵ��������
	 * @param table ��ԱΪ����
	 * @param key ����ֵ
	 * @return λ�ã��Ҳ����򷵻ظ�����λ��
	 */
	public static int bsearch_a(ListBase1 table, Object key) {
		int low = 1, high = table.size();
		int cmp = 0;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Object []r = (Object[])table.get(mid);

			cmp = Variant.compare(r[0], key, true);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}

	/**
	 * �����ֶ�����ֵ��������
	 * @param table ��ԱΪ����
	 * @param keys ����ֵ����
	 * @return λ�ã��Ҳ����򷵻ظ�����λ��
	 */
	public static int bsearch_a(ListBase1 table, Object []keys, int count) {
		int low = 1, high = table.size();
		int cmp = 0;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Object []r = (Object[])table.get(mid);

			for (int c = 0; c < count; ++c) {
				cmp = Variant.compare(r[c], keys[c], true);
				if (cmp != 0) break;
			}

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}

	/**
	 * ��ֵ����������
	 * @param table ��ԱΪ���У��ҵ�һ����Ա����ָ��ֵ������
	 * @param key ֵ
	 * @return λ�ã��Ҳ����򷵻ظ�����λ��
	 */
	public static int bsearch_g(ListBase1 table, Object key) {
		int low = 1, high = table.size();
		int cmp = 0;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Sequence group = (Sequence)table.get(mid);
			cmp = Variant.compare(group.get(1), key, true);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}
	
	/**
	 * ȡ�ȵ�ǰ����Сһ������������������ϣ����Ķ��λ���
	 * @return ����
	 */
	public int getPrevCapacity() {
		int capacity = this.capacity;
		if (capacity < 11369) {
			return 11369 ;
		}
		
		for (int i = 0, len = PRIMES.length; i < len; ++i) {
			if (PRIMES[i] >= capacity) {
				return PRIMES[i - 1];
			}
		}
		
		return getNearCapacity(capacity / 2);
	}
}
