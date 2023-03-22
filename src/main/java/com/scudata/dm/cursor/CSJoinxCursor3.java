package com.scudata.dm.cursor;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.*;
import com.scudata.dm.op.Operation;
import com.scudata.dw.ColPhyTable;
import com.scudata.dw.Cursor;
import com.scudata.expression.Expression;
import com.scudata.parallel.ClusterCursor;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;

/**
 * �α�joinx�࣬�鲢joinx
 * �α���һ���ɷֶμ��ļ���ʵ��T���鲢join���㡣
 * @author LiWei
 * 
 */
public class CSJoinxCursor3 extends ICursor {
	private ICursor srcCursor;//Դ�α�
	private Expression []keys;//ά���ֶ�
	private Expression []exps;//�µı��ʽ
	
	private ICursor mergeCursor;//�鲢�α�
	private DataStruct ds = null;
	private int len1;//ԭ��¼�ֶ���
	private int len2;//�±��ʽ�ֶ���
	
	private boolean isEnd;
	private int n;//����������
	private String[] expNames;
	private String option;
	
	/**
	 * ������
	 * @param cursor	Դ�α�
	 * @param fields	��ʵjoin���ֶ�
	 * @param fileTable	ά�����
	 * @param keys		ά��join�ֶ�
	 * @param exps		ά���±��ʽ
	 * @param expNames	ά���±��ʽ����
	 * @param fname
	 * @param ctx
	 * @param n
	 * @param option
	 */
	public CSJoinxCursor3(ICursor cursor, Expression []fields, Object fileTable, 
			Expression[] keys, Expression[] exps, String[] expNames, String fname, Context ctx, int n, String option) {
		srcCursor = cursor;
		this.keys = keys;
		this.exps = exps;
		this.ctx = ctx;
		this.n = n;
		this.option = option;
		this.expNames = expNames;
		if (this.n < ICursor.FETCHCOUNT) {
			this.n = ICursor.FETCHCOUNT;
		}
		
		//���newNames����null������newExps���
		if (exps != null && expNames != null) {
			for (int i = 0, len = expNames.length; i < len; i++) {
				if (expNames[i] == null && exps[i] != null) {
					expNames[i] = exps[i].getFieldName();
				}
			}
		}

		//�鲢�����α�
		ICursor cursor2 = toCursor(fileTable);
		ICursor cursors[] = {cursor, cursor2};
		String names[] = {null, null};
		if (keys == null) {
			//û�й����ֶ�ʱȡ����
			String[] pkeys = cursor2.getDataStruct().getPrimary();
			int size = fields.length;
			if (pkeys == null || pkeys.length < size) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinx" + mm.getMessage("ds.lessKey"));
			}
			keys = new Expression[size];
			for (int i = 0; i < size; i++) {
				keys[i] = new Expression(pkeys[i]);
			}
		}
		Expression joinKeys[][] = {fields, keys};
		mergeCursor = joinx(cursors, names, joinKeys, option, ctx);
	}

	/**
	 * �α�Թ����ֶ�����������鲢����
	 * @param cursors �α�����
	 * @param names ������ֶ�������
	 * @param exps �����ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx Context ����������
	 * @return ICursor ������α�
	 */
	private static ICursor joinx(ICursor []cursors, String []names, Expression [][]exps, String opt, Context ctx) {
		boolean isPJoin = false, isIsect = false, isDiff = false;
		if (opt != null) {
			if (opt.indexOf('p') != -1) {
				isPJoin = true;
			} else if (opt.indexOf('i') != -1) {
				isIsect = true;
			} else if (opt.indexOf('d') != -1) {
				isDiff = true;
			}
		}
		
		int count = cursors.length;
		boolean isCluster = true; // �Ƿ��м�Ⱥ�α�
		boolean isMultipath = false; // �Ƿ��Ƕ�·�α�����
		int pathCount = 1;
		
		for (int i = 0; i < count; ++i) {
			if (cursors[i] instanceof IMultipath) {
				if (i == 0) {
					isMultipath = true;
					pathCount = ((IMultipath)cursors[i]).getPathCount();
				} else if (pathCount != ((IMultipath)cursors[i]).getPathCount()) {
					isMultipath = false;
				}
			} else {
				isMultipath = false;
			}
			
			if (!(cursors[i] instanceof ClusterCursor)) {
				isCluster = false;
			}
		}
		
		if (isCluster) {
			ClusterCursor []tmp = new ClusterCursor[count];
			System.arraycopy(cursors, 0, tmp, 0, count);
			return ClusterCursor.joinx(tmp, exps, names, opt, ctx);
		} else if (isMultipath && pathCount > 1) {
			// ��·�α����ͬ���ֶΣ�ֻҪÿ�������Ӧ·�����Ӽ���
			ICursor []result = new ICursor[pathCount];
			ICursor [][]multiCursors = new ICursor[count][];
			for (int i = 0; i < count; ++i) {
				IMultipath multipath = (IMultipath)cursors[i];
				multiCursors[i] = multipath.getParallelCursors();
			}
			
			for (int i = 0; i < pathCount; ++i) {
				if (isPJoin) {
					ICursor []curs = new ICursor[count];
					for (int c = 0; c < count; ++c) {
						curs[c] = multiCursors[c][i];
					}

					result[i] = new PJoinCursor(curs, names);
				} else if (isIsect || isDiff) {
					ICursor []curs = new ICursor[count];
					for (int c = 0; c < count; ++c) {
						curs[c] = multiCursors[c][i];
					}
					
					Context tmpCtx = ctx.newComputeContext();
					Expression [][]tmpExps = Operation.dupExpressions(exps, tmpCtx);
					result[i] = new MergeFilterCursor(curs, tmpExps, opt, tmpCtx);
				} else {
					if (count == 2 && exps[0].length == 1) {
						Context tmpCtx = ctx.newComputeContext();
						Expression exp1 = Operation.dupExpression(exps[0][0], tmpCtx);
						Expression exp2 = Operation.dupExpression(exps[1][0], tmpCtx);
						result[i] = new JoinxCursor3(multiCursors[0][i], exp1, multiCursors[1][i], exp2, names, opt, tmpCtx);
					} else {
						ICursor []curs = new ICursor[count];
						for (int c = 0; c < count; ++c) {
							curs[c] = multiCursors[c][i];
						}
						
						Context tmpCtx = ctx.newComputeContext();
						Expression [][]tmpExps = Operation.dupExpressions(exps, tmpCtx);
						result[i] = new JoinxCursor(curs, tmpExps, names, opt, tmpCtx);
					}
				}
			}
			
			// ÿһ·�Ĺ����������ɶ�·�α�
			return new MultipathCursors(result, ctx);
		} else if (isPJoin) {
			return new PJoinCursor(cursors, names);
		} else if (isIsect || isDiff) {
			return new MergeFilterCursor(cursors, exps, opt, ctx);
		} else {
			if (count == 2 && exps[0].length == 1) {
				// �Թ����ֶθ���Ϊ1�������������Ż�
				return new JoinxCursor2(cursors[0], exps[0][0], cursors[1], exps[1][0], names, opt, ctx);
			} else {
				return new JoinxCursor(cursors, exps, names, opt, ctx);
			}
		}
	}
	
	void init() {
		//��֯���ݽṹ
		if (option !=null && (option.indexOf('i') != -1 || option.indexOf('d') != -1)) {
			Sequence temp = mergeCursor.peek(1);
			if (temp != null) {
				BaseRecord r = (BaseRecord) temp.getMem(1);
				ds = r.dataStruct();
				len1 = 0;
			}
		} else {
			Sequence temp = mergeCursor.peek(1);
			if (temp != null) {
				BaseRecord r = (BaseRecord) temp.getMem(1);
				BaseRecord r1 = (BaseRecord) r.getNormalFieldValue(0);
				len1 = r1.getFieldCount();
				len2 = exps == null ? 0 : exps.length;
				String[] names = new String[len1 + len2];
				System.arraycopy(r1.getFieldNames(), 0, names, 0, len1);
				System.arraycopy(expNames, 0, names, len1, len2);
				ds = new DataStruct(names);
			}
		}
	}
	
	/**
	 * ��join�ֶκ��±��ʽ����ȡ��Ҫ���ֶ�
	 * @param dataExps
	 * @param newExps
	 * @param ctx
	 * @return
	 */
	private static String[] makeFields(Expression []dataExps, Expression []newExps ,Context ctx) {
		int len = dataExps.length;
		ArrayList<String> keys = new ArrayList<String>(len);
		for (int j = 0; j < len; j++) {
			keys.add(dataExps[j].toString());
		}
		for (Expression exp : newExps) {
			exp.getUsedFields(ctx, keys);
		}
		String[] arr = new String[keys.size()];
		keys.toArray(arr);
		return arr;
	}
	
	/**
	 * ��ά�����ת�����α�
	 * @param obj
	 * @return
	 */
	private ICursor toCursor(Object obj) {
		if (obj instanceof ColPhyTable) {
			String fields[] = makeFields(keys, exps, ctx);
			return (Cursor) ((ColPhyTable) obj).cursor(null, fields, null, null, null, null, null, ctx);
		} else if (obj instanceof FileObject) {
			return new BFileCursor((FileObject) obj, null, null, null);
		} else if (obj instanceof ICursor) {
			return (ICursor) obj;
		} else {
			return null;
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			exps = Operation.dupExpressions(exps, ctx);
			super.resetContext(ctx);
		}
	}

	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		
		if (ds == null) {
			init();
		}
		
		Sequence temp = mergeCursor.fetch(n);
		if (temp == null || temp.length() == 0) {
			return null;
		}
		
		if (len1 == 0) {
			return temp;
		}
		
		Context ctx = this.ctx;
		Expression []exps = this.exps;
		int len1 = this.len1;
		int len2 = this.len2;
		int len = temp.length();
		Table result = new Table(ds);
		for (int i = 1; i <= len; i++) {
			BaseRecord r = (BaseRecord) temp.getMem(i);
			BaseRecord r1 = (BaseRecord) r.getNormalFieldValue(0);
			Object r2 = r.getNormalFieldValue(1);
			
			BaseRecord record = result.newLast(r1.getFieldValues());
			for (int f = 0; f < len2; f++) {
				if (r2 != null) {
					if (r2 instanceof BaseRecord) {
						record.setNormalFieldValue(f + len1, ((BaseRecord)r2).calc(exps[f], ctx));	
					} else if (r2 instanceof Sequence) {
						record.setNormalFieldValue(f + len1, ((Sequence)r2).calc(exps[f], ctx));
					}
					
				}
			}
		}
		
		return result;
	}

	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		long total = 0;
		while (n > 0) {
			Sequence seq;
			if (n > FETCHCOUNT) {
				seq = get(FETCHCOUNT);
			} else {
				seq = get((int)n);
			}
			
			if (seq == null || seq.length() == 0) {
				break;
			}
			
			total += seq.length();
			n -= seq.length();
		}
		
		return total;
	}

	public synchronized void close() {
		super.close();
		srcCursor.close();
		isEnd = true;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		super.close();
		srcCursor.reset();
		isEnd = false;
		return true;
	}
	
	/**
	 * �鲢join���������ݣ�
	 * @param cursor	Դ�α�
	 * @param fields	��ʵjoin���ֶ�
	 * @param fileTable	ά�����
	 * @param keys		ά��join�ֶ�
	 * @param exps		ά���±��ʽ
	 * @param expNames	ά���±��ʽ����
	 * @param fname
	 * @param ctx
	 * @param n
	 * @param option
	 * @return
	 */
	public static ICursor MergeJoinx(ICursor cursor, Expression[][] fields, Object[] fileTable, Expression[][] keys,
			Expression[][] exps, String[][] expNames, String fname, Context ctx, int n, String option) {
		if (fileTable == null) {
			return null;
		}
		
		if (option.indexOf('i') == -1) {
			option += '1';
		} else {
			option = option.replaceAll("i", "");
		}
		
		if (cursor instanceof MultipathCursors) {
			return MultipathMergeJoinx((MultipathCursors)cursor, fields, fileTable, keys,
					exps, expNames, fname, ctx, n, option);
		}

		ICursor temp = null;
		FileObject tempFile = null;
		int fileCount =  fileTable.length;
		try {
			/**
			 * �Զ������ݽ���join��ÿ�εĽ��д�����ļ��������������һ��join��
			 */
			for (int i = 0; i < fileCount - 1; i++) {
				temp = new CSJoinxCursor3(cursor, fields[i], fileTable[i], keys[i], exps[i], 
						expNames[i], fname, ctx, n, option);
				
				tempFile = FileObject.createTempFileObject();
				cursor = new BFileCursor(tempFile, null, "x", ctx);
				tempFile.setFileSize(0);
				
				Sequence table = temp.fetch(FETCHCOUNT);
				while (table != null && table.length() != 0) {
					tempFile.exportSeries(table, "ab", null);
					table = temp.fetch(FETCHCOUNT);
				}
				temp = null;
			}
		} catch (Exception e) {
			if (temp != null) {
				temp.close();
			}
			if (tempFile != null && tempFile.isExists()) {
				tempFile.delete();
			}
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		int i = fileCount - 1;
		return new CSJoinxCursor3(cursor, fields[i], fileTable[i], keys[i], exps[i], 
				expNames[i], fname, ctx, n, option);
	}
	
	/**
	 * ��ά�����ת�����α�
	 * @param obj
	 * @return
	 */
	private static MultipathCursors toMultipathCursors(Object obj, MultipathCursors mcs,  String fields[], Context ctx) {
		if (obj instanceof ColPhyTable) {
			return (MultipathCursors) ((ColPhyTable) obj).cursor(null, fields, null, null, null, null, mcs, "k", ctx);
		} if (obj instanceof MultipathCursors) {
			return (MultipathCursors) obj;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("joinx" + mm.getMessage("dw.needMCursor"));
		}
	}
	
	public static ICursor MultipathMergeJoinx(MultipathCursors cursor, Expression[][] fields, Object[] fileTable, Expression[][] keys,
			Expression[][] exps, String[][] expNames, String fname, Context ctx, int n, String option) {
		ICursor[] cursors = cursor.getParallelCursors();
		int pathCount = cursor.getPathCount();
		ICursor results[] = new ICursor[pathCount];
		
		String[] names = makeFields(keys[0], exps[0], ctx);
		ICursor[] fileTableCursors = toMultipathCursors(fileTable[0], cursor, names, ctx).getParallelCursors();
		
		if (fileTableCursors == null) {
			for (int i = 0; i < pathCount; ++i) {
				Expression[][] fields_ = Operation.dupExpressions(fields, ctx);
				Expression[][] keys_ = Operation.dupExpressions(keys, ctx);
				Expression[][] exps_ = Operation.dupExpressions(exps, ctx);
				
				results[i] = MergeJoinx(cursors[i], fields_, fileTable,
						keys_, exps_, expNames, fname, ctx, n, option);
			}
		} else {
			if (fileTable.length != 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
			}
			for (int i = 0; i < pathCount; ++i) {
				Expression[][] fields_ = Operation.dupExpressions(fields, ctx);
				Expression[][] keys_ = Operation.dupExpressions(keys, ctx);
				Expression[][] exps_ = Operation.dupExpressions(exps, ctx);
				
				results[i] = MergeJoinx(cursors[i], fields_, new Object[] {fileTableCursors[i]},
						keys_, exps_, expNames, fname, ctx, n, option);
			}
		}
		
		return new MultipathCursors(results, ctx);
	}
}
