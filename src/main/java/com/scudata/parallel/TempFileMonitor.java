package com.scudata.parallel;

import java.io.*;

import com.scudata.common.*;
import com.scudata.dm.*;

/**
 * ��ʱ�ļ�������
 * ��ʱ(System.currentTimeMillis()-proxy.lastAccessTime()>service.getTimeout)�Ķ���رղ�ɾ��
 * @author Joancy
 *
 */
public class TempFileMonitor extends Thread {
	volatile boolean stop = false;
	int timeOut = 2;
	int interval = 5;

	/**
	 * ������ʱ�ļ�������
	 * @param timeOut ��ʱʱ��
	 * @param interval ��鳬ʱ���
	 */
	public TempFileMonitor(int timeOut, int interval){
		this.setName(toString());
		this.timeOut = timeOut;
		this.interval = interval;
	}
	
	/**
	 * ֹͣ�����߳�
	 */
	public void stopThread() {
		stop = true;
	}
	
	/**
	 * ʵ��toString�ı�����
	 */
	public String toString(){
		return "TempFileMonitor";
	}
	
	/**
	 * ������ʱ
	 * @param tmpDirectory 
	 * @param timeOut
	 */
	private void checkTimeOut(File tmpDirectory, int timeOut) {
		try {
			File[] tmpFiles = tmpDirectory.listFiles();
			if (tmpFiles == null) {
				return;
			}
			for (int i = 0; i < tmpFiles.length; i++) {
				File tmpFile = tmpFiles[i];
				if (tmpFile.isDirectory()) {
					checkTimeOut(tmpFile, timeOut);
				}
				long fileCreateTime = tmpFile.lastModified();
				if ((System.currentTimeMillis() - fileCreateTime) / 1000 > timeOut) {
					tmpFile.delete();
				}
			}
		} catch (Exception x) {
		}
	}

	/**
	 * ���е�ǰ�߳�
	 */
	public void run() {
		// timeOutΪ0ʱ������鳬ʱ
		if (interval == 0 || timeOut == 0) {
			return;
		}

		Logger.debug("Temporary file directory is:\r\n" + Env.getTempPath()
				+ ". \r\nFiles in temporary directory will be deleted on every "
				+ timeOut + " hours.\r\n");
		timeOut *= 3600;
		
		while (!stop) {
			try {
				sleep(interval * 1000);
				File f = new File(Env.getTempPath());
				if (!f.isDirectory()) {
					return;
				}
				checkTimeOut(f, timeOut);
			} catch (Exception x) {
			}
		}
	}
}
