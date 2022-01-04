package com.scudata.server;

import java.util.ArrayList;

import com.scudata.common.Logger;

/**
 * ����ӿڳ�����
 * 
 * @author Joancy
 *
 */
public abstract class IProxy
{
	int id = -1;
	IProxy parent = null;
	public ArrayList<IProxy> subProxies=null;
	long lastAccessTime = -1;
	
	/**
	 * ���캯��
	 * @param parent ������
	 * @param id Ψһ���
	 */
	public IProxy(IProxy parent, int id){
		this.parent = parent;
		this.id = id;
	}
	
	/**
	 * ˢ�´���ķ���ʱ��
	 */
	public void access() {
		lastAccessTime = System.currentTimeMillis();
		if(parent!=null){
			parent.access();
		}
	}

	/**
	 * ���ô���ķ���ʱ��
	 */
	public void resetAccess() {
		lastAccessTime = -1;
	}
	
	/**
	 * ���ٴ������
	 */
	public synchronized void destroy(){
//		�ȹ��Ӵ���
		if(subProxies!=null){
			for(int i=0;i<subProxies.size();i++){
				IProxy p = subProxies.get(i);
				p.destroy();
			}
			subProxies.clear();
		}
//		�ٹ��Լ�
		close();
//		�Ӹ������Ƴ��Լ�
		if(parent!=null){
			parent.removeProxy(this);
		}
	}
	
	/**
	 * ׷��һ���Ӵ���
	 * @param proxy �Ӵ������
	 */
	public synchronized void addProxy(IProxy proxy){
		if(subProxies==null){
			subProxies = new ArrayList<IProxy>();
		}
		subProxies.add(proxy);
	}
	
	/**
	 * �Ƴ��Ӵ���
	 * @param proxy �Ӵ������
	 */
	public synchronized void removeProxy(IProxy proxy){
		if(subProxies!=null){
			subProxies.remove(proxy);
		}
	}

	/**
	 * ���ݱ�Ż�ȡ�Ӵ���
	 * @param id ���
	 * @return �Ӵ������
	 */
	public synchronized IProxy getProxy(int id){
		if(subProxies==null){
			return null;
		}
		for(int i=0;i<subProxies.size(); i++){
			IProxy sub = subProxies.get(i);
			if(sub.getId()==id){
				return sub;
			}
		}
		return null;
	}

	/**
	 * ��鵱ǰ�����Ƿ���ʳ�ʱ
	 * @param timeOut ��ʱʱ��
	 * @return ��ʱ�����ٶ��󣬷���true�����򷵻�false
	 */
	public synchronized boolean checkTimeOut(int timeOut) {
		if(subProxies!=null){
			for(int i=0;i<subProxies.size(); i++){
				IProxy sub = subProxies.get(i);
				sub.checkTimeOut(timeOut);
			}
		}
		if (lastAccessTime < 0) {
			return false; // ��û����������ܼ�����
		}
		// ������룬timeOut��λΪ��
		long unvisit = (System.currentTimeMillis() - lastAccessTime) / 1000;
		if (unvisit > timeOut) {
			Logger.debug(this + " is timeout.");
			destroy();
			return true;
		}
		return false;
	}
	
	/**
	 * ��ȡ������
	 * @return ���
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * ��ȡ������
	 * @return ���������
	 */
	public IProxy getParent(){
		return parent;
	}
	
	/**
	 * ��ȡ�Ӵ�����ܸ���
	 * @return �Ӵ������
	 */
	public synchronized int size(){
		if(subProxies==null) return 0;
		return subProxies.size();
	}
	
	/**
	 * �رյ�ǰ�����ͷ���Դ
	 */
	public abstract void close();
	
	/**
	 * ��ȡ��������������Ϣ
	 */
	public abstract String toString();
}
