package com.scudata.common;

import java.io.*;
public class JNDIConfig extends DBInfo implements Cloneable, Serializable
{
	private String jndi;

	/**
	 * ��ʼ��
	 * @param dbType	���ݿ�����
	 */
	public JNDIConfig( int dbType ) {
		super(dbType);
	}

	/**
	 * ��ʼ��
	 * @param dbType	���ݿ�����
	 * @param jndi	jndi��
	 */
	public JNDIConfig( int dbType, String jndi ) {
		super(dbType);
		this.jndi = jndi;
	}

	/**
	 * ����jndi
	 * @param jndi
	 */
	public void setJNDI( String jndi ) {
		this.jndi = jndi;
	}

	/**
	 * ��ȡjndi
	 * @return
	 */
	public String getJNDI() {
		return this.jndi;
	}

	/**
	 * �������ӹ���
	 */
	public ISessionFactory createSessionFactory() throws Exception {
		return new JNDISessionFactory( this );
	}

}
