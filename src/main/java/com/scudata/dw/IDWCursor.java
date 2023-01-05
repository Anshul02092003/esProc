package com.scudata.dw;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.ObjectReader;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;

public abstract class IDWCursor extends ICursor {
	abstract public void setAppendData(Sequence seq);
	abstract public void setSegment(int startBlock, int endBlock);
	abstract public PhyTable getTableMetaData();
	abstract protected Sequence get(int n);
	abstract protected Sequence getStartBlockData(int n);//ֻ����һ�������,����appendData
	abstract public void setCache(Sequence cache);
	
	abstract public int getStartBlock();
	abstract public int getEndBlock();
	abstract public void setEndBlock(int endBlock);
	
	private String option;
	
	public void setOption(String option) {
		this.option = option;
		if (option != null && option.indexOf('x') != -1) {
			getTableMetaData().groupTable.openCursorEvent();
		}
	}
	
	public void close() {
		super.close();
		if (option != null && option.indexOf('x') != -1) {
			getTableMetaData().groupTable.closeCursorEvent();
		}
	}
	
	/**
	 * ȡ�ֶ��α����ʼֵ������зֶ��ֶ��򷵻طֶ��ֶε�ֵ��û���򷵻�ά�ֶε�ֵ
	 * @return �ֶ��α�������¼�ķֶ��ֶε�ֵ�������ǰ����Ϊ0�򷵻�null
	 */
	public Object[] getSegmentStartValues() {
		ColPhyTable table = (ColPhyTable) getTableMetaData();
		int startBlock = getStartBlock();
		String segmentCol = table.getSegmentCol();
		try {
			if (segmentCol != null) {
				Object[] startValues = new Object[1];
				ObjectReader reader = table.getColumn(segmentCol).getSegmentReader();
				for (int i = 0; i < startBlock; ++i) {
					reader.readLong40();
					reader.skipObject();
					reader.skipObject();
					reader.skipObject();
				}
				reader.readLong40();
				reader.skipObject();
				reader.skipObject();
				startValues[0] = reader.readObject(); //startValue
				return startValues;
			} else {
				ColumnMetaData[] cols = table.getSortedColumns();
				int colCount = cols.length;
				Object[] startValues = new Object[colCount];
				ObjectReader []readers = new ObjectReader[colCount];
				for (int f = 0; f < colCount; ++f) {
					readers[f] = cols[f].getSegmentReader();
				}
				
				for (int i = 0; i < startBlock; ++i) {
					for (int f = 0; f < colCount; ++f) {
						readers[f].readLong40();
						readers[f].skipObject();
						readers[f].skipObject();
						readers[f].skipObject();
					}
					
				}
				for (int f = 0; f < colCount; ++f) {
					readers[f].readLong40();
					readers[f].skipObject();
					readers[f].skipObject();
					startValues[f] = readers[f].readObject();
				}
				return startValues;
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
}