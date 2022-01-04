package com.scudata.dw;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.common.MD5;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileGroup;
import com.scudata.dm.FileObject;
import com.scudata.dm.LongArray;
import com.scudata.dm.cursor.ICursor;
import com.scudata.resources.EngineMessage;
import com.scudata.util.FileSyncManager;

/**
 * �����
 * ������*1���ļ�ͷ���ļ���ʶ�������С������λ�á�����Ϣ������Ϣ���������ݽṹ���ֶ���Ϣ��������Ϣ�������¼�����޸ļ�¼����ɾ����¼��������ÿ�θ�����д
 * ������*T����ֿ���Ϣ��ÿ���п�ļ�¼����
 * ������*C���зֿ���Ϣ��������λ�á�ά�ֶ������Сֵ��
 * ������*C�������ݣ�������ѹ����
 * ������*T�����飬ÿ�θ�����д
 * @author runqian
 *
 */
abstract public class GroupTable implements IBlockStorage {
	// ���ļ���׺��_SF��Ϊ.ext
	public static final String SF_SUFFIX = ".ext"; //���ļ���׺
	
	protected File file;
	protected RandomAccessFile raf;
	protected TableMetaData baseTable;
	
	protected int blockSize; // ���С
	protected transient int enlargeSize; // �����ļ�ʱ������
	protected BlockLink headerBlockLink;
	
	protected byte []reserve = new byte[32]; // �������ֽ�1��Ű汾���ֽ�2����Ƿ�ѹ����0��ѹ����1����ѹ��
	protected long freePos = 0; // ����λ��
	protected long fileSize; // �ļ��ܴ�С
	
	// �����ѷ������ļ��б���������ԣ� ��дʱ������������
	protected String writePswHash; // д�����ϣֵ���汾1���
	protected String readPswHash; // �������ϣֵ���汾1���
	
	protected String distribute; // �ֲ����ʽ���汾2���

	//private transient boolean canWrite = true;
	//private transient boolean canRead = true;
	
	protected StructManager structManager; // ���е����ݽṹ
	protected transient Context ctx;
	
	private transient GroupTable sfGroupTable;
	private transient Integer partition; // ���ļ���������

	/**
	 * ������Ĳ��ļ�
	 * @param file ����ļ�
	 * @return
	 */
	public static File getSupplementFile(File file) {
		String pathName = file.getAbsolutePath();
		pathName += SF_SUFFIX;
		return new File(pathName);
	}

	/**
	 * ���Ƶ�ǰ���Ľṹ����������ļ�
	 * @param sf
	 * @return
	 */
	public GroupTable dupStruct(File sf) {
		checkWritable();
		GroupTable newGroupTable = null;
		
		try {
			//����������ļ�
			if (this instanceof ColumnGroupTable) {
				newGroupTable = new ColumnGroupTable(sf, (ColumnGroupTable)this);
			} else {
				newGroupTable = new RowGroupTable(sf, (RowGroupTable)this);
			}
		} catch (Exception e) {
			if (newGroupTable != null) newGroupTable.close();
			sf.delete();
			throw new RQException(e.getMessage(), e);
		}
		
		return newGroupTable;
	}
	
	/**
	 * �õ����ļ�����
	 * @param isCreate �Ƿ��½�
	 * @return
	 */
	public GroupTable getSupplement(boolean isCreate) {
		if (sfGroupTable == null) {
			File sf = getSupplementFile(file);
			if (sf.exists()) {
				try {
					sfGroupTable = open(sf, ctx);
					//sfGroupTable.canWrite = canWrite;
					//sfGroupTable.canRead = canRead;
				} catch (IOException e) {
					throw new RQException(e);
				}
			} else if (isCreate) {
				sfGroupTable = dupStruct(sf);
			}
		}
		
		return sfGroupTable;
	}

	/**
	 * ���Ѿ����ڵ����
	 * @param file
	 * @param ctx
	 * @return
	 * @throws IOException
	 */
	public static GroupTable open(File file, Context ctx) throws IOException {
		if (!file.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileNotExist", file.getAbsolutePath()));
		}
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			raf.seek(6);
			if (raf.read() == 'r') {
				return new RowGroupTable(file, ctx);
			} else {
				return new ColumnGroupTable(file, ctx);
			}
		} finally {
			raf.close();
		}
	}

	/**
	 * ���Ѿ����ڵ����,����������־�����ڲ�ʹ��
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static GroupTable createGroupTable(File file) throws IOException {
		if (!file.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileNotExist", file.getAbsolutePath()));
		}
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			raf.seek(6);
			if (raf.read() == 'r') {
				return new RowGroupTable(file);
			} else {
				return new ColumnGroupTable(file);
			}
		} finally {
			raf.close();
		}
	}
	
	/**
	 * �򿪻���
	 * @param file
	 * @param ctx
	 * @return
	 */
	public static TableMetaData openBaseTable(File file, Context ctx) {
		try {
			GroupTable groupTable = open(file, ctx);
			return groupTable.getBaseTable();
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ȡ����
	 * @return
	 */
	public TableMetaData getBaseTable() {
		return baseTable;
	}

	/**
	 * ���������С
	 * @param size
	 */
	protected void setBlockSize(int size) {
		blockSize = size;
		enlargeSize = size * 16;
	}
	
	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * ɾ������ļ�
	 */
	public void delete() {
		// ����f.create@y()
		checkWritable();
		GroupTable sgt = getSupplement(false);
		if (sgt != null) {
			sgt.delete();
		}
		
		TableMetaData table = getBaseTable();
		
		try {
			table.deleteIndex(null);
			table.deleteCuboid(null);
			ArrayList<TableMetaData> tables = table.getTableList();
			for (TableMetaData td : tables) {
				td.deleteIndex(null);
				td.deleteCuboid(null);
			}

			close();
			file.delete();
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	public void close() {
		try {
			baseTable.appendCache();
			ArrayList <TableMetaData> tables = baseTable.getTableList();
			for (TableMetaData table : tables) {
				table.appendCache();
			}
			
			raf.close();
			if (sfGroupTable != null) {
				sfGroupTable.close();
			}
		} catch (IOException e) {
			throw new RQException(e);
		}
	}

	protected abstract void readHeader() throws IOException;
	
	protected abstract void writeHeader() throws IOException;
	
	protected void flush() throws IOException {
		raf.getChannel().force(false);
	}
	
	void save() throws IOException {
		writeHeader();
		flush();
	}
	
	public int getBlockSize() {
		return blockSize;
	}
	
	/**
	 * ��ȡһ������
	 */
	public synchronized void loadBlock(long pos, byte []block) throws IOException {
		raf.seek(pos);
		raf.readFully(block);
	}

	public void saveBlock(long pos, byte []block) throws IOException {
		raf.seek(pos);
		raf.write(block);
	}
	
	public void saveBlock(long pos, byte []block, int off, int len) throws IOException {
		raf.seek(pos);
		raf.write(block, off, len);
	}
	
	/**
	 * ����һ���¿�
	 */
	public long applyNewBlock() throws IOException {
		long pos = freePos;
		if (pos >= fileSize) {
			enlargeFile();
		}
		
		freePos += blockSize;
		return pos;
	}
	
	private void enlargeFile() throws IOException {
		fileSize += enlargeSize;
		raf.setLength(fileSize);
	}

	public StructManager getStructManager() {
		return structManager;
	}
	
	int getDataStructID(DataStruct ds) {
		return structManager.getDataStructID(ds);
	}

	DataStruct getDataStruct(int id) {
		return structManager.getDataStruct(id);
	}
	
	public File getFile() {
		return file;
	}

	/**
	 * �������񱣻�
	 * �����Ĺؼ���Ϣ�ݴ浽��ʱ�ļ�
	 * @param table
	 * @throws IOException
	 */
	protected void beginTransaction(TableMetaData table) throws IOException {
		byte []bytes = new byte[blockSize];
		raf.seek(0);
		raf.readFully(bytes);
		byte []mac = MD5.get(bytes);
		
		String dir = file.getAbsolutePath() + "_TransactionLog";
		FileObject logFile = new FileObject(dir);
		logFile.delete();
		RandomAccessFile raf = new RandomAccessFile(logFile.getLocalFile().file(), "rw");
		raf.seek(0);
		raf.write(bytes);
		raf.write(mac);
		raf.getChannel().force(false);
		raf.close();
		
		//���Ÿ��µ�table name����Ϊ�˻ָ�����
		if (table != null) {
			if (table.indexNames == null)
				return;
			dir = file.getAbsolutePath() + "_I_TransactionLog";
			logFile = new FileObject(dir);
			logFile.delete();
			raf = new RandomAccessFile(logFile.getLocalFile().file(), "rw");
			raf.seek(0);
			raf.writeUTF(table.tableName);
			raf.getChannel().force(false);
			raf.close();
		}
	}
	
	/**
	 * �ύ����
	 * ɾ��������־�ļ�
	 * @param step 0��ɾ�������־��1��ɾ��������־
	 */
	protected void commitTransaction(int step) {
		if (step == 1) {
			String dir = file.getAbsolutePath() + "_I_TransactionLog";
			FileObject logFile = new FileObject(dir);
			logFile.delete();
		} else {
			String dir = file.getAbsolutePath() + "_TransactionLog";
			FileObject logFile = new FileObject(dir);
			logFile.delete();
		}
	}
	
	/**
	 * �ָ�����
	 * ��д���ʱ�����쳣�����ʹ������������ָ���д����֮ǰ��״̬
	 */
	protected void restoreTransaction() {
		String dir = file.getAbsolutePath() + "_TransactionLog";
		FileObject logFile = new FileObject(dir);
		if (logFile.isExists()) {
			//��Ҫ����rollback
			MessageManager mm = EngineMessage.get();
			throw new RQException(file.getName() + mm.getMessage("dw.needRollback"));
		}
		
		dir = file.getAbsolutePath() + "_I_TransactionLog";
		logFile = new FileObject(dir);
		if (logFile.isExists()) {
			//��Ҫ����rollback
			MessageManager mm = EngineMessage.get();
			throw new RQException(file.getName() + mm.getMessage("dw.needRollback"));
		}

	}
	
	/**
	 * �������
	 * @param file ����ļ�
	 * @param opt r,����Ϊ�д档c,����Ϊ�д档
	 * @param ctx
	 * @param distribute �µķֲ����ʽ��ʡ������ԭ����
	 * @return
	 */
	public boolean reset(File file, String opt, Context ctx, String distribute) {
		checkWritable();
		if (distribute == null) {
			distribute = this.distribute;
		}
		
		boolean isCol = this instanceof ColumnGroupTable;
		boolean hasQ = false;
		boolean hasN = false;
		boolean compress = false; // ѹ��
		boolean uncompress = false; // ��ѹ��
		
		if (opt != null) {
			if (opt.indexOf('r') != -1) {
				isCol = false;
			} else if (opt.indexOf('c') != -1) {
				isCol = true;
			}
			
			if (opt.indexOf('u') != -1) {
				uncompress = true;
			}
			
			if (opt.indexOf('z') != -1) {
				compress = true;
			}
			
			if (compress && uncompress) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(opt + mm.getMessage("engine.optConflict"));
			}
			
			if (opt.indexOf('q') != -1) {
				hasQ = true;
				if (file != null) {
					//��@qʱ������f'
					MessageManager mm = EngineMessage.get();
					throw new RQException("reset" + mm.getMessage("function.invalidParam"));
				}
			}
			
			if (opt.indexOf('n') != -1) {
				hasN = true;
				if (file == null) {
					//��@nʱ������f'
					MessageManager mm = EngineMessage.get();
					throw new RQException("reset" + mm.getMessage("function.invalidParam"));
				}
			}
		}
		
		GroupTable sgt = getSupplement(false);
		if (hasQ) {
			if (sgt != null) {
				sgt.reset(file, opt, ctx, distribute);
				sgt.close();
				sgt = null;
			}
		}
		
		String []srcColNames = baseTable.getColNames();
		int len = srcColNames.length;
		String []colNames = new String[len];
		
		if (baseTable instanceof ColumnTableMetaData) {
			for (int i = 0; i < len; i++) {
				ColumnMetaData col = ((ColumnTableMetaData)baseTable).getColumn(srcColNames[i]);
				if (col.isDim()) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		} else {
			boolean[] isDim = ((RowTableMetaData)baseTable).getDimIndex();
			for (int i = 0; i < len; i++) {
				if (isDim[i]) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		}

		File newFile;
		FileObject newFileObj = null;
		if (file == null) {
			newFileObj = new FileObject(this.file.getAbsolutePath());
			newFileObj = new FileObject(newFileObj.createTempFile(this.file.getName()));
			newFile = newFileObj.getLocalFile().file();
		} else {
			newFile = file;
		}
		
		// ���ɷֶ�ѡ��Ƿ񰴵�һ�ֶηֶ�
		String newOpt = null;
		String segmentCol = baseTable.getSegmentCol();
		if (segmentCol != null) {
			newOpt = "p";
		}
		
		GroupTable newGroupTable = null;
		try {
			//����������ļ�
			if (isCol) {
				newGroupTable = new ColumnGroupTable(newFile, colNames, distribute, newOpt, ctx);
				if (compress) {
					newGroupTable.setCompress(true);
				} else if (uncompress) {
					newGroupTable.setCompress(false);
				} else {
					newGroupTable.setCompress(isCompress());
				}
			} else {
				newGroupTable = new RowGroupTable(newFile, colNames, distribute, newOpt, ctx);
			}
			
			//����ֶ�
			boolean needSeg = baseTable.segmentCol != null;
			if (needSeg) {
				newGroupTable.baseTable.setSegmentCol(baseTable.segmentCol, baseTable.segmentSerialLen);
			}
			TableMetaData newBaseTable = newGroupTable.baseTable;
			
			//����������ӱ�
			ArrayList<TableMetaData> tableList = baseTable.tableList;
			for (TableMetaData t : tableList) {
				srcColNames = t.getColNames();
				len = srcColNames.length;
				colNames = new String[len];
				
				if (t instanceof ColumnTableMetaData) {
					for (int i = 0; i < len; i++) {
						ColumnMetaData col = ((ColumnTableMetaData)t).getColumn(srcColNames[i]);
						if (col.isDim()) {
							colNames[i] = "#" + srcColNames[i];
						} else {
							colNames[i] = srcColNames[i];
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						boolean[] isDim = ((RowTableMetaData)t).getDimIndex();
						if (isDim[i]) {
							colNames[i] = "#" + srcColNames[i];
						} else {
							colNames[i] = srcColNames[i];
						}
					}
				}
				newBaseTable.createAnnexTable(colNames, t.getSerialBytesLen(), t.tableName);
			}
			
			if (hasN) {
				newGroupTable.save();
				newGroupTable.close();
				return Boolean.TRUE;
			}
			
			//д���ݵ��»���
			ICursor cs = null;
			if (hasQ) {
				//����Լ��Ĵ��α꣨�������ļ��ģ�
				if (baseTable instanceof ColumnTableMetaData) {
					cs = new Cursor((ColumnTableMetaData)baseTable);
				} else {
					cs = new RowCursor((RowTableMetaData)baseTable);
				}
				
			} else {
				cs = baseTable.cursor();
			}
			
			int startBlock = -1;//hasQʱ������
			if (hasQ) {
				//�ӻ������и��ݲ������ǰ�Ŀ��
				startBlock = baseTable.getFirstBlockFromModifyRecord();
				tableList = baseTable.tableList;
				for (TableMetaData t : tableList) {
					int blk = t.getFirstBlockFromModifyRecord();
					if (startBlock == -1 ) {
						startBlock = blk;
					} else if (blk != -1 && startBlock > blk) {
						startBlock = blk;
					}
				}
				if (startBlock < 0) {
					newGroupTable.delete();
					return Boolean.FALSE;
				} else if (startBlock == 0) {
					hasQ = false;//���reset����ڵ�һ�飬����ȫreset
				} else {
					((Cursor) cs).setSegment(startBlock, baseTable.getDataBlockCount());
				}
			}
			newBaseTable.append(cs);
			newBaseTable.appendCache();
			
			//д���ݵ�������ӱ�
			for (TableMetaData t : tableList) {
				TableMetaData newTable = newBaseTable.getAnnexTable(t.tableName);
				if (hasQ) {
					//����Լ��Ĵ��α꣨�������ļ��ģ�
					if (t instanceof ColumnTableMetaData) {
						cs = new Cursor((ColumnTableMetaData)t, t.allColNames);
					} else {
						cs = new RowCursor((RowTableMetaData)t, t.allColNames);
					}
				} else {
					cs = t.cursor(t.allColNames);
				}
				if (hasQ) {
					((JoinTableCursor) cs).setSegment(startBlock, t.getDataBlockCount());
				}
				newTable.append(cs);
			}

			if (file != null) {
				newGroupTable.close();
				return Boolean.TRUE;
			}
			
			if (hasQ) {
				//resetԭ�����ֹ�����
				long pos, freePos;
				freePos = baseTable.resetByBlock(startBlock);
				for (TableMetaData t : tableList) {
					pos = t.resetByBlock(startBlock);
					if (freePos < pos) {
						freePos = pos;
					}
				}
				this.freePos = freePos;
				save();
				readHeader();
				tableList = baseTable.tableList;
				
				//д����֮�������
				cs = newBaseTable.cursor();
				baseTable.append(cs);
				ArrayList<TableMetaData> newTableList = newBaseTable.tableList;
				for (int i = 0; i < tableList.size(); i++) {
					TableMetaData t = newTableList.get(i);
					cs = t.cursor(t.allColNames);
					tableList.get(i).append(cs);
				}
				
				//ɾ����ʱ���
				newGroupTable.close();
				newGroupTable.file.delete();
				
				//�ؽ������ļ�
				baseTable.resetIndex(ctx);
				newTableList = baseTable.tableList;
				for (TableMetaData table : newTableList) {
					table.resetIndex(ctx);
				}
				
				//hasQʱ���ô���cuboid
				return Boolean.TRUE;
			} else {
				if (sgt != null) {
					sgt.delete();
				}
			}
			
			//���û��f'�����ͽ�������
			//��������
			String []indexNames = baseTable.indexNames;
			if (indexNames != null) {
				String [][]indexFields = baseTable.indexFields;
				String [][]indexValueFields = baseTable.indexValueFields;
				for (int j = 0, size = indexNames.length; j < size; j++) {
					newBaseTable.addIndex(indexNames[j], indexFields[j], indexValueFields[j]);
				}
			}
			//�ӱ�����
			ArrayList<TableMetaData> newTableList = newBaseTable.tableList;
			len = tableList.size();
			for (int i = 0; i < len; i++) {
				TableMetaData oldTable = tableList.get(i);
				TableMetaData newTable = newTableList.get(i);
				indexNames = oldTable.indexNames;
				if (indexNames == null) continue;
				String [][]indexFields = oldTable.indexFields;
				String [][]indexValueFields = oldTable.indexValueFields;
				for (int j = 0, size = indexNames.length; j < size; j++) {
					newTable.addIndex(indexNames[j], indexFields[j], indexValueFields[j]);
				}
			}
			
			//����cuboid
			String []cuboids = baseTable.cuboids;
			if (cuboids != null) {
				for (String cuboid : cuboids) {
					newBaseTable.addCuboid(cuboid);
				}
			}
			//�ӱ�cuboid
			for (int i = 0; i < len; i++) {
				TableMetaData oldTable = tableList.get(i);
				TableMetaData newTable = newTableList.get(i);
				cuboids = oldTable.cuboids;
				if (cuboids == null) continue;
				for (String addCuboid : cuboids) {
					newTable.addCuboid(addCuboid);
				}
			}
			
		} catch (Exception e) {
			if (newGroupTable != null) newGroupTable.close();
			newFile.delete();
			throw new RQException(e.getMessage(), e);
		}
		
		//ɾ���ɵ����
		String path = this.file.getAbsolutePath();
		close();
		boolean b = this.file.delete();
		if (!b) 
			return Boolean.FALSE;
		newGroupTable.close();
		newFileObj.move(path, null);

		try{
			newGroupTable = open(this.file, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		//�ؽ������ļ���cuboid
		newGroupTable.baseTable.resetIndex(ctx);
		newGroupTable.baseTable.resetCuboid(ctx);
		ArrayList<TableMetaData> newTableList = newGroupTable.baseTable.tableList;
		for (TableMetaData table : newTableList) {
			table.resetIndex(ctx);
			table.resetCuboid(ctx);
		}
		newGroupTable.close();
		
		return Boolean.TRUE;
	}
	
	/**
	 * �ѵ�ǰ���д����һ���µ��ļ���
	 * @param fileGroup �ļ���
	 * @param opt r,����Ϊ�д档c,����Ϊ�д档
	 * @param ctx
	 * @param distribute �µķֲ����ʽ��ʡ������ԭ����
	 * @return
	 */
	public boolean resetFileGroup(FileGroup fileGroup, String opt, Context ctx, String distribute) {
		if (distribute == null) {
			distribute = this.distribute;
		}
		boolean isCol = this instanceof ColumnGroupTable;
		boolean uncompress = false; // ��ѹ��
		if (opt != null) {
			if (opt.indexOf('r') != -1) {
				isCol = false;
			} else if (opt.indexOf('c') != -1) {
				isCol = true;
			}
			
			if (opt.indexOf('u') != -1) {
				uncompress = true;
			}
			
			if (opt.indexOf('z') != -1) {
				uncompress = false;
			}
		}
		
		String []srcColNames = baseTable.getColNames();
		int len = srcColNames.length;
		String []colNames = new String[len];
		
		if (baseTable instanceof ColumnTableMetaData) {
			for (int i = 0; i < len; i++) {
				ColumnMetaData col = ((ColumnTableMetaData)baseTable).getColumn(srcColNames[i]);
				if (col.isDim()) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		} else {
			boolean[] isDim = ((RowTableMetaData)baseTable).getDimIndex();
			for (int i = 0; i < len; i++) {
				if (isDim[i]) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		}
		
		// ���ɷֶ�ѡ��Ƿ񰴵�һ�ֶηֶ�
		String newOpt = "y";
		String segmentCol = baseTable.getSegmentCol();
		if (segmentCol != null) {
			newOpt = "p";
		}
		
		if (isCol) {
			newOpt += 'c';
		} else {
			newOpt += 'r';
		}
		
		if (uncompress) {
			newOpt += 'u';
		}
		try {
			//д����
			TableMetaDataGroup newTableGroup = fileGroup.create(colNames, distribute, newOpt, ctx);
			ICursor cs = baseTable.cursor();
			newTableGroup.append(cs, "xi");
			
			//д�ӱ�
			ArrayList<TableMetaData> tableList = baseTable.tableList;
			for (TableMetaData t : tableList) {
				len = t.colNames.length;
				colNames = Arrays.copyOf(t.colNames, len);
				if (t instanceof ColumnTableMetaData) {
					for (int i = 0; i < len; i++) {
						ColumnMetaData col = ((ColumnTableMetaData)t).getColumn(colNames[i]);
						if (col.isDim()) {
							colNames[i] = "#" + colNames[i];
						}
					}
				} else {
					boolean[] isDim = ((RowTableMetaData)t).getDimIndex();
					for (int i = 0; i < len; i++) {
						if (isDim[i]) {
							colNames[i] = "#" + colNames[i];
						}
					}
				}
				ITableMetaData newTable = newTableGroup.createAnnexTable(colNames, t.getSerialBytesLen(), t.tableName);
				
				//������α꣬ȡ���ֶ���Ҫ�������������ֶΣ�������Ϊ��Ҫ����ֲ�
				String[] allColNames = Arrays.copyOf(srcColNames, srcColNames.length + t.colNames.length);
				System.arraycopy(t.colNames, 0, allColNames, srcColNames.length, t.colNames.length);
				cs = t.cursor(allColNames);
				newTable.append(cs, "xi");
			}

			newTableGroup.close();
			return Boolean.TRUE;
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public abstract long[] getBlockLinkInfo();
	
	/**
	 * �Աȿ�����Ϣ
	 * @param blockLinkInfo ������ļ���������Ϣ
	 * @return ÿ����Ҫͬ��������������ʼ��ַ
	 */
	public long[] cmpBlockLinkInfo(long []blockLinkInfo) {
		//������������Ķ����Ǿ��ļ�
		long []localBlockInfo = getBlockLinkInfo();
		int localSize = localBlockInfo.length / 4;
		int size = blockLinkInfo.length / 4;
		LongArray posArray = new LongArray(1024);
		
		for (int i = 0; i < size; i++) {
			long firstBlockPos = blockLinkInfo[i * 4];
			long lastBlockPos = blockLinkInfo[i * 4 + 1];
			int freeIndex = (int) blockLinkInfo[i * 4 + 2];
			int blockCount = (int) blockLinkInfo[i * 4 + 3];
			
			if (firstBlockPos >= fileSize) {
				//�������file size�򲻴���
				continue;
			}
			boolean find = false;
			for (int j = 0; j < localSize; j++) {
				long localFirstBlockPos = localBlockInfo[j * 4];
				long localLastBlockPos = localBlockInfo[j * 4 + 1];
				int localFreeIndex = (int) localBlockInfo[j * 4 + 2];
				int localBlockCount = (int) localBlockInfo[j * 4 + 3];
				if (firstBlockPos == localFirstBlockPos) {
					find = true;
					if (lastBlockPos < localLastBlockPos) {
						//�쳣
					}
					if ((lastBlockPos != localLastBlockPos) ||
							(freeIndex != localFreeIndex) || 
							(blockCount !=localBlockCount)) {
						//�ҵ��˵��ǲ���ȣ�Ҳ��Ҫͬ��
						posArray.add(localLastBlockPos);
					}
					break;
				}
				
			}
			if (!find) {
				//û�ҵ�����Ҫͬ��
				posArray.add(firstBlockPos);
			}
		}
		if (posArray.size() == 0) {
			return null;
		}
		return posArray.toArray();
	}
	
	/**
	 * �õ�ͬ����ַ
	 * @param positions ÿ����Ҫͬ��������������ʼ��ַ
	 * @return ��Ҫͬ�������е�ַ
	 */
	public long[] getSyncPosition(long []positions) {
		//����blockLink ��Ҫͬ��
		//headerBlockLink ��ŵ����һ��ͬ��
		//�ļ�β�������ֻ��ر���
		LongArray posArray = new LongArray(1024);
		byte []block = new byte[5];
		for (int i = 0, len = positions.length; i < len; ++i) {
			long pos = positions[i];
			if (pos > 1) {
				try {
					while (pos > 1) {
						posArray.add(pos);
						raf.seek(pos + blockSize - POS_SIZE);
						raf.read(block);
						pos = (((long)(block[0] & 0xff) << 32) +
								((long)(block[1] & 0xff) << 24) +
								((block[2] & 0xff) << 16) +
								((block[3] & 0xff) <<  8) +
								(block[4] & 0xff));
					}
					
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				}
			}
		}
		if (posArray.size() == 0) {
			return null;
		}
		return posArray.toArray();
	}

	/**
	 * 
	 * @return ������Ҫͬ�������е�ַ
	 */
	public long[] getModifyPosition() {
		int count = 1 + baseTable.tableList.size();
		
		long []positions = new long[count * 2];
		int c = 0;
		
		positions[c++] = baseTable.modifyBlockLink1.firstBlockPos;
		positions[c++] = baseTable.modifyBlockLink2.firstBlockPos;
		
		for (TableMetaData table : baseTable.tableList) {
			positions[c++] = table.modifyBlockLink1.firstBlockPos;
			positions[c++] = table.modifyBlockLink2.firstBlockPos;
		}
		
		LongArray posArray = new LongArray(1024);
		byte []block = new byte[5];
		for (int i = 0, len = positions.length; i < len; ++i) {
			long pos = positions[i];
			if (pos > 1) {
				try {
					while (pos > 1) {
						posArray.add(pos);
						raf.seek(pos + blockSize - POS_SIZE);
						raf.read(block);
						pos = (((long)(block[0] & 0xff) << 32) +
								((long)(block[1] & 0xff) << 24) +
								((block[2] & 0xff) << 16) +
								((block[3] & 0xff) <<  8) +
								(block[4] & 0xff));
					}
					
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				}
			}
		}
		if (posArray.size() == 0) {
			return null;
		}
		return posArray.toArray();
	}
	
	/**
	 * ���ؿ����ϵ�����ͷ��ַ
	 * @return
	 */
	public long[] getHeaderPosition() {
		//headerBlockLink ��ŵ����һ��ͬ��
		LongArray posArray = new LongArray(1024);
		byte []block = new byte[5];
		long pos = 0;
		try {
			do {
				posArray.add(pos);
				raf.seek(pos + blockSize - POS_SIZE);
				raf.read(block);
				pos = (((long)(block[0] & 0xff) << 32) +
						((long)(block[1] & 0xff) << 24) +
						((block[2] & 0xff) << 16) +
						((block[3] & 0xff) <<  8) +
						(block[4] & 0xff));
			} while (pos > 1);
			
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		if (posArray.size() == 0) {
			return null;
		}
		return posArray.toArray();
	}
	
	void setPassword(String writePsw, String readPsw) {
		if (writePsw != null) {
			MD5 md5 = new MD5();
			this.writePswHash = md5.getMD5ofStr(writePsw);
		}
		
		if (readPsw != null) {
			MD5 md5 = new MD5();
			this.readPswHash = md5.getMD5ofStr(readPsw);
		}
	}
	
	/**
	 * �����Ƿ�����������
	 * @return true�������룬false��û����
	 */
	public boolean hasPassword() {
		return writePswHash != null || readPswHash != null;
	}
	
	/**
	 * ����������������Ҫ���ô˺������ܷ���
	 * @param psw д������߶����룬�����д������ȿɶ��ֿ�д������Ƕ�������ֻ�ɶ�
	 */
	public void checkPassword(String psw) {
		/*if (writePswHash != null) {
			if (psw == null) {
				canWrite = false;
			} else {
				MD5 md5 = new MD5();
				canWrite = md5.getMD5ofStr(psw).equals(writePswHash);
				if (canWrite) {
					canRead = true;
					return;
				}
			}
		}
		
		if (readPswHash != null) {
			if (psw == null) {
				canRead = false;
			} else {
				MD5 md5 = new MD5();
				canRead = md5.getMD5ofStr(psw).equals(readPswHash);
				if (!canRead) {
					//added by hhw 2019.6�������falseдҲ��Ϊfalse
					canWrite = false;
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("cellset.pswError"));
				}
			}
		}*/
	}
	
	public void checkWritable() {
		/*if (!canWrite) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.needWritePassword"));
		}*/
	}
	
	public void checkReadable() {
		/*if (!canRead) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.needReadPassword"));
		}*/
	}
	
	/**
	 * �����Ƿ��д
	 * @return
	 */
	public boolean canWrite() {
		return true;
	}
	
	/**
	 * �����Ƿ�ɶ�
	 * @return
	 */
	public boolean canRead() {
		return true;
	}
	
	Object getSyncObject() {
		return FileSyncManager.getSyncObject(file);
	}
	
	// ȡ�ֲ����ʽ��
	public String getDistribute() {
		return distribute;
	}
	
	// �����Ƿ�ѹ��������
	public boolean isCompress() {
		return reserve[1] == 0;
	}
	
	// �����Ƿ�ѹ��������
	public void setCompress(boolean isCompress) {
		if (isCompress) {
			reserve[1] = 0;
		} else {
			reserve[1] = 1;
		}
	}
	
	public void setPartition(Integer partition) {
		this.partition = partition;
	}
	
	public Integer getPartition() {
		return partition;
	}
}