package com.scudata.dw;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.ObjectReader;
import com.scudata.util.Variant;

/**
 * �д��¼������
 * @author runqian
 *
 */
class RowRecordSeqSearcher {
	private RowTableMetaData table;
	private long prevRecordCount = 0;//��ǰ�Ѿ�ȡ���ļ�¼��
	private int curBlock = -1;//��ǰ���
	private int totalBlockCount;//�ܿ���
	
	private BlockLinkReader rowReader;
	private ObjectReader segmentReader;
	
	private long position; // ��ǰ���λ��
	private Object []minValues; // ��ǰ���ÿ�����е���Сֵ
	private Object []maxValues; // ��ǰ���ÿ�����е����ֵ
	
	private int curRecordCount = 0; // ��ǰ��ļ�¼��
	private int curIndex = -1; // ��������������-1����黹û����
	private Object [][]blockKeyValues;
	private boolean isEnd = false;
	
	public RowRecordSeqSearcher(RowTableMetaData table) {
		this.table = table;
		init();
	}
	
	private void init() {
		totalBlockCount = table.getDataBlockCount();
		if (totalBlockCount == 0) {
			isEnd = true;
			return;
		}
		
		String[] columns = table.getSortedColNames();
		int keyCount = columns.length;
		rowReader = table.getRowReader(true);
		segmentReader = table.getSegmentObjectReader();
		minValues = new Object[keyCount];
		maxValues = new Object[keyCount];
		blockKeyValues = new Object[keyCount][];

		nextBlock();
	}
	
	private boolean nextBlock() {
		prevRecordCount += curRecordCount;
		curIndex = -1;
		
		if (++curBlock == totalBlockCount) {
			isEnd = true;
			return false;
		}

		try {
			curRecordCount = segmentReader.readInt32();
			position = segmentReader.readLong40();
			int keyCount = minValues.length;			
			for (int k = 0; k < keyCount; ++k) {
				minValues[k] = segmentReader.readObject();
				maxValues[k] = segmentReader.readObject();
			}
			
			//����Ƕ��ֶ�ά����ȡ���һ��άֵ��Ϊmax
			if (keyCount > 1) {
				loadKeyValues();
				int idx = blockKeyValues[0].length - 1;
				for (int k = 0; k < keyCount; ++k) {
					maxValues[k] = blockKeyValues[k][idx];
				}
			}
			return true;
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	private void loadKeyValues() {
		try {
			int keyCount = blockKeyValues.length;
			int skipCount = table.getColNames().length - keyCount;
			int count = curRecordCount + 1;
			BufferReader reader = rowReader.readBlockBuffer(position);
			Object [][]vals = new Object[keyCount][count];
			
			for (int k = 0; k < keyCount; ++k) {
				blockKeyValues[k] = vals[k];
			}
			
			for (int i = 1; i < count; ++i) {
				reader.skipObject();//����α��
				for (int k = 0; k < keyCount; ++k) {
					vals[k][i] = reader.readObject();
				}
				for (int s = 0; s < skipCount; ++s) {
					reader.skipObject();
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	/**
	 * ���� �����ֶ�����ʱ��
	 * ������ҵ��򷵻ؼ�¼��ţ��Ҳ����򷵻ظ�����λ��
	 * @param keyValue
	 * @return
	 */
	public long findNext(Object keyValue) {
		if (isEnd) {
			return -prevRecordCount - 1;
		}
		
		if (curIndex != -1) {
			int cmp = Variant.compare(keyValue, maxValues[0]);
			if (cmp > 0) {
				nextBlock();
				return findNext(keyValue);
			} else if (cmp == 0) {
				curIndex = curRecordCount;
				return prevRecordCount + curIndex;
			}  else {
				Object []values = blockKeyValues[0];
				for (int i = curIndex, end = curRecordCount; i < end; ++i) {
					cmp = Variant.compare(keyValue, values[i]);
					if (cmp == 0) {
						curIndex = i;
						return prevRecordCount + i;
					} else if (cmp < 0) {
						curIndex = i;
						return -prevRecordCount - i;
					}
				}
				
				curIndex = curRecordCount;
				return -prevRecordCount - curIndex;
			}
		}
		
		while (true) {
			int cmp = Variant.compare(keyValue, maxValues[0]);
			if (cmp > 0) {
				if (!nextBlock()) {
					return -prevRecordCount - 1;
				}
			} else if (cmp == 0) {
				this.curIndex = curRecordCount;
				return prevRecordCount + curRecordCount;
			} else {
				loadKeyValues();
				this.curIndex = 1;
				return findNext(keyValue);
			}
		}
	}
	
	/**
	 * ���� �����ֶ�����ʱ��
	 * ������ҵ��򷵻ؼ�¼��ţ��Ҳ����򷵻ظ�����λ��
	 * @param keyValues
	 * @return
	 */
	public long findNext(Object []keyValues) {
		if (isEnd) {
			return -prevRecordCount - 1;
		}
		
		if (curIndex != -1) {
			int cmp = Variant.compareArrays(keyValues, maxValues);
			if (cmp > 0) {
				nextBlock();
				return findNext(keyValues);
			} else if (cmp == 0) {
				curIndex = curRecordCount;
				return prevRecordCount + curIndex;
			}  else {
				Object [][]blockKeyValues = this.blockKeyValues;
				int keyCount = keyValues.length;
				
				Next:
				for (int i = curIndex, end = curRecordCount; i < end; ++i) {
					for (int k = 0; k < keyCount; ++k) {
						cmp = Variant.compare(keyValues[k], blockKeyValues[k][i]);
						if (cmp > 0) {
							continue Next;
						} else if (cmp < 0) {
							curIndex = i;
							return -prevRecordCount - i;
						}
					}
					
					// �����
					curIndex = i;
					return prevRecordCount + i;
				}
				
				curIndex = curRecordCount;
				return -prevRecordCount - curIndex;
			}
		}
		
		while (true) {
			int cmp = Variant.compareArrays(keyValues, maxValues);
			if (cmp > 0) {
				if (!nextBlock()) {
					return -prevRecordCount - 1;
				}
			} else if (cmp == 0) {
				this.curIndex = curRecordCount;
				return prevRecordCount + curRecordCount;
			} else {
				this.curIndex = 1;
				return findNext(keyValues);
			}
		}
	}
	
	boolean isEnd() {
		return isEnd;
	}
}