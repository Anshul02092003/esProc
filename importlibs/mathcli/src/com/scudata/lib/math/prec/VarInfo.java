package com.scudata.lib.math.prec;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.HashSet;

import com.scudata.dm.Sequence;
import com.scudata.util.Variant;
import com.scudata.lib.math.Sd;

/**
 * ��������������Ԥ������Ϣ
 * 
 * @author bd
 *
 */
public class VarInfo implements Externalizable {
	private static final long serialVersionUID = 5143312822863255779L;

	// ����������״̬
	// ��������
	public final static byte VAR_NORMAL = 0;
	// ����ΪID�����������봦��
	public final static byte VAR_DEL_ID = 1;
	// ��������ȱʧֵ���౻ɾ��
	public final static byte VAR_DEL_MISSING = 2;
	// ��������Ϊ��ֵ��ɾ��
	public final static byte VAR_DEL_SINGLE = 3;
	// ��������Ϊ��ֵ��ɾ��
	public final static byte VAR_DEL_WRONGTYPE = 4;
	// ��������ö��ֵ���౻ɾ��
	public final static byte VAR_DEL_CATEGORY = 11;
	// ����δͨ��T���鱻ɾ��
	public final static byte VAR_DEL_TTEST = 21;
	// ����δͨ���������鱻ɾ��
	public final static byte VAR_DEL_CHI_SQUARE = 22;
	// ����δͨ��SPEARMAN���鱻ɾ��
	public final static byte VAR_DEL_SPEARMAN = 23;
	// ����δͨ��PEARSON���鱻ɾ��
	public final static byte VAR_DEL_PEARSON = 24;

	// ��������������
	// ԭ����
	public final static byte TYPE_ORIGIN = 0;
	// MI����
	public final static byte TYPE_MI = 1;
	// MVP����
	public final static byte TYPE_MVP = 2;
	// BI����
	public final static byte TYPE_BI = 3;
	// ������������
	public final static byte TYPE_DERIVE = 4;

	// ������Ԥ�������������
	public final static String FILL_IMPUTE = "$YM_Auto_Impute$";

	// ������
	private String name;
	// ָ��ĳ�ʼ������
	private String srcName;
	// ��������
	private byte varType = TYPE_ORIGIN;

	// �������ͣ���ֵ����ֵ����ֵ��������
	private byte type = Consts.F_TWO_VALUE;
	// ����״̬
	private byte status = VAR_NORMAL;
	// ȱʧ��
	private double missingRate = 0d;

	// Ԥ�����������ȱֵ��Ϊnullʱ�������貹ȱ��ΪFILL_IMPUTEʱ����ʹ�������ܲ�ȱ
	private Object fillMissing = null;
	// Ԥ�����������ʱֻ��¼�����Ż�ȡ���С����ֵʹ�õ�����
	private Object fillOthers = null;
	// Ԥ�����������ʱֻ��¼�����Ż�ȡ������������ʹ�õķ�����
	private Sequence keepValues = null;

	// ��ֵ��ֵ����ֵ���ݵ�ָ��
	// �������
	private int category = 0;

	// ��ֵ���������ݵ�ָ��
	// ƫ��
	private double skewness0 = 0d;
	// ��ֵ
	private double average = 0d;
	// ��λ��
	private Number median = null;
	// ����
	private double variance = 0d;
	// Ԥ����������Ƿ�����ƽ��������
	private boolean ifSmooth = false;
	// Ԥ����������Ƿ�����ƽ�����������Ƿ������������
	private boolean ifSmoothDerive = false;
	// �����ƫ��
	private double skewness1 = 0d;
	// ��ƫ������
	private byte skewMode = SCRec.MODE_ORI;
	// ��ƫʹ�õ���
	private double skewP = 0d;
	// �����쳣ֵ��
	private int cleanCount = 0;

	// �����С����ֵ
	private Date minDate = null;
	private Date maxDate = null;

	/**
	 * ��ʼ�������л���
	 */
	public VarInfo() {
	}

	/**
	 * ��ʼ��������ԭʼ���������Լ���������
	 * @param srcName
	 * @param type
	 */
	public VarInfo(String srcName, byte type) {
		this.srcName = srcName;
		this.type = type;
	}

	/**
	 * ��ȡ������������
	 * @return the name
	 */
	public String getName() {
		if (this.name == null || this.name.trim().length() < 1) {
			return this.getSrcName();
		}
		return name;
	}

	/**
	 * ���ñ�����������
	 * @param name	the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ��ȡ����������ʼ
	 * @return the srcName
	 */
	public String getSrcName() {
		return srcName;
	}

	/**
	 * ���ñ���������ʼ
	 * @param srcName	the srcName to set
	 */
	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	/**
	 * ��ȡ��������
	 * @return the varType
	 */
	public byte getVarType() {
		return varType;
	}

	/**
	 * ���ñ�������
	 * @param varType	the varType to set
	 */
	public void setVarType(byte varType) {
		this.varType = varType;
	}

	/**
	 * ��ȡ��������
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * ������������
	 * @param type	the type to set
	 */
	public void setType(byte type) {
		this.type = type;
	}

	/**
	 * ��ȡ����״̬
	 * @return the status
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * ���ñ���״̬
	 * @param status	the status to set
	 */
	public void setStatus(byte status) {
		this.status = status;
	}

	/**
	 * ��ȡȱʧ��
	 * @return the missingRate
	 */
	public double getMissingRate() {
		return missingRate;
	}

	/**
	 * ����ȱʧ��
	 * @param missingRate	the missingRate to set
	 */
	public void setMissingRate(double missingRate) {
		this.missingRate = missingRate;
	}

	/**
	 * ��ȡȱʧ�ֵ
	 * @return the fillMissing
	 */
	public Object getFillMissing() {
		return fillMissing;
	}

	/**
	 * ��ȡȱʧ�ֵ
	 * @param fillMissing
	 *            the fillMissing to set
	 */
	public void setFillMissing(Object fillMissing) {
		// ���������ȱʧֵ������Ҳ��Ȼ��ִ�е������Ҫ�ж�
		if (this.fillMissing == null || this.fillMissing != FILL_IMPUTE) {
			this.fillMissing = fillMissing;
		}
	}

	/**
	 * ��ȡ�ͷ����ֵ
	 * @return the fillMissing
	 */
	public Object getFillOthers() {
		return this.fillOthers;
	}

	/**
	 * ���õͷ����ֵ
	 * 
	 * @param fillMissing
	 *            the fillMissing to set
	 */
	public void setFillOthers(Object o) {
		this.fillOthers = o;
	}

	/**
	 * ��ȡ�����������ֵ
	 * 
	 * @return the fillMissing
	 */
	public Sequence getKeepValues() {
		return this.keepValues;
	}

	/**
	 * ���÷����������ֵ
	 * 
	 * @param fillMissing
	 *            the fillMissing to set
	 */
	public void setKeepValues(Sequence seq) {
		this.keepValues = seq;
	}

	/**
	 * ��ȡ������
	 * 
	 * @return the category
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * ���÷�����
	 * 
	 * @param category
	 *            the category to set
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * ��ȡ��ʼƫ��
	 * 
	 * @return the skewness0
	 */
	public double getSkewness0() {
		return skewness0;
	}

	/**
	 * ���ó�ʼƫ��
	 * 
	 * @param skewness0
	 *            the skewness0 to set
	 */
	public void setSkewness0(double skewness0) {
		this.skewness0 = skewness0;
	}

	/**
	 * ��ȡ��ֵ
	 * 
	 * @return the average
	 */
	public double getAverage() {
		return average;
	}

	/**
	 * ���þ�ֵ
	 * 
	 * @param average
	 *            the average to set
	 */
	public void setAverage(double average) {
		this.average = average;
	}

	/**
	 * ��ȡ��λ��
	 * 
	 * @return the median
	 */
	public Number getMedian() {
		return this.median;
	}

	/**
	 * ������λ��
	 * 
	 * @param median
	 *            to set
	 */
	public void setMedian(Number med) {
		this.median = med;
	}

	/**
	 * ��ȡ����
	 * 
	 * @return the variance
	 */
	public double getVariance() {
		return variance;
	}

	/**
	 * ���÷���
	 * 
	 * @param variance
	 *            the variance to set
	 */
	public void setVariance(double variance) {
		this.variance = variance;
	}

	/**
	 * ��ȡ��ƫ��ƫ��
	 * 
	 * @return the skewness1
	 */
	public double getSkewness1() {
		return this.skewness1;
	}

	/**
	 * ���þ�ƫ��ƫ��
	 * 
	 * @param skewness1
	 *            the skewness1 to set
	 */
	public void setSkewness1(double skewness1) {
		this.skewness1 = skewness1;
	}

	/**
	 * ��ȡ��ƫ������
	 * 
	 * @return the skew mode
	 */
	public byte getSkewMode() {
		return this.skewMode;
	}

	/**
	 * ���þ�ƫ������
	 * 
	 * @param skew
	 *            mode
	 */
	public void setSkewMode(byte mode) {
		this.skewMode = mode;
	}

	/**
	 * ��ȡ��ƫʹ���ݣ�����ln�ȼ��㷵��0
	 * 
	 * @return the skew power
	 */
	public double getSkewP() {
		return this.skewP;
	}

	/**
	 * ���þ�ƫʹ����
	 * 
	 * @param skew
	 *            power
	 */
	public void setSkewP(double p) {
		this.skewP = p;
	}

	/**
	 * ��ȡ�����쳣ֵ��
	 * 
	 * @return
	 */
	public int getCleanCount() {
		return this.cleanCount;
	}

	/**
	 * ���������쳣ֵ��
	 * 
	 * @param count
	 */
	public void setCleanCount(int count) {
		this.cleanCount = count;
	}

	/**
	 * ��ȡ��Сֵ
	 * 
	 * @return the min
	 */
	public Object getMinDate() {
		return this.minDate;
	}

	/**
	 * ������Сֵ
	 * 
	 * @param min
	 *            the min to set
	 */
	public void setMinDate(Date min) {
		this.minDate = min;
	}

	/**
	 * ��ȡ���ֵ
	 * 
	 * @return the max
	 */
	public Date getMaxDate() {
		return this.maxDate;
	}

	/**
	 * �������ֵ
	 * 
	 * @param max
	 *            the max to set
	 */
	public void setMaxDate(Date max) {
		this.maxDate = max;
	}

	/**
	 * �Ƿ�����ƽ����
	 * 
	 * @return if smooth
	 */
	public boolean ifSmooth() {
		return this.ifSmooth;
	}

	/**
	 * �����Ƿ�����ƽ����
	 * @param b	�Ƿ�����ƽ����
	 */
	public void setIfSmooth(boolean b) {
		this.ifSmooth = b;
	}

	/**
	 * �Ƿ�����ƽ��������
	 * 
	 * @return if smooth
	 */
	public boolean ifSmoothDerive() {
		return this.ifSmoothDerive;
	}

	/**
	 * �����Ƿ�����ƽ��������
	 * @param b	�Ƿ�����ƽ����
	 */
	public void setIfSmoothDerive(boolean b) {
		this.ifSmoothDerive = b;
	}

	/**
	 * ���������ݳ�ʼ��ͳ����Ϣ
	 * 
	 * @param vs
	 */
	public void init(Sequence vs) {
		int size = vs.length();
		if (size < 1) {
			return;
		}
		if (this.type == Consts.F_ENUM || this.type == Consts.F_SINGLE_VALUE || this.type == Consts.F_TWO_VALUE) {
			// ö�������, ����ȱʧ�ʺͷ�����������ǿ�ֵ��
			int missing = 0;
			HashSet<Object> hs = new HashSet<Object>();

			for (int i = 1; i <= size; i++) {
				Object obj = vs.get(i);
				if (obj == null) {
					missing++;
				} else if (obj instanceof Number) {
					hs.add(obj);
				}
			}
			this.missingRate = missing * 1d / size;
			this.category = hs.size();
		} else if (this.type == Consts.F_NUMBER || this.type == Consts.F_COUNT) {
			// ��ֵ����
			Number result = null;
			int count = 0;
			int missing = 0;

			for (int i = 1; i <= size; i++) {
				Object obj = vs.get(i);
				if (obj == null) {
					missing++;
				} else if (obj instanceof Number) {
					count++;
					if (result == null) {
						result = (Number) obj;
					} else {
						result = Variant.addNum(result, (Number) obj);
					}
				}
			}
			this.missingRate = missing * 1d / size;
			//this.average = (Double) Variant.avg(result, count);
			this.average = ((Number) Variant.avg(result, count)).doubleValue();
			this.variance = Sd.sd(vs, this.average);
			this.median = VarInfo.getMedian(vs);
		} else if (this.type == Consts.F_COUNT) {
			// ��������
			Number result = null;
			int count = 0;
			int missing = 0;

			for (int i = 1; i <= size; i++) {
				Object obj = vs.get(i);
				if (obj == null) {
					missing++;
				} else if (obj instanceof Number) {
					count++;
					if (result == null) {
						result = (Number) obj;
					} else {
						result = Variant.addNum(result, (Number) obj);
					}
				}
			}
			this.missingRate = missing * 1d / size;
			this.average = (Double) Variant.avg(result, count);
			this.median = VarInfo.getMedian(vs);
		} else if (this.type == Consts.F_DATE) {
			// ���ڱ���
			int missing = 0;
			Date max = null;
			Date min = null;

			for (int i = 1; i <= size; i++) {
				Object obj = vs.get(i);
				if (obj == null) {
					missing++;
				} else if (obj instanceof Date) {
					if (max == null || Variant.compare(obj, max) > 0) {
						max = (Date) obj;
					}
					if (min == null || Variant.compare(obj, min) < 0) {
						min = (Date) obj;
					}
				}
			}
			this.missingRate = missing * 1d / size;
			this.maxDate = max;
			this.minDate = min;
		} else {
			// ID���������ı�����
			// �޲���
		}

	}

	/**
	 * ��ȡ��λ��
	 * @param cvs	��������ֵ���У�����
	 * @return
	 */
	private static Number getMedian(Sequence cvs) {
		int size = cvs == null ? 0 : cvs.length();
		if (size < 1) {
			return 0;
		}
		Sequence cloneSeq = new Sequence(cvs);
		return (Number) bfptr(cloneSeq, 1, size, size/2);
	}

	/**
	 * ��������left��right�����䷶Χ�ڵ�������������
	 * @param seq
	 * @param left
	 * @param right
	 */
	private static void insertSort(Sequence seq, int left, int right) {
		for (int i = left + 1; i <= right; i++) {
			if (Variant.compare(seq.get(i - 1), seq.get(i)) > 0) {
				Object t = seq.get(i);
				int j = i;
				while (j > left && Variant.compare(seq.get(j - 1), t) > 0) {
					seq.set(j, seq.get(j - 1));
					j--;
				}
				seq.set(j, t);
			}
		}
	}

	// Ѱ����λ������λ��
	private static Object findMid(Sequence seq, int left, int right) {
		if (left == right) {
			return seq.get(left);
		}
		int i = 0;
		int n = 0;
		// ��leftλ����ÿ5����Ա��������
		for (i = left; i < right - 5; i += 5) {
			insertSort(seq, i, i + 4);
			n = i - left;
			//���к���ÿһ�ε���λ����left��������
			swap(seq, left + n / 5, i + 2);
		}

		// ����ʣ��Ԫ��
		int num = right - i + 1;
		if (num > 0) {
			insertSort(seq, i, i + num - 1);
			n = i - left;
			swap(seq, left + n / 5, i + num / 2);
		}
		n /= 5;
		if (n == 1) {
			return seq.get(left);
		}
		return findMid(seq, left, left + n);
	}

	/**
	 * ���������е�������Ա
	 * @param seq
	 * @param i
	 * @param j
	 */
	private static void swap(Sequence seq, int i, int j) {
		Object o = seq.get(i);
		seq.set(i, seq.get(j));
		seq.set(j, o);
	}

	// Ѱ����λ��������λ��
	private static int findId(Sequence seq, int left, int right, Object num) {
		for (int i = left; i <= right; i++)
			if (Variant.compare(seq.get(i), num) == 0 ) {
				return i;
			}
		return -1;
	}

	// ���л��ֹ���
	private static int partion(Sequence seq, int left, int right, int p) {
		swap(seq, p, left);
		int i = left;
		int j = right;
		Object pivot = seq.get(left);
		while (i < j) {
			while (Variant.compare(seq.get(j), pivot) >= 0 && i < j) {
				j--;
			}
			seq.set(i, seq.get(j));
			while (Variant.compare(seq.get(i), pivot) <= 0 && i < j) {
				i++;
			}
			seq.set(j, seq.get(i));
		}
		seq.set(i, pivot);
		// ��ȵ����Ƚ϶�ʱ������һ�£���ֹ��ջ���
    	int num = right - i;
    	if (num > 1000) {
    		if (Variant.compare(seq.get(i+num/2), pivot) == 0) {
    			return i + num/2;
    		}
    		else if (Variant.compare(seq.get(i+num/8), pivot) == 0) {
    			return i + num/8;
    		}
    		else if (Variant.compare(seq.get(i+num/32), pivot) == 0) {
    			return i + num/32;
    		}
    	}
		return i;
	}

	private static Object bfptr(Sequence seq, int left, int right, int k) {
		Object num = findMid(seq, left, right); // Ѱ����λ������λ��
		int p = findId(seq, left, right, num); // �ҵ���λ������λ����Ӧ��id
		int i = partion(seq, left, right, p);

		int m = i - left + 1;
		if (m == k) {
			return seq.get(i);
		}
		if (m > k) {
			return bfptr(seq, left, i - 1, k);
		}
		return bfptr(seq, i + 1, right, k - m);
	}

	/*
	public static void main(String[] args) {
		Expression exp = new Expression("to(10000000).sort(rand())");
		Context ctx = new Context();
		Sequence seq = (Sequence) exp.calculate(ctx);
		long begin = System.currentTimeMillis();
		Object o = seq.median(0, 0);
		long cost = System.currentTimeMillis() - begin;
		System.out.println("1, Median is " + o.toString() + ", cost " + cost + " ms.");
		begin = System.currentTimeMillis();
		o = getMedian(seq);
		cost = System.currentTimeMillis() - begin;
		System.out.println("2, Median is " + o.toString() + ", cost " + cost + " ms.");
	}
	*/

	/************************* ���¼̳���Externalizable ************************/
	private byte version = 5;// 5��Ϊ��ʼֵ

	/**
	 * д���ݵ���
	 * 
	 * @param out
	 *            �����
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		out.writeObject(this.name);
		out.writeObject(this.srcName);
		out.writeByte(this.varType);
		out.writeByte(this.type);
		out.writeByte(this.status);
		out.writeDouble(this.missingRate);
		writeObject(out, this.fillMissing);
		writeObject(out, this.fillOthers);
		int size = this.keepValues == null ? 0 : this.keepValues.length();
		out.writeInt(size);
		for (int i = 1; i <= size; i++) {
			writeObject(out, this.keepValues.get(i));
		}

		out.writeInt(this.category);
		out.writeDouble(this.skewness0);
		out.writeDouble(this.average);
		writeObject(out, this.median);
		out.writeDouble(this.variance);

		out.writeBoolean(this.ifSmooth);
		out.writeDouble(this.skewness1);
		out.writeByte(this.skewMode);
		out.writeDouble(this.skewP);
		out.writeInt(this.cleanCount);

		if (this.minDate == null) {
			out.writeByte(0);
		}
		else {
			out.writeByte(1);
			out.writeLong(this.minDate.getTime());
		}
		if (this.maxDate == null) {
			out.writeByte(0);
		}
		else {
			out.writeByte(1);
			out.writeLong(this.maxDate.getTime());
		}
	}

	/**
	 * �����ж�����
	 * 
	 * @param in
	 *            ������
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte version = in.readByte();
		this.name = (String) in.readObject();
		this.srcName = (String) in.readObject();
		this.varType = in.readByte();
		this.type = in.readByte();
		this.status = in.readByte();
		this.missingRate = in.readDouble();
		this.fillMissing = readObject(in);
		this.fillOthers = readObject(in);
		int size = in.readInt();
		this.keepValues = new Sequence(size);
		for (int i = 1; i <= size; i++) {
			this.keepValues.add(readObject(in));
		}

		this.category = in.readInt();
		this.skewness0 = in.readDouble();
		this.average = in.readDouble();
		this.median = (Number) readObject(in);
		this.variance = in.readDouble();

		this.ifSmooth = in.readBoolean();
		this.skewness1 = in.readDouble();
		this.skewMode = in.readByte();
		this.skewP = in.readDouble();
		this.cleanCount = in.readInt();

		byte b = in.readByte();
		if (b > 0) {
			this.minDate = new Date(in.readLong());
		}
		b = in.readByte();
		if (b > 0) {
			this.maxDate = new Date(in.readLong());
		}

		if (version > 5) {
		}
	}

	/**
	 * ����д���������͵Ķ���
	 * 
	 * @throws IOException
	 */
	private void writeObject(ObjectOutput out, Object obj) throws IOException {
		if (obj == null) {
			out.writeByte(0);
		} else if (obj.equals(VarInfo.FILL_IMPUTE)) {
			out.writeByte(1);
		} else if (obj instanceof Date) {
			out.writeByte(2);
			out.writeLong(((Date) obj).getTime());
		} else if (obj instanceof Integer) {
			out.writeByte(3);
			out.writeInt((Integer) obj);
		} else if (obj instanceof Number) {
			out.writeByte(4);
			out.writeDouble(((Number) obj).doubleValue());
		} else if (obj instanceof String) {
			out.writeByte(5);
			out.writeObject(obj.toString());
		} else {
			// ������ȱֵ���п��ܶ�д�쳣
			out.writeByte(255);
			out.writeObject(obj);
		}
	}

	private Object readObject(ObjectInput in) throws IOException, ClassNotFoundException {
		byte type = in.readByte();
		if (type == 0) {
			return null;
		} else if (type == 1) {
			return VarInfo.FILL_IMPUTE;
		} else if (type == 2) {
			return new Date(in.readLong());
		} else if (type == 3) {
			return in.readInt();
		} else if (type == 4) {
			return in.readDouble();
		} else if (type == 5) {
			return in.readObject().toString();
		} else {
			return in.readObject();
		}
	}
}
