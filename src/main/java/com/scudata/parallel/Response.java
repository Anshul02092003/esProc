package com.scudata.parallel;

import java.io.Serializable;

import com.scudata.common.RQException;

/**
 * ������Ӧ
 * 
 * @author Joancy
 *
 */
public class Response implements Serializable {
	private static final long serialVersionUID = -5641784958339382118L;
	
	private Exception exception = null;//һ���Եļ����쳣
	private Error error = null;//������ȴ����ڴ������
	private Object result = null;
	
	transient String fromHost = null;
	/**
	 * ����ȱʡ��Ӧ
	 */
	public Response() {
	}
	
	/**
	 * ������Ӧ����
	 * @param result ����ֵ
	 */
	public Response(Object result) {
		this.result = result;
	}
	
	/**
	 * ������Ӧ��ip��Դ
	 * @param ip ip��ַ
	 */
	public void setFromHost(String ip){
		this.fromHost = ip;
	}
	
	/**
	 * ȡ��Ӧ�е��쳣
	 * @return ���쳣ʱ�����쳣�����򷵻�null 
	 */
	public Exception getException() {
		return exception;
	} 
	
	/**
	 * ������Ӧ���쳣
	 * @param e �쳣����
	 */
	public void setException(Exception e) {
		this.exception = e;
	}
	
	/**
	 * ������Ӧ�Ĵ���
	 * @param e �������
	 */
	public void setError(Error e){
		this.error = e;
	}
	/**
	 * ȡ��Ӧ�Ĵ���
	 * @return �������
	 */
	public Error getError(){
		return error;
	}
	

	/**
	 * ȡ��Ӧ���
	 * @return ���ֵ
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * ������Ӧ�Ľ��ֵ
	 * @param res ���ֵ
	 */
	public void setResult(Object res) {
		this.result = res;
	}
	
	/**
	 * ���ݽ���Լ��쳣��Ϣ�����з�����Ӧ���
	 * @return ��Ӧ����Ӧֵ
	 */
	public Object checkResult() {
		if (result != null) {
			return result;
		} else if (exception != null) {
			throw new RQException("["+fromHost+"]"+exception.getMessage(), exception);
		} else if (error != null) {
			throw new RQException("["+fromHost+"]"+error.getMessage(), error);
		} else {
			return null; // �����null
		}
	}
}
