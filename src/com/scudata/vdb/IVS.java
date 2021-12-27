package com.scudata.vdb;

import java.sql.Timestamp;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;

/**
 * ���ݿ����ӽӿ�
 * @author RunQian
 *
 */
public interface IVS {
	/**
	 * ȡ���Ӷ�Ӧ�ĸ�����
	 * @return VDB
	 */
	VDB getVDB();
	
	/**
	 * ȡ���ӵĵ�ǰ��
	 * @return ISection
	 */
	ISection getHome();
	
	// 
	/**
	 * ���õ�ǰ·����������д����������ڴ�·��
	 * @param path
	 * @return IVS
	 */
	IVS home(Object path);
	
	/**
	 * ���ص�ǰ·��
	 * @param opt
	 * @return Object
	 */
	Object path(String opt);

	/**
	 * ��ס��ǰ·��
	 * @param opt ѡ�r������·���򶼼�д����u������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int lock(String opt);
	
	/**
	 * ��סָ��·��
	 * @param path ·����·������
	 * @param opt ѡ�r������·���򶼼�д����u������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int lock(Object path, String opt);
	
	/**
	 * �г���ǰ·���µ����ļ������س�����
	 * @param opt ѡ�d���г���Ŀ¼��w���������ļ���Ŀ¼ȫ���г���l���ȼ���
	 * @return Sequence
	 */
	Sequence list(String opt);
	
	/**
	 * �г�ָ��·���µ����ļ������س�����
	 * @param path ·����·������
	 * @param opt ѡ�d���г���Ŀ¼��w���������ļ���Ŀ¼ȫ���г���l���ȼ���
	 * @return Sequence
	 */
	Sequence list(Object path, String opt);
	
	/**
	 * ����ǰ��������
	 * @param opt ѡ�l���ȼ���
	 * @return Object
	 */
	Object load(String opt);
	
	/**
	 * ��ָ��·���ı�������
	 * @param path ·����·������
	 * @param opt ѡ�l���ȼ���
	 * @return Object
	 */
	Object load(Object path, String opt);
	
	/**
	 * ����·���������ύʱ��
	 * @return Timestamp
	 */
	Timestamp date();

	/**
	 * ������д�뵽��ǰ��
	 * @param value ����
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int save(Object value);
	
	/**
	 * ������д�뵽ָ��·���ı�
	 * @param value ����
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int save(Object value, Object path, Object name);

	/**
	 * ������ǰ·�������а���ָ���ֶεı�������
	 * @param fields �ֶ�������
	 * @return ���
	 */
	Table importTable(String []fields);
	
	 /**
	  *  ������ǰ·�������а���ָ���ֶεı�������
	  * @param fields �ֶ�������
	  * @param filters ���˱��ʽ����
	  * @param ctx ����������
	  * @return ���
	  */
	Table importTable(String []fields, Expression []filters, Context ctx);
	
	/**
	 * ���ѡ��Ϊ����ɾ���ڵ㣬���ѡ��Ϊ��e����ɾ�����µĿ��ӽڵ�
	 * @param opt e��ֻɾ�����µĿսڵ�
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int delete(String opt); // ɾ����
	
	/**
	 * ���ѡ��Ϊ����ɾ���ڵ㣬���ѡ��Ϊ��e����ɾ�����µĿ��ӽڵ�
	 * @param path ·����·������
	 * @param opt e��ֻɾ�����µĿսڵ�
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int delete(Object path, String opt); // ɾ����
	
	/**
	 * ɾ�����
	 * @param paths
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int deleteAll(Sequence paths);
	
	/**
	 * �ƶ�Ŀ¼��ָ��Ŀ¼
	 * @param srcPath Դ·����·������
	 * @param destPath Ŀ��·����·������
	 * @param name Ŀ��·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int move(Object srcPath, Object destPath, Object name);
	
	/**
	 * ����Ŀ¼
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int makeDir(Object path, Object name);
	
	/**
	 * �����ݿ��ж�ȡ�����ֶ�׷�ӵ�ָ��������
	 * @param seq ����
	 * @param pathExp ·�����ʽ
	 * @param fields Ҫ��ȡ���ֶ�������
	 * @param filter ���˱��ʽ
	 * @param ctx ����������
	 * @return ��������
	 */
	Table read(Sequence seq, Expression pathExp, String []fields, Expression filter, Context ctx);
	
	/**
	 * �����е�ָ���ֶ�д�뵽��
	 * @param seq ����
	 * @param pathExp ·�����ʽ
	 * @param fieldExps �ֶ�ֵ���ʽ����
	 * @param fields �ֶ�������
	 * @param filter ���˱��ʽ
	 * @param ctx ����������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int write(Sequence seq, Expression pathExp, Expression []fieldExps, 
			String []fields, Expression filter, Context ctx);

	/**
	 * ����������������
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ���飬���ڹ���
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fields ������Ҫ�����ֶ�������
	 * @param filter ��������
	 * @param opt ѡ�r����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return ���������
	 */
	Sequence retrieve(String []dirNames, Object []dirValues, boolean []valueSigns,
			String []fields, Expression filter, String opt, Context ctx);
	
	/**
	 * �ҳ����������ĵ��ݺ��д���ݵ��ֶ�ֵ
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ���飬���ڹ���
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fvals �����е��ֶ�ֵ����
	 * @param fields �����е��ֶ�������
	 * @param filter ��������
	 * @param opt ѡ�r����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int update(String []dirNames, Object []dirValues, boolean []valueSigns, 
			Object []fvals, String []fields, Expression filter, String opt, Context ctx);
	
	/**
	 * ���渽����ͨ����ͼƬ
	 * @param oldValues ��һ�ε��ôκ����ķ���ֵ
	 * @param newValues �޸ĺ��ֵ
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return ֵ���У�������һ�ε��ô˺���
	 */
	Sequence saveBlob(Sequence oldValues, Sequence newValues, Object path, String name);
	
	/**
	 * ��������·����
	 * @param ·����·������
	 * @param ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int rename(Object path, String name);
	
	/**
	 * �鵵ָ��·�����鵵��·��������д��ռ�õĿռ���С����ѯ�ٶȻ���
	 * @param path ·����·������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int archive(Object path);
	
	/**
	 * ����·����ָ��·����
	 * @param destPath Ŀ��·����·������
	 * @param destName Ŀ��·������·��������
	 * @param src Դ���ݿ�����
	 * @param srcPath Դ·����·������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	int copy(Object destPath, Object destName, IVS src, Object srcPath);
}
