package com.scudata.parallel;

/**
 * ��ҵ�������쳣
 * 
 * @author Joancy
 *
 */
public class RedispatchableException extends Throwable{
  private static final long serialVersionUID = 1L;
  private Exception x;
  private Error e;
  
  /**
   * �����������쳣
   * @param o �쳣��Ϣ
   */
  public RedispatchableException( Object o ){
    if( o instanceof Exception ){
      this.x = (Exception)o;
    }else{
      this.e = (Error)o;
    }
  }

  private Throwable getThrowable(){
    if( x!=null ){
      return x;
    }else{
      return e;
    }
  }

  /**
   * �쳣��ԭ��
   */
  public Throwable getCause(){
      return getThrowable().getCause();
  }

  /**
   * ��ȡ����������Ϣ
   */
  public String getLocalizedMessage(){
      return getThrowable().getLocalizedMessage();
  }

  /**
   * ��ȡ�쳣��Ϣ
   */
  public String getMessage(){
    return getThrowable().getMessage();
  }

  /**
   * ȡ�쳣�Ķ�ջ
   */
  public StackTraceElement[] getStackTrace(){
    return getThrowable().getStackTrace();
  }
}
