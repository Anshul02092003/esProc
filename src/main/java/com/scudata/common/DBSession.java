package com.scudata.common;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * ����Դ����
 */
public class DBSession {
	private Object session = null;
	private DBInfo info = null;
	//private boolean autoCommit = true; // deleted 2010/11/2 ��DataList��update��ͻ

	//added by bdl, 2010.8.30
	private SQLException error;
	private boolean registerCause = false;
	private HashMap<String,Savepoint> map = null;  //����ع���
	

	/**
	 * ���캯��
	 * @param dbType ����Դ
	 * @param session ����Դ������
	 */
	public DBSession( Object session, DBInfo info ) {
		this.session = session;
		this.info = info;
		if( session != null && info != null )
			detectDBType(session, info);
	}


	/**
	 * ������Դ������
	 */
	public Object getSession() {
		return this.session;
	}

	/**
	 * ȡ��ϵ���ݿ�����ݲֿ������
	 */
	public void setSession( Object session ) {
		this.session = session;
	}

	/**
	 * ȡ����Դ��Ϣ
	 */
	public DBInfo getInfo() {
		return this.info;
	}

	/**
	 * ������Դ��Ϣ
	 *@param info ����Դ��Ϣ
	 */
	public void setInfo( DBInfo info ) {
		this.info = info;
	}

	/**
	 * �ر�����
	 */
	public void close() {
		if ( session == null ) return;
		try {
			//����д����Ϊ�˲�ʹ��ESSBASEʱ������Ҫ����ESSBASE�İ�
			if ( info.getDBType() == DBTypes.ESSBASE ) {
				Method m = session.getClass().getMethod( "disconnect", new Class[]{} );
				m.invoke( session, new Object[]{} );
			} else {
				map = null;		//��ʱֱ�Ӷ�����δ����releaseSavepoint
				Method m = session.getClass().getMethod( "close", new Class[]{} );
				m.setAccessible(true);
				m.invoke( session, new Object[]{} );
			}
		} catch( Exception e ) {
		  throw new RQException(e);
		}
	}
	
	/**
	 * �������ݿ��򷵻��Ƿ��Զ��ύ�����򷵻�false
	 */
	public boolean getAutoCommit() {
		if(session instanceof Connection){
			try {
				return ((Connection)session).getAutoCommit();
			}catch(SQLException e){
			}
		}
		return true;
	}
	
	/**
	 * ��ȡ�����õ�ǰ���ݿ����ӵ���������Լ���
	 * @param option null�Ϳմ���ʾȡ��ǰ�����Լ��𣬵��ַ�ncurs��һ����ʾ��Ӧ�ļ���
	 * @return ԭ���𣬷����ݿ�ʱ����null
	 * @throws SQLException
	 */
	public String isolate(String option) throws SQLException {
		if( !(session instanceof Connection) ) return null;
		Connection conn = (Connection)session;
		//edited by bd, 2017.7.4, ��ݿƼ������ݿ�JDBC�����ƣ���û��getTransactionIsolation
		//�޸ģ�������ĵ���tryһ�£�siĬ��ֵΪ��n��
		//String si = null;
		String si = "n";
		try {
			int i = conn.getTransactionIsolation();
			switch(i) {
				case Connection.TRANSACTION_NONE: si="n"; break;
				case Connection.TRANSACTION_READ_COMMITTED: si="c"; break;
				case Connection.TRANSACTION_READ_UNCOMMITTED: si="u"; break;
				case Connection.TRANSACTION_REPEATABLE_READ: si="r"; break;
				case Connection.TRANSACTION_SERIALIZABLE: si="s"; break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if(option==null) return si;
		//edited by bd, 2017.7.4, ��ݿƼ������ݿ�JDBC�����ƣ���û��getTransactionIsolation
		//���Ƶģ��������setTransactionIsolation����Ҳtryһ��
		try {
			if(option.indexOf('n')>=0) conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
			else if(option.indexOf('c')>=0) conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			else if(option.indexOf('u')>=0) conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			else if(option.indexOf('r')>=0) conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			else if(option.indexOf('s')>=0) conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return si;
	}
	
	/**
	 * ���ûع���
	 * @param name���ع������ƣ�������Ϊ��
	 * @return �����ݿⷵ��false�����ݿⷵ��true
	 */
	public boolean savepoint(String name) throws SQLException {
		if( !(session instanceof Connection) ) return false;
		Connection conn = (Connection)session;
		Savepoint sp = conn.setSavepoint(name);
		if(map==null) map = new HashMap<String,Savepoint>();
		map.put(name, sp);
		return true;
	}
	
	/** �ع���ָ�����ƵĻع���
	 * @param name���ع������ƣ�Ϊnullʱ��ʾ�ع������޸�
	 * @return �����ݿ����ָ�����ƵĻع���ʱ����false�����ݿⷵ��true
	 */
	public boolean rollback(String name) throws SQLException {
		if( !(session instanceof Connection) ) return false;
		Connection conn = (Connection)session;
		if(name==null) {
			conn.rollback();
			return true;
		}
		if(map==null) return false;
		Savepoint sp = map.get(name);
		if(sp==null) return false;
		conn.rollback(sp);
		return true;
	}

	/**
	 * ����Ƿ�ر�
	 */
	public boolean isClosed() {
		if ( session == null ) return true;
		try {
			//����д����Ϊ�˲�ʹ��ESSBASEʱ������Ҫ����ESSBASE�İ�
			if ( info.getDBType() == DBTypes.ESSBASE ) {
				Method m = session.getClass().getDeclaredMethod( "isConnected", new Class[]{} );
				Object o = m.invoke( session, new Object[]{} );
				return ((Boolean)o).booleanValue();
			} else {
				Method m = session.getClass().getDeclaredMethod( "isClosed", new Class[]{} );
				m.setAccessible( true );
				Object o = m.invoke( session, new Object[]{} );
				return ((Boolean)o).booleanValue();
			}
		}
		catch (java.lang.NoSuchMethodException noMethodE ) {
		  //added by bdl, 2009.12.3�������ݿ�������û�С�isClosed������ʱ����Ϊ����Դδ�ر�
		  return false;
		}
		catch( Exception e ) {
		  e.printStackTrace();
		}
		return true;
	}

	public String getField(String field) {
		if (getInfo() instanceof DBConfig) {
			DBConfig dbc = (DBConfig)getInfo();
			if (dbc.isAddTilde()) {
				int dbType = dbc.getDBType();
				return DBTypes.getLeftTilde(dbType) + field + DBTypes.getRightTilde(dbType);
			}
		}

		return field;
	}

		/**
		 * �趨������Ϣ
		 * @return SQLException
		 */
		public void setError(SQLException error) {
		  this.error = error;
		}

		/**
		 * ��ȡ������Ϣ
		 * @return SQLException
		 */
		public SQLException error() {
		  return this.error;
		}

		/**
		 * �Ƿ��¼�����쳣
		 * @param registerCause boolean
		 */
		public void setErrorMode(boolean registerCause) {
		  this.registerCause = registerCause;
		}

		/**
		 * ��ȡ�Ƿ��¼�����쳣
		 * @return boolean
		 */
		public boolean getErrorMode() {
		  return this.registerCause;
		}

		private void detectDBType(Object session, DBInfo info) {
			if (info.getDBType()!=DBTypes.UNKNOWN) return;

			String className = session.getClass().getName();
			int t = getType(className);
			if( t!=DBTypes.UNKNOWN ) {
				info.setDBType(t);
				return;
			}

			if(session instanceof Connection) {
				String product = null;
				try{
					DatabaseMetaData dmd = ((Connection)session).getMetaData();
					product = dmd.getDatabaseProductName();
				}catch(Throwable e){
				}
				if(product==null)
					return;
				t = getType(product);
				info.setDBType(t);

			}
		}

		private int getType(String name) {
			name = name.toLowerCase();
			if( name.indexOf("oracle")>=0 )
				return DBTypes.ORACLE;
			if( name.indexOf("sqlserver")>=0 )
				return DBTypes.SQLSVR;
			if( name.indexOf("db2")>=0 )
				return DBTypes.DB2;
			if( name.indexOf("mysql")>=0 )
				return DBTypes.MYSQL;
			if( name.indexOf("informix")>=0 )
				return DBTypes.INFMIX;
			if( name.indexOf("derby")>=0 )
				return DBTypes.DERBY;
			if( name.indexOf("essbase")>=0 )
				return DBTypes.ESSBASE;
			if( name.indexOf("access")>=0 )
				return DBTypes.ACCESS;
			if( name.indexOf("anywhere")>=0 )
				return DBTypes.SQLANY;

			return DBTypes.UNKNOWN;
		}

		protected void finalize() throws Throwable {
			close();
		}
}
