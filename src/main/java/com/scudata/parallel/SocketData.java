package com.scudata.parallel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import com.scudata.common.Logger;
import com.scudata.dm.Env;

/**
 * ��װ�˲���������������׽���
 * ʹ�øö���󣬿ͻ��˺ͷ���˿���ֱ�ӽ��и�Ч�����ݴ���
 * @author Joancy
 *
 */
public class SocketData{
	int socketBuf = Env.getFileBufSize();
	private Socket socket;
	
	private ObjectOutputStream oos=null;
	private ObjectInputStream ois=null;
	
	/**
	 * ����socket����һ�������׽���
	 * @param socket �׽��ֶ���
	 * @throws Exception �������ʱ�׳��쳣
	 */
	public SocketData(Socket socket) throws Exception{
		this.socket = socket;
		socket.setTcpNoDelay(true);
		socket.setKeepAlive(true);
		socket.setReceiveBufferSize(socketBuf);
		socket.setSendBufferSize(socketBuf);
		socket.setSoLinger(true, 1);
		socket.setReuseAddress(true);
	}
	
	/**
	 * �ͻ��˴����׽������ݺ���Ҫ�����ͻ���ͨѶ��
	 * @throws IOException ����ʱ�׳��쳣
	 */
	public void holdCommunicateStreamClient() throws IOException{
		OutputStream os = socket.getOutputStream();
		// ���Խ����socket�Ľ��ջ���Խ��ԭ����Խ�죻�����ļ����Ķ�д��û�л�����ʱ��
		//�ٶ�ҲԽ�죬���Ҷ���������һ�������úܴ�ʱ�����ڴ��������ע��
		// ���ڴ����ݷ���ʱ���л��������
		BufferedOutputStream bos = new BufferedOutputStream(os);// ,socketBuf,���˻����size��������ò��Ǻܴ�
		oos = new ObjectOutputStream(bos);
		oos.flush();//������������flush������������дʱ������
		
		InputStream is = socket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		ois = new ObjectInputStream(bis);
	}

	/**
	 * ����˴����׽������ݺ���Ҫ���������ͨѶ��
	 * @throws IOException ����ʱ�׳��쳣
	 */
	public void holdCommunicateStreamServer() throws IOException{
		InputStream is = socket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		ois = new ObjectInputStream(bis);

		OutputStream os = socket.getOutputStream();
		// ���Խ����socket�Ľ��ջ���Խ��ԭ����Խ�죻�����ļ����Ķ�д��û�л�����ʱ���ٶ�ҲԽ�죬���Ҷ���������һ�������úܴ�ʱ�����ڴ��������ע��
		// ���ڴ����ݷ���ʱ���л��������
		BufferedOutputStream bos = new BufferedOutputStream(os);// ,socketBuf,���˻����size��������ò��Ǻܴ�
		oos = new ObjectOutputStream(bos);
		oos.flush();//������������flush������������дʱ������
		
	}

	/**
	 * ���ӵ��׽��ֵ�ַ
	 * @param endpoint �׽��ֵ�ַ
	 * @param timeout ���ӳ�ʱ��ʱ�䣬��λ����
	 * @throws Exception �����쳣
	 */
	public void connect(SocketAddress endpoint,
            int timeout) throws Exception{
		try{
			socket.connect( endpoint, timeout);
			holdCommunicateStreamClient();
		}catch(Exception e){
			throw new Exception(endpoint+":"+e.getMessage(),e);
		}
	}
	
	/**
	 * ��ȡԭʼ�׽���
	 * @return �׽��ֶ���
	 */
	public Socket getSocket(){
		return socket;
	}
	
	/**
	 * д��һ������
	 * @param obj ���ݶ���
	 * @throws IOException д�����쳣
	 */
	public void write(Object obj)
			throws IOException {
		oos.writeUnshared(obj);
		oos.flush();
		oos.reset();//д�������ڴ棬�����ڴ����
	}

	/**
	 * ������������
	 * @return ���ݶ���
	 * @throws IOException IO�쳣
	 * @throws ClassNotFoundException ���쳣
	 */
	public Object read()
			throws IOException, ClassNotFoundException {
		Object obj = ois.readUnshared();
		return obj;
	}

	/**
	 * �鿴�׽����Ƿ�ر�
	 * @return �ر�ʱ����true�����򷵻�false
	 */
	public boolean isClosed(){
		return socket.isClosed();
	}
	
	/**
	 * �������˹ر�ʱ���ø÷������ͷ��׽���
	 * @throws Exception �ر��쳣
	 */
	public void serverClose() throws Exception{
		socket.close();
	}

	/**
	 * �ͻ��˹ر�ʱ���ø÷������ͷ��׽���
	 * �ͻ�����Ҫ�ȷ���null����֪ͨ������˳��߳�
	 * @throws Exception �ر��쳣
	 */
	public void clientClose() throws Exception{
		write(null);//�ص������̶߳˵�socket��ͨѶ���Ѿ��ж���
		serverClose();
	}
}