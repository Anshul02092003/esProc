package com.scudata.expression.fn;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;
import com.scudata.common.*;
import com.scudata.dm.FileObject;
import com.scudata.dm.IFile;
import com.scudata.dm.Sequence;

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
					String encoding = detectEncoding(new URL(sTmp));
					return encoding;
				}else{ // for file
					File file = new File(sTmp);
					if (file.exists()){		
						String encoding = UniversalDetector.detectCharset(file);
						return encoding;
					}else{
						Logger.info("File: "+objs[0].toString()+" not existed.");
					}	
				}
			}else if(objs[0] instanceof FileObject){
				FileObject fo = (FileObject)objs[0];
				//String charset = fo.getCharset();
				IFile iff = fo.getFile();
				if (iff.exists()){		
					String encoding = UniversalDetector.detectCharset(iff.getInputStream());
					return encoding;
				}else{
					Logger.info("File: "+objs[0].toString()+" not existed.");
				}	
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
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
}
