package com.scudata.lib.webservice.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import com.scudata.common.Logger;

public class HttpRequest {
    /**
     * ��ָ��URL����GET����������
     * 
     * @param url
     *            ���������URL
     * @param param
     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return URL ������Զ����Դ����Ӧ���
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
        	if (param != null && param != "") param += "?" + param;
            String urlNameString = url + param;
            URL realUrl = new URL(urlNameString);
            // �򿪺�URL֮�������
            URLConnection connection = realUrl.openConnection();
            // ����ͨ�õ���������
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // ����ʵ�ʵ�����
            connection.connect();
//            // ��ȡ������Ӧͷ�ֶ�
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // �������е���Ӧͷ�ֶ�
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // ���� BufferedReader����������ȡURL����Ӧ
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("����GET��������쳣��" + e);
            Logger.error(e.getMessage());
        }
        // ʹ��finally�����ر�������
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * ��ָ�� URL ����POST����������
     * 
     * @param url
     *            ��������� URL
     * @param param
     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return ������Զ����Դ����Ӧ���
     */
    public static String sendPost(String url, String param) {
    	OutputStreamWriter osw = null;
        InputStream in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // �򿪺�URL֮�������
            URLConnection conn = realUrl.openConnection();
            // ����ͨ�õ���������
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "text/xml;charset:UTF-8");
            // ����POST�������������������
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // ��ȡURLConnection�����Ӧ�������
            osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // �����������
            osw.write(param);
            // flush������Ļ���
            osw.flush();
            // ����BufferedReader����������ȡURL����Ӧ
            in = conn.getInputStream();
            byte bs[] = new byte[in.available()];
            in.read(bs);
            result = new String(bs,"UTF-8");
        } catch (Exception e) {
            System.out.println("���� POST ��������쳣��"+e);
            Logger.error(e.getMessage());
        }
        //ʹ��finally�����ر��������������
        finally{
            try{
                if(osw!=null){
                    osw.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        //���� GET ����
//        String s=HttpRequest.sendGet("http://ip.taobao.com/service/getIpInfo.php?ip=223.72.74.217", "");
//        System.out.println(s);

    	for (int i=0; i<1000; i++) {
            String s=HttpRequest.sendGet("http://www.nedvr.com/c?action=2&oper=checkNed&deviceId=11", "");
            System.out.println(i + s);
    	}

//        //���� POST ����
//        String sr=HttpRequest.sendPost("http://localhost:8080/solr/lifetraces/update?commit=true", "<add><doc><field name=\"eid\">7</field><field name=\"pid\" update=\"set\">1</field><field name=\"tid\" update=\"set\">8</field><field name=\"type\" update=\"set\">1</field><field name=\"content\" update=\"set\">�й��ձ�����</field></doc></add>");
//        System.out.println(sr);
    	
      //���� POST ����
//      String sr=HttpRequest.sendPost("http://localhost:8008/solr/lifetraces/update?commit=true", "<delete><query>eid:*</query></delete>");
//      System.out.println(sr);
  	
//        //���� POST ����
//        try {
//        	String url = "http://localhost:8080/solr/lifetraces/select?wt=xml&indent=true&q=eid:7+tid:8";//+URLEncoder.encode("*", "UTF-8");
//        	System.out.println(url);
//			String sr2=HttpRequest.sendGet(url, "");
//			System.out.println(sr2);
//		} catch (Exception e) {
//			Logger.error(e.getMessage());
//		}

    }
}