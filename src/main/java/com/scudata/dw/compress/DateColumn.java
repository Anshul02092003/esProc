package com.scudata.dw.compress;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dw.BufferReader;
import com.scudata.resources.EngineMessage;

public class DateColumn extends Column {
	// ����Сֵ��ʾnull
	private static final long NULL = Long.MIN_VALUE;
	
	// ���ݰ���洢��ÿ����Column.BLOCK_RECORD_COUNT����¼
	private ArrayList<long[]> blockList = new ArrayList<long[]>(1024);
	private int lastRecordCount = Column.BLOCK_RECORD_COUNT; // ���һ��ļ�¼��
	
	public void addData(Object data) {
		long value;
		if (data instanceof java.util.Date) {
			value = ((java.util.Date)data).getTime();
		} else if (data == null) {
			value = NULL;
		} else {
			// ���쳣
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.colTypeDif"));
		}
		
		if (lastRecordCount < Column.BLOCK_RECORD_COUNT) {
			long []block = blockList.get(blockList.size() - 1);
			block[lastRecordCount++] = value;
		} else {
			long []block = new long[Column.BLOCK_RECORD_COUNT];
			block[0] = value;
			blockList.add(block);
			lastRecordCount = 1;
		}
	}
	
	/**
	 * ȡ��row�е�����
	 */
	public Object getData(int row) {
		// row�кţ���1��ʼ����
		row--;
		long []block = blockList.get(row / Column.BLOCK_RECORD_COUNT);
		long value = block[row % Column.BLOCK_RECORD_COUNT];
		if (value != NULL) {
			return new Date(value);
		} else {
			return null;
		}
	}
	
	public Column clone() {
		return new DateColumn();
	}
	
	public void appendData(BufferReader br) throws IOException {
		addData(br.readObject());
	}
}
