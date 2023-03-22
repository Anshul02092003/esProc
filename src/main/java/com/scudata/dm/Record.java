package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.array.IArray;
import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.IRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * ��¼�����࣬�ֶ�������0��ʼ����
 * @author WangXiaoJun
 *
 */
public class Record extends BaseRecord implements Externalizable, IRecord {
	private static final long serialVersionUID = 0x02010002;

	protected DataStruct ds;
	protected Object []values;

	// ���л�ʱʹ��
	public Record() {}

	/**
	 * �����¼�¼
	 * @param ds DataStruct ��¼�Ľṹ
	 */
	public Record(DataStruct ds) {
		this.ds = ds;
		values = new Object[ds.getFieldCount()];
	}

	/**
	 * �����¼�¼
	 * @param ds DataStruct ��¼�Ľṹ
	 * @param initVals Object[] ��ʼֵ
	 */
	public Record(DataStruct ds, Object []initVals) {
		this.ds = ds;
		values = new Object[ds.getFieldCount()];
		System.arraycopy(initVals, 0, values, 0, initVals.length);
	}
	
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
	 * ���ؼ�¼�Ľṹ
	 * @return DataStruct
	 */
	public DataStruct dataStruct() {
		return ds;
	}

	/**
	 * ���ü�¼�����ݽṹ
	 * @param ds
	 */
	public void setDataStruct(DataStruct ds) {
		this.ds = ds;
	}

	/**
	 * ���������ڽṹ�е�������û�ж��������򷵻ؿ�
	 * @return int[]
	 */
	public int[] getPKIndex() {
		return ds.getPKIndex();
	}
	
	/**
	 * �Ѽ�¼���л����ֽ�����
	 * @return �ֽ�����
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();

		int count = values.length;
		out.writeInt(count);
		Object []values = this.values;
		for (int i = 0; i < count; ++i) {
			out.writeObject(values[i], true);
		}

		out.writeRecord(ds);
		return out.toByteArray();
	}

	/**
	 * ���ֽ���������¼
	 * @param buf �ֽ�����
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);

		int count = in.readInt();
		Object []values = new Object[count];
		this.values = values;
		for (int i = 0; i < count; ++i) {
			values[i] = in.readObject(true);
		}
		
		if (in.available() > 0) {
			ds = new DataStruct();
			in.readRecord(ds);
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1); // �汾��
		out.writeObject(ds);
		out.writeObject(values);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte(); // �汾��
		ds = (DataStruct) in.readObject();
		values = (Object[]) in.readObject();
	}

	/**
	 * �����ֶε���Ŀ
	 * @return int
	 */
	public int getFieldCount() {
		return values.length;
	}

	/**
	 * ���ؼ�¼�����ֶ���
	 * @return String[]
	 */
	public String[] getFieldNames() {
		return dataStruct().getFieldNames();
	}

	/**
	 * �޸ļ�¼�����ݽṹ
	 * @param newDs �½ṹ
	 * @param newValues ���ֶ�ֵ
	 */
	void alter(DataStruct newDs, Object []newValues) {
		int newCount = newValues.length;
		if (values.length != newCount) {
			values = new Object[newCount];
		}

		System.arraycopy(newValues, 0, values, 0, newCount);
		ds = newDs;
	}

	/**
	 * �����ֶε�������α�ֶδӷ�α�ֶε���Ŀ��ʼ����������ֶβ������򷵻�-1
	 * @param name String
	 * @return int
	 */
	public int getFieldIndex(String name) {
		return dataStruct().getFieldIndex(name);
	}

	/**
	 * ���������ֶε�ֵ
	 * @return Object[]
	 */
	public Object []getFieldValues() {
		return values;
	}

	/**
	 * ����ĳһ�ֶε�ֵ
	 * @param index int �ֶ���������0��ʼ����
	 * @return Object
	 */
	public Object getFieldValue(int index) {
		if (index < 0) {
			int i = index + values.length;
			if (i < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(index + mm.getMessage("ds.fieldNotExist"));
			}
			
			return values[i];
		} else if (index < values.length) {
			return values[index];
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(index + 1 + mm.getMessage("ds.fieldNotExist"));
		}
	}
	
	// �ֶβ�����ʱ���ؿ�
	/**
	 * ȡ�ֶ�ֵ���ֶβ����ڷ��ؿգ��˷���Ϊ��֧�ֽṹ����������
	 * @param index �ֶ���ţ���0��ʼ����
	 * @return
	 */
	public Object getFieldValue2(int index) {
		if (index < 0) {
			int i = index + values.length;
			if (i >= 0) {
				return values[i];
			} else {
				return null;
			}
		} else if (index < values.length) {
			return values[index];
		} else {
			return null;
		}
	}

	/**
	 * ȡ�ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @return
	 */
	public Object getNormalFieldValue(int index) {
		return values[index];
	}
	
	/**
	 * ȡ�ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @param out ���ڴ�Ž���������㹻�����������ж�
	 */
	public void getNormalFieldValue(int index, IArray out) {
		out.push(values[index]);
	}

	/**
	 * �����ֶ�ֵ�������߽���
	 * @param index �ֶ���ţ���0��ʼ����
	 * @param val �ֶ�ֵ
	 */
	public void setNormalFieldValue(int index, Object val) {
		values[index] = val;
	}

	/**
	 * ����ĳһ�ֶε�ֵ
	 * @param name String �ֶ���
	 * @return Object
	 */
	public Object getFieldValue(String name) {
		int index = dataStruct().getFieldIndex(name);
		if (index != -1) {
			return values[index];
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
	}

	/**
	 * ����ĳһ�ֶε�ֵ��ֻ���Ƿ�α�ֶ�
	 * @param index int  �ֶ���������0��ʼ����
	 * @param val Object �ֶ���ֵ
	 */
	public void set(int index, Object val) {
		if (index < 0) {
			int i = index + values.length;
			if (i < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(index + mm.getMessage("ds.fieldNotExist"));
			}
			
			values[i] = val;
		} else if (index < values.length) {
			values[index] = val;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(index + 1 + mm.getMessage("ds.fieldNotExist"));
		}
	}

	/**
	 * �����ֶ�ֵ������ֶβ����ڲ����κδ���
	 * @param index
	 * @param val
	 */
	public void set2(int index, Object val) {
		if (index < 0) {
			int i = index + values.length;
			if (i >= 0) {
				values[i] = val;
			}
		} else if (index < values.length) {
			values[index] = val;
		}
	}

	/**
	 * ����ĳһ�ֶε�ֵ��ֻ���Ƿ�α�ֶ�
	 * @param name String �ֶ���
	 * @param val Object  �ֶ���ֵ
	 */
	public void set(String name, Object val) {
		int index = dataStruct().getFieldIndex(name);
		if (index != -1) {
			set(index, val);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
	}
	
	/**
	 * ���ֶ�ֵ�Ƚ�������¼�Ĵ�С
	 * @param r BaseRecord
	 * @return int
	 */
	public int compare(BaseRecord r) {
		if (r == this) {
			return 0;
		} else if (r == null) {
			return 1;
		}

		Object []vals1 = this.values;
		int len1 = vals1.length;
		int len2 = r.getFieldCount();
		int minLen = len1 > len2 ? len2 : len1;
		
		for (int i = 0; i < minLen; ++i) {
			int result = Variant.compare(vals1[i], r.getNormalFieldValue(i), true);
			if (result != 0) {
				return result;
			}
		}
		
		return len1 == len2 ? 0 : (len1 > len2 ? 1 : -1);
	}

	/**
	 * �Ƚ�ָ���ֶε�ֵ
	 * @param fields int[] �ֶ�����
	 * @param fvalues Object[] �ֶ�ֵ
	 * @return int
	 */
	public int compare(int []fields, Object []fvalues) {
		for (int i = 0, len = fields.length; i < len; ++i) {
			int result = Variant.compare(getFieldValue(fields[i]), fvalues[i], true);
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	/**
	 * ���ֶ�ֵ�Ƚ�������¼�Ƿ���ȣ���¼�����ݽṹ������ͬ��
	 * @param r BaseRecord
	 * @return boolean
	 */
	public boolean isEquals(BaseRecord r) {
		if (r == null)return false;
		if (r == this)return true;

		int count = values.length;
		Object[] vals = r.getFieldValues();
		if (vals.length != count) return false;

		for (int i = 0; i < count; ++i) {
			if (!Variant.isEquals(values[i], vals[i]))return false;
		}
		return true;
	}

	/**
	 * �ж�����¼��ָ���ֶ��Ƿ����
	 * @param r BaseRecord Ҫ�Ƚϵ��ֶ�
	 * @param index int[] �ֶ�����
	 * @return boolean
	 */
	public boolean isEquals(BaseRecord r, int []index) {
		Object[] vals = r.getFieldValues();
		for (int i = 0; i < index.length; ++i) {
			if (!Variant.isEquals(values[index[i]], vals[index[i]])) return false;
		}
		return true;
	}

	/**
	 * ��r�Ŀ��ı����ֶ�ת���ִ�
	 * @param opt String t����'\t'�ָ��ֶΣ�ȱʡ�ö��ţ�q������Ա����ʱ�������ţ�ȱʡ���ᴦ��
	 * f����ת��r���ֶ��������ֶ�ֵ
	 * @return String
	 */
	public String toString(String opt) {
		char sep = ',';
		boolean addQuotation = false, bTitle = false;
		if (opt != null) {
			if (opt.indexOf('t') != -1) sep = '\t';
			if (opt.indexOf('q') != -1) addQuotation = true;
			if (opt.indexOf('f') != -1) bTitle = true;
		}

		int fcount = getFieldCount();
		StringBuffer sb = new StringBuffer(20 * fcount);
		if (bTitle) {
			DataStruct ds = dataStruct();
			for (int f = 0; f < fcount; ++f) {
				if (f > 0) sb.append(sep);
				if (addQuotation) {
					sb.append('"');
					sb.append(ds.getFieldName(f));
					sb.append('"');
				} else {
					sb.append(ds.getFieldName(f));
				}
			}
		} else {
			boolean bFirst = true;
			Object []values = this.values;
			for (int f = 0; f < fcount; ++f) {
				Object obj = values[f];
				if (Variant.canConvertToString(obj)) {
					if (bFirst) {
						bFirst = false;
					} else {
						sb.append(sep);
					}

					if (addQuotation && obj instanceof String) {
						sb.append('"');
						sb.append((String)obj);
						sb.append('"');
					} else {
						sb.append(Variant.toString(obj));
					}
				}
			}
		}
		
		return sb.toString();
	}

	/**
	 * �Ż�ʱʹ�ã��ж����ڵļ�¼�����ݽṹ�Ƿ���ͬ
	 * @param cur
	 * @return
	 */
	public boolean isSameDataStruct(BaseRecord cur) {
		return ds == cur.dataStruct();
	}
	
	/**
	 * �޸ļ�¼���ֶ�ֵ
	 * @param exps ֵ���ʽ����
	 * @param fields �ֶ�������
	 * @param ctx ����������
	 */
	public void modify(Expression[] exps, String[] fields, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		stack.push(this);
		try {
			for (int f = 0, fcount = fields.length; f < fcount; ++f) {
				int findex = getFieldIndex(fields[f]);
				if (findex < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fields[f] + mm.getMessage("ds.fieldNotExist"));
				}

				setNormalFieldValue(findex, exps[f].calculate(ctx));
			}
		} finally {
			stack.pop();
		}
	}

	/**
	 * ��Լ�¼������ʽ
	 * @param exps Expression[] ������ʽ
	 * @param ctx Context
	 * @return Sequence
	 */
	public Sequence calc(Expression []exps, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		stack.push(this);
		try {
			int count = exps.length;
			Sequence seq = new Sequence(count);
			for (int i = 0; i < count; ++i) {
				seq.add(exps[i].calculate(ctx));
			}

			return seq;
		} finally {
			stack.pop();
		}
	}

	/**
	 * ������ʽ
	 * @param exp Expression ������ʽ
	 * @param ctx Context ���������Ļ���
	 */
	public void run(Expression exp, Context ctx) {
		if (exp == null) return;

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
	 * @param assignExps Expression[] ��ֵ���ʽ
	 * @param exps Expression[] ֵ���ʽ
	 * @param ctx Context
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
	 * �Ѽ�¼r�ĸ��ֶε�ֵ��������˼�¼����¼�ֶ�����ͬ
	 * @param r BaseRecord
	 */
	public void set(BaseRecord r) {
		Object[] vals = r.getFieldValues();
		System.arraycopy(vals, 0, values, 0, vals.length);
	}

	/**
	 * ���ֶ�index��ʼ�Ѽ�¼r�ĸ��ֶε�ֵ����˼�¼
	 * @param index int
	 * @param r BaseRecord
	 */
	public void setStart(int index, BaseRecord r) {
		Object[] vals = r.getFieldValues();
		System.arraycopy(vals, 0, values, index, vals.length);
	}

	/**
	 * ���ֶ�index��ʼ��objs��Ԫ�����������¼
	 * @param index �ֶ���������0��ʼ����
	 * @param objs �ֶ�ֵ����
	 */
	public void setStart(int index, Object []objs) {
		System.arraycopy(objs, 0, values, index, objs.length);
	}

	/**
	 * ���ֶ�index��ʼ��objs��Ԫ�����������¼
	 * @param index �ֶ���������0��ʼ����
	 * @param objs �ֶ�ֵ����
	 * @param len ��ֵ���ֶ���
	 */
	public void setStart(int index, Object []objs, int len) {
		System.arraycopy(objs, 0, values, index, len);
	}

	/**
	 * ���ؼ�¼��ֵr.v()
	 * @return ��������������򷵻�����ֵ�����򷵻������ֶι��ɵ�����
	 */
	public Object value() {
		// �������л��ᵼ����ѭ����
		// ָ���ֶθ�Ϊȡ������
		int []pkIndex = ds.getPKIndex();
		if (pkIndex == null) {
			Object []values = this.values;
			Sequence seq = new Sequence(values.length);
			for (Object obj : values) {
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
	 * ���ؼ�¼����������������ɵ����У�û������ʱ���ؿ�
	 * @return Object
	 */
	public Object key() {
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
	 * ���ؼ�¼����������������ɵ����У�û���������쳣
	 * @return Object
	 */
	public Object getPKValue() {
		int []pkIndex = ds.getPKIndex();
		if (pkIndex == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.lessKey"));
			//return null;
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
	 * �Ѽ�¼r���ֶ�ֵ�����˼�¼
	 * @param sr BaseRecord
	 * @param isName boolean �Ƿ��ֶ������и���
	 */
	public void paste(BaseRecord sr, boolean isName) {
		if (sr == null) return;
		Object[] vals = sr.getFieldValues();
		
		if (isName) {
			DataStruct ds = dataStruct();
			String []srcNames = sr.dataStruct().getFieldNames();
			for (int i = 0, count = srcNames.length; i < count; ++i) {
				int index = ds.getFieldIndex(srcNames[i]);
				if (index >= 0) {
					values[index] = vals[i];
				}
			}
		} else {
			int minCount = values.length > vals.length ? vals.length :
				values.length;
			System.arraycopy(vals, 0, values, 0, minCount);
		}
	}

	/**
	 * �����е�Ԫ�����θ����˼�¼
	 * @param series Sequence
	 */
	public void paste(Sequence series) {
		if (series == null) return;
		Object values[] = this.values;
		int fcount = series.length();
		if (fcount > values.length) fcount = values.length;

		for (int f = 0; f < fcount; ++f) {
			values[f] = series.get(f + 1);
		}
	}

	/**
	 * �����е�Ԫ�����θ����˼�¼
	 * @param series Sequence
	 * @param start int ���е���ʼλ��
	 */
	public void paste(Sequence series, int start) {
		Object values[] = this.values;
		int fcount = series.length() - start + 1;
		if (fcount > values.length) fcount = values.length;

		for (int f = 0; f < fcount; ++f) {
			values[f] = series.get(f + start);
		}
	}

	/**
	 * ����ֶ��Ƿ������ö���
	 * @return boolean true�������ö���false��û�����ö���
	 */
	public boolean checkReference() {
		Object []values = this.values;
		for (int i  = 0, len = values.length; i < len; ++i) {
			Object val = values[i];
			if (val instanceof BaseRecord || val instanceof Table) {
				return true;
			} else if (val instanceof Sequence) {
				if (((Sequence)val).hasRecord()) return true;
			}
		}

		return false;
	}
	
	/**
	 * �Լ�¼��������ݹ��ѯ
	 * @param field ����ֶ���
	 * @param p ָ������ռ�¼
	 * @param maxLevel �������Ĳ��
	 * @return ���ü�¼���ɵ�����
	 */
	public Sequence prior(String field, BaseRecord p, int maxLevel) {
		int f = ds.getFieldIndex(field);
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
	 * ���ؼ�¼�Ƿ���ʱ���
	 * @return true����ʱ���
	 */
	public boolean hasTimeKey() {
		return ds.getTimeKeyCount() > 0;
	}
	
	/**
	 * �ѵ�ǰ��¼ת��Record�͵ļ�¼�����������Record����ֱ�ӷ���
	 * @return Record
	 */
	public Record toRecord() {
		return this;
	}
}
