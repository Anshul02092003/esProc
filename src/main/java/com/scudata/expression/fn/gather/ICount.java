package com.scudata.expression.fn.gather;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * ȡ���ظ���Ԫ�ظ�����ȥ��ȡֵΪfalse��Ԫ��
 * icount(x1,��)
 * @author RunQian
 *
 */
public class ICount extends Gather {
	private Expression exp; // ���ʽ
	private boolean isSorted = false; // �����Ƿ񰴱��ʽ����
	
	// ����icount���м�����Ϣ
	static class ICountInfo_o implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int count;
		private Object startValue;
		private Object endValue;
		
		public ICountInfo_o() {
		}
		
		public ICountInfo_o(Object startValue) {
			if (startValue != null) {
				count = 1;
				this.startValue = startValue;
				this.endValue = startValue;
			}
		}
		
		public void put(Object value) {
			if (value instanceof ICountInfo_o) {
				ICountInfo_o next = (ICountInfo_o)value;
				if (!Variant.isEquals(endValue, next.startValue)) {
					count += next.count;
				} else {
					count += next.count - 1;
				}
				
				endValue = next.endValue;
			} else {
				if (!Variant.isEquals(endValue, value)) {
					count++;
					endValue = value;
				}
			}
		}
	}
	
	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
		isSorted = option != null && option.indexOf('o') != -1;
	}

	/**
	 * ��һ����¼����������ݣ���ӵ���ʱ�м�����
	 */
	public Object gather(Context ctx) {
		// ���ݰ�icount�ֶ�����
		if (isSorted) {
			Object val = exp.calculate(ctx);
			if (val instanceof ICountInfo_o){
				return val;
			} else {
				return new ICountInfo_o(val);
			}
		}

		Object val = exp.calculate(ctx);
		if (val instanceof HashSet) {
			return val;
		} else if (val instanceof Sequence){
			Sequence seq = (Sequence)val;
			int len = seq.length();
			HashSet<Object> set = new HashSet<Object>(len + 8);
			for (int i = 1; i <= len; ++i) {
				set.add(seq.getMem(i));
			}
			
			return set;
		} else if (val != null) {
			HashSet<Object> set = new HashSet<Object>();
			if (val != null) {
				set.add(val);
			}
			
			return set;
		} else {
			return null;
		}
	}
	
	/**
	 * �������������ϵ���ʱ�м�����
	 * 		
	 * @param	oldValue	�ϵ�����(������null�����Ϊ��ϣ)
	 * @param	ctx			�����ı���
	 */
	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (val == null) {
			return oldValue;
		}
		
		// ���ݰ�icount�ֶ�����
		if (isSorted) {
			((ICountInfo_o)oldValue).put(val);
			return oldValue;
		}
		
		if (oldValue == null) {
			if (val instanceof HashSet) {
				return val;
			} else if (val instanceof Sequence){
				Sequence seq = (Sequence)val;
				int len = seq.length();
				HashSet<Object> set = new HashSet<Object>(len + 8);
				for (int i = 1; i <= len; ++i) {
					set.add(seq.getMem(i));
				}
				
				return set;
			} else {
				HashSet<Object> set = new HashSet<Object>();
				set.add(val);
				return set;
			}
		} else if (oldValue instanceof HashSet){	// ������Ϊ��ϣ
			HashSet<Object> set = ((HashSet<Object>)oldValue);
			if (val instanceof HashSet) {
				set.addAll((HashSet<Object>)val);
			} else if (val instanceof Sequence){
				Sequence seq = (Sequence)val;
				int len = seq.length();
				for (int i = 1; i <= len; ++i) {
					set.add(seq.getMem(i));
				}
			} else {
				set.add(val);
			}
			
			return oldValue;
		} else {
			// ������Ϊ����(����ʱ�ļ����ص�)
			Sequence seq = (Sequence)oldValue;
			int len = seq.length();
			HashSet<Object> set = new HashSet<Object>(len + 8);
			for (int i = 1; i <= len; ++i) {
				set.add(seq.getMem(i));
			}
			
			if (val instanceof Sequence) {
				seq = (Sequence)val;
				len = seq.length();
				for (int i = 1; i <= len; ++i) {
					set.add(seq.getMem(i));
				}
			} else {
				set.add(val);
			}
			
			return set;
		} 
	}
	
	/**
	 * ȡ���λ���ʱ�þۺ��ֶζ�Ӧ�ı��ʽ
	 * @param q	��ǰ�����ֶε����
	 * @return	���ܱ��ʽ
	 */
	public Expression getRegatherExpression(int q) {
		if (isSorted) {
			String str = "icount@o(#" + q + ")";
			return new Expression(str);
		} else {
			String str = "icount(#" + q + ")";
			return new Expression(str);
		}
	}
	
	/**
	 * �Ƿ���Ҫ�����м�����ͳ���������ս��
	 */
	public boolean needFinish() {
		return true;
	}

	/**
	 * ���ڴ��е��м�����ת���ɴ������С�
	 * 		
	 * @param	val	��ת��������
	 * @return	����ת����Ľ��
	 */
	public Object finish1(Object val) {
		if (isSorted) {
			return val;
		}
		
		if (val instanceof HashSet) {
			HashSet<Object> set = (HashSet<Object>)val;
			Sequence seq = new Sequence(set.size());
			
			Iterator<Object> iter = set.iterator();
			while (iter.hasNext()) {
				seq.add(iter.next());
			}
			
			return seq;
		} else {
			return val;
		}
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).icount(option);
			} else {
				if (Variant.isTrue(obj)) {
					return ObjectCache.getInteger(1);
				} else {
					return ObjectCache.getInteger(0);
				}
			}
		}

		int size = param.getSubSize();
		HashSet<Object> set = new HashSet<Object>(size);
		for (int i = 0; i < size; ++i) {
			IParam sub = param.getSub(i);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (Variant.isTrue(obj)) {
					set.add(obj);
				}
			}
		}

		return set.size();
	}
	
	/**
	 * �Ƿ���Ҫ�����м���ʱ���ݡ�
	 */
	public boolean needFinish1() {
		return true;
	}
	
	/**
	 * ͳ����ʱ�м����ݣ��������ս����
	 */
	public Object finish(Object val) {
		if (isSorted) {
			return ((ICountInfo_o)val).count;
		}
		
		if (null == val)
			return new Integer(0);
		if (val instanceof Sequence) {
			return ((Sequence)val).length();
		}
		return new Integer(((HashSet<Object>)val).size());
	}
}

