package com.scudata.parallel;

import java.util.HashMap;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.IResource;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.Record;
import com.scudata.dm.ResourceManager;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dw.Cuboid;
import com.scudata.dw.ITableIndex;
import com.scudata.dw.ITableMetaData;
import com.scudata.dw.JoinCursor;
import com.scudata.dw.MemoryTable;
import com.scudata.dw.TableFulltextIndex;
import com.scudata.dw.TableHashIndex;
import com.scudata.dw.TableKeyValueIndex;
import com.scudata.dw.TableMetaData;
import com.scudata.dw.TableMetaDataIndex;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.ThreadPool;
import com.scudata.util.Variant;

/**
 * ��Ⱥ���
 * @author RunQian
 *
 */
public class ClusterTableMetaData implements IClusterObject, IResource {
	public static final int TYPE_TABLE = 0;
	public static final int TYPE_NEW = 1;
	public static final int TYPE_NEWS = 2;
	public static final int TYPE_DERIVE = 3;
	private ClusterFile clusterFile;
	private int[] tmdProxyIds; // ��Ӧ�Ľڵ����������ʶ
	
	private String []allColNames; // �����ֶ�
	private String []sortedColNames; // �����������ֶ�
	private Expression distribute; // �ֲ����ʽ
	private Context ctx;

	public ClusterTableMetaData(ClusterFile clusterFile, int[] tmdProxyIds, Context ctx) {
		this.clusterFile = clusterFile;
		this.tmdProxyIds = tmdProxyIds;
		this.ctx = ctx;
	}

	public Cluster getCluster() {
		return clusterFile.getCluster();
	}

	public ClusterFile getClusterFile() {
		return clusterFile;
	}

	/**
	 * ����ͬ���ֶμ�Ⱥ�α�
	 * @param mcs ��Ⱥ�α꣬���մ��α�ķֶζԵ�ǰ�α���ͬ���ֶ�
	 * @param exps �ֶα��ʽ����
	 * @param fields �ֶ�������
	 * @param filter ��������
	 * @param fkNames ���������
	 * @param codeExps ά����ʽ����
	 * @param opts �����ֶν��й�����ѡ��
	 * @param opt ѡ�k����mcs���׼���Ӧ
	 * @param ctx ����������
	 * @return ��Ⱥ�α�
	 */
	public ClusterCursor cursor(ClusterCursor mcs, Expression []exps, String []fields, Expression filter, 
			String []fkNames, Expression []codeExps, String []opts, String opt, Context ctx) {
		Cluster cluster = getCluster();
		int count = cluster.getUnitCount();
		int[] mcsIds = mcs.getCursorProxyIds();
		if (mcsIds.length != count || !(mcs.getSource() instanceof ClusterTableMetaData)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
		}
		
		String []expStrs = null;
		if (exps != null) {
			int len = exps.length;
			expStrs = new String[len];
			for (int i = 0; i < len; ++i) {
				if (exps[i] != null) {
					expStrs[i] = exps[i].toString();
				}
			}
		}

		int[] cursorProxyIds = new int[count];
		String []codeExpStrs = null;
		if (codeExps != null) {
			int fkCount = codeExps.length;
			codeExpStrs = new String[fkCount];
			for (int i = 0; i < fkCount; ++i) {
				codeExpStrs[i] = codeExps[i].toString();
			}
		}
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.CREATE_SYNC_GT_CURSOR);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("cursorProxyId", new Integer(mcsIds[i]));

				command.setAttribute("expStrs",  expStrs);
				command.setAttribute("fields", fields);
				command.setAttribute("filter", filter == null ? null : filter.toString());
				command.setAttribute("fkNames",  fkNames);
				command.setAttribute("codeExps",  codeExpStrs);
				command.setAttribute("opts",  opts);
				command.setAttribute("unit", new Integer(i));
				command.setAttribute("opt",  opt);
				
				if (filter != null) {
					if (codeExps != null) {
						int fkCount = codeExps.length;
						Expression []totalExps = new Expression[fkCount + 1];
						System.arraycopy(codeExps, 0, totalExps, 0, fkCount);
						totalExps[fkCount] = filter;
						ClusterUtil.setParams(command, totalExps, ctx);
					} else {
						ClusterUtil.setParams(command, filter, ctx);
					}
				} else if (codeExps != null) {
					ClusterUtil.setParams(command, codeExps, ctx);
				}

				Response response = client.send(command);
				Integer id = (Integer) response.checkResult();
				cursorProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}

		ClusterCursor result = new ClusterCursor(this, cursorProxyIds, mcs.isDistributed());
		result.setDistribute(distribute);
		result.setSortedColNames(getAllSortedColNames());
		return result;
	}

	/**
	 * �ڵ����ִ�д���ͬ����·�α�
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeCreateSyncCursor(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		Integer mcsId = (Integer) attributes.get("cursorProxyId");
		String []expStrs = (String[]) attributes.get("expStrs");
		String []fields = (String[]) attributes.get("fields");
		String filter = (String) attributes.get("filter");
		String []fkNames = (String[]) attributes.get("fkNames");
		String []codeExps = (String[]) attributes.get("codeExps");
		String []opts = (String[]) attributes.get("opts");
		Integer unit = (Integer) attributes.get("unit");
		String opt = (String) attributes.get("opt");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmdp = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData tableMetaData = tmdp.getTableMetaData();
			CursorProxy cp = (CursorProxy) rm.getProxy(mcsId.intValue());
			ICursor mcs = cp.getCursor();
			
			Context ctx = ClusterUtil.createContext(js, attributes, "cursor", null);
			Expression exp = filter == null ? null : new Expression(ctx, filter);
			
			Expression []exps = null;
			if (expStrs != null) {
				int len = expStrs.length;
				exps = new Expression[len];
				for (int i = 0; i < len; ++i) {
					exps[i] = new Expression(ctx, expStrs[i]);
				}
			}

			Sequence []codes = null;
			if (codeExps != null) {
				int fkcount = codeExps.length;
				codes = new Sequence[fkcount];
				for (int i = 0; i < fkcount; ++i) {
					Expression codeExp = new Expression(ctx, codeExps[i]);
					Object val = codeExp.calculate(ctx);
					if (val instanceof Sequence) {
						codes[i] = (Sequence)val;
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
					}
				}
			}

			ICursor cursor;
			if (mcs instanceof MultipathCursors) {
				cursor = tableMetaData.cursor(exps, fields, exp, fkNames, codes, opts, (MultipathCursors)mcs, opt, ctx);
			} else {
				cursor = tableMetaData.cursor(exps, fields, exp, fkNames, codes, opts, ctx);
			}
			
			IProxy proxy = new CursorProxy(cursor, unit);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
		
	/**
	 * ������Ⱥ�α�
	 * @param exps �ֶα��ʽ����
	 * @param fields �ֶ�������
	 * @param filter ��������
	 * @param fkNames ���������
	 * @param codeExps ά����ʽ����
	 * @param opts �����ֶν��й�����ѡ��
	 * @param opt ѡ�k����mcs���׼���Ӧ
	 * @param ctx ����������
	 * @return ��Ⱥ�α�
	 */
	public ClusterCursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Expression []codeExps, String []opts, int segCount, String opt, Context ctx) {
		ClusterFile clusterFile = getClusterFile();
		int count = clusterFile.getUnitCount();
		int[] cursorProxyIds = new int[count];
		
		// �ֲ��ļ�������zѡ��ʱ���ڵ�������ļ������з�
		boolean isDistributedFile = clusterFile.isDistributedFile();
		boolean isSeg = !isDistributedFile; // Ŀǰֻ�зֲ��ļ��ˣ�����Ҫ����֣���ʱ���������ʶ
		
		String []expStrs = null;
		if (exps != null) {
			int len = exps.length;
			expStrs = new String[len];
			for (int i = 0; i < len; ++i) {
				if (exps[i] != null) {
					expStrs[i] = exps[i].toString();
				}
			}
		}
		
		String []codeExpStrs = null;
		if (codeExps != null) {
			int fkCount = codeExps.length;
			codeExpStrs = new String[fkCount];
			for (int i = 0; i < fkCount; ++i) {
				codeExpStrs[i] = codeExps[i].toString();
			}
		}
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(clusterFile.getHost(i), clusterFile.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.CREATE_GT_CURSOR);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				
				command.setAttribute("expStrs",  expStrs);
				command.setAttribute("fields", fields);
				command.setAttribute("filter", filter == null ? null : filter.toString());
				command.setAttribute("fkNames",  fkNames);
				command.setAttribute("codeExps",  codeExpStrs);
				command.setAttribute("opts",  opts);
				command.setAttribute("option", opt);
				
				command.setAttribute("unit", new Integer(i));
				command.setAttribute("unitCount", new Integer(count));
				command.setAttribute("isSeg", isSeg);
				command.setAttribute("segCount", new Integer(segCount));

				if (filter != null) {
					if (codeExps != null) {
						int fkCount = codeExps.length;
						Expression []totalExps = new Expression[fkCount + 1];
						System.arraycopy(codeExps, 0, totalExps, 0, fkCount);
						totalExps[fkCount] = filter;
						ClusterUtil.setParams(command, totalExps, ctx);
					} else {
						ClusterUtil.setParams(command, filter, ctx);
					}
				} else if (codeExps != null) {
					ClusterUtil.setParams(command, codeExps, ctx);
				}
				
				Response response = client.send(command);
				Integer id = (Integer) response.checkResult();
				cursorProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}

		ClusterCursor result = new ClusterCursor(this, cursorProxyIds, isDistributedFile || isSeg);
		result.setDistribute(distribute);
		result.setSortedColNames(getAllSortedColNames());
		return result;
	}

	/**
	 * �ڵ����ִ�д����α�
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeCreateCursor(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String []expStrs = (String[]) attributes.get("expStrs");
		String []fields = (String[]) attributes.get("fields");
		String filter = (String) attributes.get("filter");
		String []fkNames = (String[]) attributes.get("fkNames");
		String []codeExps = (String[]) attributes.get("codeExps");
		String []opts = (String[]) attributes.get("opts");
		String opt = (String) attributes.get("option");

		Integer unit = (Integer) attributes.get("unit");
		int unitCount = (Integer)attributes.get("unitCount");
		boolean isSeg = (Boolean)attributes.get("isSeg");
		int segCount = (Integer)attributes.get("segCount");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData tableMetaData = tmd.getTableMetaData();
			Context ctx = ClusterUtil.createContext(js, attributes, "cursor", null);
			Expression exp = filter == null ? null : new Expression(ctx, filter);
			
			Expression []exps = null;
			if (expStrs != null) {
				int len = expStrs.length;
				exps = new Expression[len];
				for (int i = 0; i < len; ++i) {
					exps[i] = new Expression(ctx, expStrs[i]);
				}
			}
			
			Sequence []codes = null;
			if (codeExps != null) {
				int fkcount = codeExps.length;
				codes = new Sequence[fkcount];
				for (int i = 0; i < fkcount; ++i) {
					Expression codeExp = new Expression(ctx, codeExps[i]);
					Object val = codeExp.calculate(ctx);
					if (val instanceof Sequence) {
						codes[i] = (Sequence)val;
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
					}
				}
			}
			
			if (opt != null && opt.indexOf('m') != -1) {
				if (segCount < 2) {
					segCount = Env.getCursorParallelNum();
				}
			} else {
				segCount = 1;
			}
			
			ICursor cursor;
			if (isSeg) {
				// �ڵ������Ҫ�����з�����
				cursor = tableMetaData.cursor(exps, fields, exp, fkNames, codes, opts, unit + 1, unitCount, segCount, ctx);
			} else {
				if (segCount > 1) {
					cursor = tableMetaData.cursor(exps, fields, exp, fkNames, codes, opts, segCount, ctx);
				} else {
					cursor = tableMetaData.cursor(exps, fields, exp, fkNames, codes, opts, ctx);
				}
			}
			
			IProxy proxy = new CursorProxy(cursor, unit);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ��������ֶγɼ�Ⱥ�ڱ�
	 * @param fields Ҫ�������ֶ�������
	 * @param filter ��������
	 * @param ctx ����������
	 * @return ��Ⱥ�ڱ�
	 */
	public ClusterMemoryTable memory(String []fields, Expression filter, Context ctx) {
		ClusterFile clusterFile = getClusterFile();
		int count = clusterFile.getUnitCount();
		RemoteMemoryTable[] tables = new RemoteMemoryTable[count];
		
		UnitJob []jobs = new UnitJob[count];
		ThreadPool pool = TaskManager.getPool();
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(clusterFile.getHost(i), clusterFile.getPort(i));
			UnitCommand command = new UnitCommand(UnitCommand.MEMORY_GT);
			command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
			command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
			
			command.setAttribute("fields", fields);
			command.setAttribute("filter", filter == null ? null : filter.toString());
			command.setAttribute("unit", new Integer(i));
			
			ClusterUtil.setParams(command, filter, ctx);
			jobs[i] = new UnitJob(client, command);
			pool.submit(jobs[i]);
		}

		for (int i = 0; i < count; ++i) {
			// �ȴ�����ִ�����
			jobs[i].join();
			tables[i] = (RemoteMemoryTable)jobs[i].getResult();
		}

		ClusterMemoryTable result = new ClusterMemoryTable(getCluster(), tables, clusterFile.isDistributedFile());
		result.setDistribute(distribute);
		result.setSortedColNames(getAllSortedColNames());
		return result;
	}
	
	/**
	 * �ڵ����ִ�ж�ȡ���ݳ��ڱ�
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeMemory(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String []fields = (String[]) attributes.get("fields");
		String filter = (String) attributes.get("filter");
		Integer unit = (Integer) attributes.get("unit");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();

			TableMetaDataProxy tmdProxy = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData tmd = tmdProxy.getTableMetaData();
			Context ctx = ClusterUtil.createContext(js, attributes);
			Expression exp = filter == null ? null : new Expression(ctx, filter);

			ICursor cursor = tmd.cursor(fields, exp, ctx);
			Sequence seq = cursor.fetch();
			Table table;
			if (seq instanceof Table) {
				table = (Table)seq;
			} else {
				table = seq.derive("o");
			}

			MemoryTable memoryTable = new MemoryTable(table);
			if (tmd instanceof TableMetaData) {
				String distribute = tmd.getDistribute();
				Integer partition = ((TableMetaData)tmd).getGroupTable().getPartition();
				if (partition != null) {
					memoryTable.setDistribute(distribute);
					memoryTable.setPart(partition);
				}
			}

			IProxy proxy = new TableProxy(memoryTable, unit);
			rm.addProxy(proxy);
			
			RemoteMemoryTable rmt = ClusterMemoryTable.newRemoteMemoryTable(proxy.getProxyId(), memoryTable);
			return new Response(rmt);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ʹ��������ѯ
	 * @param fields ȡ���ֶ�
	 * @param filter ���˱��ʽ
	 * @param iname �����ֶ�
	 * @param opt ����'u'ʱ,������filter��������Ĺ������ȼ�
	 * @param ctx ������
	 * @return ��Ⱥ�α�
	 */
	public ClusterCursor icursor(String []fields, Expression filter, String iname, String opt, Context ctx) {
		ClusterFile clusterFile = getClusterFile();
		int count = clusterFile.getUnitCount();
		int[] cursorProxyIds = new int[count];
		boolean isDistributedFile = clusterFile.isDistributedFile();
		if (!isDistributedFile) {
			count = 1;
		}
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(clusterFile.getHost(i), clusterFile.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.CREATE_GT_ICURSOR);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				
				command.setAttribute("fields", fields);
				command.setAttribute("filter", filter == null ? null : filter.toString());
				command.setAttribute("iname", iname);
				command.setAttribute("opt", opt);
				
				ClusterUtil.setParams(command, filter, ctx);
				Response response = client.send(command);
				Integer id = (Integer) response.checkResult();
				cursorProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}

		ClusterCursor result = new ClusterCursor(this, cursorProxyIds, isDistributedFile);
		result.setDistribute(distribute);
		result.setSortedColNames(getAllSortedColNames());
		return result;
	}
	
	/**
	 * �ڵ����ִ�����������������ݹ��ˣ������α�
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeICursor(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String []fields = (String[]) attributes.get("fields");
		String filter = (String) attributes.get("filter");
		String iname = (String) attributes.get("iname");
		String opt = (String) attributes.get("opt");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			Context ctx = ClusterUtil.createContext(js, attributes);
			Expression exp = filter == null ? null : new Expression(ctx, filter);

			ICursor cursor = tmd.icursor(fields, exp, iname, opt, ctx);
			IProxy proxy = new CursorProxy(cursor, 0);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ȡ����
	 * @param tableName ������
	 * @return ��Ⱥ���
	 */
	public ClusterTableMetaData getTableMetaData(String tableName) {
		ClusterFile clusterFile = this.clusterFile;
		int count = tmdProxyIds.length;
		int []tableProxyIds = new int[count];
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(clusterFile.getHost(i), clusterFile.getPort(i));
			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.GET_TABLEMETADATA);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("tableName", tableName);
				Response response = client.send(command);
				Integer id = (Integer)response.checkResult();
				tableProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}
		
		ClusterTableMetaData table = new ClusterTableMetaData(clusterFile, tableProxyIds, ctx);
		table.setDistribute(distribute);
		return table;
	}

	/**
	 * �ڵ����ȡ����
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGetTableMetaData(HashMap<String, Object> attributes) {
		String jobSpaceID = (String)attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer)attributes.get("tmdProxyId");
		String tableName = (String)attributes.get("tableName");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy table = (TableMetaDataProxy)rm.getProxy(tmdProxyId.intValue());
			
			IProxy proxy = new TableMetaDataProxy(table.attach(tableName));
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	private void checkDistributedFile() {
		if (!clusterFile.isDistributedFile()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.needDistributed"));
		}
	}
	
	private Sequence []group(Sequence seq, Expression distribute, Context ctx, int unitCount) {
		int size = seq.length();
		Sequence []groups = new Sequence[unitCount];
		for (int i = 0; i < unitCount; ++i) {
			groups[i] = new Sequence(size / unitCount + 10);
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			for (int i = 1; i <= size; ++i) {
				current.setCurrent(i);
				Object val = distribute.calculate(ctx);
				if (!(val instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("append" + mm.getMessage("function.paramTypeError"));
				}
				
				int n = ((Number)val).intValue();
				if (n < 1 || n > unitCount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(n + mm.getMessage("engine.indexOutofBound"));
				}
				
				Object mem = seq.getMem(i);
				groups[n - 1].add(mem);
			}
		} finally {
			stack.pop();
		}
		
		return groups;
	}
	
	/*private ICursor []group(ICursor cs, Expression distribute, Context ctx, int unitCount) {
		Sequence data = cs.peek(ICursor.FETCHCOUNT);
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		FileObject []files = new FileObject[unitCount];
		BFileWriter []writers = new BFileWriter[unitCount];
		try {
			for (int i = 0; i < unitCount; ++i) {
				files[i] = FileObject.createTempFileObject();
				writers[i] = new BFileWriter(files[i], null);
				writers[i].prepareWrite(ds, false);
			}
			
			while (true) {
				data = cs.fetch(ICursor.FETCHCOUNT);
				if (data == null || data.length() == 0) {
					break;
				}
				
				Sequence []groups = group(data, distribute, ctx, unitCount);
				for (int i = 0; i < unitCount; ++i) {
					writers[i].write(groups[i]);
				}
			}
		} catch (Exception e) {
			for (int i = 0; i < unitCount; ++i) {
				if (files[i] == null) {
					break;
				}
				
				if (writers[i] != null) {
					writers[i].close();
				}
				
				files[i].delete();
			}
			
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		BFileCursor []cursors = new BFileCursor[unitCount];
		for (int i = 0; i < unitCount; ++i) {
			writers[i].close();
			cursors[i] = new BFileCursor(files[i], null, "x", ctx);
		}
		
		return cursors;
	}*/
	
	/**
	 * ���ֲ����ʽ׷�����ݵ��ڵ��
	 * @param cursor �����α�
	 * @param distribute �ֲ����ʽ������[1,������]֮�������
	 * @param ctx
	 */
	private void append(ICursor cursor, Expression distribute, Context ctx) {
		this.distribute = distribute;
		this.ctx = ctx;
		
		checkDistributedFile();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		UnitClient []clients = new UnitClient[unitCount];
		int fetchCount = ICursor.FETCHCOUNT * unitCount;
		
		try {
			int []seqs = new int[unitCount];
			for (int i = 0; i < unitCount; ++i) {
				clients[i] = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				seqs[i] = 0;
			}
			
			while (true) {
				Sequence data = cursor.fetch(fetchCount);
				if (data == null || data.length() == 0) {
					break;
				}
				
				Sequence []groups = group(data, distribute, ctx, unitCount);
				for (int i = 0; i < unitCount; ++i) {
					if (groups[i].length() == 0) {
						continue;
					}
					
					UnitCommand command = new UnitCommand(UnitCommand.GT_APPEND_BY_DATA);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					command.setAttribute("data", groups[i]);
					command.setAttribute("seq", seqs[i]);

					Response response = clients[i].send(command);
					response.checkResult();
					seqs[i]++;
				}
			}
			
			for (int i = 0; i < unitCount; ++i) {
				UnitCommand command = new UnitCommand(UnitCommand.GT_APPEND_BY_DATA);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("data", null);
				command.setAttribute("seq", seqs[i]);

				Response response = clients[i].send(command);
				response.checkResult();
			}
		} finally {
			for (int i = 0; i < unitCount; ++i) {
				if (clients[i] != null) {
					clients[i].close();
				}
			}
		}
	}
	
	public void append(ICursor cursor) {
		if (distribute != null) {
			append(cursor, distribute, ctx);
			Cuboid.update(this, ctx);
			return;
		}
		
		checkDistributedFile();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		
		if (cursor instanceof ClusterCursor) {
			// tmdProxyIds��ClusterCursor.cursorProxyIdsһһ��Ӧ
			// ��cursorProxyId������Ӧ�Ľڵ�����ڵ��ȡ��cursor��append
			ClusterCursor cs = (ClusterCursor)cursor;
			if (!cluster.isEquals(cs.getCluster())) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("dw.mcsNotMatch"));
			}
			
			int []csProxyIds = cs.getCursorProxyIds();
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				try {
					UnitCommand command = new UnitCommand(UnitCommand.GT_APPEND_BY_CSID);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					command.setAttribute("csProxyId", new Integer(csProxyIds[i]));
					
					Response response = client.send(command);
					response.checkResult();
				} finally {
					client.close();
				}
			}
		} else {
			// Ҫ�󸽱��Ѿ�append�����ݣ�������ά�ֶ�������
			// ������ڵ��ȡ��������¼��άֵ������cursor�����ݣ�����άֵ���ֳ������Ľڵ����Ȼ������ݴ��͵��ڵ������append
			Object [][]firstKeyValues = new Object[unitCount][];
			String []keys = null;

			//get first values
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				try {
					UnitCommand command = new UnitCommand(UnitCommand.GT_FIRST_KEY_VALUE);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					
					Response response = client.send(command);
					Record record = (Record) response.checkResult();
					firstKeyValues[i] = record.getFieldValues();
					if (keys == null) {
						keys = record.dataStruct().getFieldNames();
					}
				} finally {
					client.close();
				}
			}
			
			//send data
			Sequence data = cursor.peek(1);
			if (data == null) return;
			
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				int seq = 0;
				
				try {
					while (true) {
						if (i == unitCount - 1) {
							data = cursor.fetch(ICursor.FETCHCOUNT);
						} else {
							data = fetchToValue(cursor, keys, firstKeyValues[i + 1]);
						}

						UnitCommand command = new UnitCommand(UnitCommand.GT_APPEND_BY_DATA);
						command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
						command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
						command.setAttribute("data", data);
						command.setAttribute("seq", seq);
	
						Response response = client.send(command);
						response.checkResult();
						
						if (data == null || data.length() == 0) {
							break;
						} else {
							seq++;
						}
					}
				} finally {
					client.close();
				}
			}
		}
		Cuboid.update(this, ctx);
	}
	
	/**
	 * �ڵ����ִ�и��������α�����
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeAppendByCSID(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		Integer csProxyId = (Integer) attributes.get("csProxyId");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			CursorProxy csProxy = (CursorProxy)rm.getProxy(csProxyId.intValue());
			tmd.getTableMetaData().append(csProxy.getCursor());
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * �ڵ����ִ��ȡ��һ����¼��άֵ
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGetFirstKeyValue(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData table = tmd.getTableMetaData();
			ICursor cursor = table.cursor(table.getAllSortedColNames());
			
			Sequence seq = cursor.peek(1);
			if (seq == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("dw.needClusterCursor"));
			}
			
			// ��Ϊ��executeAppendByData������ʱ�ļ�
			//FileObject fo = FileObject.createTempFileObject();
			//tmd.setTempFile(fo);
			
			cursor.close();
			Record record = (Record) seq.get(1);
			return new Response(record);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}

	/**
	 * �ڵ����ִ�и���������������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeAppendByData(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		Sequence data = (Sequence) attributes.get("data");
		int seq = (Integer) attributes.get("seq");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());

			Context ctx = ClusterUtil.createContext(js, attributes);
			if (seq == 0) {
				// ��һ�ε��ô�����ʱ�ļ����洫��������
				tmd.createTempFile();
			}
			
			FileObject fo = tmd.getTempFile();
			if (data == null || data.length() == 0) {
				BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
				tmd.getTableMetaData().append(bfc);
			} else {
				fo.exportSeries(data, "ab", null);
			}
			
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}

	private static Sequence fetchToValue(ICursor cursor, String []names, Object []vals) {
		Sequence seq = cursor.peek(ICursor.FETCHCOUNT);
		if (seq == null || seq.length() == 0) return null;
		
		int fcount = names.length;
		int []findex = new int[fcount];
		DataStruct ds = ((Record)seq.getMem(1)).dataStruct();
		for (int f = 0; f < fcount; ++f) {
			findex[f] = ds.getFieldIndex(names[f]);
			if (findex[f] == -1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(names[f] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		//Sequence result = null;
		Object []curVals = new Object[fcount];
		
		int len = seq.length();
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)seq.getMem(i);
			for (int f = 0; f < fcount; ++f) {
				curVals[f] = r.getNormalFieldValue(findex[f]);
			}
			
			if (Variant.compareArrays(curVals, vals) >= 0) {
				if (i == 1) {
					return null;
				} else {
					return cursor.fetch(i - 1);
				}
			}
		}

		cursor.skip(ICursor.FETCHCOUNT);
		return seq;
	}
	
	/**
	 * ��������
	 * @param data Ҫ���µ����ݣ�����Ҫ������
	 * @param opt 'n',����д��ɹ�������
	 * @return Sequence
	 */
	public Sequence update(Sequence data, String opt) {
		if (data == null || data.length() == 0) {
			return null;
		}
		
		checkDistributedFile();
		
		Sequence result = new Sequence();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		// ��cursorProxyId������Ӧ�Ľڵ�����ڵ����update

		if (distribute != null) {
			Sequence []groups = group(data, distribute, ctx, unitCount);
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				try {
					if (groups[i] == null || groups[i].length() == 0) { 
						continue;
					}
					
					UnitCommand command = new UnitCommand(UnitCommand.GT_UPDATE);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					command.setAttribute("data", groups[i]);
					command.setAttribute("opt", opt);
					
					Response response = client.send(command);
					Sequence seq = (Sequence)response.checkResult();
					if (seq != null && seq.length() != 0) {
						result.addAll(seq);
					}
				} finally {
					client.close();
				}
			}
		} else {
			Object [][]firstKeyValues = new Object[unitCount][];
			String []keys = null;

			//get first values
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				try {
					UnitCommand command = new UnitCommand(UnitCommand.GT_FIRST_KEY_VALUE);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					
					Response response = client.send(command);
					Record record = (Record) response.checkResult();
					firstKeyValues[i] = record.getFieldValues();
					if (keys == null) {
						keys = record.dataStruct().getFieldNames();
					}
				} finally {
					client.close();
				}
			}
			
			data.sortFields(keys);
			ICursor cursor = new MemoryCursor(data);
			
			for (int i = 0; i < unitCount; ++i) {
				UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
				try {
					if (i == unitCount - 1) {
						data = cursor.fetch(ICursor.FETCHCOUNT);
					} else {
						data = fetchToValue(cursor, keys, firstKeyValues[i + 1]);
					}
					
					if (data == null) {
						continue;
					}
					
					UnitCommand command = new UnitCommand(UnitCommand.GT_UPDATE);
					command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
					command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
					command.setAttribute("data", data);
					command.setAttribute("opt", opt);
					
					Response response = client.send(command);
					Sequence seq = (Sequence) response.checkResult();
					if (seq != null && seq.length() != 0) {
						result.addAll(seq);
					}
				} finally {
					client.close();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * �ڵ����ִ�и�������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeUpdate(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		Sequence data = (Sequence) attributes.get("data");
		String opt = (String) attributes.get("opt");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());

			Sequence result = tmd.getTableMetaData().update(data, opt);
			return new Response(result);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ɾ������
	 * @param data Ҫ���µ����� ������Ҫ������
	 * @param opt 'n',����ɾ���ɹ�������
	 * @return Sequence
	 */
	public Sequence delete(Sequence data, String opt) {
		checkDistributedFile();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		// ��cursorProxyId������Ӧ�Ľڵ�����ڵ����delete

		Sequence result = new Sequence();
		if (data == null || data.length() == 0) {
			return null;
		}
		
		for (int i = 0; i < unitCount; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			try {
				UnitCommand command = new UnitCommand(UnitCommand.GT_DELETE);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("data", data);
				command.setAttribute("opt", opt);
				
				Response response = client.send(command);
				Sequence seq = (Sequence) response.checkResult();
				if (seq != null && seq.length() != 0)
					result.addAll(seq);
			} finally {
				client.close();
			}
		}
		
		return result;
	}
	
	/**
	 * �ڵ����ִ��ɾ��ָ������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeDelete(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		Sequence data = (Sequence) attributes.get("data");
		String opt = (String) attributes.get("opt");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());

			Sequence result = tmd.getTableMetaData().delete(data, opt);
			return new Response(result);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * ɾ������
	 * @param indexName �����������indexNameΪnull����ʾɾ������
	 * @return Sequence
	 */
	public Sequence deleteIndex(String indexName) {
		Sequence result = new Sequence();
		checkDistributedFile();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		// ��cursorProxyId������Ӧ�Ľڵ�����ڵ����delete

		for (int i = 0; i < unitCount; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			try {
				UnitCommand command = new UnitCommand(UnitCommand.GT_DELETE_INDEX);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("indexName", indexName);
				
				Response response = client.send(command);
				Object val = response.checkResult();
				result.add(val);//����ÿ���ڵ����ִ�����
			} finally {
				client.close();
			}
		}
		return result;
	}
	
	/**
	 * �ڵ����ɾ������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeDeleteIndex(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String indexName = (String) attributes.get("indexName");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());

			boolean result = tmd.getTableMetaData().deleteIndex(indexName);
			return new Response(new Boolean(result));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	//��������
	public void createIndex(String I, String []fields, Object obj, String opt, Expression w) {
		checkDistributedFile();
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		// ��cursorProxyId������Ӧ�Ľڵ�����ڵ����index

		for (int i = 0; i < unitCount; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			try {
				UnitCommand command = new UnitCommand(UnitCommand.GT_INDEX);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("I", I);
				command.setAttribute("fields", fields);
				command.setAttribute("obj", obj);
				command.setAttribute("opt", opt);
				if (w != null) 
					command.setAttribute("w", w.toString());
				
				Response response = client.send(command);
				response.checkResult();
			} finally {
				client.close();
			}
		}
	}

	/**
	 * �ڵ����ִ�д�������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeCreateIndex(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String I = (String) attributes.get("I");
		String []fields = (String[]) attributes.get("fields");
		Object obj = attributes.get("obj");
		String opt = (String) attributes.get("opt");
		String exp = (String) attributes.get("w");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			TableMetaData table = (TableMetaData)tmd.getTableMetaData();
			Context ctx = ClusterUtil.createContext(js, attributes);
			Expression w = exp == null ? null : new Expression(ctx, exp);
			
			if (obj == null) {
				if  (opt != null) {
					if (opt.indexOf('w') != -1) {
						TableFulltextIndex index = new TableFulltextIndex(table, I);
						index.create(fields, opt, ctx, w);
					} else {
						//load index
						FileObject indexFile = null;
						String dir = table.getGroupTable().getFile().getAbsolutePath() + "_";
						if (I != null) {
							indexFile = new FileObject(dir + table.getTableName() + "_" + I);
							if (indexFile.isExists()) {
								ITableIndex index = table.getTableMetaDataIndex(indexFile, I, true);
								if (opt.indexOf('2') != -1) {
									index.loadAllBlockInfo();
								} else if (opt.indexOf('3') != -1) {
									index.loadAllKeys();
								} else if (opt.indexOf('0') != -1) {
									index.unloadAllBlockInfo();
								}
							}
						}
					}
				} else {
					TableMetaDataIndex index = new TableMetaDataIndex(table, I);
					index.create(fields, opt, ctx, w);
				}
			} else if (obj instanceof String[]) {
				TableKeyValueIndex index = new TableKeyValueIndex(table, I);
				index.create(fields, (String[]) obj, opt, ctx, w);
			} else if (obj instanceof Integer) {
				TableHashIndex index = new TableHashIndex(table, I, (Integer) obj);
				index.create(fields, opt, ctx, w);
			}
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * �������е�ά�ֶ���
	 * @return �ֶ�������
	 */
	public String[] getAllSortedColNames() {
		if (sortedColNames != null) {
			return sortedColNames;
		}
		
		Cluster cluster = getCluster();
		UnitClient client = new UnitClient(cluster.getHost(0), cluster.getPort(0));
		try {
			UnitCommand command = new UnitCommand(UnitCommand.GT_GET_PKEY);
			command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
			command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[0]));
			Response response = client.send(command);
			sortedColNames = (String[]) response.checkResult();
			return sortedColNames;
		} finally {
			client.close();
		}
	}
	
	/**
	 * �ڵ����ִ��ȡά�ֶ���
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGetPkey(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData table = tmd.getTableMetaData();
			String []pkeys = table.getAllSortedColNames();
			return new Response(pkeys);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}

	/**
	 * ���������ֶ���
	 * @return �ֶ�������
	 */
	public String[] getAllColNames() {
		if (allColNames != null) {
			return allColNames;
		}
		
		Cluster cluster = getCluster();
		UnitClient client = new UnitClient(cluster.getHost(0), cluster.getPort(0));
		try {
			UnitCommand command = new UnitCommand(UnitCommand.GT_GET_COL_NAMES);
			command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
			command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[0]));
			Response response = client.send(command);
			allColNames = (String[]) response.checkResult();
			return allColNames;
		} finally {
			client.close();
		}
	}
	
	/**
	 * �ڵ����ִ��ȡά�ֶ���
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeGetAllColNames(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData table = tmd.getTableMetaData();
			String []names = table.getAllColNames();
			return new Response(names);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	Expression getDistribute() {
		return distribute;
	}

	void setDistribute(Expression distribute) {
		this.distribute = distribute;
	}
	
	/**
	 * T.new/news/derive
	 * @param exps	ѡ���ֶλ���ʽ
	 * @param fields	ѡ���ֶ�������
	 * @param cursor2	
	 * @param type
	 * @param option
	 * @param filter
	 * @param fkNames
	 * @param codes
	 * @return
	 */
	public ClusterCursor news(Expression []exps, String []fields, Object cursor2, 
			int type, String option,  Expression filter, String []fkNames, Sequence []codes) {
		Cluster cluster = getCluster();
		int unitCount = cluster.getUnitCount();
		
		Boolean isSeq = false;
		ClusterCursor cs = null;
		ClusterMemoryTable cmt = null;
		
		if (cursor2 instanceof ClusterCursor) {
			// ��cursorProxyId������Ӧ�Ľڵ�����ڵ��ȡ��cursor��append
			cs = (ClusterCursor)cursor2;
			if (!cluster.isEquals(cs.getCluster())) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("dw.mcsNotMatch"));
			}
		} else if (cursor2 instanceof ClusterMemoryTable) {
			cmt = (ClusterMemoryTable)cursor2;
			isSeq = true;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.mcsNotMatch"));
		}
		
		int len = exps.length;
		String []expStrs = new String[len];
		for (int i = 0; i < len; ++i) {
			expStrs[i] = exps[i].toString();
		}
		
		String filterStr = filter == null ? null : filter.toString();
		
		int []newCursorProxyIds = new int[unitCount];
		int []cs2ProxyIds = isSeq ? cmt.getProxyIds() : cs.getCursorProxyIds();
		for (int i = 0; i < unitCount; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));
			try {
				UnitCommand command = new UnitCommand(UnitCommand.GT_NEWS);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				command.setAttribute("expStrs", expStrs);
				command.setAttribute("fields", fields);
				command.setAttribute("type", new Integer(type));
				command.setAttribute("option", option);
				command.setAttribute("filterStr", filterStr);
				command.setAttribute("fkNames", fkNames);
				command.setAttribute("codes", codes);
				command.setAttribute("unit", new Integer(i));
				command.setAttribute("isSeq", isSeq);
				command.setAttribute("cs2ProxyId", new Integer(cs2ProxyIds[i]));
				
				Response response = client.send(command);
				Integer id = (Integer)response.checkResult();
				newCursorProxyIds[i] = id.intValue();
			} finally {
				client.close();
			}
		}
		
		return new ClusterCursor(cluster, newCursorProxyIds, true);
	}
	
	/**
	 * �ڵ����ִ��news����
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeNews(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		String []expStrs = (String[])attributes.get("expStrs");
		String []fields = (String[])attributes.get("fields");
		Integer type = (Integer) attributes.get("type");
		String option = (String) attributes.get("option");
		String filterStr = (String) attributes.get("filterStr");
		String []fkNames = (String[])attributes.get("fkNames");
		Sequence []codes = (Sequence[]) attributes.get("codes");
		Integer unit = (Integer) attributes.get("unit");
		Boolean isSeq = (Boolean) attributes.get("isSeq");
		Integer cs2ProxyId = (Integer) attributes.get("cs2ProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			Context ctx = ClusterUtil.createContext(js, attributes);
			
			int count = expStrs.length;
			Expression exps[] = new Expression[count];
			for (int f = 0; f < count; ++f) {
				exps[f] = new Expression(ctx, expStrs[f]);
			}
			
			Expression filter = filterStr == null ? null : new Expression(ctx, filterStr);
			
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ICursor cursor = null;
			if (isSeq) {
				TableProxy tableProxy = (TableProxy)rm.getProxy(cs2ProxyId.intValue());
				cursor = new MemoryCursor(tableProxy.getTable());
			} else {
				CursorProxy cs2Proxy = (CursorProxy)rm.getProxy(cs2ProxyId.intValue());
				cursor = cs2Proxy.getCursor();
			}
			
			if (cursor instanceof MultipathCursors) {
				MultipathCursors mcursor = (MultipathCursors) cursor;
				ICursor cursors[] = mcursor.getCursors();
				int pathCount = cursors.length;
				for (int i = 0; i < pathCount; ++i) {
					Expression w = null;
					if (filter != null) {
						w = filter.newExpression(ctx); // �ֶβ��ж�ȡʱ��Ҫ���Ʊ��ʽ��ͬһ�����ʽ��֧�ֲ�������
					}
					ICursor cs = new JoinCursor(tmd.getTableMetaData(), exps, fields, cursors[i], 
							type | 0x10, option, w, fkNames, codes, ctx);
					cursors[i] = cs;
				}
				cursor = new MultipathCursors(cursors, ctx);
			} else {
				cursor = new JoinCursor(tmd.getTableMetaData(), exps, fields, cursor, type, 
						option, filter, fkNames, codes, ctx);
			}
			
			IProxy proxy = new CursorProxy(cursor, unit);
			rm.addProxy(proxy);
			return new Response(new Integer(proxy.getProxyId()));
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	// �رռ�Ⱥ���
	public void close() {
		Cluster cluster = clusterFile.getCluster();
		int count = cluster.getUnitCount();
		
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(cluster.getHost(i), cluster.getPort(i));			
			try {
				UnitCommand command = new UnitCommand(UnitCommand.CLOSE_GT);
				command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
				command.setAttribute("tmdProxyId", tmdProxyIds[i]);
				client.send(command);
			} finally {
				client.close();
			}
		}
	}

	/**
	 * �ڵ����ִ�йر����
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeCloseGroupTable(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy)rm.getProxy(tmdProxyId.intValue());
			tmd.close();
			return new Response();
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	//Զ��cgroups,Ŀǰ�ò������ȱ���
	public Sequence cgroups(String []expNames, String []names, String []newExpNames, String []newNames,
			Expression w, boolean hasM, int n, String option,  Context ctx) {
		ClusterFile clusterFile = getClusterFile();
		int count = clusterFile.getUnitCount();
		
		Sequence result = null;
		for (int i = 0; i < count; ++i) {
			UnitClient client = new UnitClient(clusterFile.getHost(i), clusterFile.getPort(i));

			try {
				UnitCommand command = new UnitCommand(UnitCommand.GT_CGROUPS);
				command.setAttribute("jobSpaceId", clusterFile.getJobSpaceId());
				command.setAttribute("tmdProxyId", new Integer(tmdProxyIds[i]));
				
				command.setAttribute("expNames", expNames);
				command.setAttribute("names", names);
				command.setAttribute("newExpNames", newExpNames);
				command.setAttribute("newNames", newNames);
				command.setAttribute("w", w == null ? null : w.toString());
				command.setAttribute("hasM", new Boolean(hasM));
				command.setAttribute("n", new Integer(n));
				command.setAttribute("option", option);
				
				ClusterUtil.setParams(command, w, ctx);
				Response response = client.send(command);
				if (result == null) {
					result = (Sequence)response.checkResult();
				} else {
					result.addAll( (Sequence)response.checkResult());
				}
			} finally {
				client.close();
			}
		}
		return result;
	}
	
	/**
	 * �ڵ����ִ������Ԥ�������������������
	 * @param attributes ����
	 * @return Response �������Ļ�Ӧ
	 */
	public static Response executeCgroups(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");

		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			TableMetaData srcTable = (TableMetaData) tmd.getTableMetaData();
			Context ctx = ClusterUtil.createContext(js, attributes);
			
			String []expNames = (String[])attributes.get("expNames");
			String []names = (String[])attributes.get("names");
			String []newExpNames = (String[])attributes.get("newExpNames");
			String []newNames = (String[])attributes.get("newNames");
			String w = (String)attributes.get("w");
			Expression exp = w == null ? null : new Expression(ctx, w);
			Boolean hasM = (Boolean)attributes.get("hasM");
			Integer n = (Integer) attributes.get("n");
			String option = (String) attributes.get("option");
			Sequence result = Cuboid.cgroups(expNames, names, newExpNames, newNames, srcTable, exp, hasM, n, option, ctx);
			return new Response(result);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
}