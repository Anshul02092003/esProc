package com.scudata.common;

import java.io.*;
public class DBInfo implements Cloneable, Externalizable {
  protected int dbType = DBTypes.UNKNOWN;
  protected String name, dbCharset, clientCharset;
  protected boolean needTranContent = false, needTranSentence = false;
  protected String df, tf, dtf;
  private boolean isPublic = true;
  private int batchSize = 1000;

  private static final long serialVersionUID = 10001110L;

  /**
   * ���캯��
   */
  public DBInfo() {
  }

  /**
   * ���캯��
   *@param dbType ����Դ���ͣ��μ�DBTypes
   */
  public DBInfo(int dbType) {
	this.dbType = dbType;
  }

  public DBInfo(DBInfo other) {
	  this.dbType = other.dbType;
	  this.name = other.name;
	  this.dbCharset = other.dbCharset;
	  this.clientCharset = other.clientCharset;
	  this.df = other.df;
	  this.tf = other.tf;
	  this.dtf = other.dtf;
	  this.isPublic = other.isPublic;
	  this.batchSize = other.batchSize;
	  this.needTranContent = other.needTranContent;
	  this.needTranSentence = other.needTranSentence;
  }

  /**
   * �������ݿ����Ȩ��
   * @param isPublic boolean true��ӵ�ж����ݿ��ȫ��Ȩ�ޣ�false��ֻ�������ɼ�
   */
  public void setAccessPrivilege(boolean isPublic) {
	this.isPublic = isPublic;
  }

  /**
   * �������ݿ����Ȩ��
   * @return boolean true��ӵ�ж����ݿ��ȫ��Ȩ�ޣ�false��ֻ�������ɼ�
   */
  public boolean getAccessPrivilege() {
	return isPublic;
  }

  /**
   * ȡ����Դ���ͣ�ȡֵ��DBTypes
   */
  public int getDBType() {
	return this.dbType;
  }

  /**
   * ������Դ����
   *@param dbType ����Դ���ͣ��μ�DBTypes
   */
  public void setDBType(int dbType) {
	this.dbType = dbType;
  }

  /**
   * ������Դ����
   *@param name ����Դ����
   */
  public void setName(String name) {
	this.name = name;
  }

  /**
   * ȡ����Դ����
   */
  public String getName() {
	return name;
  }

  /**
   * ȡ����Դʹ�õ��ַ�����
   */
  public String getDBCharset() {
	return this.dbCharset;
  }

  /**
   * ������Դʹ�õ��ַ�����
   *@param charset1 �ַ�����
   */
  public void setDBCharset(String dbCharset) {
	this.dbCharset = dbCharset;
  }

  /**
   * ȡ�ͻ���ʹ�õ��ַ�����
   */
  public String getClientCharset() {
	return this.clientCharset;
  }

  /**
   * ��ͻ���ʹ�õ��ַ�����
   *@param charset2 �ַ�����
   */
  public void setClientCharset(String clientCharset) {
	this.clientCharset = clientCharset;
  }

  /**
   * ȡ�Ƿ���Ҫת���������ݵı���
   */
  public boolean getNeedTranContent() {
	return this.needTranContent;
  }

  /**
   * ���Ƿ���Ҫת���������ݵı���
   */
  public void setNeedTranContent(boolean needTranContent) {
	this.needTranContent = needTranContent;
  }

  /**
   * ȡ�Ƿ���Ҫת���������ı���
   */
  public boolean getNeedTranSentence() {
	return this.needTranSentence;
  }

  /**
   * ���Ƿ���Ҫת���������ı���
   */
  public void setNeedTranSentence(boolean needTranSentence) {
	this.needTranSentence = needTranSentence;
  }

  /**
   * ȡ����Դ�����ڸ�ʽ
   */
  public String getDateFormat() {
	return this.df;
  }

  /**
   * ������Դ�����ڸ�ʽ���˺�����ҪΪƴSQL���ʹ��
   * @param df ���ڸ�ʽ
   */
  public void setDateFormat(String df) {
	this.df = df;
  }

  /**
   * ȡ����Դ��ʱ���ʽ
   */
  public String getTimeFormat() {
	return this.tf;
  }

  /**
   * ������Դ��ʱ���ʽ���˺�����ҪΪƴSQL���ʹ��
   * @param tf ʱ���ʽ
   */
  public void setTimeFormat(String tf) {
	this.tf = tf;
  }

  /**
   * ȡ����Դ������ʱ���ʽ
   */
  public String getDatetimeFormat() {
	return this.dtf;
  }

  /**
   * ������Դ������ʱ���ʽ���˺�����ҪΪƴSQL���ʹ��
   * @param dtf ����ʱ���ʽ
   */
  public void setDatetimeFormat(String dtf) {
	this.dtf = dtf;
  }

  /**
   * ��Batch Size
   *@param size Batch Size
   */
  public void setBatchSize(int size) {
	this.batchSize = size;
  }

  /**
   * ȡBatch Size
   */
  public int getBatchSize() {
	return this.batchSize;
  }

  /**
   * ��������Դ���ӹ���
   * ������ֱ�����쳣����Ҫ��������
   */
  public ISessionFactory createSessionFactory() throws Exception {
	throw new RuntimeException("not implemented");
  }

  /** �汾�ţ�ȡֵbyte */
  private static byte version = (byte)2; // 2009.9.14�����޸� �����editValue

  /*************************���¼̳���Externalizable************************/
  /**
   * д���ݵ���
   *@param out �����
   */
  public void writeExternal(ObjectOutput out) throws IOException {
	out.writeByte(version);
	out.writeInt(dbType);
	out.writeObject(name);
	out.writeObject(dbCharset);
	out.writeObject(clientCharset);
	out.writeObject(df);
	out.writeObject(tf);
	out.writeObject(dtf);
	out.writeBoolean(isPublic);
	out.writeInt(batchSize);

	// �汾2
	out.writeBoolean(needTranContent);
	out.writeBoolean(needTranSentence);
  }

  /**
   * �����ж�����
   *@param in ������
   */
  public void readExternal(ObjectInput in) throws IOException,
	  ClassNotFoundException {
	byte ver = in.readByte();
	dbType = in.readInt();
	name = (String) in.readObject();
	dbCharset = (String) in.readObject();
	clientCharset = (String) in.readObject();
	df = (String) in.readObject();
	tf = (String) in.readObject();
	dtf = (String) in.readObject();
	isPublic = in.readBoolean();
	batchSize = in.readInt();

	if (ver > 1) {
	  needTranContent = in.readBoolean();
	  needTranSentence = in.readBoolean();
	}
  }
}



