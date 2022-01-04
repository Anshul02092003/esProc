package com.scudata.server.unit;

import java.util.List;

import com.scudata.common.Logger;
import com.scudata.dm.Context;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;
import com.scudata.server.ConnectionProxyManager;
import com.scudata.server.IProxy;
import com.scudata.util.DatabaseUtil;

/**
 * ���Ӵ������
 * 
 * @author Joancy
 *
 */
public class ConnectionProxy extends IProxy
{
	Context context;
	String spaceId;
	boolean closed = false;

	/**
	 * ����һ�����Ӵ���
	 * @param cpm ���Ӵ��������
	 * @param id ������
	 * @param spaceId �ռ���
	 */
	public ConnectionProxy(ConnectionProxyManager cpm, int id, String spaceId){
		super(cpm, id);
		this.spaceId = spaceId;
		context = new Context();
		List connectedDsNames = null;
		UnitServer us = UnitServer.instance;
		if( us != null ){
			if(us.getRaqsoftConfig()!=null){
				connectedDsNames = us.getRaqsoftConfig().getAutoConnectList();
			}
		}
		DatabaseUtil.connectAutoDBs(context, connectedDsNames);

		JobSpace js = JobSpaceManager.getSpace(spaceId);
		context.setJobSpace(js);
		access();
		Logger.debug(this+" connected.");
	}
	
	/**
	 * ����id��ȡStatement������
	 * @param id ������
	 * @return Statement������
	 * @throws Exception
	 */
	public StatementProxy getStatementProxy(int id) throws Exception{
		StatementProxy sp = (StatementProxy)getProxy(id); 
		if(sp==null){
			throw new Exception("Statement "+id+" is not exist or out of time!");
		}
		return sp; 
	}
	
	/**
	 * ��ȡ���㻷��������
	 * @return ����������
	 */
	public Context getContext(){
		return context;
	}
	/**
	 * ��ȡ����ռ��
	 * @return �ռ���
	 */
	public String getSpaceId(){
		return spaceId;
	}
	
	/**
	 * �ж������Ƿ��ѹر�
	 * @return �رշ���true�����򷵻�false
	 */
	public boolean isClosed(){
		return closed;
	}
	
	/**
	 * �ص���ǰ���Ӵ�����
	 */
	public void close() {
		closed = true;
		Logger.debug(this+" closed.");
	}

	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString() {
		return "Connection "+getId();
	}
	
}