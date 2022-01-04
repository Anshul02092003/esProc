package com.scudata.dm;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dw.ColumnGroupTable;
import com.scudata.dw.ColumnMetaData;
import com.scudata.dw.ColumnTableMetaData;
import com.scudata.dw.GroupTable;
import com.scudata.dw.ITableMetaData;
import com.scudata.dw.RowGroupTable;
import com.scudata.dw.RowTableMetaData;
import com.scudata.dw.TableMetaData;
import com.scudata.dw.TableMetaDataGroup;
import com.scudata.resources.EngineMessage;

/**
 * �ļ���
 * file(fn:z) z����
 * @author RunQian
 *
 */
public class FileGroup implements Externalizable {
	private String fileName;
	private int []partitions;

	public FileGroup(String fileName, int []partitions) {
		this.fileName = fileName;
		this.partitions = partitions;
	}
	
	/**
	 * �ѵ�ǰ����д�������
	 * @param out �����
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileName);
		out.writeInt(partitions.length);
		for (int p : partitions) {
			out.writeInt(p);
		}
	}
	
	/**
	 * �������������ļ������
	 * @param in ������
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileName = (String)in.readObject();
		int count = in.readInt();
		partitions = new int[count];
		for (int i = 0; i < count; ++i) {
			partitions[i] = in.readInt();
		}
	}
	
	/**
	 * ȡ�ļ���
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * ȡ�ֱ��
	 * @return
	 */
	public int[] getPartitions() {
		return partitions;
	}
	
	/**
	 * �����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return
	 */
	public TableMetaDataGroup open(String opt, Context ctx) {
		int pcount = partitions.length;
		TableMetaData []tables = new TableMetaData[pcount];
		
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			tables[i] = GroupTable.openBaseTable(file, ctx);
			tables[i].getGroupTable().setPartition(partitions[i]);
		}
		
		return new TableMetaDataGroup(fileName, tables, partitions, opt, ctx);
	}
	
	/**
	 * �������
	 * @param colNames �ֶ�������
	 * @param distribute �ֲ����ʽ
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return
	 * @throws IOException
	 */
	public TableMetaDataGroup create(String []colNames, String distribute, String opt, Context ctx) throws IOException {
		int pcount = partitions.length;
		TableMetaData []tables = new TableMetaData[pcount];
		boolean yopt = opt != null && opt.indexOf('y') != -1;
		boolean ropt = opt != null && opt.indexOf('r') != -1;
		
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			if (file.exists()) {
				if (yopt) {
					try {
						GroupTable table = GroupTable.open(file, ctx);
						table.delete();
					} catch (IOException e) {
						throw new RQException(e.getMessage(), e);
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("file.fileAlreadyExist", fileName));
				}
			}
			
			GroupTable table;
			if (ropt) {
				table = new RowGroupTable(file, colNames, distribute, opt, ctx);
			} else {
				table = new ColumnGroupTable(file, colNames, distribute, opt, ctx);
			}
			
			table.setPartition(partitions[i]);
			tables[i] = table.getBaseTable();
		}
		
		return new TableMetaDataGroup(fileName, tables, partitions, opt, ctx);
	}
	
	/**
	 * �����������
	 * @param opt ѡ��
	 * @param ctx����������
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean resetGroupTable(String opt, Context ctx) {
		int pcount = partitions.length;
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			TableMetaData tmd = GroupTable.openBaseTable(file, ctx);
			boolean result = tmd.getGroupTable().reset(null, opt, ctx, null);
			if (!result) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * �Ѹ��������ɵ����
	 * @param newFile ������Ӧ���ļ�
	 * @param opt ѡ��
	 * @param ctx����������
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean resetGroupTable(File newFile, String opt, Context ctx) {
		TableMetaDataGroup tableGroup = open(null, ctx);
		TableMetaData baseTable = (TableMetaData) tableGroup.getTables()[0];
		
		boolean isCol = baseTable.getGroupTable() instanceof ColumnGroupTable;
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
			
			if (newFile == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.invalidParam"));
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
		String newOpt = null;
		String segmentCol = baseTable.getSegmentCol();
		if (segmentCol != null) {
			newOpt = "p";
		}
		
		GroupTable newGroupTable = null;
		try {
			//����������ļ�
			if (isCol) {
				newGroupTable = new ColumnGroupTable(newFile, colNames, null, newOpt, ctx);
				if (compress) {
					newGroupTable.setCompress(true);
				} else if (uncompress) {
					newGroupTable.setCompress(false);
				} else {
					newGroupTable.setCompress(baseTable.getGroupTable().isCompress());
				}
			} else {
				newGroupTable = new RowGroupTable(newFile, colNames, null, newOpt, ctx);
			}
			
			//����ֶ�
			boolean needSeg = baseTable.getSegmentCol() != null;
			if (needSeg) {
				newGroupTable.getBaseTable().setSegmentCol(baseTable.getSegmentCol(), baseTable.getSegmentSerialLen());
			}
			
			if (hasN) {
				newGroupTable.close();
				return Boolean.TRUE;
			}
			
			//�»���
			TableMetaData newBaseTable = newGroupTable.getBaseTable();
			ICursor cs = tableGroup.merge(ctx);

			newBaseTable.append(cs);
			newBaseTable.appendCache();
			
			//������ӱ�
			ArrayList<TableMetaData> tableList = baseTable.getTableList();
			for (TableMetaData t : tableList) {
				colNames = t.getColNames();
				len = colNames.length;
				if (t instanceof ColumnTableMetaData) {
					for (int i = 0; i < len; i++) {
						ColumnMetaData col = ((ColumnTableMetaData)t).getColumn(colNames[i]);
						if (col.isDim()) {
							colNames[i] = "#" + colNames[i];
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						boolean[] isDim = ((RowTableMetaData)t).getDimIndex();
						if (isDim[i]) {
							colNames[i] = "#" + colNames[i];
						}
					}
				}
				TableMetaData newTable = newBaseTable.createAnnexTable(colNames, t.getSerialBytesLen(), t.getTableName());
				cs = ((TableMetaDataGroup) tableGroup.getAnnexTable(t.getTableName())).merge(ctx);
				newTable.append(cs);
			}
		
		} catch (Exception e) {
			if (newGroupTable != null) newGroupTable.close();
			newFile.delete();
			throw new RQException(e.getMessage(), e);
		}

		newGroupTable.close();

		try{
			newGroupTable = GroupTable.open(newFile, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		//�ؽ������ļ���cuboid
		newGroupTable.getBaseTable().resetIndex(ctx);
		newGroupTable.getBaseTable().resetCuboid(ctx);
		ArrayList<TableMetaData> newTableList = newGroupTable.getBaseTable().getTableList();
		for (TableMetaData table : newTableList) {
			table.resetIndex(ctx);
			table.resetCuboid(ctx);
		}
		newGroupTable.close();
		
		return Boolean.TRUE;
	}
	
	/**
	 * �Ѹ��������������һ�����������зֲ����ʽ�������ݵķֲ�
	 * @param newFileGroup ���ļ���
	 * @param opt ѡ��
	 * @param distribute �ֲ����ʽ
	 * @param ctx����������
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean resetGroupTable(FileGroup newFileGroup, String opt, String distribute, Context ctx) {
		if (distribute == null || distribute.length() == 0) {
			// �ֲ�����
			int pcount = partitions.length;
			if (pcount != newFileGroup.partitions.length) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.paramCountNotMatch"));
			}
			
			for (int i = 0; i < pcount; ++i) {
				File file = getPartitionFile(i);
				File newFile = newFileGroup.getPartitionFile(i);
				
				TableMetaData tmd = GroupTable.openBaseTable(file, ctx);
				boolean result = tmd.getGroupTable().reset(newFile, opt, ctx, null);
				if (!result) {
					return false;
				}
			}
		} else {
			// �����ֲ����ʽ
			TableMetaDataGroup tableGroup = open(null, ctx);
			TableMetaData baseTable = (TableMetaData) tableGroup.getTables()[0];
			boolean isCol = baseTable.getGroupTable() instanceof ColumnGroupTable;
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
				TableMetaDataGroup newTableGroup = newFileGroup.create(colNames, distribute, newOpt, ctx);
				ICursor cs = tableGroup.merge(ctx);
				newTableGroup.append(cs, "xi");
				
				//д�ӱ�
				ArrayList<TableMetaData> tableList = baseTable.getTableList();
				for (TableMetaData t : tableList) {
					len = t.getColNames().length;
					colNames = Arrays.copyOf(t.getColNames(), len);
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
					ITableMetaData newTable = newTableGroup.createAnnexTable(colNames, t.getSerialBytesLen(), t.getTableName());
					
					//������α꣬ȡ���ֶ���Ҫ�������������ֶΣ�������Ϊ��Ҫ����ֲ�
					String[] allColNames = Arrays.copyOf(srcColNames, srcColNames.length + t.getColNames().length);
					System.arraycopy(t.getColNames(), 0, allColNames, srcColNames.length, t.getColNames().length);
					cs = tableGroup.getAnnexTable(t.getTableName()).cursor(allColNames);
					newTable.append(cs, "xi");
				}

				newTableGroup.close();
				return Boolean.TRUE;
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		return true;
	}
	
	/**
	 * ȡָ����Ŷ�Ӧ�ķֱ��ļ�
	 * @param index ��ţ���0��ʼ����
	 * @return
	 */
	public File getPartitionFile(int index) {
		return Env.getPartitionFile(partitions[index], fileName);
	}
	
	/**
	 * ȡ�ֱ���
	 * @return �ֱ���
	 */
	public int getPartitionCount() {
		return partitions.length;
	}
}