package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.Sequence;
import com.scudata.expression.CurrentSeq;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����α��ܵ���join�ӳټ��㺯�������˵��������ϵļ�¼
 * @author RunQian
 *
 */
public class FilterJoin extends Operation {
	private Expression [][]exps; // �����ֶα��ʽ����
	private Sequence []codes; // ���������
	private Expression [][]dataExps; // ������������ʽ����
	private String opt; // ѡ��
	
	private IndexTable []indexTables; // �����hashֵ����
	private boolean containNull; // �е�ά���Ƿ��ǿ�
	private boolean isMerge; // �Ƿ�ʹ�ù鲢�����й��������б������ֶ�����
	
	public FilterJoin(Expression[][] exps, Sequence[] codes, Expression[][] dataExps) {
		this(null, exps, codes, dataExps, null);
	}

	public FilterJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps, String opt) {
		super(function);
		this.exps = exps;
		this.codes = codes;
		this.dataExps = dataExps;
		this.opt = opt;
		
		if (opt != null && opt.indexOf('m') != -1) {
			isMerge = true;
		}
		
		int count = exps.length;
		for (int i = 0; i < count; ++i) {
			if (codes[i] == null) {
				containNull = true;
			} else if (codes[i].length() == 0 && codes[i].getIndexTable() == null) {
				codes[i] = null;
				containNull = true;
			}
		}
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
				
		return new FilterJoin(function, exps1, codes, dataExps1, opt);
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
				continue;
			}

			Expression []curExps = dataExps[i];
			IndexTable indexTable;
			
			if (isMerge) {
				if (curExps == null) {
					Object obj = code.getMem(1);
					if (obj instanceof BaseRecord) {
						DataStruct ds = ((BaseRecord)obj).dataStruct();
						String[] pks = ds.getPrimary();
						if (pks == null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("ds.lessKey"));
						}
						
						int pkCount = pks.length;
						if (exps[i].length != pkCount) {
							MessageManager mm = EngineMessage.get();
							throw new RQException("join" + mm.getMessage("function.invalidParam"));
						}

						curExps = new Expression[pkCount];
						dataExps[i] = curExps;
						for (int k = 0; k < pkCount; ++k) {
							curExps[k] = new Expression(ctx, pks[k]);
						}
						
						indexTable = code.newMergeIndexTable(curExps, ctx);
					} else {
						indexTable = code.newMergeIndexTable(null, ctx);
					}
				} else {
					if (exps[i].length != curExps.length) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
					
					indexTable = code.newMergeIndexTable(curExps, ctx);
				}
			} else if (curExps == null) {
				indexTable = code.getIndexTable();
				if (indexTable == null) {
					Object obj = code.getMem(1);
					if (obj instanceof BaseRecord) {
						DataStruct ds = ((BaseRecord)obj).dataStruct();
						String[] pks = ds.getPrimary();
						if (pks == null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("ds.lessKey"));
						}
						
						int pkCount = pks.length;
						if (ds.getTimeKeyCount() == 0 && exps[i].length != pkCount) {
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

					indexTable = code.newIndexTable(curExps, ctx);
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
						indexTable = code.newIndexTable(curExps, ctx);
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
		if (containNull) {
			return null;
		}
		
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
		
		Current current = new Current(data);
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
						if (r == null) continue Next;
					} else {
						Object val = exps[fk][0].calculate(ctx);
						if (val instanceof Number) {
							int seq = ((Number)val).intValue();
							if (seq < 1 || seq > codes[fk].length() || Variant.isFalse(codes[fk].getMem(seq))) {
								continue Next;
							}
						} else {
							continue Next;
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
