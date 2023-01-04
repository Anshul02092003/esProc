package com.scudata.lib.hdfs.function;

import com.scudata.common.RQException;

/*** @author 
 * hdfs_upload@d(hd, localFile, remoteFile/remotePath)
 * remotePathʱ��·���������.
 * ѡ��d,Ҫ��Դ��Ŀ�궼���ļ���
 */
public class HdfsDownloadFile extends HdfsFunction {
	public Object doQuery( Object[] objs){
		try {
			if(objs==null ){
				throw new RQException("upload function.invalidParam");
			}else if (objs.length!=2){
				throw new RQException("upload ParamSize should have been 2");
			}else if (!(objs[0] instanceof String && objs[0] instanceof String)){
				throw new RQException("upload ParamType should have been String");
			}
			boolean bDirCopy = false;
			
			if (option!=null){
				if( option.equals("d")){
					bDirCopy = true;
				}				
			}
			String localFile = objs[0].toString();
			String hdFile = objs[1].toString();
		
			HdfsFileImpl hfile = new HdfsFileImpl(m_hdfs);
			if (bDirCopy){
				hfile.downloadFiles( hdFile, localFile);
			}else{
				hfile.downloadFile( hdFile, localFile);
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
}
