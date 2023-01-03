package com.scudata.dm;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dw.ColPhyTable;
import com.scudata.dw.Cursor;
import com.scudata.dw.MemoryTable;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

//���ڶ���߳�ͬ����������ļ����α�ȡ��
public class SyncReader {

	private Object srcObj;
	private ArrayList<Integer> countList = new ArrayList<Integer>();//ÿ�ε�����
	private Sequence values = new Sequence();//ÿ�ε�����ֵ
	
	private int fetched[];
	private int parallCount = 1;
	private int SYNC_THREAD_NUM = 8;//ͬʱȡ����ά�������ʱ�̱�֤����ô�������ã�
	private Sequence datas[];
	private String[] fields;//��Ҫ��ά���ֶ�
	private Thread []threads;
	
	public ArrayList<Integer> getCountList() {
		return countList;
	}

	public Sequence getValues() {
		return values;
	}

	private void init() {
		int size = countList.size();
		if (srcObj instanceof ColPhyTable) {
			size = countList.size() / 2;
		}
		datas = new Sequence[size];
		fetched = new int[size];
		threads = new Thread[size];
		for (int i = 0; i < size; ++i) {
			threads[i] = newLoadDataThread(srcObj, i, countList, datas, fields);
		}

		if (SYNC_THREAD_NUM > size) {
			SYNC_THREAD_NUM = size;
		}
	}
	
	public SyncReader(ColPhyTable table, String[] fields, int n) {
		try {
			String[] keys = table.getAllSortedColNames();
			if (keys == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("ds.lessKey"));
			}
			table.getSegmentInfo2(keys, countList, values, n);
		} catch (IOException e) {
			throw new RQException(e);
		}
		
		srcObj = table;
		this.fields = fields;
		init();
	}
	
	public SyncReader(FileObject file, Expression []exps, int n) {
		//ת��ΪString[]
		int len = exps.length;
		String []keys = new String[len];
		for (int j = 0; j < len; j++) {
			keys[j] = exps[j].toString();
		}
		BFileReader reader = new BFileReader(file, keys, null);

		try {
			reader.getSegmentInfo(countList, values, n);
		} catch (IOException e) {
			throw new RQException(e);
		}
		srcObj = file;//
		init();
	}
	
	public SyncReader(Cursor cursor, Expression []exps, int n) {
		//ת��ΪString[]
		int len = exps.length;
		String []keys = new String[len];
		for (int j = 0; j < len; j++) {
			keys[j] = exps[j].toString();
		}
		
		ColPhyTable tempTable;
		try {
			tempTable = (ColPhyTable) ((Cursor)cursor).getTableMetaData();
			tempTable.getSegmentInfo2(keys, countList, values, n);
		} catch (IOException e) {
			throw new RQException(e);
		}
		srcObj = tempTable;
		init();
	}
	
	public void loadData(int index) {
		if (srcObj instanceof ColPhyTable) {
			int len = fields.length;
			Expression[] fieldExps = new Expression[len];
			for (int i = 0; i < len; i++) {
				fieldExps[i] = new Expression(fields[i]);
			}
			Cursor cursor = (Cursor) ((ColPhyTable) srcObj).cursor(fieldExps, null, null, null, null, null, null);
			cursor.setSegment(false);
			cursor.reset();
			cursor.setSegment(countList.get(index * 2), countList.get(index * 2 + 1));
			datas[index] = new MemoryTable(cursor);
		} else {
			ICursor srcCursor = new BFileCursor((FileObject) srcObj, null, null, null);
			//TODO ���÷ֶ�pos
			datas[index] = srcCursor.fetch(countList.get(index));
		}
	}
	
	public static void loadData(Object srcObj, int index, ArrayList<Integer> countList, Sequence []datas, String[] fields) {
		if (index >= datas.length) {
			return;
		}
		if (srcObj instanceof ColPhyTable) {
			Cursor cursor = (Cursor) ((ColPhyTable) srcObj).cursor(fields);
			cursor.setSegment(false);
			cursor.reset();
			cursor.setSegment(countList.get(index * 2), countList.get(index * 2 + 1));
			datas[index] = new MemoryTable(cursor);
		} else {
			ICursor srcCursor = new BFileCursor((FileObject) srcObj, null, null, null);
			//TODO ���÷ֶ�pos
			datas[index] = new MemoryTable(srcCursor, countList.get(index));
		}
	}
	
	public synchronized Sequence getData(int index) {
		fetched[index] ++;
		if (datas[index] == null) {
			//loadData(index);
			if (parallCount == 1 || srcObj instanceof FileObject) {
				loadData(index);
			} else {

				try {
					//run�������˵��join���ٶȱ�����ȡ��Ҫ��
					//һ���������
					int num = SYNC_THREAD_NUM;
					Thread[] threads = this.threads;
					for (int i = 0; i < num; i++) {
						if (index + i >= threads.length) {
							break;
						}
						if (threads[index + i].getState() == Thread.State.NEW) {
							threads[index + i].start(); //����
						}
					}
					for (int i = 0; i < num; i++) {
						if (index + i >= threads.length) {
							break;
						}
						threads[index + i].join();
					}
				} catch (InterruptedException e) {
					throw new RQException(e);
				}
				
			}
		}
		
		if (fetched[index] == parallCount) {
			//������е��߳�(joinx�߳�)��ȡ����
			Sequence data = datas[index];
			datas[index] = null;
			
			if (!(srcObj instanceof FileObject)) {
				//������һ��δ�������߳�
				int next = index + SYNC_THREAD_NUM;
				if (next < threads.length) {
					Thread t = threads[next];
					if (t.getState() == Thread.State.NEW) {
						t.start();
					}
				}
			}
			return data;
		}
		return datas[index];
	}
	
	public void setParallCount(int parallCount) {
		if (SYNC_THREAD_NUM < parallCount) {
			parallCount = SYNC_THREAD_NUM;
		}
		this.parallCount = parallCount;
		
		if (parallCount == 1) return;
		for (int i = 0; i < SYNC_THREAD_NUM; ++i) {
			threads[i].start(); // �����̷ֶ߳�ȡ��
		}
	}
	
	private static Thread newLoadDataThread(final Object srcObj, final int index, 
			final ArrayList<Integer> countList, final Sequence []datas, final String[] fields) {
		return new Thread() {
			public void run() {
				loadData(srcObj, index, countList, datas, fields);
			}
		};
	}
}
