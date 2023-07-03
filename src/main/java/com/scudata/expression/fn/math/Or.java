package com.scudata.expression.fn.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����г�Ա���������İ�λ��,����ֵ��Ա������
 * @author yanjing
 *
 */
public class Or extends Function {
	public static Object or(Object v1, Object v2) {
		long longValue = 0;
		BigInteger bi = null; // �������BigInteger�򷵻�BigInteger
		
		// ����г�Ա������BigDecimal��BigInteger��ʹ��BigInteger����
		if (v1 instanceof BigDecimal) {
			bi = ((BigDecimal)v1).toBigInteger();
		} else if (v1 instanceof BigInteger) {
			bi = (BigInteger)v1;
		} else if (v1 instanceof Number) {
			longValue = ((Number)v1).longValue();
		} else if (v1 == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("or" + mm.getMessage("function.paramTypeError"));
		}
		
		if (bi != null) {
			if (v2 instanceof Number) {
				BigInteger tmp = Variant.toBigInteger((Number)v2);
				bi = bi.or(tmp);
			} else if (v2 == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("or" + mm.getMessage("function.paramTypeError"));
			}
		} else if (v2 instanceof BigDecimal) {
			bi = ((BigDecimal)v2).toBigInteger();
			bi = bi.or(BigInteger.valueOf(longValue));
		} else if (v2 instanceof BigInteger) {
			bi = (BigInteger)v2;
			bi = bi.or(BigInteger.valueOf(longValue));
		} else if (v2 instanceof Number) {
			longValue |= ((Number)v2).longValue();
		} else if (v2 == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("or" + mm.getMessage("function.paramTypeError"));
		}
		
		if (bi == null) {
			return longValue;
		} else {
			return new BigDecimal(bi);
		}
	}
	
	// �������г�Ա��λ��
	private static Object or(Sequence seq) {
		int size = seq.length();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return seq.getMem(1);
		}
		
		Object obj = seq.getMem(1);
		boolean returnInt = true; // �Ƿ�ȫ������Integer��������򷵻�Integer�����򷵻�Long
		long longValue = 0;
		BigInteger bi = null; // �������BigInteger�򷵻�BigInteger
		
		// ����г�Ա������BigDecimal��BigInteger��ʹ��BigInteger����
		if (obj instanceof BigDecimal) {
			bi = ((BigDecimal)obj).toBigInteger();
		} else if (obj instanceof BigInteger) {
			bi = (BigInteger)obj;
		} else if (obj instanceof Number) {
			if (!(obj instanceof Integer)) {
				returnInt = false;
			}
			
			longValue = ((Number)obj).longValue();
		} else if (obj == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("or" + mm.getMessage("function.paramTypeError"));
		}
		
		for (int i = 2; i <= size; ++i) {
			obj = seq.getMem(i);
			if (bi != null) {
				if (obj instanceof Number) {
					BigInteger tmp = Variant.toBigInteger((Number)obj);
					bi = bi.or(tmp);
				} else if (obj == null) {
					return null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("or" + mm.getMessage("function.paramTypeError"));
				}
			} else if (obj instanceof BigDecimal) {
				bi = ((BigDecimal)obj).toBigInteger();
				bi = bi.or(BigInteger.valueOf(longValue));
			} else if (obj instanceof BigInteger) {
				bi = (BigInteger)obj;
				bi = bi.or(BigInteger.valueOf(longValue));
			} else if (obj instanceof Number) {
				if (!(obj instanceof Integer)) {
					returnInt = false;
				}
				
				longValue |= ((Number)obj).longValue();
			} else if (obj == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("or" + mm.getMessage("function.paramTypeError"));
			}
		}
		
		if (bi == null) {
			if (returnInt) {
				return (int)longValue;
			} else {
				return longValue;
			}
		} else {
			return new BigDecimal(bi);
		}
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("or" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return or((Sequence)obj);
			} else {
				return obj;
			}
		} else {
			int size = param.getSubSize();
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("or" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = sub.getLeafExpression().calculate(ctx);
			boolean returnInt = true; // �Ƿ�ȫ������Integer��������򷵻�Integer�����򷵻�Long
			long longValue = 0;
			BigInteger bi = null; // �������BigInteger�򷵻�BigInteger
			
			// ����г�Ա������BigDecimal��BigInteger��ʹ��BigInteger����
			if (obj instanceof BigDecimal) {
				bi = ((BigDecimal)obj).toBigInteger();
			} else if (obj instanceof BigInteger) {
				bi = (BigInteger)obj;
			} else if (obj instanceof Number) {
				if (!(obj instanceof Integer)) {
					returnInt = false;
				}
				
				longValue = ((Number)obj).longValue();
			} else if (obj == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("or" + mm.getMessage("function.paramTypeError"));
			}
			
			for (int i = 1; i < size; ++i) {
				sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("or" + mm.getMessage("function.invalidParam"));
				}
				
				obj = sub.getLeafExpression().calculate(ctx);
				if (bi != null) {
					if (obj instanceof Number) {
						BigInteger tmp = Variant.toBigInteger((Number)obj);
						bi = bi.or(tmp);
					} else if (obj == null) {
						return null;
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException("or" + mm.getMessage("function.paramTypeError"));
					}
				} else if (obj instanceof BigDecimal) {
					bi = ((BigDecimal)obj).toBigInteger();
					bi = bi.or(BigInteger.valueOf(longValue));
				} else if (obj instanceof BigInteger) {
					bi = (BigInteger)obj;
					bi = bi.or(BigInteger.valueOf(longValue));
				} else if (obj instanceof Number) {
					if (!(obj instanceof Integer)) {
						returnInt = false;
					}
					
					longValue |= ((Number)obj).longValue();
				} else if (obj == null) {
					return null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("or" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			if (bi == null) {
				if (returnInt) {
					return (int)longValue;
				} else {
					return longValue;
				}
			} else {
				return new BigDecimal(bi);
			}
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		if (param.isLeaf()) {
			IArray array = param.getLeafExpression().calculateAll(ctx);
			int len = array.size();
			IArray result = new ObjectArray(len);
			
			
			for (int i = 1; i <= len; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Sequence) {
					result.push(or((Sequence)obj));
				} else {
					result.push(obj);
				}
			}
			
			return result;
		} else {
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("and" + mm.getMessage("function.invalidParam"));
			}
			
			IArray result = sub.getLeafExpression().calculateAll(ctx);
			for (int i = 1, size = param.getSubSize(); i < size; ++i) {
				sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("and" + mm.getMessage("function.invalidParam"));
				}
				
				IArray array = sub.getLeafExpression().calculateAll(ctx);
				result = result.bitwiseOr(array);
			}
			
			return result;
		}
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		return calculateAll(ctx);
	}
}
