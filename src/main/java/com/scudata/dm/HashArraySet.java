package com.scudata.dm;

import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * �ɶ��ֶ����������Ĺ�ϣ����������ϣ����
 * @author WangXiaoJun
 *
 */
public class HashArraySet {
	// ���ڴ�Ź�ϣ�����Ԫ�أ���ϣֵ��ͬ��Ԫ��������洢
	private static class Entry {
		Object []keys;
		Entry next;
		
		public Entry(Object []keys, Entry next) {
			this.keys = keys;
			this.next = next;
		}
	}
	
	protected HashUtil hashUtil; // ���ڼ����ϣֵ
	protected Entry[] entries; // ��hashֵ����
	
	/**
	 * ������ϣ���鼯��
	 */
	public HashArraySet() {
		hashUtil = new HashUtil();
		entries = new Entry[hashUtil.getCapacity()];
	}
	
	/**
	 * ������ϣ���鼯��
	 * @param capacity ��ϣ������
	 */
	public HashArraySet(int capacity) {
		hashUtil = new HashUtil(capacity);
		entries = new Entry[hashUtil.getCapacity()];
	}
	
	/**
	 * ��������뵽���ϣ�����������Ѿ������������ָ��������ֵ��ͬ��ִ���κβ���
	 * @param keys ֵ����
	 * @return boolean ����������Ѱ���ָ�������򷵻�false�����򷵻�true
	 */
	public boolean put(Object []keys) {
		int keyCount = keys.length;
		int hash = hashUtil.hashCode(keys, keyCount);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compareArrays(entry.keys, keys, keyCount) == 0) {
				return false;
			}
		}
		
		entries[hash] = new Entry(keys, entries[hash]);
		return true;
	}
	
	/**
	 * ���ؼ������Ƿ����ָ������
	 * @param keys ֵ����
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object []keys) {
		int count = keys.length;
		int hash = hashUtil.hashCode(keys, count);
		for (Entry entry = entries[hash]; entry != null; entry = entry.next) {
			if (Variant.compareArrays(entry.keys, keys) == 0) {
				return true;
			}
		}

		return false; // key not found
	}
}
