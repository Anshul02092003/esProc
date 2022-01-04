package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * ���ڱ��ֹܵ���ǰ������Ϊ�����
 * @author RunQian
 *
 */
public class FetchResult implements IResult {
	private Sequence result = new Sequence();
	
	public FetchResult() {
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public void push(Sequence table, Context ctx) {
		result.addAll(table);
	}
	
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return ���
	  */
	public Object result() {
		Sequence seq = result;
		if (seq == null || seq.length() == 0) {
			return null;
		}
		
		result = null;
		return seq;
	}
	
	/**
	 * ��Ⱥ�ܵ������Ҫ�Ѹ��ڵ���ķ��ؽ���ٺϲ�һ��
	 * @param results ÿ���ڵ���ļ�����
	 * @return �ϲ���Ľ��
	 */
	public Object combineResult(Object []results) {
		int count = results.length;
		Sequence result = new Sequence();
		for (int i = 0; i < count; ++i) {
			if (results[i] instanceof Sequence) {
				result.addAll((Sequence)results[i]);
			} else if (results[i] != null) {
				result.add(results[i]);
			}
		}

		return result;
	}
}