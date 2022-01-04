package com.scudata.parallel;

import com.scudata.common.Logger;

public class PerfMonitor {
//�ֽ����������������ҵ/�ʺ���ҵ������,Ҳ������ı���	
	private static Object LOCK1 = new Object();
	private static volatile int concurrents = 0;

//�������������������ҵ������
	private static Object LOCKPROCESS = new Object();
	private static volatile int processConcurrents = 0;

	static HostManager hm = HostManager.instance();
	
	public static void enterProcess() {
		synchronized (LOCKPROCESS) {
			int maxNum = hm.getMaxTaskNum();
			if (processConcurrents >= maxNum) {
				try {
					LOCKPROCESS.wait();
				} catch (InterruptedException e) {
				}
			}
			processConcurrents++;
		}
	}

	public static void leaveProcess() {
		synchronized (LOCKPROCESS) {
			processConcurrents--;
			LOCKPROCESS.notify();
		}
	}

	/**
	 * ��ȡ�ֻ����������еĲ���������Ŀ��Զ�̷��������ļ���Ҫ��ѡ�������ٵķֻ�
	 * @return
	 */
	public static int getConcurrentTasks() {
		return processConcurrents;
	}

	/**
	 * �������񣬴����������ȴ���
	 */
	public static void enterTask(Object mark) {
		synchronized (LOCK1) {
			int maxNum = hm.getMaxTaskNum();
			
			if (concurrents >= maxNum) {
				try {
					LOCK1.wait();
				} catch (InterruptedException e) {
				}
			}
			concurrents++;
			if(mark!=null){
				Logger.debug(mark);
			}
		}
	}

	public static void leaveTask(Object mark,String suffix) {
		synchronized (LOCK1) {
			concurrents--;
			LOCK1.notify();
			if(mark!=null){
				Logger.debug(suffix+" "+mark);
			}
		}
	}

}
