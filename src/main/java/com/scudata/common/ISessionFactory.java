package com.scudata.common;

public interface ISessionFactory {
	/**
	 * ȡ����Դ����
	 */
	public DBSession getSession() throws Exception;

}