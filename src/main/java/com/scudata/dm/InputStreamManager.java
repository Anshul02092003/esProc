package com.scudata.dm;

import java.util.LinkedList;

/**
 * ���ڶ԰������������������й���
 * @author WangXiaoJun
 *
 */
public final class InputStreamManager extends Thread {
	private static InputStreamManager manager;
	
	// �ڵȴ���ȡ���ݵ�������
	private LinkedList <BlockInputStream>bisList = new LinkedList<BlockInputStream>();

	private InputStreamManager(ThreadGroup group) {
		super(group, "InputStreamManager");
	}

	/**
	 * ȡ�������������
	 * @return InputStreamManager
	 */
	public synchronized static InputStreamManager getInstance() {
		if (manager == null) { // || !manager.isAlive()
			ThreadGroup group = currentThread().getThreadGroup();
			while (true) {
				ThreadGroup g = group.getParent();
				if (g == null) {
					break;
				} else {
					group = g;
				}
			}

			manager = new InputStreamManager(group);
			manager.setDaemon(true);
			manager.start();
		}

		return manager;
	}

	/**
	 * ����������������ж�ȡһ������
	 * @param is
	 */
	public void read(BlockInputStream is) {
		synchronized(bisList) {
			bisList.add(is);
			bisList.notify();
		}
	}

	/**
	 * �����ݺ���
	 */
	public void run() {
		while (true) {
			synchronized(bisList) {
				if (bisList.size() == 0) {
					try {
						// �ȴ�����������read
						bisList.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// ѭ�����������У�ֱ�����е�����������ȡ������
			while (true) {
				BlockInputStream bis;
				synchronized(bisList) {
					if (bisList.size() == 0) {
						break;
					}
					
					bis = bisList.removeFirst();
				}

				bis.fillBuffers();
			}
		}
	}
}
