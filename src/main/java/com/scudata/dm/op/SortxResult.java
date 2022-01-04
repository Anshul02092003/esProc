package com.scudata.dm.op;

import java.util.ArrayList;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MergesCursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.EnvUtil;

/**
 * ���ڶ�������������ִ�������������
 * @author RunQian
 *
 */
public class SortxResult implements IResult {
	private Expression[] exps; // �����ֶα��ʽ����
	private int []orders; // ��������ÿ���ֶε�������
	private Context ctx; // ����������
	private int capacity; // �ڴ��ܹ���ŵļ�¼��
	private String opt; // ѡ��

	private Sequence data = new Sequence(); // ���������
	private ArrayList<ICursor> cursorList = new ArrayList<ICursor>();
	
	public SortxResult(Expression[] exps, Context ctx, int capacity, String opt) {
		this.exps = exps;
		this.ctx = ctx;
		this.capacity = capacity;
		this.opt = opt;
		
		int fcount = exps.length;
		orders = new int[fcount];
		for (int i = 0; i < fcount; ++i) {
			orders[i] = 1;
		}
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		if (capacity < 1) {
			Object obj = table.get(1);
			int fcount = 1;
			if (obj instanceof Record) {
				fcount = ((Record)obj).getFieldCount();
			}
			
			capacity = EnvUtil.getCapacity(fcount);
		}
		
		data.addAll(table);
		if (data.length() >= capacity) {
			Sequence sequence;
			if (exps.length == 1) {
				sequence = table.sort(exps[0], null, opt, ctx);
			} else {
				sequence = table.sort(exps, orders, null, opt, ctx);
			}
			
			FileObject fo = FileObject.createTempFileObject();
			MessageManager mm = EngineMessage.get();
			Logger.info(mm.getMessage("engine.createTmpFile") + fo.getFileName());
			
			fo.exportSeries(sequence, "b", null);
			BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
			cursorList.add(bfc);
			
			data = new Sequence();
		}
	}
	
	/**
	 * �������ͽ�����ȡ���յļ�����
	 * @return
	 */
	public Object result() {
		if (data == null) {
			return null;
		}
		
		if (data.length() > 0) {
			Sequence sequence;
			if (exps.length == 1) {
				sequence = data.sort(exps[0], null, null, ctx);
			} else {
				sequence = data.sort(exps, orders, null, null, ctx);
			}
			
			FileObject fo = FileObject.createTempFileObject();
			fo.exportSeries(sequence, "b", null);
			BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
			cursorList.add(bfc);
		}
		
		data = null;
		
		int size = cursorList.size();
		int bufSize = Env.getMergeFileBufSize(size);
		for (int i = 0; i < size; ++i) {
			BFileCursor bfc = (BFileCursor)cursorList.get(i);
			bfc.setFileBufferSize(bufSize);
		}

		if (size == 1) {
			return (ICursor)cursorList.get(0);
		} else {
			ICursor []cursors = new ICursor[size];
			cursorList.toArray(cursors);
			if (opt == null || opt.indexOf('0') == -1) {
				return new MergesCursor(cursors, exps, ctx);
			} else {
				return new MergesCursor(cursors, exps, "0", ctx);
			}
		}
	}

	public Object combineResult(Object []results) {
		throw new RuntimeException();
	}
}