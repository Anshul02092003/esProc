package com.scudata.dm;

import java.sql.Connection;

import com.scudata.common.DBSession;
import com.scudata.common.ISessionFactory;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.DatabaseUtil;

/**
 * ���ݿ⺯���������
 */
public class DBObject implements IResource {
	private DBSession dbSession; // ���ݿ�����
	private Context ctx; // ������
	private boolean canClose; // �����Ƿ���Ա��ر�
	private boolean isLower = false; // �ֶ����Ƿ�תСд

	/**
	 * �������ݿ����
	 * @param dbSession DBSession ���ݿ�����
	 */
	public DBObject(DBSession dbSession) {
		this.dbSession = dbSession;
	}

	/**
	 * ����һ���ݿ�����,ʹ�������Ҫ����close�ر�
	 * @param dbsf ISessionFactory
	 * @param opt String e�������쳣����ʱ��¼�쳣��Ϣ�������׳��쳣
	 * @throws Exception
	 */
	public DBObject(ISessionFactory dbsf, String opt, Context ctx) throws Exception {
		dbSession = dbsf.getSession();
		this.canClose = true;
		this.ctx = ctx;
		if (ctx != null) ctx.addResource(this);
		
		if (opt != null) {
			if (opt.indexOf('e') != -1) dbSession.setErrorMode(true);
			if (opt.indexOf('l') != -1) isLower = true;
			dbSession.isolate(opt);
		}
	}

	// ֻ���ù���������DBObject���ܵ���close
	public boolean canClose() {
		return canClose;
	}
	
	/**
	 * �ر���connect����������
	 */
	public void close() {
		if (!canClose) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dbCloseError"));
		}
		
		DBSession dbSession = getDbSession();
		if (!dbSession.isClosed()) {
			if (ctx != null) ctx.removeResource(this);
			if (!dbSession.getAutoCommit()) {
				// �е����ݿ����rollbackʱ���׳��쳣
				try {
					rollback(null); // �����˺���close���׳��쳣
				} catch (Exception e) {
				}
			}
			
			dbSession.close();
		}
	}
	
	/**
	 * ��������Ĵ�����Ϣ�����������
	 * @param opt String m�����ش����ַ�����Ĭ�Ϸ���errorCode
	 * @return Object
	 */
	public Object error(String opt) {
		DBSession session = getDbSession();
		java.sql.SQLException e = session.error();
		session.setError(null); // ȡ��������������

		if (e == null) {
			if (opt == null || opt.indexOf('m') == -1) {
				return new Integer(0);
			} else {
				return null;
			}
		} else {
			if (opt == null || opt.indexOf('m') == -1) {
				return new Integer(e.getErrorCode());
			} else {
				String str = e.getMessage();
				if (str == null) {
					str = "SQLException error code��" + e.getErrorCode();
				}

				return str;
			}
		}
	}

	/**
	 * �����ύ�����ݿ�
	 */
	public void commit() {
		try {
			Connection con = (Connection)dbSession.getSession();
			con.commit();
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ȡ���ϴ��ύ�������ĸ���
	 * @param name String �ջع�����
	 */
	public boolean rollback(String name) {
		try {
			return dbSession.rollback(name);
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	public String isolate(String opt) {
		try {
			return dbSession.isolate(opt);
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	// �ֶ����Ƿ�ʹ��Сд
	public boolean isLower() {
		return isLower;
	}
	
	public boolean savepoint(String name) {
		try {
			return dbSession.savepoint(name);
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	/**
	 * �������ݿ����ӣ�ʹ��������releaseDBSession�ͷ�
	 * @return DBSession
	 */
	public DBSession getDbSession() {
		return dbSession;
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof DBObject)) return false;

		DBObject other = (DBObject)obj;
		return dbSession == other.dbSession;
	}

	/**
	 * ִ�в�ѯ���
	 * @param sql String ��ѯ���
	 * @param params Object[] ����ֵ
	 * @param types byte[] ��������
	 * @param opt String i�������ֻ��1��ʱ���س�����
	 * @param ctx Context
	 * @return Sequence
	 */
	public Sequence query(String sql, Object []params, byte []types, String opt, Context ctx) {
		if (isLower) {
			if (opt == null) {
				opt = "l";
			} else {
				opt += "l";
			}
		}
		
		Sequence result = DatabaseUtil.query(sql,params,types,opt,ctx,getDbSession());
		
		if (opt != null && opt.indexOf('x') != -1 && canClose()) {
			close();
		}
		
		return result;
	}
	
	/**
	 * ������е�ÿһ��Ԫ��ִ�в�ѯ��䣬���ؽ�����ϲ������
	 * @param srcSeries Sequence Դ����
	 * @param sql String ��ѯ���
	 * @param params Expression[] �������ʽ
	 * @param types byte[] ��������
	 * @param opt String
	 * @param ctx Context
	 * @return Sequence
	 */
	public Sequence query(Sequence srcSeries, String sql, Expression[] params,
			   byte[] types, String opt, Context ctx) {
		if (isLower) {
			if (opt == null) {
				opt = "l";
			} else {
				opt += "l";
			}
		}
		
		Sequence result = DatabaseUtil.query(srcSeries,sql,params,types,opt,ctx,getDbSession());
		
		if (opt != null && opt.indexOf('x') != -1 && canClose()) {
			close();
		}
		
		return result;
	}


	/**
	 * ִ�в�ѯ��䣬�������������ĵ�һ����¼���ֶλ���ֶι��ɵ����С�
	 * @param sql String ��ѯ���
	 * @param params Object[] ����
	 * @param types byte[] ��������
	 * @param opt String
	 * @return Object
	 */
	public Object query1(String sql, Object []params, byte []types, String opt) {
		if (isLower) {
			if (opt == null) {
				opt = "l";
			} else {
				opt += "l";
			}
		}

		DBSession dbs = getDbSession();
		Sequence sequence = DatabaseUtil.query(sql, params, types, dbs, opt);
		
		if (opt != null && opt.indexOf('x') != -1 && canClose()) {
			close();
		}

		if (sequence == null || sequence.length() == 0) return null;

		Object obj = sequence.get(1);
		if (obj instanceof Record) {
			Record r = (Record)obj;
			Object []vals = r.getFieldValues();
			if (vals.length == 1) {
				return vals[0];
			} else {
				return new Sequence(vals);
			}
		} else {
			return obj;
		}
	}

	/**
	 * ִ�д洢���̷��ؽ�����У�������ض�����ݼ����򷵻����е�����
	 * @param sql String sql���
	 * @param params Object[] ����ֵ
	 * @param types byte[] ��������
	 * @param modes byte[] �������ģʽ
	 * @param outParams String[] ���������������������
	 * @param ctx Context
	 * @return Sequence
	 */
	public Sequence proc(String sql, Object[] params, byte[] types, byte[] modes,
					   String[] outParams, Context ctx) {
		DBSession dbs = getDbSession();
		Sequence series = DatabaseUtil.proc(sql, params, modes, types, outParams, dbs, ctx);
		return series == null ? new Sequence(0) : series;
	}

	/**
	 * ������ݿ�ִ��sql���
	 * @param sql String
	 * @param params Object[] ����ֵ
	 * @param types byte[] ��������
	 * @param opt String k: ���ύ����ȱʡ���ύ
	 * @return Object
	 */
	public Object execute(String sql, Object []params, byte []types, String opt) {
		if (isLower) {
			if (opt == null) {
				opt = "l";
			} else {
				opt += "l";
			}
		}

		DBSession dbs = getDbSession();
		Object ret = DatabaseUtil.execute(sql, params, types, dbs, opt);
		if (opt == null || opt.indexOf('k') == -1) commit();
		return ret;
	}

	/**
	 * ������е�ÿһ��Ԫ��ִ��sql���
	 * @param srcSeries Sequence Դ����
	 * @param sql String
	 * @param params Expression[] �������ʽ
	 * @param types byte[] ��������
	 * @param opt String k: ���ύ����ȱʡ���ύ
	 * @param ctx Context
	 */
	public void execute(Sequence srcSeries, String sql, Expression[] params,
			byte[] types, String opt, Context ctx) {
		DBSession dbs = getDbSession();
		DatabaseUtil.execute(srcSeries,sql,params,types,ctx,dbs);//opt,
		if (opt == null || opt.indexOf('k') == -1) commit();
	}

	public void execute(ICursor cursor, String sql, Expression[] params,
			byte[] types, String opt, Context ctx) {
		DBSession dbs = getDbSession();
		DatabaseUtil.execute(cursor,sql,params,types,ctx,dbs);//opt,
		if (opt == null || opt.indexOf('k') == -1) commit();
	}

	/**
	 * ����srcSeries���±�table�е��ֶ�fields
	 * @param srcSeries Sequence Դ����
	 * @param table String ����
	 * @param fields String[] �ֶ���
	 * @param fopts String[] p���ֶ���������a���ֶ��������ֶ�
	 * @param exps Expression[] ֵ���ʽ
	 * @param opt String
	 * @param ctx Context
	 * @return int ���¼�¼��
	 */
	public int update(Sequence srcSeries, String table, String[] fields,
					   String[] fopts, Expression[] exps, String opt, Context ctx) {
		if (srcSeries == null || srcSeries.length() == 0) return 0;

		DBSession dbs = getDbSession();
		int count = DatabaseUtil.update(srcSeries, table, fields, fopts, exps, opt, dbs, ctx);

		if (opt == null || opt.indexOf('k') == -1) {
			commit();
		}

		return count;
	}

	public int update(ICursor cursor, String table, String[] fields,
					   String[] fopts, Expression[] exps, String opt, Context ctx) {
		DBSession dbs = getDbSession();
		int count = DatabaseUtil.update(cursor, table, fields, fopts, exps, opt, dbs, ctx);

		if (opt == null || opt.indexOf('k') == -1) {
			commit();
		}

		return count;
	}
	
	public int update(Sequence seq1, Sequence seq2, String table, String[] fields,
					   String[] fopts, Expression[] exps, String opt, Context ctx) {
		DBSession dbs = getDbSession();
		int count = DatabaseUtil.update(seq1, seq2, table, fields, fopts, exps, opt, dbs, ctx);

		if (opt == null || opt.indexOf('k') == -1) {
			commit();
		}

		return count;
	}
}
