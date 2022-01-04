package com.scudata.dm;

import java.text.DecimalFormat;
import java.util.*;

import com.scudata.common.*;
import com.scudata.dm.UnitTasks.UnitTask;
import com.scudata.expression.Expression;
import com.scudata.parallel.*;
import com.scudata.thread.ThreadPool;

/**
 * ʵ�ֲ������ú���callx(dfx,��;hs;rdfx)�Ĳ���������
 * ��ֻ�����hs������ҵִ�нű�dfx���̳�����ռ䣻����ÿ����ҵ�ķ���ֵ���ɵ����С�
 * ��ҵ����������(end msg)�����·��䣨�ܿ��ѷ�����ֻ���������Ҳ������÷ֻ�������ʧ�ܣ���ҵ���䵽�����÷ֻ�ʱ����Ϊ����������
 * ȱʡ�����η��䣬����i����ҵ���䵽hs.m@r(i)��
 * rdfx�����������Ľű�������reduce��������ʡ�ԡ�������ֱ��ǵ�ǰ�ۻ�ֵ�͵�ǰ����ֵ��rdfx����ֵ����Ϊ�µ��ۻ�ֵ����ʼ�ۻ�ֵΪnull
 * ��rdfxʱ������ֵ����ÿ���ֻ��ķ���ֵ���ɵ����У���hs�Ĵ���
 * @author Joancy
 *
 */
public class ParallelCaller extends ParallelProcess {
	// ��Ϊ�����Ӻ�nodeһ�������ͻ�����ռ�����ӣ������㷨��ֱ��������
	// �������㷨����ֱ��ʹ��nodes�������� ucList�еĶ��зֻ��������㷨��nodes���˷ѳ�ʼ����
	private UnitClient[] nodes = null;

	private Context ctx;
	private int activeHostCount = 0;

	private final byte TYPE_DEFAULT = 0;//ȱʡ˳�η��䣬������ֻ�
	private final byte TYPE_ONE = 1;//һ����ҵȫ�����䣬���ķ��غ��ж�������ҵ
	private final byte TYPE_RANDOM = 2;//�������
//	private final byte TYPE_MUCH = 3;//Ԥ����Ȼ������ҵ

	private String opt = null;

	/**
	 * ����һ���������ö���
	 * @param dfx �������
	 * @param hosts �ֻ�
	 * @param ports �˿�
	 */
	public ParallelCaller(Object dfx, String[] hosts, int[] ports) {
		super(dfx);
		if (hosts == null)
			return;// �����߳�ִ��ʱ

		nodes = new UnitClient[hosts.length];
		for (int i = 0; i < hosts.length; i++) {
			UnitClient uc = new UnitClient(hosts[i], ports[i]);
			try {
				uc.connect();
			} catch (Exception x) {
				Logger.severe(uc + ":" + x.getMessage());
			}
			nodes[i] = uc;
		}
	}

	/**
	 * ���ü���ѡ��
	 * @param ops ѡ��
	 */
	public void setOptions(String ops) {
		this.opt = ops;
	}

	/**
	 * ���������
	 * @return �������ѡ���true
	 */
	public boolean isAOption() {
		return opt != null && opt.indexOf('a') != -1;
	}

	/**
	 * ֻ��һ����ҵҲ��������п��÷ֻ����κ�һ��������ɣ��ж���������
	 * @return һ����ҵѡ���true
	 */
	public boolean is1Option() {
		return opt != null && opt.indexOf('1') != -1;
	}

	/**
	 * ȱʡ˳�η���
	 * @return ȱʡ˳�η��䷵��true
	 */
	public boolean isDefaultOption() {
		return opt == null;
	}
	/**
	 * ���ü��㻷���������Ķ���
	 * @param ctx ������
	 */
	public void setContext(Context ctx) {
		this.ctx = ctx;
		ctx.addResource(this);
	}

	// �÷���������ҷֻ�����
	private void randomUCList() {
		Sequence sortSeq = new Sequence();
		while(!ucList.isEmpty()){
			sortSeq.add(ucList.removeFirst());
		}
		Expression exp = new Expression("rand()");
		sortSeq.sort(exp, null, "o", new Context());
		for(int i=1;i<=sortSeq.length();i++){
			ucList.add((UnitClient)sortSeq.get(i));
		}
	}

	private byte getCalcType() {
		if (isAOption()) {
			return TYPE_RANDOM;
		}
		if (is1Option()) {
			return TYPE_ONE;
		}
		return TYPE_DEFAULT;
	}

	/**
	 * ִ����ҵ�����ؼ�����
	 * @return ������
	 */
	public Object execute() {
		if (nodes == null) {
			//�������߳�ִ��
			return super.execute();
		}
		try {
			checkLiveNodes();
			checkCallerSize();

			byte calcType = getCalcType();
			Object result = null;
			switch (calcType) {
			case TYPE_ONE:
				result = executeOne();
				break;
//			case TYPE_MUCH:
//				result = executeMuch();
//				break;
			default:
				result = execute( calcType );
			}
			return result;
		} finally {
			closeConnects();
		}
	}
	
	/**
	 * ֻ��һ����ҵʱ��ʹ��1ѡ����䵽���зֻ���ȡ���Ľ���󷵻�
	 * @return ������
	 */
	public Object executeOne() {
		Logger.debug("1����ҵʱ���ָ����зֻ���ȡ���Ľ��");
		ThreadPool pool = null;
		try {
			List<ProcessCaller> pCallers = new ArrayList<ProcessCaller>();
			ucList = new LinkedList<UnitClient>();

			int size = nodes.length;
			// ��Ҫ�ݴ��ҳ���ķֻ��������ݴ����
			for (int i = 0; i < size; i++) {
				UnitClient uc = nodes[i];
				if (!uc.isConnected()) {
					continue;
				}
				appendClient(uc, false);
				activeHostCount++;
			}

			Sequence argPos = new Sequence();
			argPos.add(1);
			List<List> args = reserveResult(argPos);
//			����ҵ�ָ����еķֻ�
			for (int i = 1; i <= size; i++) {
				ProcessCaller pcaller = new ProcessCaller(args);
				pcaller.setPositions(argPos);
				pcaller.setOneOption();
				pCallers.add(pcaller);
			}

			callers.clear();
			for (ProcessCaller pc : pCallers) {
				callers.add(pc);
			}
			// �����Ĺ����أ�ֱ���Ƿֻ�����Ŀ
			int poolSize = ucList.size();
			pool = ThreadPool.newSpecifiedInstance(poolSize);
			for (int i = 0; i < size; i++) {
				if (isCanceled)
					continue;
				ProcessCaller pcaller = (ProcessCaller) callers.get(i);
				UnitClient uc = getClient();
				pcaller.setUnitClient(uc);
				pool.submit(pcaller);
			}
			joinCallers();

			return result;
		} catch (Throwable x) {
			if (x instanceof OutOfMemoryError) {
				Logger.severe(x);
			}
			interruptAll(null, x);

			if(x.getMessage().equals(ParallelProcess.ONE_OPTION)){
//				1ѡ�����������׳�ONE_OPTION�쳣����ϱ����ҵ��Ȼ��ֱ�ӷ��ؽ����
				return result;
			}
			throw new RuntimeException(x);
		} finally {
			if (pool != null) {
				pool.shutdown();
			}
		}
	}

	/**
	 * ȱʡ˳�η���
	 * @return ������
	 */
	public Object execute(byte calcType) {
		String msg;
		if(calcType==TYPE_DEFAULT){
			msg = "ȱʡ˳�η���";
		}else{
			msg = "���������ҵ";
		}
		Logger.debug( msg );
		ThreadPool pool = null;
		try {
			List<ProcessCaller> pCallers = new ArrayList<ProcessCaller>();
			ucList = new LinkedList<UnitClient>();

			int size = nodes.length;
			int maxTaskNum = 0;
			HashMap<String, Integer> ucTaskMap = new HashMap<String, Integer>();
			// ��Ҫ�ݴ��ҳ���ķֻ��������ݴ����
			for (int i = 0; i < size; i++) {
				UnitClient uc = nodes[i];
				if (!uc.isConnected()) {
					continue;
				}
				int maxNum = uc.getUnitMaxNum();
				if (maxNum > maxTaskNum) {
					maxTaskNum = maxNum;
				}
				ucTaskMap.put(uc.toString(), maxNum);
				appendClient(uc, false);
				activeHostCount++;
			}

			// ���ݷֻ��������ҵ��������ֻ�����ҵ����
			size = callers.size();
			for (int i = 1; i <= maxTaskNum; i++) {
				for (int n = 0; n < activeHostCount; n++) {
					UnitClient tmp = ucList.get(n);
					Integer tmpMax = ucTaskMap.get(tmp.toString());
					if (i < tmpMax) {
						// ����ķֻ���ȱʡ��¡
						appendClient(tmp);
					}
				}
			}
			
			// Ϊ�˷�ֹ������ҵȫ�����ڵ�һ̨����ʱ������б�û��ȫ���ճ������˶����Ա�֤����ĸ������Ե��ڷֻ�����
			if (reduce != null) {
				int maxIndex = nodes.length;
				result.set(maxIndex, null);
			}

			for (int i = 1; i <= size; i++) {
				Sequence argPos = new Sequence();
				argPos.add(i);
				List<List> args;
				if (reduce != null) {
//					��ǰ��û�з���ֻ�������1�ŷֻ�ռ���طֻ�ռλ��
					args = reserveResult(argPos,1);
				} else {
//					����Ҫreduceʱ�����ؽ�����������ռλ
					args = reserveResult(argPos);
				}
				
				ProcessCaller pcaller = new ProcessCaller(args);
				if (reduce == null) {
					pcaller.setPositions(argPos);
				}
				// �����ݴ�
				pcaller.setDispatchable(true);
				pCallers.add(pcaller);
			}

			callers.clear();
			for (ProcessCaller pc : pCallers) {
				callers.add(pc);
			}
			
			if(calcType==TYPE_RANDOM){
				randomUCList();
			}
			
			// �����Ĺ����أ�������ҵ�����ֻ��������ҵ������ȡС�ķ���
			int poolSize = Math.min(size, ucList.size());
			pool = ThreadPool.newSpecifiedInstance(poolSize);
			for (int i = 0; i < size; i++) {
				if (isCanceled)
					continue;
				ProcessCaller pcaller = (ProcessCaller) callers.get(i);
				UnitClient uc = getClient();
				if (reduce != null) {
//					int ucIndex = indexOfUC(uc);
					pcaller.setReduce(reduce, accumulateLocation,currentLocation);//,ucIndex);
				}
				pcaller.setUnitClient(uc);
				pool.submit(pcaller);
			}
			joinCallers();

			if (reduce != null) {
//				��reduceʱ�����Ӹ��ֻ���reduce�Ľ��ȡ��
				for(int i=0;i<nodes.length;i++){
					UnitClient uc = nodes[i];
					if(!uc.isAlive()){
						continue;
					}
					Object reduceResult = uc.getReduceResult(spaceId);
					int ucIndex = indexOfUC(uc);
					result.set(ucIndex,reduceResult);
				}
			}
			return result;
		} catch (Throwable x) {
			if (x instanceof OutOfMemoryError) {
				Logger.severe(x);
			}
			interruptAll(null, x);
			if (x instanceof RQException) {
				throw (RQException) x;
			}
			throw new RuntimeException(x.getMessage(), x);
		} finally {
			if (pool != null) {
				pool.shutdown();
			}
		}
	}


	/*
	 * ����˳��Ŵ�1��ʼ�ķֻ����
	 * */
	private int indexOfUC(UnitClient uc) {
		for (int i = 0; i < nodes.length; i++) {
			UnitClient tmp = nodes[i];
			if (uc.equals(tmp)) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * ������ҵ�㷨�������ݴ����ò���x����s
	 * @return ������
	 */
//	public Object executeMuch() {
//		Logger.debug(mm.getMessage("ParallelCaller.much"));
//
//		ThreadPool pool = null;
//		ArrayList<Caller> leftCallers = null;
//		try {
//			float threshold = 0.8f;
//			int size = callers.size();
//			int leftSize = size - (int)(size * threshold);
//			int dispatchedSize = size - leftSize;
//			ucList = new LinkedList<UnitClient>();
//			// �����ֵ��������ҵ����2���������ʣ����ҵ�������㷨
//			if (leftSize > 2) {
//				ArrayList<Caller> tmpCallers = new ArrayList<Caller>();
//				leftCallers = new ArrayList<Caller>();
//				int threashNum = size - leftSize;
//				for (int i = 0; i < size; i++) {
//					if (i < threashNum) {
//						tmpCallers.add(callers.get(i));
//					} else {
//						leftCallers.add(callers.get(i));
//					}
//				}
//				callers = tmpCallers;
//			}
//
//			// Ϊ�˷�ֹ������������ʱ��������ҵȫ�����ڵ�һ̨����ʱ������б�û��ȫ���ճ������˶����Ա�֤����ĸ������Ե��ڷֻ�����
//			if (reduce != null) {
//				int maxIndex = nodes.length;
//				result.set(maxIndex, null);
//			}
//
//			List<ProcessCaller> pCallers = new ArrayList<ProcessCaller>();
//
//			HashMap<UnitClient, Sequence> ucTaskMap = dispatchTask();
//			Iterator<UnitClient> ucs = ucTaskMap.keySet().iterator();
//			while (ucs.hasNext()) {
//				UnitClient uc = ucs.next();
//				Sequence argPos = ucTaskMap.get(uc);
//				if (nodes.length > 1) {
//					Logger.debug(mm.getMessage("ParallelCaller.dispatchedS",uc, argPos.toExportString()));
//				}
//				int ucIndex = indexOfUC(uc);
//				List<List> args = reserveResult(argPos, ucIndex);
//				ProcessCaller pcaller = new ProcessCaller(args);
//				if (reduce != null) {
//					pcaller.setReduce(reduce, ucIndex);
//				} else {
//					pcaller.setPositions(argPos);
//				}
//				appendClient(uc, false);
//				pCallers.add(pcaller);
//			}
//
//			callers.clear();
//			for (ProcessCaller pc : pCallers) {
//				callers.add(pc);
//				pc.join();
//			}
//
//			size = callers.size();
//			pool = ThreadPool.newSpecifiedInstance(size);
//			int loopSize = size;
//			if (leftCallers != null) {
//				leftSize = leftCallers.size();
//				Logger.debug(mm.getMessage("ParallelCaller.leftJob", leftSize));
//
//				loopSize += leftSize;
//				for (int i = 1; i <= leftSize; i++) {
//					Sequence argPos = new Sequence();
//					argPos.add(i);
//					List<List> args = reserveResult(leftCallers, argPos, 1);
//					ProcessCaller pcaller = new ProcessCaller(args);
//					if (reduce != null) {
//						pcaller.setMainReduce(reduce);
//					} else {
//						Sequence argPos2 = new Sequence();
//						argPos2.add(i + dispatchedSize);
//						pcaller.setPositions(argPos2);
//					}
//
//					callers.add(pcaller);// ��Ҫ������ĵ�����ҵ׷�ӵ�callers
//				}
//			}
//
//			for (int i = 0; i < loopSize; i++) {
//				Caller caller = callers.get(i);
//				caller.setUnitClient(getClient());
//				pool.submit(caller);
//			}
//			
//			joinCallers();
//			return result;
//		} catch (Throwable x) {
//			if (x instanceof OutOfMemoryError) {
//				Logger.severe(x);
//			}
//			interruptAll(null, x);
//			if (x instanceof RQException) {
//				throw (RQException) x;
//			}
//			throw new RuntimeException(x.getMessage(), x);
//		} finally {
//			if (pool != null) {
//				pool.shutdown();
//			}
//		}
//
//	}


	void closeConnects() {
		super.closeConnects();

		if (nodes == null)
			return;
		for (UnitClient uc : nodes) {
			if (uc == null)
				continue;
			uc.close();
		}
	}

	private void checkLiveNodes() {
		int len = nodes.length;
		boolean existLive = false;

		for (int i = 0; i < len; i++) {
			UnitClient uc = nodes[i];
			if (!uc.isConnected())
				continue;
			existLive = true;
		}

		if (!existLive) {
			throw new RQException(mm.getMessage("UnitTasks.noActiveHost"));
		}
	}

	private List<List> reserveResult(Sequence posIndexes) {
		return reserveResult(posIndexes, null);
	}

	private List<List> reserveResult(Sequence posIndexes, Integer ucIndex) {
		return reserveResult(callers, posIndexes, ucIndex);
	}

	private List<List> reserveResult(ArrayList<Caller> acs,
			Sequence posIndexes, Integer ucIndex) {
		List<List> args = new ArrayList<List>();
		for (int i = 1; i <= posIndexes.length(); i++) {
			int pos = (Integer) posIndexes.get(i);
			Caller c = acs.get(pos - 1);
			args.add(c.getArgs());
		}

		if (reduce != null) {
			if (ucIndex != null) {
				result.set(ucIndex, null);
			} else {
				result.add(null);
			}
		} else {
			for (int i = 0; i < posIndexes.length(); i++) {
				result.add(null);
			}
		}
		return args;
	}
	
//���ֻ��������ҵ��һ����ҵ�����ж������������reduce
	class ProcessCaller extends Caller implements IResource {
		private Object reduce = null;
		private CellLocation accumulateLocation = null;
		private CellLocation currentLocation = null;
		
//		private int argPosition = 1;
		private Sequence argPositions = null;
		private boolean isDispatchable = false;

		Context pcCtx = null;
		Expression pcExp = null;

		public ProcessCaller(List<List> argList) {
			super(argList);
		}

		public void setDispatchable(boolean d) {
			isDispatchable = d;
		}
		
		public void setUnitClient(UnitClient uc) throws Exception {
			this.uc = uc;

			Request req = new Request(Request.DFX_TASK);
			req.setAttr(Request.TASK_DfxName, dfx);
			req.setAttr(Request.TASK_ArgList, argList);
			req.setAttr(Request.TASK_SpaceId, spaceId);
			req.setAttr(Request.TASK_IsProcessCaller, true);
			req.setAttr(Request.TASK_Reduce, reduce);
			req.setAttr(Request.TASK_AccumulateLocation, accumulateLocation);
			req.setAttr(Request.TASK_CurrentLocation, currentLocation);

			Response res = uc.send(req);
			if (res.getException() != null) {
				throw res.getException();
			}
			taskId = (Integer) res.getResult();
			registResource();
		}

		private void registResource() {
			ctx.getJobSpace().addHosts(uc.getHost(), uc.getPort());
			ctx.addResource(this);
		}

		public void setReduce(Object reduce, CellLocation accumulateLocation, CellLocation currentLocation){//, int argPos) {
			this.reduce = reduce;
			this.accumulateLocation = accumulateLocation;
			this.currentLocation = currentLocation;
//			this.argPosition = argPos;
		}

		/**
		 * �����Ĵ�����ҵʱ�����ȷֳ�һ����ҵ���ֻ�����ʱ��һ����ҵ���ڷֻ�reduce
		 * ��ʣ�������ҵ�����������˵���MainReduce�����ֻ���һ����ҵ���صĽ���ٴ�reduce
		 * @param reduce
		 */
//		public void setMainReduce(String reduce) {
//			pcCtx = new Context();
//			pcExp = new Expression(pcCtx, reduce);
//			this.reduce = REDUCE_MAIN;
//		}

		public void setPositions(Sequence argPos) {
			this.argPositions = argPos;
		}

//		void reduceMain(int index, Object rVal) {
//			synchronized (reduceResults) {
//				Object preVal = result.get(index);
//				if (preVal == null) {
//					preVal = rVal;
//				} else {
//					preVal = reduce(preVal, rVal, pcExp, pcCtx);
//				}
//				result.set(index, preVal);
//			}
//		}

		void setResponseValue(Object rVal) {
			int index = 0;
			if (reduce != null) {
//				if (reduce.equals(REDUCE_MAIN)) {
//					index = indexOfUC(uc);
//					Object tmp = rVal;
//					if (rVal instanceof Sequence) {
//						Sequence sval = (Sequence) rVal;
//						tmp = sval.get(1);// �˴�ȥ���ֻ�ֵ���������
//					}
//					reduceMain(index, tmp);
//				} else if (reduce == REDUCE_NULL) {
//					// ������������ҵʱ��ʹ��REDUCE_NULL���߷ֻ�������reduce�����Ƿֻ�������ֵ�϶��ǰ���һ��Ԫ�ص�����
//					index = argPosition;
//					Object tmp = rVal;
//					if (rVal instanceof Sequence) {
//						Sequence sval = (Sequence) rVal;
//						tmp = sval.get(1);// �˴�ȥ���ֻ�ֵ���������
//					}
//					setResult(index, tmp);
//				} else {
//					index = argPosition;
//					setResult(index, rVal);
//				}
			} else {
				Sequence pos = argPositions;
				Sequence val = null;
				if (rVal instanceof Sequence) {
					val = (Sequence) rVal;
				} else {
					// ������С��host.lengthʱ��Ҳ�ǲ��ö������������������������������ڴ�ʱÿ̨�ֻ����һ����ҵ��
					// ���Է���ֵ����Sequence����ʱ��argPositionsֻ��Ϊһ����Ա�����У��˴���resultҲת��Ϊ
					// ֻ��һ����Ա�����С�
					val = new Sequence();
					val.add(rVal);
				}

				for (int i = 1; i <= pos.length(); i++) {
					index = (Integer) pos.get(i);
					Object tmp = null;
					if (i <= val.length()) {
						tmp = val.get(i);
					} else {
						Logger.severe(mm.getMessage("ParallelCaller.emptysub"));//"�ӳ��򷵻ص�ֵΪ�գ�");
					}
					setResult(index, tmp);
				}
			}
		}

		public void close() {
			interruptAll(this, new Exception(TERMINATE));
			if (uc != null) {
				uc.close();
			}
			ctx.removeResource(this);
		}

		// ������ķֻ������������·���
		private transient HashSet<UnitClient> errorNodes = new HashSet<UnitClient>();

		public void run() {
			if (!isDispatchable) {
				super.run();
				return;
			}

			isRunning = true;
			try {
				while (true) {
					if (isCanceled) {
						Logger.debug(mm.getMessage("ParallelProcess.canceled",
								this));
						// + " is canceled.");
						break;
					}
					try {
						long l1 = System.currentTimeMillis();
						Logger.debug(mm.getMessage("Task.taskBegin", this));
						runOnNode();
						long l2 = System.currentTimeMillis();
						DecimalFormat df = new DecimalFormat("###,###");
						long lastTime = l2 - l1;
						Logger.debug(mm.getMessage("Task.taskEnd", this,
								df.format(lastTime)));// this+" ��ʼ���㡣");
						break;
					} catch (RetryException re) { // ��Ҫ���·���ֻ��쳣ʱ
						releaseClient(uc);
						if (!errorNodes.contains(uc)) {
							errorNodes.add(uc.clone());
						}
						try {
							UnitClient tmpuc = getDispatchNode(errorNodes,
									args2String(argList), re.getMessage());
							setUnitClient(tmpuc);
							Logger.debug(mm.getMessage(
									"ParallelProcess.reassign", this));
						} catch (Exception ex) {
							interruptAll(this, ex);
							break;
						}
					}
				}
			} catch (Throwable t) {
				interruptAll(this, t);
			} finally {
				releaseClient(uc);
				isRunning = false;
			}
		}
	}

	/**
	 * ��ȡһ������ִ�еķֻ�,�����ݴ�ĳ��ִ���е������������жϻ��߷ֻ������ȣ� ���·���ֻ�
	 * 
	 * @return UnitClient �ֻ��ͻ���
	 */
	public UnitClient getDispatchNode(HashSet<UnitClient> errorNodes,
			String argString, String cause) throws Exception {
		if (errorNodes.size() == activeHostCount) {
			throw new Exception(mm.getMessage("ParallelProcess.exeFail",
					argString, cause));
		}
		UnitClient uc = getClient();
		while (contains(errorNodes, uc)) {
			releaseClient(uc);
			uc = getClient();
		}
		return uc;
	}

	// �÷���ֻ���������̶˿ں����Ƚϣ�����ͬһ���ֻ����ӽ��̣�����ͬ�ġ�
	// errorNodes.contains��Ƚ�hashKey���Ὣ�ӽ��̶˿�Ҳ���ȥ
	private boolean contains(HashSet<UnitClient> errorNodes, UnitClient uc) {
		Iterator<UnitClient> nodes = errorNodes.iterator();
		while (nodes.hasNext()) {
			UnitClient tmp = nodes.next();
			if (tmp.equals(uc)) {
				return true;
			}
		}
		return false;
	}

}
