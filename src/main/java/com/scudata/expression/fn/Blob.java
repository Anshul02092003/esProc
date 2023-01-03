package com.scudata.expression.fn;

import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * blob���������л�����ÿ�ֽڶ�Ӧһ����Ա
 * blob(b)
 * @author RunQian
 *
 */
public class Blob extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("blob" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("blob" + mm.getMessage("function.invalidParam"));
		}
	}
	
	public Node optimize(Context ctx) {
		param.optimize(ctx);
		return this;
	}

	public Object calculate(Context ctx) {
		Object obj = param.getLeafExpression().calculate(ctx);
		if (obj instanceof Sequence) {
			return toBlob((Sequence)obj);
		} else if (obj instanceof byte[]) {
			return toSequence((byte[])obj);
		} else if (obj == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("blob" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	private static Sequence toSequence(byte []bytes) {
		int len = bytes.length;
		if (len == 0) {
			return null;
		}
		
		Sequence seq = new Sequence(len);
		for (byte b : bytes) {
			seq.add(ObjectCache.getInteger(b & 0xff));
		}
		
		return seq;
	}
	
	private static byte[] toBlob(Sequence seq) {
		int len = seq.length();
		if (len == 0) {
			return null;
		}
		
		byte []bytes = new byte[len];
		for (int i = 1; i <= len; ++i) {
			Object obj = seq.getMem(i);
			if (obj instanceof Number) {
				bytes[i - 1] = ((Number)obj).byteValue();
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needIntSeries"));
			}
		}
		
		return bytes;
	}
}
