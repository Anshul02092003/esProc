package com.scudata.lib.math.prec;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.RQException;
import com.scudata.array.IArray;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * ��ֵ��һЩͳ����Ϣ
 * @author bd
 *
 */
public class NumStatis implements Externalizable {
	private static final long serialVersionUID = -5543191050053733230L;

	private double min = 0;
	
	//sd���������쳣ֵʱ�Ż��õ����ˣ����avg���ʹ�ã�����δ�غ�min��ͬһ��������
	private boolean hasSd = false;
	private double sd = 0;
	private double avg = 0;
	
	public NumStatis() {
	}
	
	// ���minValue���������ǰ���й���ƫ������Ҫ������Сֵ���������쳣ֵʱ����Сֵ�ǲ�Ӧ�ü�¼��
	public NumStatis(Sequence cvs, double minValue) {
		IArray mems = cvs.getMems();
		int size = mems.size();
		if (size < 1) {
			return;
		}
		Number result = null;
		Number minVal = null;
		int count = 0;
		int i = 1;
		for (; i <= size; ++i) {
			Object obj = mems.get(i);
			if (obj instanceof Number) {
				count++;
				result = (Number)obj;
				minVal = (Number)obj;
				break;
			}
		}

		for (++i; i <= size; ++i) {
			Object obj = mems.get(i);
			if (obj instanceof Number) {
				count++;
				result = Variant.addNum(result, (Number)obj);
				if (Variant.compare(obj, minVal, true) < 0) {
					minVal = (Number) obj;
				}
			}
		}

		this.avg = ((Number) Variant.avg(result, count)).doubleValue();
		this.min = minValue;
	}
	
	public NumStatis(Sequence cvs) {
		IArray mems = cvs.getMems();
		int size = mems.size();
		if (size < 1) {
			return;
		}
		Number result = null;
		Number minVal = null;
		int count = 0;
		int i = 1;
		for (; i <= size; ++i) {
			Object obj = mems.get(i);
			if (obj instanceof Number) {
				count++;
				result = (Number)obj;
				minVal = (Number)obj;
				break;
			}
		}

		for (++i; i <= size; ++i) {
			Object obj = mems.get(i);
			if (obj instanceof Number) {
				count++;
				result = Variant.addNum(result, (Number)obj);
				if (Variant.compare(obj, minVal, true) < 0) {
					minVal = (Number) obj;
				}
			}
		}

		this.avg = ((Number) Variant.avg(result, count)).doubleValue();
		this.min = minVal.doubleValue();
	}

	public double getMin() {
		return min;
	}
	
	/**
	 * �����ֵ���������ƫ�ȼ��㣬��ô��ֱ�ӻ�þ�ֵ�ͱ�׼��
	 * @param avg
	 * @param sd
	 */
	public void setAvgSd(double avg, double sd) {
		this.hasSd = true;
		this.avg = avg;
		this.sd = sd;
	}

	public double getAvg() {
		return avg;
	}

	public double getSd(Sequence cvs) {
		if (this.hasSd) {
			return sd;
		}
		else {
			calcSd(cvs);
			this.hasSd = true;
			return sd;
		}
	}
	
	protected void calcSd(Sequence cvs) {
		if (this.hasSd) {
			//���ռ�����rank�������ֵȫ�仯�ˣ�avg��sdҪ������
			Object avg = cvs.average();
			if (avg instanceof Number) {
				double avgValue = ((Number) avg).doubleValue();
				int n = cvs.length();
				double result = 0;
				for(int i = 1; i <= n; i++){
					Number tmp = (Number) cvs.get(i);
					double v = tmp == null ? 0 : tmp.doubleValue();
					if (tmp!=null){
						result+=Math.pow(v-avgValue, 2);
					}
				}
				this.avg = avgValue;
				this.sd = Math.sqrt(result / (n - 1));
			}
			else {
				this.avg = 0;
				this.sd = 0;
			}
		}
		else {
			//ʹ�ó�ʼֵ�������avg�Ѿ����ˣ����sd�ͺ�
			int n = cvs.length();
			double result = 0;
			for(int i = 1; i <= n; i++){
				Number tmp = (Number) cvs.get(i);
				double v = tmp == null ? 0 : tmp.doubleValue();
				if (tmp!=null){
					result+=Math.pow(v-avg, 2);
				}
			}
			this.sd = Math.sqrt(result / (n - 1));
		}
	}
	
	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(4);
		seq.add(this.min);
		seq.add(this.avg);
		int i = this.hasSd ? 1 : 0;
		seq.add(i);
		seq.add(this.sd);
		
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public void init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 4) {
			throw new RQException("Can't get the Number Statistics Record, invalid data.");
		}
		this.min = ((Number) seq.get(1)).doubleValue();
		this.avg = ((Number) seq.get(2)).doubleValue();
		this.hasSd = ((Number) seq.get(3)).intValue() == 1;
		this.sd = ((Number) seq.get(4)).doubleValue();
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 1;
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		this.min = in.readDouble();
		this.avg = in.readDouble();
		this.hasSd = in.readBoolean();
		this.sd = in.readDouble();
		if (ver > 1) {
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		
		out.writeDouble(this.min);
		out.writeDouble(this.avg);
		out.writeBoolean(this.hasSd);
		out.writeDouble(this.sd);
	}
}
