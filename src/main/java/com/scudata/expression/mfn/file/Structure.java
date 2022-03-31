package com.scudata.expression.mfn.file;

import java.io.File;
import java.util.ArrayList;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dw.Cuboid;
import com.scudata.dw.GroupTable;
import com.scudata.dw.ITableIndex;
import com.scudata.dw.RowTableMetaData;
import com.scudata.dw.TableMetaData;
import com.scudata.expression.FileFunction;
import com.scudata.parallel.ClusterFile;
import com.scudata.parallel.ClusterTableMetaData;

/**
 * �������ļ��Ľṹ
 * f.structure()
 * @author LiWei
 *
 */
public class Structure extends FileFunction {
	private static final String FIELD_NAMES[] = { "field", "keys", "row", "zip", "seg", "zonex", "index", "cuboid", "attach" };
	private static final String ATTACH_FIELD_NAMES[] = { "name", "field", "keys", "row", "zip", "seg", "zonex", "index", "cuboid", "attach" };
	private static final String CUBOID_FIELD_NAMES[] = { "name", "keys", "aggr" };
	private static final String CUBOID_AGGR_FIELD_NAMES[] = { "name", "exp" };
	
	public Object calculate(Context ctx) {
		if (file.isRemoteFile()) {
			// Զ���ļ�
			String host = file.getIP();
			int port = file.getPort();
			String fileName = file.getFileName();
			Integer partition = file.getPartition();
			int p = partition == null ? -1 : partition.intValue();
			ClusterFile cf = new ClusterFile(host, port, fileName, p, ctx);
			ClusterTableMetaData table = cf.openGroupTable(ctx);
			Sequence seq = new Sequence();
			seq.add(getTableStruct(table, option));
			table.close();
			return seq;
		} else {
			// �����ļ�
			File f = file.getLocalFile().file();
			TableMetaData table = GroupTable.openBaseTable(f, ctx);
			
			Integer partition = file.getPartition();
			if (partition != null && partition.intValue() > 0) {
				table.getGroupTable().setPartition(partition);
			}
			Sequence seq = new Sequence();
			seq.add(getTableStruct(table, option));
			table.close();
			return seq;
		}
	}
	
	protected static Record getTableStruct(ClusterTableMetaData table, String option) {
		return table.getStructure();
	}
	
	/**
	 * ���table�Ľṹ�����浽out��
	 * @param table
	 */
	protected static Record getTableStruct(TableMetaData table, String option) {
		int idx = 0;
		boolean hasI = false;
		boolean hasC = false;
		if (option != null) {
			if (option.indexOf('i') != -1)
				hasI = true;
			if (option.indexOf('c') != -1)
				hasC = true;
		}
		
		Record rec;
		if (table.isBaseTable()) {
			rec = new Record(new DataStruct(FIELD_NAMES));
		} else {
			rec = new Record(new DataStruct(ATTACH_FIELD_NAMES));
			rec.setNormalFieldValue(idx++, table.getTableName());
		}
		
		String[] colNames = table.getAllColNames();
		rec.setNormalFieldValue(idx++, new Sequence(colNames));
		rec.setNormalFieldValue(idx++, new Sequence(table.getAllKeyColNames()));
		rec.setNormalFieldValue(idx++, table instanceof RowTableMetaData);
		rec.setNormalFieldValue(idx++, table.getGroupTable().isCompress());
		
		String seg = table.getSegmentCol();
		rec.setNormalFieldValue(idx++, seg != null && colNames[0] != null && seg.equals(colNames[0]));
		rec.setNormalFieldValue(idx++, table.getGroupTable().getDistribute());
		if (hasI) {
			rec.setNormalFieldValue(idx, getTableIndexStruct(table));
		}
		idx++;
		
		if (hasC) {
			rec.setNormalFieldValue(idx, getTableCuboidStruct(table));
		}
		idx++;
		
		ArrayList<TableMetaData> tables = table.getTableList();
		if (tables != null && tables.size() > 0) {
			Sequence seq = new Sequence();
			for (TableMetaData tbl : tables) {
				seq.add(getTableStruct(tbl, option));
			}
			rec.setNormalFieldValue(idx, seq);
		}
		
		return rec;
	}
	
	/**
	 * ���table�������Ľṹ
	 * @param table
	 * @returnhy
	 */
	protected static Sequence getTableIndexStruct(TableMetaData table) {
		String inames[] = table.getIndexNames();
		if (inames == null) {
			return null;
		}
		Sequence seq = new Sequence();
		String dir = table.getGroupTable().getFile().getAbsolutePath() + "_";
		for (String iname: inames) {
			FileObject indexFile = new FileObject(dir + table.getTableName() + "_" + iname);
			if (indexFile.isExists()) {
				ITableIndex index = table.getTableMetaDataIndex(indexFile, iname, true);
				seq.add(index.getIndexStruct());
			}
		}
		return seq;
	}
	
	/**
	 * ���table��Ԥ����Ľṹ
	 * @param table
	 * @return
	 */
	protected static Sequence getTableCuboidStruct(TableMetaData table) {
		String cuboids[] = table.getCuboids();
		if (cuboids == null) {
			return null;
		}

		Sequence seq = new Sequence();
		String dir = table.getGroupTable().getFile().getAbsolutePath() + "_";
		for (String cuboid: cuboids) {
			FileObject fo = new FileObject(dir + table.getTableName() + Cuboid.CUBE_PREFIX + cuboid);
			File file = fo.getLocalFile().file();
			Cuboid srcCuboid = null;
			
			try {
				srcCuboid = new Cuboid(file, null);
				Record rec = new Record(new DataStruct(CUBOID_FIELD_NAMES));
				rec.setNormalFieldValue(0, cuboid);
				rec.setNormalFieldValue(1, new Sequence(srcCuboid.getExps()));//������ʽ
				
				/**
				 * ��֯���ܱ��ʽ
				 */
				Sequence aggr = new Sequence();
				String[] newExps = srcCuboid.getNewExps();//���ܱ��ʽ
				String[] names = srcCuboid.getBaseTable().getAllColNames();//����ĺ�벿���ǻ��ܱ��ʽ��name
				int len = newExps.length;
				int start = names.length - len;
				for (int i = 0; i < len; i++) {
					Record r = new Record(new DataStruct(CUBOID_AGGR_FIELD_NAMES));
					r.setNormalFieldValue(0, names[start + i]);
					r.setNormalFieldValue(1, newExps[i]);
					aggr.add(r);
				}
				rec.setNormalFieldValue(2, aggr);
				seq.add(rec);
				srcCuboid.close();
			} catch (Exception e) {
				if (srcCuboid != null) srcCuboid.close();
			}
		}
		return seq;
	}
}
