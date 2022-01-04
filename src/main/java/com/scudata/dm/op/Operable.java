package com.scudata.dm.op;

import com.scudata.dm.Context;

/**
 * ���Ը�������Ľӿ�
 * @author WangXiaoJun
 *
 */
public interface Operable {
	/**
	 * ��������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	Operable addOperation(Operation op, Context ctx);
}