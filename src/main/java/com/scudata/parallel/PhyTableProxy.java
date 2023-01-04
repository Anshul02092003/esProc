package com.scudata.parallel;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dw.IPhyTable;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ������
 * @author RunQian
 *
 */
public class PhyTableProxy extends IProxy {
	private IPhyTable tableMetaData;
	private FileObject tempFile;//append��ʱ�ļ�
	
	public PhyTableProxy(IPhyTable tableMetaData) {
		this.tableMetaData = tableMetaData;
	}
	
	public IPhyTable getTableMetaData() {
		return tableMetaData;
	}
	
	public IPhyTable attach(String tableName) {
		IPhyTable table = tableMetaData.getAnnexTable(tableName);
		if (table == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(tableName + mm.getMessage("dw.tableNotExist"));
		}
		
		return table;
	}
	
	// ȡappend�����õ���ʱ�ļ�
	public FileObject getTempFile() {
		return tempFile;
	}

	// ������ʱ�ļ������ڴ��append����
	public void createTempFile() {
		tempFile = FileObject.createTempFileObject();
	}

	public void close() {
		tableMetaData.close();
	}
		
	public ICursor icursor(String []fields, Expression filter, String iname, String opt, Context ctx) {
		return tableMetaData.icursor(fields, filter, iname, opt, ctx);
	}
	
	public String[] getAllSortedColNames() {
		return tableMetaData.getAllSortedColNames();
	}
}