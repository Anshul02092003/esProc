package com.scudata.dw;

import java.io.IOException;

import com.scudata.dm.Context;
import com.scudata.dm.IResource;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.expression.Expression;

/**
 * ����ӿ���
 * @author runqian
 *
 */
public interface ITableMetaData extends IResource {
	
	void close();	
	
	/**
	 * �½���һ������
	 * @param colNames ������
	 * @param serialBytesLen �źų��ȣ�0��ʾ�����źţ�
	 * @param tableName ������
	 * @return
	 * @throws IOException
	 */
	ITableMetaData createAnnexTable(String []colNames, int []serialBytesLen, String tableName) throws IOException;
	
	/**
	 * �򿪸���
	 * @param tableName
	 * @return
	 */
	ITableMetaData getAnnexTable(String tableName);

	/**
	 * ׷���α������
	 * @param cursor
	 * @throws IOException
	 */
	void append(ICursor cursor) throws IOException;
	
	/**
	 * ׷���α������
	 * @param cursor
	 * @param opt 'a',׷�ӵ����ļ���'m',�벹�ļ��ϲ���'i',����д���ļ�
	 * @throws IOException
	 */
	void append(ICursor cursor, String opt) throws IOException;
	
	/**
	 * ����
	 * @param data Ҫ���µ����� ������Ҫ������
	 * @param opt 'n',����д��ɹ�������
	 * @return
	 * @throws IOException
	 */
	Sequence update(Sequence data, String opt) throws IOException;
	
	/**
	 * ɾ��
	 * @param data Ҫ���µ����� ������Ҫ������
	 * @param opt 'n',����ɾ���ɹ�������
	 * @return
	 * @throws IOException
	 */
	Sequence delete(Sequence data, String opt) throws IOException;

	/**
	 * �������������α�
	 * @return
	 */
	ICursor cursor();
	
	/**
	 * �������������α�
	 * @param fields ȡ���ֶ�
	 * @return
	 */
	ICursor cursor(String []fields);
	
	/**
	 * �����α�
	 * @param fields ȡ���ֶ�
	 * @param filter ���˱��ʽ
	 * @param ctx ������
	 * @return
	 */
	ICursor cursor(String []fields, Expression filter, Context ctx);
	
	/**
	 * �����α�
	 * ���expsΪnull����fieldsΪѡ���ֶ�
	 * @param exps �ֶα��ʽ
	 * @param fields ѡ���ֶα���
	 * @param filter ���˱��ʽ
	 * @param fkNames ָ��FK���˵��ֶ�����
	 * @param codes ָ��FK���˵���������
	 * @param opts �����ֶν��й�����ѡ��
	 * @param ctx
	 * @return
	 */
	ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, Context ctx);
	
	/**
	 * ���ض�·�α꣬pathCountΪ1ʱ������ͨ�α�
	 * @param exps �ֶα��ʽ
	 * @param fields ѡ���ֶα���
	 * @param filter ���˱��ʽ
	 * @param fkNames ָ��FK���˵��ֶ�����
	 * @param codes ָ��FK���˵���������
	 * @param opts �����ֶν��й�����ѡ��
	 * @param pathCount ·��
	 * @param ctx
	 * @return
	 */
	ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, int pathCount, Context ctx);
	
	/**
	 * ���طֶ��α�
	 * @param exps �ֶα��ʽ
	 * @param fields ѡ���ֶα���
	 * @param filter ���˱��ʽ
	 * @param fkNames ָ��FK���˵��ֶ�����
	 * @param codes ָ��FK���˵���������
	 * @param opts �����ֶν��й�����ѡ��
	 * @param segSeq �ڼ���
	 * @param segCount  �ֶ�����
	 * @param ctx ������
	 * @return
	 */
	ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, int pathSeq, int pathCount, Context ctx);
	
	/**
	 * ������mcsͬ���ֶεĶ�·�α�
	 * @param exps �ֶα��ʽ
	 * @param fields ѡ���ֶα���
	 * @param filter ���˱��ʽ
	 * @param fkNames ָ��FK���˵��ֶ�����
	 * @param codes ָ��FK���˵���������
	 * @param opts �����ֶν��й�����ѡ��
	 * @param mcs �ο��ֶεĶ�·�α�
	 * @param opt ѡ��
	 * @param ctx
	 * @return
	 */
	ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, MultipathCursors mcs, String opt, Context ctx);

	/**
	 * ���ηֶε��α�
	 * ���ڼ�Ⱥ�Ľڵ��
	 * @param exps ȡ���ֶα��ʽ����expsΪnullʱ����fieldsȡ����
	 * @param fields ȡ���ֶε�������
	 * @param filter ���˱��ʽ
	 * @param fkNames ָ��FK���˵��ֶ�����
	 * @param codes ָ��FK���˵���������
	 * @param opts �����ֶν��й�����ѡ��
	 * @param pathSeq �ڼ���
	 * @param pathCount �ڵ����
	 * @param pathCount2 �ڵ����ָ���Ŀ���
	 * @param ctx ������
	 * @return
	 */
	ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, int pathSeq, int pathCount, int pathCount2, Context ctx);
	
	/**
	 * �����������Ҽ�¼
	 * @param values ��������
	 * @return ��������values ������ͬ�ļ�¼
	 * @throws IOException
	 */
	Table finds(Sequence values) throws IOException;
	
	/**
	 * �����������Ҽ�¼
	 * @param values ��������
	 * @param selFields ȡ���ֶ�
	 * @return ��������values ������ͬ�ļ�¼
	 * @throws IOException
	 */
	Table finds(Sequence values, String []selFields) throws IOException;
	
	/**
	 * ʹ��������ѯ
	 * @param fields ȡ���ֶ�
	 * @param filter ���˱��ʽ
	 * @param iname �����ֶ�
	 * @param opt ����'u'ʱ,������filter��������Ĺ������ȼ�
	 * @param ctx ������
	 * @return �����α꣬Ҳ�����������α�
	 */
	ICursor icursor(String []fields, Expression filter, String iname, String opt, Context ctx);
	
	/**
	 * �޸��ֶ���
	 * @param srcFields ������
	 * @param newFields ������
	 * @param ctx
	 * @throws IOException
	 */
	void rename(String []srcFields, String []newFields, Context ctx) throws IOException;
	
	/**
	 * ���������ֶ�����������
	 * @return �����ֶ�������
	 */
	String[] getAllSortedColNames();
	
	/**
	 * ȡ�����ֶ�����������
	 * @return �����ֶ�������
	 */
	String[] getAllKeyColNames();
	
	/**
	 * ��������������������)
	 * @return
	 */
	String[] getAllColNames();
	
	/**
	 * ɾ������
	 * @param indexName
	 * @return
	 * @throws IOException
	 */
	boolean deleteIndex(String indexName) throws IOException;

	/**
	 * �½�����
	 * @param I ��������
	 * @param fields �ֶ�����
	 * @param obj ��KV����ʱ��ʾֵ�ֶ����ƣ���hash����ʱ��ʾhash�ܶ�
	 * @param opt ����'a'ʱ��ʾ׷��, ����'r'ʱ��ʾ�ؽ�����
	 * @param w ����ʱ�Ĺ�������
	 * @param ctx ������
	 */
	void createIndex(String I, String []fields, Object obj, String opt, Expression w, Context ctx);
	
	/**
	 * ȡ�ֲ����ʽ��
	 * @return
	 */
	String getDistribute();
	
	/**
	 * ���һ��
	 * @param colName ����
	 * @param exp ��ֵ���ʽ
	 * @param ctx 
	 */
	void addColumn(String colName, Expression exp, Context ctx);
	
	/**
	 * ɾ��һ��
	 * @param colName ����
	 */
	void deleteColumn(String colName);
}