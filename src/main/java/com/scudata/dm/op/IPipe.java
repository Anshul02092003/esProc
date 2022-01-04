package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * �ܵ��ӿ�
 * @author RunQian
 *
 */
public interface IPipe {
	/**
	 * ���ܵ���������
	 * @param seq ����
	 * @param ctx ����������
	 */
	void push(Sequence seq, Context ctx);
	
	/**
	 * �������ͽ���
	 * @param ctx
	 */
	void finish(Context ctx);
}