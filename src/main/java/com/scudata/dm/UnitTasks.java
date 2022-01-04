package com.scudata.dm;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.parallel.UnitClient;
import com.scudata.resources.ParallelMessage;

/**
 * ��Ӧ��callx�Ķ������
 * @author Joancy
 *
 */
public class UnitTasks {
	ArrayList<UnitTask> uts = new ArrayList<UnitTask>();
	static MessageManager mm = ParallelMessage.get();
	
	/**
	 * ���캯��
	 * @param ucs �ڵ���б�
	 * @throws Exception ��������ӳ��쳣
	 */
	public UnitTasks(ArrayList<UnitClient> ucs) throws Exception{
		int len = ucs.size();
		for(int i=0;i<len; i++){
			UnitClient uc = ucs.get(i);
			UnitTask ut = new UnitTask(uc);
			uts.add(ut);
		}
	}
	
	/**
	 * ���캯��
	 * @param nodes �ڵ������
	 * @throws Exception ����ʱ�ӳ��쳣
	 */
	public UnitTasks(UnitClient[] nodes) throws Exception{
		int len = nodes.length;
		for(int i=0;i<len; i++){
			UnitClient uc = nodes[i];
			String h = null;
			if(uc!=null){
				h = uc.getHost();
			}
			UnitTask ut;
			if(h==null){
				ut = new UnitTask(null);
			}else{
				if(!uc.isConnected()){
					ut = new UnitTask(null);
				}else{
					ut = new UnitTask(uc);
				}
			}
			uts.add(ut);
		}
	}
	/**
	 * ��ȡ��ǰ��������Ľڵ��
	 * �÷���ִ�к󣬽ڵ������������ʺ���ҵ������һ��ֱ����������ڵ������
	 * @return ��������ڵ��
	 */
	public UnitClient getMaxCapacityUC(){
		return getMaxCapacityUC(null);
	}
	
	/**
	 * ��ָ����ŵĽڵ�����ҳ���������Ľڵ��
	 * @param ucIndexes ָ�����λ�õĽڵ��
	 * @return ��������ڵ��
	 */
	public UnitClient getMaxCapacityUC(ArrayList<Integer> ucIndexes){
		UnitTask ut = getMaxCapacityUT(ucIndexes);
		int prefer = ut.preferredNum;
		ut.addTaskNum(prefer);
		return ut.getUnitClient();
	}
	
	/**
	 * ��ȡ�ڵ�����
	 * utIndexΪ Sequence�����õ�1��ʼ����ţ���������Ҫ��ȥ1
	 * @param utIndex
	 * @return
	 */
	public UnitTask getUnitTask(int utIndex){
		return uts.get(utIndex-1);
	}

	/**
	 * ʵ�ִ����ӿ�
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(UnitTask ut:uts){
			if(sb.length()>0){
				sb.append(",");
			}
			sb.append(ut);
		}
		return sb.toString();
	}
	/**
	 * ��ȡ��ָ����Χ�Ļ������ҳ���������Ľڵ�����
	 * �÷������ؽڵ�����󣬲��Զ����ӽڵ��������������Ҫ������
	 * UnitTask.addTaskNum(n)����������
	 * @param ucIndexes��ָ����Χ�ķֻ�����б�
	 * @return �ڵ�����
	 */
	public UnitTask getMaxCapacityUT(ArrayList<Integer> ucIndexes){
		if(ucIndexes==null){
			return getMaxCapacityUT();
		}
		UnitTask ut = uts.get(ucIndexes.get(0));
		double c = ut.capacity();
		for(int i=1;i<ucIndexes.size();i++){
			UnitTask tmp = uts.get(ucIndexes.get(i));
			double dc = tmp.capacity();
			if(dc>c){
				c = dc;
				ut = tmp;
			}
		}
		return ut;
	}
	
	/**
	 * �ڵ�ǰȫ����Χ�Ļ������ҳ���������
	 * @return �ڵ�����
	 */
	public UnitTask getMaxCapacityUT(){
		UnitTask ut = uts.get(0);
		double c = ut.capacity();
		for(int i=1;i<uts.size();i++){
			UnitTask tmp = uts.get(i);
			double dc = tmp.capacity();
			if(dc>c){
				c = dc;
				ut = tmp;
			}
		}
		return ut;
	}
	
	
	class UnitTask{
		int preferredNum = 0;
		int currentNum = 0;
		UnitClient uc;
		
		UnitTask(UnitClient uc) throws Exception{
			this.uc = uc;
			if(uc!=null){
				int[] tasks = uc.getTaskNums();
				preferredNum = tasks[0];
				currentNum = tasks[1];
			}
		}
		
		UnitClient getUnitClient(){
			return uc;
		}
		
		public String toString(){
			return uc.toString();
		}
		
		double capacity(){
			return preferredNum*1.0f/(currentNum+0.5f);
		}
		
		public void addTaskNum(int tasks){
			currentNum += tasks;
		}
	}

}
