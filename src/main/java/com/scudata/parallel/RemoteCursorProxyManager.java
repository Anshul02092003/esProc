package com.scudata.parallel;

import java.util.*;

import com.scudata.common.*;
import com.scudata.dm.cursor.ICursor;

import java.lang.reflect.*;

/**
 * Զ���α���������
 * @author Joancy
 *
 */
public class RemoteCursorProxyManager {
	private static RemoteCursorProxyManager instance = null;
	
	ITask task;
	ArrayList<RemoteCursorProxy> proxys = new ArrayList<RemoteCursorProxy>();

	/**
	 * Ϊ��֧�ּ�Ⱥ�α꣬ʹ�þ�̬���������ù������������κ�Task��taskΪnull
	 * @return �α���������
	 */
	public static RemoteCursorProxyManager getInstance(){
		if(instance==null){
			instance = new RemoteCursorProxyManager(null);
		}
		return instance;
	}
	
	/**
	 * ����һ���α꣬�����α�����
	 * @param c �α�
	 * @return �����
	 */
	public static int addCursor(ICursor c){
		RemoteCursorProxyManager rcpm = RemoteCursorProxyManager.getInstance();
		RemoteCursorProxy rcp = new RemoteCursorProxy(c);
		rcpm.addProxy(rcp);
		return rcp.getProxyID();
	}

	/**
	 * �����α���������
	 * @param t ����������
	 */
	public RemoteCursorProxyManager(ITask t) {
		this.task = t;
	}

	/**
	 * ��ȡ�����б�
	 * @return �����б�
	 */
	public ArrayList<RemoteCursorProxy> getProxys(){
		return proxys;
	}
	
	/**
	 * ���ٵ�ǰ������
	 */
	public void destroy() {
		for (int i = 0; i < proxys.size(); i++) {
			RemoteCursorProxy rcp = proxys.get(i);
			rcp.destroy();
		}
		proxys.clear();
	}

	/**
	 * ��ȡ�������
	 * @return ����
	 */
	public ITask getTask() {
		return task;
	}

	/**
	 * ִ������
	 * @param req ����
	 * @return ��Ӧ
	 */
	public Response execute(Request req) {
		if(task!=null){
			task.resetAccess();
		}
		Response res = new Response();
		try {
			int proxyId = ((Number) req.getAttr(Request.METHOD_ProxyId))
					.intValue();
			RemoteCursorProxy rcp = getProxy(proxyId);
			String methodName = (String) req.getAttr(Request.METHOD_MethodName);
			if(methodName.equals("close")&&rcp==null){
//���ڴ�����α�fetch����Զ�close���ˣ���ʱ�ͻ����ٷ�����close�����账���Ѿ������ڸô���				
				return res;
			}
			Object[] args = (Object[]) req.getAttr(Request.METHOD_ArgValues);
			try {
				Object result = invokeMethod(rcp, methodName, args);
				res.setResult(result);
			} catch (Exception x) {
				x.printStackTrace();
				res.setException(new RQException(x.getCause().getMessage(), x
						.getCause()));
			} catch (Error er) {
				er.printStackTrace();
				res.setError(er);
			}
		} finally {
			if(task!=null){
				task.access();
			}
		}
		return res;
	}

	/**
	 * �����α����
	 * @param rcp �α����
	 */
	public synchronized void addProxy(RemoteCursorProxy rcp) {
		proxys.add(rcp);
	}

	/**
	 * ɾ���������
	 * ���д���ɾ�պ������еĳ������ͷ�
	 * @param proxyID Ҫɾ���Ĵ�����
	 */
	public synchronized void delProxy(int proxyID) {
		for (int i = 0; i < proxys.size(); i++) {
			RemoteCursorProxy rcp = proxys.get(i);
			if (rcp.getProxyID() == proxyID) {
				proxys.remove(i);
				break;
			}
		}
		if (proxys.isEmpty()) {
			if(task!=null){
				TaskManager.delTask(task.getTaskID());
			}
		}
	}

	/**
	 * ���ݴ�����proxyIDȥ�������
	 * @param proxyID ������
	 * @return �α�������
	 */
	public synchronized RemoteCursorProxy getProxy(int proxyID) {
		for (int i = 0; i < proxys.size(); i++) {
			RemoteCursorProxy rcp = proxys.get(i);
			if (rcp.getProxyID() == proxyID) {
				return rcp;
			}
		}
		return null;
	}

	/**
	 * ʹ�÷���ִ����ķ���
	 * @param owner ����
	 * @param methodName ������
	 * @param args ����
	 * @return ִ�н��
	 * @throws Exception ����ʱ�׳��쳣
	 */
	public static Object invokeMethod(Object owner, String methodName,
			Object[] args) throws Exception {
		Class ownerClass = owner.getClass();

		Method[] ms = ownerClass.getMethods();
		for (int i = 0; i < ms.length; i++) {
			Method m = ms[i];
			if (m.getName().equals(methodName) && isArgsMatchMethod(m, args)) {
				return m.invoke(owner, args);
			}
		}
		StringBuffer argNames = new StringBuffer();
		argNames.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				argNames.append(",");
			}
			if (args[i] == null) {
				argNames.append("null");
			} else {
				argNames.append(args[i].getClass().getName());
			}
		}
		argNames.append(")");
		throw new Exception(methodName + argNames + " not found.");
	}

	private static boolean isArgsMatchMethod(Method m, Object[] args) {
		if (args == null) {
			args = new Object[] {};
		}
		Class[] mArgs = m.getParameterTypes();
		if (mArgs.length != args.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (!localMatch(mArgs[i], args[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean localMatch(Class c, Object o) {
		// ����Ϊnullʱ������ƥ��
		if (o == null)
			return true;

		String n1 = c.getName();
		if (n1.equals("boolean")) {
			return o instanceof Boolean;
		}
		if (n1.equals("byte")) {
			return o instanceof Byte;
		}
		if (n1.equals("short")) {
			return o instanceof Short;
		}
		if (n1.equals("int")) {
			return o instanceof Integer;
		}
		if (n1.equals("long")) {
			return o instanceof Long;
		}
		if (n1.equals("float")) {
			return o instanceof Float;
		}
		if (n1.equals("double")) {
			return o instanceof Double;
		}

		return c.isInstance(o);
	}

	/**
	 * ������ʱ
	 * @param proxyTimeOut ��ʱʱ��
	 */
	public static synchronized void checkTimeOut(int proxyTimeOut) {
		RemoteCursorProxyManager instance = RemoteCursorProxyManager.getInstance();
		// ������룬timeOut��λΪ��
		ArrayList<RemoteCursorProxy> proxys = instance.getProxys();
		for (int i = proxys.size() - 1; i >= 0; i--) {
			RemoteCursorProxy rcp = proxys.get(i);
			if (rcp.checkTimeOut(proxyTimeOut)) {
				proxys.remove(rcp);
			}
		}
	}
	
}
