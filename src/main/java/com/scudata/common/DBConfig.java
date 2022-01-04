package com.scudata.common;

import java.util.*;
import java.io.*;

public class DBConfig extends DBInfo implements Cloneable, Externalizable
{
  private String driver, url, user, password, extend;
  private boolean useSchema, caseSentence, isAddTilde = false;
  private Properties info;

  private static final long serialVersionUID = 10001101L;

  public DBConfig() {
  }

  /**
   * ��������Դ������
   *@param dbType ���ݿ����ͣ��μ�DBTypes
   */
  public DBConfig(int dbType) {
    super(dbType);
  }

  public DBConfig(DBConfig other) {
    super(other);
    this.driver = other.driver;
    this.url = other.url;
    this.user = other.user;
    this.password = other.password;
    this.extend = other.extend;
    this.useSchema = other.useSchema;
    this.caseSentence = other.caseSentence;
    this.isAddTilde = other.isAddTilde;
    if (other.info != null) {
      this.info = (Properties) other.info.clone();
    }
  }

  /**
   * �趨��������������
   * @param driver String ��������������·��
   */
  public void setDriver(String driver) {
    this.driver = driver;
  }

  /**
   * ��ȡ��������������
   */
  public String getDriver() {
    return driver;
  }

  /**
   * �趨��������·��
   * @param url String ����·��
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * ��ȡ��������·��
   */
  public String getUrl() {
    return url;
  }

  /**
   * �趨�û���
   * @param user String
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * ��ȡ�û���
   * @return String
   */
  public String getUser() {
    return user;
  }

  /**
   * �趨����
   * @param password String
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * ��ȡ����
   * @return String
   */
  public String getPassword() {
    return password;
  }

  /**
   * �趨�Ƿ�ʹ�ô�ģʽ�ı�
   * @param useSchema boolean
   */
  public void setUseSchema(boolean useSchema) {
    this.useSchema = useSchema;
  }

  /**
   * ��ȡ�Ƿ�ʹ�ô�ģʽ�ı�
   * @param info Properties
   */
  public void setInfo(Properties info) {
    this.info = info;
  }

  /**
   * ��ȡ����
   */
  public Properties getInfo() {
    return this.info;
  }

  /**
   * �趨sql�Ƿ��Сд����
   * @param bcase boolean
   */
  public void setCaseSentence(boolean bcase) {
    this.caseSentence = bcase;
  }

  /**
   * ��ȡsql�Ƿ��Сд����
   */
  public boolean isCaseSentence() {
    return caseSentence;
  }

  /**
   * ��ȡsql�Ƿ��Сд����
   */
  public boolean isUseSchema() {
    return useSchema;
  }

  /**
   * �趨�Ƿ�ʹ�ô��ֺ�
   * @return boolean
   */
  public boolean isAddTilde() {
    return isAddTilde;
  }

  /**
   * ��ȡ�Ƿ�ʹ�ô��ֺ�
   */
  public void setAddTilde(boolean b) {
    isAddTilde = b;
  }

  /**
   * �趨������չ����
   * @param extend String
   */
  public void setExtend(String extend) {
    this.extend = extend;
  }

  /**
   * ��ȡ������չ����
   * @return String
   */
  public String getExtend() {
    return extend;
  }

  public ISessionFactory createSessionFactory() throws Exception {
    return new DBSessionFactory(this);
  }

  /*************************���¼̳���Externalizable************************/
  /**
   * д���ݵ���
   *@param out �����
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeByte( (byte) 1);
    out.writeObject(driver);
    out.writeObject(url);
    out.writeObject(user);
    out.writeObject(password);
    out.writeObject(extend);
    out.writeBoolean(useSchema);
    out.writeBoolean(caseSentence);
    out.writeBoolean(isAddTilde);
    out.writeObject(info);
  }

  /**
   * �����ж�����
   *@param in ������
   */
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
    in.readByte(); // version
    driver = (String) in.readObject();
    url = (String) in.readObject();
    user = (String) in.readObject();
    password = (String) in.readObject();
    extend = (String) in.readObject();
    useSchema = in.readBoolean();
    caseSentence = in.readBoolean();
    isAddTilde = in.readBoolean();
    info = (Properties) in.readObject();
  }

}
