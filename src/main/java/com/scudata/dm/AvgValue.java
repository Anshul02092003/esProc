package com.scudata.dm;

import java.io.IOException;

import com.scudata.util.Variant;

/**
 * ���ڼ�¼����������ƽ��ֵ����ʱֵ
 * @author WangXiaoJun
 *
 */
public class AvgValue {
	private Object sumVal; // Ԫ�ػ���ֵ
	private int count; // Ԫ����������null
	
	public AvgValue() {
	}
	
	public AvgValue(Object val) {
		if (val != null) {
			this.sumVal = val;
			this.count = 1;
		}
	}

	/**
	 * ���Ԫ��
	 * @param val
	 */
	public void add(Object val) {
		if (val instanceof AvgValue) {
			AvgValue av = (AvgValue)val;
			sumVal = Variant.add(av.sumVal, sumVal);
			count += av.count;
		} else if (val != null) {
			sumVal = Variant.add(val, sumVal);
			count++;
		}
	}
	
	/**
	 * ȡƽ��ֵ
	 * @return
	 */
	public Object getAvgValue() {
		return Variant.avg(sumVal, count);
	}
	
	public void writeData(ObjectWriter out) throws IOException {
		out.writeObject(sumVal);
		out.writeInt(count);
	}
	
	public void readData(ObjectReader in) throws IOException {
		sumVal = in.readObject();
		count = in.readInt();
	}
}
