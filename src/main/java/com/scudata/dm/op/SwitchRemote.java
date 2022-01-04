package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.expression.CurrentSeq;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.parallel.ClusterMemoryTable;
import com.scudata.resources.EngineMessage;

/**
 * ��Զ��ά�������Ӳ������������С��αꡢ�ܵ������Ӳ���
 * @author WangXiaoJun
 *
 */
public class SwitchRemote extends Operation {
	private String []fkNames; // ����ֶ�������
	private Object[] datas; // ����ά����߼�Ⱥά������
	private Expression []exps; // ά����������ʽ����
	private String opt; // ѡ��
	
	private int []fkIndex; // ����ֶ���������
	private Sequence []codes;
	private IndexTable []indexTables; // ά���Ӧ������������
	private ClusterMemoryTable []cts; // ��Ⱥά������

	private boolean isIsect; // ��������
	private boolean isDiff; // ��������
	
	private boolean isLeft; // �����ӣ��Ҳ���F��Ӧֵʱ�����������ݽṹ���ɿ�ֵ���������⣩��¼��Ӧ
	private DataStruct []dataStructs; // isLeftΪtrueʱʹ�ã���Ӧά������ݽṹ
	private int []keySeqs; // ά�������ֶε����
	
	public SwitchRemote(String[] fkNames, Object[] datas, Expression[] exps, String opt) {
		this(null, fkNames, datas, exps, opt);
	}
	
	public SwitchRemote(Function function, String[] fkNames,Object[] datas, Expression[] exps, String opt) {
		super(function);
		this.fkNames = fkNames;
		this.datas = datas;
		this.exps = exps;
		this.opt = opt;

		if (opt != null) {
			if (opt.indexOf('i') != -1) {
				isIsect = true;
			} else if (opt.indexOf('d') != -1) {
				isDiff = true;
			} else if (opt.indexOf('1') != -1) {
				isLeft = true;
			}
		}
	}
	
	/**
	 * ���ش˲����Ƿ��ʹ��¼������
	 * @return true�����ʹ��¼�����٣�false���������
	 */
	public boolean isDecrease() {
		return isIsect || isDiff;
	}
	
	/**
	 * ���ƴ˲���
	 * @param ctx ����������
	 * @return ���Ƶ�Switch����
	 */
	public Operation duplicate(Context ctx) {
		Expression []newExps = dupExpressions(exps, ctx);				
		return new SwitchRemote(function, fkNames, datas, newExps, opt);
	}

	private void init(Context ctx) {
		if (indexTables != null) {
			return;
		}
		
		int count = datas.length;
		codes = new Sequence[count];
		cts = new ClusterMemoryTable[count];
		indexTables = new IndexTable[count];
		
		if (isLeft) {
			dataStructs = new DataStruct[count];
			keySeqs = new int[count];
		}
		
		for (int i = 0; i < count; ++i) {
			if (datas[i] instanceof Sequence) {
				codes[i] = (Sequence)datas[i];
				Expression exp = null;
				if (exps != null && exps.length > i) {
					exp = exps[i];
				}
				
				if (exp == null || !(exp.getHome() instanceof CurrentSeq)) { // #
					indexTables[i] = codes[i].getIndexTable(exp, ctx);
					if (indexTables[i] == null) {
						indexTables[i] = IndexTable.instance(codes[i], exp, ctx);
					}
				}
				
				if (isLeft) {
					dataStructs[i] = codes[i].dataStruct();
					if (dataStructs[i] == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPurePmt"));
					}
					
					int key = -1;
					if (exp != null) {
						key = dataStructs[i].getFieldIndex(exp.getIdentifierName());
					}
					
					if (key == -1) {
						int []pks = dataStructs[i].getPKIndex();
						if (pks != null && pks.length == 1) {
							key = pks[0];
						}
					}
					
					if (key != -1) {
						keySeqs[i] = key;
					}
				}
			} else if (datas[i] instanceof ClusterMemoryTable) {
				cts[i] = (ClusterMemoryTable)datas[i];
			} else if (datas[i] != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("switch" + mm.getMessage("function.paramTypeError"));
			}
		}
	}

	/**
	 * �Դ�������������ӣ��������ӽ��
	 * @param seq Ҫ���������
	 * @param ctx ����������
	 * @return ���ӽ��
	 */
	public Sequence process(Sequence data, Context ctx) {
		init(ctx);

		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		int fkCount = fkNames.length;
		if (fkIndex == null) {
			fkIndex = new int[fkCount];
			for (int f = 0; f < fkCount; ++f) {
				fkIndex[f] = ds.getFieldIndex(fkNames[f]);
				if (fkIndex[f] == -1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fkNames[f] + mm.getMessage("ds.fieldNotExist"));
				}
			}
		}

		int srcLen = data.length();
		Sequence result = new Sequence();
		result.setMems(data.getMems());
		
		for (int fk = 0; fk < fkCount; ++fk) {
			int len = result.length();
			int f = fkIndex[fk];
			Sequence newSeq ;
			if (cts[fk] != null) {
				Sequence keyValues = result.fieldValues(f);
				newSeq = cts[fk].getRows(keyValues, null, null, ctx);
			} else if (indexTables[fk] != null) {
				newSeq = fetch(result, f, indexTables[fk], ctx);
			} else if (codes[fk] != null) {
				newSeq = fetch(result, f, codes[fk], ctx);
			} else {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)result.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Record) {
						r.setNormalFieldValue(fk, ((Record)val).getPKValue());
					}
				}
				
				continue;
			}

			if (isIsect) {
				ListBase1 resultMems = new ListBase1(len);
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)result.getMem(i);
					Object val = newSeq.getMem(i);
					if (val != null) {
						r.setNormalFieldValue(fk, val);
						resultMems.add(r);
					}
				}
				
				result.setMems(resultMems);
			} else if (isDiff) {
				ListBase1 resultMems = new ListBase1(len);
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)result.getMem(i);
					Object val = newSeq.getMem(i);
					
					// �Ҳ���ʱ����Դֵ
					if (val == null) {
						resultMems.add(r);
					}
				}
				
				result.setMems(resultMems);
			} else {
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)result.getMem(i);
					Object val = newSeq.getMem(i);
					r.setNormalFieldValue(f, val);
				}
			}
		}
		
		if (result.length() == 0) {
			return null;
		} else {
			if (srcLen != result.length()) {
				data.setMems(result.getMems());
			}
			
			return data;
		}
	}
	
	// ������ȡԪ��
	private Sequence fetch(Sequence src, int f, IndexTable it, Context ctx) {
		int len = src.length();
		Sequence result = new Sequence(len);
		
		if (isLeft) {
			DataStruct ds = dataStructs[f];
			int keySeq = keySeqs[f];
			for (int i = 1; i <= len; ++i) {
				Record r = (Record)src.getMem(i);
				Object val = r.getNormalFieldValue(f);
				val = it.find(val);
				if (val != null) {
					result.add(val);
				} else {
					Record record = new Record(ds);
					record.setNormalFieldValue(keySeq, val);
					result.add(record);
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				Record r = (Record)src.getMem(i);
				Object val = r.getNormalFieldValue(f);
				val = it.find(val);
				result.add(val);
			}
		}
		
		return result;
	}
	
	// �����ȡԪ��
	private Sequence fetch(Sequence src, int f, Sequence code, Context ctx) {
		int len = src.length();
		Sequence result = new Sequence(len);
		ListBase1 codeMems = code.getMems();
		int codeLen = codeMems.size();
		
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)src.getMem(i);
			Object val = r.getNormalFieldValue(f);
			if (val instanceof Number) {
				int seq = ((Number)val).intValue();
				if (seq > 0 && seq <= codeLen) {
					Object obj = codeMems.get(seq);
					result.add(obj);
				} else {
					result.add(null);
				}
			} else {
				result.add(null);
			}
		}				
		
		return result;
	}
}
