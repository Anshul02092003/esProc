package com.scudata.dw;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.resources.EngineMessage;

/**
 * ��¼ֵ������
 * @author runqian
 *
 */
class RecordValSearcher {
	private Cursor cs;
	private String []fields;
	private Sequence pkeyData;//ȡ��������
	private long recNUM;//��ǰα��
	private GroupTableRecord curRecord;//��ǰ��¼
	private Record curModifyRecord;//��ǰ��¼��������
	private int cur;//�������
	private int len;//��ǰ�����
	private int []index;//�ֶ�ȡ��ʱ��Ӧ��λ��

	private int indexSize;
	private ArrayList<ModifyRecord> modifyRecords;

	public RecordValSearcher() {
	}
	
	public void setData(Sequence pkeyData) {
		this.pkeyData = pkeyData;
		if (pkeyData.hasRecord()) {
			curRecord = (GroupTableRecord) pkeyData.getMem(1);
			len = pkeyData.length();
			cur = 1;
			recNUM = curRecord.getRecordSeq();
		}
		this.fields = curRecord.getFieldNames();
	}

	public RecordValSearcher(ColumnTableMetaData table, String []fields) {
		cs = (Cursor) table.cursor(fields);
		pkeyData = cs.fetch(ICursor.FETCHCOUNT);
		if (pkeyData.hasRecord()) {
			curRecord = (GroupTableRecord) pkeyData.getMem(1);
			len = pkeyData.length();
			cur = 1;
			recNUM = curRecord.getRecordSeq();
		}
		this.fields = curRecord.getFieldNames();
	}
	
	public void setIndex(int[] index) {
		this.index = index;
		if (index == null) return;
		indexSize = index.length;
	}
	
	public long getRecNum() {
		return recNUM;
	}
	
	public void setModifyRecords(ArrayList<ModifyRecord> modifyRecords) {
		this.modifyRecords = modifyRecords;
	}
	
	public void next() {
		cur++;
		if (cur > len) {
			return;
		}
		GroupTableRecord rec = (GroupTableRecord) pkeyData.getMem(cur);
		curRecord = rec;
		this.recNUM = rec.getRecordSeq();
	}
	
	public int getRecordCount() {
		return pkeyData.length();
	}

	/**
	 * ����¼��ȡ��keyֵ
	 * ��filterʱ�����
	 * @param recNum ��¼��
	 * @param r ���
	 * @return true�ɹ���falseû�ҵ�
	 */
	public boolean getKeyVals(long recNum, Record r) {
		GroupTableRecord rec;
		if (recNum < this.recNUM) {
			return false;
		} else if (recNum == this.recNUM) {
			rec = curRecord;
		} else {
			while (true) {
				cur++;
				if (cur > len) {
					return false;
				}
				rec = (GroupTableRecord) pkeyData.getMem(cur);
				if (recNum < rec.getRecordSeq()) {
					curRecord = rec;
					this.recNUM = rec.getRecordSeq();
					return false;
				}
				
				if (recNum == rec.getRecordSeq()) {
					curRecord = rec;
					this.recNUM = recNum;
					break;
				}
			}
		}
		
		int []index = this.index;
		int colCount = indexSize;
		for (int i = 0; i < colCount; ++i) {
			int id = index[i];
			if (id >= 0) {
				r.set(id, rec.getFieldValue(i));
			}
		}
		return true;
	}
	
	/**
	 * ����¼��ȡ��keyֵ
	 * ��filterʱ���������Ϊ�ӱ�ļ�¼�϶������������ҵ�
	 * @param recNum ��¼��
	 * @param r ���
	 */
	public void getKeyValues(long recNum, Record r) {
		GroupTableRecord rec;		
		if (recNum == this.recNUM) {
			rec = curRecord;
		} else {
			while (true) {
				cur++;
				if (cur > len) {
					//�쳣
					MessageManager mm = EngineMessage.get();
					throw new RQException(r.toString(null) + mm.getMessage("grouptable.invalidData"));
				}
				rec = (GroupTableRecord) pkeyData.getMem(cur);
				if (recNum == rec.getRecordSeq()) {
					curRecord = rec;
					this.recNUM = recNum;
					break;
				}
			}
		}
		
		int []index = this.index;
		int colCount = indexSize;
		for (int i = 0; i < colCount; ++i) {
			int id = index[i];
			if (id >= 0) {
				r.set(id, rec.getFieldValue(i));
			}
		}
	}

	/**
	 * ȡ������¼ֵ
	 * @param recNum ��¼�ţ�Ϊ��ֵʱ��ʾ�����ڲ��������
	 * @param r ���
	 * @param tableId Ŀǰֻ������0
	 */
	public void getMKeyValues(long recNum, Record r,int tableId) {
		Record record = null;
		
		if (modifyRecords == null) {
			return;
		}
		if (tableId == 0) {
			recNum = -recNum;
			record = modifyRecords.get((int) recNum - 1).getRecord();
			curModifyRecord = record;
		} else {
			ArrayList<ModifyRecord> modifyRecords = this.modifyRecords;
			for (ModifyRecord mr : modifyRecords) {
				if (mr.getParentRecordSeq() == recNum) {
					record = mr.getRecord();
					curModifyRecord = record;
					break;
				}
			}
		}
		
		String []fields = this.fields;
		int []index = this.index;
		int colCount = indexSize;
		for (int i = 0; i < colCount; ++i) {
			int id = index[i];
			if (id >= 0) {
				r.set(id, record.getFieldValue(fields[i]));
			}
		}
		return;
		
	}
	
	/**
	 * ��õ�ǰ��¼ֵ
	 * @param r ���
	 */
	public void getRecordValue(Record r) {
		GroupTableRecord rec = curRecord;

		int []index = this.index;
		int colCount = indexSize;
		for (int i = 0; i < colCount; ++i) {
			int id = index[i];
			if (id >= 0) {
				r.set(id, rec.getFieldValue(i));
			}
		}
	}
	
	/**
	 * ȡ��ǰ��¼���ֶ�ֵ
	 * @param index �ֶ�λ��
	 * @return
	 */
	public Object getRecordValue(int index) {
		return curRecord.getNormalFieldValue(index);
	}
	
	/**
	 * ȡ��ǰ����¼���ֶ�ֵ
	 * @param index �ֶ�λ��
	 * @return
	 */
	public Object getModifyRecordValue(int index) {
		return curModifyRecord.getNormalFieldValue(index);
	}
}