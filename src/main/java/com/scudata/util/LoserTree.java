package com.scudata.util;

/**
 * �������������������ݵĶ�·�鲢��ÿһ·Ϊ����һ���ڵ�
 * @author RunQian
 *
 */
public class LoserTree {
	private ILoserTreeNode []nodes;
	private int []tree;
	private int size;
	
	/**
	 * ��ָ���Ľڵ㹹������������·�α��ÿһ·����һ���ڵ�
	 * @param nodes �ڵ�
	 */
	public LoserTree(ILoserTreeNode []nodes) {
		int q = 0;
		for (int i = 0, len = nodes.length; i < len; ++i) {
			if (nodes[i].hasNext()) {
				if (i != q) {
					nodes[q] = nodes[i];
				}
				
				q++;
			}
		}
		
		this.nodes = nodes;
		size = q;
		tree = new int[q];
		init();
	}
	
	// ��ʼ��������������
	private void init() {
		int size = this.size;
		for (int i = 0; i < size; ++i) {
			tree[i] = -1;
		}
		
		for (int i = size - 1; i >= 0; --i) {
			initAdjust(i);
		}
	}
	
	private void initAdjust(int s) {
		ILoserTreeNode []nodes = this.nodes;
		int []tree = this.tree;
		int p;
		for (int t = (s + size) / 2; t > 0; t /= 2) {
			p = tree[t];
			if (p == -1) {
				tree[t] = s;
				tree[0] = -1;
				return;
			} else if (nodes[s].compareTo(nodes[p]) > 0) {
				tree[t] = s;
				s = p;
			}
		}
		
		tree[0] = s;
	}
	
	// s�ڵ�ֵ�����ı䣬����
	private void adjust(int s) {
		ILoserTreeNode []nodes = this.nodes;
		int []tree = this.tree;
		int p;
		for (int t = (s + size) / 2; t > 0; t /= 2) {
			p = tree[t];
			int cmp = nodes[s].compareTo(nodes[p]);
			if (cmp > 0 || (cmp == 0 && s > p)) {
				// ֵͬ����ȡǰ���Ա��
				tree[t] = s;
				s = p;
			}
		}
		
		tree[0] = s;
	}
	
	/**
	 * �����Ƿ�������
	 * @return true���У�false��û��
	 */
	public boolean hasNext() {
		return size > 0;
	}
	
	/**
	 * ������С������
	 * @return
	 */
	public Object pop() {
		int p = tree[0];
		Object obj = nodes[p].popCurrent();
		if (nodes[p].hasNext()) {
			adjust(p);
		} else {
			size--;
			int size = this.size;
			System.arraycopy(nodes, p + 1, nodes, p, size - p);
			init();
		}
		
		return obj;
	}
}