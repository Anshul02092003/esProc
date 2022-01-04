package com.scudata.dm;

import java.util.ArrayList;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.parallel.UnitClient;
import com.scudata.resources.ParallelMessage;

/**
 * �ڴ���������
 * hosts(;j)	ȡ�����ֻ�������j���ڴ����ţ�j��ʡ��
 * hosts(i;j)	���ñ��ֻ�������j���ڴ����ţ�i=0��ʾ�������j���ڴ�����
 * hosts(n,hs;j)	��hs���ҳ�hosts(;j)����ֵΪ1,��,n�Ŀ��÷ֻ����С���ȱʧ������hosts()Ϊ�յķֻ�����Ӧȱʧִֵ�г�ʼ��(init.dfx)���Ҳ����㹻��ֻ����ؿ�
 * n==0ʱ���ؿ��÷ֻ��������÷ֻ���λ�����null
 * 
 * @author Joancy
 */
public class ZoneLoader {
	private Integer I = null;
	private String J = null;
	
	private int N = 0;
	private Machines hs = null;
	

	// �Ѿ��ҵ��˼������Ļ�������Ҫִ��dfx���صĻ������ܰ����Ѿ��ҵ��������Ļ����������ظ�ָ��ͬһip�˿ڵĻ���ȷҪ���ǲ�ͬ�Ļ�����
	private transient ArrayList<UnitClient> dispatchedNodes = new ArrayList<UnitClient>();
	static MessageManager mm = ParallelMessage.get();

	/**
	 * ����һ���ڴ���������
	 */
	public ZoneLoader() {
	}

	/**
	 * ���ò���
	 * @param i ��������j���ڴ����ţ�i=0��ʾ�������j���ڴ�����
	 * @param j ��������ʡ����null
	 */
	public void setArgs(Integer i, String j) {
		this.I = i;
		this.J = j;
	}

	/**
	 * ���ò���
	 * @param n n==0ʱ���ؿ��÷ֻ��������÷ֻ���λ�����null
	 * @param hs �ֻ���
	 * @param j  ������
	 */
	public void setArgs(Integer n, Machines hs, String j) {
		if(n!=null && n>0){
			this.N = n;
		}
		this.hs = hs;
		this.J = j;
	}
	
	/**
	 * ����ָ���ķֻ��ͻ���
	 * @param nodes �ֻ��ͻ����б�
	 * @throws Exception ���ӳ����׳��쳣
	 */
	public static void connectNodes(ArrayList<UnitClient> nodes)
			throws Exception {
		for (int i = 0; i < nodes.size(); i++) {
			UnitClient uc = (UnitClient) nodes.get(i);
			uc.connect();
		}
	}

	/**
	 * �ص��ֻ��ͻ��˵�����
	 * @param nodes �ֻ��ͻ����б�
	 */
	public static void closeNodes(ArrayList<UnitClient> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			UnitClient uc = (UnitClient) nodes.get(i);
			uc.close();
		}
	}
	
	/**
	 * ��ָ���ķֻ��������ҳ������˵Ļ���
	 * @param deadAsNull û�������ķֻ�ʹ��null�������
	 * @return �ֻ��ͻ����б�
	 * @throws Exception �г����̳����׳��쳣
	 */
	public ArrayList<UnitClient> listLiveClients( boolean deadAsNull) throws Exception {
		ArrayList<UnitClient> liveNodes = new ArrayList<UnitClient>();
		StringBuffer reason = new StringBuffer();
		for (int i = 0; i < hs.size(); i++) {
			UnitClient uc = new UnitClient(hs.getHost(i), hs.getPort(i));
			if (uc.isAlive(reason)) {
				liveNodes.add(uc);
			}else if( deadAsNull ){
				liveNodes.add(null);
			}
		}
		return liveNodes;
	}
	
	/**
	 * �ɹ�������zone��Ӧ�ķֻ����У����򷵻�null
	 * @return �ֻ�����
	 */
	public Object execute() {
		if(hs!=null){
			return getMachines();
		}
		if(I==null){
//			ȡ�����ֻ�������j���ڴ����ţ�j��ʡ��
			return Env.getAreaNo(J);
		}
		Env.setAreaNo( J, I );
		return true;
	}
	
	private Sequence getMachines(){ 
		ArrayList<UnitClient> liveNodes = null;
		try {
			Sequence nodes = new Sequence();
//			n=0���������л�ķֻ�,����ķֻ���nullռλ
			if( N==0 ){
				ArrayList<UnitClient> stateNodes = listLiveClients(true);
				for(UnitClient uc:stateNodes){
					String desc = uc==null?null:uc.toString();
					nodes.add( desc );
				}
				return nodes;
			}
			
			liveNodes = listLiveClients( false );
			if (liveNodes.isEmpty()) {
				Logger.debug(new Exception(mm.getMessage("ZoneLoader.noAlives")));
				return null;
			}
			if(liveNodes.size()<N){
				Logger.debug( new Exception(mm.getMessage("ZoneLoader.notEnoughAlives",liveNodes.size())));
				return null;
			}
			
			// ���ڷ����ľ��ⲻ���ݻ��������ִ��dfx�����������⣬���Ǹ��ݵ�ǰ����ƽ�����䵽��ķֻ���
			connectNodes(liveNodes);
			
			ArrayList<Integer> areaNos = new ArrayList<Integer>();
			for (int i = 0; i < liveNodes.size(); i++) {
				UnitClient uc = liveNodes.get(i);
				Integer areaNo = uc.getAreaNo(J);
				areaNos.add( areaNo );
			}
			
//				�Ȱ��ڴ���˳�򣬽��Ѿ����ع��ķֻ��ҳ���
			boolean lackZone = false;
			for (int i = 1; i <= N; i++) {
				Integer zone = i;
				int index = areaNos.indexOf( zone );
				if( index==-1 ){
					nodes.add(null);
					Logger.debug("Data zone: "+i+" is not found.");
					lackZone = true;
				}else{
					UnitClient uc = liveNodes.get(index); 
					nodes.add( uc );
					Logger.debug("Found zone: "+i+" on "+uc);
					dispatchedNodes.add(uc);
				}
			}
//				�ٲ����м�����ȱʧ�ķֻ���֮����Ҫ�����沽����ɣ���Ҫ���Ѿ�ʹ�õķֻ��޳�
			if(lackZone){
				for (int i = 1; i <= N; i++) {
					UnitClient uc = (UnitClient)nodes.get(i);
					if(uc==null){
						for(int n=0;n<liveNodes.size();n++){
							uc = liveNodes.get(n);
							if(!dispatchedNodes.contains(uc)){
								uc.initNode(i, N, J);
								nodes.set(i, uc);
								dispatchedNodes.add(uc);
								break;
							}
						}
					}
				}
			}
			return nodes;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception x) {
			throw new RuntimeException(x.getMessage(), x);
		} finally {
			if (liveNodes != null) {
				closeNodes(liveNodes);
			}
		}
	}

}
