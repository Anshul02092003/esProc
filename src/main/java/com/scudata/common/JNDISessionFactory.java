package com.scudata.common;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
public class JNDISessionFactory implements ISessionFactory
{
	private JNDIConfig cfg;
	private DataSource ds;

	/**
	 * ��ʼ��
	 * @param cfg	jndi����
	 * @throws Exception
	 */
	public JNDISessionFactory( JNDIConfig cfg ) throws Exception {
		this.cfg = cfg;
		javax.naming.Context ctx = new InitialContext();
		ds = (DataSource) ctx.lookup( cfg.getJNDI() );
		if ( ds == null ) 
			throw new Exception( "not found JNDI: " + cfg.getJNDI() );
	}

	/**
	 * ��ȡ����Session
	 */
	public DBSession getSession() throws Exception {
		// edited by bd, 2017.5.31��Connectionȫ����Ϊ���Զ��ύ��
		Connection con = ds.getConnection();
		try{ con.setAutoCommit(false); }catch( Throwable t ) {}
		return new DBSession(con, cfg);
		//return new DBSession( ds.getConnection(), cfg );
	}
}