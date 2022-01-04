package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * �α��ܵ����ӳ������ֶδ�����
 * @author RunQian
 *
 */
public class Derive extends Operation  {
	private Expression[] exps; // �����ֶεı��ʽ����
	private String[] names; // �����ֶε��ֶ�������
	private String opt; // ѡ��
	
	private int oldColCount; // Դ�ֶ���
	private DataStruct newDs; // ��������ݽṹ
	private boolean containNull; // �Ƿ���null
	
	private int level = 0;
	
	public Derive(Expression []exps, String []names, String opt) {
		this(null, exps, names, opt, 0);
	}
	
	public Derive(Function function, Expression []exps, String []names, String opt, int level) {
		super(function);
		this.exps = exps;
		this.names = names;
		this.opt = opt;
		this.containNull = opt == null || opt.indexOf('i') == -1;
		this.level = level;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression []dupExps = dupExpressions(exps, ctx);
		return new Derive(function, dupExps, names, opt, level);
	}
	
	private DataStruct getNewDataStruct(Sequence seq) {
		if (newDs == null) {
			DataStruct ds = seq.dataStruct();
			if (ds == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needPurePmt"));
			}
			
			Expression[] exps = this.exps;
			int colCount = exps.length;
			for (int i = 0; i < colCount; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					if (exps[i] == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("derive" + mm.getMessage("function.invalidParam"));
					}
	
					names[i] = exps[i].getFieldName(ds);;
				} else {
					if (exps[i] == null) {
						exps[i] = Expression.NULL;
					}
				}
			}
			
			String []oldNames = ds.getFieldNames();
			oldColCount = oldNames.length;
			
			// �ϲ��ֶ�
			int newColCount = oldColCount + colCount;
			String []totalNames = new String[newColCount];
			System.arraycopy(oldNames, 0, totalNames, 0, oldColCount);
			System.arraycopy(names, 0, totalNames, oldColCount, colCount);
			newDs = ds.create(totalNames);
		}
		
		return newDs;
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (exps == null) {
			return seq.derive(opt);
		} else if (level > 1) {
			return seq.derive(names, exps, opt, ctx, level);
		}
		
		DataStruct newDs = getNewDataStruct(seq);
		int oldColCount = this.oldColCount;
		Expression[] exps = this.exps;
		int colCount = exps.length;
		
		ListBase1 mems = seq.getMems();
		int len = mems.size();
		Table table = new Table(newDs, len);

		ComputeStack stack = ctx.getComputeStack();
		Current newCurrent = table.new Current();
		stack.push(newCurrent);
		Current current = seq.new Current();
		stack.push(current);

		try {
			if (containNull) {
				for (int i = 1; i <= len; ++i) {
					Record or = (Record)mems.get(i);
					Record r = table.newLast(or.getFieldValues());
					
					newCurrent.setCurrent(i);
					current.setCurrent(i);

					// �������ֶ�
					for (int c = 0; c < colCount; ++c) {
						r.setNormalFieldValue(c + oldColCount, exps[c].calculate(ctx));
					}
				}
			} else {
				ListBase1 tMems = table.getMems();
				
				Next:
				for (int i = 1, q = 1; i <= len; ++i) {
					Record or = (Record)mems.get(i);
					Record r = table.newLast(or.getFieldValues());
					
					newCurrent.setCurrent(q);
					current.setCurrent(i);

					// �������ֶ�
					for (int c = 0; c < colCount; ++c) {
						Object obj = exps[c].calculate(ctx);
						if (obj != null) {
							r.setNormalFieldValue(c + oldColCount, obj);
						} else {
							tMems.remove(q); // ����exps�����������²����ļ�¼
							continue Next;
						}
					}
					
					++q;
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
		
		return table;
	}
}
