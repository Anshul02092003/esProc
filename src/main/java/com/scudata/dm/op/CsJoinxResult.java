package com.scudata.dm.op;

import java.util.ArrayList;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MergesCursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;

/**
 * �������������л������������ӵĽ��������
 * cs.joinx(C:��,f:K:��,x:F,��;��;��;n)
 * @author RunQian
 *
 */
public class CsJoinxResult implements IResult {
	private Object []fileTable;
	private Expression [][]fields;
	private Expression [][]keys;
	private Expression [][]exps;
	private String [][]names;
	private String fname;
	private Context ctx;
	private String option;
	private int capacity;

	private Sequence outTable;
	private ArrayList<ICursor> cursorList = new ArrayList<ICursor>();
	
	public CsJoinxResult(Expression [][]fields, Object []fileTable, Expression[][] keys, 
			Expression[][] exps, String[][] expNames, String fname, Context ctx, String option, int capacity) {
		this.fileTable = fileTable;
		this.fields = fields;
		this.keys = keys;
		this.exps = exps;
		this.names = expNames;
		this.fname = fname;
		this.ctx = ctx;
		this.option = option;
		this.capacity = capacity;
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		try {
			Sequence result = CursorUtil.joinx(table, fields, fileTable, keys, exps, names, fname, ctx, option);
			if (result == null) return;
			if (outTable == null) {
				outTable = result;
			} else {
				if (outTable.length() + result.length() >= capacity) {
					FileObject fo = FileObject.createTempFileObject();
					MessageManager mm = EngineMessage.get();
					Logger.info(mm.getMessage("engine.createTmpFile") + fo.getFileName());

					fo.exportSeries(outTable, "b", null);
					fo.exportSeries(result, "ab", null);
					BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
					cursorList.add(bfc);

					outTable.clear();
					outTable = null;
					result.clear();
				} else {
					outTable.addAll(result);
				}
			}
			
		} catch(RuntimeException e) {
			delete();
			throw e;
		}
	}
	
	private void delete() {
		this.outTable = null;
		
		for (ICursor cursor : cursorList) {
			cursor.close();
		}
	}
	
	/**
	 * ȡ������α�
	 * @return ICursor
	 */
	public ICursor getResultCursor() {
		ArrayList<ICursor> cursorList = this.cursorList;
		int size = cursorList.size();
		if (size > 0) {
			int bufSize = Env.getMergeFileBufSize(size);
			for (int i = 0; i < size; ++i) {
				BFileCursor bfc = (BFileCursor)cursorList.get(i);
				bfc.setFileBufferSize(bufSize);
			}
		}

		if (outTable != null && outTable.length() > 0) {
			cursorList.add(new MemoryCursor(outTable));
			size++;
		}

		this.outTable = null;

		if (size == 0) {
			return null;
		} else if (size == 1) {
			return (ICursor)cursorList.get(0);
		} else {
			int keyCount = exps.length;
			ICursor []cursors = new ICursor[size];
			cursorList.toArray(cursors);
			Expression []keyExps = new Expression[keyCount];
			for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
				keyExps[i] = new Expression(ctx, "#" + q);
			}

			MergesCursor mc = new MergesCursor(cursors, keyExps, ctx);
			return mc;
		}
	}
	
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return
	  */
	public Object result() {
		return getResultCursor();
	}
	
	/**
	 * ��֧�ִ˺���
	 */
	public Object combineResult(Object []results) {
		throw new RuntimeException();
	}
}