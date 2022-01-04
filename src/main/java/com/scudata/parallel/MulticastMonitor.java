package com.scudata.parallel;

import java.net.*;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.resources.ParallelMessage;

/**
 * �ಥ������������鲥�ھ�������Ӧ��
 * �������ķֻ���������
 * @author Joancy
 *
 */
public class MulticastMonitor extends Thread {
	public static String MULTICAST_HOST = "231.0.0.1";
	public static int MULTICAST_PORT = 18281;
	public static int MULTICAST_PORT2 = 18282;
	
//�㲥����Ϣһ�����������ڵ����ʹ��REPLY_LIVENODES��Ϊǰ׺Ӧ��
//�����Ϊ����˸��ͻ��������͵���ͨ��Ϣ	
	public static String LIST_LIVENODES = "List live nodes";
	public static String REPLY_LIVENODES="REPLY_LIVENODES";

	private String host = null;
	private int port = -1;

	private volatile boolean stop = false;
	private MulticastListener listener;

	private boolean isServer = false;
	static MessageManager mm = ParallelMessage.get();
	final static boolean localDebug = false;
	
	/**
	 * ��ȡ��ǰ�ಥ��������IP������Ϣ
	 */
	public String toString(){
		return "["+host+":"+port+"]";
	}

	/**
	 * ��Ⱥ����Ķಥ��ʹ����һ�׵�ַ
	 */
	public void setClusterManager(){
		MULTICAST_HOST = "231.0.0.2";
		MULTICAST_PORT = 18381;
		MULTICAST_PORT2 = 18382;
	}
	
	/**
	 * ���õ�ǰ������������IP���˿���Ϣ
	 * @param host IP��ַ
	 * @param port �˿ں�
	 */
	public void setHost(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * ֹͣ��ǰ�ಥ����
	 */
	public void stopThread() {
		stop = true;
	}

	/**
	 * ����һ���ಥ����߳�,���ø�ȱʡ������
	 * ���캯����ʾ�öಥ����Ϊ�����
	 */
	public MulticastMonitor() {
		isServer = true;
	}
	
	/**
	 * ʹ�öಥ���������һ���ಥ����߳�
	 * ��ʱ�Ķಥ�����Ϊ�ͻ���
	 * @param listener �ಥ�����
	 */
	public MulticastMonitor(MulticastListener listener) {
		this.listener = listener;
		this.setName(toString());
	}

	/**
	 * ���������ڷ��͹㲥��Ϣ��Ѱ�һ�Ľڵ��
	 */
	public void broadcast() {
		send(LIST_LIVENODES);
	}

	private void send(String message) {
		try {
			int multicastPort = MULTICAST_PORT;
			if (isServer) {
				multicastPort = MULTICAST_PORT2;
			}
			InetAddress ip = InetAddress.getByName(MULTICAST_HOST);
			MulticastSocket ms = new MulticastSocket();
			ms.joinGroup(ip);
			DatagramPacket packet = new DatagramPacket(message.getBytes(),
					message.length(), ip, multicastPort);
			ms.send(packet);
			Logger.debug("Use port:"+multicastPort+" broad a message��"+message);
			ms.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handle(DatagramPacket packet) {
		try {
			String message = new String(packet.getData(), 0, packet.getLength());
			Logger.debug("Receive message��"+message);
			if (message.equals(LIST_LIVENODES)) {
				if (host == null) {
					return;
				}
				send(REPLY_LIVENODES+host + ":" + port);
				return;
			}
			else{
//				ֻ�пͻ��˲���listener
				if (listener == null) {
					return;
				}
				if(message.startsWith(REPLY_LIVENODES)){
					message = message.substring(REPLY_LIVENODES.length());
					String unitHost = UnitClient.parseHost(message);
					if (unitHost == null) {
						return;
					}
					int unitPort = UnitClient.parsePort(message);
					listener.addUnitClient(unitHost, unitPort);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	boolean isRunning = false;
	boolean isRunning(){
		return isRunning;
	}
	
	public void run() {
		try {
			int multicastPort = MULTICAST_PORT2;
			String thisHost = UnitContext.getDefaultHost();
			String msg = "Host�� "+ thisHost;
			int waitTime = 1000;
			if (isServer) {
				waitTime = 3000;
				multicastPort = MULTICAST_PORT;
			}

			MulticastSocket ms = new MulticastSocket(multicastPort);
			ms.setTrafficClass(0x04);
			ms.setSoTimeout( waitTime );
			InetAddress ip = InetAddress.getByName(MULTICAST_HOST);
			ms.joinGroup(ip);
			Logger.debug(msg + " has started multicast listening.");
			while (!stop) {
				isRunning = true;
				try{
					byte[] data = new byte[256];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					ms.receive(packet);
					handle( packet );
				} catch (java.net.SocketTimeoutException ste) {
				}
			}
			ms.close();
			Logger.debug(msg+ " halt multicast listening.");
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	
}
