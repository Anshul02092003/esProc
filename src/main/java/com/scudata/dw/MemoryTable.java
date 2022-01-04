package com.scudata.dw;

import java.util.Arrays;

import com.scudata.common.IntArrayList;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.CompressIndexTable;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dw.compress.ColumnList;
import com.scudata.resources.EngineMessage;

/**
 * �ڱ���
 * @author runqian
 *
 */
public class MemoryTable extends Table {
	private static final long serialVersionUID = 0x03310004;
	
	// ����зֲ����ʽ�������ڽڵ�����Ƿֲ��ģ����û����Ҫ������������Ҫ�ȽϽڵ�������ݵķ�Χ�Ƿ��غ�
	private String distribute; // �ֲ����ʽ
	private int part = -1; // �����ķֱ�
	
	private int []segmentFields; // ���ɶ�·�α�ʱ���ڷֶε��ֶ�
	
	/**
	 * ���л�ʱʹ��
	 */
	public MemoryTable() {}

	/**
	 * ����
	 * @param fields String[] ���е���ͨ�ֶ�
	 * @return Table
	 */
	public MemoryTable(String []fields) {
		super(fields);
	}

	public MemoryTable(DataStruct ds) {
		super(ds);
	}

	public MemoryTable(String []fields, int initialCapacity) {
		super(fields, initialCapacity);
	}

	public MemoryTable(DataStruct ds, int initialCapacity) {
		super(ds, initialCapacity);
	}
	
	public MemoryTable(Table table) {
		this.ds = table.dataStruct();
		this.mems = table.getMems();
	}
	
	/**
	 * ����ѹ���ڴ��
	 * cursor��ļ�¼�����ȥ
	 * @param cursor
	 */
	public MemoryTable(ICursor cursor) {
		this.mems = new ColumnList(cursor);
		this.ds = ((ColumnList)mems).dataStruct();
		if (ds == null) return;
		int index[] = ds.getPKIndex();
		if (index == null) return;
		CompressIndexTable indexTable = new CompressIndexTable((ColumnList) mems, index);
		this.setIndexTable(indexTable);
	}
	
	/**
	 * ����ѹ���ڴ��
	 * cursor��ȡn����¼�����ȥ
	 * @param cursor
	 * @param n
	 */
	public MemoryTable(ICursor cursor, int n) {
		this.mems = new ColumnList(cursor, n);
		this.ds = ((ColumnList)mems).dataStruct();
		if (ds == null) return;
		int index[] = ds.getPKIndex();
		if (index == null) return;
		CompressIndexTable indexTable = new CompressIndexTable((ColumnList) mems, index);
		this.setIndexTable(indexTable);
	}

	/**
	 * �Ƿ���ѹ����ѹ����������ʱ�����������
	 * @return
	 */
	public boolean isCompressTable() {
		return mems instanceof ColumnList;
	}
	
	/**
	 * ׷��
	 * @param table
	 */
	public void append(Sequence table) {
		ListBase1 addMems = table.getMems();

		// ���ļ�¼���������к����
		DataStruct ds = this.ds;
		for (int i = 1, addCount = addMems.size(); i <= addCount; ++i) {
			Record r = (Record)addMems.get(i);
			r.setDataStruct(ds);
		}

		mems.addAll(addMems);
	}
	
	/**
	 * ����
	 * @param data
	 * @param opt
	 * @return
	 */
	public Sequence update(Sequence data, String opt) {
		boolean isInsert = true;
		boolean isUpdate = true;
		Sequence result = null;
		if (opt != null) {
			if (opt.indexOf('i') != -1) isUpdate = false;
			if (opt.indexOf('u') != -1) {
				if (!isUpdate) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(opt + mm.getMessage("engine.optConflict"));
				}
				
				isInsert = false;
			}
			
			if (opt.indexOf('n') != -1) result = new Sequence();
		}
		
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		if (!ds.isCompatible(this.ds)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		int oldLen = length();
		if (oldLen == 0) {
			if (isInsert) {
				append(data);
				if (result != null) {
					result.addAll(data);
				}
			}
			
			if (result == null) {
				return this;
			} else {
				return result;
			}
		}
		
		ds = this.ds;
		int []keyIndex = ds.getPKIndex();
		if (keyIndex == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.lessKey"));
		}
		
		int keyCount = keyIndex.length;
		int len = data.length();
		int []seqs = new int[len + 1];
		
		if (keyCount == 1) {
			int k = keyIndex[0];
			for (int i = 1; i <= len; ++i) {
				Record r = (Record)data.getMem(i);
				seqs[i] = pfindByKey(r.getNormalFieldValue(k), true);
			}
		} else {
			Object []keyValues = new Object[keyCount];
			for (int i = 1; i <= len; ++i) {
				Record r = (Record)data.getMem(i);
				for (int k = 0; k < keyCount; ++k) {
					keyValues[k] = r.getNormalFieldValue(keyIndex[k]);
				}
				
				seqs[i] = pfindByFields(keyValues, keyIndex);
			}
		}
		
		// ��Ҫ��������ĵ���append׷��
		ListBase1 mems = this.mems;
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)data.getMem(i);
			if (seqs[i] > 0) {
				if (isUpdate) {
					Record sr = (Record)mems.get(seqs[i]);
					sr.set(r);
					if (result != null) {
						result.add(r);
					}
				}
			} else if (isInsert) {
				r = new Record(ds, r.getFieldValues());
				mems.add(r);
				if (result != null) {
					result.add(r);
				}
			}
		}
		
		if (mems.size() > oldLen) {
			sortFields(keyIndex);
		}
		
		rebuildIndexTable();
		
		if (result == null) {
			return this;
		} else {
			return result;
		}
	}
	
	/**
	 * ɾ��
	 */
	public Sequence delete(Sequence data, String opt) {
		if (data == null || data.length() == 0) {
			if (opt == null || opt.indexOf('n') == -1) {
				return this;
			} else {
				return new Sequence(0);
			}
		}

		String []pks = getPrimary();
		if (pks == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.lessKey"));
		}
		
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		int keyCount = pks.length;
		int []keyIndex = new int[keyCount];
		for (int k = 0; k < keyCount; ++k) {
			keyIndex[k] = ds.getFieldIndex(pks[k]);
			if (keyIndex[k] < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pks[k] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		ListBase1 mems = this.mems;
		int delCount = 0;

		ListBase1 dataMems = data.getMems();
		int count = dataMems.size();
		int []index = new int[count];
		Sequence delete = null;
		if (opt != null && opt.indexOf('n') != -1) {
			delete = new Sequence(count);
		}

		if (keyCount == 1) {
			// ����Ҫɾ���ļ�¼�������е�λ��
			int ki = keyIndex[0];
			for (int i = 1; i <= count; ++i) {
				Record r = (Record)dataMems.get(i);
				int seq = pfindByKey(r.getNormalFieldValue(ki), true);
				if (seq > 0) {
					index[delCount] = seq;
					delCount++;
					if (delete != null) {
						delete.add(r);
					}
				}
			}
		} else {
			// ����Ҫɾ���ļ�¼�������е�λ��
			int []srcPKIndex = this.ds.getPKIndex();
			Object []keyValues = new Object[keyCount];
			for (int i = 1; i <= count; ++i) {
				Record r = (Record)dataMems.get(i);
				for (int k = 0; k < keyCount; ++k) {
					keyValues[k] = r.getNormalFieldValue(keyIndex[k]);
				}
				
				int seq = pfindByFields(keyValues, srcPKIndex);
				if (seq > 0) {
					index[delCount] = seq;
					delCount++;
					if (delete != null) {
						delete.add(r);
					}
				}
			}
		}

		if (delCount == 0) {
			if (opt == null || opt.indexOf('n') == -1) {
				return this;
			} else {
				return new Sequence(0);
			}
		}

		if (delCount < count) {
			int []tmp = new int[delCount];
			System.arraycopy(index, 0, tmp, 0, delCount);
			index = tmp;
		}

		// ��������������
		Arrays.sort(index);
		mems.remove(index);

		rebuildIndexTable();

		if (delete == null) {
			return this;
		} else {
			return delete;
		}
	}

	/**
	 * ����
	 */
	public Object findByKey(Object key, boolean isSorted) {
		IndexTable indexTable = getIndexTable();
		if (indexTable == null) {
			int index = pfindByKey(key, true);
			return index > 0 ? mems.get(index) : null;
		} else {
			if (key instanceof Sequence) {
				// key�������ӱ�ļ�¼������������B
				if (length() == 0) {
					return null;
				}
				
				Object startVal = mems.get(1);
				if (startVal instanceof Record) {
					startVal = ((Record)startVal).getPKValue();
				}
				
				Sequence seq = (Sequence)key;
				int klen = seq.length();
				if (klen == 0) {
					return 0;
				}
				
				if (startVal instanceof Sequence) {
					int klen2 = ((Sequence)startVal).length();
					if (klen2 == 1) {
						return indexTable.find(seq.getMem(1));
					} else if (klen > klen2) {
						Object []vals = new Object[klen2];
						for (int i = 1; i <= klen2; ++i) {
							vals[i - 1] = seq.getMem(i);
						}

						return indexTable.find(vals);
					} else {
						return indexTable.find(seq.toArray());
					}
				} else {
					return indexTable.find(seq.getMem(1));
				}
			} else {
				return indexTable.find(key);
			}
		}
	}
	
	/**
	 * ���÷ֱ���ʽ
	 * @param exp �ֱ���ʽ
	 */
	public void setDistribute(String exp) {
		distribute = exp;
	}
	
	/**
	 * ȡ�ֲ����ʽ
	 * @return
	 */
	public String getDistribute() {
		return distribute;
	}
	
	/**
	 * ���÷ֱ��
	 * @param part �ֱ��
	 */
	public void setPart(int part) {
		this.part = part;
	}
	
	/**
	 * ȡ�ֱ��
	 * @return
	 */
	public int getPart() {
		return part;
	}
	
	/**
	 * ���÷ֶ��ֶ�
	 * @param fields
	 */
	public void setSegmentFields(String []fields) {
		IntArrayList list = new IntArrayList(fields.length);
		for (String field : fields) {
			int f = ds.getFieldIndex(field);
			if (f != -1) {
				list.addInt(f);
			}
		}
		
		if (list.size() > 0) {
			segmentFields = list.toIntArray();
		}
	}
	
	/**
	 * ���õ�һ���ֶ�Ϊ�ֶ��ֶ�
	 */
	public void setSegmentField1() {
		segmentFields = new int[] {0};
	}
	
	/**
	 * ȡ�ֶ��ֶ�����
	 * @return
	 */
	public int[] getSegmentFields() {
		return segmentFields;
	}
	
	/**
	 * ������һ�εĿ�ʼλ��
	 * @param i �к�
	 * @param segmentFields �ֶ��ֶ�
	 * @return
	 */
	private int getSegmentEnd(int i, int []segmentFields) {
		Record r = (Record)getMem(i);
		int len = length();
		for (++i; i <= len; ++i) {
			Record record = (Record)getMem(i);
			if (!r.isEquals(record, segmentFields)) {
				return i;
			}
		}
		
		return len + 1;
	}
	
	public ICursor cursor(int segSeq, int segCount, Context ctx) {
		if (segCount <= 1) {
			return new MemoryCursor(this);
		}

		// ������·�α�
		int len = length();
		int blockSize = len / segCount;
		int []segmentFields = this.segmentFields;
		
		if (segSeq < 1) {
			if (blockSize < 0) {
				return new MemoryCursor(this);
			}
			
			ICursor []cursors = new ICursor[segCount];
			int start = 1; // ����
			int end; // ������
			
			if (segmentFields == null) {
				for (int i = 1; i <= segCount; ++i) {
					if (i == segCount) {
						end = len + 1;
					} else {
						end = start + blockSize;
					}
					
					cursors[i - 1] = new MemoryCursor(this, start, end);
					start = end;
				}
			} else {
				// �зֶ��ֶ�ʱ���ֶ��ֶ�ֵ��ͬ�Ĳ��ᱻ�������α�
				for (int i = 1; i <= segCount; ++i) {
					if (i == segCount) {
						end = len + 1;
					} else {
						end = blockSize * i;
						if (start <= end) {
							end = getSegmentEnd(end, segmentFields);
						}
					}
					
					cursors[i - 1] = new MemoryCursor(this, start, end);
					start = end;
				}
			}
			
			return new MultipathCursors(cursors, ctx);
		} else {
			// �����ֶ��α�
			int start = 1; // ����
			int end; // ������
			if (segSeq == segCount) {
				start = blockSize * (segSeq - 1) + 1;
				end = len + 1;
			} else {
				start = blockSize * (segSeq - 1) + 1;
				end = start + blockSize;
			}
			
			if (segmentFields != null) {
				if (start > 1) {
					start = getSegmentEnd(start - 1, segmentFields);
				}
				
				if (start < end) {
					end = getSegmentEnd(end - 1, segmentFields);
				}
			}
			
			return new MemoryCursor(this, start, end);
		}
	}
}