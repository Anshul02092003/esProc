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
import com.scudata.resources.EngineMessage;

/**
 * ���Ӳ������������С��αꡢ�ܵ������Ӳ���
 * @author WangXiaoJun
 *
 */
public class Switch extends Operation {
	private String []fkNames; // ����ֶ�������
	private String []timeFkNames; // ʱ������ֶ�������
	
	private Sequence []codes; // ά������
	private Expression []exps; // ά����������ʽ����
	private Expression []timeExps; // ά���ʱ����¼�����
	private String opt; // ѡ��

	private int []fkIndex; // ����ֶ���������
	private int []timeFkIndex; // ʱ������ֶ���������
	private IndexTable []indexTables; // ά���Ӧ������������
	private boolean isIsect; // ��������
	private boolean isDiff; // ��������
	
	private boolean isLeft; // �����ӣ��Ҳ���F��Ӧֵʱ�����������ݽṹ���ɿ�ֵ���������⣩��¼��Ӧ
	private DataStruct []dataStructs; // isLeftΪtrueʱʹ�ã���Ӧά������ݽṹ
	private int []keySeqs; // ά�������ֶε����
	
	public String[] getFkNames() {
		return fkNames;
	}

	public Sequence[] getCodes() {
		return codes;
	}

	public Expression[] getExps() {
		return exps;
	}
	
	public boolean isIsect() {
		return isIsect;
	}

	public Switch(String[] fkNames, Sequence[] codes, Expression[] exps, String opt) {
		this(null, fkNames, codes, exps, opt);
	}
	
	public Switch(Function function, String[] fkNames, Sequence[] codes, Expression[] exps, String opt) {
		this(function, fkNames, null, codes, exps, null, opt);
	}
	
	/**
	 * ���캯��
	 * @param function �����ĺ�������
	 * @param fkNames ����ֶ�������
	 * @param timeFkNames ʱ�����������
	 * @param codes ά������
	 * @param exps ά����������
	 * @param timeExps ά���ʱ����¼�����
	 * @param opt ѡ��
	 */
	public Switch(Function function, String[] fkNames, String[] timeFkNames, Sequence[] codes, Expression[] exps, Expression[] timeExps, String opt) {
		super(function);
		this.fkNames = fkNames;
		this.timeFkNames = timeFkNames;
		this.codes = codes;
		this.exps = exps;
		this.timeExps = timeExps;
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
		Expression []exps = dupExpressions(this.exps, ctx);
		Expression []timeExps = dupExpressions(this.timeExps, ctx);
		return new Switch(function, fkNames, timeFkNames, codes, exps, timeExps, opt);
	}

	private IndexTable getIndexTable(int index, Context ctx) {
		if (indexTables == null) {
			int count = codes.length;
			indexTables = new IndexTable[count];
			
			if (isLeft) {
				dataStructs = new DataStruct[count];
				keySeqs = new int[count];
			}
			
			for (int i = 0; i < count; ++i) {
				Sequence code = codes[i];
				if (code == null) {
					continue;
				}
				
				Expression exp = null;
				if (exps != null && exps.length > i) {
					exp = exps[i];
				}

				Expression timeExp = null;
				if (timeExps != null && timeExps.length > i) {
					timeExp = timeExps[i];
				}
				
				if (timeExp != null) {
					Expression []curExps = new Expression[]{exp, timeExp};
					indexTables[i] = code.getIndexTable(curExps, ctx);
					if (indexTables[i] == null) {
						indexTables[i] = IndexTable.instance(code, curExps, ctx);
					}
				} else if (exp == null || !(exp.getHome() instanceof CurrentSeq)) { // #
					indexTables[i] = code.getIndexTable(exp, ctx);
					if (indexTables[i] == null) {
						indexTables[i] = IndexTable.instance(code, exp, ctx);
					}
				}
				
				if (isLeft) {
					dataStructs[i] = code.dataStruct();
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
			}
		}

		return indexTables[index];
	}

	/**
	 * �Դ�������������ӣ��������ӽ��
	 * @param seq Ҫ���������
	 * @param ctx ����������
	 * @return ���ӽ��
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (isIsect) {
			switch_i(seq, ctx);
			if (seq.length() == 0) {
				return null;
			}
		} else if (isDiff) {
			switch_d(seq, ctx);
			if (seq.length() == 0) {
				return null;
			}
		} else {
			switch1(seq, ctx);
		}
		
		return seq;
	}
	
	private void getFkIndex(Sequence data) {
		if (fkIndex == null) {
			DataStruct ds = data.dataStruct();
			if (ds == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needPurePmt"));
			}
	
			int fkCount = fkNames.length;
			fkIndex = new int[fkCount];
			timeFkIndex = new int[fkCount];
			for (int f = 0; f < fkCount; ++f) {
				fkIndex[f] = ds.getFieldIndex(fkNames[f]);
				if (fkIndex[f] == -1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fkNames[f] + mm.getMessage("ds.fieldNotExist"));
				}
				
				if (timeFkNames != null && timeFkNames[f] != null) {
					timeFkIndex[f] = ds.getFieldIndex(timeFkNames[f]);
					if (timeFkIndex[f] == -1) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(timeFkNames[f] + mm.getMessage("ds.fieldNotExist"));
					}
				} else {
					timeFkIndex[f] = -1;
				}
			}
		}
	}
	
	private void switch1(Sequence data, Context ctx) {
		getFkIndex(data);
		int fkCount = fkNames.length;
		int len = data.length();
		
		for (int f = 0; f < fkCount; ++f) {
			int fk = fkIndex[f];
			IndexTable indexTable = getIndexTable(f, ctx);

			if (indexTable != null) {
				int timeFk = timeFkIndex[f];
				if (isLeft) {
					DataStruct ds = dataStructs[f];
					int keySeq = keySeqs[f];
					if (timeFk == -1) {
						for (int i = 1; i <= len; ++i) {
							Record r = (Record)data.getMem(i);
							Object key = r.getNormalFieldValue(fk);
							Object obj = indexTable.find(key);
							if (obj != null) {
								r.setNormalFieldValue(fk, obj);
							} else {
								Record record = new Record(ds);
								record.setNormalFieldValue(keySeq, key);
								r.setNormalFieldValue(fk, record);
							}
						}
					} else {
						// ��ʱ����¼�ʱ����ʱ�������ֶβ���
						Object []values = new Object[2];
						for (int i = 1; i <= len; ++i) {
							Record r = (Record)data.getMem(i);
							values[0] = r.getNormalFieldValue(fk);
							values[1] = r.getNormalFieldValue(timeFk);
							
							Object obj = indexTable.find(values);
							if (obj != null) {
								r.setNormalFieldValue(fk, obj);
							} else {
								Record record = new Record(ds);
								record.setNormalFieldValue(keySeq, values[0]);
								r.setNormalFieldValue(fk, record);
							}
						}
					}
				} else {
					if (timeFk == -1) {
						for (int i = 1; i <= len; ++i) {
							Record r = (Record)data.getMem(i);
							Object key = r.getNormalFieldValue(fk);
							Object obj = indexTable.find(key);
							r.setNormalFieldValue(fk, obj);
						}
					} else {
						// ��ʱ����¼�ʱ����ʱ�������ֶβ���
						Object []values = new Object[2];
						for (int i = 1; i <= len; ++i) {
							Record r = (Record)data.getMem(i);
							values[0] = r.getNormalFieldValue(fk);
							values[1] = r.getNormalFieldValue(timeFk);

							Object obj = indexTable.find(values);
							r.setNormalFieldValue(fk, obj);
						}
					}
				}
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Record) {
						r.setNormalFieldValue(fk, ((Record)val).getPKValue());
					}
				}
			} else { // #
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Number) {
						int seq = ((Number)val).intValue();
						if (seq > 0 && seq <= codeLen) {
							r.setNormalFieldValue(fk, code.getMem(seq));
						} else {
							r.setNormalFieldValue(fk, null);
						}
					}
				}
			}
		}
	}

	private void switch_i(Sequence data, Context ctx) {
		getFkIndex(data);
		int fkCount = fkNames.length;
		ListBase1 mems = data.getMems();
		
		for (int f = 0; f < fkCount; ++f) {
			int fk = fkIndex[f];
			IndexTable indexTable = getIndexTable(f, ctx);
			int len = mems.size();

			if (indexTable != null) {
				ListBase1 resultMems = new ListBase1(len);
				int timeFk = timeFkIndex[f];
				
				if (timeFk == -1) {
					for (int i = 1; i <= len; ++i) {
						Record r = (Record)mems.get(i);
						Object key = r.getNormalFieldValue(fk);
						Object obj = indexTable.find(key);
						if (obj != null) {
							r.setNormalFieldValue(fk, obj);
							resultMems.add(r);
						}
					}
				} else {
					// ��ʱ����¼�ʱ����ʱ�������ֶβ��ң��������һ���ֶ���
					Object []values = new Object[2];
					for (int i = 1; i <= len; ++i) {
						Record r = (Record)mems.get(i);
						values[0] = r.getNormalFieldValue(fk);
						values[1] = r.getNormalFieldValue(timeFk);
						
						Object obj = indexTable.find(values);
						if (obj != null) {
							r.setNormalFieldValue(fk, obj);
							resultMems.add(r);
						}
					}
				}
				
				mems = resultMems;
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Record) {
						r.setNormalFieldValue(fk, ((Record)val).getPKValue());
					}
				}
			} else { // #
				ListBase1 resultMems = new ListBase1(len);
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Number) {
						int seq = ((Number)val).intValue();
						if (seq > 0 && seq <= codeLen) {
							r.setNormalFieldValue(fk, code.getMem(seq));
							resultMems.add(r);
						}
					}
				}
	
				mems = resultMems;
			}
		}
		
		if (mems.size() != data.length()) {
			data.setMems(mems);
		}
	}

	private void switch_d(Sequence data, Context ctx) {
		getFkIndex(data);
		int fkCount = fkNames.length;
		ListBase1 mems = data.getMems();

		for (int f = 0; f < fkCount; ++f) {
			int fk = fkIndex[f];
			IndexTable indexTable = getIndexTable(f, ctx);
			int len = mems.size();

			if (indexTable != null) {
				ListBase1 resultMems = new ListBase1(len);
				int timeFk = timeFkIndex[f];
				
				if (timeFk == -1) {
					for (int i = 1; i <= len; ++i) {
						Record r = (Record)mems.get(i);
						Object key = r.getNormalFieldValue(fk);
						Object obj = indexTable.find(key);
						if (obj == null) {
							resultMems.add(r);
						}
					}
				} else {
					// ��ʱ����¼�ʱ����ʱ�������ֶβ���
					Object []values = new Object[2];
					for (int i = 1; i <= len; ++i) {
						Record r = (Record)mems.get(i);
						values[0] = r.getNormalFieldValue(fk);
						values[1] = r.getNormalFieldValue(timeFk);
						
						Object obj = indexTable.find(values);
						if (obj == null) {
							resultMems.add(r);
						}
					}
				}

				mems = resultMems;
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Record) {
						r.setNormalFieldValue(fk, ((Record)val).getPKValue());
					}
				}
			} else { // #
				ListBase1 resultMems = new ListBase1(len);
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					Object val = r.getNormalFieldValue(fk);
					if (val instanceof Number) {
						int seq = ((Number)val).intValue();
						if (seq < 1 || seq > codeLen) {
							resultMems.add(r);
						}
					}
				}
	
				mems = resultMems;
			}
		}
		
		if (mems.size() != data.length()) {
			data.setMems(mems);
		}
	}
}
