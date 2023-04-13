package com.scudata.dm.cursor;

import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ���α�������鲢���ӣ��α갴�����ֶ�����
 * cs.joinx(C:��,f:K:��,x:F,��;��)
 * @author RunQian
 *
 */
public class MergeJoinxCursor extends ICursor {
	private ICursor srcCursor;
	private Expression [][]exps; // �����ֶα��ʽ����
	private ICursor []cursors; // ������α�����
	private Expression [][]codeExps; // ������������ʽ����
	private Expression [][]newExps; // ȡ���Ĵ������ֶα��ʽ����
	private String [][]newNames; // ȡ���Ĵ������ֶ�������
	
	//private String opt; // ѡ��
	private boolean isIsect; // �����ӣ�Ĭ��Ϊ������
	private boolean isDiff; // �����ӣ�Ĭ��Ϊ������
	
	private DataStruct oldDs; // Դ�����ݽṹ
	private DataStruct newDs; // ��������ݽṹ
	private Expression [][]codeAllExps; // �����������ѡ���ֶα��ʽ����
	private ObjectArray[][] codeArrays;//�����Ĺ����ֶκ�ȡ���ֶ���ɵ�����
	private int []seqs; // �����ĵ�ǰ���
	private boolean containNull = false; // �Ƿ��еĴ����Ϊ��
		
	protected Sequence cache; // ���������
	private boolean isEnd = false; // �Ƿ�ȡ������

	public MergeJoinxCursor(ICursor srcCursor, Expression[][] exps, ICursor []cursors, Expression[][] codeExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		this.srcCursor = srcCursor;
		this.exps = exps;
		this.cursors = cursors;
		this.codeExps = codeExps;
		this.newExps = newExps;
		this.newNames = newNames;
		this.ctx = ctx;
		//this.opt = opt;
		
		if (opt != null) {
			if (opt.indexOf('i') != -1) {
				isIsect = true;
			} else if (opt.indexOf('d') != -1) {
				isDiff = true;
				this.newExps = null;
				this.newNames = null;
			}
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			srcCursor.resetContext(ctx);
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			exps = Operation.dupExpressions(exps, ctx);
			newExps = Operation.dupExpressions(newExps, ctx);
			super.resetContext(ctx);
		}
	}
	
	private void fetchDimTableData(int t) {
		seqs[t] = 1;
		Sequence codeData = cursors[t].fuzzyFetch(ICursor.FETCHCOUNT);
		
		if (codeData != null && codeData.length() > 0) {
			Expression []curAllExps = codeAllExps[t];
			int fcount = curAllExps.length;
			int len = codeData.length();
			ObjectArray []curArrays = codeArrays[t];
			
			for (int f = 0; f < fcount; ++f) {
				curArrays[f].clear();
				curArrays[f].ensureCapacity(len);
			}

			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(codeData);
			stack.push(current);
			
			try {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < fcount; ++f) {
						curArrays[f].push(curAllExps[f].calculate(ctx));
					}
				}
			} finally {
				stack.pop();
			}
		} else {
			codeArrays[t] = null;
			containNull = true;
			if (isIsect) {
				isEnd = true;
			}
		}
	}

	private void init(Sequence data, Context ctx) {
		if (newDs != null) {
			return;
		}
		
		oldDs = data.dataStruct();
		if (oldDs == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		Sequence resultFields = new Sequence();
		resultFields.addAll(oldDs.getFieldNames());
		int tableCount = cursors.length;
		codeArrays = new ObjectArray[tableCount][];
		codeAllExps = new Expression[tableCount][];
		seqs = new int[tableCount];
		ComputeStack stack = ctx.getComputeStack();
		
		for (int t = 0; t < tableCount; ++t) {
			Expression []curNewExps = newExps != null ? newExps[t] : null;;
			if (curNewExps != null) {
				String []curNames = newNames != null ? newNames[t] : null;
				int newFieldCount = curNewExps.length;
				if (curNames == null) {
					curNames = new String[newFieldCount];
				}
				
				for (int f = 0; f < newFieldCount; ++f) {
					if (curNames[f] == null || curNames[f].length() == 0) {
						curNames[f] = curNewExps[f].getFieldName();
					}
				}
				
				resultFields.addAll(curNames);
			}
			
			seqs[t] = 1;
			Sequence codeData = null;
			if (cursors[t] != null) {
				codeData = cursors[t].fuzzyFetch(ICursor.FETCHCOUNT);
			}
			
			if (codeData != null && codeData.length() > 0) {
				int joinFieldCount = exps[t].length;
				Expression []curKeyExps = codeExps != null ? codeExps[t] : null;
				if (curKeyExps == null) {
					DataStruct curDs = codeData.dataStruct();
					if (curDs == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("ds.lessKey"));
					}
					
					int []pks = curDs.getPKIndex();
					if (pks == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("ds.lessKey"));
					}
					
					if (pks.length != joinFieldCount) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
					
					curKeyExps = new Expression[joinFieldCount];
					for (int f = 0; f < joinFieldCount; ++f) {
						curKeyExps[f] = new Expression(ctx, "#" + (pks[f] + 1));
					}
				} else {
					if (curKeyExps.length != joinFieldCount) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
				}
				
				Expression []curAllExps;
				if (curNewExps != null) {
					curAllExps = new Expression[joinFieldCount + curNewExps.length];
					System.arraycopy(curKeyExps, 0, curAllExps, 0, joinFieldCount);
					System.arraycopy(curNewExps, 0, curAllExps, joinFieldCount, curNewExps.length);
				} else {
					curAllExps = curKeyExps;
				}
				
				int len = codeData.length();
				int fcount = curAllExps.length;
				ObjectArray []curArrays = new ObjectArray[fcount];
				for (int f = 0; f < fcount; ++f) {
					curArrays[f] = new ObjectArray(len);
				}
				
				codeAllExps[t] = curAllExps;
				codeArrays[t] = curArrays;
				Current current = new Current(codeData);
				stack.push(current);
				
				try {
					for (int i = 1; i <= len; ++i) {
						current.setCurrent(i);
						for (int f = 0; f < fcount; ++f) {
							curArrays[f].push(curAllExps[f].calculate(ctx));
						}
					}
				} finally {
					stack.pop();
				}
			} else {
				int joinFieldCount = exps[t].length;
				Expression []curKeyExps = codeExps != null ? codeExps[t] : null;
				
				Expression []curAllExps;
				if (curNewExps != null) {
					curAllExps = new Expression[joinFieldCount + curNewExps.length];
					if (curKeyExps != null) {
						System.arraycopy(curKeyExps, 0, curAllExps, 0, joinFieldCount);
					}
					
					System.arraycopy(curNewExps, 0, curAllExps, joinFieldCount, curNewExps.length);
				} else {
					if (curKeyExps != null) {
						curAllExps = curKeyExps;
					} else {
						curAllExps = new Expression[joinFieldCount];
					}
				}
				
				codeAllExps[t] = curAllExps;
				containNull = true;
				if (isIsect) {
					isEnd = true;
				}
			}
		}
		
		int resultFieldCount = resultFields.length();
		if (resultFieldCount > oldDs.getFieldCount()) {
			String []names = new String[resultFieldCount];
			resultFields.toArray(names);
			newDs = new DataStruct(names);
			
			String []oldKey = oldDs.getPrimary();
			if (oldKey != null) {
				newDs.setPrimary(oldKey);
			}
		} else {
			newDs = oldDs;
		}
	}
	
	private void leftJoin(ObjectArray []fkArrays, int t, Table result, int findex) {
		int fkCount = fkArrays.length;
		if (fkCount == 1) {
			leftJoin(fkArrays[0], t, result, findex);
			return;
		}
		
		int len = fkArrays[0].size();
		ObjectArray []curCodeArrays = codeArrays[t];
		int codeLen = curCodeArrays[0].size();
		int fieldCount = curCodeArrays.length;
		int curSeq = seqs[t];
		int cmp = 0;
		
		Next:
		for (int i = 1; i <= len;) {
			for (int f = 0; f < fkCount; ++f) {
				cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
				if (cmp != 0) {
					break;
				}
			}

			if (cmp == 0) {
				Record r = (Record)result.getMem(i++);
				for (int f = fkCount, fseq = findex; f < fieldCount; ++f, ++fseq) {
					r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
				}
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					for (int f = 0; f < fkCount; ++f) {
						cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
						if (cmp != 0) {
							break;
						}
					}
					
					if (cmp == 0) {
						Record r = (Record)result.getMem(i++);
						for (int f = fkCount, fseq = findex; f < fieldCount; ++f, ++fseq) {
							r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
						}
						
						continue Next;
					} else if (cmp < 0) {
						i++;
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					break;
				} else {
					codeLen = curCodeArrays[0].size();
				}
			} else {
				i++;
			}
		}
		
		seqs[t] = curSeq;
	}
	
	private void leftJoin(ObjectArray fkArray, int t, Table result, int findex) {
		int len = fkArray.size();
		ObjectArray []curCodeArrays = codeArrays[t];
		ObjectArray codeKeyArray = curCodeArrays[0];
		int codeLen = codeKeyArray.size();
		int fieldCount = curCodeArrays.length;
		int curSeq = seqs[t];
		
		Next:
		for (int i = 1; i <= len;) {
			int cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
			if (cmp == 0) {
				Record r = (Record)result.getMem(i++);
				for (int f = 1, fseq = findex; f < fieldCount; ++f, ++fseq) {
					r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
				}
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
					if (cmp == 0) {
						Record r = (Record)result.getMem(i++);
						for (int f = 1, fseq = findex; f < fieldCount; ++f, ++fseq) {
							r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
						}
						
						continue Next;
					} else if (cmp < 0) {
						i++;
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					break;
				} else {
					codeKeyArray = curCodeArrays[0];
					codeLen = codeKeyArray.size();
				}
			} else {
				i++;
			}
		}
		
		seqs[t] = curSeq;
	}
	
	private Sequence leftJoin(Sequence data, Context ctx) {
		if (newExps == null) {
			return data;
		}
		
		int findex = oldDs.getFieldCount();
		int len = data.length();
		Table result = new Table(newDs, len);

		for (int i = 1; i <= len; ++i) {
			BaseRecord old = (BaseRecord)data.getMem(i);
			result.newLast(old.getFieldValues());
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(result);
		stack.push(current);
		
		try {
			for (int t = 0, tcount = exps.length; t < tcount; ++t) {
				// ��ǰά��û��ѡ���ֶ�������
				if (newExps[t] == null) {
					continue;
				}
				
				Expression []curExps = exps[t];
				int joinFieldCount = curExps.length;
				
				if (codeArrays[t] != null) {
					// ��ǰά���α껹û�б�����
					ObjectArray []fkArrays = new ObjectArray[joinFieldCount];
					for (int f = 0; f < joinFieldCount; ++f) {
						fkArrays[f] = new ObjectArray(len);
					}
					
					for (int i = 1; i <= len; ++i) {
						current.setCurrent(i);
						for (int f = 0; f < joinFieldCount; ++f) {
							fkArrays[f].push(curExps[f].calculate(ctx));
						}
					}
					
					leftJoin(fkArrays, t, result, findex);
				}
				
				findex += codeAllExps[t].length - joinFieldCount;
			}
		} finally {
			stack.pop();
		}
		
		return result;
	}
	
	private void innerJoin(ObjectArray fkArray, int t, Table result, int findex) {
		int len = fkArray.size();
		ObjectArray resultArray = new ObjectArray(len);
		ObjectArray []curCodeArrays = codeArrays[t];
		ObjectArray codeKeyArray = curCodeArrays[0];
		int codeLen = codeKeyArray.size();
		int fieldCount = curCodeArrays.length;
		int curSeq = seqs[t];
				
		Next:
		for (int i = 1; i <= len;) {
			int cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
			if (cmp == 0) {
				Record r = (Record)result.getMem(i++);
				resultArray.push(r);
				for (int f = 1, fseq = findex; f < fieldCount; ++f, ++fseq) {
					r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
				}
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
					if (cmp == 0) {
						Record r = (Record)result.getMem(i++);
						resultArray.push(r);
						for (int f = 1, fseq = findex; f < fieldCount; ++f, ++fseq) {
							r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
						}

						continue Next;
					} else if (cmp < 0) {
						i++;
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					break;
				} else {
					codeKeyArray = curCodeArrays[0];
					codeLen = codeKeyArray.size();
				}
			} else {
				i++;
			}
		}
		
		seqs[t] = curSeq;
		result.setMems(resultArray);
	}
	
	private void innerJoin(ObjectArray []fkArrays, int t, Table result, int findex) {
		int fkCount = fkArrays.length;
		if (fkCount == 1) {
			innerJoin(fkArrays[0], t, result, findex);
			return;
		}
		
		int len = fkArrays[0].size();
		ObjectArray resultArray = new ObjectArray(len);
		ObjectArray []curCodeArrays = codeArrays[t];
		int codeLen = curCodeArrays[0].size();
		int fieldCount = curCodeArrays.length;
		int curSeq = seqs[t];
		int cmp = 0;
		
		Next:
		for (int i = 1; i <= len;) {
			for (int f = 0; f < fkCount; ++f) {
				cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
				if (cmp != 0) {
					break;
				}
			}
			
			if (cmp == 0) {
				Record r = (Record)result.getMem(i++);
				resultArray.push(r);
				for (int f = fkCount, fseq = findex; f < fieldCount; ++f, ++fseq) {
					r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
				}
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					for (int f = 0; f < fkCount; ++f) {
						cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
						if (cmp != 0) {
							break;
						}
					}
					
					if (cmp == 0) {
						Record r = (Record)result.getMem(i++);
						resultArray.push(r);
						for (int f = fkCount, fseq = findex; f < fieldCount; ++f, ++fseq) {
							r.setNormalFieldValue(fseq, curCodeArrays[f].get(curSeq));
						}

						continue Next;
					} else if (cmp < 0) {
						i++;
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					break;
				} else {
					codeLen = curCodeArrays[0].size();
				}
			} else {
				i++;
			}
		}
		
		seqs[t] = curSeq;
		result.setMems(resultArray);
	}
	
	private Sequence innerJoin(Sequence data, Context ctx) {
		if (containNull) {
			return null;
		}
		
		int findex = oldDs.getFieldCount();
		int len = data.length();
		Table result = new Table(newDs, len);

		for (int i = 1; i <= len; ++i) {
			BaseRecord old = (BaseRecord)data.getMem(i);
			result.newLast(old.getFieldValues());
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(result);
		stack.push(current);
		
		try {
			for (int t = 0, tcount = exps.length; t < tcount; ++t) {
				Expression []curExps = exps[t];
				int joinFieldCount = curExps.length;
				
				ObjectArray []fkArrays = new ObjectArray[joinFieldCount];
				for (int f = 0; f < joinFieldCount; ++f) {
					fkArrays[f] = new ObjectArray(len);
				}
				
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < joinFieldCount; ++f) {
						fkArrays[f].push(curExps[f].calculate(ctx));
					}
				}
				
				innerJoin(fkArrays, t, result, findex);
				len = result.length();
				if (len == 0) {
					return null;
				}
				
				findex += codeAllExps[t].length - joinFieldCount;
			}
		} finally {
			stack.pop();
		}
		
		return result;
	}
	
	private void diffJoin(ObjectArray fkArray, int t, Sequence result) {
		ObjectArray []curCodeArrays = codeArrays[t];
		if (curCodeArrays == null) {
			return;
		}
		
		int len = fkArray.size();
		ObjectArray resultArray = new ObjectArray(len);
		ObjectArray codeKeyArray = curCodeArrays[0];
		int codeLen = codeKeyArray.size();
		int curSeq = seqs[t];
				
		Next:
		for (int i = 1; i <= len;) {
			int cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
			if (cmp == 0) {
				++i;
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					cmp = fkArray.compareTo(i, codeKeyArray, curSeq);
					if (cmp == 0) {
						++i;
						continue Next;
					} else if (cmp < 0) {
						Object r = result.getMem(i++);
						resultArray.push(r);
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					for (; i <= len; ++i) {
						Object r = result.getMem(i);
						resultArray.push(r);
					}
					
					break;
				} else {
					codeKeyArray = curCodeArrays[0];
					codeLen = codeKeyArray.size();
				}
			} else {
				Object r = result.getMem(i++);
				resultArray.push(r);
			}
		}
		
		seqs[t] = curSeq;
		result.setMems(resultArray);
	}
	
	private void diffJoin(ObjectArray []fkArrays, int t, Sequence result) {
		int fkCount = fkArrays.length;
		if (fkCount == 1) {
			diffJoin(fkArrays[0], t, result);
			return;
		}
		
		ObjectArray []curCodeArrays = codeArrays[t];
		if (curCodeArrays == null) {
			return;
		}
		
		int len = fkArrays[0].size();
		ObjectArray resultArray = new ObjectArray(len);
		int codeLen = curCodeArrays[0].size();
		int curSeq = seqs[t];
		int cmp = 0;
		
		Next:
		for (int i = 1; i <= len;) {
			for (int f = 0; f < fkCount; ++f) {
				cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
				if (cmp != 0) {
					break;
				}
			}
			
			if (cmp == 0) {
				++i;
			} else if (cmp > 0) {
				for (++curSeq; curSeq <= codeLen; ++curSeq) {
					for (int f = 0; f < fkCount; ++f) {
						cmp = fkArrays[f].compareTo(i, curCodeArrays[f], curSeq);
						if (cmp != 0) {
							break;
						}
					}
					
					if (cmp == 0) {
						++i;
						continue Next;
					} else if (cmp < 0) {
						Object r = result.getMem(i++);
						resultArray.push(r);
						continue Next;
					}
				}
				
				// ��ȡά�����һ��
				fetchDimTableData(t);
				curCodeArrays = codeArrays[t];
				curSeq = 1;
				
				if (curCodeArrays == null) {
					// ά���Ѿ���������β
					for (; i <= len; ++i) {
						Object r = result.getMem(i);
						resultArray.push(r);
					}
					
					break;
				} else {
					codeLen = curCodeArrays[0].size();
				}
			} else {
				Object r = result.getMem(i++);
				resultArray.push(r);
			}
		}
		
		seqs[t] = curSeq;
		result.setMems(resultArray);
	}

	private Sequence diffJoin(Sequence data, Context ctx) {
		int len = data.length();
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(data);
		stack.push(current);
		
		try {
			for (int t = 0, tcount = exps.length; t < tcount; ++t) {
				Expression []curExps = exps[t];
				int joinFieldCount = curExps.length;
				
				ObjectArray []fkArrays = new ObjectArray[joinFieldCount];
				for (int f = 0; f < joinFieldCount; ++f) {
					fkArrays[f] = new ObjectArray(len);
				}
				
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < joinFieldCount; ++f) {
						fkArrays[f].push(curExps[f].calculate(ctx));
					}
				}
				
				diffJoin(fkArrays, t, data);
				len = data.length();
				if (len == 0) {
					return null;
				}
			}
		} finally {
			stack.pop();
		}
		
		return data;
	}

	private Sequence process(Sequence seq) {
		init(seq, ctx);
		if (isIsect) {
			return innerJoin(seq, ctx);
		} else if (isDiff) {
			return diffJoin(seq, ctx);
		} else {
			return leftJoin(seq, ctx);
		}
	}
	
	/**
	 * ģ��ȡ��¼�����صļ�¼�����Բ��������������ͬ
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	protected Sequence fuzzyGet(int n) {
		if (n < 1) {
			return null;
		}
		
		Sequence result = cache;
		cache = null;
		
		while (!isEnd && (result == null || result.length() < n)) {
			Sequence seq = srcCursor.fuzzyFetch(n);
			if (seq == null || seq.length() == 0) {
				return result;
			}
			
			Sequence curResult = process(seq);
			if (curResult != null) {
				if (result == null) {
					result = curResult;
				} else {
					result = append(result, curResult);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		Sequence result = fuzzyGet(n);
		if (result == null || result.length() <= n) {
			return result;
		} else {
			cache = result.split(n + 1);
			return result;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		Sequence data;
		long rest = n;
		long count = 0;
		
		while (rest != 0) {
			if (rest > FETCHCOUNT) {
				data = get(FETCHCOUNT);
			} else {
				data = get((int)rest);
			}
			
			if (data == null) {
				break;
			} else {
				count += data.length();
			}
			
			rest -= data.length();
		}
		
		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		
		oldDs = null;
		newDs = null;
		codeArrays = null;
		cache = null;
		isEnd = true;
		
		srcCursor.close();
		for (int i = 0, count = cursors.length; i < count; ++i) {
			cursors[i].close();
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		if (!srcCursor.reset()) {
			return false;
		}
		
		for (int i = 0, count = cursors.length; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		containNull = false;
		isEnd = false;
		return true;
	}
}
