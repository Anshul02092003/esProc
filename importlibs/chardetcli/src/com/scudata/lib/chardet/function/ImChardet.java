package com.scudata.lib.chardet.function;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.mozilla.universalchardet.UniversalDetector;
import com.scudata.common.*;
import com.scudata.dm.FileObject;
import com.scudata.dm.IFile;

/***************************************
 * 
 * v_chardet@us(p)
 * ����p����Ϊ�ַ�����������ֵ��URL��FILE
 * 
 * 
 * */
// 
// parament p: string, file or bytes
public class ImChardet extends ImFunction {
	
	protected Object doQuery(Object[] objs) {
		int result = BytesEncodingDetect.OTHER;
		try {
			if (objs==null || objs.length!=1){
				throw new Exception("chardet paramSize error!");
			}
			
			// check encoding for string
			if(option!=null && option.contains("s")){
				BytesEncodingDetect detector = new BytesEncodingDetect();
				if(objs[0] instanceof String){					
					String str = objs[0].toString();
					result = detector.detectEncoding(str.getBytes());
					return BytesEncodingDetect.nicename[result];
				}else if(objs[0] instanceof Object){
					result = detector.detectEncoding((byte[])objs[0]);
					return BytesEncodingDetect.nicename[result];
				}	
			}else if(option!=null && option.contains("u")){
				if(objs[0] instanceof String){
					URL url = new URL(objs[0].toString());
					String encoding = detectEncoding(url);
					return encoding;
				}				
			}else if(objs[0] instanceof String){ // check encoding of file
				File file = new File(objs[0].toString());
				if (file.exists()){		
					String encoding = UniversalDetector.detectCharset(file);
					return encoding;
				}else{
					Logger.info("File: "+objs[0].toString()+" not existed.");
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
	
}
