package com.scudata.dw.compress;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.dw.BufferReader;

public class SeqRefColumn extends Column {
	private Sequence table; // ���õı�
	
	// ���ݰ���洢��ÿ����Column.BLOCK_RECORD_COUNT����¼
	private ArrayList<int[]> blockList = new ArrayList<int[]>(1024);
	private int lastRecordCount = Column.BLOCK_RECORD_COUNT; // ���һ��ļ�¼��
	
	public SeqRefColumn() {
	}
	
	public SeqRefColumn(Sequence table) {
		this.table = table;
	}
	
	/**
	 * ������ü�¼�����
	 * @param seq ָ���ֶ�ָ��ļ�¼�������е����
	 */
	public void addData(int seq) {
		if (lastRecordCount < Column.BLOCK_RECORD_COUNT) {
			int []block = blockList.get(blockList.size() - 1);
			block[lastRecordCount++] = seq;
		} else {
			int []block = new int[Column.BLOCK_RECORD_COUNT];
			block[0] = seq;
			blockList.add(block);
			lastRecordCount = 1;
		}
	}
	
	public void addData(Object data) {
		throw new RQException();
	}
	
	// ȡ��row�е�����
	public Object getData(int row) {
		// row�кţ���1��ʼ����
		row--;
		int []block = blockList.get(row / Column.BLOCK_RECORD_COUNT);
		int value = block[row % Column.BLOCK_RECORD_COUNT];
		if (value > 0) {
			return table.get(value);
		} else {
			return null;
		}
	}
	
	// ȡ��row�е�seq
	public int getSeq(int row) {
		// row�кţ���1��ʼ����
		row--;
		int []block = blockList.get(row / Column.BLOCK_RECORD_COUNT);
		int value = block[row % Column.BLOCK_RECORD_COUNT];
		return value;
	}
		
	public Column clone() {
		return new SeqRefColumn(table);
	}
	
	public void appendData(BufferReader br) throws IOException {
		addData(br.readObject());
	}
}
