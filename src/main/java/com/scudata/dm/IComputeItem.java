package com.scudata.dm;

// ������ԵĶ��󣬿�Ϊ���л��¼
/**
 * ��Ա�������ڰ�������ѹջ
 * A.f()�ڼ���ǰӦ����A����һ��IComputeItem��Ȼ���IComputeItem����ѹ��Context.ComputeStack��
 * @author WangXiaoJun
 *
 */
public interface IComputeItem {
	/**
	 * ȡ��ǰѭ����Ԫ��
	 * @return Object
	 */
	Object getCurrent();
	
	/**
	 * ȡ��ǰѭ�����
	 * @return ��ţ����е�Ԫ�ش�1��ʼ����
	 */
	int getCurrentIndex();
	
	/**
	 * ȡ��ǰ��ѭ������
	 * @return
	 */
	Sequence getCurrentSequence();
	
	/**
	 * ������ɣ���ѹջ�Ķ����ջ
	 */
	void popStack();
	
	/**
	 * �ж϶����Ƿ��ڶ�ջ��
	 * @param stack ��ջ
	 * @return true���ڶ�ջ�У�false�����ڶ�ջ��
	 */
	boolean isInStack(ComputeStack stack);
}
