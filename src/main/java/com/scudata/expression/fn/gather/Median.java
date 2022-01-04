package com.scudata.expression.fn.gather;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * �ۺϺ���Median��
 * 
 * @author ��־��
 *
 */
public class Median extends Gather  {
	int	parK = 0;	// ѡ���ĸ��ֶΡ���Ϊ0���ʽ��ֵΪ��
	int	parN = 0;	// �ֶ��ٶΣ���Ϊ0��ʾ��ֵΪ�ա�
	private Expression exp;	// ������ʽ
	
	/**
	 * ����median(k:n, exp)�ֱ����k��n��exp
	 */
	public void prepare(Context ctx) {
		if (param == null || param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("median" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		exp = sub1.getLeafExpression();
		
		// median k��n���������Ϊ�գ���ʱȡ��ֵ��
		if (null == sub0) {
			parK	= 1;
			parN	= 2;
			return;
		}
		
		sub1 = sub0.getSub(1);
		sub0 = sub0.getSub(0);
		
		try {
			Expression data;
			Object obj;
			if (null == sub0)
				parK = 0;
			else {
				data = sub0.getLeafExpression();
				obj = data.calculate(null);
				parK = (Integer)obj;
			}
			
			if (null == sub1)
				parN = 0;
			else {
				data = sub1.getLeafExpression();
				obj = data.calculate(null);
				parN = (Integer)obj;
			}
			
			if (parN < 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
			if (parK > parN) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
		} catch(Exception e) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("median" + mm.getMessage("function.invalidParam"));
		}
	}

	/**
	 * ��һ����¼��ӵ�meidan�м����ݻ��������ʱ�������ϵ�median��
	 * 		���������ݰ����������ݺͷ��������ݡ�
	 * 		����������Ҫ�����в�ֺ󣬺ϲ���������ֺ�����ݻ��������У�������һ����֣�
	 * 		����������ֱ�Ӻϲ���
	 * @param	oldValue	��ǰ���м�����
	 * @param	ctx	�����ı������ṩ�����м����ݵ�������
	 */
	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (val == null)
			return oldValue;
		
		if(oldValue == null) {
			if (val instanceof Sequence) {
				return val;
			} else {
				Sequence seq = new Sequence();
				seq.add(val);
				return seq;
			}
		} else {
			if (val instanceof Sequence) {
				((Sequence)oldValue).addAll((Sequence)val);
			} else {
				((Sequence)oldValue).add(val);
			}
			
			return oldValue;
		}

	}

	/**
	 * �Ѽ��������ݺϲ�Ϊ�ڴ���м�����
	 * 		
	 */
	public Object gather(Context ctx) {
		Object val = exp.calculate(ctx);

		if (val instanceof Sequence) {
			return val;
		} else {
			Sequence seq = new Sequence();
			seq.add(val);
			return seq;
		}
	}

	/**
	 * ����median�ڵĲ�������ԭ���ʽ�ַ�����
	 * @param	q	��ԭʱ����Ӧ�������С�
	 */
	public Expression getRegatherExpression(int q) {
		String str = "median("+parK+":"+parN+",#"+ + q + ")";
		return new Expression(str);

	}

	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.unknownFunction") + "icount");
	}

	/**
	 * �Ƿ���Ҫ����ͳ�Ʋ���
	 */
	public boolean needFinish() {
		return true;
	}
	
	/**
	 * ͳ����ʱ�м����ݣ��������ս����
	 */
	public Object finish(Object val) {
		if (val == null || !(val instanceof Sequence)) {
			return val;
		}
	
		Sequence seq = ((Sequence) val).sort(null);
		return seq.median(1, seq.length(), parK, parN);	
	}
}
