package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.IndexTable;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.expression.CurrentSeq;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * �������α��ܵ��ϵĲ����ӹ��˲���
 * cs.join@d
 * @author RunQian
 *
 */
public class DiffJoin extends Operation {
	private Expression [][]exps; // �����ֶα��ʽ����
	private Sequence []codes; // ����������
	private Expression [][]dataExps; // ��������������ʽ����
	
	private IndexTable []indexTables; // �����hashֵ����
		
	public DiffJoin(Expression[][] exps, Sequence[] codes, Expression[][] dataExps) {
		this(null, exps, codes, dataExps);
	}

	public DiffJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps) {
		super(function);
		this.exps = exps;
		this.codes = codes;
		this.dataExps = dataExps;
	}
	
	/**
	 * ȡ�����Ƿ�����Ԫ������������˺�������ټ�¼
	 * �˺��������α�ľ�ȷȡ����������ӵĲ�������ʹ��¼��������ֻ�谴���������ȡ������
	 * @return true���ᣬfalse������
	 */
	public boolean isDecrease() {
		return true;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression [][]exps1 = dupExpressions(exps, ctx);
		Expression [][]dataExps1 = dupExpressions(dataExps, ctx);
				
		return new DiffJoin(function, exps1, codes, dataExps1);
	}

	private void init(Sequence data, Context ctx) {
		if (indexTables != null) {
			return;
		}
		
		int count = codes.length;
		indexTables = new IndexTable[count];

		for (int i = 0; i < count; ++i) {
			Sequence code = codes[i];
			if (code == null) {
				codes[i] = null;
				continue;
			} else if (code.length() == 0) {
				codes[i] = null;
				continue;
			}

			Expression []curExps = dataExps[i];
			IndexTable indexTable;
			if (curExps == null) {
				indexTable = code.getIndexTable();
				if (indexTable == null) {
					Object obj = code.getMem(1);
					if (obj instanceof Record) {
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
					}

					indexTable = IndexTable.instance(code, curExps, ctx);
				}
			} else {
				int fcount = exps[i].length;
				if (fcount != curExps.length) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("join" + mm.getMessage("function.invalidParam"));
				}

				// ���������#����������������
				if (fcount != 1 || !(curExps[0].getHome() instanceof CurrentSeq)) {
					indexTable = code.getIndexTable(curExps, ctx);
					if (indexTable == null) {
						indexTable = IndexTable.instance(code, curExps, ctx);
					}
				} else {
					indexTable = null;
				}
			}

			indexTables[i] = indexTable;
		}
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence data, Context ctx) {
		init(data, ctx);
		
		Expression [][]exps = this.exps;
		int fkCount = exps.length;

		int len = data.length();
		Sequence result = new Sequence(len);

		Object [][]pkValues = new Object[fkCount][];
		for (int fk = 0; fk < fkCount; ++fk) {
			pkValues[fk] = new Object[exps[fk].length];
		}

		IndexTable []indexTables = this.indexTables;
		Sequence []codes = this.codes;
		ComputeStack stack = ctx.getComputeStack();
		
		Sequence.Current current = data.new Current();
		stack.push(current);

		try {
			Next:
			for (int i = 1; i <= len; ++i) {
				current.setCurrent(i);
				for (int fk = 0; fk < fkCount; ++fk) {
					if (indexTables[fk] != null) {
						Expression []curExps = exps[fk];
						Object []curPkValues = pkValues[fk];
						for (int f = 0; f < curExps.length; ++f) {
							curPkValues[f] = curExps[f].calculate(ctx);
						}

						Object r = indexTables[fk].find(curPkValues);
						if (r != null) continue Next;
					} else if (codes[fk] != null) {
						Object val = exps[fk][0].calculate(ctx);
						if (val instanceof Number) {
							int seq = ((Number)val).intValue();
							if (seq >= 1 && seq <= codes[fk].length()) {
								continue Next;
							}
						}
					}
				}

				result.add(data.getMem(i));
			}
		} finally {
			stack.pop();
		}

		if (result.length() != 0) {
			return result;
		} else {
			return null;
		}
	}
}
