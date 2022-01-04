package com.scudata.dw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileWriter;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MergeCursor;
import com.scudata.dm.cursor.MergeCursor2;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * �ļ���
 * @author runqian
 *
 */
public class TableMetaDataGroup implements ITableMetaData {
	private String fileName;
	private ITableMetaData []tables;
	private int []partitions;
	private String opt;
	private Context ctx;
	private Expression distribute;
	
	public TableMetaDataGroup(String fileName, ITableMetaData []tables, int []partitions, String opt, Context ctx) {
		this.fileName = fileName;
		this.tables = tables;
		this.partitions = partitions;
		this.opt = opt;
		this.ctx = ctx;
		distribute = new Expression(ctx, tables[0].getDistribute());
	}
	
	public TableMetaDataGroup(String fileName, ITableMetaData []tables, int []partitions, String opt, Expression distribute, Context ctx) {
		this.fileName = fileName;
		this.tables = tables;
		this.partitions = partitions;
		this.opt = opt;
		this.ctx = ctx;
		this.distribute = distribute;
	}
	
	public void close() {
		for (ITableMetaData table : tables) {
			table.close();
		}
	}
	
	public ITableMetaData createAnnexTable(String []colNames, int []serialBytesLen, String tableName) throws IOException {
		int count = tables.length;
		ITableMetaData []annexTables = new ITableMetaData[count];
		for (int i = 0; i < count; ++i) {
			annexTables[i] = tables[i].createAnnexTable(colNames, serialBytesLen, tableName);
		}
		
		return new TableMetaDataGroup(fileName, annexTables, partitions, opt, distribute, ctx);
	}
	
	public ITableMetaData getAnnexTable(String tableName) {
		int count = tables.length;
		ITableMetaData []annexTables = new ITableMetaData[count];
		for (int i = 0; i < count; ++i) {
			annexTables[i] = tables[i].getAnnexTable(tableName);
		}
		
		return new TableMetaDataGroup(fileName, annexTables, partitions, opt, distribute, ctx);
	}
	
	public void append(ICursor cursor) throws IOException {
		append(cursor, null);
	}
	
	/**
	 * ��xѡ��ʱ������׷�ӣ��α�������ݶ�Ӧ�������
	 * @param cursor
	 * @param opt
	 * @throws IOException
	 */
	private void append_x(ICursor cursor, String opt) throws IOException {
		Expression distribute = this.distribute;
		if (distribute == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.lessDistribute"));
		}
		
		Sequence data = cursor.fetch(ICursor.INITSIZE);
		if (data == null || data.length() == 0) {
			return;
		}
		
		Context ctx = this.ctx;
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		ArrayList<BFileWriter>tmpFiles = new ArrayList<BFileWriter>();
		tmpFiles.add(null); // 0λ�ñ���������1����List������1��
		int partCount = 0;
		
		try {
			while (data != null && data.length() > 0) {
				Sequence group = data.group(distribute, null, ctx);
				int gcount = group.length();
				for (int g = 1; g <= gcount; ++g) {
					Sequence curGroup = (Sequence)group.getMem(g);
					Object obj = curGroup.calc(1, distribute, ctx);
					if (!(obj instanceof Number)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("dw.distributeError"));
					}
					
					int p = ((Number)obj).intValue();
					if (p < 1) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("dw.distributeError"));
					}
					
					if (p > partCount) {
						for (; partCount < p; ++partCount) {
							tmpFiles.add(null);
						}
					}
					
					BFileWriter writer = tmpFiles.get(p);
					if (writer == null) {
						writer = new BFileWriter(FileObject.createTempFileObject(), null);
						writer.prepareWrite(ds, false);
						tmpFiles.set(p, writer);
					}
					
					writer.write(curGroup);
				}
				
				data = cursor.fetch(ICursor.INITSIZE);
			}
			
			for (int p = 1; p <= partCount; ++p) {
				BFileWriter writer = tmpFiles.get(p);
				if (writer == null) {
					continue;
				}
				
				writer.close();
				BFileCursor bcs = new BFileCursor(writer.getFile(), null, null, ctx);
				getPartition(p).append(bcs, opt);
			}
		} finally {
			// ɾ����ʱ�ļ�
			for (int i = 1; i <= partCount; ++i) {
				BFileWriter writer = tmpFiles.get(i);
				if (writer != null) {
					writer.getFile().delete();
				}
			}
		}
	}
	
	public void append(ICursor cursor, String opt) throws IOException {
		if (opt != null && opt.indexOf('x') != -1) {
			append_x(cursor, opt);
			return;
		}
		
		Expression distribute = this.distribute;
		if (distribute == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.lessDistribute"));
		}
		
		// ÿ����·�α����������ͬһ������
		if (cursor instanceof MultipathCursors) {
			ICursor []cursors = ((MultipathCursors)cursor).getCursors();
			for (ICursor cs : cursors) {
				Sequence data = cs.peek(ICursor.FETCHCOUNT);
				if (data == null || data.length() == 0) {
					continue;
				}
				
				Object obj = data.calc(1, distribute, ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("dw.distributeError"));
				}
				
				int p = ((Number)obj).intValue();
				if (p < 1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("dw.distributeError"));
				}
				
				getPartition(p).append(cs, opt);
			}
		} else {
			Sequence data = cursor.peek(ICursor.FETCHCOUNT);
			if (data == null || data.length() == 0) {
				return;
			}
			
			Object obj = data.calc(1, distribute, ctx);
			if (!(obj instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("dw.distributeError"));
			}
			
			int p = ((Number)obj).intValue();
			if (p < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("dw.distributeError"));
			}
			
			getPartition(p).append(cursor, opt);
		}
	}
	
	private ITableMetaData getPartition(int p) {
		int pcount = partitions.length;
		for (int i = 0; i < pcount; ++i) {
			if (partitions[i] == p) {
				return tables[i];
			}
		}
		
		// �����û�д����˷���������򴴽�
		File file = Env.getPartitionFile(p, fileName);
		TableMetaData tmd = (TableMetaData)tables[0];
		GroupTable gt = tmd.getGroupTable().dupStruct(file);
		TableMetaData result = gt.getBaseTable();
		String tableName = tmd.getTableName();
		if (tableName != null && tableName.length() != 0) {
			result = result.getAnnexTable(tableName);
		}
		
		ITableMetaData []tmpTables = new ITableMetaData[pcount + 1];
		int []tmpPartitions = new int[pcount + 1];
		System.arraycopy(tables, 0, tmpTables, 0, pcount);
		System.arraycopy(partitions, 0, tmpPartitions, 0, pcount);
		tmpTables[pcount] = result;
		tmpPartitions[pcount] = p;
		
		this.tables = tmpTables;
		this.partitions = tmpPartitions;
		return result;
	}
	
	public Sequence update(Sequence data, String opt) {
		throw new RQException("'update' function is unimplemented in file group!");
	}
	
	public Sequence delete(Sequence data, String opt) {
		throw new RQException("'delete' function is unimplemented in file group!");
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, MultipathCursors mcs, String opt, Context ctx) {
		throw new RQException("'mcursor' function is unimplemented in file group!");
	}
	
	public void rename(String[] srcFields, String[] newFields, Context ctx) throws IOException {
		throw new RQException("'rename' function is unimplemented in file group!");
	}
	
	public ICursor cursor() {
		return cursor(null, null, null, null, null, null, ctx);
	}
	
	public ICursor cursor(String []fields) {
		return cursor(null, fields, null, null, null, null, ctx);
	}
	
	public ICursor cursor(String []fields, Expression filter, Context ctx) {
		return cursor(null, fields, filter, null, null, null, ctx);
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String[] opts, Context ctx) {
		int count = tables.length;
		ICursor []cursors = new ICursor[count];
		for (int i = 0; i < count; ++i) {
			cursors[i] = tables[i].cursor(exps, fields, filter, fkNames, codes, opts, ctx);
		}
		
		return new ConjxCursor(cursors);
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String[] opts, int pathCount, Context ctx) {
		if (pathCount < 2) {
			return cursor(exps, fields, filter, fkNames, codes, opts, ctx);
		}
		
		// ��ÿ���ļ��ֳ�pathCount·��Ȼ�����е�i·�ϳ�һ���α꣬�������ɶ�·�α�
		int tableCount = tables.length;
		ArrayList<ICursor> []lists = new ArrayList[pathCount];
		for (int i = 0; i < pathCount; ++i) {
			lists[i] = new ArrayList<ICursor>(tableCount);
		}
		
		for (int i = 0; i < tableCount; ++i) {
			ICursor cursor = tables[i].cursor(exps, fields, filter, fkNames, codes, opts, pathCount, ctx);
			if (cursor instanceof MultipathCursors) {
				MultipathCursors mcs = (MultipathCursors)cursor;
				ICursor []cursors = mcs.getCursors();
				for (int c = 0; c < cursors.length; ++c) {
					lists[c].add(cursors[c]);
				}
			} else {
				lists[0].add(cursor);
			}
		}
		
		ArrayList<ICursor> list = new ArrayList<ICursor>(pathCount);
		for (int i = 0; i < pathCount; ++i) {
			int size = lists[i].size();
			if (size > 1) {
				ICursor []cursors = new ICursor[size];
				lists[i].toArray(cursors);
				list.add(new ConjxCursor(cursors));
			} else if (size == 1) {
				list.add(lists[i].get(0));
			}
		}
		
		int size = list.size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return list.get(1);
		} else {
			ICursor []cursors = new ICursor[size];
			list.toArray(cursors);
			return new MultipathCursors(cursors, ctx);
		}
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String[] opts, int segSeq, int segCount, Context ctx) {
		if (segCount < 2) {
			return cursor(exps, fields, filter, fkNames, codes, opts, ctx);
		}
		
		int count = tables.length;
		ArrayList<ICursor> list = new ArrayList<ICursor>(count);
		for (int i = 0; i < count; ++i) {
			ICursor cursor = tables[i].cursor(exps, fields, filter, fkNames, codes, opts, segSeq, segCount, ctx);
			if (cursor != null) {
				list.add(cursor);
			}
		}
		
		ICursor []cursors = new ICursor[list.size()];
		list.toArray(cursors);
		return new ConjxCursor(cursors);
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String[] opts, int pathSeq, int pathCount, int pathCount2, Context ctx) {
		throw new RQException("'mcursor' function is unimplemented in file group!");
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String[] opts, ICursor cs, int seg, Object [][]endValues, Context ctx) {
		throw new RQException("'mcursor' function is unimplemented in file group!");
	}
	
	public Table finds(Sequence values) throws IOException {
		Table result = null;
		for (ITableMetaData table : tables) {
			Table cur = table.finds(values);
			if (cur != null) {
				if (result == null) {
					result = cur;
				} else {
					result.append(cur, null);
				}
			}
		}
		
		if (opt == null || opt.indexOf('o' ) == -1) {
			String []sortFields = getAllSortedColNames();
			result.sortFields(sortFields);
		}
		
		return result;
	}
	
	public Table finds(Sequence values, String []selFields) throws IOException {
		Table result = null;
		for (ITableMetaData table : tables) {
			Table cur = table.finds(values, selFields);
			if (cur != null) {
				if (result == null) {
					result = cur;
				} else {
					result.append(cur, null);
				}
			}
		}
		return result;
	}
	
	public ICursor icursor(String []fields, Expression filter, String iname, String opt, Context ctx) {
		boolean sort = true;
		int[] sortFields = null;
		int count = tables.length;
		ArrayList<ICursor> list = new ArrayList<ICursor>(count);
		for (int i = 0; i < count; ++i) {
			ICursor cursor = tables[i].icursor(fields, filter, iname, opt, ctx);
			if (cursor != null) {
				list.add(cursor);
				if (sort) {
					if (cursor instanceof IndexCursor) {
						sortFields = ((IndexCursor)cursor).getSortFieldsIndex();
						if (sortFields == null) {
							sort = false;
						}
					}  else if (cursor instanceof IndexFCursor) {
						sortFields = ((IndexFCursor)cursor).getSortFieldsIndex();
						if (sortFields == null) {
							sort = false;
						}
					} else if (cursor instanceof Cursor) {
						//�п��ܷ���Cursor����ʱ����
						sort = false;
					} else if (cursor instanceof ConjxCursor) {
						sort = false;
					} else if (cursor instanceof MergeCursor2) {
						sortFields = ((MergeCursor2)cursor).getFields();
						if (sortFields == null) {
							sort = false;
						}
					}
				}
			}
		}

		ICursor []cursors = new ICursor[list.size()];
		list.toArray(cursors);

		if (sort) {
			return new MergeCursor(cursors, sortFields, null, ctx);
		} else {
			return new ConjxCursor(cursors);
		}
	}

	/**
	 * ȡ�����ֶ�����������
	 * @return �����ֶ�������
	 */
	public String[] getAllKeyColNames() {
		return tables[0].getAllKeyColNames();
	}
	
	/**
	 * ���������ֶ�����������
	 * @return �����ֶ�������
	 */
	public String[] getAllSortedColNames() {
		return tables[0].getAllSortedColNames();
	}
	
	/**
	 * ���������С���������key�У�
	 * @return
	 */
	public String[] getAllColNames() {
		return tables[0].getAllColNames();
	}
	
	public boolean deleteIndex(String indexName) throws IOException {
		boolean result = true;
		for (ITableMetaData table : tables) {
			if (!table.deleteIndex(indexName)) {
				result = false;
			}
		}
		
		return result;
	}
	
	public void createIndex(String I, String []fields, Object obj, String opt, Expression w, Context ctx) {
		for (ITableMetaData table : tables) {
			table.createIndex(I, fields, obj, opt, w, ctx);
		}
	}
	
	// ȡ�ֲ����ʽ��
	public String getDistribute() {
		return tables[0].getDistribute();
	}

	public void addColumn(String colName, Expression exp, Context ctx) {
		for (ITableMetaData table : tables) {
			table.addColumn(colName, exp, ctx);
		}
	}

	public void deleteColumn(String colName) {
		for (ITableMetaData table : tables) {
			table.deleteColumn(colName);
		}
	}
	
	/**
	 * �������ֶι鲢�ֱ�����ݷ��س��α�
	 * @param ctx ����������
	 * @return �α�
	 */
	public ICursor merge(Context ctx) {
		String []sortFields = tables[0].getAllSortedColNames();
		if (sortFields == null || sortFields.length == 0) {
			return cursor();
		}
		
		int count = tables.length;
		ICursor []cursors = new ICursor[count];
		for (int i = 0; i < count; ++i) {
			cursors[i] = tables[i].cursor();
		}
		
		// �����ֶ���ǰ��
		int fcount = sortFields.length;
		int []fields = new int[fcount];
		for (int i = 0; i < fcount; ++i) {
			fields[i] = 0;
		}
		
		return new MergeCursor(cursors, fields, null, ctx);
	}
	

	public ITableMetaData[] getTables() {
		return tables;
	}
	
	/**
	 * ʹ��Ԥ������з������
	 * @param exps ������ʽ
	 * @param names ������ʽ������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames ���ܱ��ʽ������
	 * @param srcTable Ҫ��������
	 * @param w ��������
	 * @param hasM �Ƿ���
	 * @param n ������
	 * @param option
	 * @param ctx
	 * @return
	 */
	public Sequence cgroups(String []expNames, String []names, String []newExpNames, String []newNames,
			Expression w, boolean hasM, int n, String option,  Context ctx) {
			int count = tables.length;
			
			//�����б�Ľ���ŵ�һ��
			Sequence result = Cuboid.cgroups(expNames, names, newExpNames, newNames, 
					(TableMetaData) tables[0], w, hasM, n, option, ctx);
			for (int i = 1; i < count; ++i) {
				Sequence seq = Cuboid.cgroups(expNames, names, newExpNames, newNames, 
						(TableMetaData) tables[i], w, hasM, n, option, ctx);
				result.addAll(seq);
			}
			
			//��֯������ʽ
			count = names.length;
			Expression exps[] = new Expression[count];
			for (int i = 0; i < count; i++) {
				exps[i] = new Expression(names[i]);
			}
			
			//��֯�پۺϱ��ʽ
			Expression newExps[] = new Expression[count = newNames.length];//�����洢Ҫ�ۺϵ�ԭ�ֶ�
			for (int i = 0, len = count; i < len; i++) {
				String str = newExpNames[i];
				//��count�پۺϣ�Ҫ��Ϊ�ۼ�
				if (str.indexOf("count(") != -1) {
					str = str.replaceFirst("count", "sum");
				}
				
				//�پۺ�ʱҪ�滻һ���ֶ���
				String sub = str.substring(str.indexOf('(') + 1, str.indexOf(')'));
				str = str.replaceAll(sub, "'" + newNames[i] + "'");
				newExps[i] = new Expression(str);
			}
			return result.groups(exps, names, newExps, newNames, option, ctx);
	}
}