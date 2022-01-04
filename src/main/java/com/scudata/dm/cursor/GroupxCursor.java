package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;

/**
 * �������α꣬���α����ݽ��������飬Ȼ�����α귽ʽ����
 * ���ڹ�ϣ��ʵ�������麯��cs.groupx()�Ķ��η��飬�������ѷ����ֶι�ϣֵ��ͬ����д��ͬһ����ʱ�ļ���
 * Ȼ��ÿ����ʱ�ļ�����һ�����α꣬Ҫȡ���α�����ʱ�����ж���������
 * @author RunQian
 *
 */
public class GroupxCursor extends ICursor {
	private ICursor src; // �α�
	private Expression[] exps; // ������ʽ
	private String []names; // �����ֶ���
	private Expression[] calcExps; // ���ܱ��ʽ
	private String []calcNames; // �����ֶ���
	private String opt; // ѡ��
	private int capacity; // �ڴ��б��������������
	
	private ICursor result; // ������α�

	/**
	 * �����������α�
	 * @param cursor Դ�����α�
	 * @param exps ������ʽ����
	 * @param names	�����ֶ�������
	 * @param calcExps ���ܱ��ʽ	����
	 * @param calcNames	�����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity	�ڴ��б��������������
	 */
	public GroupxCursor(ICursor cursor, Expression[] exps, String []names, 
			Expression[] calcExps, String []calcNames, String opt, Context ctx, int capacity) {
		this.src = cursor;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
		this.capacity = capacity;
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			src.resetContext(ctx);
			super.resetContext(ctx);
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (result == null) {
			if (src == null) {
				return null;
			}
			
			result = src.groupx(exps, names, calcExps, calcNames, opt, ctx, capacity);
		}
		
		return result.fetch(n);
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (result == null) {
			if (src == null) {
				return 0;
			}
			
			result = src.groupx(exps, names, calcExps, calcNames, opt, ctx, capacity);
		}
		
		return result.skipOver(n);
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (src != null) {
			src.close();
			src = null;
			
			if (result != null) {
				result.close();
				result = null;
			}
		}
	}
}
