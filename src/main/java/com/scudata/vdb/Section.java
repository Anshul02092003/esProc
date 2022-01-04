package com.scudata.vdb;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ���ݿ���Ľ�
 * @author WangXiaoJun
 *
 */
class Section extends ISection {
	private int header; // �׿��
	private volatile HeaderBlock headerBlock; // �׿����ݣ�ʵ���õ�ʱ��������
	
	private Dir dir; // �ڶ�Ӧ��Ŀ¼��Ϣ
	private volatile VDB lockVDB; // ������ǰ�ڵ��߼���
	private boolean isModified; // �Ƿ��޸���
	
	// �����½�
	public Section(Dir dir) {
		headerBlock = new HeaderBlock();
		this.dir = dir;
	}
	
	public Section(Dir dir, int header, byte []bytes) throws IOException {
		this.dir = dir;
		this.header = header;
		headerBlock = new HeaderBlock();
		headerBlock.read(bytes, this);
	}

	// �����ݿ��ȡ��
	/*public Section(Library library, int header, Dir dir) {
		this.header = header;
		this.dir = dir;
		headerBlock = readHeaderBlock(library, header);
	}

	// ���׿�
	private HeaderBlock readHeaderBlock(Library library, int block) {
		try {
			HeaderBlock headerBlock = new HeaderBlock();
			byte []bytes = library.readBlocks(block);
			headerBlock.read(bytes, this);
			return headerBlock;
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}*/
	
	public void setHeader(int header) {
		this.header = header;
	}
	
	/**
	 * ȡ�ڶ�Ӧ��·������
	 * @return
	 */
	public IDir getDir() {
		return dir;
	}
	
	/**
	 * ���ش˽��Ƿ��б�
	 * @return
	 */
	public boolean isFile() {
		return headerBlock.isFile();
	}
	
	/**
	 * ���ش˽��Ƿ���·�������Ƿ�����
	 * @return
	 */
	public boolean isDir() {
		return headerBlock.isDir();
	}
	
	private boolean isNullSection() {
		return headerBlock.isNullSection();
	}
	
	private boolean isLockVDB(VDB vdb) {
		return lockVDB == vdb;
	}
	
	/**
	 * ȡ�ڵĸ���
	 * @return Section
	 */
	public Section getParentSection() {
		if (dir == null) {
			return null;
		} else {
			return dir.getParentSection();
		}
	}

	/**
	 * ȡ�ӽ�
	 * @param vdb ���ݿ����
	 * @param path ��·��ֵ
	 * @return �ӽ�
	 */
	public ISection getSub(VDB vdb, Object path) {
		if (headerBlock.isKeySection()) {
			Dir dir = headerBlock.getSubKeyDir(path);
			ISection section = dir.getLastZone().getSection(vdb.getLibrary(), dir);
			return section.getSub(vdb, path);
		}
		
		Dir subDir = headerBlock.getSubDir(path);
		if (subDir == null) {
			return null;
		}
		
		DirZone zone = subDir.getZone(vdb, isLockVDB(vdb));
		if (zone == null) {
			return null;
		}
		
		return zone.getSection(vdb.getLibrary(), subDir);
	}
	
	// �������һ��Section
	private Section getSubForWrite(VDB vdb, Sequence paths, Sequence names) {
		int pcount = paths.length();
		int diff = pcount;
		
		// names�ĳ��ȿ��Ա�paths�Σ��Ӻ���ǰ��Ӧ
		if (names != null) {
			diff -= names.length();
		}
		
		Section sub = this;
		for (int i = 1; i <= pcount; ++i) {
			int index = i - diff;
			String name = index > 0 ? (String)names.getMem(index) : null;
			
			sub = sub.getSubForWrite(vdb, paths.getMem(i), name);
			if (sub == null) {
				return null;
			}
		}
		
		return sub;
	}
		
	// д��ʱ������λ���������µģ��������û��ϵ
	// �������������޸�·���͵ȴ�
	// ���·���������򴴽�����ס�����û���޸�·��isLockָʾ�Ƿ����
	private Section getSubForWrite(VDB vdb, Object path, String name) {
		if (headerBlock.isKeySection()) {
			Dir dir = headerBlock.getSubKeyDir(path);
			Section section = dir.getLastZone().getSectionForWrite(vdb.getLibrary(), dir);
			return section.getSubForWrite(vdb, path, name);
		}
		
		int result = lock(vdb);
		if (result == VDB.S_LOCKTIMEOUT) {
			return null;
		}
		
		try {
			Library library = vdb.getLibrary();
			Dir subDir = headerBlock.getSubDir(path);
			if (subDir == null) {
				if (result != VDB.S_LOCKTWICE) {
					vdb.addModifySection(this);
				}
				
				isModified = true;
				subDir = headerBlock.createSubDir(path, name, this);
				DirZone zone = subDir.getLastZone();
				return zone.getSectionForWrite(library, subDir);
			}
			
			DirZone zone = subDir.getLastZone();
			if (zone == null || !zone.valid()) {
				if (result != VDB.S_LOCKTWICE) {
					vdb.addModifySection(this);
				}

				isModified = true;
				zone = subDir.addZone(Dir.S_NORMAL);
				return zone.getSectionForWrite(library, subDir);
			} else {
				if (result != VDB.S_LOCKTWICE) {
					unlock();
				}
				
				return zone.getSectionForWrite(library, subDir);
			}
		} catch (Exception e) {
			unlock();
			return null;
		}
	}
	
	/**
	 * ȡ�ӽ��������ƶ�����
	 * @param vdb ���ݿ����
	 * @param path ��·��ֵ
	 * @return �ӽ�
	 */
	public Section getSubForMove(VDB vdb, Object path) {
		if (headerBlock.isKeySection()) {
			Dir dir = headerBlock.getSubKeyDir(path);
			DirZone zone = dir.getLastZone();
			Section section = zone.getSectionForWrite(vdb.getLibrary(), dir);
			return section.getSubForMove(vdb, path);
		}
		
		Dir subDir = headerBlock.getSubDir(path);
		if (subDir == null) {
			return null;
		}
		
		DirZone zone = subDir.getLastZone();
		if (zone == null || !zone.valid()) {
			return null;
		} else {
			return zone.getSectionForWrite(vdb.getLibrary(), subDir);
		}
	}
	
	/**
	 * ȡ�ӽ��������ƶ�����
	 * @param vdb ���ݿ����
	 * @param paths ��·��ֵ����
	 * @return �ӽ�
	 */
	public Section getSubForMove(VDB vdb, Sequence paths) {
		Section sub = this;
		for (int i = 1, pcount = paths.length(); i <= pcount; ++i) {
			sub = sub.getSubForMove(vdb, paths.getMem(i));
			if (sub == null) {
				return null;
			}
		}
		
		return sub;
	}
		
	/**
	 * ��ס�����ܲ��޸�Ȼ�����
	 * @param vdb
	 * @return
	 */
	public synchronized int lock(VDB vdb) {
		if (lockVDB == null) {
			lockVDB = vdb;
			return VDB.S_SUCCESS;
		} else if (lockVDB != vdb) {
			try {
				wait(Library.MAXWAITTIME);
				if (lockVDB != null) { // ��ʱ
					vdb.setError(VDB.S_LOCKTIMEOUT);
					return VDB.S_LOCKTIMEOUT;
				} else {
					lockVDB = vdb;
					return VDB.S_SUCCESS;
				}
			} catch (InterruptedException e) {
				vdb.setError(VDB.S_LOCKTIMEOUT);
				return VDB.S_LOCKTIMEOUT;
			}
		} else {
			return VDB.S_LOCKTWICE;
		}
	}
	
	/**
	 * ��ס���޸�
	 */
	public synchronized int lockForWrite(VDB vdb) {
		if (lockVDB == null) {
			lockVDB = vdb;
			vdb.addModifySection(this);
			return VDB.S_SUCCESS;
		} else if (lockVDB != vdb) {
			try {
				wait(Library.MAXWAITTIME);
				if (lockVDB != null) { // ��ʱ
					vdb.setError(VDB.S_LOCKTIMEOUT);
					return VDB.S_LOCKTIMEOUT;
				} else {
					lockVDB = vdb;
					vdb.addModifySection(this);
					return VDB.S_SUCCESS;
				}
			} catch (InterruptedException e) {
				vdb.setError(VDB.S_LOCKTIMEOUT);
				return VDB.S_LOCKTIMEOUT;
			}
		} else {
			return VDB.S_SUCCESS;
		}
	}
	
	/**
	 * ������ǰ��
	 */
	public synchronized void unlock() {
		isModified = false;
		lockVDB = null;
		notify();
	}
	
	/**
	 * ������ǰ��
	 * @param vdb ���ݿ����
	 */
	public synchronized void unlock(VDB vdb) {
		if (lockVDB == vdb) {
			if (isModified) {
				headerBlock.roolBack(vdb.getLibrary());
			}

			isModified = false;
			lockVDB = null;
			notify();
		}
	}

	/**
	 * �ع���ǰ�����������޸�
	 * @param library ���ݿ����
	 */
	public void rollBack(Library library) {
		if (isModified) {
			headerBlock.roolBack(library);
		}
		
		unlock();
	}
	
	/**
	 * �г���ǰ�������е����ļ���
	 * @param vdb ���ݿ����
	 * @param opt d���г���Ŀ¼�ڣ�w���������ļ���Ŀ¼ȫ���г���l��������ǰ��
	 * @return �ӽ�����
	 */
	public Sequence list(VDB vdb, String opt) {
		Dir []dirs = headerBlock.getSubDirs();
		if (dirs == null) return null;
		
		int size = dirs.length;
		Sequence seq = new Sequence(size);
		boolean isLockVDB = isLockVDB(vdb);
		Library library = vdb.getLibrary();
		
		boolean listFiles = true, listDirs = false;
		if (opt != null) {
			if (opt.indexOf('w') != -1) {
				listFiles = false;
			} else if (opt.indexOf('d') != -1) {
				listDirs = true;
				listFiles = false;
			}
			
			if (opt.indexOf('l') != -1) {
				lockForWrite(vdb);
			}
		}
		
		for (Dir dir : dirs) {
			DirZone zone = dir.getZone(vdb, isLockVDB);
			if (zone != null) {
				ISection section = zone.getSection(library, dir);
				if (listFiles) {
					if (section.isFile()) {
						seq.add(new VS(vdb, section));
					}
				} else if (listDirs) {
					if (section.isDir()) {
						seq.add(new VS(vdb, section));
					}
				} else {
					seq.add(new VS(vdb, section));
				}
			}
		}
		
		return seq;
	}

	/**
	 * ��ȡ��ǰ�ڵı�
	 * @param vdb ���ݿ����
	 * @param opt l��������ǰ��
	 * @return ������
	 * @throws IOException
	 */
	public Object load(VDB vdb, String opt) throws IOException {
		if (opt != null && opt.indexOf('l') != -1) {
			int result = lockForWrite(vdb);
			if (result == VDB.S_LOCKTIMEOUT) {
				return null;
			}
		}
		
		Zone zone = headerBlock.getFileZone(vdb, isLockVDB(vdb));
		if (zone != null) {
			return zone.getData(vdb.getLibrary());
		} else {
			return null;
		}
	}
	
	/**
	 * ����ֵ����ǰ����
	 * @param vdb ���ݿ����
	 * @param value ֵ��ͨ��������
	 * @return
	 */
	public int save(VDB vdb, Object value) {
		int result = lockForWrite(vdb);
		if (result == VDB.S_LOCKTIMEOUT) {
			return VDB.S_LOCKTIMEOUT;
		}
		
		isModified = true;
		Library library = vdb.getLibrary();
		int block = library.writeDataBlock(header, value);
		headerBlock.createFileZone(library, block);
		return VDB.S_SUCCESS;
	}

	/**
	 * ����ֵ���ӱ���
	 * @param vdb ���ݿ����
	 * @param value ֵ��ͨ��������
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	public int save(VDB vdb, Object value, Object path, Object name) {
		Section sub;
		if (path instanceof Sequence) {
			Sequence paths = (Sequence)path;
			Sequence names = null;
			if (name instanceof Sequence) {
				names = (Sequence)name;
				for (int i = 1, len = names.length(); i <= len; ++i) {
					Object obj = names.getMem(i);
					if (!(obj instanceof String)) {
						vdb.setError(VDB.S_PARAMTYPEERROR);
						return VDB.S_PARAMTYPEERROR;
					}
				}
			} else if (name instanceof String) {
				names = new Sequence(1);
				names.add(names);
			} else if (name != null) {
				vdb.setError(VDB.S_PARAMTYPEERROR);
				return VDB.S_PARAMTYPEERROR;
			}
			
			sub = getSubForWrite(vdb, paths, names);
		} else {
			if (name != null && !(name instanceof String)) {
				vdb.setError(VDB.S_PARAMTYPEERROR);
				return VDB.S_PARAMTYPEERROR;
			}
			
			sub = getSubForWrite(vdb, path, (String)name);
		}
		
		if (sub != null) {
			return sub.save(vdb, value);
		} else {
			return VDB.S_LOCKTIMEOUT;
		}
	}
	
	/**
	 * ������·��
	 * @param vdb ���ݿ����
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	public int makeDir(VDB vdb, Object path, Object name) {
		Section sub;
		if (path instanceof Sequence) {
			Sequence paths = (Sequence)path;
			Sequence names = null;
			if (name instanceof Sequence) {
				names = (Sequence)name;
				for (int i = 1, len = names.length(); i <= len; ++i) {
					Object obj = names.getMem(i);
					if (!(obj instanceof String)) {
						vdb.setError(VDB.S_PARAMTYPEERROR);
						return VDB.S_PARAMTYPEERROR;
					}
				}
			} else if (name instanceof String) {
				names = new Sequence(1);
				names.add(name);
			} else if (name != null) {
				vdb.setError(VDB.S_PARAMTYPEERROR);
				return VDB.S_PARAMTYPEERROR;
			}
			
			sub = getSubForWrite(vdb, paths, names);
		} else {
			if (name != null && !(name instanceof String)) {
				vdb.setError(VDB.S_PARAMTYPEERROR);
				return VDB.S_PARAMTYPEERROR;
			}
			
			sub = getSubForWrite(vdb, path, (String)name);
		}
		
		if (sub != null) {
			return VDB.S_SUCCESS;
		} else {
			return VDB.S_LOCKTIMEOUT;
		}
	}
	
	/**
	 * ���������
	 * @param vdb ���ݿ����
	 * @param key ��������
	 * @param len ��ϣ����
	 * @return 0���ɹ�
	 */
	public int createSubKeyDir(VDB vdb, Object key, int len) {
		// ��ס���ύ�޸�
		int result = lockForWrite(vdb);
		if (result == VDB.S_LOCKTIMEOUT) {
			return result;
		}
		
		Library library = vdb.getLibrary();
		isModified = true;
		Dir subDir = headerBlock.createSubDir(key, null, this);
		DirZone zone = subDir.getLastZone();
		Section section = zone.getSectionForWrite(library, subDir);
		section.setKeySection(vdb, len);
		
		return VDB.S_SUCCESS;
	}
	
	/**
	 * ���渽����ͨ����ͼƬ
	 * @param vdb ���ݿ����
	 * @param oldValues ��һ�ε��ôκ����ķ���ֵ
	 * @param newValues �޸ĺ��ֵ
	 * @param name ����
	 * @return ֵ���У�������һ�ε��ô˺���
	 */
	public Sequence saveBlob(VDB vdb, Sequence oldValues, Sequence newValues, String name) {
		ArrayList<Object> deleteList = new ArrayList<Object>();
		ArrayList<Object> addDirs = new ArrayList<Object>();
		ArrayList<Object> addBlobs = new ArrayList<Object>();
		Sequence result = null;
		
		if (newValues == null || newValues.length() == 0) {
			if (oldValues != null && oldValues.length() > 0) {
				int len = oldValues.length();
				for (int i = 1; i <= len; ++i) {
					Object val = oldValues.getMem(i);
					if (val instanceof Integer) {
						deleteList.add(val);
					} else {
						vdb.setError(VDB.S_PARAMTYPEERROR);
						return null;
					}
				}
			} else {
				return new Sequence(0);
			}
		} else {
			int len = newValues.length();
			result = new Sequence(len);
			try {
				if (oldValues == null || oldValues.length() == 0) {
					for (int i = 1; i <= len; ++i) {
						Object val = newValues.getMem(i);
						if (val instanceof String) {
							FileObject fo = new FileObject((String)val);
							val = fo.read(0, -1, "b");
							addBlobs.add(val);
							addDirs.add(i);
							result.add(i);
						} else {
							vdb.setError(VDB.S_PARAMTYPEERROR);
							return null;
						}
					}
				} else {
					int max = Integer.MIN_VALUE;
					for (int i = 1, size = oldValues.length(); i <= size; ++i) {
						Object val = oldValues.getMem(i);
						if (!(val instanceof Integer)) {
							vdb.setError(VDB.S_PARAMTYPEERROR);
							return null;
						}
						
						deleteList.add(val);
						int n = ((Integer)val).intValue();
						if (max < n) {
							max = n;
						}
					}

					for (int i = 1; i <= len; ++i) {
						Object val = newValues.getMem(i);
						if (val instanceof String) {
							max++;
							FileObject fo = new FileObject((String)val);
							val = fo.read(0, -1, "b");
							addBlobs.add(val);
							addDirs.add(max);
							result.add(max);
						} else if (val instanceof Integer) {
							if (!deleteList.remove(val)) {
								vdb.setError(VDB.S_PARAMERROR);
								return null;
							}
							
							result.add(val);
						} else {
							vdb.setError(VDB.S_PARAMTYPEERROR);
							return null;
						}
					}
				}
			} catch (Exception e) {
				vdb.setError(VDB.S_IOERROR);
				return null;
			}
		}
		
		int deleteSize = deleteList.size();
		int addSize = addBlobs.size();
		if (deleteSize == 0 && addSize == 0) {
			return result;
		}
		
		int state = lockForWrite(vdb);
		if (state == VDB.S_LOCKTIMEOUT) {
			return null;
		}
		
		for (Object p : deleteList) {
			ISection sub = getSub(vdb, p);
			if (sub != null) {
				sub.delete(vdb);
			}
		}
		
		for (int i = 0; i < addSize; ++i) {
			Object p = addDirs.get(i);
			Object v = addBlobs.get(i);
			Section sub = getSubForWrite(vdb, p, name);
			if (sub != null) {
				sub.save(vdb, v);
			} else {
				return null;
			}
		}
		
		return result;
	}
	
	private void setKeySection(VDB vdb, int len) {
		isModified = true;
		headerBlock.setKeySection(vdb, this, len);
	}
		
	protected void importTable(VDB vdb, Table table, Object []values, boolean []signs, 
			Expression filter, int []filterIndex, Context ctx) throws IOException {
		boolean isLockVDB = isLockVDB(vdb);
		Library library = vdb.getLibrary();
		Zone zone = headerBlock.getFileZone(vdb, isLockVDB);
		
		if (zone != null) {
			Object data = zone.getData(library);
			addDataToTable(table, values, signs, data, filter, ctx);
			// ����е���������ѡ���ֶ����ٱ�����
			//if (isAdded) {
			//	return;
			//}
		}
		
		Dir[] subDirs = headerBlock.getSubDirs();
		if (subDirs == null) return;
		
		DataStruct ds = table.dataStruct();
		ISection sub;
		
		for (Dir dir : subDirs) {
			DirZone dirZone = dir.getZone(vdb, isLockVDB);
			if (dirZone != null && (sub = dirZone.getSection(library, dir)) != null) {
				int findex = ds.getFieldIndex(dir.getName());
				if (findex != -1) {
					values[findex] = dir.getValue();
					signs[findex] = true;
					
					boolean isAll = true;
					for (int index : filterIndex) {
						if (!signs[index]) {
							isAll = false;
							break;
						}
					}
					
					if (isAll) {
						Record r = new Record(ds, values);
						Object result = r.calc(filter, ctx);
						if (!(result instanceof Boolean)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needBoolExp"));
						}
						
						if ((Boolean)result) {
							sub.importTable(vdb, table, values, signs);
						}				
					} else {
						sub.importTable(vdb, table, values, signs, filter, filterIndex, ctx);
					}
					
					values[findex] = null;
					signs[findex] = false;
				} else {
					sub.importTable(vdb, table, values, signs, filter, filterIndex, ctx);
				}
			}
		}
	}
	
	protected void importTable(VDB vdb, Table table, Object []values, boolean []signs) throws IOException {
		boolean isLockVDB = isLockVDB(vdb);
		Library library = vdb.getLibrary();
		Zone zone = headerBlock.getFileZone(vdb, isLockVDB);
		
		if (zone != null) {
			Object data = zone.getData(library);
			addDataToTable(table, values, signs, data);
			// ����е���������ѡ���ֶ����ٱ�����
			//if (isAdded) {
			//	return;
			//}
		}
		
		Dir[] subDirs = headerBlock.getSubDirs();
		if (subDirs == null) return;
		
		DataStruct ds = table.dataStruct();
		ISection sub;
		
		for (Dir dir : subDirs) {
			DirZone dirZone = dir.getZone(vdb, isLockVDB);
			if (dirZone != null && (sub = dirZone.getSection(library, dir)) != null) {
				int findex = ds.getFieldIndex(dir.getName());
				if (findex != -1) {
					values[findex] = dir.getValue();
					signs[findex] = true;
					
					sub.importTable(vdb, table, values, signs);
					values[findex] = null;
					signs[findex] = false;
				} else {
					sub.importTable(vdb, table, values, signs);
				}
			}
		}
	}
	
	protected void retrieve(VDB vdb, Filter filter, boolean isRecursion, Sequence out) throws IOException {
		if (filter.isDirMatch()) {
			boolean isLockVDB = isLockVDB(vdb);
			Library library = vdb.getLibrary();
			Zone zone = headerBlock.getFileZone(vdb, isLockVDB);
			
			if (zone != null) {
				Object data = zone.getData(library);
				Sequence seq = filter.select(data);
				if (seq != null) {
					out.addAll(seq);
				}

				// ����е���������ѡ���ֶ����ٱ����ӣ�
			}
			
			// ���ݹ���������ӽڵ�
			if (!isRecursion) {
				return;
			}
		}
		
		Dir[] subDirs = headerBlock.getSubDirs();
		if (subDirs == null) return;
		
		Library library = vdb.getLibrary();
		boolean isLockVDB = isLockVDB(vdb);
		ISection sub;
		
		for (Dir dir : subDirs) {
			DirZone dirZone = dir.getZone(vdb, isLockVDB);
			if (dirZone != null && (sub = dirZone.getSection(library, dir)) != null) {
				if (filter.pushDir(dir.getName(), dir.getValue())) {
					sub.retrieve(vdb, filter, isRecursion, out);
					filter.popDir();
				}
			}
		}
	}
	
	/**
	 * ����ָ��������ֶ�ֵ
	 * @param vdb ���ݿ����
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ����
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fvals ����Ҫ�޸ĵ��ֶ�ֵ����
	 * @param fields ����Ҫ�޸ĵ��ֶ�������
	 * @param exp �������ʽ
	 * @param isRecursion true����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return 0���ɹ�
	 */
	public int update(VDB vdb, String []dirNames, Object []dirValues, boolean []valueSigns, 
			Object []fvals, String []fields, Expression exp, boolean isRecursion, Context ctx) {
		Filter filter = new Filter(dirNames, dirValues, valueSigns, exp, ctx);
		
		try {
			return update(vdb, filter, fvals, fields, isRecursion);
		} catch (IOException e) {
			return VDB.S_IOERROR;
		}
	}
	
	private int update(VDB vdb, Filter filter, Object []fvals, String []fields, boolean isRecursion) throws IOException {
		if (filter.isDirMatch()) {
			int result = lockForWrite(vdb);
			if (result == VDB.S_LOCKTIMEOUT) {
				return VDB.S_LOCKTIMEOUT;
			}

			Library library = vdb.getLibrary();
			Zone zone = headerBlock.getFileZone(vdb, true);
			
			if (zone != null) {
				Object data = zone.getData(library);
				if (filter.update(data, fvals, fields)) {
					int block = library.writeDataBlock(header, data);
					headerBlock.createFileZone(library, block);
					isModified = true;
				}
			}
			
			if (!isRecursion) {
				return VDB.S_SUCCESS;
			}
		}
		
		Dir[] subDirs = headerBlock.getSubDirs();
		if (subDirs == null) return VDB.S_SUCCESS;
		
		Library library = vdb.getLibrary();
		boolean isLockVDB = isLockVDB(vdb);
		Section sub;
		
		for (Dir dir : subDirs) {
			DirZone dirZone = dir.getZone(vdb, isLockVDB);
			if (dirZone != null && (sub = dirZone.getSectionForWrite(library, dir)) != null) {
				if (filter.pushDir(dir.getName(), dir.getValue())) {
					int result = sub.update(vdb, filter, fvals, fields, isRecursion);
					if (result != VDB.S_SUCCESS) {
						return result;
					}
					
					filter.popDir();
				}
			}
		}
		
		return VDB.S_SUCCESS;
	}
	
	/**
	 * ɾ����ǰ��
	 * @param vdb ���ݿ����
	 * @return 0���ɹ�
	 */
	public int delete(VDB vdb) {
		Section parent = getParentSection();
		if (parent != null) {
			int result = parent.lockForWrite(vdb);
			if (result == VDB.S_LOCKTIMEOUT) {
				vdb.setError(VDB.S_LOCKTIMEOUT);
				return VDB.S_LOCKTIMEOUT;
			}
			
			parent.deleteSubDir(vdb, dir);
			return VDB.S_SUCCESS;
		} else {
			int result = lockForWrite(vdb);
			if (result == VDB.S_LOCKTIMEOUT) {
				vdb.setError(VDB.S_LOCKTIMEOUT);
				return VDB.S_LOCKTIMEOUT;
			}
			
			deleteAllSubDirs(vdb);
			return VDB.S_SUCCESS;
		}
	}
	
	/**
	 * �������ݿ��ά����ִ�д˲���ʱӦ��ֹͣ����
	 * ����˽ڵ�Ϊ����ɾ��������ɾ���˽ڵ��µĿ��ӽڵ�
	 * @param vdb ���ݿ����
	 * @return true���п�Ŀ¼��ɾ����false��û��
	 */
	public boolean deleteNullSection(VDB vdb) {
		int result = lockForWrite(vdb);
		if (result == VDB.S_LOCKTIMEOUT) {
			// �ڵ��ڱ������޸�
			return false;
		}
		
		Dir []subDirs = headerBlock.getSubDirs();
		boolean isSubDelete = false;
		if (subDirs != null) {
			Library library = vdb.getLibrary();
			for (Dir dir : subDirs) {
				DirZone zone = dir.getLastZone();
				if (zone != null && zone.valid()) {
					ISection section = zone.getSection(library, dir);
					if (section.deleteNullSection(vdb)) {
						headerBlock.removeSubDir(dir);
						isSubDelete = true;
					}
					
					zone.releaseSection();
				}
			}
		}
		
		if (isNullSection()) {
			if (dir == null && isSubDelete) {
				isModified = true;
			} else {
				vdb.removeModifySection(this);
			}
			
			return true;
		} else {
			if (isSubDelete) {
				isModified = true;
			} else {
				vdb.removeModifySection(this);
			}
			
			return false;
		}
	}
	
	// ��Ҫ����ԴĿ¼����Ҫ�ж�Դ��û���ύ
	/**
	 * �ƶ���ǰ�ڵ�ָ��Ŀ¼��
	 * @param vdb ���ݿ����
	 * @param dest Ŀ��·��
	 * @param value �½�ֵ��ʡ����ԭ���Ľ�ֵ
	 * @return 0���ɹ�
	 */
	public int move(VDB vdb, Section dest, Object value) {
		Section parent = (Section)getParent();
		if (parent == null) {
			return VDB.S_PATHNOTEXIST;
		}
		
		//Object path = dir.getValue();
		if (value == null) {
			value = dir.getValue();
		}
		
		if (dest.getSub(vdb, value) != null) {
			return VDB.S_TARGETPATHEXIST;
		}
		
		if (lockForWrite(vdb) == VDB.S_LOCKTIMEOUT) {
			return VDB.S_LOCKTIMEOUT;
		}
		
		if (parent.lockForWrite(vdb) == VDB.S_LOCKTIMEOUT) {
			return VDB.S_LOCKTIMEOUT;
		}
		
		if (dest.lockForWrite(vdb) == VDB.S_LOCKTIMEOUT) {
			return VDB.S_LOCKTIMEOUT;
		}
		
		parent.moveSubDir(vdb, dir, this);
		Dir subDir = dest.createSubDir(vdb, value, dir.getName());
		
		if (header <= 0) {
			// ��û���ύ����ʱ�¸�·���������޸Ķ��еĺ��棬��Ҫ�������׿�
			header = vdb.getLibrary().applyHeaderBlock();
		}
		
		dir = subDir;
		DirZone zone = subDir.getLastZone();
		zone.setSection(header, this);
		
		return VDB.S_SUCCESS;
	}
		
	private Dir createSubDir(VDB vdb, Object path, String name) {
		isModified = true;
		return headerBlock.createSubDir(path, name, this);
	}
	
	private void deleteSubDir(VDB vdb, Dir dir) {
		isModified = true;
		headerBlock.deleteSubDir(dir);
	}
	
	private void moveSubDir(VDB vdb, Dir dir, ISection section) {
		isModified = true;
		headerBlock.moveSubDir(dir, section);
	}

	private void deleteAllSubDirs(VDB vdb) {
		isModified = true;
		headerBlock.deleteAllSubDirs();
	}
	
	/**
	 * ɾ����ʱ�Ĳ����ٱ����õ�����λ
	 * @param library ���ݿ����
	 * @param outerSeq ����
	 * @param txSeq �ڴ��
	 */
	public void deleteOutdatedZone(Library library, int outerSeq, long txSeq) {
		headerBlock.deleteOutdatedZone(library, outerSeq, txSeq);
	}
	
	/**
	 * �ύ����
	 * @param library ���ݿ����
	 * @param outerSeq ����
	 * @param innerSeq �ڴ��
	 * @throws IOException
	 */
	public void commit(Library library, int outerSeq, long innerSeq) throws IOException {
		// �ڿ��������������ؽ���û�ύ��ɾ���ˣ���ʱ���ύ��
		if (isModified && header > 0) {
			headerBlock.commit(library, header, outerSeq, innerSeq);
		}
		
		unlock();
	}
	
	/**
	 * ɨ���Ѿ�������飬���ڴ洢����
	 * @param library ���ݿ����
	 * @param manager �������
	 * @throws IOException
	 */
	public void scanUsedBlocks(Library library, BlockManager manager) throws IOException {
		manager.setBlockUsed(header);
		headerBlock.scanUsedBlocks(library, manager);
	}
	
	/**
	 * ȡ��ǰ�ڵ��ύʱ��
	 * @return ʱ��ֵ
	 */
	public long getCommitTime() {
		return headerBlock.getCommitTime();
	}
	
	/**
	 * ����������
	 * @param vdb ���ݿ����
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	public int rename(VDB vdb, Object path, String name) {
		Section section;
		if (path instanceof Sequence) {
			ISection sub = getSub(vdb, (Sequence)path);
			if (sub instanceof ArchiveSection) {
				throw ArchiveSection.getModifyException();
			}
			
			section = (Section)sub;
		} else if (path != null) {
			ISection sub = getSub(vdb, path);
			if (sub instanceof ArchiveSection) {
				throw ArchiveSection.getModifyException();
			}
			
			section = (Section)sub;
		} else {
			section = this;
		}
		
		if (section == null) {
			return VDB.S_SUCCESS;
		}
		
		Dir dir = section.dir;
		if (dir.isEqualName(name)) {
			return VDB.S_SUCCESS;
		}
		
		Section parent = dir.getParentSection();
		int result = parent.lockForWrite(vdb);
		if (result == VDB.S_LOCKTIMEOUT) {
			return result;
		}
		
		parent.isModified = true;
		dir.rename(name);
		return VDB.S_SUCCESS;
	}
	
	/**
	 * �Ƿ��Ǽ����
	 * @return
	 */
	public boolean isKeySection() {
		return headerBlock.isKeySection();
	}
	
	public ArrayList<ISection> getSubList(Library library) {
		return headerBlock.getSubList(library);
	}
	
	/**
	 * ��ָ���ڹ鵵
	 * @param vdb ���ݿ����
	 * @param path ·��
	 * @return
	 */
	public int archive(VDB vdb, Object path) {
		if (path instanceof Sequence) {
			Sequence seq = (Sequence)path;
			int len = seq.length();
			if (len == 0) {
				return VDB.S_PARAMERROR;
			}
			
			Section section = this;
			for (int i = 1; i < len; ++i) {
				ISection tmp = section.getSub(vdb, seq.getMem(i));
				if (tmp instanceof Section) {
					section = (Section)tmp;
				} else {
					return VDB.S_ACHEIVED;
				}
			}
			
			return section.archive(vdb, seq.getMem(len));
		}
		
		lockForWrite(vdb);
		Dir subDir = headerBlock.getSubDir(path);
		if (subDir == null) {
			return VDB.S_PATHNOTEXIST;
		}
		
		DirZone zone = subDir.getLastZone();
		if (zone == null || !zone.valid()) {
			return VDB.S_PATHNOTEXIST;
		}
		
		Library library = vdb.getLibrary();
		ISection sub = zone.getSection(library, subDir);
		if (!(sub instanceof Section)) {
			return VDB.S_ACHEIVED;
		}
		
		try {
			int subHeader = ArchiveSection.archive(vdb, (Section)sub);
			subDir.addZone(subHeader);
			isModified = true;
			return VDB.S_SUCCESS;
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	/**
	 * �ͷŻ���Ľڶ��������ͷ��ڴ�
	 */
	public void releaseSubSection() {
		headerBlock.releaseSubSection();
	}
	
	/**
	 * �������ݿ����ݵ��¿�
	 * @param srcLib Դ��
	 * @param destLib Ŀ���
	 * @param destHeader Ŀ����׿�
	 * @throws IOException
	 */
	public void reset(Library srcLib, Library destLib, int destHeader) throws IOException {
		headerBlock.reset(srcLib, destLib, destHeader);
	}
	
	public int copy(VDB vdb, VDB srcVdb, ISection srcSection) {
		IDir dir = srcSection.getDir();
		Section destSection = getSubForWrite(vdb, dir.getValue(), dir.getName());
		if (srcSection.isFile()) {
			try {
				Object value = srcSection.load(srcVdb, null);
				int state = destSection.save(vdb, value);
				if (state != VDB.S_SUCCESS) {
					return state;
				}
			} catch (IOException e) {
				e.printStackTrace();
				vdb.setError(VDB.S_IOERROR);
				return VDB.S_IOERROR;
			}
		}
		
		Sequence subs = srcSection.list(srcVdb, "w");
		if (subs != null) {
			for (int i = 1, size = subs.length(); i <= size; ++i) {
				IVS sub = (IVS)subs.getMem(i);
				int state = destSection.copy(vdb, srcVdb, sub.getHome());
				if (state != VDB.S_SUCCESS) {
					return state;
				}
			}
		}
		
		return VDB.S_SUCCESS;
	}
}
