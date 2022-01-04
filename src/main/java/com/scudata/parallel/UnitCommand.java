package com.scudata.parallel;

import java.io.Serializable;
import java.util.HashMap;

public class UnitCommand implements Serializable {
	private static final long serialVersionUID = 559160970976735495L;

	//public static final int GET_UNITS = 0; // ȡ�ֻ������н���
	public static final int CREATE_GT = 1; // ������Ⱥ���
	public static final int GET_TABLEMETADATA = 2; // �ڷֻ��ϴ�������Զ�̴���
	public static final int GET_GT_DISTRIBUTE = 3; // ȡ���ֲ�
	public static final int CLOSE_GT = 4; // �رռ�Ⱥ���
	public static final int OPEN_GT = 5; // �򿪼�Ⱥ���
	public static final int LIST_FILE_PARTS = 6; // �г��ڵ��������Щ�ֱ��ļ�
	
	public static final int MEMORY_GT = 10; // �ڴ滯���
	public static final int MEMORY_CLUSTERCURSOR = 11; // �ڴ滯��Ⱥ�α�
	public static final int MEMORY_TABLE = 12; // ��Զ������ɼ�Ⱥ�ڱ�
	
	public static final int CREATE_MT_INDEX = 13; // Զ���ڱ�����
	public static final int GET_MT_ROW = 14; // �����ڱ��¼
	public static final int DUP_CLUSTER_MT = 15; // ����Ⱥ�ڱ�Tƴ�ɱ����ڱ�
	public static final int DUP_LOCAL_MT = 16; // �ѱ����ڱ��Ƴɼ�Ⱥ��д�ڱ�
	public static final int GET_MT_ROWS = 17; // ȡ�����ڱ��¼
	public static final int MT_ADD_OPERATION = 18; // Ϊ��Ⱥ�ڱ�������switch
	public static final int CLOSE_MT = 19; // �ͷż�Ⱥ�ڱ�
	
	public static final int CREATE_BINARY_CURSOR = 20; //����Զ�̼��ļ��α�
	public static final int CREATE_GT_CURSOR = 21; // ����Զ�̸����α꣬�Ǹ�д��ʱseg=-1
	public static final int CREATE_SYNC_GT_CURSOR = 22; // ����ͬ���ֶμ�Ⱥ�α�
	public static final int CREATE_GT_ICURSOR = 23; // ���������α�
	public static final int CREATE_MT_CURSOR = 24; // �ɼ�Ⱥ�ڱ�����Ⱥ�α�
	
	public static final int CURSOR_ADD_OPERATION = 30; // Ϊ�α긽������
	public static final int CURSOR_FETCH = 31; // �α�ȡ��
	public static final int CURSOR_SKIP = 32; // �α���������
	public static final int CURSOR_CLOSE = 33;
	public static final int CURSOR_GET_MINVALUES = 34;
	public static final int CURSOR_TO_REMOTE = 35; // �Ѽ�Ⱥ�α��ɶ��Զ���α�
	
	public static final int GROUPS = 40;
	public static final int JOINX = 41;
	public static final int SORTX = 42;
	public static final int GROUPX = 43;
	public static final int TOTAL = 44;
	
	public static final int CHANNEL_CS = 70; // �ɼ�Ⱥ�α괴����Ⱥ�ܵ�
	public static final int CHANNEL_CH = 71; // �ɼ�Ⱥ�ܵ�������Ⱥ�ܵ�
	public static final int CHANNEL_ADD_OPERATION = 72; // Ϊ�ܵ���������
	public static final int CHANNEL_GROUPS = 73;
	public static final int CHANNEL_GROUPX = 74;
	public static final int CHANNEL_SORTX = 75;
	public static final int CHANNEL_FETCH = 76;
	public static final int CHANNEL_RESULT = 77;
	
	public static final int GT_APPEND_BY_CSID = 80; // ��ͬ�ֲ��α���append
	public static final int GT_APPEND_BY_DATA = 81; // �ɱ����α���append������άֵ������Ӧ�����ݵ��ڵ����append
	public static final int GT_FIRST_KEY_VALUE = 82;
	public static final int GT_UPDATE = 83;
	public static final int GT_DELETE = 84;
	public static final int GT_DELETE_INDEX = 85;
	public static final int GT_INDEX = 86;
	public static final int GT_RESET = 87;
	public static final int GT_GET_PKEY = 88;
	public static final int GT_NEWS = 89;
	public static final int GT_CGROUPS = 90;//�������ѯ
	public static final int GT_GET_COL_NAMES = 91;
	
	public static final int PSEUDO_CURSOR = 101;//������α�
	public static final int PSEUDO_ADD_COLNAME = 102;
	public static final int PSEUDO_ADD_OPERATION = 103;
	public static final int PSEUDO_CLONE = 104;
	public static final int PSEUDO_CREATE = 105;
	public static final int PSEUDO_JOINX = 106;
	public static final int PSEUDO_SET_MCS = 107;
	public static final int PSEUDO_SET_PATHCOUNT = 108;
	//����Զ����ʱ�ļ�
	public static final int CREATE_TMPFILE = 200;

	//�������ļ�localFile������ʱ������Զ����ʱ�ļ�
	public static final int CREATE_TMPFILE_FROM = 201;
	
	//�����׼���������Сֵ������¼д����Ӧ�ֻ���Զ����ʱ�ļ������ظ��ֻ����Ӧ����ʱ�ļ�����
	//��RemoteFileֱ��дԶ�̣�������д�������ٴ���
	//attr: int proxyId, String firstKey, Object[] segMinValues, String[] hosts, int[] ports
	//���ݸ��ֻ���������ʱ�ļ�����reduce�������α�proxyId
	public static final int SHUFFLE = 202;
	
	//�����м��keys=null��������������
	//attr: int csProxyId, String[] keys
	//return: int ���򸽱�proxyId
	public static final int INTERM = 205;
	
	
	//private String jobSpaceId; // ��attributes��
	private int command;
	private HashMap<String, Object> attributes = new HashMap<String, Object>();
	
	public UnitCommand(int command) {
		this.command = command;
	}
		
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Response execute() {
		switch (command) {
			case CREATE_BINARY_CURSOR:
				return PartitionFile.executeCreateBinaryCursor(attributes);
			case CURSOR_ADD_OPERATION:
				return ClusterCursor.executeAddOperation(attributes);
			case CURSOR_FETCH:
				return ClusterCursor.executeFetch(attributes);
			case CURSOR_SKIP:
				return ClusterCursor.executeSkip(attributes);
			case CURSOR_CLOSE:
				return ClusterCursor.executeClose(attributes);
			case CREATE_GT:
				return PartitionFile.executeCreateGroupTable(attributes);
			case OPEN_GT:
				return PartitionFile.executeOpenGroupTable(attributes);
			case LIST_FILE_PARTS:
				return ClusterUtil.executeListFileParts(attributes);
			case GET_GT_DISTRIBUTE:
				return ClusterFile.executeGetDistribute(attributes);
			case CLOSE_GT:
				return ClusterTableMetaData.executeCloseGroupTable(attributes);
			case GET_TABLEMETADATA:
				return ClusterTableMetaData.executeGetTableMetaData(attributes);
			case CREATE_GT_CURSOR:
				return ClusterTableMetaData.executeCreateCursor(attributes);
			case CREATE_SYNC_GT_CURSOR:
				return ClusterTableMetaData.executeCreateSyncCursor(attributes);
			case CREATE_GT_ICURSOR:
				return ClusterTableMetaData.executeICursor(attributes);
			case CREATE_MT_CURSOR:
				return ClusterMemoryTable.executeCreateCursor(attributes);
			case GROUPS:
				return ClusterCursor.executeGroups(attributes);
			case JOINX:
				return ClusterCursor.executeJoinx(attributes);
			case GROUPX:
				return ClusterCursor.executeGroupx(attributes);
			case SORTX:
				return ClusterCursor.executeSortx(attributes);
			case TOTAL:
				return ClusterCursor.executeTotal(attributes);
			case CURSOR_GET_MINVALUES:
				return ClusterCursor.executeGetMinValues(attributes);
			case CURSOR_TO_REMOTE:
				return ClusterCursor.executeGetParallelCursors(attributes);
			case CREATE_MT_INDEX:
				return ClusterMemoryTable.executeCreateIndex(attributes);
			case MEMORY_GT:
				return ClusterTableMetaData.executeMemory(attributes);
			case MEMORY_CLUSTERCURSOR:
				return ClusterCursor.executeMemory(attributes);
			case MEMORY_TABLE:
				return ClusterMemoryTable.executeMemory(attributes);
			case GET_MT_ROW:
				return ClusterMemoryTable.executeGetRow(attributes);
			case DUP_CLUSTER_MT:
				return ClusterMemoryTable.executeDup(attributes);
			case DUP_LOCAL_MT:
				return ClusterMemoryTable.executeDupLocal(attributes);
			case GET_MT_ROWS:
				return ClusterMemoryTable.executeGetRows(attributes);
			case MT_ADD_OPERATION:
				return ClusterMemoryTable.executeAddOperation(attributes);
			case CLOSE_MT:
				return ClusterMemoryTable.executeClose(attributes);
			case CHANNEL_CS:
				return ClusterChannel.executeCreateChannel_CS(attributes);
			case CHANNEL_CH:
				return ClusterChannel.executeCreateChannel_CH(attributes);
			case CHANNEL_ADD_OPERATION:
				return ClusterChannel.executeAddOperation(attributes);
			case CHANNEL_GROUPS:
				return ClusterChannel.executeGroups(attributes);
			case CHANNEL_GROUPX:
				return ClusterChannel.executeGroupx(attributes);
			case CHANNEL_SORTX:
				return ClusterChannel.executeSortx(attributes);
			case CHANNEL_FETCH:
				return ClusterChannel.executeFetch(attributes);
			case CHANNEL_RESULT:
				return ClusterChannel.executeResult(attributes);
			case GT_APPEND_BY_CSID:
				return ClusterTableMetaData.executeAppendByCSID(attributes);
			case GT_FIRST_KEY_VALUE:
				return ClusterTableMetaData.executeGetFirstKeyValue(attributes);
			case GT_APPEND_BY_DATA:
				return ClusterTableMetaData.executeAppendByData(attributes);
			case GT_UPDATE:
				return ClusterTableMetaData.executeUpdate(attributes);
			case GT_DELETE:
				return ClusterTableMetaData.executeDelete(attributes);
			case GT_RESET:
				return PartitionFile.executeResetGroupTable(attributes);
			case GT_INDEX:
				return ClusterTableMetaData.executeCreateIndex(attributes);
			case GT_DELETE_INDEX:
				return ClusterTableMetaData.executeDeleteIndex(attributes);
			case GT_GET_PKEY:
				return ClusterTableMetaData.executeGetPkey(attributes);
			case GT_CGROUPS:
				return ClusterTableMetaData.executeCgroups(attributes);
			case GT_NEWS:
				return ClusterTableMetaData.executeNews(attributes);
			case GT_GET_COL_NAMES:
				return ClusterTableMetaData.executeGetAllColNames(attributes);
			case PSEUDO_CURSOR:
				return ClusterPseudo.executeCreateCursor(attributes);
			case PSEUDO_ADD_OPERATION:
				return ClusterPseudo.executeAddOperation(attributes);
			case PSEUDO_ADD_COLNAME:
				return ClusterPseudo.executeAddColName(attributes);
			case PSEUDO_CLONE:
				return ClusterPseudo.executeClone(attributes);
			case PSEUDO_CREATE:
				return ClusterPseudo.executeCreateClusterPseudo(attributes);
			default:
				throw new RuntimeException();
		}
	}
}
