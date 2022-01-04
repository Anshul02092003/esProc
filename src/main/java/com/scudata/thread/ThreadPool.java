package com.scudata.thread;

import java.util.LinkedList;

import com.scudata.dm.Env;

/**
 * �̳߳ض������ڶ��̴߳�������
 * �߳���������Env.getParallelNum()
 * @author WangXiaoJun
 *
 */
public class ThreadPool{
	private static ThreadPool instance;

	private WorkThread[] threads;
	private LinkedList<Job> jobList = new LinkedList<Job>();
	private boolean shutdown; // �Ƿ�ر��߳�

	// �̳߳������Ĺ����߳�
	private class WorkThread extends Thread {
		private WorkThread(ThreadGroup group, String name) {
			super(group, name);
		}

		public void run() {
			while (true) {
				// �������б���ͬ��
				synchronized(jobList) {
					if (shutdown) {
						// �̳߳ص����˹رգ������߳�
						return;
					}

					// ���û��������ȴ�
					if (jobList.size() == 0) {
						try {
							jobList.wait();
						} catch (InterruptedException e) {
							if (shutdown) {
								return;
							}
						}
					}
				}

				// �ѵ�һ������ȡ����ִ��
				Job job = null;
				synchronized(jobList) {
					if (jobList.size() > 0) {
						job = jobList.removeFirst();
					}
				}

				if (job != null) {
					try {
						job.run();
					} catch (Throwable e) {
						job.setError(e);
					}

					job.finish();
				}
			}
		}
	}

	private ThreadPool(int threadCount) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		// ���鲻��ide�����ģ�ide�����ide�������߳���Ľ����̷߳������˷�����ݹ�������������߳�����߳�
		/*while (true) {
			ThreadGroup g = group.getParent();
			if (g == null) {
				break;
			} else {
				group = g;
			}
		}*/

		threads = new WorkThread[threadCount];
		for (int i = 0; i < threadCount; ++i) {
			threads[i] = new WorkThread(group, "ThreadPool" + i);
			threads[i].setDaemon(true);
			threads[i].start();
		}
	}

	/**
	 * ȡ���̳߳أ��߳���ΪEnv.getCallxParallelNum()��MAX_THREAD_COUNT�е�С�ߣ����Ҳ�С��2
	 * @return ThreadPool
	 */
	public static synchronized ThreadPool instance() {
		if (instance == null || instance.shutdown) {
			int n = Env.getParallelNum();
			if (n < 2) {
				n = 2;
			}

			instance = new ThreadPool(n);
		} else {
			// ����Ƿ����߳�����
			WorkThread[] threads = instance.threads;
			for (int i = 0, len = threads.length; i < len; ++i) {
				if (!threads[i].isAlive()) {
					threads[i] = instance.new WorkThread(threads[i].getThreadGroup(), "ThreadPool" + i);
					threads[i].setDaemon(true);
					threads[i].start();
				}
			}
		}

		return instance;
	}
	
	/**
	 * �²���һ���̳߳�
	 * @param threadCount �߳�����������������õ��������������������
	 * @return ThreadPool
	 */
	public static synchronized ThreadPool newInstance(int threadCount) {
		int n = Env.getParallelNum();
		if (threadCount > n) {
			if (n < 1) {
				threadCount = 1;
			} else {
				threadCount = n;
			}
		}
		
		return new ThreadPool(threadCount);
	}

	/**
	 * �ڵ����Ҫ�ϸ���ִ�У���Ҫ֧��1������
	 * @param size �߳���
	 * @return
	 */
	public static synchronized ThreadPool newSpecifiedInstance(int size) {
		int n = size;
		if (n < 1) {
			n = 1;
		}
		
		return new ThreadPool(n);
	}

	/**
	 * �ر��Ѿ������̳߳�ʵ�������̳߳�ʵ�����ܼ���ʹ��
	 */
	public synchronized void shutdown() {
		shutdown = true;
		synchronized(jobList) {
			jobList.notifyAll();
			jobList.clear();
		}
	}

	/**
	 * �ύһ�������������أ�job.join�ȴ��������
	 * @param job Job
	 */
	public void submit(Job job) {
		job.reset();
		synchronized(jobList) {
			jobList.add(job);
			jobList.notify();
		}
	}
	
	protected void finalize() throws Throwable {
		try {
			if (!shutdown) {
				jobList.notifyAll();
				jobList.clear();
			}
		} catch (Throwable e) {
		}
	}

	/**
	 * �����̳߳�����߳�����
	 * @return
	 */
	public int getThreadCount() {
		return threads.length;
	}
}
