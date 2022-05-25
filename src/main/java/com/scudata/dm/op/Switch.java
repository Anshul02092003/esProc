package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dw.IColumnCursorUtil;
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

	private IndexTable []indexTables; // ά���Ӧ������������
	private boolean isIsect; // ��������
	private boolean isDiff; // ��������
	
	private boolean isLeft; // �����ӣ��Ҳ���F��Ӧֵʱ�����������ݽṹ���ɿ�ֵ���������⣩��¼��Ӧ
	private DataStruct []dataStructs; // isLeftΪtrueʱʹ�ã���Ӧά������ݽṹ
	private int []keySeqs; // ά�������ֶε����
	
	public String[] getFkNames() {
		return fkNames;
	}

	public String [] getTimeFkNames() {
		return timeFkNames;
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
	
	public boolean isDiff() {
		return isDiff;
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
		return new Switch(function, fkNames, getTimeFkNames(), codes, exps, timeExps, opt);
	}

	public IndexTable getIndexTable(int index, Context ctx) {
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
		if (seq.isColumnTable()) {
			return switch_column(seq, ctx);
		}
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
		
	private void switch1(Sequence data, Context ctx) {
		int fkCount = fkNames.length;
		ListBase1 mems = data.getMems();
		int len = mems.size();
		
		for (int f = 0; f < fkCount; ++f) {
			IndexTable indexTable = getIndexTable(f, ctx);
			String fkName = fkNames[f];
			String timeName = getTimeFkNames() == null ? null : getTimeFkNames()[f];
			int col = -1; // �ֶ�����һ����¼������
			int timeCol = -1;
			Record prevRecord = null; // ��һ����¼

			if (indexTable != null) {
				if (isLeft) {
					DataStruct ds = dataStructs[f];
					int keySeq = keySeqs[f];
					if (timeName == null) {
						for (int i = 1; i <= len; ++i) {
							Object obj = mems.get(i);
							if (obj instanceof Record) {
								Record cur = (Record)obj;
								if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
									col = cur.getFieldIndex(fkName);
									if (col < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
									}

									prevRecord = cur;							
								}
								
								Object key = cur.getNormalFieldValue(col);
								Object p = indexTable.find(key);
								if (p != null) {
									cur.setNormalFieldValue(col, p);
								} else {
									Record record = new Record(ds);
									record.setNormalFieldValue(keySeq, key);
									cur.setNormalFieldValue(col, record);
								}
							} else if (obj != null) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(mm.getMessage("engine.needPmt"));
							}
						}
					} else {
						// ��ʱ����¼�ʱ����ʱ�������ֶβ���
						Object []values = new Object[2];
						for (int i = 1; i <= len; ++i) {
							Object obj = mems.get(i);
							if (obj instanceof Record) {
								Record cur = (Record)obj;
								if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
									col = cur.getFieldIndex(fkName);
									if (col < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
									}
									
									timeCol = cur.getFieldIndex(timeName);
									if (timeCol < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(timeName + mm.getMessage("ds.fieldNotExist"));
									}

									prevRecord = cur;							
								}
								
								values[0] = cur.getNormalFieldValue(col);
								values[1] = cur.getNormalFieldValue(timeCol);								
								Object p = indexTable.find(values);
								
								if (p != null) {
									cur.setNormalFieldValue(col, p);
								} else {
									Record record = new Record(ds);
									record.setNormalFieldValue(keySeq, values[0]);
									cur.setNormalFieldValue(col, record);
								}
							} else if (obj != null) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(mm.getMessage("engine.needPmt"));
							}
						}
					}
				} else {
					if (timeName == null) {
						for (int i = 1; i <= len; ++i) {
							Object obj = mems.get(i);
							if (obj instanceof Record) {
								Record cur = (Record)obj;
								if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
									col = cur.getFieldIndex(fkName);
									if (col < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
									}

									prevRecord = cur;							
								}
								
								Object key = cur.getNormalFieldValue(col);
								Object p = indexTable.find(key);
								cur.setNormalFieldValue(col, p);
							} else if (obj != null) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(mm.getMessage("engine.needPmt"));
							}
						}
					} else {
						// ��ʱ����¼�ʱ����ʱ�������ֶβ���
						Object []values = new Object[2];
						for (int i = 1; i <= len; ++i) {
							Object obj = mems.get(i);
							if (obj instanceof Record) {
								Record cur = (Record)obj;
								if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
									col = cur.getFieldIndex(fkName);
									if (col < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
									}
									
									timeCol = cur.getFieldIndex(timeName);
									if (timeCol < 0) {
										MessageManager mm = EngineMessage.get();
										throw new RQException(timeName + mm.getMessage("ds.fieldNotExist"));
									}

									prevRecord = cur;							
								}
								
								values[0] = cur.getNormalFieldValue(col);
								values[1] = cur.getNormalFieldValue(timeCol);
								Object p = indexTable.find(values);
								cur.setNormalFieldValue(col, p);
							} else if (obj != null) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(mm.getMessage("engine.needPmt"));
							}
						}
					}
				}
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Record) {
							cur.setNormalFieldValue(col, ((Record)val).getPKValue());
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
				}
			} else { // #
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Number) {
							int seq = ((Number)val).intValue();
							if (seq > 0 && seq <= codeLen) {
								cur.setNormalFieldValue(col, code.getMem(seq));
							} else {
								cur.setNormalFieldValue(col, null);
							}
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
				}
			}
		}
	}

	private void switch_i(Sequence data, Context ctx) {
		int fkCount = fkNames.length;
		ListBase1 mems = data.getMems();
		
		for (int f = 0; f < fkCount; ++f) {
			IndexTable indexTable = getIndexTable(f, ctx);
			int len = mems.size();

			String fkName = fkNames[f];
			String timeName = getTimeFkNames() == null ? null : getTimeFkNames()[f];
			int col = -1; // �ֶ�����һ����¼������
			int timeCol = -1;
			Record prevRecord = null; // ��һ����¼
			
			if (indexTable != null) {
				ListBase1 resultMems = new ListBase1(len);
				if (timeName == null) {
					for (int i = 1; i <= len; ++i) {
						Object obj = mems.get(i);
						if (obj instanceof Record) {
							Record cur = (Record)obj;
							if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
								col = cur.getFieldIndex(fkName);
								if (col < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
								}

								prevRecord = cur;							
							}
							
							Object key = cur.getNormalFieldValue(col);
							Object p = indexTable.find(key);
							if (p != null) {
								cur.setNormalFieldValue(col, p);
								resultMems.add(cur);
							}
						} else if (obj != null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needPmt"));
						}
					}
				} else {
					// ��ʱ����¼�ʱ����ʱ�������ֶβ��ң��������һ���ֶ���
					Object []values = new Object[2];
					for (int i = 1; i <= len; ++i) {
						Object obj = mems.get(i);
						if (obj instanceof Record) {
							Record cur = (Record)obj;
							if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
								col = cur.getFieldIndex(fkName);
								if (col < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
								}
								
								timeCol = cur.getFieldIndex(timeName);
								if (timeCol < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(timeName + mm.getMessage("ds.fieldNotExist"));
								}

								prevRecord = cur;							
							}
							
							values[0] = cur.getNormalFieldValue(col);
							values[1] = cur.getNormalFieldValue(timeCol);							
							Object p = indexTable.find(values);
							if (p != null) {
								cur.setNormalFieldValue(col, p);
								resultMems.add(cur);
							}
						} else if (obj != null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needPmt"));
						}
					}
				}
				
				mems = resultMems;
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Record) {
							cur.setNormalFieldValue(col, ((Record)val).getPKValue());
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
				}
			} else { // #
				ListBase1 resultMems = new ListBase1(len);
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Number) {
							int seq = ((Number)val).intValue();
							if (seq > 0 && seq <= codeLen) {
								cur.setNormalFieldValue(col, code.getMem(seq));
								resultMems.add(cur);
							}
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
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
		int fkCount = fkNames.length;
		ListBase1 mems = data.getMems();

		for (int f = 0; f < fkCount; ++f) {
			IndexTable indexTable = getIndexTable(f, ctx);
			int len = mems.size();
			
			String fkName = fkNames[f];
			String timeName = getTimeFkNames() == null ? null : getTimeFkNames()[f];
			int col = -1; // �ֶ�����һ����¼������
			int timeCol = -1;
			Record prevRecord = null; // ��һ����¼

			if (indexTable != null) {
				ListBase1 resultMems = new ListBase1(len);
				if (timeName == null) {
					for (int i = 1; i <= len; ++i) {
						Object obj = mems.get(i);
						if (obj instanceof Record) {
							Record cur = (Record)obj;
							if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
								col = cur.getFieldIndex(fkName);
								if (col < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
								}

								prevRecord = cur;							
							}
							
							Object key = cur.getNormalFieldValue(col);
							if (indexTable.find(key) == null) {
								resultMems.add(cur);
							}
						} else if (obj != null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needPmt"));
						}
					}
				} else {
					// ��ʱ����¼�ʱ����ʱ�������ֶβ���
					Object []values = new Object[2];
					for (int i = 1; i <= len; ++i) {
						Object obj = mems.get(i);
						if (obj instanceof Record) {
							Record cur = (Record)obj;
							if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
								col = cur.getFieldIndex(fkName);
								if (col < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
								}
								
								timeCol = cur.getFieldIndex(timeName);
								if (timeCol < 0) {
									MessageManager mm = EngineMessage.get();
									throw new RQException(timeName + mm.getMessage("ds.fieldNotExist"));
								}

								prevRecord = cur;							
							}
							
							values[0] = cur.getNormalFieldValue(col);
							values[1] = cur.getNormalFieldValue(timeCol);
							
							if (indexTable.find(values) == null) {
								resultMems.add(cur);
							}
						} else if (obj != null) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("engine.needPmt"));
						}
					}
				}

				mems = resultMems;
			} else if (codes[f] == null) {
				// ָ���ֶα��ֵ
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Record) {
							cur.setNormalFieldValue(col, ((Record)val).getPKValue());
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
				}
			} else { // #
				ListBase1 resultMems = new ListBase1(len);
				Sequence code = codes[f];
				int codeLen = code.length();
				for (int i = 1; i <= len; ++i) {
					Object obj = mems.get(i);
					if (obj instanceof Record) {
						Record cur = (Record)obj;
						if (prevRecord == null || !prevRecord.isSameDataStruct(cur)) {
							col = cur.getFieldIndex(fkName);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(fkName + mm.getMessage("ds.fieldNotExist"));
							}

							prevRecord = cur;							
						}
						
						Object val = cur.getNormalFieldValue(col);
						if (val instanceof Number) {
							int seq = ((Number)val).intValue();
							if (seq < 1 || seq > codeLen) {
								resultMems.add(cur);
							}
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
				}
	
				mems = resultMems;
			}
		}
		
		if (mems.size() != data.length()) {
			data.setMems(mems);
		}
	}
	
	private Sequence switch_column(Sequence data, Context ctx) {
		int fkCount = fkNames.length;
		Sequence result = data;
		for (int f = 0; f < fkCount; ++f) {
			IndexTable indexTable = getIndexTable(f, ctx);
			String timeName = getTimeFkNames() == null ? null : getTimeFkNames()[f];
			DataStruct ds = dataStructs == null ? null : dataStructs[f];
			int keySeq = keySeqs == null ? -1 : keySeqs[f];
			
			result = IColumnCursorUtil.util.switchColumnTable(result, isIsect, isDiff, fkNames[f], codes[f],
			timeName, indexTable, ds, keySeq, isLeft, ctx);
			
			if (result == null) return result;
		}
		return result;
	}
}
