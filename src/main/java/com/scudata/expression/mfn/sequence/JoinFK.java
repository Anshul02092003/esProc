package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.DiffJoin;
import com.scudata.dm.op.FilterJoin;
import com.scudata.dm.op.Join;
import com.scudata.dm.op.JoinRemote;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.parallel.ClusterMemoryTable;
import com.scudata.resources.EngineMessage;

/**
 * ����������������������������һЩ�ֶ������������
 * A.join(x:��,A:y:��,z:F,��;x:��,A:y:��,z:F,��;)
 * @author RunQian
 *
 */
public class JoinFK extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("join" + mm.getMessage("function.missingParam"));
		}

		Expression [][]exps;
		Object []codes;
		Expression [][]dataExps;
		Expression [][]newExps;
		String [][]newNames;
		boolean isOrg = option != null && option.indexOf('o') != -1;
		String fname = null;
		
		if (param.getType() == IParam.Comma) {
			if (isOrg) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("join" + mm.getMessage("function.invalidParam"));
			}
			
			exps = new Expression[1][];
			codes = new Object[1];
			dataExps = new Expression[1][];
			newExps = new Expression[1][];
			newNames = new String[1][];

			parseJoinParam(param, 0, exps, codes, dataExps, newExps, newNames, ctx);
		} else if (param.getType() == IParam.Semicolon) {
			int count = param.getSubSize();
			if (isOrg) {
				IParam sub = param.getSub(0);
				if (sub != null) {
					if (!sub.isLeaf()) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
					
					fname = sub.getLeafExpression().getIdentifierName();
				}
				
				int len = count - 1;
				exps = new Expression[len][];
				codes = new Object[len];
				dataExps = new Expression[len][];
				newExps = new Expression[len][];
				newNames = new String[len][];
	
				for (int i = 0; i < len; ++i) {
					sub = param.getSub(i + 1);
					if (sub == null || sub.getType() != IParam.Comma) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
	
					parseJoinParam(sub, i, exps, codes, dataExps, newExps, newNames, ctx);
				}
			} else {
				exps = new Expression[count][];
				codes = new Object[count];
				dataExps = new Expression[count][];
				newExps = new Expression[count][];
				newNames = new String[count][];
	
				for (int i = 0; i < count; ++i) {
					IParam sub = param.getSub(i);
					if (sub == null || sub.getType() != IParam.Comma) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("join" + mm.getMessage("function.invalidParam"));
					}
	
					parseJoinParam(sub, i, exps, codes, dataExps, newExps, newNames, ctx);
				}
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("join" + mm.getMessage("function.invalidParam"));
		}

		int count = codes.length;
		Sequence []seqs = new Sequence[count];
		boolean hasClusterTable = false;
		boolean hasNewExps = false;
		for (int i = 0; i < count; ++i) {
			if (newExps[i] != null && newExps[i].length > 0) {
				hasNewExps = true;
			}
			
			if (codes[i] instanceof Sequence || codes[i] == null) {
				seqs[i] = (Sequence)codes[i];
			} else if (codes[i] instanceof ClusterMemoryTable) {
				hasClusterTable = true;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("join" + mm.getMessage("function.paramTypeError"));
			}
		}
		
		boolean isIsect = false, isDiff = false;
		if (!hasNewExps && option != null) {
			if (option.indexOf('i') != -1) {
				isIsect = true;
			} else if (option.indexOf('d') != -1) {
				isDiff = true;
			}
		}
		
		Operation op;
		if (isIsect) {
			op = new FilterJoin(this, exps, seqs, dataExps);
		} else if (isDiff) {
			op = new DiffJoin(this, exps, seqs, dataExps);
		} else if (hasClusterTable) {
			op = new JoinRemote(this, fname, exps, codes, dataExps, newExps, newNames, option);
		} else {
			op = new Join(this, fname, exps, seqs, dataExps, newExps, newNames, option);
		}
		
		return op.process(srcSequence, ctx);
	}
}
