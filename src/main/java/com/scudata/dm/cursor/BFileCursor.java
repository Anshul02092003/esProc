package com.scudata.dm.cursor;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.BFileReader;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;

/**
 * ���ļ��α�
 * @author
 *
 */
public class BFileCursor extends ICursor {
	private FileObject fileObject; // �ļ�����
	private String []fields; // ѡ���ֶ�
	
	// ���ڶ��߳�����
	private int segSeq; // �ֶκ�
	private int segCount; // �ܶ���
	
	private String opt; // ѡ��
	private int fileBufSize = Env.FILE_BUFSIZE; // �������ļ�ʱ�Ļ�������С
	private BFileReader reader; // ���ļ���ȡ��
	private boolean isDeleteFile; // �α�رպ�ɾ��Դ�ļ������ڼ�������в�������ʱ���ļ�

	// �Էֶ������㣬����������ļ�����ʼλ�úͽ���λ�ã�������ͷȥβ����
	private long startPos = -1;
	private long endPos;
	
	/**
	 * �������ļ��α�
	 * @param fileObject �ļ�����
	 * @param fields ѡ���ֶ�
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public BFileCursor(FileObject fileObject, String []fields, String opt, Context ctx) {
		this(fileObject, fields, 1, 1, opt, ctx);
	}

	/**
	 * �������ļ��α�
	 * @param fileObject �ļ�����
	 * @param fields ѡ���ֶ�
	 * @param segSeq ��ǰ�α�Ҫ���ĶΣ���1��ʼ����
	 * @param segCount �ֶ���
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public BFileCursor(FileObject fileObject, String []fields, 
			int segSeq, int segCount, String opt, Context ctx) {
		this.fileObject = fileObject;
		this.fields = fields;
		this.segSeq = segSeq;
		this.segCount = segCount;
		this.opt = opt;
		
		this.ctx = ctx;
		reader = new BFileReader(fileObject, fields, segSeq, segCount, opt);
		if (opt != null) {
			if (opt.indexOf('x') != -1) {
				if (ctx != null) ctx.addResource(this);
				isDeleteFile = true;
			}
		}
	}

	/**
	 * ���ö��ļ�����ʼλ�úͽ���λ��
	 * @param startPos ��ʼλ�ã�������ͷȥβ����
	 * @param endPos ����λ��
	 */
	public void setPosRange(long startPos, long endPos) {
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	/**
	 * ���ö��ļ���������С
	 * @param size
	 */
	public void setFileBufferSize(int size) {
		this.fileBufSize = size;
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (n < 1 || reader == null) {
			return null;
		}
		
		try {
			if (!reader.isOpen()) {
				reader.open(fileBufSize);
				DataStruct ds = reader.getResultSetDataStruct();
				setDataStruct(ds);
				
				if (!isDeleteFile && ctx != null) {
					ctx.addResource(this);
				}
				
				if (startPos > 0) {
					reader.seek(startPos);
					reader.setEndPos(endPos);
				}
			}
			
			Sequence seq = reader.read(n);
			return seq;
		} catch (Exception e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (n < 1 || reader == null) {
			return 0;
		}
		
		try {
			if (!reader.isOpen()) {
				reader.open(fileBufSize);
				DataStruct ds = reader.getResultSetDataStruct();
				setDataStruct(ds);
				
				if (startPos > 0) {
					reader.seek(startPos);
					reader.setEndPos(endPos);
				}
			}
			
			long count = reader.skip(n);
			return count;
		} catch (Exception e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		
		if (fileObject != null) {
			if (reader != null) {
				if (ctx != null) ctx.removeResource(this);
				try {
					reader.close();
				} catch (IOException e) {
				}
				
				reader = null;
			}

			if (isDeleteFile) {
				fileObject.delete();
				fileObject = null;
			}
		}
	}

	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		if (fileObject != null) {
			reader = new BFileReader(fileObject, fields, segSeq, segCount, opt);
			return true;
		} else {
			return false;
		}
	}
}
