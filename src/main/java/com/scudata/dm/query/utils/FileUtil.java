package com.scudata.dm.query.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
	/**
	    * �ڱ��ļ����²���
	    * @param s String �ļ���
	    * @return File[] �ҵ����ļ�
	    */
	   public static File[] getCurrDirectoryFiles(String s)
	   {
	     return getFiles("./",s);
	   }

	   public static File[] getFiles(String s)
	   {
		   //String s = "d:\\test\\emps*.txt";
		   s = s.replaceAll("\\\\", "/");
		   String folder = s.substring(0, s.lastIndexOf("/")+1);
		   String name = s.substring(s.lastIndexOf("/")+1);
		   return getFiles(folder,name);
	   }

	   /**
	    * ��ȡ�ļ�
	    * ���Ը���������ʽ����
	    * @param dir String �ļ�������
	    * @param s String �����ļ������ɴ�*.?����ģ����ѯ
	    * @return File[] �ҵ����ļ�
	    */
	   public static File[] getFiles(String dir,String s) {
	     //��ʼ���ļ���
	     File file = new File(dir);
	     s = s.replace('.', '#');
	     s = s.replaceAll("#", "\\\\.");
	     s = s.replace('*', '#');
	     s = s.replaceAll("#", ".*");
	     s = s.replace('?', '#');
	     s = s.replaceAll("#", ".?");
	     s = "^" + s + "$";
	     //System.out.println(s);
	     Pattern p = Pattern.compile(s);
	     ArrayList list = filePattern(file, p, true);
	     if (list == null) return null;
	     File[] rtn = new File[list.size()];
	     list.toArray(rtn);
	     return rtn;
	   }
	   /**
	    * @param file File ��ʼ�ļ���
	    * @param p Pattern ƥ������
	    * @return ArrayList ���ļ����µ��ļ���
	    */
	   private static ArrayList filePattern(File file, Pattern p, boolean first) {
	     if (file == null) {
	       return null;
	     }
	     else if (file.isFile()) {
	       Matcher fMatcher = p.matcher(file.getName());
	       if (fMatcher.matches()) {
	         ArrayList list = new ArrayList();
	         list.add(file);
	         return list;
	       }
	     }
	     else if (file.isDirectory()) {
	    	 if (!first) return null;
	       File[] files = file.listFiles();
	       if (files != null && files.length > 0) {
	         ArrayList list = new ArrayList();
	         for (int i = 0; i < files.length; i++) {
	           ArrayList rlist = filePattern(files[i], p, false);
	           if (rlist != null) {
	             list.addAll(rlist);
	           }
	         }
	         return list;
	       }
	     }
	     return null;
	   }
	   /**
	    * ����
	    * @param args String[]
	    */
	   public static void main(String[] args) {
//		   String s = "d:\\test\\emps*.txt";
//		   s = s.replaceAll("\\\\", "/");
//		   String folder = s.substring(0, s.lastIndexOf("/"));
//		   String name = s.substring(s.lastIndexOf("/")+1);
//		   System.out.println(s);		   
//		   File[] fs = getFiles("d:\\test\\","emps*.txt");
//		   File[] fs = getFiles("d:\\test\\emps*.txt");
		   File[] fs = getFiles("d:\\test\\emps1.txt");
		   for (int i=0; i<fs.length; i++) {
			   System.out.println(fs[i].getAbsolutePath());
		   }
	   }

}
