package com.scudata.parallel;

import com.scudata.common.MessageManager;
import com.scudata.dm.Env;
import com.scudata.dm.ZoneManager;
import com.scudata.resources.ParallelMessage;
import com.scudata.server.unit.UnitServer;

/**
 * �ڵ����ҵ�߳�
 * 
 * @author Joancy
 *
 */
public class UnitWorker extends Thread {
	SocketData socketData;
	
//	������socket�Ŀͻ����Ƿ�ͨ���˰�������֤
	boolean errorCheck = false;
	String clientIP=null;
	
	private volatile boolean stop = false;

	/**
	 * ����һ����ҵ�߳�
	 * @param tg �߳���
	 * @param name �߳�����
	 */
	public UnitWorker(ThreadGroup tg, String name){
		super(tg,name);
	}
	
	public void setErrorCheck(String clientIP){
		errorCheck = true;
		this.clientIP = clientIP;
	}
	
	/**
	 * ���������׽���ͨѶ����
	 * @param sd �����׽���
	 */
	public void setSocket(SocketData sd){
		this.socketData = sd;
	}

	/**
	 * �߳�����
	 */
	public void run() {
		try {
			Response response = null;
			while (!stop) {
				Object obj = socketData.read();

				if (obj == null || !(obj instanceof Request)) {
					break;
				}
				Request req = (Request) obj;
				switch (req.getActionType()) {
				case Request.TYPE_DFX:
					setName("UnitWorker[execute dfx]:"+req);
					if(errorCheck){
						response = new Response();
						MessageManager mm = ParallelMessage.get();
						Exception error = new Exception(mm.getMessage("UnitWorker.errorcheck",clientIP));
						response.setException(error);
						break;
					}else{
						response = TaskManager.execute(req);
					}
					break;
				case Request.TYPE_CURSOR:
					setName("UnitWorker[serve cursor]:"+req);
					int taskId = ((Number) req.getAttr(Request.METHOD_TaskId)).intValue();
					try {
						RemoteCursorProxyManager rcpm;
						if(taskId==-1){
//							��Ⱥ�α�û������ţ�ʹ�þ�̬���α������
							rcpm = RemoteCursorProxyManager.getInstance();
						}else{
							ITask t = TaskManager.getTask(taskId);
							rcpm = t.getCursorManager();
						}
						response = rcpm.execute(req);
					} catch (Exception x) {
						response = new Response();
						response.setException(x);
					}
					break;
				case Request.TYPE_FILE:
					setName("UnitWorker[serve file]:"+req);
					//Ϊ����߶�ȡ�ļ����ٶȲ����ڶ��ļ��Ĺ����в���request,response���ʴ�ʽ��������Ҫ
					//��socketData����д�ļ����ݣ��������������Ҫ��socketData������Ӧ�����������·���partition����ͬ��
					response = RemoteFileProxyManager.execute(req, socketData);
					break;
				case Request.TYPE_PARTITION:
					setName("UnitWorker[serve partition]:"+req);
					response = PartitionManager.execute(req, socketData);
					break;
				case Request.TYPE_ZONE:
					setName("UnitWorker[ZONE]:"+req);
					response = ZoneManager.execute(req);
					break;
				case Request.TYPE_UNITCOMMAND:
					setName("UnitWorker[UnitCommand]:"+req);
					UnitCommand uc = (UnitCommand)req.getAttr(Request.EXE_Object);
					response = uc.execute();
					break;
				case Request.TYPE_JDBC:
					setName("UnitWorker[JDBC]:"+req);
					response = com.scudata.server.unit.JdbcManager.execute(req);
					break;
				default: // Type Server
					setName("UnitWorker[execute cmd]:"+req);
					response = UnitServer.getInstance().execute(req);
				}

				socketData.write(response);
			}
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			try {
				socketData.serverClose();
			} catch (Exception x) {
			}
		}
	}

	/**
	 * ֹͣ�߳���ҵ
	 */
	public void shutdown() {
		stop = true;
	}

	/**
	 * ʵ��toString������������Բ鿴�߳���Ϣ
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("UnitWorker:");
		sb.append(socketData.getSocket().getRemoteSocketAddress());
		sb.append("-"+socketData.getSocket().hashCode());
		return sb.toString();
	}


}
