package com.scudata.parallel;

import java.util.*;

import com.scudata.common.CellLocation;
import com.scudata.dm.ParallelProcess;
import com.scudata.server.unit.UnitServer;
import com.scudata.thread.ThreadPool;

/**
 * ��ҵ������(����������ɡ�δ��ɡ�ȡ������ҵ)����Ҫ���ڼ��
 * 
 * @author Joancy
 *
 */
public class TaskManager {
	static ArrayList<ITask> tasks = new ArrayList<ITask>();

//	�ֻ�����������������
	static ThreadPool pool=null;
	public static ThreadPool getPool(){
		if(pool==null){
			HostManager hostManager = HostManager.instance();
			int size = hostManager.getMaxTaskNum();
			pool = ThreadPool.newSpecifiedInstance(size); 
		}
		return pool;
	}
	
	/**
	 * ִ�м�������
	 * @param req ����
	 * @return ��Ӧ
	 */
	public static Response execute(Request req) {
		int cmd = req.getAction();
		int taskId;
		Task task;
		Response res = new Response();
		try {
			switch (cmd) {
			case Request.DFX_TASK:
				Object dfx = req.getAttr(Request.TASK_DfxName);
				List args = (List) req.getAttr(Request.TASK_ArgList);
				String spaceId = (String) req.getAttr(Request.TASK_SpaceId);
				boolean isProcessCaller = false;
				Boolean B = (Boolean) req.getAttr(Request.TASK_IsProcessCaller);
				if(B!=null){
					isProcessCaller = B.booleanValue();
				}
				Object reduce = req.getAttr(Request.TASK_Reduce);
				CellLocation accumulateLocation = (CellLocation)req.getAttr(Request.TASK_AccumulateLocation);
				CellLocation currentLocation = (CellLocation)req.getAttr(Request.TASK_CurrentLocation);
				taskId = UnitServer.nextId();
				task = new Task(dfx, args, taskId, spaceId, isProcessCaller,reduce,accumulateLocation,currentLocation);
				Integer processTaskId = (Integer) req.getAttr(Request.TASK_ProcessTaskId);
				if(processTaskId!=null){
					task.setProcessTaskId(processTaskId);
				}
				
				addTask(task);
				res.setResult(new Integer(taskId));
				break;
			case Request.DFX_CALCULATE:
				taskId = ((Number) req.getAttr(Request.CALCULATE_TaskId))
						.intValue();
				task = (Task)getTask(taskId);
				
				getPool().submit( task );
				task.join();
				res = task.getResponse();
				break;
			case Request.DFX_CANCEL:
				taskId = ((Number) req.getAttr(Request.CANCEL_TaskId))
						.intValue();
				String cancelReason = (String)req.getAttr(Request.CANCEL_Reason);
				task = (Task)getTask(taskId);
				res = task.cancel( cancelReason );
				break;
			case Request.DFX_GET_REDUCE:
				spaceId = (String) req.getAttr(Request.GET_REDUCE_SpaceId);
				res.setResult(ParallelProcess.getReduceResult(spaceId));
				break;
			}
		} catch (Exception x) {
			res.setException(x);
		}

		return res;
	}

	/**
	 * ��������
	 * @param t ����
	 */
	public synchronized static void addTask(ITask t) {
		tasks.add(t);
	}

	/**
	 * ɾ������
	 * @param taskId �����
	 */
	public synchronized static void delTask(int taskId) {
		for (int i = 0; i < tasks.size(); i++) {
			ITask t = tasks.get(i);
			if (t.getTaskID() == taskId) {
				tasks.remove(i);
				break;
			}
		}
	}

	/**
	 * ���������ȡ���������
	 * @param taskId ����� 
	 * @return �������
	 * @throws Exception ����ʱ�׳��쳣
	 */
	public synchronized static ITask getTask(int taskId) throws Exception {
		for (int i = 0; i < tasks.size(); i++) {
			ITask t = tasks.get(i);
			if (t.getTaskID() == taskId) {
				return t;
			}
		}
		throw new Exception("Task:" + taskId + " is timeout.");
	}

	/**
	 * ȡ�����б�ĸ���
	 * @return �����б�
	 */
	public synchronized static List<ITask> getTaskList() {
		ArrayList<ITask> al = new ArrayList<ITask>();
		al.addAll(tasks);
		return al;
	}

	/**
	 * ������ʱ
	 */
	public synchronized static void checkTimeOut(int proxyTimeOut) {
		for (int i = tasks.size() - 1; i >= 0; i--) {
			ITask t = tasks.get(i);
			if (t.checkTimeOut(proxyTimeOut)) {
				t.close();
				tasks.remove(t);
			}
		}
	}

}
