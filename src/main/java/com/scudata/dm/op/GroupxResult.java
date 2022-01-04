package com.scudata.dm.op;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileWriter;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.GroupmCursor;
import com.scudata.dm.cursor.GroupxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MergesCursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;

/**
 * ���ڶ�������������ִ�а����ֶν����������������
 * @author RunQian
 *
 */
public class GroupxResult implements IResult {
	private Expression[] exps; // ������ʽ
	private String []names; // �����ֶ���
	private Expression[] calcExps; // ���ܱ��ʽ
	private String []calcNames; // �����ֶ���
	private Context ctx; // ����������
	private String opt; // ѡ��

	private Node[] gathers = null; // �ۺϺ�������
	private DataStruct ds; // ��������ݽṹ
	private HashUtil hashUtil; // ��ϣ���ߣ����ڼ����ϣֵ
	
	// �Ƿ��������鲢�����ж��λ���
	private boolean isSort = true;

	// ��������鲢�����ж��λ���
	private ListBase1 []groups; // �����ϣ��
	private Table outTable; // ��ʱ���������
	private int []sortFields; // �����ֶΣ��������ֶΣ����
	private ArrayList<ICursor> cursorList = new ArrayList<ICursor>(); // ������ʱ�ļ���Ӧ���α�
	
	// ���ù�ϣ�����ֵ�����ļ�
	private RecordTree []recordsArray; // �����ϣ��
	private int totalRecordCount; // �ڴ��еķ�������¼����
	private final int fileCount = 29; // ��ʱ�ļ���
	private FileObject []tmpFiles; // ��ʱ�ļ�����
	private BFileWriter []writers; // ����д���ļ��Ķ���
	
	/**
	 * ����������������
	 * @param exps ������ʽ����
	 * @param names	�����ֶ�������
	 * @param calcExps ���ܱ��ʽ	����
	 * @param calcNames	�����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity	�ڴ��б��������������
	 */	
	public GroupxResult(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx, int capacity) {
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
		this.hashUtil = new HashUtil(capacity);

		capacity = hashUtil.getCapacity();
		int keyCount = exps.length;
		int valCount = this.calcExps == null ? 0 : this.calcExps.length;
		
		if (names == null) names = new String[keyCount];
		for (int i = 0; i < keyCount; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				names[i] = exps[i].getFieldName();
			}
		}

		if (this.calcNames == null) this.calcNames = new String[valCount];
		for (int i = 0; i < valCount; ++i) {
			if (this.calcNames[i] == null || this.calcNames[i].length() == 0) {
				this.calcNames[i] = this.calcExps[i].getFieldName();
			}
		}

		String[] colNames = new String[keyCount + valCount];
		System.arraycopy(names, 0, colNames, 0, keyCount);
		if (this.calcNames != null) {
			System.arraycopy(this.calcNames, 0, colNames, keyCount, valCount);
		}

		ds = new DataStruct(colNames);
		ds.setPrimary(names);
		this.gathers = Sequence.prepareGatherMethods(this.calcExps, ctx);
		
		if (opt == null || opt.indexOf('u') == -1) {
			groups = new ListBase1[capacity];
			outTable = new Table(ds, capacity);
			outTable.setPrimary(names);
	
			sortFields = new int[keyCount];
			for (int i = 0; i < keyCount; ++i) {
				sortFields[i] = i;
			}
		} else {
			isSort = false;
			recordsArray = new RecordTree[capacity];
		}
	}
	
	// �������Ҫ����ʱ�ķ��鷽��
	private void sortGroup(Sequence table, Context ctx) {
		ListBase1 []groups = this.groups;
		Node []gathers = this.gathers;
		Expression[] exps = this.exps;
		Expression[] calcExps = this.calcExps;
		HashUtil hashUtil = this.hashUtil;
		Table outTable = this.outTable;
		
		int keyCount = exps.length;
		int valCount = calcExps == null ? 0 : calcExps.length;

		final int INIT_GROUPSIZE = HashUtil.getInitGroupSize();
		Object []keys = new Object[keyCount];
		int capacity = groups.length;

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}

				Record r;
				int hash = hashUtil.hashCode(keys);
				if (groups[hash] == null) {
					if (outTable.length() == capacity) {
						outTable.finishGather1(gathers);
						outTable.sortFields(sortFields);
						FileObject fo = FileObject.createTempFileObject();
						MessageManager mm = EngineMessage.get();
						Logger.info(mm.getMessage("engine.createTmpFile") + fo.getFileName());

						fo.exportSeries(outTable, "b", null);
						BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
						cursorList.add(bfc);

						outTable.clear();
						for (int g = 0, glen = groups.length; g < glen; ++g) {
							groups[g] = null;
						}
					}

					groups[hash] = new ListBase1(INIT_GROUPSIZE);
					r = outTable.newLast(keys);
					groups[hash].add(r);
					
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					int index = HashUtil.bsearch_r(groups[hash], keys);
					if (index < 1) {
						if (outTable.length() == capacity) {
							outTable.finishGather1(gathers);
							outTable.sortFields(sortFields);
							FileObject fo = FileObject.createTempFileObject();
							MessageManager mm = EngineMessage.get();
							Logger.info(mm.getMessage("engine.createTmpFile") + fo.getFileName());

							fo.exportSeries(outTable, "b", null);
							BFileCursor bfc = new BFileCursor(fo, null, "x", ctx);
							cursorList.add(bfc);
	
							outTable.clear();
							for (int g = 0, glen = groups.length; g < glen; ++g) {
								groups[g] = null;
							}
	
							groups[hash] = new ListBase1(INIT_GROUPSIZE);
							r = outTable.newLast(keys);
							groups[hash].add(r);
						} else {
							r = outTable.newLast(keys);
							groups[hash].add( -index, r);
						}
						
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(ctx);
							r.setNormalFieldValue(f, val);
						}
					} else {
						r = (Record)groups[hash].get(index);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
							r.setNormalFieldValue(f, val);
						}
					}
				}
			}
		} catch(RuntimeException e) {
			delete();
			throw e;
		} finally {
			stack.pop();
		}
	}
	
	private void writeTempFile(RecordTree []recordsArray) throws IOException {
		final int fileCount = this.fileCount;
		FileObject []tmpFiles = this.tmpFiles;
		BFileWriter []writers = this.writers;
		int len = recordsArray.length;
		
		if (tmpFiles == null) {
			tmpFiles = new FileObject[fileCount];
			writers = new BFileWriter[fileCount];
			this.tmpFiles = tmpFiles;
			this.writers = writers;
			MessageManager mm = EngineMessage.get();
			
			for (int i = 0; i < fileCount; ++i) {
				tmpFiles[i] = FileObject.createTempFileObject();
				writers[i] = new BFileWriter(tmpFiles[i], null);
				writers[i].prepareWrite(ds, false);
				
				Logger.info(mm.getMessage("engine.createTmpFile") + tmpFiles[i].getFileName());
				BFileCursor cursor = new BFileCursor(tmpFiles[i], null, "x", ctx);
				cursorList.add(cursor);
			}
		}
		
		Sequence []seqs = new Sequence[fileCount];
		int initSize = totalRecordCount / fileCount + 1024;
		for (int i = 0; i < fileCount; ++i) {
			seqs[i] = new Sequence(initSize);
		}
		
		for (int i = 0; i < len; ++i) {
			if (recordsArray[i] != null) {
				recordsArray[i].recursiveTraverse(seqs[i % fileCount]);
				recordsArray[i] = null;
			}
		}
		
		for (int i = 0; i < fileCount; ++i) {
			seqs[i].finishGather1(this.gathers);
			writers[i].write(seqs[i]);

		}
	}
	
	// ���������Ҫ����ʱ�ķ��鷽��
	private void hashGroup(Sequence table, Context ctx) {
		DataStruct ds = this.ds;
		RecordTree []recordsArray = this.recordsArray;
		Node []gathers = this.gathers;
		Expression[] exps = this.exps;
		Expression[] calcExps = this.calcExps;
		HashUtil hashUtil = this.hashUtil;
		int totalRecordCount = this.totalRecordCount;
		
		int keyCount = exps.length;
		int valCount = calcExps == null ? 0 : calcExps.length;

		Object []keys = new Object[keyCount];
		int capacity = recordsArray.length;

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}

				Record r;
				int hash = hashUtil.hashCode(keys);
				if (recordsArray[hash] == null) {
					if (totalRecordCount == capacity) {
						writeTempFile(recordsArray);
						totalRecordCount = 0;
					}

					totalRecordCount++;
					r = new Record(ds, keys);
					recordsArray[hash] = new RecordTree(r);
					
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					RecordTree.Node node = recordsArray[hash].get(keys);
					r = node.r;
					
					// ����û���ҵ���Ӧ����ֵ�ļ�¼
					if (r == null) {
						r = new Record(ds, keys);
						node.r = r;
						
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(ctx);
							r.setNormalFieldValue(f, val);
						}
						
						if (totalRecordCount < capacity) {
							totalRecordCount++;
						} else {
							writeTempFile(recordsArray);
							totalRecordCount = 0;
						}
					} else {
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
							r.setNormalFieldValue(f, val);
						}
					}
				}
			}
			
			this.totalRecordCount = totalRecordCount;
		} catch(RuntimeException re) {
			delete();
			throw re;
		} catch(Exception e) {
			delete();
			throw new RQException(e.getMessage(), e);
		} finally {
			stack.pop();
		}
	}
	
	/**
	 * �Դ�������������飬�ۻ������з�����
	 * @param table ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		if (isSort) {
			sortGroup(table, ctx);
		} else {
			hashGroup(table, ctx);
		}
	}
	
	/**
	 * ɾ����ʱ�ļ�������ڴ�����
	 */	
	private void delete() {
		this.hashUtil = null;
		
		if (isSort) {
			this.groups = null;
			this.outTable = null;
			for (ICursor cursor : cursorList) {
				cursor.close();
			}
		} else {
			this.recordsArray = null;
			if (writers != null) {
				for (BFileWriter writer : writers) {
					writer.close();
				}
				
				for (FileObject file : tmpFiles) {
					file.delete();
				}
			}
		}
	}

	// ȡ�������Ҫ����ʱ�ķ��鷽�����ɵĽ����
	private ICursor sortGroupResult() {
		ListBase1 []groups = this.groups;
		if (groups == null) return null;
		
		ArrayList<ICursor> cursorList = this.cursorList;
		int size = cursorList.size();
		if (size > 0) {
			int bufSize = Env.getMergeFileBufSize(size);
			for (int i = 0; i < size; ++i) {
				BFileCursor bfc = (BFileCursor)cursorList.get(i);
				bfc.setFileBufferSize(bufSize);
			}
		}

		if (outTable.length() > 0) {
			if (size == 0) {
				outTable.finishGather(gathers);
			} else {
				outTable.finishGather1(gathers);
			}
			
			outTable.sortFields(sortFields);
			cursorList.add(new MemoryCursor(outTable));
			size++;
		}

		this.hashUtil = null;
		this.groups = null;
		this.outTable = null;

		if (size == 0) {
			return null;
		} else if (size == 1) {
			return (ICursor)cursorList.get(0);
		} else {
			int keyCount = exps.length;
			ICursor []cursors = new ICursor[size];
			cursorList.toArray(cursors);
			Expression []keyExps = new Expression[keyCount];
			for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
				keyExps[i] = new Expression(ctx, "#" + q);
			}

			MergesCursor mc = new MergesCursor(cursors, keyExps, ctx);
			int valCount = calcExps == null ? 0 : calcExps.length;
			Expression []valExps = new Expression[valCount];
			for (int i = 0, q = keyCount + 1; i < valCount; ++i, ++q) {
				valExps[i] = gathers[i].getRegatherExpression(q);
			}

			return new GroupmCursor(mc, keyExps, names, valExps, calcNames, ctx);
		}
	}
	
	// ȡ���������Ҫ����ʱ�ķ��鷽�����ɵĽ����
	private ICursor hashGroupResult() {
		RecordTree []recordsArray = this.recordsArray;
		if (recordsArray == null) return null;
		
		// �ж��Ƿ��õ������
		FileObject []tmpFiles = this.tmpFiles;
		if (tmpFiles == null) {
			Sequence seq = new Sequence(totalRecordCount);
			for (RecordTree tree : recordsArray) {
				if (tree != null) {
					tree.recursiveTraverse(seq);
				}
			}
			seq.finishGather(gathers);
			this.recordsArray = null;
			this.tmpFiles = null;
			this.writers = null;
			return new MemoryCursor(seq);
		}
		
		try {
			// ���ڴ��еļ�¼д����Ӧ���ļ�
			writeTempFile(recordsArray);
		} catch(Exception e) {
			delete();
			throw new RQException(e.getMessage(), e);
		}

		// �ر�д
		BFileWriter []writers = this.writers;
		for (BFileWriter writer : writers) {
			writer.close();
		}
		
		int fileCount = this.fileCount;
		ICursor []cursors = new ICursor[fileCount];
		
		// ȡ���ξۺ���Ҫ�õı��ʽ
		int keyCount = exps.length;
		Expression []keyExps = new Expression[keyCount];
		for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
			keyExps[i] = new Expression(ctx, "#" + q);
		}

		int valCount = calcExps == null ? 0 : calcExps.length;
		Expression []valExps = new Expression[valCount];
		for (int i = 0, q = keyCount + 1; i < valCount; ++i, ++q) {
			valExps[i] = gathers[i].getRegatherExpression(q);
		}
		
		int capacity = hashUtil.getPrevCapacity();
		for (int i = 0; i < fileCount; ++i) {
			ICursor cursor = cursorList.get(i);
			cursors[i] = new GroupxCursor(cursor, keyExps, names, valExps, calcNames, opt, ctx, capacity);
		}

		this.recordsArray = null;
		this.tmpFiles = null;
		this.writers = null;
		
		return new ConjxCursor(cursors);
	}
	
	/**
	 * ���ؽ���α�
	 * @return ICursor
	 */
	public ICursor getResultCursor() {
		if (isSort) {
			return sortGroupResult();
		} else {
			return hashGroupResult();
		}
	}
	
	/**
	 * ���ؼ�����
	 * @return Object
	 */
	public Object result() {
		return getResultCursor();
	}
	
	/**
	 * ��֧�ִ˷���
	 */
	public Object combineResult(Object []results) {
		throw new RuntimeException();
	}
}