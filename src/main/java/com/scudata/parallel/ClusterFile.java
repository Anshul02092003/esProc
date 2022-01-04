package com.scudata.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scudata.common.IntArrayList;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.ResourceManager;
import com.scudata.dm.Sequence;
import com.scudata.dw.ITableMetaData;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.resources.ParallelMessage;

/**
 * ��Ⱥ�ļ�
 * @author RunQian
 *
 */
public class ClusterFile implements IClusterObject {
	private Cluster cluster; // �ڵ����Ϣ
	private String fileName; // �ļ������ַ����������ļ������飨�ַ������飩
	private String opt; // ѡ��
	
	private PartitionFile []pfs; // ÿ��������Ӧ�Ľڵ��
	private boolean isDistributedFile = true; // true���ֲ��ļ���false����д�ļ�
	
	private ClusterFile() {
	}
		
	/**
	 * ����Զ���ļ��������
	 * @param host �ڵ��IP��ַ
	 * @param port �ڵ���˿�
	 * @param fileName �ļ���
	 * @param part ����
	 * @param ctx ����������
	 */
	public ClusterFile(String host, int port, String fileName, int part, Context ctx) {
		cluster = new Cluster(new String[]{host}, new int[]{port}, ctx);
		pfs = new PartitionFile[1];
		pfs[0] = new PartitionFile(this, host, port, part);
		
		this.fileName = fileName;
		if (part > 0) {
			isDistributedFile = true;
		}
	}

	/**
	 * ������ָ����Ⱥ�ļ�ͬ�ֲ��ļ�Ⱥ�ļ�
	 * @param clusterFile ���ռ�Ⱥ�ļ�
	 * @param fileName �ļ���
	 * @param parts �������飬ʡ�����ò��յļ�Ⱥ�ļ��ķ���
	 * @param opt ѡ��
	 */
	public ClusterFile(ClusterFile clusterFile, String fileName, int []parts, String opt) {
		this.fileName = fileName;
		this.opt = opt;
		
		if (parts == null || parts.length == 0) {
			isDistributedFile = clusterFile.isDistributedFile;
			cluster = clusterFile.cluster;
			int count = clusterFile.pfs.length;
			pfs = new PartitionFile[count];
			
			for (int i = 0; i < count; ++i) {
				pfs[i] = clusterFile.pfs[i].dup(this);
			}
		} else {
			isDistributedFile = true;
			int count = parts.length;
			String []hosts = new String[count];
			int []ports = new int[count];
			pfs = new PartitionFile[count];
			
			Next:
			for (int i = 0; i < count; ++i) {
				for (PartitionFile pf : clusterFile.pfs) {
					if (pf.getPartition() == parts[i]) {
						pfs[i] = pf.dup(this);
						hosts[i] = pf.getHost();
						ports[i] = pf.getPort();
						continue Next;
					}
				}
				
				MessageManager mm = ParallelMessage.get();
				throw new RQException(mm.getMessage("PartitionUtil.lackfile2", fileName, parts[i]));
			}
			
			Context ctx = clusterFile.cluster.getContext();
			cluster = new Cluster(hosts, ports, ctx);
		}
	}
	
	/**
	 * �Ӹ����Ľڵ���б���ѡ������ָ���ֱ��ļ��Ľڵ����������Ⱥ�ļ�
	 * @param cluster �ڵ����Ϣ
	 * @param fileName �ļ���
	 * @param parts ��������
	 * @param opt ѡ��
	 */
	public ClusterFile(Cluster cluster, String fileName, int []parts, String opt) {
		this.fileName = fileName;
		this.opt = opt;
		isDistributedFile = true;
		
		String []hosts = cluster.getHosts();
		int []ports = cluster.getPorts();
		int hcount = hosts.length;
		int pcount = parts.length;
		pfs = new PartitionFile[pcount];

		// ��Ⱥ��д���ļ���z��hsһһ��Ӧ
		if (opt != null && opt.indexOf('w') != -1) {
			if (hcount != pcount) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("file" + mm.getMessage("function.paramCountNotMatch"));
			}
			
			for (int i = 0; i < hcount; ++i) {
				pfs[i] = new PartitionFile(this, hosts[i], ports[i], parts[i]);
			}
			
			this.cluster = cluster;
			return;
		}
		
		// �г�ÿ������������Щ�ڵ����
		List<IntArrayList> hostList = new ArrayList<IntArrayList>();
		for (int i = 0; i < hcount; ++i) {
			try {
				int []partList = ClusterUtil.listFileParts(hosts[i], ports[i], fileName, parts);
				for (int p : partList) {
					for (int size = hostList.size(); size <= p; ++size) {
						hostList.add(new IntArrayList());
					}
					
					hostList.get(p).addInt(i);
				}
			} catch (Exception e) {
				// ���쳣����ʱ����ʹ�ô˽ڵ��������ѭ��
			}
		}
		
		// ÿ������ѡ��һ���ڵ����ƽ������
		int []weights = new int[hcount];
		String []useHosts = new String[pcount];
		int []usePorts = new int[pcount];
		
		for (int i = 0; i < pcount; ++i) {
			int p = parts[i];
			if (hostList.size() <= p) {
				MessageManager mm = ParallelMessage.get();
				throw new RQException(mm.getMessage("PartitionUtil.lackfile2", fileName, p));
			}
			
			IntArrayList list = hostList.get(p);
			int size = list.size();
			if (size == 0) {
				MessageManager mm = ParallelMessage.get();
				throw new RQException(mm.getMessage("PartitionUtil.lackfile2", fileName, p));
			}
			
			int h = list.getInt(0);
			for (int j = 1; j < list.size(); ++j) {
				int cur = list.getInt(j);
				if (weights[cur] < weights[h]) {
					h = cur;
				}
			}
			
			weights[h]++;
			pfs[i] = new PartitionFile(this, hosts[h], ports[h], p);
			useHosts[i] = hosts[h];
			usePorts[i] = ports[h];
		}
		
		this.cluster = new Cluster(useHosts, usePorts, cluster.getContext());
	}
	
	/**
	 * ������Ⱥ�ֲ�д�ļ�
	 * @param cluster �ڵ����Ϣ
	 * @param fileName �ļ���
	 * @param opt ѡ�w�������ֲ�д�ļ�
	 */
	public ClusterFile(Cluster cluster, String fileName, String opt) {
		this.cluster = cluster;
		this.fileName = fileName;
		this.opt = opt;
		isDistributedFile = true;
		
		String []hosts = cluster.getHosts();
		int []ports = cluster.getPorts();
		int hcount = hosts.length;
		pfs = new PartitionFile[hcount];
		
		for (int i = 0; i < hcount; ++i) {
			pfs[i] = new PartitionFile(this, hosts[i], ports[i], i + 1);
		}
	}
	
	/**
	 * ��ָ���ļ��������뵱ǰ��Ⱥ�ļ�ͬ�ֲ��ļ�Ⱥ�ļ�
	 * @param pathName �ļ���
	 * @return ��Ⱥ�ļ�
	 */
	public ClusterFile newFile(String pathName) {
		ClusterFile clusterFile = new ClusterFile();
		clusterFile.cluster = cluster;
		clusterFile.fileName = pathName;
		clusterFile.isDistributedFile = isDistributedFile;
		clusterFile.opt = opt;
		
		PartitionFile []pfs = this.pfs;
		int count = pfs.length;
		clusterFile.pfs = new PartitionFile[count];
		for (int i = 0; i < count; ++i) {
			clusterFile.pfs[i] = pfs[i].dup(clusterFile);
		}
		
		return clusterFile;
	}
	
	/**
	 * �����Ƿ��Ƿֲ��ļ�
	 * @return true���ֲ��ļ���false����д�ļ�
	 */
	public boolean isDistributedFile() {
		return isDistributedFile;
	}
	
	/**
	 * ȡ�ڵ����
	 * @return
	 */
	public int getUnitCount() {
		return cluster.getUnitCount();
	}
	
	/**
	 * ȡ��Ⱥ�ļ���Ӧ��ÿ���ڵ���ķֱ��ļ�
	 * @return
	 */
	public PartitionFile[] getPartitionFiles() {
		return pfs;
	}
	
	//�������ȡ�ֻ�
	public String getHost(int unit) {
		return cluster.getHost(unit);
	}
	
	/**
	 * ȡָ���ڵ���Ķ˿�
	 * @param unit
	 * @return
	 */
	public int getPort(int unit) {
		return cluster.getPort(unit);
	}
	
	// �ļ������ַ����������ļ������飨�ַ������飩
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * ȡѡ��
	 * @return
	 */
	public String getOption() {
		return opt;
	}
	
	/**
	 * ȡ����������
	 * @return
	 */
	public Context getContext() {
		return cluster.getContext();
	}
	
	/**
	 * ȡ����ռ��ʶ
	 * @return
	 */
	public String getJobSpaceId() {
		return cluster.getJobSpaceId();
	}
	
	/**
	 * ȡ��Ⱥ�ڵ��
	 */
	public Cluster getCluster() {
		return cluster;
	}
	
	/**
	 * �������ڼ��ļ��ļ�Ⱥ�α�
	 * @param fields Ҫ��ȡ���ֶ�
	 * @param opt
	 * @return
	 */
	public ClusterCursor createBinaryCursor(String []fields, String opt) {
		int count = pfs.length;
		int []proxyIds = new int[count];
		boolean isDistributed = isDistributedFile;
		if (!isDistributed && opt != null && opt.indexOf('z') != -1) {
			isDistributed = true;
			for (int i = 0; i < count; ++i) {
				proxyIds[i] = pfs[i].createBinaryCursor(fields, opt, i + 1, count, i);
			}
		} else {
			for (int i = 0; i < count; ++i) {
				proxyIds[i] = pfs[i].createBinaryCursor(fields, opt, 0, 0, i);
			}
		}
		
		return new ClusterCursor(this, proxyIds, isDistributed);
	}
	
	/**
	 * ������Ⱥ���
	 * @param colNames �ֶ�������
	 * @param serialBytesLen �źż��ֶγ�������
	 * @param segmentCol �ֶ��ֶ�
	 * @param serialLen ����ֶ��ֶ����źţ���ָ�����õ��źų���
	 * @param writePsw д����
	 * @param readPsw ������
	 * @param distribute �������ʽ
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ��Ⱥ���
	 */
	public ClusterTableMetaData createGroupTable(String []colNames, Expression distribute, String opt, Context ctx) {
		int count = pfs.length;
		int []proxyIds = new int[count];
		String dis = distribute == null ? null : distribute.toString();
		
		for (int i = 0; i < count; ++i) {
			proxyIds[i] = pfs[i].createGroupTable(colNames, dis, opt);
		}
		
		ClusterTableMetaData table = new ClusterTableMetaData(this, proxyIds, ctx);
		table.setDistribute(distribute);
		return table;
	}
	
	/**
	 * �����
	 * @param ctx ����������
	 * @return ��Ⱥ���
	 */
	public ClusterTableMetaData openGroupTable(Context ctx) {
		int count = pfs.length;
		int []proxyIds = new int[count];
		for (int i = 0; i < count; ++i) {
			proxyIds[i] = pfs[i].openGroupTable();
		}
		
		UnitClient client = new UnitClient(cluster.getHost(0), cluster.getPort(0));
		Expression distribute = null;
		
		try {
			UnitCommand command = new UnitCommand(UnitCommand.GET_GT_DISTRIBUTE);
			command.setAttribute("jobSpaceId", cluster.getJobSpaceId());
			command.setAttribute("tmdProxyId", new Integer(proxyIds[0]));
			
			Response response = client.send(command);
			String str = (String)response.checkResult();
			if (str != null) {
				distribute = new Expression(ctx, str);
			}
		} finally {
			client.close();
		}
		
		ClusterTableMetaData table = new ClusterTableMetaData(this, proxyIds, ctx);
		table.setDistribute(distribute);
		return table;
	}
	
	/**
	 * �ڽڵ����ִ��ȡ�ֲ����ʽ
	 * @param attributes
	 * @return
	 */
	public static Response executeGetDistribute(HashMap<String, Object> attributes) {
		String jobSpaceID = (String) attributes.get("jobSpaceId");
		Integer tmdProxyId = (Integer) attributes.get("tmdProxyId");
		
		try {
			JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
			ResourceManager rm = js.getResourceManager();
			TableMetaDataProxy tmd = (TableMetaDataProxy) rm.getProxy(tmdProxyId.intValue());
			ITableMetaData table = tmd.getTableMetaData();
			String distribute = table.getDistribute();
			return new Response(distribute);
		} catch (Exception e) {
			Response response = new Response();
			response.setException(e);
			return response;
		}
	}
	
	/**
	 * �������
	 * @param file ������Ӧ���ļ���ʡ���򸲸�Դ�ļ�
	 * @param option ѡ��
	 * @param distribute �·ֲ����ʽ
	 * @return �������
	 */
	public Sequence resetGroupTable(String file, String option, String distribute) {
		if (!isDistributedFile()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.needDistributed"));
		}
		
		Sequence result = new Sequence();
		int count = pfs.length;
		for (int i = 0; i < count; ++i) {
			result.add(pfs[i].resetGroupTable(file, option, distribute));
		}
		
		return result;
	}
}