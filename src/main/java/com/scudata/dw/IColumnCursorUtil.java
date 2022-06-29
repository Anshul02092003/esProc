package com.scudata.dw;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;

/**
 * ��ʽ�α�ӿ�
 * @author runqian
 *
 */
abstract public class IColumnCursorUtil {
	public static IColumnCursorUtil util;
	
	static {
		try {
			Class<?> cls = Class.forName("com.scudata.dw.columns.ColumnCursorUtil");
			util = (IColumnCursorUtil) cls.newInstance();
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
	}
	
	public abstract ICursor cursor(ITableMetaData table);

	public abstract ICursor cursor(ITableMetaData table, String []fields, Expression filter, Context ctx);
	
	public abstract ICursor cursor(ITableMetaData table, Expression []exps, String []fields, Expression filter, 
			String []fkNames, Sequence []codes, String []opts, Context ctx);
	
	public abstract ICursor cursor(ITableMetaData table, Expression []exps, String []fields, Expression filter, String []fkNames, 
			Sequence []codes, String []opts, int pathCount, Context ctx);
	
	public abstract ICursor cursor(ITableMetaData table, Expression []exps, String []fields, Expression filter, String []fkNames, 
			Sequence []codes, String []opts, int segSeq, int segCount, Context ctx);
	
	public abstract ICursor cursor(ITableMetaData table, Expression []exps, String []fields, Expression filter, String []fkNames, 
			Sequence []codes,  String []opts, MultipathCursors mcs, String opt, Context ctx);
	
	public abstract ICursor createCursor(Object table, IParam param, String opt, Context ctx);
	
	public abstract IGroupsResult getGroupsResultInstance(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx);
	
	public abstract Table groups(ICursor cursor, Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx);
	
	public abstract Table groups(ICursor cursor, Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx, int groupCount);
	
	/**
	 * �α�Թ����ֶ�����������鲢����
	 * @param cursors �α�����
	 * @param names ������ֶ�������
	 * @param exps �����ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx Context ����������
	 * @return ICursor ������α�
	 */
	//public abstract ICursor joinx(ICursor []cursors, String []names, Expression [][]exps, String opt, Context ctx);
	
	/**
	 * ת��Ϊ�д����
	 * @param src
	 * @return
	 */
	public abstract Sequence convert(Sequence src);
	
	public abstract Sequence switchColumnTable(Sequence seq, boolean isIsect, boolean isDiff, String fkName, Sequence code,
			String timeName, IndexTable indexTable, DataStruct ds, int keySeq, boolean isLeft, Context ctx);
	
	public abstract Sequence derive(Sequence table, Expression[] exps, DataStruct newDs, boolean containNull, Context ctx);
	
	public abstract Object createMemoryTable(ICursor cursor, String[] keys, String opt);
}
