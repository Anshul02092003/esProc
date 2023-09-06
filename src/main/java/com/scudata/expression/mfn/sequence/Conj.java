package com.scudata.expression.mfn.sequence;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.ParamParser;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * �������г�Ա�ĺ���
 * A.conj() A.conj(x)��A�����е�����
 * @author RunQian
 *
 */
public class Conj extends SequenceFunction {
	/**
	 * ���ú�������
	 * @param cs �������
	 * @param ctx ����������
	 * @param param ���������ַ���
	 */
	public void setParameter(ICellSet cs, Context ctx, String param) {
		strParam = param;
		this.cs = cs;
		
		// A.conj(x,��)�Ѳ�������һ�����崴���ɶ��ű��ʽ
		this.param = ParamParser.newLeafParam(param, cs, ctx);
		if (next != null) {
			next.setParameter(cs, ctx, param);
		}
	}
	
	public Object calculate(Context ctx) {
		if (srcSequence.ifn() instanceof ICursor) {
			int len = srcSequence.length();
			Sequence cursorSeq = new Sequence(len);
			
			for (int i = 1; i <= len; ++i) {
				Object obj = srcSequence.getMem(i);
				if (obj instanceof ICursor) {
					cursorSeq.add(obj);
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("conjx" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			len = cursorSeq.length();
			if (len > 0) {
				ICursor[] cursors = new ICursor[len];
				cursorSeq.toArray(cursors);
				return new ConjxCursor(cursors);
			} else {
				return null;
			}
		} else if (param == null) {
			return srcSequence.conj(option);
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			return srcSequence.calc(exp, "o", ctx).conj(option);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("conj" + mm.getMessage("function.invalidParam"));
		}
	}
}