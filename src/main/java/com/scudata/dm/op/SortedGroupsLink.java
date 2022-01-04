package com.scudata.dm.op;

import com.scudata.dm.DataStruct;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Table;
import com.scudata.util.Variant;

/**
 * �����¼����,����A.groups@h(...)
 * @author RunQian
 *
 */
class SortedGroupsLink {
	/**
	 * ����Ľڵ�
	 * @author RunQian
	 *
	 */
	static class Node {
		private Record r; // ��¼
		private Node next; // ��һ���ڵ�
		
		public Node() {
		}
		
		public Node(Record r) {
			this.r = r;
		}
		
		public void setReocrd(Record r) {
			this.r = r;
		}
		
		public Record getRecord() {
			return r;
		}
		
		public int cmp(Object []values) {
			return Variant.compareArrays(r.getFieldValues(), values, values.length);
		}
		
		public int cmp(Object value) {
			return Variant.compare(r.getNormalFieldValue(0), value, true);
		}
	}
	
	private Node first; // �׽ڵ�
	private Node prevNode; // ��һ���ҵ����Ǹ��ڵ�
	private int len = 0; // ����
	
	
	/**
	 * ȡ��������ļ�¼���س����
	 * @param ds
	 * @return
	 */
	public Table toTable(DataStruct ds) {
		Table table = new Table(ds, len);
		ListBase1 mems = table.getMems();
		for (Node node = first; node != null; node = node.next) {
			mems.add(node.r);
		}
		
		return table;
	}
	
	/**
	 * ���ݶ��ֶ�����ֵ�ҵ�����Ľڵ㣬�Ҳ���������Ӧ��λ���½�һ���ڵ㷵�أ�����ḳֵ��¼
	 * @param values ����ֵ����
	 * @return
	 */
	public Node put(Object []values) {
		if (prevNode == null) {
			len++;
			return first = prevNode= new Node();
		}
		
		Node prev = null;
		Node cur = prevNode;
		
		// ���ϴβ����ֵ��ʼ�Ƚ����С���ϴβ����ֵ���ͷ��ʼ��
		while (true) {
			int cmp = cur.cmp(values);
			if (cmp < 0) {
				if (cur.next == null) {
					len++;
					Node node = new Node();
					cur.next = node;
					
					prevNode = node;
					return node;
				} else {
					prev = cur;
					cur = cur.next;
				}
			} else if (cmp == 0) {
				prevNode = cur;
				return cur;
			} else {
				if (prev == null) {
					break;
				} else {
					len++;
					Node node = new Node();
					prev.next = node;
					node.next = cur;
					
					prevNode = node;
					return node;
				}
			}
		}
		
		// �²����ֵ���ϴβ����ֵС���ͷ��ʼ����
		prev = null;
		cur = first;
		while (true) {
			int cmp = cur.cmp(values);
			if (cmp < 0) {
				prev = cur;
				cur = cur.next;
			} else if (cmp ==0) {
				prevNode = cur;
				return cur;
			} else {
				len++;
				Node node = new Node();
				if (prev != null) {
					prev.next = node;
					node.next = cur;
				} else {
					first = node;
					node.next = cur;
				}
				
				prevNode = node;
				return node;
			}
		}
	}
	
	/**
	 * ���ݵ��ֶ�����ֵ�ҵ�����Ľڵ㣬�Ҳ���������Ӧ��λ���½�һ���ڵ㷵�أ�����ḳֵ��¼
	 * @param value ����ֵ
	 * @return
	 */
	public Node put(Object value) {
		if (prevNode == null) {
			len++;
			return first = prevNode= new Node();
		}
		
		Node prev = null;
		Node cur = prevNode;
		
		// ���ϴβ����ֵ��ʼ�Ƚ����С���ϴβ����ֵ���ͷ��ʼ��
		while (true) {
			int cmp = cur.cmp(value);
			if (cmp < 0) {
				if (cur.next == null) {
					len++;
					Node node = new Node();
					cur.next = node;
					
					prevNode = node;
					return node;
				} else {
					prev = cur;
					cur = cur.next;
				}
			} else if (cmp == 0) {
				prevNode = cur;
				return cur;
			} else {
				if (prev == null) {
					break;
				} else {
					len++;
					Node node = new Node();
					prev.next = node;
					node.next = cur;
					
					prevNode = node;
					return node;
				}
			}
		}
		
		// �²����ֵ���ϴβ����ֵС���ͷ��ʼ����
		prev = null;
		cur = first;
		while (true) {
			int cmp = cur.cmp(value);
			if (cmp < 0) {
				prev = cur;
				cur = cur.next;
			} else if (cmp ==0) {
				prevNode = cur;
				return cur;
			} else {
				len++;
				Node node = new Node();
				if (prev != null) {
					prev.next = node;
					node.next = cur;
				} else {
					first = node;
					node.next = cur;
				}
				
				prevNode = node;
				return node;
			}
		}
	}
}