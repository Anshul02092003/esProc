package com.scudata.expression.fn;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.KeyWord;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * xjoin(Ai:Fi,xi;��)
 * ��Ai����������������������һ���������������ֶ�Fi,����ɣ�ÿ��Fi����ԭ����Ai��һ����Ա����˹����У�
 * ���˳�Ai����������xi�ĳ�Ա
 * @author runqian
 *
 */
public class XJoin extends Function {
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}

	public Object calculate(Context ctx) {
		if (param == null || param.getType() != IParam.Semicolon) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
		}

		int count = param.getSubSize();
		Sequence []sequences = new Sequence[count];
		String []names = new String[count];
		Expression []exps = new Expression[count];

		for (int i = 0; i < count; ++i) {
			IParam sub = param.getSub(i);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
			} else if (sub.isLeaf()) { // Ai
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Sequence) {
					sequences[i] = (Sequence)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.paramTypeError"));
				}
			} else if (sub.getType() == IParam.Colon) { // Ai:Fi
				if (sub.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
				}

				IParam sub0 = sub.getSub(0);
				if (sub0 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
				}

				Object obj = sub0.getLeafExpression().calculate(ctx);
				if (obj instanceof Sequence) {
					sequences[i] = (Sequence)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.paramTypeError"));
				}

				IParam sub1 = sub.getSub(1);
				if (sub1 != null) {
					names[i] = sub1.getLeafExpression().getIdentifierName();
				}
			} else { // Ai:Fi,xi
				if (sub.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
				}

				IParam seqParam = sub.getSub(0);
				if (seqParam == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
				} else if (seqParam.isLeaf()) {
					Object obj = seqParam.getLeafExpression().calculate(ctx);
					if (obj instanceof Sequence) {
						sequences[i] = (Sequence)obj;
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("xjoin" + mm.getMessage("function.paramTypeError"));
					}
				} else {
					if (seqParam.getSubSize() != 2) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
					}

					IParam sub0 = seqParam.getSub(0);
					if (sub0 == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
					}

					Object obj = sub0.getLeafExpression().calculate(ctx);
					if (obj instanceof Sequence) {
						sequences[i] = (Sequence)obj;
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("xjoin" + mm.getMessage("function.paramTypeError"));
					}

					IParam sub1 = seqParam.getSub(1);
					if (sub1 != null) {
						names[i] = sub1.getLeafExpression().getIdentifierName();
					}
				}

				IParam expParam = sub.getSub(1);
				if (expParam == null || !expParam.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xjoin" + mm.getMessage("function.invalidParam"));
				}

				exps[i] = expParam.getLeafExpression();
			}
		}

		String []opts = KeyWord.parseStringOptions(names);
		Sequence result = Sequence.xjoin(sequences, exps, opts, names, option, ctx);
		return checkOptionX(sequences, result, names);
	}

	private Sequence checkOptionX(Sequence []sequences, Sequence table, String[] names) {
		if (option == null || option.indexOf('x') == -1 || table == null) {
			return table;
		}
		
		ListBase1 mems = table.getMems();
		int size = mems.size();
		if (size == 0) {
			return table;
		}
		
		int count = names.length;
		int newCount = count;
		boolean hasAllRecord = false;
		boolean isAllRecord[] = new boolean[count];
		int fcount[] = new int[count];
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			fcount[i] = sequences[i].dataStruct().getFieldCount();
			isAllRecord[i] = true;
			for (int j = 0; j < fcount[i]; j++) {
				boolean b = isAllRecord(mems, i, j, list, names);
				if (!b) {
					isAllRecord[i] = false;
					break;
				}
			}
			if (isAllRecord[i]) {
				newCount += (fcount[i] - 1);
				hasAllRecord = true;
			}
		}
		
		if (!hasAllRecord) {
			return table;
		}
		
		String newNames[] = new String[newCount];
		list.toArray(newNames);
		int findex = 0;

		Table out = new Table(newNames);
		for (int i = 1; i <= size; ++i) {
			findex = 0;
			Record record = out.newLast();
			Record oldRecord = (Record) mems.get(i);
			for (int j = 0; j < count; j++) {
				if (isAllRecord[j]) {
					Record subRecord = (Record) oldRecord.getFieldValue(j);
					for (int f = 0; f < fcount[j]; f++) {
						record.set(findex + f, subRecord.getFieldValue(f));
					}
					findex += fcount[j];
				} else {
					record.set(findex, oldRecord.getFieldValue(j));
					findex++;
				}
			}
		}

		return out;
	}
	
	private boolean isAllRecord(ListBase1 mems, int i, int j, ArrayList<String> list, String names[]) {
		boolean b = true;
		int size = mems.size();
		int c;
		for (c = 1; c <= size; c++) {
			Record record = (Record) mems.get(c);
			if (null == record) {
				continue;
			}
			record = (Record) record.getFieldValue(i);
			if (null == record) {
				continue;
			}
			Object obj = record.getFieldValue(j);
			if (obj == null) {
				continue;
			} else if (obj instanceof Record) {
				String name = record.getFieldNames()[j];
				if (list.contains(name)) {
					list.add(names[i] + "_" + name);
				} else {
					list.add(name);
				}
				break;
			} else {
				b = false;
				break;
			}
		}
		
		if (!b) {
			list.add(names[i]);
		}
		if (c > size) {
			list.add(names[i] + "_" + j);
		}
		return b;
	}
}
