package com.scudata.expression.fn;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scudata.common.*;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import org.mozilla.universalchardet.UniversalDetector;

/***************************************
 * 
 * chardetect@v(p)
 * ����p����Ϊ�ַ�����������ֵ��URL���ļ���
 * ����ֵ����������ֵʱΪString, ����Ϊ����
 * 
 * */

public class CharDetect extends CharFunction {
	
	protected Object doQuery(Object[] objs) {
		List<String> result = null;
		try {
			if (objs==null || objs.length!=1){
				throw new Exception("chardet paramSize error!");
			}
			
			// check encoding for string
			if(option!=null && option.contains("v")){
				CharEncodingDetect detector = new CharEncodingDetect();
				if(objs[0] instanceof String){					
					String str = objs[0].toString();
					result = detector.autoDetectEncoding(str.getBytes());					
				}else if(objs[0] instanceof byte[]){
					result = detector.autoDetectEncoding((byte[])objs[0]);
				}	
				
				if (result==null){
					return null;
				}

				if (result.size()==1){
					return result.get(0);
				}else{
					Sequence seq = new Sequence(result.toArray(new String[result.size()]));
					return seq;
				}
			}else if(objs[0] instanceof String){ 
				String sTmp = objs[0].toString();				
				String reg = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
				if (isMatch(sTmp, reg)){ // for url
					return detectEncoding(new URL(sTmp));
				}else{ // for file
					return detectCharsetFile(sTmp);
				}
			}else if(objs[0] instanceof FileObject){
				FileObject fo = (FileObject)objs[0];
				
				return detectCharsetFile(fo.getFileName());
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		return null;
	}
	
	private Object detectCharsetFile(String sfile) throws IOException
	{
		File file = new File(sfile);
		if (file.exists()){						
			return getFileCharset(file);
		}
		String fullFile = null;
		
		// 1. �û����õ�main
		String path = Env.getMainPath();
		if (path!=null){
			fullFile = path+File.separatorChar+sfile;
			file = new File(fullFile);
			if (file.exists()){	
				return getFileCharset(file);
			}
		}
		
		// 2. ϵͳ�Դ���main
		path = System.getProperty("start.home");
		fullFile = path+File.separatorChar+"main"+File.separatorChar+sfile;
		file = new File(fullFile);
		if (file.exists()){	
			return getFileCharset(file);
		}
		// 3. ϵͳ�Դ���demo
		fullFile = path+File.separatorChar+"demo"+File.separatorChar+sfile;
		file = new File(fullFile);
		if (file.exists()){	
			return getFileCharset(file);
		}else{
			Logger.info("File: "+ sfile +" not existed.");
		}
		
		return null;
	}
	
    private static String udetect(byte[] content) {
        UniversalDetector detector = new UniversalDetector(null);
 
        //��ʼ��һ�������ݣ���ѧϰһ�°����ٷ�������1000��byte���ң���Ȼ��1000��byte��ð�������֮��ģ�
        detector.handleData(content, 0, content.length);
        //ʶ�������������������
        detector.dataEnd();
        //�����ʱ�̾�����������ˣ������ַ������롣
        return detector.getDetectedCharset();
    }
	
    /**
	 * Function : detectEncoding Aruguments: URL Returns : One of the encodings from the Encoding enumeration (GB2312, HZ, BIG5,
	 * EUC_TW, ASCII, or OTHER) Description: This function looks at the URL contents and assigns it a probability score for each
	 * encoding type. The encoding type with the highest probability is returned.
	 */
    private String detectEncoding(URL testurl) {
		byte[] rawtext = new byte[1024*10];
		int bytesread = 0, byteoffset = 0;
		
		InputStream istream;
		try {
			istream = testurl.openStream();
			while ((bytesread = istream.read(rawtext, byteoffset, rawtext.length - byteoffset)) > 0) {
				byteoffset += bytesread;
			}
			;
			istream.close();
			return udetect(rawtext);			
		} catch (Exception e) {
			System.err.println("Error loading or using URL " + e.toString());
			
		}
		return null;
	}
	
	// ͨ��Url��ȡ��������port, warehouse
	private boolean isMatch(String strUrl, String regExp)
	{
		if (strUrl==null || strUrl.isEmpty()){
			throw new RQException("spark isMatch strUrl is empty");
		}
		
		if (regExp==null || regExp.isEmpty()){
			throw new RQException("spark isMatch regExp is empty");
		}
		
		Pattern p=Pattern.compile(regExp);
		Matcher m = p.matcher(strUrl);
		
		return m.matches();
	}
	
	private String getFileCharset(File file) throws IOException {
	    byte[] buf = new byte[4096];
	    BufferedInputStream ins = new BufferedInputStream(new FileInputStream(file));
	    final UniversalDetector detector = new UniversalDetector(null);
	    int nread;
	    while ((nread = ins.read(buf)) > 0 && !detector.isDone()) {
	    	detector.handleData(buf, 0, nread);
	    }
	    detector.dataEnd();
	    String encoding = detector.getDetectedCharset();
	    detector.reset();
	    ins.close();

	    //ʶ��ı��뾡������EncodingEx.htmlname��Χ��.
	    if (encoding ==null){
	    	encoding = CharEncodingDetectEx.getJavaEncode(file.getAbsolutePath());
	    }else {
	    	BytesEncodingDetect s = new BytesEncodingDetect(); 
	    	boolean bExisted = false;
	    	for(String name : EncodingEx.htmlname){
	    		if (name.equals(encoding)){
	    			bExisted = true;
	    			break;
	    		}
	    	}
	    	if (!bExisted){
	    		String encode = CharEncodingDetectEx.getJavaEncode(file.getAbsolutePath());
	    		if (encode!=null){
	    			encoding = encode;
	    		}
	    	}
	    }
	   
	    return encoding;
	}
}
