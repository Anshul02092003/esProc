package com.scudata.util;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileWriter;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.BFileSortxCursor;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ���ļ��Ĺ�����
 * @author LW
 *
 */
public class BFileUtil {
	/**
	 * �Լ��ļ������������
	 * @param file �ļ�
	 * @param outFile ����ļ�
	 * @param fields �����ֶ�����
	 * @param ctx ����������
	 * @param opt ѡ��(��ʱ����)
	 * @return �ź�����α�
	 */
	public static void sortx(FileObject file, FileObject outFile, String[] fields, Context ctx, String opt) {
		int fcount = fields.length;
		BFileSortxCursor cursor = new BFileSortxCursor(file, fields);
		DataStruct ds  = cursor.getFileDataStruct();
		
		Expression[] tempExps = new Expression[fcount];
		for (int i = 0; i < fcount; i++) {
			tempExps[i] = new Expression(fields[i]);
		}
		EnvUtil.setMaxUsedMemoryPercent(0.2);
		ICursor cs = CursorUtil.sortx(cursor, tempExps, ctx, 0, opt);
		EnvUtil.setMaxUsedMemoryPercent(0.4);
		
		BFileWriter writer = new BFileWriter(outFile, null);
		writer.exportBinary(cs, ds, fcount, ctx);
		writer.close();
	}
	
	/**
	 * �Լ��ļ������н����������
	 * @param files �ļ�����
	 * @param outFile ����ļ�
	 * @param fields �����ֶ�����
	 * @param ctx ����������
	 * @param opt ѡ��(��ʱ����)
	 * @return �ź�����α�
	 */
	public static void sortx(Sequence files, FileObject outFile, String[] fields, Context ctx, String opt) {
		int fcount = fields.length;
		
		int len = files.length();
		BFileSortxCursor[] cursors = new BFileSortxCursor[len];
		for (int i = 1; i <= len; i++) {
			Object obj = files.get(i);
			if (obj instanceof FileObject) {
				FileObject file = (FileObject) obj;
				cursors[i] = new BFileSortxCursor(file, fields);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
			}
		}
		ConjxCursor cursor = new ConjxCursor(cursors);
		
		DataStruct ds  = cursors[0].getFileDataStruct(); 
		Expression[] tempExps = new Expression[fcount];
		for (int i = 0; i < fcount; i++) {
			tempExps[i] = new Expression(fields[i]);
		}
		
		EnvUtil.setMaxUsedMemoryPercent(0.2);
		ICursor cs = CursorUtil.sortx(cursor, tempExps, ctx, 0, opt);
		EnvUtil.setMaxUsedMemoryPercent(0.4);
		
		BFileWriter writer = new BFileWriter(outFile, null);
		writer.exportBinary(cs, ds, fcount, ctx);
		writer.close();
	}
}
