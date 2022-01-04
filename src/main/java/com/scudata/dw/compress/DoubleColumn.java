package com.scudata.dw.compress;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dw.BufferReader;
import com.scudata.resources.EngineMessage;

public class DoubleColumn extends Column {
	// ����Сֵ��ʾnull
	private static final double NULL = Double.NaN;
	
	// ���ݰ���洢��ÿ����Column.BLOCK_RECORD_COUNT����¼
	private ArrayList<double[]> blockList = new ArrayList<double[]>(1024);
	private int lastRecordCount = Column.BLOCK_RECORD_COUNT; // ���һ��ļ�¼��
	
	public void addData(Object data) {
		double value;
		if (data instanceof Number) {
			value = ((Number)data).doubleValue();
		} else if (data == null) {
			value = NULL;
		} else {
			// ���쳣
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.colTypeDif"));
		}
		
		if (lastRecordCount < Column.BLOCK_RECORD_COUNT) {
			double []block = blockList.get(blockList.size() - 1);
			block[lastRecordCount++] = value;
		} else {
			double []block = new double[Column.BLOCK_RECORD_COUNT];
			block[0] = value;
			blockList.add(block);
			lastRecordCount = 1;
		}
	}
	
	// ȡ��row�е�����
	public Object getData(int row) {
		// row�кţ���1��ʼ����
		row--;
		double []block = blockList.get(row / Column.BLOCK_RECORD_COUNT);
		double value = block[row % Column.BLOCK_RECORD_COUNT];
		if (value != NULL) {
			return new Double(value);
		} else {
			return null;
		}
	}
	
	public Column clone() {
		return new DoubleColumn();
	}
	
	public void appendData(BufferReader br) throws IOException {
		addData(br.readObject());
	}
}
