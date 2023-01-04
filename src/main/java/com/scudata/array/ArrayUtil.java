package com.scudata.array;

import java.util.Date;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

public final class ArrayUtil {
	public static IArray newArray(Object value, int capacity) {
		if (value instanceof Integer) {
			IntArray result = new IntArray(capacity);
			result.pushInt(((Integer)value).intValue());
			return result;
		} else if (value instanceof Long) {
			LongArray result = new LongArray(capacity);
			result.pushLong(((Long)value).longValue());
			return result;
		} else if (value instanceof Double) {
			DoubleArray result = new DoubleArray(capacity);
			result.pushDouble(((Double)value).doubleValue());
			return result;
		} else if (value instanceof Date) {
			DateArray result = new DateArray(capacity);
			result.pushDate((Date)value);
			return result;
		} else if (value instanceof String) {
			StringArray result = new StringArray(capacity);
			result.pushString((String)value);
			return result;
		} else if (value instanceof Boolean) {
			BoolArray result = new BoolArray(capacity);
			result.pushBool(((Boolean)value).booleanValue());
			return result;
		} else {
			ObjectArray result = new ObjectArray(capacity);
			result.push(value);
			return result;
		}
	}
	
	/**
	 * ȡ�����Ա�Ĳ���ֵ���������
	 * @param array
	 * @param value
	 * @return
	 */
	public static BoolArray booleanValue(IArray array, boolean value) {
		if (value) {
			BoolArray result = array.isTrue();
			if (result == array) {
				return (BoolArray)result.dup();
			} else {
				return result;
			}
		} else {
			return array.isFalse();
		}
	}
	
	/**
	 * ��������ĳ�Ա��null�Ĺ�ϵ
	 * @param signs �����Ա�Ƿ�Ϊ�ձ�־��trueΪ��
	 * @param size �����Ա��
	 * @param relation �ȽϹ�ϵ
	 * @return BoolArray �Ƚ�ֵ����
	 */
	public static BoolArray calcRelationNull(boolean []signs, int size, int relation) {
		boolean []resultDatas = new boolean[size + 1];		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			if (signs != null) {
				System.arraycopy(signs, 1, resultDatas, 1, size);
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!signs[i]) {
						resultDatas[i] = true;
					}
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = true;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			if (signs != null) {
				System.arraycopy(signs, 1, resultDatas, 1, size);
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!signs[i]) {
						resultDatas[i] = true;
					}
				}
			}
		} else if (relation == Relation.OR) {
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !signs[i];
				}
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	/**
	 * ����ĳ�Ա��null���Ƚ�
	 * @param datas ����
	 * @param size �����Ա��
	 * @param relation �ȽϹ�ϵ
	 * @return BoolArray �Ƚ�ֵ����
	 */
	public static BoolArray calcRelationNull(Object []datas, int size, int relation) {
		boolean []resultDatas = new boolean[size + 1];		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				if (datas[i] == null) {
					resultDatas[i] = true;
				}
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					resultDatas[i] = true;
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = true;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				if (datas[i] == null) {
					resultDatas[i] = true;
				}
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					resultDatas[i] = true;
				}
			}
		} else if (relation == Relation.OR) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = datas[i] != null;
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	/**
	 * ��������ĳ�Ա��null�Ĺ�ϵ
	 * @param signs �����Ա�Ƿ�Ϊ�ձ�־��trueΪ��
	 * @param size �����Ա��
	 * @param relation �ȽϹ�ϵ
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public static void calcRelationsNull(boolean []signs, int size, int relation, BoolArray result, boolean isAnd) {
		boolean []resultDatas = result.getDatas();
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = false;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!signs[i]) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (signs != null) {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (signs != null) {
					for (int i = 1; i <= size; ++i) {
						if (!signs[i]) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (signs != null) {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = false;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (signs != null) {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = true;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!signs[i]) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (signs != null) {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = true;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!signs[i]) {
							resultDatas[i] = true;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * ����ĳ�Ա��null���Ƚ�
	 * @param datas ����
	 * @param size �����Ա��
	 * @param relation �ȽϹ�ϵ
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public static void calcRelationsNull(Object []datas, int size, int relation, BoolArray result, boolean isAnd) {
		boolean []resultDatas = result.getDatas();	
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] == null) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] == null) {
						resultDatas[i] = false;
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] == null) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] == null) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						resultDatas[i] = true;
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * ȡ������г�Ա�����
	 * @param o1 Object
	 * @param o2 Object
	 * @return Object
	 */
	public static Object mod(Object o1, Object o2) {
		if (o1 instanceof Number) {
			if (o2 instanceof Number) {
				return Variant.mod((Number)o1, (Number)o2);
			} else if (o2 instanceof Sequence) {
				Sequence seq1 = new Sequence(1);
				seq1.add(o1);
				return CursorUtil.xor(seq1, (Sequence)o2);
			} else if (o2 == null) {
				return null;
			}
		} else if (o1 instanceof Sequence) {
			if (o2 instanceof Sequence) {
				return CursorUtil.xor((Sequence)o1, (Sequence)o2);
			} else if (o2 == null) {
				return o1;
			} else {
				Sequence seq2 = new Sequence(1);
				seq2.add(o2);
				return CursorUtil.xor((Sequence)o1, seq2);
			}
		} else if (o2 instanceof Sequence) {
			if (o1 == null) {
				return o2;
			} else {
				Sequence seq1 = new Sequence(1);
				seq1.add(o1);
				return CursorUtil.xor(seq1, (Sequence)o2);
			}
		} else if (o1 == null) {
			if (o2 instanceof Sequence) {
				return o2;
			} else if (o2 instanceof Number) {
				return null;
			} else if (o2 == null) {
				return null;
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(Variant.getDataType(o1) + mm.getMessage("Variant2.with") +
				Variant.getDataType(o2) + mm.getMessage("Variant2.illMod"));
	}
	
	/**
	 * ���������г�Ա�
	 * @param o1 Object
	 * @param o2 Object
	 * @return Object
	 */
	public static Object intDivide(Object o1, Object o2) {
		if (o1 instanceof Number) {
			if (o2 instanceof Number) {
				return Variant.intDivide((Number)o1, (Number)o2);
			} else if (o2 == null) {
				return null;
			}
		} else if (o1 instanceof Sequence) {
			if (o2 instanceof Sequence) {
				return ((Sequence)o1).diff((Sequence)o2, false);
			} else if (o2 == null) {
				return o1;
			} else {
				Sequence seq2 = new Sequence(1);
				seq2.add(o2);
				return ((Sequence)o1).diff(seq2, false);
			}
		} else if (o1 == null) {
			if (o2 instanceof Number) {
				return null;
			} else if (o2 == null) {
				return null;
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(Variant.getDataType(o1) + mm.getMessage("Variant2.with") +
				Variant.getDataType(o2) + mm.getMessage("Variant2.illDivide"));
	}
}
