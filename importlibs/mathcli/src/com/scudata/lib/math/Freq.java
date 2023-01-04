package com.scudata.lib.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ����һ����ĳ��ֵ��Ƶ�� 
 * @author bd
 * ԭ��Ƶ�Ⱥ�����D.freq(V,v)��D.count(V==v)/D.len()
 */
public class Freq extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param != null && param.getSubSize() > 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("freq" + mm.getMessage("function.missingParam"));
		}
		Object o = param == null ? null : param.getLeafExpression().calculate(ctx);
		
		return freq(srcSequence, o);
	}
	
	protected static double freq(Sequence seq, Object v) {
		Object res = seq.pos(v, "a");
		int count = 1;
		if (res instanceof Sequence) {
			count = ((Sequence) res).length();
			if (v == null) {
				//���жϿ�ֵ��Ƶ��ʱ��ͬʱ��Ҫ���ǿ��ַ�����NA�Ĵ��ڣ������ֵ
				res = seq.pos("NA", "a");
				if (res instanceof Sequence) {
					count += ((Sequence) res).length();
				}
				res = seq.pos("", "a");
				if (res instanceof Sequence) {
					count += ((Sequence) res).length();
				}
			}
		}
		return count*1d/seq.length();
	}
}
