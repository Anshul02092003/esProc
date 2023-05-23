package com.scudata.dm.op;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���α��ܵ�����bind����
 * @author WangXiaoJun
 *
 */
public class ForeignJoin extends Operation {
	private Expression []dimExps; // �����ֶα��ʽ����
	private String []aliasNames; // ����
	private Expression [][]newExps; // ȡ���Ĵ������ֶα��ʽ����
	private String [][]newNames; // ȡ���Ĵ������ֶ�������
	private String opt; // ѡ��
	
	private DataStruct oldDs; // Դ�����ݽṹ
	private DataStruct newDs; // ��������ݽṹ
	private int [][]tgtIndexs; // newExps�ֶ��ڽ������λ��
	private boolean isIsect; // �����ӣ�Ĭ��Ϊ������
	
	public ForeignJoin(Expression[] dimExps, String []aliasNames, Expression[][] newExps, String[][] newNames, String opt) {
		this(null, dimExps, aliasNames, newExps, newNames, opt);
	}

	public ForeignJoin(Function function, Expression[] dimExps, String []aliasNames, 
			Expression[][] newExps, String[][] newNames, String opt) {
		super(function);
		this.dimExps = dimExps;
		this.aliasNames = aliasNames;
		this.newExps = newExps;
		this.opt = opt;
		
		int count = newExps.length;
		if (newNames == null) {
			newNames = new String[count][];
		}
		
		this.newNames = newNames;
		isIsect = opt != null && opt.indexOf('i') != -1;
		
		for (int i = 0; i < count; ++i) {
			Expression []curExps = newExps[i];
			if (curExps == null) {
				continue;
			}
			
			int curLen = curExps.length;
			if (newNames[i] == null) {
				newNames[i] = new String[curLen];
			}
			
			String []curNames = newNames[i];
			for (int j = 0; j < curLen; ++j) {
				if (curNames[j] == null || curNames[j].length() == 0) {
					curNames[j] = curExps[j].getFieldName();
				}
			}
		}
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
		Expression []dimExps1 = dupExpressions(dimExps, ctx);
		Expression [][]newExps1 = dupExpressions(newExps, ctx);
		return new ForeignJoin(function, dimExps1, aliasNames, newExps1, newNames, opt);
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
		
		Sequence seq = new Sequence();
		String []oldKey = oldDs.getPrimary();
		seq.addAll(oldDs.getFieldNames());
		int dcount = newNames.length;
		tgtIndexs = new int[dcount][];
		
		for (int i = 0; i < dcount; ++i) {
			String []curNames = newNames[i];
			if (curNames == null) {
				continue;
			}
			
			int curLen = curNames.length;
			int []tmp = new int[curLen];
			tgtIndexs[i] = tmp;
			
			for (int f = 0; f < curLen; ++f) {
				// ����¼ӵ��ֶ���Դ�����Ѵ������д�����ֶ�
				int index = oldDs.getFieldIndex(curNames[f]);
				if (index == -1) {
					tmp[f] = seq.length();
					seq.add(curNames[f]);
				} else {
					tmp[f] = index;
				}
			}
		}

		String []names = new String[seq.length()];
		seq.toArray(names);
		newDs = new DataStruct(names);
		if (oldKey != null) {
			newDs.setPrimary(oldKey);
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
	
	private Sequence join(Sequence data, Context ctx) {
		int len = data.length();
		Table result = new Table(newDs, len);
		ComputeStack stack = ctx.getComputeStack();
		
		// �������C����ǰ���F������һ����join���
		for (int i = 1; i <= len; ++i) {
			BaseRecord old = (BaseRecord)data.getMem(i);
			result.newLast(old.getFieldValues());
		}
		
		Current current = new Current(result);
		stack.push(current);

		try {
			for (int fk = 0, fkCount = dimExps.length; fk < fkCount; ++fk) {
				Expression dimExp = dimExps[fk];
				int []tgtIndexs = this.tgtIndexs[fk];
				
				if (tgtIndexs == null) {
					for (int i = 1; i <= len; ++i) {
						current.setCurrent(i);
						dimExp.calculate(ctx);
					}
				} else {
					Sequence dimData = new Sequence(len);
					IArray mems = dimData.getMems();
					
					for (int i = 1; i <= len; ++i) {
						current.setCurrent(i);
						mems.push(dimExp.calculate(ctx));
					}
					
					Param param = null;
					Object oldValue = null;
					if (aliasNames != null && aliasNames[fk] != null) {
						param = ctx.getParam(aliasNames[fk]);
						if (param == null) {
							param = new Param(aliasNames[fk], Param.VAR, dimData);
							ctx.addParam(param);
						} else {
							oldValue = param.getValue();
							param.setValue(dimData);
						}
					}
					
					Expression []curNewExps = newExps[fk];
					int newCount = curNewExps.length;
					
					try {
						Current dimCurrent = new Current(dimData);
						stack.push(dimCurrent);
						
						if (param == null) {
							for (int i = 1; i <= len; ++i) {
								current.setCurrent(i);
								dimCurrent.setCurrent(i);
								
								BaseRecord r = (BaseRecord)result.getMem(i);
								for (int f = 0; f < newCount; ++f) {
									r.setNormalFieldValue(tgtIndexs[f], curNewExps[f].calculate(ctx));
								}
							}
						} else {
							for (int i = 1; i <= len; ++i) {
								current.setCurrent(i);
								dimCurrent.setCurrent(i);
								param.setValue(mems.get(i));
								
								BaseRecord r = (BaseRecord)result.getMem(i);
								for (int f = 0; f < newCount; ++f) {
									r.setNormalFieldValue(tgtIndexs[f], curNewExps[f].calculate(ctx));
								}
							}
						}
					} finally {
						stack.pop();
						if (param != null) {
							param.setValue(oldValue);
						}
					}
				}
			}
		} finally {
			stack.pop();
		}

		return result;
	}
	
	private Table join_i(Sequence data, Context ctx) {
		int len = data.length();
		Table result = new Table(newDs, len);
		boolean []signs = new boolean[len + 1];
		for (int i = 1; i <= len; ++i) {
			signs[i] = true;
		}
		
		// �������C����ǰ���F������һ����join���
		for (int i = 1; i <= len; ++i) {
			BaseRecord old = (BaseRecord)data.getMem(i);
			result.newLast(old.getFieldValues());
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(result);
		stack.push(current);

		try {
			for (int fk = 0, fkCount = dimExps.length; fk < fkCount; ++fk) {
				Expression dimExp = dimExps[fk];
				int []tgtIndexs = this.tgtIndexs[fk];
				
				if (tgtIndexs == null) {
					for (int i = 1; i <= len; ++i) {
						if (signs[i]) {
							current.setCurrent(i);
							Object dr = dimExp.calculate(ctx);
							if (Variant.isFalse(dr)) {
								signs[i] = false;
							}
						}
					}
				} else {
					Sequence dimData = new Sequence(len);
					IArray mems = dimData.getMems();
					
					for (int i = 1; i <= len; ++i) {
						if (signs[i]) {
							current.setCurrent(i);
							Object dr = dimExp.calculate(ctx);
							mems.push(dr);
							
							if (Variant.isFalse(dr)) {
								signs[i] = false;
							}
						} else {
							mems.pushNull();
						}
					}
					
					Param param = null;
					Object oldValue = null;
					if (aliasNames != null && aliasNames[fk] != null) {
						param = ctx.getParam(aliasNames[fk]);
						if (param == null) {
							param = new Param(aliasNames[fk], Param.VAR, dimData);
							ctx.addParam(param);
						} else {
							oldValue = param.getValue();
							param.setValue(dimData);
						}
					}
					
					Expression []curNewExps = newExps[fk];
					int newCount = curNewExps.length;
					
					try {
						Current dimCurrent = new Current(dimData);
						stack.push(dimCurrent);
						
						if (param == null) {
							for (int i = 1; i <= len; ++i) {
								if (signs[i]) {
									current.setCurrent(i);
									dimCurrent.setCurrent(i);
									
									BaseRecord r = (BaseRecord)result.getMem(i);
									for (int f = 0; f < newCount; ++f) {
										r.setNormalFieldValue(tgtIndexs[f], curNewExps[f].calculate(ctx));
									}
								}
							}
						} else {
							for (int i = 1; i <= len; ++i) {
								if (signs[i]) {
									current.setCurrent(i);
									dimCurrent.setCurrent(i);
									param.setValue(mems.get(i));
									
									BaseRecord r = (BaseRecord)result.getMem(i);
									for (int f = 0; f < newCount; ++f) {
										r.setNormalFieldValue(tgtIndexs[f], curNewExps[f].calculate(ctx));
									}
								}
							}
						}
					} finally {
						stack.pop();
						if (param != null) {
							param.setValue(oldValue);
						}
					}
				}
			}
		} finally {
			stack.pop();
		}

		IArray mems = result.getMems();
		BoolArray signArray = new BoolArray(signs, len);
		mems = mems.select(signArray);
		result.setMems(mems);
		return result;
	}
}
