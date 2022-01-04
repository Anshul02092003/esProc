package com.scudata.parallel;

import java.util.HashMap;

import com.scudata.dm.Context;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.ResourceManager;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.Channel;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.Push;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.FunctionLib;

/**
 * ��Ⱥ�ܵ�
 * @author RunQian
 *
 */
public class ClusterChannel extends Channel {
	private Cluster cluster;
	private int []channelProxyIds; // ��Ӧ�Ľڵ���ܵ������ʶ
	
	public ClusterChannel(ClusterChannel channel, Context ctx) {
		super(ctx);
		
		cluster = channel.getCluster();
		int count = cluster.getUnitCount();
		channelProxyIds = new int[count];
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_CH);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channel.channelProxyIds[i]));

				Response response = client.send(command);
				Integer id = (Integer) response.checkResult();
				channelProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}
	}
	
	public ClusterChannel(ClusterCursor cursor, Context ctx) {
		super(ctx);
		
		cluster = cursor.getCluster();
		int count = cluster.getUnitCount();
		int[] csIds = cursor.getCursorProxyIds();
		
		channelProxyIds = new int[count];
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_CS);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("cursorProxyId", new Integer(csIds[i]));

				Response response = client.send(command);
				Integer id = (Integer) response.checkResult();
				channelProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}
	}
	
	public static Response executeCreateChannel_CH(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer chId = (Integer) attributes.get("channelProxyId");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			ChannelProxy cp = (ChannelProxy) rm.getProxy(chId.intValue());
			Context ctx = ClusterUtil.createContext(js, attributes);

			Channel channel = new Channel(ctx);
			Push push = new Push(null, channel);
			cp.getChannel().addOperation(push, ctx);
			IProxy proxy = new ChannelProxy(channel);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	public static Response executeCreateChannel_CS(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer csId = (Integer) attributes.get("cursorProxyId");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			CursorProxy cp = (CursorProxy) rm.getProxy(csId.intValue());
			Context ctx = ClusterUtil.createContext(js, attributes);

			Channel channel = new Channel(ctx);
			Push push = new Push(null, channel);
			ICursor cursor = cp.getCursor();
			if (cursor != null) {
				cursor.addOperation(push, ctx);
			}
			
			IProxy proxy = new ChannelProxy(channel);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	public Cluster getCluster() {
		return cluster;
	}
	
	/**
	 * ����Ⱥ�ܵ���������
	 * @param op ����
	 * @param ctx ����������
	 */
	public Operable addOperation(Operation op, Context ctx) {
		super.addOperation(op, ctx);
		
		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		Function function = op.getFunction();
		String functionName = function.getFunctionName();
		String option = function.getOption();
		String param = function.getParamString();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_ADD_OPERATION);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));
				//command.setAttribute("unit", new Integer(i));
				command.setAttribute("functionName", functionName);
				command.setAttribute("option", option);
				command.setAttribute("param", param);
				
				ClusterUtil.setParams(command, function, ctx);
 				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
		
		return this;
	}

	/**
	 * �ڵ����ִ��Ϊ�ܵ���������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeAddOperation(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		//Integer unit = (Integer)attributes.get("unit");
		String functionName = (String)attributes.get("functionName");
		String option = (String)attributes.get("option");
		String param = (String)attributes.get("param");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			Context ctx = ClusterUtil.createContext(js, attributes, functionName, option);
			
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			FunctionLib.executeMemberFunction(channel.getChannel(), functionName, param, option, ctx);
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}

	/**
	 * �����ܵ���ǰ������Ϊ�����
	 * @return ��ǰ��Ⱥ�ܵ�
	 */
	public Channel fetch() {
		super.fetch();
		
		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_FETCH);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));				
 				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
		
		return this;
	}

	/**
	 * �ڵ����ִ�б����ܵ���ǰ������Ϊ�����
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeFetch(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			channel.getChannel().fetch();
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * Ϊ��Ⱥ�ܵ���ӷ�����������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @return ��ǰ��Ⱥ�ܵ�
	 */
	public Channel groups(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt) {
		super.groups(exps, names, calcExps, calcNames, opt);
		
		int dcount = exps == null ? 0 : exps.length;
		int mcount = calcExps == null ? 0 : calcExps.length;
		String []dexps = null;
		String []mexps = null;
		Expression[] totalExps = new Expression[dcount + mcount];
		
		if (dcount > 0) {
			dexps = new String[dcount];
			for (int i = 0; i < dcount; ++i) {
				dexps[i] = exps[i].toString();
				totalExps[i] = exps[i];
			}
		}
		
		if (mcount > 0) {
			mexps = new String[mcount];
			for (int i = 0; i < mcount; ++i) {
				mexps[i] = calcExps[i].toString();
				totalExps[i + dcount] = calcExps[i];
			}
		}

		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_GROUPS);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));
				
				command.setAttribute("dexps", dexps);
				command.setAttribute("names", names);
				command.setAttribute("mexps", mexps);
				command.setAttribute("calcNames", calcNames);
				command.setAttribute("option", opt);
				
				ClusterUtil.setParams(command, totalExps, ctx);
 				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
		
		return this;
	}
	
	/**
	 * �ڵ����ִ��Ϊ�ܵ���ӷ�������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGroups(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		String []dexps = (String[])attributes.get("dexps");
		String []names = (String[])attributes.get("names");
		String []mexps = (String[])attributes.get("mexps");
		String []calcNames = (String[])attributes.get("calcNames");
		String opt = (String)attributes.get("option");
		Expression[] exps = null;
		Expression[] calcExps = null;
		
		JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
		Context ctx = ClusterUtil.createContext(js, attributes);
		
		if (dexps != null) {
			int dcount = dexps.length;
			exps = new Expression[dcount];
			for (int i = 0; i < dcount; ++i) {
				exps[i] = new Expression(ctx, dexps[i]);
			}
		}
		
		if (mexps != null) {
			int mcount = mexps.length;
			calcExps = new Expression[mcount];
			for (int i = 0; i < mcount; ++i) {
				calcExps[i] = new Expression(ctx, mexps[i]);
			}
		}
		
		try {
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			channel.getChannel().groups(exps, names, calcExps, calcNames, opt);
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}

	/**
	 * Ϊ��Ⱥ�ܵ������������������
	 * @param exps ������ʽ����
	 * @param names	�����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames	�����ֶ�������
	 * @param opt ѡ��
	 * @param capacity �ڴ��ܹ���ŵķ�����������
	 * @return ��ǰ��Ⱥ�ܵ�
	 */
	public Channel groupx(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt, int capacity) {
		super.groupx(exps, names, calcExps, calcNames, opt, capacity);
		
		int dcount = exps == null ? 0 : exps.length;
		int mcount = calcExps == null ? 0 : calcExps.length;
		String []dexps = null;
		String []mexps = null;
		Expression[] totalExps = new Expression[dcount + mcount];
		
		if (dcount > 0) {
			dexps = new String[dcount];
			for (int i = 0; i < dcount; ++i) {
				dexps[i] = exps[i].toString();
				totalExps[i] = exps[i];
			}
		}
		
		if (mcount > 0) {
			mexps = new String[mcount];
			for (int i = 0; i < mcount; ++i) {
				mexps[i] = calcExps[i].toString();
				totalExps[i + dcount] = calcExps[i];
			}
		}

		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_GROUPX);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));
				
				command.setAttribute("dexps", dexps);
				command.setAttribute("names", names);
				command.setAttribute("mexps", mexps);
				command.setAttribute("calcNames", calcNames);
				command.setAttribute("option", opt);
				command.setAttribute("capacity", new Integer(capacity));
				
				ClusterUtil.setParams(command, totalExps, ctx);
 				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
		
		return this;
	}

	/**
	 * �ڵ����ִ��Ϊ�ܵ��������������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGroupx(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		String []dexps = (String[])attributes.get("dexps");
		String []names = (String[])attributes.get("names");
		String []mexps = (String[])attributes.get("mexps");
		String []calcNames = (String[])attributes.get("calcNames");
		String opt = (String)attributes.get("option");
		Integer capacity = (Integer)attributes.get("capacity");
		Expression[] exps = null;
		Expression[] calcExps = null;
		
		JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
		Context ctx = ClusterUtil.createContext(js, attributes);
		
		if (dexps != null) {
			int dcount = dexps.length;
			exps = new Expression[dcount];
			for (int i = 0; i < dcount; ++i) {
				exps[i] = new Expression(ctx, dexps[i]);
			}
		}
		
		if (mexps != null) {
			int mcount = mexps.length;
			calcExps = new Expression[mcount];
			for (int i = 0; i < mcount; ++i) {
				calcExps[i] = new Expression(ctx, mexps[i]);
			}
		}
		
		try {
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			channel.getChannel().groupx(exps, names, calcExps, calcNames, opt, capacity);
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * Ϊ��Ⱥ�ܵ�������������������
	 * @param exps ������ʽ����
	 * @param capacity �ڴ����ܹ�����ļ�¼�������û���������Զ�����һ��
	 * @param opt ѡ��
	 * @return ��ǰ��Ⱥ�ܵ�
	 */
	public Channel sortx(Expression[] exps, int capacity, String opt) {
		super.sortx(exps, capacity, opt);
		
		int fcount = exps.length;
		String []fields = new String[fcount];
		for (int i = 0; i < fcount; ++i) {
			fields[i] = exps[i].toString();
		}

		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_SORTX);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));
				command.setAttribute("fields", fields);
				command.setAttribute("capacity", new Integer(capacity));
				command.setAttribute("opt", opt);
				
				ClusterUtil.setParams(command, exps, ctx);
 				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
		
		return this;
	}

	/**
	 * �ڵ����ִ��Ϊ�ܵ���������������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeSortx(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		String []fields = (String[])attributes.get("fields");
		Integer capacity = (Integer)attributes.get("capacity");
		String opt = (String)attributes.get("opt");
		
		JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
		Context ctx = ClusterUtil.createContext(js, attributes);
		
		int fcount = fields.length;
		Expression []exps = new Expression[fcount];
		for (int i = 0; i < fcount; ++i) {
			exps[i] = new Expression(ctx, fields[i]);
		}

		try {
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			channel.getChannel().sortx(exps, capacity, opt);
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ���ؼ�Ⱥ�ܵ��ļ�����
	 * @return Object
	 */
	public Object result() {
		if (result == null) {
			return null;
		}
		
		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		Object []results = new Object[count];
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CHANNEL_RESULT);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("channelProxyId", new Integer(channelProxyIds[i]));				
 				Response response = client.send(command);
 				results[i] = response.checkResult();
 				
 				if (results[i] instanceof Integer) {
 					// �α����id
 					int id = ((Integer)results[i]).intValue();
 					results[i] = new RemoteCursor(cluster.getHost(i), cluster.getPort(i), id);
 				}
			} finally {
				client.close();
			}
		}
		
		return result.combineResult(results);
	}

	/**
	 * �ڵ����ִ��ȡ�ܵ��ļ�����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeResult(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer channelProxyId = (Integer)attributes.get("channelProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			ChannelProxy channel = (ChannelProxy)rm.getProxy(channelProxyId.intValue());
			Object result = channel.getChannel().result();
			if (result instanceof ICursor) {
				RemoteCursorProxy rcp = new RemoteCursorProxy((ICursor)result);
				return new Response(new Integer(rcp.getProxyID()));
			} else {
				return new Response(result);
			}
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
}