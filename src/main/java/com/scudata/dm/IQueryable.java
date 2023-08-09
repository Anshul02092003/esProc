package com.scudata.dm;

import com.scudata.cellset.ICellSet;

/**
 * ��ִ��query�����Ķ���
 * @author WangXiaoJun
 *
 */
public interface IQueryable {
	/**
	 * ִ�в�ѯ���
	 * @param sql String ��ѯ���
	 * @param params Object[] ����ֵ
	 * @param cs ICellSet �������
	 * @param ctx Context
	 * @return Object
	 */
	Object query(String sql, Object []params, ICellSet cs, Context ctx);
}
