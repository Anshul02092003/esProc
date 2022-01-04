package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * �α�͹ܵ��������㺯�����࣬�����α��ܵ����յļ�����
 * @author RunQian
 *
 */
public interface IResult {
	// ����߳̿���ͬʱ����ͬ�Ĺܵ�push����Ҫʹ���߳��Լ���ctx������ͬʱ�����ʱ����ܳ���
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence seq, Context ctx);
	
	/**
	 * �������ͽ�����ȡ���յļ�����
	 * @return
	 */
	public Object result();
	
	/**
	 * ��Ⱥ�ܵ������Ҫ�Ѹ��ڵ���ķ��ؽ���ٺϲ�һ��
	 * @param results ÿ���ڵ���ļ�����
	 * @return �ϲ���Ľ��
	 */
	public Object combineResult(Object []results);
}