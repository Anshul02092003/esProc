package com.scudata.dm.op;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.CurrentElement;
import com.scudata.expression.CurrentSeq;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.parallel.ClusterMemoryTable;
import com.scudata.resources.EngineMessage;

/**
 * ���α��ܵ�����join���㣬���ӵı��а���Զ�̱�
 * @author WangXiaoJun
 *
 */
public class JoinRemote extends Operation {
	private String fname; // @oѡ����ʹ�ã�ԭ��¼������Ϊ�¼�¼���ֶ�
	private Expression [][]exps; // �����ֶα��ʽ����
	private Object[] datas; // ���������
	private Expression [][]dataExps; // ������������ʽ����
	private Expression [][]newExps; // ȡ���Ĵ������ֶα��ʽ����
	private String [][]newExpStrs; // ȡ���Ĵ������ֶα��ʽ�ַ���
	private String [][]newNames; // ȡ���Ĵ������ֶ�������
	private String opt; // ѡ��
	
	private DataStruct oldDs; // Դ�����ݽṹ
	private DataStruct newDs; // ��������ݽṹ
	private IndexTable []indexTables; // �����hashֵ����
	private Sequence []codes; // ���ش�������飬���ĳ���Ǽ�Ⱥ���������Ӧλ��Ϊ��
	private ClusterMemoryTable []cts; // ��Ⱥ��������飬���ĳ���Ǳ��ش��������Ӧλ��Ϊ��
	
	private boolean isIsect; // �����ӣ�Ĭ��Ϊ������
	private boolean isOrg;
	private boolean containNull; // �Ƿ��еĴ����Ϊ��
	
	public JoinRemote(String fname, Expression[][] exps, 
			Object[] datas, Expression[][] dataExps, 
			Expression[][] newExps, String[][] newNames, String opt) {
		this(null, fname, exps, datas, dataExps, newExps, newNames, opt);
	}
	
	public JoinRemote(Function function, String fname, Expression[][] exps, 
			Object[] datas, Expression[][] dataExps, 
			Expression[][] newExps, String[][] newNames, String opt) {
		super(function);
		this.fname = fname;
		this.exps = exps;
		this.datas = datas;
		this.dataExps = dataExps;
		this.newExps = newExps;
		this.opt = opt;
		
		if (opt != null) {
			if (opt.indexOf('i') != -1) isIsect = true;
			if (opt.indexOf('o') != -1) isOrg = true;
		}
		
		ArrayList<String[]> srcFieldsList = new ArrayList<String[]>();
		ArrayList<String> refFieldList = new ArrayList<String>();

		int count = datas.length;		
		newExpStrs = new String[count][];
		if (newNames == null) newNames = new String[count][];
		
		for (int i = 0; i < count; ++i) {
			Expression []curExps = newExps[i];
			int curLen = curExps.length;

			newExpStrs[i] = new String[curLen];
			if (newNames[i] == null) newNames[i] = new String[curLen];
			String []curNames = newNames[i];

			for (int j = 0; j < curLen; ++j) {
				newExpStrs[i][j] = curExps[j].toString();
				if (curNames[j] == null || curNames[j].length() == 0) {
					curNames[j] = curExps[j].getFieldName();
				}
			}
			
			// x��~ʱ���ڽ������м�¼F��C:����Ӧ��ϵ����ʶ��Ԥ�������
			if (curLen == 1 && curExps[0].getHome() instanceof CurrentElement) {
				Expression []srcExps = exps[i];
				int srcCount = srcExps.length;
				String []srcFields = new String[srcCount];
				for (int f = 0; f < srcCount; ++f) {
					srcFields[f] = srcExps[f].getFieldName();
				}
				
				srcFieldsList.add(srcFields);
				refFieldList.add(curNames[0]);
			}
		}
		
		this.newNames = newNames;
	}
	
	/**
	 * ȡ�����Ƿ�����Ԫ������������˺�������ټ�¼
	 * �˺��������α�ľ�ȷȡ����������ӵĲ�������ʹ��¼��������ֻ�谴���������ȡ������
	 * @return true���ᣬfalse������
	 */
	public boolean isDecrease() {
		return isIsect;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression [][]exps1 = dupExpressions(exps, ctx);
		Expression [][]dataExps1 = dupExpressions(dataExps, ctx);
		Expression [][]newExps1 = dupExpressions(newExps, ctx);
				
		return new JoinRemote(function, fname, exps1, codes, dataExps1, newExps1, newNames, opt);
	}

	private void init(Sequence data, Context ctx) {
		if (newDs != null) {
			return;
		}
		
		Sequence seq = new Sequence();
		String []oldKey = null;
		if (isOrg) {
			seq.add(fname);
		} else {
			oldDs = data.dataStruct();
			if (oldDs == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needPurePmt"));
			}
			
			oldKey = oldDs.getPrimary();
			seq.addAll(oldDs.getFieldNames());
		}

		for (int i = 0; i < newNames.length; ++i) {
			seq.addAll(newNames[i]);
		}

		String []names = new String[seq.length()];
		seq.toArray(names);
		newDs = new DataStruct(names);
		if (oldKey != null) {
			newDs.setPrimary(oldKey);
		}

		int count = datas.length;
		codes = new Sequence[count];
		cts = new ClusterMemoryTable[count];
		indexTables = new IndexTable[count];
		
		for (int i = 0; i < count; ++i) {
			if (datas[i] instanceof Sequence) {
				codes[i] = (Sequence)datas[i];
				if (codes[i] == null || codes[i].length() == 0) {
					codes[i] = null;
					containNull = true;
				}
				
				Expression []curExps = dataExps[i];
				IndexTable indexTable;
				if (curExps == null) {
					indexTable = codes[i].getIndexTable();
					if (indexTable == null) {
						Object obj = codes[i].getMem(1);
						if (!(obj instanceof Record)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException("join: " + mm.getMessage("engine.needPmt"));
						}

						String[] pks = ((Record)obj).dataStruct().getPrimary();
						if (pks == null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("ds.lessKey"));
						}
						
						int pkCount = pks.length;
						if (exps[i].length != pkCount) {
							MessageManager mm = EngineMessage.get();
							throw new RQException("join" + mm.getMessage("function.invalidParam"));
						}

						if (pkCount > 1) {
							curExps = new Expression[pkCount];
							dataExps[i] = curExps;
							for (int k = 0; k < pkCount; ++k) {
								curExps[k] = new Expression(ctx, pks[k]);
							}
						}

						indexTable = IndexTable.instance(codes[i], curExps, ctx);
					}
				} else {
					int fcount = exps[i].length;
					if (fcount != curExps.length) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}

					// ���������#����������������
					if (fcount != 1 || !(curExps[0].getHome() instanceof CurrentSeq)) {
						indexTable = codes[i].getIndexTable(curExps, ctx);
						if (indexTable == null) {
							indexTable = IndexTable.instance(codes[i], curExps, ctx);
						}
					} else {
						indexTable = null;
					}
				}

				indexTables[i] = indexTable;
			} else if (datas[i] instanceof ClusterMemoryTable) {
				cts[i] = (ClusterMemoryTable)datas[i];
			} else {
				containNull = true;
				//MessageManager mm = EngineMessage.get();
				//throw new RQException("join" + mm.getMessage("function.paramTypeError"));
			}
		}
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		init(seq, ctx);
		if (isIsect) {
			return join_i(seq, ctx);
		} else {
			return join(seq, ctx);
		}
	}
	
	private Sequence calc(Sequence src, Expression []exps, Context ctx) {
		if (exps == null || exps.length == 0) {
			return src;
		} else if (exps.length == 1) {
			return src.calc(exps[0], ctx);
		} else {
			int keyCount = exps.length;
			int size = src.length();
			Sequence result = new Sequence(size);

			ComputeStack stack = ctx.getComputeStack();
			Sequence.Current current = src.new Current();
			stack.push(current);

			try {
				for (int i = 1; i <= size; ++i) {
					current.setCurrent(i);
					Object []keys = new Object[keyCount];
					result.add(keys);

					for (int k = 0; k < keyCount; ++k) {
						keys[k] = exps[k].calculate(ctx);
					}
				}
			} finally {
				stack.pop();
			}

			return result;
		}
	}

	private Sequence join(Sequence data, Context ctx) {
		int len = data.length();
		Table result = new Table(newDs, len);
		
		int findex;
		if (isOrg) {
			findex = 1;
			for (int i = 1; i <= len; ++i) {
				Record old = (Record)data.getMem(i);
				result.newLast().setNormalFieldValue(0, old);
			}

			for (int fk = 0, fkCount = exps.length; fk < fkCount; ++fk) {
				Sequence newSeq ;
				if (cts[fk] != null) {
					Sequence keyValues = calc(data, exps[fk], ctx);
					newSeq = cts[fk].getRows(keyValues, newExpStrs[fk], newNames[fk], ctx);
				} else if (indexTables[fk] != null) {
					newSeq = fetch(data, exps[fk], indexTables[fk], newExps[fk], newNames[fk], ctx);
				} else if (codes[fk] != null) {
					newSeq = fetch(data, exps[fk], codes[fk], newExps[fk], newNames[fk], ctx);
				} else {
					continue;
				}
				
				ListBase1 newMems = newSeq.getMems();
				for (int i = 1; i <= len; ++i) {
					Record nr = (Record)newMems.get(i);
					if (nr != null) {
						Record r = (Record)result.getMem(i);
						r.setStart(findex, nr);
					}
				}

				findex += newExps[fk].length;
			}
		} else {
			findex = oldDs.getFieldCount();
			for (int i = 1; i <= len; ++i) {
				Record old = (Record)data.getMem(i);
				result.newLast(old.getFieldValues());
			}

			for (int fk = 0, fkCount = exps.length; fk < fkCount; ++fk) {
				Sequence newSeq ;
				if (cts[fk] != null) {
					Sequence keyValues = calc(result, exps[fk], ctx);
					newSeq = cts[fk].getRows(keyValues, newExpStrs[fk], newNames[fk], ctx);
				} else if (indexTables[fk] != null) {
					newSeq = fetch(result, exps[fk], indexTables[fk], newExps[fk], newNames[fk], ctx);
				} else if (codes[fk] != null) {
					newSeq = fetch(result, exps[fk], codes[fk], newExps[fk], newNames[fk], ctx);
				} else {
					continue;
				}
				
				ListBase1 newMems = newSeq.getMems();
				for (int i = 1; i <= len; ++i) {
					Record nr = (Record)newMems.get(i);
					if (nr != null) {
						Record r = (Record)result.getMem(i);
						r.setStart(findex, nr);
					}
				}

				findex += newExps[fk].length;
			}
		}

		return result;
	}

	private Sequence fetch(Sequence src, Expression[] exps, IndexTable it, 
			Expression[] newExps, String[] newNames, Context ctx) {
		int pkCount = exps.length;
		int newCount = newExps.length;
		Object []pkValues = new Object[pkCount];
		DataStruct ds = new DataStruct(newNames);
		int len = src.length();
		Sequence result = new Sequence(len);
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = src.new Current();
		stack.push(current);
		try {
			for (int i = 1; i <= len; ++i) {
				current.setCurrent(i);
				for (int f = 0; f < pkCount; ++f) {
					pkValues[f] = exps[f].calculate(ctx);
				}
	
				Record r = (Record)it.find(pkValues);
				if (r != null) {
					stack.push(r);
					Record nr = new Record(ds);
					result.add(nr);
					try {
						for (int f = 0; f < newCount; ++f) {
							nr.setNormalFieldValue(f, newExps[f].calculate(ctx));
						}
					} finally {
						stack.pop();
					}
				} else {
					result.add(null);
				}
			}
		} finally {
			stack.pop();
		}
		
		return result;
	}
	
	private Sequence fetch(Sequence src, Expression[] exps, Sequence code, 
			Expression[] newExps, String[] newNames, Context ctx) {
		int newCount = newExps.length;
		DataStruct ds = new DataStruct(newNames);
		int len = src.length();
		Sequence result = new Sequence(len);
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = src.new Current();
		stack.push(current);
		Sequence.Current codeCurrent = code.new Current();
		stack.push(codeCurrent);
		
		try {
			Expression exp = exps[0];
			ListBase1 codeMems = code.getMems();
			int codeLen = codeMems.size();
			for (int i = 1; i <= len; ++i) {
				current.setCurrent(i);
				Object val = exp.calculate(ctx);
				if (val instanceof Number) {
					int seq = ((Number)val).intValue();
					if (seq > 0 && seq <= codeLen) {
						codeCurrent.setCurrent(seq);
						Record nr = new Record(ds);
						result.add(nr);
						for (int f = 0; f < newCount; ++f) {
							nr.setNormalFieldValue(f, newExps[f].calculate(ctx));
						}
					} else {
						result.add(null);
					}
				} else {
					result.add(null);
				}
			}				
		} finally {
			stack.pop();
			stack.pop();
		}
		
		return result;
	}

	private Table join_i(Sequence data, Context ctx) {
		if (containNull) return null;
		
		Sequence newSeq;
		if (cts[0] != null) {
			Sequence keyValues = calc(data, exps[0], ctx);
			newSeq = cts[0].getRows(keyValues, newExpStrs[0], newNames[0], ctx);
		} else if (indexTables[0] != null) {
			newSeq = fetch(data, exps[0], indexTables[0], newExps[0], newNames[0], ctx);
		} else {
			newSeq = fetch(data, exps[0], codes[0], newExps[0], newNames[0], ctx);
		}
		
		ListBase1 newMems = newSeq.getMems();
		int findex = oldDs.getFieldCount();
		int len = data.length();
		Table result = new Table(newDs, len);
		
		for (int i = 1; i <= len; ++i) {
			Record nr = (Record)newMems.get(i);
			if (nr != null) {
				Record old = (Record)data.getMem(i);
				Record r = result.newLast(old.getFieldValues());
				r.setStart(findex, nr);
			}
		}
		
		findex += newExps[0].length;
		for (int fk = 1, fkCount = exps.length; fk < fkCount; ++fk) {
			len = result.length();
			if (len == 0) break;

			ListBase1 tmpMems = new ListBase1(len);
			if (cts[fk] != null) {
				Sequence keyValues = calc(result, exps[fk], ctx);
				newSeq = cts[fk].getRows(keyValues, newExpStrs[fk], newNames[fk], ctx);
			} else if (indexTables[fk] != null) {
				newSeq = fetch(result, exps[fk], indexTables[fk], newExps[fk], newNames[fk], ctx);
			} else {
				newSeq = fetch(result, exps[fk], codes[fk], newExps[fk], newNames[fk], ctx);
			}

			newMems = newSeq.getMems();
			for (int i = 1; i <= len; ++i) {
				Record nr = (Record)newMems.get(i);
				if (nr != null) {
					Record r = (Record)result.getMem(i);
					r.setStart(findex, nr);
					tmpMems.add(r);
				}
			}

			result.setMems(tmpMems);
			findex += newExps[fk].length;
		}

		if (result.length() != 0) {
			return result;
		} else {
			return null;
		}
	}
}
