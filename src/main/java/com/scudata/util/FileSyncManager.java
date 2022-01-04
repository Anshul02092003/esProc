package com.scudata.util;

import java.io.File;
import java.util.HashMap;

import com.scudata.dm.FileObject;
import com.scudata.dm.LocalFile;


/**
 * ����ȡ�ļ���ͬ������
 * ���߳��޸��ļ�ʱ���������ͬ���������ļ�ȡ��ͬ����������ͬ��
 * @author RunQian
 *
 */
public final class FileSyncManager {
	// �ļ���ͬ������ӳ��
	private static HashMap<File, File> fileMap = new HashMap<File, File>();
	
	/**
	 * ȡ���ļ���ͬ�����󣬷����ļ�ʱ�ڴ˶����ϼ���
	 * @param fo �ļ�����
	 * @return Object ����ͬ������
	 */
	public static Object getSyncObject(FileObject fo) {
		LocalFile lf = fo.getLocalFile();
		File file = lf.file();
		return getSyncObject(file);
	}
	
	/**
	 * ȡ���ļ���ͬ�����󣬷����ļ�ʱ�ڴ˶����ϼ���
	 * @param file �ļ�
	 * @return Object ����ͬ������
	 */
	public static Object getSyncObject(File file) {
		synchronized(fileMap) {
			File f = fileMap.get(file);
			if (f == null) {
				fileMap.put(file, file);
				return file;
			} else {
				return f;
			}
		}
	}
}