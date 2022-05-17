package com.scudata.common;

import java.io.Serializable;

/**
 * Spring����Դ����
 *
 */
public class SpringDBConfig extends DBInfo implements Cloneable, Serializable {

	private String id; // datasource��id

	/**
	 * ��ʼ��
	 * @param dbType	���ݿ�����
	 */
	public SpringDBConfig(int dbType) {
		super(dbType);
	}

	/**
	 * ��ʼ��
	 * @param dbType	���ݿ�����
	 * @param id	Spring��datasource��id
	 */
	public SpringDBConfig(int dbType, String id) {
		super(dbType);
		this.id = id;
	}

	/**
	 * ȡID
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * ����ID
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * �������ӹ���
	 */
	public void createDBSessionFactory() throws Exception {
		SpringDBSessionFactory.create(id, dbType);
	}
}
