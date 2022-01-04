package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.util.LoserTree;
import com.scudata.util.LoserTreeNode_CS;
import com.scudata.util.LoserTreeNode_CS1;

/**
 * ���ṹ�Ķ���α�������鲢�����γɵ��α�
 * CS.mergex(xi,��)
 * @author RunQian
 *
 */
public class MergeCursor extends ICursor {
	private ICursor []cursors; // �α��������Ѿ����鲢�ֶ���������
	private int []fields; // �鲢�ֶ�
	private boolean isNullMin = true; // null�Ƿ���Сֵ
	
	private LoserTree loserTree; // ÿһ·�α���Ϊ���Ľڵ㰴�鲢�ֶ�ֵ���ɰ�����
	
	/**
	 * ������Ч�鲢�α�
	 * @param cursors �α�����
	 * @param fields �����ֶ�����
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public MergeCursor(ICursor []cursors, int []fields, String opt, Context ctx) {
		this.cursors = cursors;
		this.fields = fields;
		this.ctx = ctx;
		
		setDataStruct(cursors[0].getDataStruct());
		
		if (opt != null && opt.indexOf('0') !=-1) {
			isNullMin = false;
		}
		
		int count = cursors.length;
		if (fields.length == 1) {
			LoserTreeNode_CS1 []nodes = new LoserTreeNode_CS1[count];
			for (int i = 0; i < count; ++i) {
				nodes[i] = new LoserTreeNode_CS1(cursors[i], fields[0], isNullMin);
			}
			
			loserTree = new LoserTree(nodes);
		} else {
			LoserTreeNode_CS []nodes = new LoserTreeNode_CS[count];
			for (int i = 0; i < count; ++i) {
				nodes[i] = new LoserTreeNode_CS(cursors[i], fields, isNullMin);
			}
			
			loserTree = new LoserTree(nodes);
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			super.resetContext(ctx);
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		LoserTree loserTree = this.loserTree;
		if (loserTree == null || n < 1) {
			return null;
		}
		
		Sequence table;
		if (n > INITSIZE) {
			table = new Sequence(INITSIZE);
		} else {
			table = new Sequence(n);
		}

		// ѭ��ȡ������仺������ѭ�������жԸ�·�α��ȡ�����������鲢��
		for (int i = 0; i < n && loserTree.hasNext(); ++i) {
			table.add(loserTree.pop());
		}

		if (table.length() > 0) {
			return table;
		} else {
			return null;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		LoserTree loserTree = this.loserTree;
		if (loserTree == null || n < 1) return 0;
		
		long i = 0;
		for (; i < n && loserTree.hasNext(); ++i) {
			loserTree.pop();
		}

		return i;
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

			loserTree = null;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		ICursor []cursors = this.cursors;
		int count = cursors.length;
		for (int i = 0; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		if (fields.length == 1) {
			LoserTreeNode_CS1 []nodes = new LoserTreeNode_CS1[count];
			for (int i = 0; i < count; ++i) {
				nodes[i] = new LoserTreeNode_CS1(cursors[i], fields[0], isNullMin);
			}
			
			loserTree = new LoserTree(nodes);
		} else {
			LoserTreeNode_CS []nodes = new LoserTreeNode_CS[count];
			for (int i = 0; i < count; ++i) {
				nodes[i] = new LoserTreeNode_CS(cursors[i], fields, isNullMin);
			}
			
			loserTree = new LoserTree(nodes);
		}
		
		return true;
	}
	
	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getSortFields() {
		return cursors[0].getSortFields();
	}
}
