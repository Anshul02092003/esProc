package com.scudata.parallel;

import java.io.*;

import com.scudata.common.*;
import com.scudata.dm.*;

/**
 * Զ���ļ�����
 * 
 * @author Joancy
 *
 */
public class RemoteFileProxy {
	String fileName;
	InputStream is;
	RandomOutputStream os;
	int proxyId = -1;
	
	Integer partition=null;
	boolean isAppend = false;
	private long lastAccessTime = -1;
	private long readPosition = -1;
	
	/**
	 * ����Զ���ļ�������
	 * @param fileName �ļ���
	 * @param partition ������
	 * @param id ������
	 * @param isAppend �Ƿ�׷��
	 */
	public RemoteFileProxy(String fileName, Integer partition,int id, Boolean isAppend) {
		this.fileName = fileName;
		this.partition = partition;
		this.proxyId = id;
		if(isAppend!=null){//С�����ܵ��ڴ󲼶���ֵΪnullʱ�����ת����,nullʱ��ת������
			this.isAppend = isAppend;
		}
		access();
	}
	

	/**
	 * ʵ��toString������Ϣ
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("RemoteFileProxy "+fileName+" id:"+proxyId);
		return sb.toString();
	}
	
	 // ��ǰ�����еĴ�����
	int getProxyID() {
		return proxyId;
	}

	void access() {
		lastAccessTime = System.currentTimeMillis();
	}
	
	public void setReadPosition(long rp){
		readPosition = rp;
	}

	byte[] buf = null;
	
	/**
	 * ��ȡָ����Ŀ���ֽ�����
	 * @param bufSize ��Ŀ
	 * @return �ֽ�����
	 * @throws Exception ����ʱ�׳��쳣
	 */
	public byte[] read(int bufSize) throws Exception {
		if (buf == null) {
			buf = new byte[bufSize];
		}

		InputStream is = getInputStream();
		int n = 0, len = bufSize;
		while (n < len) {
			int count = is.read(buf, n, len - n);
			if (count < 0) {
				break;
			}
			n += count;
		}
		len = n;
		byte[] buf2;
		if (len <= 0) {
			buf2 = new byte[0];
		} else if (len != bufSize) {
			buf2 = new byte[len];
			System.arraycopy(buf, 0, buf2, 0, len);
		} else {
			buf2 = buf;
		}
		access();

		return buf2;
	}

	/**
	 * ��Զ���ļ�д���ֽ�����
	 * @param bytes �ֽ�����
	 * @throws Exception д����ʱ�׳��쳣
	 */
	public void write(byte[] bytes) throws Exception {
		RandomOutputStream os = getRandomOutputStream();
		os.write(bytes);
		access();
	}
	
	/**
	 * ���ö�д�ļ����α�λ��
	 * @param posi λ��ֵ
	 * @throws IOException ���ó����׳��쳣
	 */
	public void setPosition(long posi) throws IOException{
		RandomOutputStream os = getRandomOutputStream();
		os.position(posi);
	}
	
	/**
	 * ȡ��ǰ��дλ��
	 * @return λ��ֵ
	 * @throws IOException ����ʱ�׳��쳣
	 */
	public long position() throws IOException{
		RandomOutputStream os = getRandomOutputStream();
		return os.position();
	}

	private InputStream getInputStream() throws Exception{
		if (is == null) {
			if(readPosition!=-1 && os!=null){
				is = os.getInputStream(readPosition);
			}else{
				FileObject fo = new FileObject(fileName);
				fo.setPartition(partition);
				if(!fo.isExists()){
					String msg = fileName;
					msg+=" is not exist.";
					Logger.debug(msg);
					throw new Exception(msg);
				}
				is = fo.getInputStream();
			}
		}
		return is;
	}

	private RandomOutputStream getRandomOutputStream() {
		if (os == null) {
			FileObject fo = new FileObject(fileName);
			fo.setPartition(partition);
			os = fo.getRandomOutputStream(isAppend);
		}
		return os;
	}

	/**
	 * ���Ը��ļ�����
	 * @return �ɹ���������true�����򷵻�false
	 * @throws Exception �����쳣
	 */
	public boolean tryLock() throws Exception{
		return getRandomOutputStream().tryLock();
	}

	/**
	 * ������ǰ�ļ�
	 * @return ���ɹ�����true�����򷵻�false
	 * @throws Exception ���������׳��쳣
	 */
	public boolean lock() throws Exception{
		return getRandomOutputStream().lock();
	}
	
	/**
	 * �ر��ļ�����
	 */
	public void close() {
		destroy();
		RemoteFileProxyManager.delProxy(proxyId);
	}

	/**
	 * ���ٴ������
	 */
	public void destroy() {
		if (is != null) {
			try {
				is.close();
			} catch (Exception x) {
			}
		}
		if (os != null) {
			try {
				os.close();
			} catch (Exception x) {
			}
		}
	}

	/**
	 * ���������ĳ�ʱ
	 * @param timeOut ��ʱ��ʱ��
	 * @return ��ʱ�����ٶ��󲢷���true�����򷵻�false
	 */
	public boolean checkTimeOut(int timeOut) {
		// ������룬timeOut��λΪ��
		if ((System.currentTimeMillis() - lastAccessTime) / 1000 > timeOut) {
			Logger.info(this + " is timeout.");
			destroy();
			return true;
		}
		return false;
	}

}
