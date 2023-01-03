package com.scudata.dm;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import com.scudata.cellset.INormalCell;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.*;
import com.scudata.expression.Expression;
import com.scudata.parallel.*;
import com.scudata.resources.*;
import com.scudata.server.unit.UnitServer;
import com.scudata.thread.Job;
import com.scudata.thread.ThreadPool;
import com.scudata.util.CellSetUtil;

/**
 * ��������
 * ��Ӧ��callx����H�����Σ��ڱ��������ϣ�ģ���߳�ִ������
 * �ֻ��ӵ���pCallerҲ�Ǹ���ִ�У�pCaller����һ�δ���һ�����߶������
 * @author Joancy
 *
 */
public class ParallelProcess implements IResource{
	public static String ONE_OPTION = "1ѡ���������ɡ�";
	
	Object dfx;// dfx������1��dfx�ļ����֣�2��һ�δ��룻3��һ��PgmCellSet����
	// host���� ʡ�ԣ�ʡ��ʱ���ӱ������߳�ִ��
	String spaceId = null;
	Object reduce=null;// reduce ������һ�����ļ���������һ��PgmCellSet
	CellLocation accumulateLocation=null;//�������λ��Ϊnullʱ�� ��ʹ�õ�1���������ۼ�ֵ
	CellLocation currentLocation=null;// �������λ��Ϊnullʱ�� ��ʹ�õ�2����������ǰֵ

	
	ArrayList<Caller> callers = new ArrayList<Caller>();
	Sequence result = new Sequence();

	private volatile boolean interrupt = false;
	private Throwable interruptException;

	HostManager hostManager = HostManager.instance();
	
//	�ݴ��㷨ʱ���ö��л����uc���ʺ���ҵ�����������������ʼ�����ӣ��õ���ʱ�Ż���������
//	��������uc�� һ���ʺ���ҵ3��һ���ʺ���ҵ4�����������г���Ϊ7�ķֻ�����
	LinkedList<UnitClient> ucList = null;
	
	transient volatile boolean isCanceled = false;
	String TERMINATE = "Terminated by user.";
	
	//�����̵�����Id
	private int processTaskId = 0;
	static MessageManager mm = ParallelMessage.get();
	static Map<String,Object> reduceResults = Collections.synchronizedMap(new HashMap<String,Object>());
	
	/**
	 * ָ���������Ĺ��캯��
	 * @param dfx ������Ķ���  dfx������
	 * 1��dfx�ļ����֣�
	 * 2��һ��SPL���룻
	 * 3��һ��PgmCellSet����
	 */
	public ParallelProcess(Object dfx) {
		if (dfx instanceof String || dfx instanceof PgmCellSet) {
			this.dfx = dfx;
		} else {
			String className = dfx.getClass().getName();
			throw new RuntimeException(
					"ParallelCaller does not support class type:" + className);
		}
		TERMINATE = mm.getMessage("ParallelProcess.terminate");
	}
	/**
	 * ���õ�ǰ���̵������
	 * @param pTaskId �����
	 */
	public void setProcessTaskId(int pTaskId){
		this.processTaskId = pTaskId;
	}
	
	/**
	 * ʵ��toString������������ʾ��ǰ���̵Ĳ�������
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(dfxDelegate(dfx));
		sb.append("   ");
		int size = callers.size();
		List<Object> args = new ArrayList<Object>();
		for(int i=0;i<size; i++){
			args.addAll(callers.get(i).getArgs());
		}
		sb.append(args2String(args));
		return sb.toString();
	}

	private static String getListString(List list){
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		int size = list.size();
		int min = 2;
		min = Math.min(min, size);
		for (int i = 0; i < min; i++) {
			if (i > 0) {
				sb.append(",");
			}
			Object mem = list.get(i);
			if(mem instanceof List){
				sb.append(getListString((List)mem));
			}else{
				sb.append( mem );
			}
		}
		if(min<size){
			sb.append("...");
			Object mem = list.get(size-1);
			if(mem instanceof List){
				sb.append(getListString((List)mem));
			}else{
				sb.append( mem );
			}
		}
		sb.append(" ]");
		return sb.toString();
	}
	
	void closeConnects(){
		if(ucList==null) return;
		for(UnitClient uc:ucList){
			uc.close();
		}
	}
	
	/**
	 * �������б�Ķ������ת��Ϊ�ɶ���
	 * @param argList �����б�
	 * @return ת���Ĵ�
	 */
	public static String args2String(List argList) {
		StringBuffer sb = new StringBuffer();
		if (argList != null) {
			MessageManager mm = EngineMessage.get();
			sb.append(mm.getMessage("callx.arg"));
			sb.append(" = ");
			List sub = argList;
			if(argList.size()==1 && argList.get(0) instanceof List){
				sub = (List)argList.get(0);
			}

			sb.append( getListString(sub) );
		}
		return sb.toString();
	}

	/**
	 * ��������ռ��
	 * @param id ����ռ�
	 */
	public void setJobSpaceId(String id) {
		this.spaceId = id;
	}
	
	/**
	 * ��������ִ�����reduce���ʽ
	 * @param reduce reduce���ʽ����reduce�������
	 */
	public void setReduce(Object reduce){
		if(reduce==null){
			return;
		}
		if(reduce instanceof PgmCellSet){
			this.reduce = (PgmCellSet)reduce;
		}else{
			String dfx = (String)reduce;
			DfxManager dfxManager = DfxManager.getInstance();
			FileObject fo = new FileObject(dfx, "s");
			PgmCellSet pcs = dfxManager.removeDfx(fo, new Context());
			this.reduce = pcs;
		}
	}
	
	/**
	 * ��������reduce����
	 * @param reduce �������reduce����
	 * @param accumulateLocation �ۼ�ֵλ��
	 * @param currentLocation    ��ǰֵλ��
	 */
	public void setReduce(Object reduce,CellLocation accumulateLocation, CellLocation currentLocation){
		setReduce(reduce);
		this.accumulateLocation = accumulateLocation;
		this.currentLocation = currentLocation;
	}

	/**
	 * �жϵ�ǰ��dfxarg��һ���ļ�������һ������
	 * 
	 * @param dfxarg dfx����
	 * @return �����һ�νű�������true�����򷵻�false
	 */
	public static boolean isScript(Object dfxarg) {
		if (!(dfxarg instanceof String))
			return false;
		String dfx = (String) dfxarg;
		boolean b = dfx.indexOf('\t') > 0 || dfx.indexOf('\n') > 0;
		if (b)
			return true;// ���д���϶��ǽű�
		b = dfx.toLowerCase().startsWith("return ");// ֻ��һ�д���ʱ��д�˷��ص�Ҳ�ǽű�
		if (b)
			return true;
		String lowDfx = dfx.toLowerCase();
		b = lowDfx.endsWith(".dfx") || lowDfx.endsWith(".splx");
		return !b;// ���� 123 �����ű�����
	}

	/**
	 * ��ȡdfx�������д��ʾ�����ڴ����ʾ���ߵ�����Ϣ
	 * ��dfx��һ������ʱ����ʾ��Ϣֻ��ʾͷʮ����ĸ����
	 * @param dfx �������
	 * @return ��д��ʾ�����������
	 */
	public static String dfxDelegate(Object dfx) {
		boolean isScript = isScript(dfx);
		if (isScript) {
			String str = (String) dfx;
			if (str.length() > 10) {
				return str.substring(0, 10) + "...";
			}
		} else if (dfx instanceof PgmCellSet) {
			PgmCellSet pcs = (PgmCellSet) dfx;
			if(pcs.getName()==null) return "PgmCellSet";
			return "PgmCellSet[" + pcs.getName() + "]";
		}
		return (String) dfx;
	}

	/**
	 * ���ü������
	 * @param dfx
	 */
	public void setDfx(Object dfx) {
		this.dfx = dfx;
	}

	/**
	 * ����һ������ҵ
	 * @param argList ����ҵ��ִ�в���
	 * @throws RQException ���ڲ�����Ҫ�����л����͵���ķֻ�ȥִ�У�
	 * ����ûʵ�����л��ӿڵĲ������׳��쳣
	 */
	public void addCall(List<Object> argList) throws RQException {
		for (Object obj : argList) {
			if(obj==null){
				continue;
			}
			if (obj instanceof Serializable) {
				continue;
			}
			throw new RQException(mm.getMessage("ParallelProcess.invalidarg",obj));
		}
		Caller caller = new Caller(argList);
		callers.add(caller);
	}

	private int indexOf(Caller caller) {
		for (int i = 0; i < callers.size(); i++) {
			if (callers.get(i).equals(caller)) {
				return i + 1;
			}
		}
		return 0;
	}

	void setResult(int index, Object val) {
		synchronized ( reduceResults ) {
			result.set(index, val);
		}
	}

	/**
	 * ȡ��ִ�е�ǰ��ҵ
	 * @param reason ȡ����ҵ��ԭ��
	 */
	public void cancel(String reason){
		isCanceled = true;
		if(reason!=null){
			TERMINATE = reason;
		}
		int size = callers.size();
		for (int i = 0; i < size; i++) {
			Caller caller = callers.get(i);
			caller.cancel();
		}
	}
	
	void joinCallers() throws Throwable{
		try {
			int size = callers.size();
			for (int i = 0; i < size; i++) {
				Caller caller = callers.get(i);
				caller.join();
			}
		} catch (Exception x) {
			interruptAll(null, x);
			throw x;
		}

		if (interrupt) {
			throw interruptException;
		}
	}

	void checkCallerSize() {
		int size = callers.size();
		if (size == 0) {
//		û�в���Ҳ�ܵ���callx�����ڽ�����Ҫִ�зֻ���һ��dfx��������Ҫ���������Ρ�
			Caller caller = new Caller(new ArrayList());
			callers.add(caller);
		}
	}
	
	/**
	 * ��ȡ��ǰ��ҵ�µ��߳���Ŀ�����ڵ���
	 * @return �̼߳���
	 */
	public static int threadCount() {
		return threadCount(false);
	}
	
	/**
	 * ��ȡ�߳���Ŀ
	 * @param showName ����ǰ�̵߳�����ͬʱ��ӡ������̨�����Ե���
	 * @return �̼߳���
	 */
	public static int threadCount(boolean showName) {
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		if(showName){
		 Iterator<Thread> it = map.keySet().iterator();
		 while(it.hasNext())
			 System.out.println( it.next() );
		}

		return map.size();
	}
	
	/**
	 * ִ�е�ǰ��ҵ
	 * @return ������
	 */
	public Object execute(){
		checkCallerSize();
		int size = callers.size();
		
		int min = Env.getParallelNum();
		ThreadPool pool = ThreadPool.newSpecifiedInstance( min );
		try {
			for (int i = 0; i < size; i++) {
				if( isCanceled ) continue;
				if( !needReduce() ){
					result.add(null);
				}
				Caller caller = callers.get(i);
//				ÿ���ֽ��̵�UCֻ��һ�������ǹ���ģ�����ÿ���ֽ���ִ�е���ҵֻ����һ����Ҳ���Ǵ˴���֤�˷ֽ��̵Ĵ�����ҵ
//				UnitClient uc = getClient();
				caller.setUnitClient( null );//ȫ��Ϊ�����߳�ִ��
				pool.submit(caller);
			}
			joinCallers();
			return getResult();
		}catch(RetryException re){
			throw re;
		}catch (Throwable x) {
			interruptAll(null, x);
			throw new RuntimeException(x);
		}finally{
			pool.shutdown();
			closeConnects();
		}
	}
	
	boolean needReduce(){
		return !(reduce==null);
	}
	
	/**
	 * reduce���������֣�һ��ֱ�ӱ��ʽ����
	 * ��һ��ʹ���������ò���������
	 * @return ����Ǳ��ʽ�����reduce����true
	 */
	boolean isExpReduce(){
		return (reduce instanceof String);
	}
	
	/**
	 * ������������ҵ��������ɺ󣬸���SPACEID����ȡ�ֻ��ϵ�reduce�����ս��
	 * ���ȡ���ͬʱ��շֻ��Ľ�����档
	 * @param SPACEID ����Ŀռ�ID
	 * @return ������ĳ�ֻ��ϵ�reduce���
	 */
	public static Object getReduceResult(String SPACEID){
		Object val = reduceResults.get(SPACEID);
		reduceResults.remove(SPACEID);
		return val;
	}
	
	Object getResult(){
		if( needReduce() ){
//			reduce�ĵ���������������ҵ�з��أ����Ǽ���reduceResults�У�������ҵֱ�ӷ���true��������reduce�Ľ���������
			return true;
		}else{
			return result;
		}
	}
	
	Object reducePgmCellSet(Object prevValue, Object curValue)throws Exception{
		Context context = Task.prepareEnv();

		PgmCellSet pcs = (PgmCellSet)reduce;
		pcs.setContext(context);
		if(accumulateLocation==null){//û��λ����Ϣʱ����ʹ�ò���λ��
			CellSetUtil.putArgValue(pcs, new Object[]{prevValue,curValue});
		}else{
			int row = accumulateLocation.getRow();
			int col = accumulateLocation.getCol();
			INormalCell nc = pcs.getCell(row, col);
			nc.setValue(prevValue);

			row = currentLocation.getRow();
			col = currentLocation.getCol();
			nc = pcs.getCell(row, col);
			nc.setValue(curValue);
		}
		pcs.calculateResult();
		return pcs.nextResult();
	}
	
	Object reduce(Object prevValue, Object curValue, Expression exp, Context ctx){
		Param param = ctx.getIterateParam();
		Object oldVal = param.getValue();
		param.setValue(prevValue);
		
		Sequence tmp = new Sequence(1);
		tmp.add(curValue);
		ComputeStack stack = ctx.getComputeStack();
		stack.push(new Current(tmp, 1));
		
		try {
			Object val = exp.calculate(ctx);
			return val;
		} finally {
			param.setValue(oldVal);
			stack.pop();
		}
	}

	// ��ĳ�ִ���ʱ���������
	void interruptAll(Caller master,Throwable x) {
		isCanceled = true;
		if (interrupt) {
			return; // �����߳��д�ʱ��һ����Ϲ��������̴߳��󶼺���
		}
		interrupt = true;
		interruptException = x;
		TERMINATE = x.getMessage();
		
		int size = callers.size();
		
//Dispatchable Executeʱ��ÿ���ֻ�ֻ����һ����ҵ����ʱ��ȻҲ�д�ϣ�����ʵ���Ǵ���Լ�
//����ִ�к���������caller�Ĵ���		
		if(size==1){
			return;
		}
		boolean needCancel = false;
		for (int i = 0; i < size; i++) {
			Caller caller = callers.get(i);
				if(caller.cancel()){
					needCancel = true;
				}
		}
		
		if( !needCancel ) return;
		Logger.info(mm.getMessage("ParallelProcess.cancelfor",this,TERMINATE));
		
		while(true){
			boolean isAllFinished = false;
			for (int i = 0; i < size; i++) {
				Caller caller = callers.get(i);
//				����������Ĵ���Ҫ�ȴ����������Դ˴��ķ���CallerҪ����
				if(caller!=master){
					if(caller.isRunning()){
						isAllFinished = false;
						break;
					}
				}
				isAllFinished = true;
			}
			if(isAllFinished){
				break;
			}
		}
	}

/**
 * ׷��һ���ֿͻ���uc�����뵽���е�uc������dispatchable��־
 * @param uc �ֻ��ͻ���
 */
	public void appendClient(UnitClient uc){
		appendClient(uc,true);
	}
	
	/**
	 * ׷��һ������ķֻ��ͻ��ˣ�
	 * ��Ҫ�ݴ�Ŀͻ��ˣ��൱����һ��uc���ݣ�Ҳ�и����ʺ���ҵ����չ��uc����Ҫ��¡����
	 * @param uc �ͻ���
	 * @param needClone �Ƿ���Ҫ��¡�ͻ���
	 */
	public void appendClient(UnitClient uc,boolean needClone){
		UnitClient ucClone = uc;
		//
		if(needClone){
			ucClone = uc.clone();
		}
		try{
			ucClone.setDispatchable();
			ucList.add(ucClone);
		}catch(Exception x){
//		����socket�ܵ�������Դ���ƣ����Լ�ʹ���Ѿ������ϵ�node��Ҳ��Ȼ���ܽ��������ӳ���
		}
	}
	
	/**
	 * ��ҵ������ɺ��ͷſͻ���
	 * @param uc �ͻ���
	 */
	public void releaseClient(UnitClient uc) {
		if(uc==null || ucList==null){
			return;
		}
//		������ǴӶ��������ȡ����UC���ͷ�ʱ�����뵽����
		if(!uc.isDispatchable()){
			return;
		}
		synchronized(ucList) {
			ucList.add(uc);
			ucList.notify();
		}
	}
	
	/**
	 * �ͻ��˶��������ȡһ�����Լ���Ķ���ʵ��
	 * getClient����������releaseClient�ɶ�ʹ��
	 * @return ����п���uc�����򷵻����������ȴ�uc���ͷ�
	 */
	public UnitClient getClient(){
//		û��дhost����ʱ��ֱ���ڱ��ض��߳�ִ�С�ֱ�ӷ���null�ͻ���
		if(ucList==null){
			if(hostManager.getHost()==null){//��������ڷֻ���ִ��ʱ�����Ǳ����߳�
				return null;
			}
		}
		
		synchronized(ucList) {
			if (ucList.size() == 0) {
				try {
					ucList.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
			return  ucList.removeFirst();
		}
		
	}
	
	
	class Caller extends Job{
		List<?> argList;
		UnitClient uc = null;
		Integer taskId = null;
		private boolean isOneOption = false;//�����1ѡ���ǰ��ҵ������ɺ󣬾������ж���������ҵ
		
		transient boolean isRunning = false;
		public Caller(List<?> argList) {
			this.argList = argList;
		}
		
		public void setOneOption(){
			isOneOption = true;
		}

		
		boolean isRunning(){
			return isRunning;
		}
		
		public List<?> getArgs(){
			return argList;
		}

		public void setUnitClient(UnitClient uc) throws Exception {
			this.uc = uc;
			if (canRunOnLocal()) {
				// ������Ա���ִ��ʱ������Ҫ�ӷֻ���ȡ����ţ�Ҳ���������ֻ��ĳ�����
				taskId = UnitServer.nextId();
				return;
			}

			Request req = new Request(Request.DFX_TASK);
			req.setAttr(Request.TASK_DfxName, getDfxObject());
			req.setAttr(Request.TASK_ArgList, argList);
			req.setAttr(Request.TASK_SpaceId, spaceId);
			req.setAttr(Request.TASK_ProcessTaskId, processTaskId);
			
			Response res = uc.send(req);
			if (res.getException() != null) {
				throw res.getException();
			}
			taskId = (Integer) res.getResult();
		}

		public boolean cancel() {
			isCanceled = true;
			if(!isRunning) return false;
			if (canRunOnLocal()) {
				try {
					Task t = (Task)TaskManager.getTask(taskId);
					t.cancel( TERMINATE );
				} catch (Exception x) {
				}
			} else {
				uc.cancel(taskId, TERMINATE );
			}
			return true;
		}

		public void breakOff() { // ����Ͽ�ʱ���ر����ͨ����
			try {
				uc.close();
			} catch (Exception x) {
			}
		}

		String getErrorDesc(String errMsg) {
			MessageManager mm = EngineMessage.get();
//			����errMsg�ں�����ջ����������Ϊ��message��Դ�к���errMsg��Ϣ��Ҳ�����²���errMsg����ȥ������Ч
			return mm.getMessage("callx.error", dfxDelegate(dfx),
					errMsg + "\r\n", this);
		}

		void runOnNode() throws Throwable{
			try {
				Request req = new Request(Request.DFX_CALCULATE);
				req.setAttr(Request.CALCULATE_TaskId, taskId);
				// �ֻ��������ʱ��˵�������������ģ�����Ҫʱ�̱���ois�Լ�oos���Ա����߳��жϳ�����ʱ������ǿ��
				// �رն�д����
				Response res = uc.send(req);

				if (res.getException() != null) {
					Exception ex = res.getException();
					if (ex instanceof RetryException) {
							throw ex;
					}
//					ȡ������Ҳ�������Ǵ�DataStoreconsole�����ģ���ʱ��Ȼ��Ҫ�жϱ��ˡ�
					if (ex instanceof CanceledException) {
						// �Ѿ��Ǳ�����ȡ�������񣬲���Ҫ�ٵ���interruptAll
					} else {
						throw ex;
					}
				} else if (res.getError() != null) { // һ��Ϊ�ڴ��������
					Error err = res.getError();
					throw err;
				} else {
					Object result = res.getResult();
					setResponseValue(result);
					if( isOneOption ){
						throw new Exception(ONE_OPTION);
					}
				}
			} catch (Throwable t) {
				throw t;
			}
		}

		void setResponseValue(Object val) throws Exception{
			if( needReduce() ){
				synchronized ( reduceResults ) {
					Object accumulateResult = reduceResults.get(spaceId);
					if(accumulateResult==null){
						accumulateResult = val;
					}else{
						accumulateResult = reducePgmCellSet( accumulateResult, val);
					}
					reduceResults.put(spaceId, accumulateResult);
				}
			}else{
				int index = indexOf(this);
				setResult( index, val );
			}
		}
		
		/**
		 * ���������ܲ���������ÿ��ʵ��һ����¡
		 */
		private Object getDfxObject(){
			Object dfxObj = dfx;
			if (dfx instanceof PgmCellSet) {
				PgmCellSet pcs = (PgmCellSet)dfx;
				dfxObj = pcs.deepClone();
			}
			return dfxObj;
		}
		
		
		void runOnLocal() throws Throwable{
			runOnLocal(false);
		}
		
		void runOnLocal(boolean isProcessCaller) throws Throwable{
			try {
				Task task = new Task(getDfxObject(), argList, taskId, spaceId);
				task.setProcessCaller(isProcessCaller);
				if( isProcessCaller ) {
					task.setReduce(reduce, accumulateLocation, currentLocation);
				}

				TaskManager.addTask(task);
				
				Response res = task.execute();
				if (res.getException() != null) {
					Exception ex = res.getException();
					if (ex instanceof RetryException) {
							throw ex;
					}
					
					if (ex instanceof CanceledException) {
//						 �Ѿ��Ǳ�����ȡ�������񣬲���Ҫ�ٵ���interruptAll
					} else {
						throw ex;
					}
				} else if (res.getError() != null) { // һ��Ϊ�ڴ��������
					Error err = res.getError();
					throw err;
				} else {
					Object result = res.getResult();
					setResponseValue(result);
				}
			} catch (CanceledException ce) {
				Logger.info(mm.getMessage("ParallelProcess.cancelfor",this,ce.getMessage()));
			} catch (Throwable t) {
				throw t;
			}
		}

		boolean canRunOnLocal() {
			if (uc == null) {
				return true;
			}
			// ����ֻ���host��IP��IDE���õĻ��������еı��ص�ַ��ͬ����Ҳ�Ǳ����߳�ִ�� 2013.10.22
			return uc.isEqualToLocal();
		}

		public void run() {
			isRunning = true;
			try {
				PerfMonitor.enterProcess();
				
				boolean cancel = isCanceled;
				if( !cancel ){
					long l1 = System.currentTimeMillis();
					Logger.debug( mm.getMessage("Task.taskBegin",this));//this+" ��ʼ���㡣");
					if (canRunOnLocal()) {
						runOnLocal();
					}else{
						runOnNode();
					}
					long l2 = System.currentTimeMillis();
					DecimalFormat df = new DecimalFormat("###,###");
					long lastTime = l2 - l1;
					Logger.debug(mm.getMessage("Task.taskEnd",this,df.format(lastTime)));//this+" ������ɣ���ʱ��"+df.format(lastTime)+ " ���롣 ");
				}else{
					Logger.info(mm.getMessage("ParallelProcess.cancelfor",this,TERMINATE));
				}
			} catch (Throwable t) {
				interruptAll(this, t);
			}finally {
				PerfMonitor.leaveProcess();
				releaseClient(uc);
				isRunning = false;
			}
		}

		public String getArgDesc(){
			return args2String(argList);
		}
		
		public String toString() {
			MessageManager mm = EngineMessage.get();

			StringBuffer sb = new StringBuffer();
			if (uc != null) {
				sb.append(uc);
			} else {
				sb.append(dfxDelegate(getDfxObject()));
//				sb.append(mm.getMessage("callx.local"));
			}
			sb.append("  ");
			sb.append( getArgDesc()+" " );
//			MessageManager pm = ParallelMessage.get();
//			if(canRunOnLocal() || UnitServer.instance==null ){//�ͻ����ϻ���������ʱ
//				sb.append(pm.getMessage("Task.taskid",taskId));//" �����=[ "+ taskId+" ]"
//			}else{
//				sb.append(pm.getMessage("Task.taskid2",taskId));//" �������=[ "+ taskId+" ]"
//			}
			return sb.toString();
		}

	}


	public void close() {
		cancel(null);
	}

}
