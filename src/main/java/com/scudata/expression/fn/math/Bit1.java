package com.scudata.expression.fn.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.LongArray;
import com.scudata.array.NumberArray;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * bit1(x) ����x�Ķ����Ʊ�ʾʱ1�ĸ���
 * @author RunQian
 *
 */
public class Bit1 extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.missingParam"));
		}
	}
	
	// ȡ���Ķ�����λ��1�ĸ���
	public static int bitCount(Object obj) {
		if (obj instanceof Long) {
			return Long.bitCount((Long)obj);
		} else if (obj instanceof BigInteger) {
			return ((BigInteger)obj).bitCount();
		} else if (obj instanceof Number) {
			return Integer.bitCount(((Number)obj).intValue());
		} else if (obj instanceof Sequence) {
			Sequence sequence = (Sequence)obj;
			return sequence.getMems().bit1();
		} else if (obj == null) {
			return 0;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	private static int bitCount(IArray array1, IArray array2) {
		int size = array1.size();
		int count = 0;
		
		if (array1 instanceof NumberArray && array2 instanceof NumberArray) {
			for (int i = 1; i <= size; ++i) {
				if (array1.isNull(i) || array2.isNull(i)) {
					continue;
				}
				
				count += Long.bitCount(array1.getLong(i) ^ array2.getLong(i));
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				count += bitCount(array1.get(i), array2.get(i));
			}
		}
		
		return count;
	}
	
	public static int bitCount(Object v1, Object v2) {
		long longValue = 0;
		BigInteger bi = null;
		
		// ����г�Ա������BigDecimal��BigInteger��ʹ��BigInteger����
		if (v1 instanceof BigDecimal) {
			bi = ((BigDecimal)v1).toBigInteger();
		} else if (v1 instanceof BigInteger) {
			bi = (BigInteger)v1;
		} else if (v1 instanceof Number) {
			longValue = ((Number)v1).longValue();
		} else if (v1 == null) {
			return 0;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
		}
		
		if (bi != null) {
			if (v2 instanceof Number) {
				BigInteger tmp = Variant.toBigInteger((Number)v2);
				bi = bi.xor(tmp);
			} else if (v2 == null) {
				return 0;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
			}
		} else if (v2 instanceof BigDecimal) {
			bi = ((BigDecimal)v2).toBigInteger();
			bi = bi.xor(BigInteger.valueOf(longValue));
		} else if (v2 instanceof BigInteger) {
			bi = (BigInteger)v2;
			bi = bi.xor(BigInteger.valueOf(longValue));
		} else if (v2 instanceof Number) {
			longValue ^= ((Number)v2).longValue();
		} else if (v2 == null) {
			return 0;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
		}
		
		if (bi == null) {
			return Long.bitCount(longValue);
		} else {
			return bi.bitCount();
		}
	}
	
	public Object calculate(Context ctx) {
		if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			return ObjectCache.getInteger(bitCount(obj));
		} else if (param.getSubSize() == 2) {
			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj1 = sub1.getLeafExpression().calculate(ctx);
			Object obj2 = sub2.getLeafExpression().calculate(ctx);
			if (obj1 instanceof Sequence && obj2 instanceof Sequence) {
				IArray array1 = ((Sequence)obj1).getMems();
				IArray array2 = ((Sequence)obj2).getMems();
				return ObjectCache.getInteger(bitCount(array1, array2));
			} else {
				return ObjectCache.getInteger(bitCount(obj1, obj2));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
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
			IntArray result = new IntArray(len);
	
			if (array instanceof IntArray) {
				IntArray intArray = (IntArray)array;
				for (int i = 1; i <= len; ++i) {
					if (intArray.isNull(i)) {
						result.pushInt(0);
					} else {
						result.pushInt(Integer.bitCount(intArray.getInt(i)));
					}
				}
			} else if (array instanceof LongArray) {
				LongArray longArray = (LongArray)array;
				for (int i = 1; i <= len; ++i) {
					if (longArray.isNull(i)) {
						result.pushInt(0);
					} else {
						result.pushInt(Long.bitCount(longArray.getLong(i)));
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					result.pushInt(bitCount(array.get(i)));
				}
			}
			
			return result;
		} else if (param.getSubSize() == 2) {
			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
			}
			
			IArray array1 = sub1.getLeafExpression().calculateAll(ctx);
			IArray array2 = sub2.getLeafExpression().calculateAll(ctx);
			int len = array1.size();
			IntArray result = new IntArray(len);
			
			if (array1 instanceof NumberArray && array2 instanceof NumberArray) {
				for (int i = 1; i <= len; ++i) {
					if (array1.isNull(i) || array2.isNull(i)) {
						result.pushInt(0);
					} else {
						int count = Long.bitCount(array1.getLong(i) ^ array2.getLong(i));
						result.pushInt(count);
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					Object obj1 = array1.get(i);
					Object obj2 = array2.get(i);
					if (obj1 instanceof Sequence && obj2 instanceof Sequence) {
						int count = bitCount(((Sequence)obj1).getMems(), ((Sequence)obj2).getMems());
						result.pushInt(count);
					} else {
						int count = bitCount(obj1, obj2);
						result.pushInt(count);
					}
				}
			}
			
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
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
		if (param.isLeaf()) {
			IArray array = param.getLeafExpression().calculateAll(ctx, signArray, sign);
			int len = array.size();
			IntArray result = new IntArray(len);
	
			if (array instanceof IntArray) {
				IntArray intArray = (IntArray)array;
				for (int i = 1; i <= len; ++i) {
					if (intArray.isNull(i)) {
						result.pushInt(0);
					} else if (signArray.isTrue(i) == sign) {
						result.pushInt(Integer.bitCount(intArray.getInt(i)));
					} else {
						result.pushInt(0);
					}
				}
			} else if (array instanceof LongArray) {
				LongArray longArray = (LongArray)array;
				for (int i = 1; i <= len; ++i) {
					if (longArray.isNull(i)) {
						result.pushInt(0);
					} else if (signArray.isTrue(i) == sign) {
						result.pushInt(Long.bitCount(longArray.getLong(i)));
					} else {
						result.pushInt(0);
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					if (signArray.isTrue(i) == sign) {
						result.pushInt(bitCount(array.get(i)));
					} else {
						result.pushInt(0);
					}
				}
			}
			
			return result;
		} else if (param.getSubSize() == 2) {
			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
			}
			
			IArray array1 = sub1.getLeafExpression().calculateAll(ctx, signArray, sign);
			IArray array2 = sub2.getLeafExpression().calculateAll(ctx, signArray, sign);
			int len = array1.size();
			IntArray result = new IntArray(len);
			
			if (array1 instanceof NumberArray && array2 instanceof NumberArray) {
				for (int i = 1; i <= len; ++i) {
					if (array1.isNull(i) || array2.isNull(i)) {
						result.pushInt(0);
					} else if (signArray.isTrue(i) == sign) {
						int count = Long.bitCount(array1.getLong(i) ^ array2.getLong(i));
						result.pushInt(count);
					} else {
						result.pushInt(0);
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					Object obj1 = array1.get(i);
					Object obj2 = array2.get(i);
					if (signArray.isTrue(i) == sign) {
						if (obj1 instanceof Sequence && obj2 instanceof Sequence) {
							int count = bitCount(((Sequence)obj1).getMems(), ((Sequence)obj2).getMems());
							result.pushInt(count);
						} else {
							int count = bitCount(obj1, obj2);
							result.pushInt(count);
						}
					} else {
						result.pushInt(0);
					}
				}
			}
			
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bit1" + mm.getMessage("function.invalidParam"));
		}
	}
}
