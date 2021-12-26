package com.scudata.vdb;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IResource;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

import java.sql.Timestamp;

/**
 * ���ݿ����ӣ���vdbase()������������Ӧ��Ŀ¼
 * @author RunQian
 *
 */
public class VDB implements IVS, IResource {
	public static final int S_SUCCESS = 0; // �ɹ�
	public static final int S_LOCKTIMEOUT = 1; // ����ʱ
	public static final int S_LOCKTWICE = -1; // �ظ���
	public static final int S_PATHNOTEXIST = 2; // ·��������
	public static final int S_TARGETPATHEXIST = 3; // Ŀ��·���Ѵ���
	public static final int S_IOERROR = 4; // IO����
	public static final int S_PARAMTYPEERROR = 5; // �������ʹ���
	public static final int S_PARAMERROR = 6; // ����ֵ����
	public static final int S_ACHEIVED = 7; // ·���Ѿ��鵵
	
	private static final long LATEST_TX_SEQ = Long.MAX_VALUE; // ��������λ�������
	
	private Library library; // ��Ӧ�������
	private ISection rootSection; // ����
	
	private ArrayList<ISection> modifySections = new ArrayList<ISection>(); // δ�ύSection�б�
	
	private int error = S_SUCCESS;
	private boolean isAutoCommit = true; // �Ƿ��Զ��ύ
	private long loadTxSeq = LATEST_TX_SEQ; // ������ţ����û����������������µ�

	public VDB(Library library) {
		this.library = library;
		this.rootSection = library.getRootSection();
	}

	public void checkValid() {
		if (library == null) {
			throw new RQException("���ӹر�");
		}
	}
	
	int getOuterTxSeq() {
		return library.getOuterTxSeq();
	}
	
	long getLoadTxSeq() {
		return loadTxSeq;
	}
	
	public Library getLibrary() {
		return library;
	}
	
	// ��������
	public boolean begin() {
		checkValid();
		
		if (!isAutoCommit) return false;
		
		error = S_SUCCESS;
		isAutoCommit = false;
		loadTxSeq = library.getLoadTxSeq();
		return true;
	}

	// �ύ����������
	public int commit() {
		if (error != S_SUCCESS) {
			int result = error;
			rollback();
			return result;
		}
		
		checkValid();

		isAutoCommit = true;
		loadTxSeq = LATEST_TX_SEQ;
		
		library.commit(this);
		modifySections.clear();
		return S_SUCCESS;
	}

	// �ع�����������
	public void rollback() {
		checkValid();

		error = S_SUCCESS;
		isAutoCommit = true;
		loadTxSeq = LATEST_TX_SEQ;

		library.rollback(this);
		modifySections.clear();
	}

	//private void printStack() {
	//	Logger.info(hashCode(), new Exception());
	//}
	
	// �ر����ݿ����ӣ��ع���ǰδ�������
	public void close() {
		if (library != null) {
			//printStack();
			rollback();
			library.deleteVDB(this);
			library = null;
			rootSection = null;
			modifySections = null;
		}
	}
	
	protected void finalize() throws Throwable {
		close();
	}
	
	public VDB getVDB() {
		return this;
	}
	
	/**
	 * ȡ���ӵĵ�ǰ��
	 * @return ISection
	 */
	public ISection getHome() {
		return rootSection;
	}

	/**
	 * ���õ�ǰ·����������д����������ڴ�·��
	 * @param path
	 * @return IVS
	 */
	public IVS home(Object path) {
		return home(rootSection, path);
	}
	
	/**
	 * ���ص�ǰ·��
	 * @param opt
	 * @return Object
	 */
	public Object path(String opt) {
		return null;
	}

	private boolean isAutoCommit() {
		return isAutoCommit;
	}
	
	private void resetError() {
		this.error = S_SUCCESS;
	}
	
	void setError(int error) {
		this.error = error;
		
		library.rollback(this);
		modifySections.clear();
	}
	
	int getError() {
		return error;
	}
	
	ArrayList<ISection> getModifySections() {
		return modifySections;
	}
	
	/**
	 * ��ӽڵ������б���
	 * @param section
	 */
	void addModifySection(ISection section) {
		modifySections.add(section);
	}
	
	/**
	 * �ѽڴӸ����б���ɾ����ȡ������
	 * @param section
	 */
	void removeModifySection(ISection section) {
		modifySections.remove(section);
		section.rollBack(library);
	}
	
	/**
	 * ������д�뵽��ǰ��
	 * @param value ����
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int save(Object value) {
		return save(rootSection, value);
	}
	
	/**
	 * ������д�뵽ָ��·���ı�
	 * @param value ����
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int save(Object value, Object path, Object name) {
		return save(rootSection, value, path, name);
	}
	
	/**
	 * ����Ŀ¼
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int makeDir(Object path, Object name) {
		return makeDir(rootSection, path, name);
	}
	
	/**
	 * ��ס��ǰ·��
	 * @param opt ѡ�r������·���򶼼�д����u������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int lock(String opt) {
		return lock(rootSection, opt);
	}
	
	/**
	 * ��סָ��·��
	 * @param path ·����·������
	 * @param opt ѡ�r������·���򶼼�д����u������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int lock(Object path, String opt) {
		return lock(rootSection, path, opt);
	}
	
	/**
	 * �г���ǰ·���µ����ļ������س�����
	 * @param opt ѡ�d���г���Ŀ¼��w���������ļ���Ŀ¼ȫ���г���l���ȼ���
	 * @return Sequence
	 */
	public Sequence list(String opt) {
		return list(rootSection, opt);
	}
	
	/**
	 * �г�ָ��·���µ����ļ������س�����
	 * @param path ·����·������
	 * @param opt ѡ�d���г���Ŀ¼��w���������ļ���Ŀ¼ȫ���г���l���ȼ���
	 * @return Sequence
	 */
	public Sequence list(Object path, String opt) {
		return list(rootSection, path, opt);
	}
	
	/**
	 * ����ǰ��������
	 * @param opt ѡ�l���ȼ���
	 * @return Object
	 */
	public Object load(String opt) {
		return load(rootSection, opt);
	}
	
	/**
	 * ��ָ��·���ı�������
	 * @param path ·����·������
	 * @param opt ѡ�l���ȼ���
	 * @return Object
	 */
	public Object load(Object path, String opt) {
		return load(rootSection, path, opt);
	}
	
	/**
	 * ����·���������ύʱ��
	 * @return Timestamp
	 */
	public Timestamp date() {
		return date(rootSection);
	}
	
	/**
	 * ������ǰ·�������а���ָ���ֶεı�������
	 * @param fields �ֶ�������
	 * @return ���
	 */
	public Table importTable(String []fields) {
		return importTable(rootSection, fields);
	}
	
	 /**
	  *  ������ǰ·�������а���ָ���ֶεı�������
	  * @param fields �ֶ�������
	  * @param filters ���˱��ʽ����
	  * @param ctx ����������
	  * @return ���
	  */
	public Table importTable(String []fields, Expression []filters, Context ctx) {
		return importTable(rootSection, fields, filters, ctx);
	}
	
	IVS home(ISection section, Object path) {
		checkValid();

		ISection sub;
		if (path instanceof Sequence) {
			sub = section.getSub(this, (Sequence)path);
		} else {
			sub = section.getSub(this, path);
		}

		if (sub != null) {
			return new VS(this, sub);
		} else {
			return null;
		}
	}
	
	Object load(ISection section, String opt) {
		checkValid();
		
		try {
			return section.load(this, opt);
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		}
	}
	
	Object load(ISection section, Object path, String opt) {
		checkValid();
		
		try {
			return section.load(this, path, opt);
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		}
	}
	
	Timestamp date(ISection section) {
		checkValid();
		return new Timestamp(section.getCommitTime());
	}

	Table importTable(ISection section, String []fields) {
		checkValid();
		
		try {
			return section.importTable(this, fields);
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		}
	}
	
	Table importTable(ISection section, String []fields, Expression []filters, Context ctx) {
		checkValid();
		
		try {
			return section.importTable(this, fields, filters, ctx);
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		}
	}
	
	int save(ISection section, Object value) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int result = section.save(this, value);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}

	int save(ISection section, Object value, Object path, Object name) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int result = section.save(this, value, path, name);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	int makeDir(ISection section, Object path, Object name) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int result = section.makeDir(this, path, name);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	int lock(ISection section, String opt) {
		checkValid();
		
		if (!isAutoCommit && error != S_SUCCESS) {
			return error;
		}
		
		if (opt == null || opt.indexOf('u') == -1) {
			return section.lockForWrite(this);
		} else {
			section.unlock(this);
			return S_SUCCESS;
		}
	}
	
	int lock(ISection section, Object path, String opt) {
		checkValid();
		
		if (!isAutoCommit && error != S_SUCCESS) {
			return error;
		}
		
		if (path instanceof Sequence) {
			ISection sub = section.getSub(this, (Sequence)path);
			if (sub != null) {
				if (opt == null || opt.indexOf('u') == -1) {
					return sub.lockForWrite(this);
				} else {
					sub.unlock(this);
					return S_SUCCESS;
				}
			} else {
				setError(S_PATHNOTEXIST);
				return S_PATHNOTEXIST;
			}
		} else {
			ISection sub = section.getSub(this, path);
			if (sub != null) {
				if (opt == null || opt.indexOf('u') == -1) {
					return sub.lockForWrite(this);
				} else {
					sub.unlock(this);
					return S_SUCCESS;
				}
			} else {
				setError(S_PATHNOTEXIST);
				return S_PATHNOTEXIST;
			}
		}
	}
	
	Sequence list(ISection section, String opt) {
		checkValid();
		
		return section.list(this, opt);
	}
	
	Sequence list(ISection section, Object path, String opt) {
		checkValid();
		
		if (path instanceof Sequence) {
			ISection sub = section.getSub(this, (Sequence)path);
			if (sub != null) {
				return sub.list(this, opt);
			} else {
				return null;
			}
		} else {
			ISection sub = section.getSub(this, path);
			if (sub != null) {
				return sub.list(this, opt);
			} else {
				return null;
			}
		}
	}

	/**
	 * ���ѡ��Ϊ����ɾ���ڵ㣬���ѡ��Ϊ��e����ɾ�����µĿ��ӽڵ�
	 * @param opt e��ֻɾ�����µĿսڵ�
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int delete(String opt) {
		return delete(rootSection, opt);
	}
	
	/**
	 * ���ѡ��Ϊ����ɾ���ڵ㣬���ѡ��Ϊ��e����ɾ�����µĿ��ӽڵ�
	 * @param path ·����·������
	 * @param opt e��ֻɾ�����µĿսڵ�
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int delete(Object path, String opt) {
		ISection sub;
		if (path instanceof Sequence) {
			sub = rootSection.getSub(this, (Sequence)path);
		} else {
			sub = rootSection.getSub(this, path);
		}

		return delete(sub, opt);
	}

	/**
	 * ɾ�����
	 * @param paths
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int deleteAll(Sequence paths) {
		return deleteAll(rootSection, paths);
	}
	
	int deleteAll(ISection root, Sequence paths) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int len = paths.length();
		if (len == 0) {
			return S_SUCCESS;
		}
		
		int result = S_SUCCESS;
		for (int i = 1; i <= len; ++i) {
			Object path = paths.getMem(i);
			ISection sub;
			if (path instanceof Sequence) {
				sub = root.getSub(this, (Sequence)path);
			} else {
				sub = root.getSub(this, path);
			}
			
			if (sub != null) {
				result = sub.delete(this);
				if (result != S_SUCCESS) {
					break;
				}
			}
		}
		
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	int delete(ISection section, String opt) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		if (section == null) {
			//setError(S_PATHNOTEXIST);
			//return S_PATHNOTEXIST;
			return S_SUCCESS; // Դ·�������ڲ��ٷ��ش��󣬷���dfxҪ���ж�
		}
		
		int result = VDB.S_SUCCESS;
		if (opt == null || opt.indexOf('e') == -1) {
			result = section.delete(this);
		} else {
			section.deleteNullSection(this);
		}
		
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * �ƶ�Ŀ¼��ָ��Ŀ¼
	 * @param srcPath Դ·����·������
	 * @param destPath Ŀ��·����·������
	 * @param name Ŀ��·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int move(Object srcPath, Object destPath, Object name) {
		return move(rootSection, srcPath, destPath, name);
	}
	
	int move(ISection section, Object srcPath, Object destPath, Object name) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}

		ISection sub = null;;
		if (srcPath instanceof Sequence) {
			sub = section.getSubForMove(this, (Sequence)srcPath);
		} else if (srcPath != null){
			sub = section.getSubForMove(this, srcPath);
		}

		if (sub == null) {
			//setError(S_PATHNOTEXIST);
			//return S_PATHNOTEXIST;
			return S_SUCCESS; // Դ·�������ڲ��ٷ��ش��󣬷���dfxҪ���ж�
		}
		
		ISection dest = null;
		Object value = null;
		if (destPath instanceof Sequence) {
			Sequence seq = (Sequence)destPath;
			int len = seq.length();
			if (len > 1) {
				Sequence tmp = new Sequence(len - 1);
				for (int i = 1; i < len; ++i) {
					tmp.add(seq.getMem(i));
				}
				
				int result = section.makeDir(this, tmp, name);
				if (result != S_SUCCESS) {
					return result;
				}
				
				dest = section.getSubForMove(this, tmp);
				value = seq.getMem(len);
			} else if (len == 1) {
				dest = sub.getParent();
				value = seq.getMem(1);
			} else {
				setError(S_PATHNOTEXIST);
				return S_PATHNOTEXIST;
			}
		} else if (destPath != null) {
			dest = sub.getParent();
			value = destPath;
		}
		
		if (dest == null) {
			setError(S_PATHNOTEXIST);
			return S_PATHNOTEXIST;
		} else if (!(dest instanceof Section)) {
			throw ArchiveSection.getModifyException();
		}
		
		int result = sub.move(this, (Section)dest, value);
		
		if (result != S_SUCCESS) {
			setError(result);
		}
		
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * �����ݿ��ж�ȡ�����ֶ�׷�ӵ�ָ��������
	 * @param seq ����
	 * @param pathExp ·�����ʽ
	 * @param fields Ҫ��ȡ���ֶ�������
	 * @param filter ���˱��ʽ
	 * @param ctx ����������
	 * @return ��������
	 */
	public Table read(Sequence seq, Expression pathExp, 
			String []fields, Expression filter, Context ctx) {
		return read(rootSection, seq, pathExp, fields, filter, ctx);
	}
	
	Table read(ISection root, Sequence seq, Expression pathExp, 
			String []fields, Expression filter, Context ctx) {
		if (fields == null) {
			return read(root, seq, pathExp, filter, ctx);
		}
		
		checkValid();
		if (seq == null || seq.length() == 0) {
			return null;
		}
		
		DataStruct ds = seq.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = seq.new Current();
		stack.push(current);
		
		String []srcFields = ds.getFieldNames();
		int srcCount = srcFields.length;
		int len = seq.length();		
		
		try {
			int count = fields.length;
			int totalCount = srcCount + count;
			String []totalFields = new String[totalCount];
			System.arraycopy(srcFields, 0, totalFields, 0, srcCount);
			System.arraycopy(fields, 0, totalFields, srcCount, count);
			Table table = new Table(totalFields);
			
			for (int i = 1; i <= len; ++i) {
				Record sr = (Record)seq.getMem(i);
				current.setCurrent(i);
				Object path = pathExp.calculate(ctx);
				Object val = root.load(this, path, null);
				if (val instanceof Sequence) {
					Sequence data = (Sequence)val;
					if (filter != null) {
						data = (Sequence)data.select(filter, null, ctx);
					}

					int curLen = data.length();
					if (curLen > 0) {
						data = data.fieldsValues(fields);
						for (int j = 1; j <= curLen; ++j) {
							Record r = (Record)data.getMem(j);
							Record nr = table.newLast(sr.getFieldValues());
							nr.setStart(srcCount, r);
						}
					}
				} else if (val instanceof Record) {
					Record r = (Record)val;
					if (filter == null || Variant.isTrue(r.calc(filter, ctx))) {
						Record nr = table.newLast(sr.getFieldValues());
						for (int f = 0, j = srcCount; f < count; ++f, ++j) {
							nr.setNormalFieldValue(j, r.getFieldValue(fields[f]));
						}
					}
				}
			}
			
			return table;
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		} finally {
			stack.pop();
		}
	}
	
	Table read(ISection root, Sequence seq, Expression pathExp, Expression filter, Context ctx) {
		checkValid();
		if (seq == null || seq.length() == 0) {
			return null;
		}
		
		DataStruct ds = seq.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = seq.new Current();
		stack.push(current);
		
		String []srcFields = ds.getFieldNames();
		int srcCount = srcFields.length;
		int len = seq.length();		
		
		try {
			Table table = null;
			DataStruct deriveDs = null;
			
			for (int i = 1; i <= len; ++i) {
				Record sr = (Record)seq.getMem(i);
				current.setCurrent(i);
				Object path = pathExp.calculate(ctx);
				Object val = root.load(this, path, null);
				if (val instanceof Sequence) {
					Sequence data = (Sequence)val;
					if (filter != null) {
						data = (Sequence)data.select(filter, null, ctx);
					}
					
					int curLen = data.length();
					if (curLen > 0) {
						DataStruct curDs = data.dataStruct();
						if (curDs == null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needPurePmt"));
						}
						
						if (deriveDs == null) {
							deriveDs = curDs;
							String []fields = deriveDs.getFieldNames();
							int count = fields.length;
							int totalCount = srcCount + count;
							String []totalFields = new String[totalCount];
							System.arraycopy(srcFields, 0, totalFields, 0, srcCount);
							System.arraycopy(fields, 0, totalFields, srcCount, count);
							table = new Table(totalFields);
						} else if (deriveDs.getFieldCount() != curDs.getFieldCount()) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.dsNotMatch"));
						}
						
						for (int j = 1; j <= curLen; ++j) {
							Record r = (Record)data.getMem(j);
							Record nr = table.newLast(sr.getFieldValues());
							nr.setStart(srcCount, r);
						}
					}
				} else if (val instanceof Record) {
					Record r = (Record)val;
					if (filter == null || Variant.isTrue(r.calc(filter, ctx))) {
						if (deriveDs == null) {
							deriveDs = r.dataStruct();
							String []fields = deriveDs.getFieldNames();
							int count = fields.length;
							int totalCount = srcCount + count;
							String []totalFields = new String[totalCount];
							System.arraycopy(srcFields, 0, totalFields, 0, srcCount);
							System.arraycopy(fields, 0, totalFields, srcCount, count);
							table = new Table(totalFields);
						} else if (deriveDs.getFieldCount() != r.getFieldCount()) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.dsNotMatch"));
						}
						
						Record nr = table.newLast(sr.getFieldValues());
						nr.setStart(srcCount, r);
					}
				}
			}
			
			return table;
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		} finally {
			stack.pop();
		}
	}
	
	/**
	 * �����е�ָ���ֶ�д�뵽��
	 * @param seq ����
	 * @param pathExp ·�����ʽ
	 * @param fieldExps �ֶ�ֵ���ʽ����
	 * @param fields �ֶ�������
	 * @param filter ���˱��ʽ
	 * @param ctx ����������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int write(Sequence seq, Expression pathExp, Expression []fieldExps, 
			String []fields, Expression filter, Context ctx) {
		return write(rootSection, seq, pathExp, fieldExps, fields, filter, ctx);
	}
	
	int write(ISection root, Sequence seq, Expression pathExp, 
			Expression []fieldExps, String []fields, Expression filter, Context ctx) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		if (seq == null || seq.length() == 0) {
			return S_SUCCESS;
		}
		
		int result = S_SUCCESS;
		ComputeStack stack = ctx.getComputeStack();
		Current current = seq.new Current();
		stack.push(current);

		try {
			int len = seq.length();
			for (int i = 1; i <= len; ++i) {
				current.setCurrent(i);
				Object path = pathExp.calculate(ctx);
				ISection sub;
				if (path instanceof Sequence) {
					sub = root.getSub(this, (Sequence)path);
				} else {
					sub = root.getSub(this, path);
				}
				
				if (sub == null) {
					continue;
				}
				
				Object value = sub.load(this, null);
				boolean isModified = false;
				if (value instanceof Sequence) {
					Sequence data = (Sequence)value;
					if (fieldExps == null) {
						// ɾ������������
						value = data.select(filter, "x", ctx);
						if (data.length() != ((Sequence)value).length()) {
							isModified = true;
						}
					} else {
						// �޸�����������
						if (filter != null) {
							data = (Sequence)data.select(filter, null, ctx);
						}
						
						if (data.length() > 0) {
							data.modifyFields(fieldExps, fields, ctx);
							isModified = true;
						}
					}
				} else if (value instanceof Record) {
					Record r = (Record)value;
					if (fieldExps == null) {
						// ɾ������������
						if (Variant.isTrue(r.calc(filter, ctx))) {
							value = null;
							isModified = true;
						}
					} else {
						if (filter == null || Variant.isTrue(r.calc(filter, ctx))) {
							r.modify(fieldExps, fields, ctx);
							isModified = true;
						}
					}
				}
				
				if (isModified) {
					sub.save(this, value);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
		} finally {
			stack.pop();
		}
		
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * ����������������
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ���飬���ڹ���
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fields ������Ҫ�����ֶ�������
	 * @param filter ��������
	 * @param opt ѡ�r����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return ���������
	 */
	public Sequence retrieve(String []dirNames, Object []dirValues, boolean []valueSigns, 
			String []fields, Expression filter, String opt, Context ctx) {
		return retrieve(rootSection, dirNames, dirValues, valueSigns, fields, filter, opt, ctx);
	}
	
	Sequence retrieve(ISection section, String []dirNames, Object []dirValues, boolean []valueSigns, 
			String []fields, Expression filter, String opt, Context ctx) {
		checkValid();
		boolean isRecursion = opt != null && opt.indexOf('r') != -1;
		
		try {
			return section.retrieve(this, dirNames, dirValues, valueSigns, fields, filter, isRecursion, ctx);
		} catch (IOException e) {
			e.printStackTrace();
			setError(S_IOERROR);
			return null;
		}
	}
	
	/**
	 * �ҳ����������ĵ��ݺ��д���ݵ��ֶ�ֵ
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ���飬���ڹ���
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fvals �����е��ֶ�ֵ����
	 * @param fields �����е��ֶ�������
	 * @param filter ��������
	 * @param opt ѡ�r����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int update(String []dirNames, Object []dirValues, boolean []valueSigns, 
			Object []fvals, String []fields, Expression filter, String opt, Context ctx) {
		return update(rootSection, dirNames, dirValues, valueSigns, fvals, fields, filter, opt, ctx);
	}
	
	int update(ISection section, String []dirNames, Object []dirValues, boolean []valueSigns, 
			Object []fvals, String []fields, Expression filter, String opt, Context ctx) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		boolean isRecursion = opt != null && opt.indexOf('r') != -1;
		int result = section.update(this, dirNames, dirValues, valueSigns, fvals, fields, filter, isRecursion, ctx);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}

	/**
	 * ���渽����ͨ����ͼƬ
	 * @param oldValues ��һ�ε��ôκ����ķ���ֵ
	 * @param newValues �޸ĺ��ֵ
	 * @param path ·����·������
	 * @param name ·������·��������
	 * @return ֵ���У�������һ�ε��ô˺���
	 */
	public Sequence saveBlob(Sequence oldValues, Sequence newValues, Object path, String name) {
		return saveBlob(rootSection, oldValues, newValues, path, name);
	}
	
	Sequence saveBlob(ISection section, Sequence oldValues, Sequence newValues, Object path, String name) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return null;
		}
		
		if (path != null) {
			if (path instanceof Sequence) {
				section = section.getSub(this, (Sequence)path);
			} else {
				section = section.getSub(this, path);
			}
			
			if (section == null) {
				setError(S_PATHNOTEXIST);
				return null;
			}
		}
		
		Sequence result = section.saveBlob(this, oldValues, newValues, name);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * ��������·����
	 * @param ·����·������
	 * @param ·������·��������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int rename(Object path, String name) {
		return rename(rootSection, path, name);
	}
	
	int rename(ISection section, Object path, String name) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int result = section.rename(this, path, name);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * �鵵ָ��·�����鵵��·��������д��ռ�õĿռ���С����ѯ�ٶȻ���
	 * @param path ·����·������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int archive(Object path) {
		return archive(rootSection, path);
	}
	
	int archive(ISection section, Object path) {
		if (section instanceof ArchiveSection) {
			return S_ACHEIVED;
		}

		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		int result = ((Section)section).archive(this, path);
		if (isAutoCommit()) {
			commit();
		}
		
		return result;
	}
	
	/**
	 * ����·����ָ��·����
	 * @param destPath Ŀ��·����·������
	 * @param destName Ŀ��·������·��������
	 * @param src Դ���ݿ�����
	 * @param srcPath Դ·����·������
	 * @return �ɹ���VDB.S_SUCCESS��������ʧ��
	 */
	public int copy(Object destPath, Object destName, IVS src, Object srcPath) {
		return copy(rootSection, destPath, destName, src, srcPath);
	}
	
	int copy(ISection destHome, Object destPath, Object destName, IVS src, Object srcPath) {
		checkValid();
		
		if (isAutoCommit) {
			resetError();
		} else if (error != S_SUCCESS) {
			return error;
		}
		
		if (!(destHome instanceof Section)) {
			throw ArchiveSection.getModifyException();
		}
		
		if (destPath != null) {
			int state = ((Section)destHome).makeDir(this, destPath, destName);
			if (state == S_SUCCESS) {
				if (destPath instanceof Sequence) {
					destHome = destHome.getSub(this, (Sequence)destPath);
				} else {
					destHome = destHome.getSub(this, destPath);
				}
				
				if (!(destHome instanceof Section)) {
					throw ArchiveSection.getModifyException();
				}
			} else {
				if (isAutoCommit()) {
					commit();
				}
				
				return state;
			}
		}

		Section destSection = (Section)destHome;
		if (srcPath != null) {
			src = src.home(srcPath);
		}
		
		VDB srcVdb = src.getVDB();
		if (srcVdb == this) {
			IDir srcDir = src.getHome().getDir();			
			IDir destDir = destSection.getDir();
			if (srcDir.getParent().getDir() == destDir) {
				// ͬһ��Ŀ¼����Ҫ����
				return S_SUCCESS;
			} else {
				// ���Դ·���ǲ���Ŀ��·���ĸ�
				while (destDir != null) {
					if (destDir == srcDir) {
						setError(S_PARAMERROR);
						if (isAutoCommit()) {
							commit();
						}
						
						return S_PARAMERROR;
					}
					
					destDir = destDir.getParent().getDir();
				}
			}
		}
		
		int state = destSection.copy(this, srcVdb, src.getHome());
		
		if (isAutoCommit()) {
			commit();
		}
		
		return state;
	}
}