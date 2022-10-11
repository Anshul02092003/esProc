package com.scudata.dm.cursor;

import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.*;
import com.scudata.dm.op.Operation;
import com.scudata.dw.ColumnTableMetaData;
import com.scudata.dw.Cursor;
import com.scudata.expression.Expression;
import com.scudata.util.CursorUtil;

/**
 * �α�joinx�࣬�鲢joinx
 * �α���һ���ɷֶμ��ļ���ʵ��T���鲢join���㡣
 * @author LiWei
 * 
 */
public class CSJoinxCursor3 extends ICursor {
	private ICursor srcCursor;//Դ�α�
	private Expression []keys;//ά���ֶ�
	private Expression []exps;//�µı��ʽ
	
	private ICursor mergeCursor;//�鲢�α�
	private DataStruct ds = null;
	private int len1;//ԭ��¼�ֶ���
	private int len2;//�±��ʽ�ֶ���
	
	private boolean isEnd;
	private int n;//����������
	
	/**
	 * ������
	 * @param cursor	Դ�α�
	 * @param fields	��ʵjoin���ֶ�
	 * @param fileTable	ά�����
	 * @param keys		ά��join�ֶ�
	 * @param exps		ά���±��ʽ
	 * @param expNames	ά���±��ʽ����
	 * @param fname
	 * @param ctx
	 * @param n
	 * @param option
	 */
	public CSJoinxCursor3(ICursor cursor, Expression []fields, Object fileTable, 
			Expression[] keys, Expression[] exps, String[] expNames, String fname, Context ctx, int n, String option) {
		srcCursor = cursor;
		this.keys = keys;
		this.exps = exps;
		this.ctx = ctx;
		this.n = n;
		if (this.n < ICursor.FETCHCOUNT) {
			this.n = ICursor.FETCHCOUNT;
		}
		
		//���newNames����null������newExps���
		if (exps != null && expNames != null) {
			for (int i = 0, len = expNames.length; i < len; i++) {
				if (expNames[i] == null && exps[i] != null) {
					expNames[i] = exps[i].getFieldName();
				}
			}
		}

		//�鲢�����α�
		ICursor cursors[] = {cursor, toCursor(fileTable)};
		String names[] = {null, null};
		Expression joinKeys[][] = {fields, keys};
		mergeCursor = CursorUtil.joinx(cursors, names, joinKeys, option, ctx);
		
		//��֯���ݽṹ
		Sequence temp = mergeCursor.peek(1);
		Record r = (Record) temp.getMem(1);
		Record r1 = (Record) r.getNormalFieldValue(0);
		len1 = r1.getFieldCount();
		len2 = exps == null ? 0 : exps.length;
		names = new String[len1 + len2];
		System.arraycopy(r1.getFieldNames(), 0, names, 0, len1);
		System.arraycopy(expNames, 0, names, len1, len2);
		ds = new DataStruct(names);
	}

	/**
	 * ��join�ֶκ��±��ʽ����ȡ��Ҫ���ֶ�
	 * @param dataExps
	 * @param newExps
	 * @param ctx
	 * @return
	 */
	private static String[] makeFields(Expression []dataExps, Expression []newExps ,Context ctx) {
		int len = dataExps.length;
		ArrayList<String> keys = new ArrayList<String>(len);
		for (int j = 0; j < len; j++) {
			keys.add(dataExps[j].toString());
		}
		for (Expression exp : newExps) {
			exp.getUsedFields(ctx, keys);
		}
		String[] arr = new String[keys.size()];
		keys.toArray(arr);
		return arr;
	}
	
	/**
	 * ��ά�����ת�����α�
	 * @param obj
	 * @return
	 */
	private ICursor toCursor(Object obj) {
		if (obj instanceof ColumnTableMetaData) {
			String fields[] = makeFields(keys, exps, ctx);
			return (Cursor) ((ColumnTableMetaData) obj).cursor(null, fields, null, null, null, null, ctx);
		} else if (obj instanceof FileObject) {
			return new BFileCursor((FileObject) obj, null, null, null);
		} else if (obj instanceof ICursor) {
			return (ICursor) obj;
		} else {
			return null;
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			exps = Operation.dupExpressions(exps, ctx);
			super.resetContext(ctx);
		}
	}

	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		Sequence temp = mergeCursor.fetch(n);
		if (temp == null || temp.length() == 0) {
			close();
			return null;
		}
		
		Context ctx = this.ctx;
		Expression []exps = this.exps;
		int len1 = this.len1;
		int len2 = this.len2;
		int len = temp.length();
		Table result = new Table(ds);
		for (int i = 1; i <= len; i++) {
			Record r = (Record) temp.getMem(i);
			Record r1 = (Record) r.getNormalFieldValue(0);
			Record r2 = (Record) r.getNormalFieldValue(1);
			
			Record record = result.newLast(r1.getFieldValues());
			for (int f = 0; f < len2; f++) {
				record.setNormalFieldValue(f + len1, r2.calc(exps[f], ctx));
			}
		}
		
		if (len < n) {
			close();
		}
		return result;
	}

	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		Sequence seq = get((int) n);
		if (seq != null) {
			return seq.length();
		} else {
			return 0;
		}
	}

	public synchronized void close() {
		super.close();
		srcCursor.close();
		isEnd = true;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		super.close();
		srcCursor.reset();
		isEnd = false;
		return true;
	}
	
	/**
	 * �鲢join���������ݣ�
	 * @param cursor	Դ�α�
	 * @param fields	��ʵjoin���ֶ�
	 * @param fileTable	ά�����
	 * @param keys		ά��join�ֶ�
	 * @param exps		ά���±��ʽ
	 * @param expNames	ά���±��ʽ����
	 * @param fname
	 * @param ctx
	 * @param n
	 * @param option
	 * @return
	 */
	public static ICursor MergeJoinx(ICursor cursor, Expression[][] fields, Object[] fileTable, Expression[][] keys,
			Expression[][] exps, String[][] expNames, String fname, Context ctx, int n, String option) {
		if (fileTable == null) {
			return null;
		}
		
		if (cursor instanceof MultipathCursors) {
			return MultipathMergeJoinx((MultipathCursors)cursor, fields, fileTable, keys,
					exps, expNames, fname, ctx, n, option);
		}

		ICursor temp = null;
		FileObject tempFile = null;
		int fileCount =  fileTable.length;
		try {
			/**
			 * �Զ������ݽ���join��ÿ�εĽ��д�����ļ��������������һ��join��
			 */
			for (int i = 0; i < fileCount - 1; i++) {
				temp = new CSJoinxCursor3(cursor, fields[i], fileTable[i], keys[i], exps[i], 
						expNames[i], fname, ctx, n, option);
				
				tempFile = FileObject.createTempFileObject();
				cursor = new BFileCursor(tempFile, null, "x", ctx);
				tempFile.setFileSize(0);
				
				Sequence table = temp.fetch(FETCHCOUNT);
				while (table != null && table.length() != 0) {
					tempFile.exportSeries(table, "ab", null);
					table = temp.fetch(FETCHCOUNT);
				}
				temp = null;
			}
		} catch (Exception e) {
			if (temp != null) {
				temp.close();
			}
			if (tempFile != null && tempFile.isExists()) {
				tempFile.delete();
			}
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		int i = fileCount - 1;
		return new CSJoinxCursor3(cursor, fields[i], fileTable[i], keys[i], exps[i], 
				expNames[i], fname, ctx, n, option);
	}
	
	public static ICursor MultipathMergeJoinx(MultipathCursors cursor, Expression[][] fields, Object[] fileTable, Expression[][] keys,
			Expression[][] exps, String[][] expNames, String fname, Context ctx, int n, String option) {

		ICursor[] cursors = cursor.getParallelCursors();
		int pathCount = cursor.getPathCount();
		ICursor results[] = new ICursor[pathCount];
		for (int i = 0; i < pathCount; ++i) {
			results[i] = MergeJoinx(cursors[i], fields, fileTable, keys, exps, expNames, fname, ctx, n, option);
		}
		return new MultipathCursors(results, ctx);
		
	}
}
