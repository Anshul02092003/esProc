package com.scudata.expression.fn;

import java.sql.Date;
import java.sql.Timestamp;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���ָ������εĶ�ͷ����һ��ͷ����ʱ��/����
 * range(s,e,k:n)
 * ��s,e֮��ȷֳ�n�ݣ����ص�k�κ͵�k+1�εĶ�ͷ�����ؽ��Ϊ�����У�����s,e���������;����������ݾ�ȷ�ȣ�
 * date�;�ȷ���죬datetime�;�ȷ���룬s,e����������������kʡ�Ե�ʱ�򷵻����еķֶ�ͷ�Լ����ݶε�ͷβ���ݣ�
 * �����ؽ��Ϊn+1���С�
 * @author runqian
 *
 */
public class Range extends Function {

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("range" + mm.getMessage("function.missingParam"));
		} else if (param.getSubSize() != 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("range" + mm.getMessage("function.invalidParam"));
		}

		IParam startParam = param.getSub(0);
		IParam endParam = param.getSub(1);
		IParam segParam = param.getSub(2);
		if (startParam == null || endParam == null || segParam == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("range" + mm.getMessage("function.invalidParam"));
		}
		
		Object startVal = startParam.getLeafExpression().calculate(ctx);
		Object endVal = endParam.getLeafExpression().calculate(ctx);
		if (Variant.compare(startVal, endVal) >= 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("range" + mm.getMessage("function.invalidParam"));
		}
		
		int k = -1;
		int n = 1;
		
		if (segParam.isLeaf()) {
			Object obj = segParam.getLeafExpression().calculate(ctx);
			if (obj instanceof Number) {
				n = ((Number)obj).intValue();
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			if (segParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = segParam.getSub(0);
			IParam sub1 = segParam.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj0 = sub0.getLeafExpression().calculate(ctx);
			if (!(obj0 instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.paramTypeError"));
			}
			
			k = ((Number)obj0).intValue();
			Object obj1 = sub1.getLeafExpression().calculate(ctx);
			if (!(obj1 instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.paramTypeError"));
			}
			
			n = ((Number)obj1).intValue();
			if (k < 1 || k > n) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("range" + mm.getMessage("function.invalidParam"));
			}
		}

		if (k == -1) {
			return range(startVal, endVal, n);
		} else {
			return range(startVal, endVal, k, n);
		}
	}
	
	private Sequence range(Object startVal, Object endVal, int n) {
		if (n <= 1) {
			Sequence seq = new Sequence(2);
			seq.add(startVal);
			seq.add(endVal);
			return seq;
		}
		
		if (startVal instanceof Number && endVal instanceof Number) {
			long len = ((Number)endVal).longValue() - ((Number)startVal).longValue();
			if (len < n) {
				n = (int)len;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					Object val = Variant.add(startVal, i);
					seq.add(val);
				}
				
				seq.add(endVal);
				return seq;
			} else {
				long avg = len / n;
				long mod = len % n;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					if (i <= mod) {
						Object val = Variant.add(startVal, avg * i + i);
						seq.add(val);
					} else {
						Object val = Variant.add(startVal, avg * i + mod);
						seq.add(val);
					}
				}
				
				seq.add(endVal);
				return seq;
			}
		} else if (startVal instanceof Date && endVal instanceof Date) {
			Date startDate = (Date)startVal;
			Date endDate = (Date)endVal;
			int len = (int)Variant.dayInterval(startDate, endDate);
			
			if (len < n) {
				n = (int)len;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					Object val = Variant.elapse(startDate, i, null);
					seq.add(val);
				}
				
				seq.add(endVal);
				return seq;
			} else {
				int avg = len / n;
				int mod = len % n;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					if (i <= mod) {
						Object val = Variant.elapse(startDate, avg * i + i, null);
						seq.add(val);
					} else {
						Object val = Variant.elapse(startDate, avg * i + mod, null);
						seq.add(val);
					}
				}
				
				seq.add(endVal);
				return seq;
			}
		} else if (startVal instanceof Timestamp && endVal instanceof Timestamp) {
			Timestamp startDate = (Timestamp)startVal;
			Timestamp endDate = (Timestamp)endVal;
			int len = (int)Variant.secondInterval(startDate, endDate);
			
			if (len < n) {
				n = (int)len;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					Object val = Variant.elapse(startDate, i, "s");
					seq.add(val);
				}
				
				seq.add(endVal);
				return seq;
			} else {
				int avg = len / n;
				int mod = len % n;
				Sequence seq = new Sequence(n + 1);
				seq.add(startVal);
				
				for (int i = 1; i < n; ++i) {
					if (i <= mod) {
						Object val = Variant.elapse(startDate, avg * i + i, "s");
						seq.add(val);
					} else {
						Object val = Variant.elapse(startDate, avg * i + mod, "s");
						seq.add(val);
					}
				}
				
				seq.add(endVal);
				return seq;
			}
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException("range" + mm.getMessage("function.paramTypeError"));
	}
	
	private Sequence range(Object startVal, Object endVal, int k, int n) {
		Sequence total = range(startVal, endVal, n);
		int len = total.length();
		
		if (k < len) {
			Sequence seq = new Sequence(2);
			seq.add(total.getMem(k));
			seq.add(total.getMem(k + 1));
			return seq;
		} else {
			return null;
		}
	}
}
