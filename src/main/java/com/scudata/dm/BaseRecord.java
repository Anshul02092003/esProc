package com.scudata.dm;

import java.io.Externalizable;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.IRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

public abstract class BaseRecord implements IComputeItem, Externalizable, IRecord, Comparable<BaseRecord> {
	
	/*���½ӿڼ̳���IComputeItem�����ڼ���*/
	public Object getCurrent() {
		return this;
	}
	
	public int getCurrentIndex() {
		throw new RuntimeException();
	}
	
	public Sequence getCurrentSequence() {
		return null;
	}
	
	public boolean isInStack(ComputeStack stack) {
		return stack.isInComputeStack(this);
	}
		
	public void popStack() {
	}
	/*���Ͻӿڼ̳���IComputeItem�����ڼ���*/

	/**
	 * ��Լ�¼������ʽ
	 * @param exp Expression ������ʽ
	 * @param ctx Context ���������Ļ���
	 * @return Object
	 */
	public Object calc(Expression exp, Context ctx) {
		if (exp == null) {
			return null;
		}

		ComputeStack stack = ctx.getComputeStack();
		stack.push(this);
		try {
			return exp.calculate(ctx);
		} finally {
			stack.pop();
		}
	}

	/**
	 * ��¼�Ƚ���hashֵ����ͬ����r.v()
	 * @param r ��¼
	 */
	public int compareTo(BaseRecord r) {
		if (r == this) {
			return 0;
		}
		
		int h1 = hashCode();
		int h2 = r.hashCode();
		
		if (h1 < h2) {
			return -1;
		} else if (h1 > h2) {
			return 1;
		} else {
			Object []vals1 = getFieldValues();
			Object []vals2 = r.getFieldValues();
			int []pkIndex1 = getPKIndex();
			int []pkIndex2 = r.getPKIndex();
			
			if (pkIndex1 != null && pkIndex2 != null && pkIndex1.length == pkIndex2.length) {
				for (int i = 0; i < pkIndex1.length; ++i) {
					int result = Variant.compare(vals1[pkIndex1[i]], vals2[pkIndex2[i]], true);
					if (result != 0) {
						return result;
					}
				}
				
				return 0;
			} else {
				int len1 = vals1.length;
				int len2 = vals2.length;
				int minLen = len1 > len2 ? len2 : len1;
				
				for (int i = 0; i < minLen; ++i) {
					int result = Variant.compare(vals1[i], vals2[i], true);
					if (result != 0) {
						return result;
					}
				}
				
				return len1 == len2 ? 0 : (len1 > len2 ? 1 : -1);
			}
		}
	}

	/**
	 * �Ƚϼ�¼ָ���ֶε�ֵ
	 * @param fields �ֶ���������
	 * @param fvalues �ֶ�ֵ����
	 * @return 1����ǰ��¼��0����ȣ�-1����ǰ��¼С
	 */
	public abstract int compare(int []fields, Object []fvalues);
	
	/**
	 * ��ָ���ֶαȽϴ�С����¼�����ݽṹ������ͬ��
	 * @param r BaseRecord
	 * @param fields int[] �ֶ�����
	 * @return int
	 */
	public int compare(BaseRecord r, int []fields) {
		/*if (r == this) {
			return 0;
		} else if (r == null) {
			return 1;
		}*/

		for (int f : fields) {
			int result = Variant.compare(getNormalFieldValue(f), r.getNormalFieldValue(f), true);
			if (result != 0) {
				return result;
			}
		}
		
		return 0;
	}
	
	/**
	 * ��ָ���ֶαȽϴ�С����¼�����ݽṹ������ͬ��
	 * @param r BaseRecord
	 * @param field �ֶ�����
	 * @return int
	 */
	public int compare(BaseRecord r, int field) {
		return Variant.compare(getNormalFieldValue(field), r.getNormalFieldValue(field), true);
	}

	/**
	 * ���ֶ�ֵ�Ƚ�������¼�Ĵ�С����¼�����ݽṹ������ͬ
	 * @param r ��¼
	 * @return 1����ǰ��¼��0����ȣ�-1����ǰ��¼С
	 */
	public abstract int compare(BaseRecord r);
	
	/**
	 * �Ѽ�¼��ָ���ֶ���ָ�������Ԫ����Ƚ�
	 * @param field �ֶ�����
	 * @param values �ֶ�ֵ����
	 * @param index �ֶ�ֵ���������
	 * @return 1����ǰ��¼��0����ȣ�-1����ǰ��¼С
	 */
	public int compare(int field, IArray values, int index) {
		return Variant.compare(getFieldValue(field), values.get(index), true);
	}
	
	/**
	 * ���ؼ�¼�Ľṹ
	 * @return DataStruct
	 */
	public abstract DataStruct dataStruct();
	
	/**
	 * �����ֶε���Ŀ
	 * @return �ֶ���
	 */
	public abstract int getFieldCount();
	
	/**
	 * �����ֶε�������α�ֶδӷ�α�ֶε���Ŀ��ʼ����������ֶβ������򷵻�-1
	 * @param name �ֶ���
	 * @return �ֶ���������0��ʼ����
	 */
	public int getFieldIndex(String name) {
		return dataStruct().getFieldIndex(name);
	}

	/**
	 * ���ؼ�¼�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getFieldNames() {
		return dataStruct().getFieldNames();
	}

	/**
	 * ����ĳһ�ֶε�ֵ
	 * @param index �ֶ���������0��ʼ����
	 * @return Object
	 */
	public abstract Object getFieldValue(int index);

	/**
	 * ����ĳһ�ֶε�ֵ
	 * @param name �ֶ���
	 * @return Object
	 */
	public abstract Object getFieldValue(String name);
	
	/**
	 * ȡ�ֶ�ֵ���ֶβ����ڷ��ؿգ��˷���Ϊ��֧�ֽṹ����������
	 * @param index �ֶ���ţ���0��ʼ����
	 * @return Object
	 */
	public abstract Object getFieldValue2(int index);

	/**
	 * ���������ֶε�ֵ
	 * @return �ֶ�ֵ����
	 */
	public abstract Object[] getFieldValues();
	
	/**
	 * ȡ�ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @return Object
	 */
	public abstract Object getNormalFieldValue(int index);
	
	/**
	 * ȡ�ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @param out ���ڴ�Ž���������㹻�����������ж�
	 */
	public abstract void getNormalFieldValue(int index, IArray out);
	
	/**
	 * ����ָ���ֶε�����
	 * @param f �ֶ���������0��ʼ����
	 * @param len ���鳤��
	 * @return IArray
	 */
	public IArray createFieldValueArray(int f, int len) {
		return new ObjectArray(len);
	}
	
	/**
	 * ���������ڽṹ�е�������û�ж��������򷵻ؿ�
	 * @return ������������
	 */
	public int[] getPKIndex() {
		return dataStruct().getPKIndex();
	}
	
	/**
	 * ���ؼ�¼����������������ɵ����У�û���������쳣
	 * @return Object
	 */
	public Object getPKValue() {
		DataStruct ds = dataStruct();
		int []pkIndex = ds.getPKIndex();
		if (pkIndex == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.lessKey"));
		} else {
			int keyCount = pkIndex.length - ds.getTimeKeyCount();
			if (keyCount == 1) {
				Object obj = getNormalFieldValue(pkIndex[0]);
				if (obj instanceof BaseRecord) {
					return ((BaseRecord)obj).getPKValue();
				} else {
					return obj;
				}
			} else {
				Sequence keySeries = new Sequence(keyCount);
				for (int i = 0; i < keyCount; ++i) {
					Object obj = getNormalFieldValue(pkIndex[i]);
					if (obj instanceof BaseRecord) {
						obj = ((BaseRecord)obj).getPKValue();
					}

					if (obj instanceof Sequence) {
						keySeries.addAll((Sequence)obj);
					} else {
						keySeries.add(obj);
					}
				}
				
				return keySeries;
			}
		}
	}
	
	/**
	 * ���ؼ�¼��ֵr.v()
	 * @return ��������������򷵻�����ֵ�����򷵻������ֶι��ɵ�����
	 */
	public Object value() {
		// �������л��ᵼ����ѭ����
		// ָ���ֶθ�Ϊȡ������
		DataStruct ds = dataStruct();
		int []pkIndex = ds.getPKIndex();
		if (pkIndex == null) {
			int fcount = ds.getFieldCount();
			Sequence seq = new Sequence(fcount);
			for (int f = 0; f < fcount; ++f) {
				Object obj = getNormalFieldValue(f);
				if (obj instanceof BaseRecord) {
					obj = ((BaseRecord)obj).key();
				}

				if (obj instanceof Sequence) {
					seq.addAll((Sequence)obj);
				} else {
					seq.add(obj);
				}
			}
			
			return seq;
		} else {
			int keyCount = pkIndex.length - ds.getTimeKeyCount();
			if (keyCount == 1) {
				Object obj = getNormalFieldValue(pkIndex[0]);
				if (obj instanceof BaseRecord) {
					return ((BaseRecord)obj).key();
				} else {
					return obj;
				}
			} else {
				Sequence keySeries = new Sequence(keyCount);
				for (int i = 0; i < keyCount; ++i) {
					Object obj = getNormalFieldValue(pkIndex[i]);
					if (obj instanceof BaseRecord) {
						//obj = ((BaseRecord)obj).value();
						obj = ((BaseRecord)obj).key();
					}

					if (obj instanceof Sequence) {
						keySeries.addAll((Sequence)obj);
					} else {
						keySeries.add(obj);
					}
				}
				
				return keySeries;
			}
		}
	}
	
	/**
	 * ���ؼ�¼�Ƿ���ʱ���
	 * @return true����ʱ���
	 */
	public boolean hasTimeKey() {
		return dataStruct().getTimeKeyCount() > 0;
	}

	/**
	 * ���ֶ�ֵ�Ƚ�������¼�Ƿ���ȣ���¼�����ݽṹ������ͬ��
	 * @param r Ҫ�Ƚϵļ�¼
	 * @return boolean true�����
	 */
	public abstract boolean isEquals(BaseRecord r);
	
	/**
	 * �ж�����¼��ָ���ֶ��Ƿ����
	 * @param r Ҫ�Ƚϵļ�¼
	 * @param index �ֶ�����
	 * @return boolean true�����
	 */
	public abstract boolean isEquals(BaseRecord r, int []index);
	
	/**
	 * �Ż�ʱʹ�ã��ж����ڵļ�¼�����ݽṹ�Ƿ���ͬ
	 * @param cur
	 * @return
	 */
	public boolean isSameDataStruct(BaseRecord cur) {
		return dataStruct() == cur.dataStruct();
	}

	/**
	 * ���ؼ�¼����������������ɵ����У�û������ʱ���ؿ�
	 * @return Object
	 */
	public Object key() {
		DataStruct ds = dataStruct();
		int []pkIndex = ds.getPKIndex();
		if (pkIndex == null) {
			return null;
		} else {
			int keyCount = pkIndex.length - ds.getTimeKeyCount();
			if (keyCount == 1) {
				Object obj = getNormalFieldValue(pkIndex[0]);
				if (obj instanceof BaseRecord) {
					return ((BaseRecord)obj).key();
				} else {
					return obj;
				}
			} else {
				Sequence keySeries = new Sequence(keyCount);
				for (int i = 0; i < keyCount; ++i) {
					Object obj = getNormalFieldValue(pkIndex[i]);
					if (obj instanceof BaseRecord) {
						obj = ((BaseRecord)obj).key();
					}

					if (obj instanceof Sequence) {
						keySeries.addAll((Sequence)obj);
					} else {
						keySeries.add(obj);
					}
				}
				
				return keySeries;
			}
		}
	}

	/**
	 * �޸ļ�¼���ֶ�ֵ
	 * @param exps ֵ���ʽ����
	 * @param fields �ֶ�������
	 * @param ctx ����������
	 */
	public abstract void modify(Expression[] exps, String[] fields, Context ctx);
	
	/**
	 * ��Դ��¼���ֶ�ֵ������ǰ��¼
	 * @param sr Դ��¼
	 * @param isName �Ƿ��ֶ������и���
	 */
	public abstract void paste(BaseRecord sr, boolean isName);
	
	/**
	 * �����е�Ԫ�����θ�����ǰ��¼
	 * @param sequence ֵ����
	 */
	public abstract void paste(Sequence sequence);
	
	/**
	 * �����е�Ԫ�����θ�����ǰ��¼
	 * @param sequence ֵ����
	 * @param start ���е���ʼλ��
	 */
	public abstract void paste(Sequence sequence, int start);
	
	/**
	 * �Լ�¼��������ݹ��ѯ
	 * @param field ����ֶ���
	 * @param p ָ������ռ�¼
	 * @param maxLevel �������Ĳ��
	 * @return ���ü�¼���ɵ�����
	 */
	public Sequence prior(String field, BaseRecord p, int maxLevel) {
		int f = getFieldIndex(field);
		if (f == -1) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(field + mm.getMessage("ds.fieldNotExist"));
		}
		
		return prior(f, p, maxLevel);
	}
	
	/**
	 * �Լ�¼��������ݹ��ѯ
	 * @param f ����ֶ���ţ���0��ʼ����
	 * @param p ָ������ռ�¼
	 * @param maxLevel �������Ĳ��
	 * @return ���ü�¼���ɵ�����
	 */
	public Sequence prior(int f, BaseRecord p, int maxLevel) {
		if (this == p) {
			return new Sequence(0);
		}
		
		Sequence seq = new Sequence();
		BaseRecord r = this;
		if (maxLevel > 0) {
			for (int i = 0; i < maxLevel; ++i) {
				Object obj = r.getNormalFieldValue(f);
				if (obj == p) {
					seq.add(r);
					return seq;
				} else if (obj == null) {
					return null;
				} else if (obj instanceof BaseRecord) {
					seq.add(r);
					r = (BaseRecord)obj;
				} else {
					return null;
				}
			}
			
			return null;
		} else {
			while (true) {
				Object obj = r.getNormalFieldValue(f);
				if (obj == p) {
					seq.add(r);
					return seq;
				} else if (obj == null) {
					return null;
				} else if (obj instanceof BaseRecord) {
					seq.add(r);
					r = (BaseRecord)obj;
				} else {
					return null;
				}
			}
		}
	}
	
	/**
	 * ������ʽ
	 * @param exp ������ʽ
	 * @param ctx ����������
	 */
	public void run(Expression exp, Context ctx) {
		if (exp == null) {
			return;
		}

		ComputeStack stack = ctx.getComputeStack();
		stack.push(this);
		try {
			exp.calculate(ctx);
		} finally {
			stack.pop();
		}
	}

	/**
	 * ��Լ�¼������ʽ�����и�ֵ
	 * @param assignExps ��ֵ���ʽ����
	 * @param exps ֵ���ʽ����
	 * @param ctx ����������
	 */
	public void run(Expression[] assignExps, Expression[] exps, Context ctx) {
		if (exps == null || exps.length == 0) return;

		int colCount = exps.length;
		if (assignExps == null) {
			assignExps = new Expression[colCount];
		} else if (assignExps.length != colCount) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("run" + mm.getMessage("function.invalidParam"));
		}

		ComputeStack stack = ctx.getComputeStack();
		stack.push(this);
		try {
			for (int c = 0; c < colCount; ++c) {
				if (assignExps[c] == null) {
					exps[c].calculate(ctx);
				} else {
					assignExps[c].assign(exps[c].calculate(ctx), ctx);
				}
			}
		} finally {
			stack.pop();
		}
	}

	/**
	 * ����ָ���ֶε�ֵ
	 * @param index �ֶ���������0��ʼ����
	 * @param val �ֶ���ֵ
	 */
	public abstract void set(int index, Object val);

	/**
	 * ����ָ���ֶε�ֵ
	 * @param name �ֶ���
	 * @param val �ֶ���ֵ
	 */
	public abstract void set(String name, Object val);

	/**
	 * ��ָ����¼���ֶε�ֵ���������ǰ��¼����¼�ֶ�������ͬ
	 * @param r ��¼
	 */
	public abstract void set(BaseRecord r);
	
	/**
	 * �����ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @param val �ֶ�ֵ
	 */
	public abstract void setNormalFieldValue(int index, Object val);
	
	/**
	 * �����ֶ�ֵ������ֶβ����ڲ����κδ���
	 * @param index �ֶ���������0��ʼ������������������κδ���
	 * @param val �ֶ���ֵ
	 */
	public abstract void set2(int index, Object val);
	
	/**
	 * �������ֵ��ָ���ֶο�ʼ���������ǰ��¼
	 * @param index �ֶ���������0��ʼ����
	 * @param objs �ֶ�ֵ����
	 */
	public abstract void setStart(int index, Object []objs);
	
	/**
	 * �������ֵ��ָ���ֶο�ʼ���������ǰ��¼
	 * @param index �ֶ���������0��ʼ����
	 * @param objs �ֶ�ֵ����
	 * @param len ��ֵ���ֶ���
	 */
	public abstract void setStart(int index, Object []objs, int len);
	
	/**
	 * �Ѽ�¼��ֵ��ָ���ֶο�ʼ���������ǰ��¼
	 * @param index �ֶ���������0��ʼ����
	 * @param r ��¼
	 */
	public abstract void setStart(int index, BaseRecord r);
	
	/**
	 * ����ǰ��¼�Ŀ��ı����ֶ�ת���ִ�
	 * @param opt t����'\t'�ָ��ֶΣ�ȱʡ�ö��ţ�q������Ա����ʱ�������ţ�ȱʡ���ᴦ��
	 * f����ת��r���ֶ��������ֶ�ֵ
	 * @return String
	 */
	public abstract String toString(String opt);
	
	/**
	 * �ѵ�ǰ��¼ת��Record�͵ļ�¼�����������Record����ֱ�ӷ���
	 * @return Record
	 */
	public abstract Record toRecord();
}
