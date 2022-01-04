package com.scudata.parallel;

import java.io.*;
import java.util.*;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Machines;
import com.scudata.dm.RemoteFile;
import com.scudata.dw.BufferReader;
import com.scudata.dw.GroupTable;
import com.scudata.resources.EngineMessage;
import com.scudata.resources.ParallelMessage;

public class PartitionUtil {
	static HostManager hm = HostManager.instance();
	static MessageManager pm = ParallelMessage.get();
	
	private static Object ask(String host, int port, Request req) {
		UnitClient uc = null;
		Response res = null;
		try {
			uc = new UnitClient(host, port);
			uc.connect();
			res = uc.send(req);
			if (res.getError() != null) {
				Error e = res.getError();
				throw new RQException("["+uc+"] "+e.getMessage(), e);// req+"  on ["+host+":"+port+"] error.",
			}
			if (res.getException() != null) {
				Exception x = res.getException();
				throw new RQException("["+uc+"] "+x.getMessage(), x);// req+"  on ["+host+":"+port+"] exception.",
			}
			return res.getResult();
		} catch (RQException rq) {
			throw rq;
		} catch (Exception x) {
			throw new RQException(pm.getMessage("PartitionUtil.askerror",host+":"+port),x);
//					"Request to node [" + host + ":" + port
//					+ "] failed.", x);
		} finally {
			if (uc != null) {
				uc.close();
			}
		}
	}

	/**
	 * �ӷֻ�����mcs�����ļ�file�����ȱ��أ�hs����1ֻ��������ɶ�д
	 * @param mcs �ֻ���
	 * @param file �ļ���
	 * @return FileObject
	 */
	public static FileObject locate(Machines mcs, String file) {
		return locate(mcs,file,null);
	}

/**
 * �ӷֻ�����hs�����ļ�fn�����ȱ��أ�hs����1ֻ��������ɶ�д
 * @param mcs �ֻ���
 * @param file �ļ���
 * @param z �ֱ�����
 * @return FileObject
 */
	public static FileObject locate(Machines mcs, String file, Integer z) {
		if (mcs == null)
			throw new RQException("hosts is null");
		FileObject fo = new FileObject(file);
		fo.setPartition(z);
		if (fo.isExists()) {//���ȱ���
			return fo;
		}

		if(mcs.size()==1){
			String host = mcs.getHost(0);
			int port = mcs.getPort(0);
			fo = new FileObject(file, host, port);
			fo.setPartition(z);
			fo.setRemoteFileWritable();//��ָ��Ψһ�ֻ�ʱ������Զ���ļ���д
			return fo;
		}
		
		HashMap<UnitClient, FileObject> existFileNodes = new HashMap<UnitClient, FileObject>();
		for (int i = 0; i < mcs.size(); i++) {
			String host = mcs.getHost(i);
			int port = mcs.getPort(i);
			try {
				RemoteFile rf = new RemoteFile(host, port, file, z);
				if (rf.exists()) {
					fo = new FileObject(file, host, port);
					fo.setPartition(z);
					existFileNodes.put(new UnitClient(host, port), fo);
				}
			} catch (RQException rx) {
			}
		}
		if (existFileNodes.isEmpty())
//			throw new RQException(pm.getMessage("PartitionUtil.lackfile2",file,partition));
			throw new RQException(file+" is not exists in node machines.");
		int taskCount = 0;
		UnitClient targetUC = null;
		Iterator it = existFileNodes.keySet().iterator();
		while (it.hasNext()) {
			UnitClient uc = (UnitClient) it.next();
			int c = uc.getCurrentTasks();
			if (taskCount == 0 || c < taskCount) {
				taskCount = c;
				targetUC = uc;
			}
		}
		return existFileNodes.get(targetUC);
	}

	/**
	 * �г��ֻ�host��·��path�µ������ļ���Ϣ
	 * @param host �ֻ�IP
	 * @param port �ֻ��˿ں�
	 * @param path ·������
	 * @return �ļ���Ϣ���б�
	 */
	public static List<FileInfo> listFiles(String host, int port,String path) {
		Request req = new Request(Request.PARTITION_LISTFILES);
		req.setAttr(Request.LISTFILES_Path, path);
		return (List) ask(host, port, req);
	}

	/**
	 * �������ļ�fn�Ƶ�hs�ֻ���p·���£�hs�������У�hʡ�Ա���
	 * @param fileName Դ�ļ�
	 * @param partition �������
	 * @param hs Ŀ��ֻ���
	 * @param dstPath Ŀ��·��
	 * @param option ѡ��
	 */
	public static void moveFile(String fileName,int partition,
			Machines hs, String dstPath, String option) {
		moveFile(null,0,fileName,partition, hs, dstPath, option);
	}
	
	public static boolean isCOption(String option){
		return option!=null && option.indexOf("c")>-1;
	}
	public static boolean isYOption(String option){
		return option!=null && option.indexOf("y")>-1;
	}
	/**
	 * movefile@yc(fn:h,p:hs)	���ֻ�h�ϵ��ļ�fn�Ƶ�hs�ֻ���p·���£�hs�������У�hʡ�Ա���
	 * hsʡ��Ϊ����
	 * p:hsʡ��ɾ��
	 * h��pʡ�Ե�hs������ɾ��hs�µ��ļ�
	 * @param host Դ��IP
	 * @param port Դ���˿ں�
	 * @param fileName Դ�ļ�
	 * @param partition �������
	 * @param hs Ŀ��ֻ���
	 * @param dstPath Ŀ��·��
	 * @param option ѡ��
	 */
	public static boolean moveFile(String host, int port, String fileName,int partition,
			Machines hs, String dstPath, String option) {
		// 1:���ͬ����host���Ǳ��ػ�����������Ϣ��host����host����ִ�б��ز�����
		if (host != null) {
			boolean isLocal = host.equals(hm.getHost()) && port == hm.getPort();
			if (!isLocal) {
				Request req = new Request(Request.PARTITION_MOVEFILE);
				req.setAttr(Request.MOVEFILE_Machines, hs);
				req.setAttr(Request.MOVEFILE_Filename, fileName);
				req.setAttr(Request.MOVEFILE_Partition, partition);
				req.setAttr(Request.MOVEFILE_DstPath, dstPath);
				req.setAttr(Request.MOVEFILE_Option, option);
				ask(host, port, req);
				return true;
			}
		}
		
		//2:��������ɾ��
		String absolute = PartitionManager.getAbsolutePath(fileName);
		File file = new File( absolute );
		
		if (hs == null) {//hsʡ��ʱ
			if(!file.exists()){//ֻ���ڱ��ز���ʱ���жϸ��ļ��Ƿ����
				throw new RQException( absolute + " is not exist.");
			}
			if(!StringUtils.isValidString(dstPath)){//pҲʡ��ʱ����ɾ�������ļ�
				file.delete();
			}else{
			//����
				if (file.exists()) {
					File newFile = new File(file.getParent(), dstPath);
					file.renameTo(newFile);
				}
			}
			return true;
		}else if(!StringUtils.isValidString(dstPath)){//pҲʡ��ʱ����ɾ��hs�ϵ�����ļ�
			//�˴���������Զ�̷ֻ��ϵ��ļ��������ж�file�Ƿ����
			for(int n=0;n<hs.size();n++){
				String tmpHost = hs.getHost(n);
				int tmpPort = hs.getPort(n);
				Request req = new Request(Request.PARTITION_DELETE);
				req.setAttr(Request.DELETE_FileName, fileName);
				req.setAttr(Request.DELETE_Option, option);
				try{
					ask(tmpHost, tmpPort, req);
				}catch(Exception x){//�����쳣����ֹһ̨�ֻ�����Ӱ�������ֻ�ִ��
					Logger.warn(x);
				}
			}
			return true;
		}
		
		if(file.isDirectory()){
			throw new RQException( absolute + " is not a file.");
		}
		if(!file.exists()){
			throw new RQException( absolute + " is not exist.");
		}

		// 4:�����ϴ������зֻ�
		File dstFile = new File(dstPath);
		boolean isDstAbsolute = dstFile.isAbsolute();
		for (int n = 0; n < hs.size(); n++) {
			ArrayList<String> tmpUpFiles = new ArrayList<String>();
			tmpUpFiles.add( absolute );
			String targetPath;
			if( isDstAbsolute ){
				targetPath = new File(dstPath, file.getName()).getAbsolutePath();
			}else{
				targetPath = new File(dstPath, file.getName()).getPath();
			}
			upload(hs.getHost(n), hs.getPort(n), tmpUpFiles, targetPath, true, isYOption(option));
		}
		
		if(!isCOption(option)){//���Ǹ���ģʽʱ�����ɾ�������ļ�
			file.delete();
		}
		
		return true;
	}
	
	private static void uploadFile(UnitClient uc, File file,
			String dstPathName, boolean isMove, boolean isY) throws Exception {
//		if (file.isDirectory()) {
//			File[] subFiles = file.listFiles();
//			for (int i = 0; i < subFiles.length; i++) {
//				File subFile = subFiles[i];
//				uploadFile(uc, subFile, dstPathName
//						+ File.separator + subFile.getName());
//			}
//			return;
//		}
		int type = getFileType(file);
		if (type > 0) {
			uploadCtxFile(uc, file, dstPathName);
			return;
		}
		Request req;
//		if(dstPartition==-1){
//			req = new Request(Request.PARTITION_UPLOAD_DFX);
//			req.setAttr(Request.UPLOAD_DFX_RelativePath, dstPathName);
//			req.setAttr(Request.UPLOAD_DFX_LastModified, new Long(file.lastModified()));
//		}else{
			req = new Request(Request.PARTITION_UPLOAD);
			req.setAttr(Request.UPLOAD_DstPath, dstPathName);
			req.setAttr(Request.UPLOAD_LastModified, new Long(file.lastModified()));
			req.setAttr(Request.UPLOAD_IsMove, isMove);
			req.setAttr(Request.UPLOAD_IsY, isY);
//		}

		uc.write(req);
		Response res = (Response) uc.read();
		if (res.getException() != null) {
			throw res.getException();
		}
		
		boolean isNeedUpdate = (Boolean)res.getResult();
		if(!isNeedUpdate){
			return;
		}
		
		FileInputStream fis = new FileInputStream(file);
		byte[] fileBuf = RemoteFileProxyManager.read(fis, Env.FILE_BUFSIZE);
		uc.write(fileBuf);
		while (fileBuf != null) {// Request.EOF) {
			fileBuf = RemoteFileProxyManager.read(fis, Env.FILE_BUFSIZE);
			uc.write(fileBuf);
		}
		fis.close();

		res = (Response) uc.read();
		if (res.getException() != null)
			throw res.getException();

		Logger.debug("upload: " + file.getAbsolutePath() + " OK.");
	}

	// ����
	/**
	 * ���ֻ�host���ϴ�һ���ļ������ļ���
	 * @param host �ֻ���IP��ַ
	 * @param port �ֻ��Ķ˿ں�
	 * @param localFile
	 *            �����ļ������ļ���
	 * @param dstPath
	 *            ���ص�Ŀ����ļ�����nullʱ��localFileͬ��
	 * @throws Exception
	 */
	public static void upload(String host, int port, List localFiles, String dstPath) {
		upload(host,port,localFiles,dstPath,false,false);
	}
	
	/**
	 * ���ֻ�host���ϴ�һ���ƶ�ģʽ���ļ��������Ƚ�LastModifiedֵ�������Ƿ��ƶ�
	 * @param host �ֻ���IP��ַ
	 * @param port �ֻ��Ķ˿ں�
	 * @param localFile
	 *            �����ļ������ļ���
	 * @param dstPath
	 *            ���ص�Ŀ����ļ�����nullʱ��localFileͬ��
	 * @param isMove �Ƿ�Ϊ�ƶ�ģʽ���ϴ��ļ�
	 * @param isY �ƶ�ģʽʱ���Ƿ�ǿ�Ƹ���Ŀ���ļ�����Ļ�������Ŀ��ʱ������
	 */
	public static void upload(String host, int port, List localFiles, String dstPath, boolean isMove, boolean isY) {
		MessageManager mm = EngineMessage.get();

		UnitClient uc = new UnitClient(host, port);
		try {
			uc.connect();
			for (int i = 0; i < localFiles.size(); i++) {
				String localFile = (String) localFiles.get(i);
				File f = new File(localFile);
				if (!f.exists()){
					Logger.warning(mm.getMessage("partitionutil.filenotexist",localFile));
					continue;
				}
				uploadFile(uc, f, dstPath, isMove, isY);
			}
		} catch (Exception x) {
			throw new RQException("["+uc+"] "+x.getMessage(), x);
		} finally {
			if (uc != null) {
				uc.close();
			}
		}
	}


	/**
	 * ������·��path�µ��ļ�ͬ����machines
	 * @param machines Ŀ�ķֻ�Ⱥ
	 * @param path ·������
	 */
	public static void syncTo(Machines machines, String path) {
		syncTo(null,0,machines,path);
	}
	/**
	 * ��host����·��p�µ��ļ�ͬ����machines�Ķ�Ӧp·��
	 * @param host ͬ���Ļ���IP
	 * @param port ͬ���Ļ����˿�
	 * @param machines ��ͬ����Ŀ�Ļ���Ⱥ
	 * @param path��Ҫͬ����·����
	 */
	public static void syncTo(String host, int port, Machines machines, String path) {
		MessageManager mm = EngineMessage.get();
		// 0:���Ŀ�ķֻ�
		if (machines == null || machines.size() == 0) {
			throw new RQException(mm.getMessage("partitionutil.notarget"));
		}
		if(path==null){
			throw new RQException("Path can not be empty.");
		}
		String absPath = PartitionManager.getAbsolutePath(path);
		File file = new File(absPath);
		if( !file.isDirectory() ){
			throw new RQException( absPath +" is not a directory!");
		}
		
		
		// 1:���ͬ����host���Ǳ��ػ�����������Ϣ��host����host����ִ�б��ز�����
		if (host != null) {
			boolean isLocal = host.equals(hm.getHost()) && port == hm.getPort();
			if (!isLocal) {
				Request req = new Request(Request.PARTITION_SYNCTO);
				req.setAttr(Request.SYNC_Machines, machines);
				req.setAttr(Request.SYNC_Path, path);
				ask(host, port, req);
				return;
			}
		}
		
		// 2:�г�Ŀ������ϵ������ļ�
		List<FileInfo>[] machineFileList = new List[machines.size()];
		for (int i = 0; i < machines.size(); i++) {
			List<FileInfo> fileInfosN = listFiles(machines.getHost(i), machines.getPort(i),path);
			machineFileList[i] = fileInfosN;
		}
		
		// 3:�г������ļ��б�
		List<FileInfo> localFiles = PartitionManager.listPathFiles( path, true);
		
		// 4:�������ļ��б����θ��µ����зֻ�
		for (int i = 0; i < localFiles.size(); i++) {
			FileInfo syncFi = (FileInfo) localFiles.get(i);// ��ͬ���ļ�
			if(syncFi.isDir()){
				continue;
			}
			for (int n = 0; n < machineFileList.length; n++) {
				List<FileInfo> fileInfosN = machineFileList[n];
				int index = fileInfosN.indexOf(syncFi);
				if (index >= 0) {
					FileInfo fiN = (FileInfo) fileInfosN.get(index);
					if (fiN.lastModified() > syncFi.lastModified())
						continue;// �ֻ����ļ�����һЩ
				}
				ArrayList<String> tmpUpFiles = new ArrayList<String>();
				File absFile = syncFi.getFile(path);
				tmpUpFiles.add( absFile.getAbsolutePath());
				String dstPath = syncFi.getDestPath(path);
				upload(machines.getHost(n), machines.getPort(n), tmpUpFiles, dstPath);
			}
		}
	}
	

	//��������ļ�
	private static void uploadCtxFile(UnitClient uc, File file, String dstPathName) throws Exception {
		Request req;
		if (!file.exists()) {
			return;
		}
		if (file.getName().indexOf(GroupTable.SF_SUFFIX) != -1) {
			return;
		}
		
		GroupTable table = GroupTable.open(file, null);
		File extFile = GroupTable.getSupplementFile(file);
		
		req = new Request(Request.PARTITION_UPLOAD_CTX);
		req.setAttr(Request.UPLOAD_DstPath, dstPathName);
		req.setAttr(Request.UPLOAD_LastModified, Long.valueOf(file.lastModified()));
		req.setAttr(Request.UPLOAD_BlockLinkInfo, table.getBlockLinkInfo());
		req.setAttr(Request.UPLOAD_FileSize, file.length());
		req.setAttr(Request.UPLOAD_FileType, Integer.valueOf(1));
		
		if (extFile.exists()) {
			req.setAttr(Request.UPLOAD_HasExtFile, Boolean.TRUE);
			req.setAttr(Request.UPLOAD_ExtFileLastModified, Long.valueOf(extFile.lastModified()));
		} else {
			req.setAttr(Request.UPLOAD_HasExtFile, Boolean.FALSE);
		}
		uc.write(req);
		
		long [] modifyPositions = table.getModifyPosition();
		long []positions = (long[]) uc.read();
		long remoteFileSize = (Long) uc.read();
		int blockSize = table.getBlockSize();
		byte []buf = new byte[blockSize];
		
		if (remoteFileSize == 0) {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			try {
				int size;
				raf.seek(0);
				size = raf.read(buf);
				while (size != -1) {
					uc.write(buf);
					size = raf.read(buf);
				}
				uc.write(null);
			} finally {
				raf.close();
				table.close();
			}
			
			if (extFile.exists()) {
				raf = new RandomAccessFile(extFile, "rw");
				try {
					int size;
					raf.seek(0);
					size = raf.read(buf);
					while (size != -1) {
						uc.write(buf);
						size = raf.read(buf);
					}
					uc.write(null);
				} finally {
					raf.close();
				}
			}
			Response res = (Response) uc.read();
			if (res.getException() != null)
				throw res.getException();

			Logger.debug("upload: " + file.getAbsolutePath() + " OK.");
			return;
		}
		
		if (positions != null && positions.length > 0) {
			positions = table.getSyncPosition(positions);
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		
		try {
			if (modifyPositions != null) {
				for (long pos : modifyPositions) {
					if (pos >= remoteFileSize) 
						continue;
					raf.seek(pos);
					raf.read(buf);
					uc.write("m");//��ʾ�ǲ���
					uc.write(pos);
					uc.write(buf);
				}
			}
			
			if (positions != null) {
				for (long pos : positions) {
					if (pos >= remoteFileSize) 
						continue;
					raf.seek(pos);
					raf.read(buf);
					uc.write("n");//��ʾ��������
					uc.write(pos);
					uc.write(buf);
				}
			}

			long fileSize = file.length();
			while (remoteFileSize < fileSize) {
				raf.seek(remoteFileSize);
				raf.read(buf);
				uc.write("a");//��ʾ��������
				uc.write(remoteFileSize);
				uc.write(buf);
				remoteFileSize += blockSize;
			}
			
			positions = table.getHeaderPosition();
			for (long pos : positions) {
				if (pos >= remoteFileSize) 
					continue;
				raf.seek(pos);
				raf.read(buf);
				uc.write("h");//��ʾ��header��
				uc.write(pos);
				uc.write(buf);
			}
			
			uc.write(null);//end
		} finally {
			table.close();
			raf.close();
		}

		if (extFile.exists()) {
			raf = new RandomAccessFile(extFile, "rw");
			try {
				int size;
				raf.seek(0);
				size = raf.read(buf);
				while (size != -1) {
					uc.write(buf);
					size = raf.read(buf);
				}
				uc.write(null);
			} finally {
				raf.close();
			}
		}
		
		Response res = (Response) uc.read();
		if (res.getException() != null)
			throw res.getException();

		Logger.debug("upload: " + file.getAbsolutePath() + " OK.");
	}
	
	//������������ļ�
	private static void uploadIdxFile(UnitClient uc, File file, String dstPathName) throws Exception {
		Request req;
		if (!file.exists()) {
			return;
		}

		FileInputStream fis = new FileInputStream(file);
		byte[] header = RemoteFileProxyManager.read(fis, 1024);
		BufferReader reader = new BufferReader(null, header, 39, 1024);
		long indexPos1, indexPos2, index1EndPos;
		reader.readLong64();
		index1EndPos = reader.readLong64();
		reader.readLong64();
		reader.readLong64();
		reader.readLong64();
		indexPos1 = reader.readLong64();
		indexPos2 = reader.readLong64();
		fis.close();

		long []pos = new long[]{indexPos1, indexPos2, index1EndPos}; 
		req = new Request(Request.PARTITION_UPLOAD_CTX);
//		req.setAttr(Request.UPLOAD_DstPartition, dstPartition);
		req.setAttr(Request.UPLOAD_DstPath, dstPathName);
		req.setAttr(Request.UPLOAD_LastModified, new Long(file.lastModified()));
		req.setAttr(Request.UPLOAD_FileSize, file.length());
		req.setAttr(Request.UPLOAD_BlockLinkInfo, pos);
		req.setAttr(Request.UPLOAD_FileType, new Integer(3));
		uc.write(req);
		
		int syncType = (Integer) uc.read();

		if (syncType == 0) {
			fis = new FileInputStream(file);
			byte[] fileBuf = RemoteFileProxyManager.read(fis, Env.FILE_BUFSIZE);
			uc.write(fileBuf);
			while (fileBuf != null) {// Request.EOF) {
				fileBuf = RemoteFileProxyManager.read(fis, Env.FILE_BUFSIZE);
				uc.write(fileBuf);
			}
			fis.close();
		} else {
			fis = new FileInputStream(file);
			byte[] fileBuf = RemoteFileProxyManager.read(fis, (int) indexPos1);
			uc.write(fileBuf);
			fis.skip(indexPos2 - indexPos1);
			while (fileBuf != null) {// Request.EOF) {
				fileBuf = RemoteFileProxyManager.read(fis, Env.FILE_BUFSIZE);
				uc.write(fileBuf);
			}
			fis.close();
		}

		Response res = (Response) uc.read();
		if (res.getException() != null)
			throw res.getException();

		Logger.debug("upload: " + file.getAbsolutePath() + " OK.");
	}

	private static int getFileType(File file) {
		RandomAccessFile raf = null;
		if (!file.exists()) {
			return 0;
		}
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(0);
			byte []bytes = new byte[32];
			raf.read(bytes);
			if (bytes[0] != 'r' || bytes[1] != 'q' || bytes[2] != 'd' || bytes[3] != 'w') {
				return 0;
			}
			
			if (bytes[4] == 'g' && bytes[5] == 't') {
				if (bytes[6] == 'c') {
					return 1;
				} else if (bytes[6] == 'r') {
					return 2;
				}
			}
//			if (bytes[4] == 'i' && bytes[5] == 'd') {
//				if (bytes[6] == 'x') {
//					return 3;
//				}
//			}
			return 0;
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}
}