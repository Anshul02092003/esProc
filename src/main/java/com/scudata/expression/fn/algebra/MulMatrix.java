package com.scudata.expression.fn.algebra;

import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.common.Logger;
import com.scudata.common.RQException;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;

/**
 * ��ά�����ṩ��ά������, ������ദ�����Դ��������еĶ�ά����
 * @author bidalong
 *
 */
public class MulMatrix {

	private Number[][] A;
	private ArrayList<MulMatrix> mtx;
	private int[] index;
	
	protected MulMatrix() {
	}
	
	/**
	 * �������еĶ�ά�����½���ά����
	 * @param mtx
	 */
	protected MulMatrix(Number[][] A, int size) {
		this.A = A;
		if (size != 0) {
			// ������1ά����
			this.index = new int[1];
			this.index[0] = size;
		}
		else {
			this.index = new int[2];
			this.index[0] = A.length;
			this.index[1] = A[0].length;
		}
	}
	
	/**
	 * �������еĶ�ά����List������ά����
	 * @param mtx
	 */
	protected MulMatrix(ArrayList<MulMatrix> mtx) {
		this.mtx = mtx;
		MulMatrix mm = mtx.get(0);
		this.index = new int[mm.index.length + 1];
		System.arraycopy(mm.index, 0, this.index, 1, mm.index.length);
		this.index[0] = mtx.size();
	}
	
	/**
	 * ��ʼ��һ����ά�������matrix�����г��Ȳ��ȣ�������󳤶�����������������0����ά�������ҲҪ��1ά���������������ת��Ϊn��1�д洢
	 * @param matrix
	 */
	protected MulMatrix(Sequence seq) {
		int rows = seq == null ? 0 : seq.length();
		if (rows > 0) {
			if (seq instanceof Table) {
				// ��ά����, ���
				Table tab = (Table) seq;
				this.setA(tab);
				return;
			}
			else {
				int[] check = null;
				for (int r = 0; r < rows; r++ ) {
					Object row = seq.get(r+1);
					if (row instanceof Sequence) {
						Object o1 = ((Sequence) row).get(1);
						if (o1 instanceof Sequence) {
							// ��ά���ϵ�����
							MulMatrix matrix = new MulMatrix((Sequence) row);
							int[] idx = matrix.index;
							if (this.index == null) {
								// ���и�ֵ
								this.index = new int[idx.length+1];
								System.arraycopy(idx, 0, this.index, 1, idx.length);
								this.index[0] = rows;
								this.mtx = new ArrayList<MulMatrix>(rows);
								check = idx;
							}
							else {
								// ������Ҫ������ṹ���׸���ȫƥ�䣬���򱨴�
								if (this.getLevel() != matrix.getLevel() + 1) {
									throw new RQException("The dimensions of Multi-Matrix is unmatched! ");
								}
								else if( !Arrays.equals(check, idx)) {
									throw new RQException("The dimensions of Multi-Matrix is unmatched! ");
								}
							}
							this.mtx.add(matrix);
						}
						else {
							// ��ά����
							if (this.index == null) {
								this.setA(seq);
								return;
							}
							else {
								throw new RQException("The dimensions of Multi-Matrix is unmatched! ");
							}
						}
					}
					else {
						// ��������Ϊ�������ݴ洢����ά��������Ƕ�ά
						this.setA(seq);
					}
				}
			}
		}
	}
	
	// �����ж����ά��һά����
	private void setA(Sequence seq) {
		int rows = seq == null ? 0 : seq.length();
		if (rows > 0) {
			if (seq instanceof Table) {
				// ��ά����, ���
				Table tab = (Table) seq;
				int cols = tab.dataStruct().getFieldCount();
				this.A = new Number[rows][cols];
				this.index = new int[2];
				this.index[0] = rows;
				this.index[1] = cols;
				for (int i = 1; i <= rows; i++) {
					Number[] row = getRow(tab.getRecord(i), cols);
					this.A[i-1] = row;
				}
			}
			else {
				int cols = 0;
				for (int r = 0; r < rows; r++ ) {
					Object row = seq.get(r+1);
					if (row instanceof Sequence) {
						int cols2 = ((Sequence) row).length();
						if (cols < cols2) {
							cols = cols2;
						}
					}
				}
				if (cols == 0) {
					// ��һ���У���������һά����
					this.index = new int[1];
					this.index[0] = rows;
					cols = 1;
					this.A = new Number[rows][1];
					for (int r = 0; r < rows; r++) {
						Object obj = ((Sequence) seq).get(r+1);
						if (obj instanceof Number) {
							this.A[r][0] = (Number) obj;
						}
					}
				}
				else {
					this.A = new Number[rows][cols];
					this.index = new int[2];
					this.index[0] = rows;
					this.index[1] = cols;
					for (int r = 0; r < rows; r++) {
						Object row = seq.get(r+1);
						if (row instanceof Sequence) {
							int cols2 = ((Sequence) row).length();
							for (int c = 0; c < cols2; c++) {
								Object obj = ((Sequence) row).get(c+1);
								if (obj instanceof Number) {
									this.A[r][c] = (Number) obj;
								}
							}
						}
						else if (row instanceof Number) {
							this.A[r][0] = (Number) row;
						}
					}
				}
			}
		}
	}
	
	private Number[] getRow(Record rec, int n) {
		Number[] row = new Number[n];
		Object[] vs = rec.getFieldValues();
		for (int i = 0; i <n; i++) {
			Object obj = vs[i];
			row[i] = getNumber(obj);
		}
		return row;
	}
	
	private static Number getNumber(Object obj) {
		double d = 0;
		if (obj instanceof Sequence ) {
			if (((Sequence) obj).length() == 0) {
				return null;
			}
			else {
				obj = ((Sequence) obj).get(1);
			}
		}
		if (obj instanceof Number) {
			return ((Number) obj);
		}
		else if (obj instanceof String) {
			d = Double.valueOf(obj.toString());
		}
		return Double.valueOf(d);
	}
	
	/**
	 * ���ÿһ�����ĳ�Ա��
	 * @return
	 */
	protected int[] getIndex() {
		return this.index;
	}
	
	/**
	 * ��ò���
	 * @return
	 */
	protected int getLevel() {
		return this.index.length;
	}
	
	// ��ȡ�ײ�����е���
	private Number getNumber(int r, int c) {
		return this.A[r][c];
	}
	
	public Sequence toSequence() {
		int level = this.getLevel();
		if (level < 1) {
			return null;
		}
		int rows = this.index[0];
		Sequence seq = new Sequence(rows);
		if (level == 1) {
			// ����
			for (int i = 0; i < rows; i++ ) {
				if (this.A.length > 1) {
					seq.add(this.A[i][0]);
				}
				else {
					seq.add(this.A[0][i]);
				}
			}
			if (this.A.length == 1 && this.index[0] > 1) {
				// ������
				Sequence rSeq = new Sequence(1);
				rSeq.add(seq);
				return rSeq;
			}
		}
		else if (level == 2) {
			// ����
			int cols = this.index[1];
			for (int i = 0; i < rows; i++ ) {
				Sequence row = new Sequence(cols);
				for (int j = 0; j < cols; j++ ) {
					row.add(this.A[i][j]);
				}
				seq.add(row);
			}
		}
		else {
			// ��ά����
			for (int i = 0; i < rows; i++ ) {
				MulMatrix mm = this.mtx.get(i);
				seq.add(mm.toSequence());
			}
		}
		return seq;
	}
	
	/**
	 * ��ĳ��ά����ͣ����ص�һ���Ķ�ά���󣬶�����������ֵ
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @param ifNull	�Ƿ����ֵ��Ĭ��false����ֵ��0����
	 * @return
	 */
	protected Object sum(int level, boolean ifNull) {
		int lm = this.getLevel();
		if (level > lm || level < 0) {
			return this;
		}
		if (level == 0) {
			level = autoLevel();
		}
		int len = this.index[level-1];
		if (lm == 1) {
			// �������õ���ֵ
			double sum = 0;
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
			}
			return sum;
		}
		MulMatrix mm = null; 
		if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			if (level == 1) {
				// ���оۼ����õ���ά���У���ֻ��һ��
				int size = this.index[1];
				Number[][] result = new Number[1][size];
				for (int c = 0; c < size; c++) {
					double sum = 0;
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
						}
					}
					result[0][c] = sum;
				}
				mm = new MulMatrix(result, size);
			}
			else {
				// ���оۼ�
				int size = this.index[0];
				Number[][] result = new Number[size][1];
				for (int r = 0; r < size; r++) {
					double sum = 0;
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
						}
					}
					result[r][0] = sum;
				}
				mm = new MulMatrix(result, size);
			}
		}
		else if (lm > 3 || lm - level >= 2) {
			// ��ײ�洢���ñ仯�ṹ��ֱ�ӾۺϾͿ�����
			if (level == 1) {
				// �ײ�ۺ�
				mm = this.mtx.get(0).create(ifNull);
				for (MulMatrix submm : this.mtx) {
					submm.sum2(mm);
				}
			}
			else {
				ArrayList<MulMatrix> mtxNew = new ArrayList<MulMatrix>(this.index[0]);
				for (MulMatrix submm : this.mtx) {
					MulMatrix subnew = (MulMatrix) submm.sum(level - 1, ifNull);
					mtxNew.add(subnew);
				}
				mm = new MulMatrix(mtxNew);
			}
		}
		else {
			// Ҫ����������ײ������
			// ��ʣ��3�㣬������ײ������ʵ�ʲ�����ֻ�迼��levelΪ2��3�������Ϊ1ʱ�ǲ���Ҫ������ײ������
			Number[][] Anew = null;
			int rows = this.index[0];
			if (level == 2) {
				Anew = new Number[rows][this.index[2]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[2]; c++) {
						double sum = 0;
						for (int i = 0, iSize = this.index[1]; i < iSize; i++) {
							if (submm.A[i][c] == null) {
								continue;
							}
							sum += submm.A[i][c].doubleValue();
						}
						Anew[r][c] =  sum;
					}
				}
			}
			else {
				// levelΪ3
				Anew = new Number[rows][this.index[1]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[1]; c++) {
						double sum = 0;
						for (int i = 0, iSize = this.index[2]; i < iSize; i++) {
							if (submm.A[c][i] == null) {
								continue;
							}
							sum += submm.A[c][i].doubleValue();
						}
						Anew[r][c] =  sum;
					}
				}
			}
			// ���ؾۺϽ����ά����������ʱ�賤��0
			mm = new MulMatrix(Anew, 0);
		}
		return mm;
	}
	
	/**
	 * ������ά���ݼ��ۺϵ���һ����ά���ݼ��ۺϴ洢�Ľ��
	 * @param mm
	 */
	private void sum2(MulMatrix mm) {
		if(this.getLevel() != mm.getLevel()) {
			//ֻ��ͬά�Ż�ۺ�
			return;
		}
		if (this.getLevel() <= 2) {
			// ��ײ����ʵ��ִ�оۺ�
			int rows = this.index[0];
			int cols = this.index[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (this.A[r][c] == null) {
						continue;
					}
					double sum = 0;
					if (mm.A[r][c] != null) {
						sum = mm.A[r][c].doubleValue();
					}
					sum += this.A[r][c].doubleValue();
					mm.A[r][c] = sum;
				}
			}
		}
		else {
			// ��ά����ۺ�
			int size = this.mtx.size();
			for (int i = 0; i < size; i++ ) {
				MulMatrix submm = this.mtx.get(i);
				MulMatrix submm2 = mm.mtx.get(i);
				submm.sum2(submm2);
			}
		}
	}
	
	/**
	 * ������ά���������һ����ֵ���󣬼����ۼӵ��������mm
	 * @param mm
	 */
	private void calcVar1(MulMatrix mm, MulMatrix avgmm) {
		if(this.getLevel() != mm.getLevel()) {
			//ֻ��ͬά���ܼ���
			return;
		}
		if (this.getLevel() <= 2) {
			// ��ײ����ʵ��ִ�оۺ�
			int rows = this.index[0];
			int cols = this.index[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (this.A[r][c] == null) {
						continue;
					}
					double var = 0;
					if (mm.A[r][c] != null) {
						var = mm.A[r][c].doubleValue();
					}
					double d = this.A[r][c].doubleValue();
					double avg = avgmm.A[r][c].doubleValue();
					var += (d-avg)*(d-avg);
					mm.A[r][c] = var;
				}
			}
		}
		else {
			// ��ά����ۺ�
			int size = this.mtx.size();
			for (int i = 0; i < size; i++ ) {
				MulMatrix submm = this.mtx.get(i);
				MulMatrix submm2 = mm.mtx.get(i);
				MulMatrix avgmm2 = avgmm.mtx.get(i);
				submm.calcVar1(submm2, avgmm2);
			}
		}
	}
	
	/**
	 * ������ά���������һ����ֵ����ͱ�׼�����ִ�й�һ������
	 * @param mm
	 */
	private void normalize(MulMatrix avgmm, MulMatrix stdmm) {
		if(this.getLevel() != avgmm.getLevel()) {
			//ֻ��ͬά���ܼ���
			return;
		}
		if (this.getLevel() <= 2) {
			// ��ײ����ʵ��ִ�оۺ�
			int rows = this.index[0];
			int cols = this.index[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (this.A[r][c] == null) {
						continue;
					}
					double d = this.A[r][c].doubleValue();
					double avg = avgmm.A[r][c].doubleValue();
					double std = stdmm.A[r][c].doubleValue();
					this.A[r][c] = (d-avg)/std;
				}
			}
		}
		else {
			// ��ά����ۺ�
			int size = this.mtx.size();
			for (int i = 0; i < size; i++ ) {
				MulMatrix submm = this.mtx.get(i);
				MulMatrix avgmm2 = avgmm.mtx.get(i);
				MulMatrix stdmm2 = stdmm.mtx.get(i);
				submm.normalize(avgmm2, stdmm2);
			}
		}
	}
	
	/**
	 * ����
	 * @param mm
	 */
	protected double sd(double avg) {
		int lm = this.getLevel();
		int len = this.index[0];
		double sd = 0;
		if (lm == 1) {
			// �������õ���ֵ
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						double v = num.doubleValue();
						sd += (v-avg)*(v-avg);
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						double v = num.doubleValue();
						sd += (v-avg)*(v-avg);
					}
				}
			}
		}
		else if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			int size = this.index[1];
			for (int r = 0; r < len; r++) {
				for (int c = 0; c < size; c++) {
					Number num = this.A[r][c];
					if (num != null) {
						double v = num.doubleValue();
						sd += (v-avg)*(v-avg);
					}
				}
			}
		}
		else {
			for (int r = 0; r < len; r++) {
				MulMatrix submm = this.mtx.get(r);
				sd += submm.sd(avg);
			}
		}
		return sd;
	}
	
	/**
	 * ȫ����ͣ����ص�һ���Ķ�ά���󣬶�����������ֵ
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @param ifNull	�Ƿ����ֵ��Ĭ��false����ֵ��0����
	 * @return
	 */
	protected double sumAll() {
		int lm = this.getLevel();
		int len = this.index[0];
		double sum = 0;
		if (lm == 1) {
			// �������õ���ֵ
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
			}
		}
		else if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			int size = this.index[1];
			for (int r = 0; r < len; r++) {
				for (int c = 0; c < size; c++) {
					Number num = this.A[r][c];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
			}
		}
		else {
			for (int r = 0; r < len; r++) {
				MulMatrix submm = this.mtx.get(r);
				sum += submm.sumAll();
			}
		}
		return sum;
	}
	
	/**
	 * ��ĳ��ά���ۻ���ͣ�����ͬά����
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @param reverse	�Ƿ������ۻ�
	 * @return
	 */
	protected MulMatrix cumsum(int level, boolean reverse) {
		int lm = this.getLevel();
		if (level > lm || level < 0) {
			return this;
		}
		if (level == 0) {
			level = autoLevel();
		}
		int len = this.index[level-1];
		MulMatrix mm = null; 
		if (lm == 1) {
			// �������õ���ֵ
			Number[][] A2 = this.A.clone();
			double sum = 0;
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					int r = i;
					if (reverse) {
						r = len - i - 1;
					}
					Number num = this.A[r][0];
					if (num != null) {
						sum += num.doubleValue();
						A2[r][0] = sum;
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					int c = i;
					if (reverse) {
						c = len - i - 1;
					}
					Number num = this.A[0][c];
					if (num != null) {
						sum += num.doubleValue();
						A2[0][c] = sum;
					}
				}
			}
			mm = new MulMatrix(A2, len);
		}
		else if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			if (level == 1) {
				// ���оۼ����õ���ά���У���ֻ��һ��
				int size = this.index[1];
				Number[][] result = new Number[len][size];
				for (int c = 0; c < size; c++) {
					double sum = 0;
					for (int r = 0; r < len; r++) {
						int i = r;
						if (reverse) {
							i = len - r - 1;
						}
						Number num = this.A[i][c];
						if (num != null) {
							sum += num.doubleValue();
							result[i][c] = sum;
						}
					}
				}
				mm = new MulMatrix(result, 0);
			}
			else {
				// ���оۼ�
				int size = this.index[0];
				Number[][] result = new Number[size][len];
				for (int r = 0; r < size; r++) {
					double sum = 0;
					for (int c = 0; c < len; c++) {
						int i = c;
						if (reverse) {
							i = len - c - 1;
						}
						Number num = this.A[r][i];
						if (num != null) {
							sum += num.doubleValue();
							result[r][i] = sum;
						}
					}
				}
				mm = new MulMatrix(result, 0);
			}
		}
		else {
			// ��ײ�洢���ñ仯�ṹ��ֱ�ӾۺϾͿ�����
			ArrayList<MulMatrix> mtx2 = new ArrayList<MulMatrix>(this.index[0]);
			if (level == 1) {
				// �ײ�ۺ�
				MulMatrix mm2 = this.mtx.get(0).create(false);
				int size = this.mtx.size();
				for (int i = 0; i < size; i++) {
					int j = i;
					if (reverse) {
						j = size - i - 1;
					}
					MulMatrix submm = this.mtx.get(j);
					submm.sum2(mm2);
					MulMatrix submm2 = mm2.deepClone();
					if (reverse) {
						mtx2.set(j, submm2);
					}
					else {
						mtx2.add(submm2);
					}
				}
			}
			else {
				for (MulMatrix submm : this.mtx) {
					MulMatrix subnew = submm.cumsum(level - 1, reverse);
					mtx2.add(subnew);
				}
			}
			mm = new MulMatrix(mtx2);
		}
		return mm;
	}
	
	/**
	 * ��ĳ��ά�ȹ�һ������ͬά����
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @param s	�Ƿ�ʹ��n-1
	 * @return
	 */
	protected MulMatrix normalize(int level, boolean s) {
		int lm = this.getLevel();
		if (level > lm || level < 0) {
			return this;
		}
		if (level == 0) {
			level = autoLevel();
		}
		int len = this.index[level-1];
		MulMatrix mm = null; 
		if (lm == 1) {
			// �������õ���ֵ
			Number[][] A2 = this.A.clone();
			double sum = 0;
			double sd = 0;
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
				double avg = sum/len;
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						double v = num.doubleValue();
						sd += (v-avg)*(v-avg);
					}
				}
				double std = calcStd(sd, len, s);
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						double v = num.doubleValue();
						A2[i][0] = (v-avg)/std;
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
				double avg = sum/len;
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						double v = num.doubleValue();
						sd += (v-avg)*(v-avg);
					}
				}
				double std = calcStd(sd, len, s);
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						double v = num.doubleValue();
						A2[0][i] = (v-avg)/std;
					}
				}
			}
			mm = new MulMatrix(A2, len);
		}
		else if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			if (level == 1) {
				// ���оۼ����õ���ά���У���ֻ��һ��
				int size = this.index[1];
				Number[][] result = new Number[len][size];
				for (int c = 0; c < size; c++) {
					double sum = 0;
					double sd = 0;
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
							result[r][c] = sum;
						}
					}
					double avg = sum/len;
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							double v = num.doubleValue();
							sd += (v-avg)*(v-avg);
						}
					}
					double std = calcStd(sd, len, s);
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							double v = num.doubleValue();
							result[r][c] = (v-avg)/std;
						}
					}
				}
				mm = new MulMatrix(result, 0);
			}
			else {
				// ���оۼ�
				int size = this.index[0];
				Number[][] result = new Number[size][len];
				for (int r = 0; r < size; r++) {
					double sum = 0;
					double sd = 0;
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
						}
					}
					double avg = sum/len;
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							double v = num.doubleValue();
							sd += (v-avg)*(v-avg);
						}
					}
					double std = calcStd(sd, len, s);
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							double v = num.doubleValue();
							result[r][c] = (v-avg)/std;
						}
					}
				}
				mm = new MulMatrix(result, 0);
			}
		}
		else {
			// ��ײ�洢���ñ仯�ṹ��ֱ�ӾۺϾͿ�����
			ArrayList<MulMatrix> mtx2 = new ArrayList<MulMatrix>(this.index[0]);
			if (level == 1) {
				// �ײ�ۺ�
				mm = this.mtx.get(0).create(false);
				int size = this.mtx.size();
				for (MulMatrix submm : this.mtx) {
					submm.sum2(mm);
				}
				// ��ʱmm����ײ��¼����size��MulMatrix�ۼ���͵Ľ�������������ֵ
				mm.divide(size);
				MulMatrix avgmm = mm;
				mm = this.mtx.get(0).create(false);
				// �þ�ֵ��
				for (MulMatrix submm : this.mtx) {
					submm.calcVar1(mm, avgmm);
				}
				mm.calcVar2(size, s);
				for (MulMatrix submm : this.mtx) {
					submm.normalize(avgmm, mm);
				}
				return this;
			}
			else {
				for (MulMatrix submm : this.mtx) {
					MulMatrix subnew = submm.normalize(level - 1, s);
					mtx2.add(subnew);
				}
			}
			mm = new MulMatrix(mtx2);
		}
		return mm;
	}
	
	protected int autoLevel() {
		int level = 0;
		int lm = this.getLevel();
		for (;level<lm; level++) {
			if (this.index[level]>1) {
				break;
			}
		}
		level = level+1;
		return level;
	}
	
	/**
	 * ��ĳ��ά������Ԫ��������ʱ��֧�ֿ�ֵ����
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @return
	 */
	protected double count(int level) {
		int lm = this.getLevel();
		if (level > lm || level < 1) {
			return 1;
		}
		int len = this.index[level-1];
		return len;
	}
	
	/**
	 * ��ĳ��ά������Ԫ��������ʱ��֧�ֿ�ֵ����
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @return
	 */
	protected double countAll() {
		int lm = this.getLevel();
		double result = 1d;
		for (int level = 0;level<lm; level++) {
			result = result * this.index[level];
		}
		return result;
	}
	
	protected void divide(double d) {
		int lm = this.getLevel();
		if (lm == 1) {
			// �������õ���ֵ
			int len = this.index[0];
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						this.A[i][0] = num.doubleValue()/d;
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						this.A[0][i] = num.doubleValue()/d;
					}
				}
			}
		}
		else if (lm == 2) {
			// ��ά����
			int size = this.index[1];
			int len = this.index[0];
			for (int c = 0; c < size; c++) {
				for (int r = 0; r < len; r++) {
					Number num = this.A[r][c];
					if (num != null) {
						this.A[r][c] = num.doubleValue()/d;
					}
				}
			}
		}
		else {
			// ��ά����
			for (MulMatrix submm : this.mtx) {
				submm.divide(d);
			}
		}
	}
	
	protected void calcVar2(double d, boolean s) {
		int lm = this.getLevel();
		if (lm == 1) {
			// �������õ���ֵ
			int len = this.index[0];
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						this.A[i][0] = calcStd(num.doubleValue(), d, s);
					}
				}
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						this.A[0][i] = calcStd(num.doubleValue(), d, s);
					}
				}
			}
		}
		else if (lm == 2) {
			// ��ά����
			int size = this.index[1];
			int len = this.index[0];
			for (int c = 0; c < size; c++) {
				for (int r = 0; r < len; r++) {
					Number num = this.A[r][c];
					if (num != null) {
						this.A[r][c] = calcStd(num.doubleValue(), d, s);
					}
				}
			}
		}
		else {
			// ��ά����
			for (MulMatrix submm : this.mtx) {
				submm.calcVar2(d, s);
			}
		}
	}
	
	private Number ZERO = Double.valueOf(0);
	
	/**
	 * ��ȿ�¡
	 * @param mm
	 */
	private MulMatrix create(boolean ifNull) {
		MulMatrix mm = new MulMatrix();
		mm.index = this.index.clone();
		if (this.getLevel() <= 2) {
			int rows = this.A.length;
			int cols = this.A[0].length;
			mm.A = new Number[rows][cols];
			if (!ifNull) {
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						mm.A[r][c] = ZERO;
					}
				}
			}
		}
		else {
			// ��ά������ȿ�¡
			mm.mtx = new ArrayList<MulMatrix>(this.mtx.size());
			for (MulMatrix submm : this.mtx) {
				mm.mtx.add(submm.create(ifNull));
			}
		}
		return mm;
	}
	
	/**
	 * ��ȿ�¡
	 * @param mm
	 */
	private MulMatrix deepClone() {
		MulMatrix mm = new MulMatrix();
		mm.index = this.index.clone();
		if (this.getLevel() <= 2) {
			int rows = this.A.length;
			int cols = this.A[0].length;
			mm.A = new Number[rows][cols];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mm.A[r][c] = this.A[r][c];
				}
			}
		}
		else {
			// ��ά������ȿ�¡
			mm.mtx = new ArrayList<MulMatrix>(this.mtx.size());
			for (MulMatrix submm : this.mtx) {
				mm.mtx.add(submm.deepClone());
			}
		}
		return mm;
	}
	
	/**
	 * ��ĳ��ά����Ƭ��ͨ�����ض�ά���󣬵��п��ܷ�����ֵ
	 * @param level	ָ���㣬��1��ʼ�������ܲ��������壬�����쳣
	 * @param n ��ţ���0��ʼ
	 * @return
	 */
	protected Object slice(int level, int n) {
		int lm = this.getLevel();
		if (level > lm) {
			return this;
		}
		if (n > this.index[level-1]) {
			Logger.warn(n+" out of range of the matrix, set n to 0.");
			n = 0;
		}
		if (lm == 1) {
			// �������г�ĳ����
			if (this.A.length > 1) {
				return this.A[n][0];
			}
			else {
				return this.A[0][n];
			}
		}
		MulMatrix mm = null; 
		if (lm == 2) {
			// ��ά���� 
			if (level == 1) {
				// ֻ����������г�������
				int size = this.index[1];
				Number[][] result = new Number[1][size];
				result[0] = this.A[n];
				mm = new MulMatrix(result, size);
			}
			else {
				// ������
				int size = this.index[0];
				Number[][] result = new Number[size][1];
				for (int i = 0; i < size; i++) {
					result[i][0] = this.A[i][n];
				}
				mm = new MulMatrix(result, size);
			}
		}
		else if (lm > 3 || lm - level >= 2) {
			// ��ײ�洢���ñ仯
			if (level == 1) {
				return this.mtx.get(n);
			}
			else {
				ArrayList<MulMatrix> mtxNew = new ArrayList<MulMatrix>(this.index[0]);
				for (MulMatrix submm : mtx) {
					MulMatrix subslice = (MulMatrix) submm.slice(level - 1, n);
					mtxNew.add(subslice);
				}
				mm = new MulMatrix(mtxNew);
			}
		}
		else {
			// Ҫ����������ײ������
			// ��ʣ��3�㣬������ײ������ʵ�ʲ�����ֻ�迼��levelΪ2��3�������Ϊ1ʱ�ǲ���Ҫ������ײ������
			Number[][] Anew = null;
			int rows = this.index[0];
			if (level == 2) {
				Anew = new Number[rows][this.index[2]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[2]; c++) {
						Anew[r][c] = submm.getNumber(n, c);
					}
				}
			}
			else {
				// levelΪ3
				Anew = new Number[rows][this.index[1]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[1]; c++) {
						Anew[r][c] = submm.getNumber(c, n);
					}
				}
			}
			// �г��ײ��ά����������ʱ�賤��0
			mm = new MulMatrix(Anew, 0);
		}
		return mm;
	}
	
	//levels���򣬶�Ӧÿ��ns
	protected Object slice(int[] levels, int[] ns) {
		int size = levels.length;
		MulMatrix mm = this;
		for (int i = 0; i<size; i++) {
			Object o = mm.slice(levels[i]-i, ns[i]);
			if (o instanceof MulMatrix) {
				mm = (MulMatrix) o;
			}
			else {
				return o;
			}
		}
		return mm;
	}
	
	/**
	 * �����׼��
	 * @param level	�ۺϲ�
	 * @param s	�Ƿ�ʹ����������n-1����
	 * @return
	 */
	protected Object std(int level, boolean s) {
		int lm = this.getLevel();
		if (level > lm || level < 0) {
			return this;
		}
		if (level == 0) {
			level = autoLevel();
		}
		int len = this.index[level-1];
		if (lm == 1) {
			// �������õ���ֵ
			double sum = 0;
			double var = 0;
			if (this.A.length > 1) {
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
				double avg = sum/len;
				for (int i = 0; i < len; i++) {
					Number num = this.A[i][0];
					if (num != null) {
						double d = num.doubleValue();
						var += (d-avg)*(d-avg);
					}
				}
				var = calcStd(var, len, s);
			}
			else {
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						sum += num.doubleValue();
					}
				}
				double avg = sum/len;
				for (int i = 0; i < len; i++) {
					Number num = this.A[0][i];
					if (num != null) {
						double d = num.doubleValue();
						var += (d-avg)*(d-avg);
					}
				}
				var = calcStd(var, len, s);
			}
			return var;
		}
		MulMatrix mm = null; 
		if (lm == 2) {
			// ��ά���� ����ά����ۺϵĽ�����������֣����оۺϵĻ����ض�ά���У����оۺϷ�������
			if (level == 1) {
				// ���оۼ����õ���ά���У���ֻ��һ��
				int size = this.index[1];
				Number[][] result = new Number[1][size];
				for (int c = 0; c < size; c++) {
					double sum = 0;
					double var = 0;
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
						}
					}
					double avg = sum/len;
					for (int r = 0; r < len; r++) {
						Number num = this.A[r][c];
						if (num != null) {
							double d = num.doubleValue();
							var += (d-avg)*(d-avg);
						}
					}
					var = calcStd(var, len, s);
					result[0][c] = var;
				}
				mm = new MulMatrix(result, size);
			}
			else {
				// ���оۼ�
				int size = this.index[0];
				Number[][] result = new Number[size][1];
				for (int r = 0; r < size; r++) {
					double sum = 0;
					double var = 0;
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							sum += num.doubleValue();
						}
					}
					double avg = sum/len;
					for (int c = 0; c < len; c++) {
						Number num = this.A[r][c];
						if (num != null) {
							double d = num.doubleValue();
							var += (d-avg)*(d-avg);
						}
					}
					var = calcStd(var, len, s);
					result[r][0] = var;
				}
				mm = new MulMatrix(result, size);
			}
		}
		else if (lm > 3 || lm - level >= 2) {
			// ��ײ�洢���ñ仯�ṹ��ֱ�ӾۺϾͿ�����
			if (level == 1) {
				// �ײ�ۺ�
				mm = this.mtx.get(0).create(false);
				int size = this.mtx.size();
				for (MulMatrix submm : this.mtx) {
					submm.sum2(mm);
				}
				// ��ʱmm����ײ��¼����size��MulMatrix�ۼ���͵Ľ�������������ֵ
				mm.divide(size);
				MulMatrix avgmm = mm;
				mm = this.mtx.get(0).create(false);
				// �þ�ֵ��
				for (MulMatrix submm : this.mtx) {
					submm.calcVar1(mm, avgmm);
				}
				mm.calcVar2(size, s);
			}
			else {
				ArrayList<MulMatrix> mtxNew = new ArrayList<MulMatrix>(this.index[0]);
				for (MulMatrix submm : this.mtx) {
					MulMatrix subnew = (MulMatrix) submm.std(level - 1, s);
					mtxNew.add(subnew);
				}
				mm = new MulMatrix(mtxNew);
			}
		}
		else {
			// Ҫ����������ײ������
			// ��ʣ��3�㣬������ײ������ʵ�ʲ�����ֻ�迼��levelΪ2��3�������Ϊ1ʱ�ǲ���Ҫ������ײ������
			Number[][] Anew = null;
			int rows = this.index[0];
			if (level == 2) {
				Anew = new Number[rows][this.index[2]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[2]; c++) {
						double sum = 0;
						double var = 0;
						for (int i = 0, iSize = this.index[1]; i < iSize; i++) {
							if (submm.A[i][c] == null) {
								continue;
							}
							sum += submm.A[i][c].doubleValue();
						}
						double avg = sum/this.index[1];
						for (int i = 0, iSize = this.index[1]; i < iSize; i++) {
							Number num = submm.A[i][c];
							if (num != null) {
								double d = num.doubleValue();
								var += (d-avg)*(d-avg);
							}
						}
						var = calcStd(var, this.index[1], s);
						Anew[r][c] =  var;
					}
				}
			}
			else {
				// levelΪ3
				Anew = new Number[rows][this.index[1]];
				for (int r = 0; r < rows; r++) {
					MulMatrix submm = this.mtx.get(r);
					for (int c = 0; c < this.index[1]; c++) {
						double sum = 0;
						double var = 0;
						for (int i = 0, iSize = this.index[2]; i < iSize; i++) {
							if (submm.A[c][i] == null) {
								continue;
							}
							sum += submm.A[c][i].doubleValue();
						}
						double avg = sum/this.index[2];
						for (int i = 0, iSize = this.index[2]; i < iSize; i++) {
							Number num = submm.A[c][i];
							if (num != null) {
								double d = num.doubleValue();
								var += (d-avg)*(d-avg);
							}
						}
						var = calcStd(var, this.index[2], s);
						Anew[r][c] =  var;
					}
				}
			}
			// ���ؾۺϽ����ά����������ʱ�賤��0
			mm = new MulMatrix(Anew, 0);
		}
		return mm;
	}
	
	protected static double calcStd(double sd, double len, boolean s) {
		if (s) {
			return Math.sqrt(sd / (len -1));
		}
		else {
			return Math.sqrt(sd/len);
		}
	}
	
	protected Object get(int[] loc) {
		return null;
	}
	
	/**
	 * ��ȡ��ά����
	 * @return	����Ķ�ά����
	 */
	public Number[][] getArray() {
		return this.A;
	}
	
	/**
	 * �Ӿ����л�ȡָ�����е���������double
	 * @param matrix	����ʹ�����е����б�ʾ
	 * @param r			�кţ���0��ʼ
	 * @param c			�кţ���0��ʼ
	 * @return
	 */
	protected static double get(Sequence matrix, int r, int c) {
		Object row = matrix.get(r+1);
		if (row instanceof Sequence) {
			int len = ((Sequence) row).length();
			if (len >= c) {
				Object obj = ((Sequence) row).get(c+1);
				if (obj instanceof Number) {
					return ((Number) obj).doubleValue();
				}
			}
		}
		return 0d;
	}
	
	public static void main(String[] args) {
		System.out.println("done");
	}
}
