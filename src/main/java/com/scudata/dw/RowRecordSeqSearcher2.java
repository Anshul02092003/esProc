package com.scudata.dw;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.ObjectReader;
import com.scudata.util.Variant;

/**
 * ��¼������ �������ã�
 * @author runqian
 *
 */
class RowRecordSeqSearcher2 {
	private RowTableMetaData table;
	private RowTableMetaData baseTable;
	private long prevRecordCount = 0;//��ǰ�Ѿ�ȡ���ļ�¼��
	private int curBlock = -1;
	private int totalBlockCount;

	private BlockLinkReader rowReader;
	private ObjectReader segmentReader;
	private BlockLinkReader baseRowReader;
	private ObjectReader baseSegmentReader;
	
	private long position; // ��ǰ���λ��
	private long basePosition; // ��ǰ���λ��
	private Object []minValues; // ��ǰ���ÿ�����е���Сֵ
	private Object []maxValues; // ��ǰ���ÿ�����е����ֵ
	
	private int curRecordCount = 0; // ��ǰ��ļ�¼��
	private int curIndex = -1; // ��������������-1����黹û����
	private Object [][]blockKeyValues;
	private boolean isEnd = false;

	private int baseKeyCount;
	private long []guideVals;
	private int []baseKeyIndex;
	private int []keyIndex;
	
	public RowRecordSeqSearcher2(RowTableMetaData table) {
		this.table = table;
		baseTable = (RowTableMetaData) table.groupTable.baseTable;
		init();
	}
	
	private void init() {
		totalBlockCount = table.getDataBlockCount();
		baseKeyCount = table.sortedColStartIndex;
		if (totalBlockCount == 0) {
			isEnd = true;
			return;
		}
		
		String[] columns = table.getAllSortedColNames();
		int keyCount = columns.length;
		rowReader = table.getRowReader(true);
		segmentReader = table.getSegmentObjectReader();
		minValues = new Object[keyCount];
		maxValues = new Object[keyCount];
		blockKeyValues = new Object[keyCount][];

		baseRowReader = baseTable.getRowReader(true);
		baseSegmentReader = baseTable.getSegmentObjectReader();
		
		keyIndex = table.getSortedColIndex();
		baseKeyIndex = baseTable.getSortedColIndex();
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
			baseSegmentReader.readInt32();
			position = segmentReader.readLong40();
			basePosition = baseSegmentReader.readLong40();
			
			for (int k = 0; k < baseKeyCount; ++k) {
				minValues[k] = baseSegmentReader.readObject();
				maxValues[k] = baseSegmentReader.readObject();
			}
			
			int keyCount = minValues.length;			
			for (int k = baseKeyCount; k < keyCount; ++k) {
				minValues[k] = segmentReader.readObject();
				maxValues[k] = segmentReader.readObject();
			}

			return true;
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	private void loadKeyValues() {
		try {
			int keyCount = blockKeyValues.length;
			int baseKeyCount = this.baseKeyCount;
			int colCount = table.getAllColNames().length;
			int baseColCount = baseTable.getColNames().length;
			int count = curRecordCount + 1;

			BufferReader reader = rowReader.readBlockBuffer(position);
			BufferReader baseReader = baseRowReader.readBlockBuffer(basePosition);
			
			Object [][]vals = new Object[keyCount][count];
			for (int k = 0; k < keyCount; ++k) {
				blockKeyValues[k] = vals[k];
			}

			Object []temp = new Object[table.getTotalColNames().length];
			Object []baseVals = new Object[baseKeyCount];
			long baseSeq = (Long) baseReader.readObject();
			for (int k = 0; k < baseColCount; ++k) {
				temp[k] = baseReader.readObject();
			}
			for (int k = 0; k < baseKeyCount; ++k) {
				baseVals[k] = temp[baseKeyIndex[k]];
			}
			
			for (int i = 1; i < count; ++i) {
				reader.skipObject();//����α��
				long seq = (Long) reader.readObject();//ȡ��α��
				
				for (int k = baseKeyCount; k < colCount; ++k) {
					temp[k] = reader.readObject();
				}
				for (int k = baseKeyCount; k < keyCount; ++k) {
					vals[k][i] = temp[keyIndex[k]];
				}
				
				//�������Ҷ�Ӧ��
				while (seq != baseSeq) {
					baseSeq = (Long) baseReader.readObject();
					//�ҵ��˶�����
					for (int k = 0; k < baseColCount; ++k) {
						temp[k] = baseReader.readObject();
					}
					for (int k = 0; k < baseKeyCount; ++k) {
						baseVals[k] = temp[baseKeyIndex[k]];
					}
				}
				for (int k = 0; k < baseKeyCount; ++k) {
					vals[k][i] = baseVals[k];
				}
			}
			
		} catch (IOException e) {
			throw new RQException(e);
		}
	}

	/**
	 * ������ҵ��򷵻ؼ�¼��ţ��Ҳ����򷵻ظ�����λ��
	 * �ӱ�insertʱ�������¼���ڣ���Ӧ�÷��ض�Ӧ����������α��
	 * �ӱ�updateʱ��������update����
	 * @param keyValue 
	 * @param block ���Ǹ����������block[0]���ҵ��Ŀ��
	 * @return
	 */
	public long findNext(Object keyValue, int block[]) {
		block[0] = -1;
		if (isEnd) {
			return -prevRecordCount - 1;
		}
		
		if (curIndex != -1) {
			int cmp = Variant.compare(keyValue, maxValues[0]);
			if (cmp > 0) {
				nextBlock();
				return findNext(keyValue, block);
			}  else {
				Object []values = blockKeyValues[0];
				for (int i = curIndex, end = curRecordCount; i <= end; ++i) {
					cmp = Variant.compare(keyValue, values[i]);
					if (cmp == 0) {
						curIndex = i;
						return prevRecordCount + i;
					} else if (cmp < 0) {
						curIndex = i;
						return -prevRecordCount - i;
					}
				}
				
				//ÿһ�εĵײ�λ��
				curIndex = curRecordCount;
				block[0] = curBlock + 1;
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
				return findNext(keyValue, block);
			}
		}
	}
	
	/**
	 * ���ֶ������Ĳ���
	 * ������ҵ��򷵻ؼ�¼��ţ��Ҳ����򷵻ظ�����λ��
	 * �ӱ�insertʱ�������¼���ڣ���Ӧ�÷��ض�Ӧ����������α��
	 * �ӱ�updateʱ��������update����
	 * @param keyValue 
	 * @param block ���Ǹ����������block[0]���ҵ��Ŀ��
	 * @return
	 */
	public long findNext(Object []keyValues, int block[]) {
		block[0] = -1;
		if (isEnd) {
			return -prevRecordCount - 1;
		}

		if (0 == curRecordCount) {
			//����Ǹ��տ�
			int cmp = Variant.compareArrays(keyValues, maxValues, baseKeyCount);
			if (cmp <= 0) {
				block[0] = curBlock + 1;
				return -prevRecordCount;
			}
		}

		if (curIndex != -1) {
			int cmp;
			if (curRecordCount == 0) {
				cmp = Variant.compareArrays(keyValues, maxValues, baseKeyCount);
			} else {
				cmp = Variant.compareArrays(keyValues, maxValues);
			}
			if (cmp > 0) {
				nextBlock();
				return findNext(keyValues, block);
			} else if (cmp == 0) {
				if (curRecordCount == 0) {
					block[0] = curBlock + 1;
					return -prevRecordCount;
				}
				curIndex = curRecordCount;
				return prevRecordCount + curIndex;
			}  else {
				Object [][]blockKeyValues = this.blockKeyValues;
				int keyCount = keyValues.length;
				
				Next:
				for (int i = curIndex, end = curRecordCount; i <= end; ++i) {
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

				//ÿһ�εĵײ�λ��
				curIndex = curRecordCount;
				block[0] = curBlock + 1;
				return -prevRecordCount - curIndex;
			}
		}
		
		while (true) {
			int cmp;
			if (curRecordCount == 0) {
				cmp = Variant.compareArrays(keyValues, maxValues, baseKeyCount);
			} else {
				cmp = Variant.compareArrays(keyValues, maxValues);
			}
			if (cmp > 0) {
				if (!nextBlock()) {
					return -prevRecordCount - 1;
				}
			} else if (cmp == 0) {
				if (curRecordCount == 0) {
					block[0] = curBlock + 1;
					return -prevRecordCount;
				}
				this.curIndex = curRecordCount;
				return prevRecordCount + curRecordCount;
			} else {
				loadKeyValues();
				this.curIndex = 1;
				return findNext(keyValues, block);
			}
		}
	}
	
	/**
	 * ���ң������ز���λ��
	 * @param keyValues
	 * @param keyLen
	 * @return
	 */
	public long findNext(Object []keyValues, int keyLen) {
		if (isEnd) {
			return -prevRecordCount - 1;
		}

		if (curIndex != -1) {
			int cmp = Variant.compareArrays(keyValues, maxValues, keyLen);
			if (cmp > 0) {
				nextBlock();
				return findNext(keyValues, keyLen);
			} else {
				Object [][]blockKeyValues = this.blockKeyValues;

				Next:
				for (int i = curIndex, end = curRecordCount; i <= end; ++i) {
					for (int k = 0; k < keyLen; ++k) {
						cmp = Variant.compare(keyValues[k], blockKeyValues[k][i]);
						if (cmp > 0) {
							continue Next;
						} else if (cmp < 0) {
							curIndex = i + 1;
							return -prevRecordCount - i;
						}
					}
					
					// �����
					curIndex = i + 1;
					return prevRecordCount + i;
				}
				
				nextBlock();
				return findNext(keyValues, keyLen);
			}
		}
		
		while (true) {
			int cmp = Variant.compareArrays(keyValues, maxValues, keyLen);
			if (cmp > 0) {
				if (!nextBlock()) {
					return -prevRecordCount - 1;
				}
			} else {
				loadKeyValues();
				this.curIndex = 1;
				return findNext(keyValues, keyLen);
			}
		}
	}
	
	//���ص�ǰ��������������α��
	long getRecNum() {
		if (isEnd || guideVals == null) {
			return 0;
		}
		return this.guideVals[curIndex];
	}
	
	public boolean isEnd() {
		return isEnd;
	}
}