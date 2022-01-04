package com.scudata.dm;

/**
 * ������
 * @author WangXiaoJun
 *
 */
class LockObject implements IResource {
	private volatile Thread thread; // ��ǰ�������߳�
 
	public LockObject(Context ctx) {
	}
	
	public synchronized void close() {
		if (thread == Thread.currentThread()) {
			thread = null;
			notify();
		}
	}
	
	/**
	 * �����˶���
	 * @param ms �ȴ���������С��0��ʾ������ʱ
	 * @param ctx ����������
	 * @return true���ɹ���false��ʧ��
	 */
	public synchronized boolean lock(long ms, Context ctx) {
		Thread cur = Thread.currentThread();
		if (thread == null) {
			thread = cur;
			ctx.addResource(this);
			return true;
		} else if (thread == cur) {
			return false;
		} else {
			try {
				if (ms >= 0) {
					wait(ms); 
				} else {
					// �������������̵߳���unlock������ֽ�����lock����̵߳�wait�ű�����
					do {
						wait();
					} while (thread != null);
				}
				
				thread = cur;
				ctx.addResource(this);
				return true;
			} catch (InterruptedException e) {
				return false;
			}
		}
	}
	
	/**
	 * ����
	 * @param ctx ����������
	 * @return true���ɹ���false��ʧ��
	 */
	public synchronized boolean unlock(Context ctx) {
		if (thread == null) {
			return true;
		} else if (thread != Thread.currentThread()) {
			return false;
		} else {
			thread = null;
			ctx.removeResource(this);
			notify();
			return true;
		}
	}
}