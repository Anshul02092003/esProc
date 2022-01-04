package com.scudata.parallel;

import java.io.File;
import java.io.Serializable;

import com.scudata.dm.Env;

/**
 * ���ڴ洢�����ĳ��·���µ��ļ���Ϣ
 * @author Joancy
 *
 */
public class FileInfo implements Serializable,Comparable<Object> {
	private static final long serialVersionUID = 3777477339763658303L;

//	public Integer partition;//������Ϊ-1ʱ����ʾfileName�����Env.getMainPath()
	public String fileName; // ���·������·������/�ָ�
	private boolean isDir; // �Ƿ�Ŀ¼
	
	private boolean isDirEmpty = false; // �����Ŀ¼����ǰ��Ŀ¼�Ƿ�Ϊ��

	private long lastModified = -1;

	public FileInfo( String fileName, boolean isDir) {
//		this.partition = partition;
		this.fileName = fileName;
		this.isDir = isDir;
	}

//	public Integer getPartition() {
//		return partition;
//	}

	public String getFileName() {
		return fileName;
	}

	public boolean isDir() {
		return isDir;
	}
	
	public boolean isDirEmpty() {
		return isDirEmpty;
	}
	
	public void setDirEmpty(boolean isEmpty){
		isDirEmpty = isEmpty;
	}

	public boolean isAbsoluteFile(){
		File f = new File(fileName);
		return f.isAbsolute();
	}
/**
 * �����ļ����Ƶ�Ŀ�������·��
 * @param parent �ļ��ڱ����ĸ�·��
 * @return Ŀ�������·�������parent�Ǿ���·������Ŀ�����ʹ����ͬ�ľ���·��
 * ���parent�����·������Ŀ�����·����Ϊȥ���˱�����·�������·��
 */
	public String getDestPath(String parent){
		File p = new File(parent);
		String header;
		if(p.isAbsolute()){
			header = parent;
			return new File(header, fileName).getAbsolutePath();
		}else{
			String mainP = Env.getMainPath();
			header = new File( mainP,parent).getAbsolutePath();
			String tmp = new File(header, fileName).getAbsolutePath();
			return tmp.substring(mainP.length()+1);
		}
	}
	
	public File getFile(String parent) {
		File p = new File(parent);
		String header;
		if(p.isAbsolute()){
			header = parent;
		}else{
			header = new File(Env.getMainPath(),parent).getAbsolutePath();
		}
		return new File(header, fileName);
	}

	public void setLastModified(long m) {
		this.lastModified = m;
	}

	public long lastModified() {
		return lastModified;
	}

	public String toString() {
		return fileName;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof FileInfo) {
			FileInfo otherFile = (FileInfo) obj;
				if (fileName != null
						&& fileName.equals(otherFile.getFileName()))
					if (isDir == otherFile.isDir())
						return true;
		}
		return false;
	}

	public int compareTo(Object o) {
		FileInfo other = (FileInfo)o;
		return fileName.compareTo(other.getFileName());
	}
}
