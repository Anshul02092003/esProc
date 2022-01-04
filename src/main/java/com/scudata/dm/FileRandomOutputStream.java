package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

import com.scudata.common.RQException;

/**
 * ���Ըı����λ�õ������
 * @author WangXiaoJun
 *
 */
public class FileRandomOutputStream extends RandomOutputStream {
	private RandomAccessFile file;
	private FileChannel channel;
	
	/**
	 * ��RandomAccessFile�������Ըı����λ�õ������
	 * @param file RandomAccessFile
	 */
	public FileRandomOutputStream(RandomAccessFile file) {
		this.file = file;
	}
	
	/**
	 * ȡ�ļ��ܵ�
	 * @return FileChannel
	 */
	public FileChannel getChannel() {
		if (channel == null) {
			channel = file.getChannel();
		}
		
		return channel;
	}
	
	/**
	 * ȡ������
	 * @param pos �ļ�λ��
	 */
	public InputStream getInputStream(long pos) throws IOException {
		FileChannel channel = getChannel();
		channel.position(pos);
		return new ObjectReader(new ChannelInputStream(channel));
	}
	
	/**
	 * �����ļ�����ֹ����һ���߳��޸�
	 */
	public boolean tryLock() throws IOException {
		return getChannel().tryLock() != null;
	}
	
	/**
	 * �����ļ�����ֹ����һ���߳��޸�
	 */
	public boolean lock() throws IOException {
		FileChannel channel = getChannel();
		while (true) {
			try {
				channel.lock();
				return true;
			} catch (OverlappingFileLockException e) {
				try {
					Thread.sleep(FileObject.LOCK_SLEEP_TIME);
				} catch (InterruptedException ie) {
				}
			} catch (Exception e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * ������һ�������λ��
	 * @param newPosition λ��
	 */
	public void position(long newPosition) throws IOException{
		file.seek(newPosition);
	}
	
	/**
	 * ���ص�ǰ�����λ��
	 * @return long λ��
	 */
	public long position() throws IOException {
		return file.getFilePointer();
	}
	
	/**
	 * д��һ���ֽ�
	 * @param b �ֽ�ֵ
	 */
	public void write(int b) throws IOException {
		file.write(b);
	}

	/**
	 * д��һ���ֽ�����
	 * @param b �ֽ�����
	 */
	public void write(byte b[]) throws IOException {
		file.write(b);
	}
	
	/**
	 * д��һ���ֽ�����
	 * @param b �ֽ�����
	 * @param off ��ʼλ��
	 * @param len ����
	 */
	public void write(byte b[], int off, int len) throws IOException {
		file.write(b, off, len);
	}

	/**
	 * �ر������
	 */
	public void close() throws IOException {
		file.close();
	}
}