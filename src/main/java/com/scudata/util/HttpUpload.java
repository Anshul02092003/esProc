package com.scudata.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.scudata.common.RQException;

//��������˵�url�����ļ�
public class HttpUpload {
	
	//�����ļ���url
	private String url;
	
	//����ʱҪ���͵Ĳ���
	private Hashtable<String,String> params;
	
	//���ص��ļ�������
	private ArrayList<String> fileArgs;
	
	//���ص��ļ�·��
	private ArrayList<String> filePaths;
	
	//���ص��ļ��ֽ�
	private ArrayList<byte[]> fileBytes;
	
	//���غ󷵻ؽ�����ݵı���
	private String resultEncoding = "UTF-8";  
	
	public HttpUpload( String url ) {
		this.url = url;
		params = new Hashtable<String,String>();
		fileArgs = new ArrayList<String>();
		filePaths = new ArrayList<String>();
		fileBytes = new ArrayList<byte[]>();
	}
	
	/**
	 * ���÷������ݵı���
	 * @param encoding
	 */
	public void setResultEncoding( String encoding ) {
		this.resultEncoding = encoding;
	}
	
	/**
	 * ���һ������
	 * @param paramName
	 * @param paramValue
	 */
	public void addParam( String paramName, String paramValue ) {
		params.put( paramName, paramValue );
	}
	
	/**
	 * ���һ�������ļ�
	 * @param fileArg    �ļ�������
	 * @param filePath   �ļ�·��
	 */
	public void addFile( String fileArg, String filePath ) {
		fileArgs.add( fileArg );
		filePaths.add( filePath );
		fileBytes.add( null );
	}
	
	/**
	 * ���һ�������ļ�
	 * @param fileArg   �ļ�������
	 * @param b         �ļ��ֽ�
	 */
	public void addFile( String fileArg, byte[] b ) {
		fileArgs.add( fileArg );
		filePaths.add( "noname" );
		fileBytes.add( b );
	}
	
	/**
	 * ʵ���ļ�����
	 * @return    �������ؽ��
	 * @throws Throwable
	 */
	public String upload() {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost;
		String result = null;
		try {
			httpClient = HttpClientBuilder.create().build();
			httpPost = new HttpPost( url );
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setCharset( Charset.forName( "UTF-8" ) );//��������ı����ʽ
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//�������������ģʽ
			for( int i = 0; i < fileArgs.size(); i++ ) {
				byte[] b = fileBytes.get( i );
				if( b == null ) {
					builder.addBinaryBody( fileArgs.get( i ), new File( filePaths.get( i ) ) );
				}
				else {
					builder.addBinaryBody( fileArgs.get( i ), b, ContentType.create( HTTP.CONTENT_TYPE ), "noname" );
				}
			}
			Enumeration<String> em = params.keys();
			while( em.hasMoreElements() ) {
				String arg = em.nextElement();
				builder.addPart( arg, new StringBody( params.get( arg ), ContentType.create( HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8) ) );//�����������
			}
			HttpEntity entity = builder.build();// ���� HTTP POST ʵ��  	
			httpPost.setEntity( entity ); //��������ʵ��
			HttpResponse response = httpClient.execute(httpPost);
			if( null != response && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				if( null != resEntity ) {
					result = EntityUtils.toString( resEntity, resultEncoding );
				}
			}
		}
		catch( Throwable t ) {
			throw new RQException( t );
		}
		finally {
			try{ httpClient.close(); }catch( Exception e ){}
		}
		return result;
	}
	
}
