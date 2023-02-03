package com.scudata.dm;

import java.util.LinkedList;
import java.util.Queue;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.thread.Job;
import com.scudata.thread.ThreadPool;
import com.scudata.util.HashUtil;

/**
 * ����ִ��groups������ͬ�����α��ȡ���ݲ�����hash
 * @author LW
 *
 */
public class GroupsSyncReader {
	private CursorReadJob readers[];
	private int tcount;
	private IntArray curTimes;//��¼��ǰ�鱻ȡ�˶��ٴ�
	private ObjectArray datas;//Ҫȡ������
	private ThreadPool threadPool;
	private boolean close;
	
	//ȡ���̴߳��α���ȡ�������ݣ��Ȼ��浽�������
	private Queue<Object[]> readyDatas = new LinkedList<Object[]>();
	
	public GroupsSyncReader(ICursor[] cursors, Expression[] exps, HashUtil hashUtil, Context ctx) {
		datas = new ObjectArray(1024);
		curTimes = new IntArray(1024);
		
		int tcount = cursors.length;
		int fetchSize = ICursor.FETCHCOUNT * 10;
		int maxCacheSize = tcount * 2;
		
		CursorReadJob readers[] = new CursorReadJob[tcount];
		ThreadPool threadPool = ThreadPool.newSpecifiedInstance(tcount / 2);
		for (int i = 0; i < tcount; ++i) {
			Context tmpCtx = ctx.newComputeContext();
			Expression []tmpExps = Operation.dupExpressions(exps, tmpCtx);
			readers[i] = new CursorReadJob(cursors[i], fetchSize, tmpExps, hashUtil, tmpCtx, maxCacheSize, readyDatas);
			threadPool.submit(readers[i]);
		}
		this.readers = readers;
		this.tcount = tcount;
		this.threadPool = threadPool;
	}
	
	//�ȴ��߳����ȡ���� ���е�����˵�������ȡ����
	private void waitReadData() {
		if (close) {
			return;
		}
		
		while (readyDatas.size() == 0) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				throw new RQException(e);
			}
			CursorReadJob readers[] = this.readers;
			boolean allClosed = true;
			for (int i = 0, len = tcount; i < len; i++) {
				if (readers[i].isClosed()) continue;
				allClosed = false;
			}
			if (allClosed) {
				return;
			}
		}
	}
	
	//�ѻ��������������͵�datas��
	private void loadData() {
		if (close) {
			return;
		}
		
		//������������û������
		if (readyDatas.size() == 0) {
			waitReadData();
			if (readyDatas.size() == 0) {
				close = true;
				threadPool.shutdown();
				return;
			}
		}
		
		//��readyDatas�����������͵�datas��
		synchronized (readyDatas) {
			int size = readyDatas.size();
			if (size > 0) {
				while (size != 0) {
					datas.add(readyDatas.poll());
					curTimes.addInt(0);
					size--;
				}
			}
		}
	}
	
	/**
	 * ��ȡһ������
	 * @param index ���
	 * @return ���ݸ�ʽ:[��ȡ������, ����������, hash������]
	 */
	public synchronized Object[] getData(int index) {
		if (close && index > datas.size()) {
			return null;
		}
		
		while (index > datas.size() || datas.isNull(index)) {
			loadData();
			if (close) {
				return null;
			}
		}

		Object[] data = (Object[]) datas.get(index);//���ݿ��ȡ��
		int[] curTimesData = curTimes.getDatas();//��Ӧ��ŵļ�����++
		curTimesData[index]++;
		
		//�����һ�鱻�����ⲿ�̶߳�ȡ����
		if (curTimesData[index] == tcount) {
			datas.set(index, null);//���
			
			//�ӻ���ȡһ������
			synchronized (readyDatas) {
				if (readyDatas.size() > 0) {
					datas.add(readyDatas.poll());
					curTimes.addInt(0);
				}
			}
		}
		return data;
	}
	
	public ICursor getCursor() {
		return readers[0].getCursor();
	}
}

class CursorReadJob extends Job {
	protected ICursor cursor; // Ҫȡ�����α�
	protected int fetchCount; // ÿ�ζ�ȡ��������
	protected boolean isClosed;
	
	protected Expression[] exps;// ����hash����
	protected int keyCount;
	protected Context ctx;
	protected HashUtil hashUtil;
	protected int []hashCodes; // ���ڱ���ÿ�������ֶεĹ�ϣֵ
	
	protected Queue<Object[]> readyDatas;
	protected int maxCacheSize;
	/**
	 * �������α�ȡ��������ʹ��getTable�õ�ȡ�����
	 * @param threadPool �̳߳�
	 * @param cursor �α�
	 * @param fetchCount ÿ�ζ�ȡ��������
	 */
	public CursorReadJob(ICursor cursor, int fetchCount, Expression[] exps, 
			HashUtil hashUtil, Context ctx, int maxCacheSize, Queue<Object[]> readyDatas) {
		this.cursor = cursor;
		this.fetchCount = fetchCount;
		
		this.hashUtil = hashUtil;
		this.ctx = ctx;
		this.exps = exps;
		keyCount = exps.length;
		hashCodes = new int[keyCount];
		
		this.maxCacheSize = maxCacheSize;
		this.readyDatas = readyDatas;
	}

	/**
	 * ���̳߳�����̵߳��ã����α��ȡ����
	 */
	public void run() {
		HashUtil hashUtil = this.hashUtil;
		ICursor cursor = this.cursor;
		Context ctx = this.ctx;
		Expression[] exps = this.exps;
		int keyCount = this.keyCount;
		
		while (true) {
			Sequence table = cursor.fuzzyFetch(fetchCount);
			if (table == null) {
				isClosed = true;
				return;
			}
			
			Object[] data = new Object[3];
			data[0] = table;
			
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(table);
			stack.push(current);
			
			try {
				int len = table.length();
				int[] hash = new int[len + 1];
				if (keyCount == 1) {
					IArray array = exps[0].calculateAll(ctx);

					for (int i = 1; i <= len; ++i) {
						hash[i] = hashUtil.hashCode(array.hashCode(i));
					}
					data[1] = array;
				} else {
					IArray[] arrays = new IArray[keyCount];
					int[] hashCodes = this.hashCodes;

					for (int k = 0; k < keyCount; ++k) {
						arrays[k] = exps[k].calculateAll(ctx);
					}

					for (int i = 1; i <= len; ++i) {
						for (int k = 0; k < keyCount; ++k) {
							hashCodes[k] = arrays[k].hashCode(i);
						}

						hash[i] = hashUtil.hashCode(hashCodes, keyCount);
					}

					data[1] = arrays;
				}
				data[2] = new IntArray(hash, null, len);
			} finally {
				stack.pop();
			}
			
			while (readyDatas.size() > maxCacheSize) {
				//ֻ�е�ȡ���ȷ����ʱ���ܽ���
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					throw new RQException(e);
				}
			}
			synchronized (readyDatas) {
				readyDatas.add(data);
			}
		}
	}
	
	public ICursor getCursor() {
		return cursor;
	}
	
	public boolean isClosed() {
		return isClosed;
	}
}
