package com.scudata.expression.fn.algebra;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
//import org.ejml.simple.SimpleMatrix;

import com.scudata.common.Logger;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;

/**
 * ��������࣬�ṩ����ĸ������
 * �������еľ���ʹ�����е����б�ʾ��һ�㲻���
 * added by bd, 2021.1.15, ��������������
 * @author bidalong
 *
 */
public class Matrix {

	private double[][] A;
	private int rows, cols;
	//added by bd, 2021.1.15, �Ƿ���������������洢Ϊ���л��о���
	private boolean ifVector = false;
	
	public Matrix(int rs, int cs) {
		this.A = new double[rs][cs];
		this.rows = rs;
		this.cols = cs;
	}
	
	/**
	 * ��ʼ������
	 * @param value	��ά�����ʾ�ľ���ֵ
	 */
	/*
	protected Matrix(SimpleMatrix smatrix) {
		this.rows = smatrix.numRows();
		this.cols = smatrix.numCols();
		this.A = new double[this.rows][this.cols];
        for (int r = 0; r < this.rows ; r++) {
            for (int c = 0; c < this.cols; c++) {
            	this.A[r][c] = smatrix.get(r, c);
            }
        }
	}
	*/
	
	/**
	 * ��ʼ��һ���������matrix�����г��Ȳ��ȣ�������󳤶�����������������0
	 * ���matrix�в�Ϊ���У������д���
	 * @param matrix
	 */
	public Matrix(Sequence matrix) {
		int rows = matrix == null ? 0 : matrix.length();
		if (rows > 0) {
			if (matrix instanceof Table) {
				//added by bd, 2021.1.22, ��Ӷ�����֧��
				// ��ά����, ���
				Table tab = (Table) matrix;
				this.cols = tab.dataStruct().getFieldCount();
				this.rows = rows;
				this.A = new double[rows][this.cols];
				for (int i = 1; i <= rows; i++) {
					double[] row = getRow(tab.getRecord(i), this.cols);
					this.A[i-1] = row;
				}
			}
			else {
				int cols = 0;
				for (int r = 0; r < rows; r++ ) {
					Object row = matrix.get(r+1);
					if (row instanceof Sequence) {
						int cols2 = ((Sequence) row).length();
						if (cols < cols2) {
							cols = cols2;
						}
					}
				}
				if (cols == 0) {
					// ��һ���У���Ϊһ�����ݣ�
					// edited by bd, 2021.1.15, ֻ����������ᱻ��Ϊ�����������������Զ���֪
					// edited by bd, 2021.2.25, ������ΪĬ�������������������һЩ����������[[1,2,3]]����
					cols = 1;
					this.ifVector = false;
					this.A = new double[rows][1];
					for (int r = 0; r < rows; r++) {
						Object obj = ((Sequence) matrix).get(r+1);
						if (obj instanceof Number) {
							this.A[r][0] = ((Number) obj).doubleValue();
						}
					}
				}
				else {
					this.A = new double[rows][cols];
					for (int r = 0; r < rows; r++) {
						Object row = matrix.get(r+1);
						if (row instanceof Sequence) {
							int cols2 = ((Sequence) row).length();
							for (int c = 0; c < cols2; c++) {
								Object obj = ((Sequence) row).get(c+1);
								if (obj instanceof Number) {
									this.A[r][c] = ((Number) obj).doubleValue();
								}
							}
						}
						else if (row instanceof Number) {
							this.A[r][0] = ((Number) row).doubleValue();
						}
					}
				}
				this.cols = cols;
				this.rows = rows;
			}
		}
	}
	
	/**
	 * ��ȡһ��һά����Ϊdouble���飬����ֵ��ȫ��0��
	 * @param seq
	 * @return
	 */
	protected static double[] getRow(Sequence seq, int n) {
		if (n < 1) {
			n = seq == null ? 0 : seq.length();
		}
		if (n < 1) {
			return null;
		}
		double[] row = new double[n];
		for (int i = 1; i <=n; i++) {
			Object obj = seq.get(i);
			row[i-1] = getNumber(obj);
		}
		return row;
	}
	
	/**
	 * ��ȡһ��һά����Ϊdouble���飬����ֵ��ȫ��0��
	 * @param seq
	 * @return
	 */
	protected static double[] getRow(Record rec, int n) {
		double[] row = new double[n];
		Object[] vs = rec.getFieldValues();
		for (int i = 0; i <n; i++) {
			Object obj = vs[i];
			row[i] = getNumber(obj);
		}
		return row;
	}
	
	/*
	 * ��һ��Objectת��Ϊdouble����
	 */
	private static double getNumber(Object obj) {
		double d = 0;
		if (obj instanceof Sequence ) {
			if (((Sequence) obj).length() == 0) {
				return d;
			}
			else {
				obj = ((Sequence) obj).get(1);
			}
		}
		if (obj instanceof Number) {
			d = ((Number) obj).doubleValue();
		}
		else if (obj instanceof String) {
			d = Double.valueOf(obj.toString());
		}
		return d;
	}
	
	/**
	 * ������������ȡ��������, added by bd, 2021.1.15
	 * @return
	 */
	protected double[] getVector() {
		if (this.ifVector) {
			if (this.rows == 1) {
				return this.A[0];
			}
			else if(this.cols == 1) {
				double[] vector = new double[this.rows];
				for (int i = 0; i < this.rows; i++) {
					vector[i] = this.A[i][0];
				}
				return vector;
			}
		}
		return null;
	}
	
	/**
	 * ��ʼ��һ����������
	 * @param vector		��������������
	 * @param vertical	�Ƿ���������falseʱΪ������
	 */
	protected Matrix(double[] vector, boolean vertical) {
		int size = vector == null ? 0 : vector.length;
		this.ifVector = true;
		if (vertical) {
			this.rows = size;
			this.cols = 1;
			this.A = new double[this.rows][this.cols];
			for (int r = 0; r < size; r++) {
				this.A[r][0] = vector[r];
			}
		}
		else {
			this.rows = 1;
			this.cols = size;
			this.A = new double[this.rows][this.cols];
			this.A[0] = vector;
		}
	}
	
	/**
	 * ��ʼ��һ����������
	 * @param vector		��������������
	 * @param vertical	�Ƿ���������falseʱΪ������
	 */
	protected Matrix(Sequence vector, boolean vertical) {
		int size = vector == null ? 0 : vector.length();
		this.ifVector = true;
		if (vertical) {
			this.rows = size;
			this.cols = 1;
			this.A = new double[this.rows][this.cols];
			for (int r = 0; r < size; r++) {
				Object obj = vector.get(r+1);
				if (obj instanceof Number) {
					this.A[r][0] = ((Number) obj).doubleValue();
				}
			}
		}
		else {
			this.rows = 1;
			this.cols = size;
			this.A = new double[this.rows][this.cols];
			for (int i = 0; i < size; i++) {
				Object obj = vector.get(i+1);
				if (obj instanceof Number) {
					this.A[0][i] = ((Number) obj).doubleValue();
				}
			}
		}
	}

	/**
	 * ��ʼ��һ����������
	 * @param A		��ά����
	 * @param rows	������Ӧ�ö�ӦA
	 * @param cols		������Ӧ�ö�ӦA
	 */
	public Matrix(double[][] A) {
		this.A = A;
		this.rows = A.length;
		int cols = 0;
		for (int i = 0; i < this.rows; i++) {
			double[] row = this.A[i];
			int thisCols = row == null ? 0 : row.length;
			if (cols < thisCols) {
				cols = thisCols;
			}
		}
		this.cols = cols;
	}

	/**
	 * ��ʼ��һ����������
	 * @param A		��ά����
	 * @param rows	������Ӧ�ö�ӦA
	 * @param cols		������Ӧ�ö�ӦA
	 */
	public Matrix(double[][] A, int rows, int cols) {
		this.A = A;
		this.rows = rows;
		this.cols = cols;
	}
	
	/**
	 * ������תΪ���У��ر�ģ�ֻ��һ����Աʱֱ�ӷ�������
	 * Ϊ����double����ʱ��ɵ������������봦��
	 * added by bd 2021.1.22, ��Ӳ���������Ϊ���������
	 * @return
	 */
	public Object toSequence(String option, boolean real) {
		boolean ift = option != null && option.indexOf('t')>-1;
		boolean ifv = option != null && option.indexOf('v')>-1;
		if (ift) {
	        String[] cols = new String[]{"_1"};
	        if (this.cols > 1){
	        	cols = new String[this.cols];
		        for(int i=0; i<this.cols; i++){
		        	cols[i] = "_"+(i+1);
		        }
	        }
	        Table tbl = new Table(cols);
	        for(int i=0; i<this.rows; i++){
	        	Double[] r = new Double[this.cols];
	        	for(int j=0; j<this.cols; j++){
		        	r[j] = getValue(i, j, real);
	        	}
	        	tbl.newLast(r);
	        }
	        
	        return tbl;
		}
		if (this.rows == 1 && this.cols == 1) {
			// ���ֻ��һ����Ա��ֱ�ӷ���
			return this.A[0][0];
		}
		/*
		double min = 1;
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				double d = Math.abs(this.A[r][c]);
				if (d > 0 && min > d) {
					min = d;
				}
			}
		}
		double pow = Math.ceil(Math.log(min)) - 5;
		double scale = Math.pow(10, pow);
		*/
		if (ifv && this.rows == 1) {
			//edited by bd, 2021.2.25, �����ֻ��һ�У��Ҽ������а�������ʱ����������
			Sequence sub = new Sequence(this.cols);
			for (int c = 0; c < this.cols; c++) {
				double d = getValue(0, c, real);
				sub.add(d);
			}
			return sub;
		}
		else if (ifv && this.cols == 1) {
			//added by bd, 2021.2.25, �����ֻ��һ�У��Ҽ������а�������ʱ����������
			Sequence sub = new Sequence(this.rows);
			for (int r = 0; r < this.rows; r++) {
				double d = getValue(r, 0, real);
				sub.add(d);
			}
			return sub;
		}
		Sequence seq = new Sequence(this.rows);
		for (int r = 0; r < this.rows; r++) {
			Sequence sub = new Sequence(this.cols);
			for (int c = 0; c < this.cols; c++) {
				double d = getValue(r, c, real);
				sub.add(d);
			}
			seq.add(sub);
		}
		return seq;
	}
	
	/*
	 * �޸ľ�����ĳ��ֵ��added by bd, 2021.4.8
	 */
	public void set(int r, int c, double v) {
		this.A[r][c] = v;
	}

	private final static double scale = 1000000d;
	private final static double range = 1e-10;
	private double getValue(int r, int c, boolean real) {
		double d = this.A[r][c];
		if (!real) {
			// added by bd, 2022.5.1, �������ֵ̫С������ԭֵ
			double abs = Math.abs(d);
			double scale1 = Matrix.scale;
			if (abs < range) {
				return d;
			}
			else if (abs < 1) {
				scale1 *= Math.pow(10, (int) Math.round((Math.log10(1/abs))));
			}
			d *= scale1;
			if (d > Long.MIN_VALUE && d < Long.MAX_VALUE) {
				d = Math.round(d)/scale1;
			} else {
				//d = d / scale;
				return this.A[r][c]; 
			}
		}
		return d;
	}

	/**
	 * ��ȡ����
	 * @return	��������
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * ��ȡ����
	 * @return	��������
	 */
	public int getCols() {
		return this.cols;
	}

	/**
	 * ��ȡ��ά����
	 * @return	����Ķ�ά����
	 */
	public double[][] getArray() {
		return this.A;
	}
	
	/**
	 * ��ǰ�����Э�������
	 * @return
	 */
	public Matrix covm() {
		Matrix X = new Matrix(this.cols, this.cols);
		double[][] xs = X.getArray();
		// ����ά�����飬��Aת��
		double[][] dims = this.transpose().getArray();
		// ����ά�ȵľ�ֵ
		double[] dimv = new double[this.cols];
		for (int i = 0; i < this.cols; i++) {
			double[] dim = dims[i];
			double res = 0;
			for (int j = 0; j < this.rows; j++) {
				res += dim[j];
			}
			dimv[i] = res/this.rows;
		}
		for (int i = 0; i < this.cols; i++) {
			for (int j = 0; j < this.cols; j++) {
				if (i > j) {
					// �Ѿ��������
					xs[i][j] = xs[j][i];
				}
				else if (i == j) {
					// �Խ����ϵ�Э����
					double cov = 0;
					for (int k = 0; k < this.rows; k++) {
						cov += Math.pow(this.A[k][i] - dimv[i], 2);
					}
					xs[i][j] = cov/(this.rows - 1);
				}
				else {
					// �������ǵ�Э����
					double cov = 0;
					for (int k = 0; k < this.rows; k++) {
						cov += (this.A[k][i] - dimv[i]) * (this.A[k][j] - dimv[j]);
					}
					xs[i][j] = cov/(this.rows - 1);
				}
			}
		}
		return X;
	}
	
	/**
	 * �þ�������������һ�����
	 * @return
	 */
	protected Table toTable() {
		String[] cns = new String[this.cols];
		for (int c = 0; c < this.cols; c++) {
			cns[c] = "Col"+(c+1);
		}
		Table t = new Table(cns);
		for (int r = 0; r < this.rows; r++) {
			Record rec = t.newLast();
			for (int c = 0; c < this.cols; c++) {
				rec.set(c, this.A[r][c]);
			}
		}
		return t;
	}

	/**
	 * ��ȡ�Ӿ���
	 * @param r		��ȡ���ĸ��к�
	 * @param j0	��ʼ�к�
	 * @param j1	�����к�
	 * @return		ָ��λ�õ��Ӿ���
	 * @exception	�쳣
	 */
	protected Matrix getMatrix(int[] r, int j0, int j1) {
		Matrix X = new Matrix(r.length, j1 - j0 + 1);
		double[][] B = X.getArray();
		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i][j - j0] = A[r[i]][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
		return X;
	}

	/**
	 * ��ȡ�Ӿ���
	 * @param r0	��ʼ�кţ���0��ʼ
	 * @param r1	�����кţ��������Ӿ�����
	 * @param c0	��ʼ�кţ���0��ʼ
	 * @param c1	�����кţ��������Ӿ�����
	 * @return		�ӵ�ǰ�����н�ȡ�Ӿ���
	 * @exception	���г��޴���
	 */
	public Matrix getMatrix(int r0, int r1, int c0, int c1) {
		Matrix X = new Matrix(r1 - r0 + 1, c1 - c0 + 1);
		double[][] B = X.getArray();
		try {
			for (int r = r0; r <= r1; r++) {
				for (int c = c0; c <= c1; c++) {
					B[r - r0][c - c0] = A[r][c];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
		return X;
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
	
	/**
	 * �ж��Ƿ���󣬲��Ǿ��󷵻�null���Ǿ��󷵻����������ɵ�����
	 * @param matrix	����
	 * @param ifNumerical	�Ƿ��жϳ�ԱΪ��ֵ
	 * @return	�Ƿ����
	 */
	protected static int[] ifMatrix(Sequence matrix, boolean ifNumerical) {
		int rows = matrix == null ? 0 : matrix.length();
		if (rows > 0) {
			int cols = 0;
			for (int r = 0; r < rows; r++ ) {
				Object row = matrix.get(r+1);
				if (row instanceof Sequence) {
					int cols2 = ((Sequence) row).length();
					if (cols == 0) {
						cols = cols2;
					}
					else if (cols != cols2) {
						return null;
					}
				}
				else {
					return null;
				}
				if (ifNumerical) {
					for (int c = 0; c < cols; c++) {
						Object obj = ((Sequence) row).get(c+1);
						if (!(obj instanceof Number)) {
							return null;
						}
					}
				}
			}
			int[] bak = {rows, cols};
			return bak;
		}
		return null;
	}
	
	/**
	 * �ж��Ƿ���󣬲��Ǿ��󷵻�null���Ǿ��󷵻����������ɵ�����
	 * @return	��ʵ���Ƿ����
	 */
	protected boolean ifMatrix() {
		return this.A != null;
	}
	
	/**
	 * �ж��Ƿ����Ƿ��󷵻ط����������(���)
	 * @param matrix	����
	 * @param ifNumerical	�Ƿ��жϳ�ԱΪ��ֵ
	 * @return	�Ƿ���
	 */
	protected static int ifSquare(Sequence matrix, boolean ifNumerical) {
		int rows = matrix == null ? 0 : matrix.length();
		if (rows > 0) {
			for (int r = 0; r < rows; r++ ) {
				Object row = matrix.get(r+1);
				if (row instanceof Sequence) {
					int cols = ((Sequence) row).length();
					if (cols != rows) {
						return 0;
					}
					if (ifNumerical) {
						for (int c = 0; c < cols; c++) {
							Object obj = ((Sequence) row).get(c+1);
							if (!(obj instanceof Number)) {
								return 0;
							}
						}
					}
				}
				else {
					return 0;
				}
			}
			return rows;
		}
		return 0;
	}
	
	/**
	 * �ж��Ƿ���
	 * @return
	 */
	protected boolean ifSquare() {
		return this.rows > 0 && this.rows == this.cols;
	}

	/**
	 * ת��
	 * @return
	 */
	public Matrix transpose(){
		Matrix X = new Matrix(this.cols, this.rows);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				X.A[c][r] = A[r][c];
			}
		}
		return X;
	}

	/**
	 * ת�ã����matrix���������ڷ��ص���������null����
	 * @param matrix
	 * @return
	 */
	public static Sequence transpose(Sequence matrix){
		int rows = matrix == null ? 0 : matrix.length();
		if (rows > 0) {
			int cols = 0;
			for (int r = 0; r < rows; r++ ) {
				Object row = matrix.get(r+1);
				if (row instanceof Sequence) {
					int cols2 = ((Sequence) row).length();
					if (cols < cols2) {
						cols = cols2;
					}
				}
			}
			Sequence trans = new Sequence(cols);
			for (int c = 0; c < cols; c++) {
				Sequence row0 = new Sequence(rows);
				trans.add(row0);
				for (int r = 0; r < rows; r++) {
					Object row = matrix.get(r+1);
					if (row instanceof Sequence) {
						int cols2 = ((Sequence) row).length();
						if (cols2 > c) {
							row0.add(((Sequence) row).get(c+1));
						}
						else {
							row0.add(null);
						}
					}
					else if (c == 0) {
						row0.add(row);
					}
					else {
						row0.add(null);
					}
				}
			}
			return trans;
		}
		return null;
	}

	/**
	 * ��ȡ������ָ��λ�õ��������������ж�
	 * @param i		�к�
	 * @param j		�к�
	 * @return
	 */
	public double get(int r, int c) {
		return this.A[r][c];
	}

	/**
	 * ���������������������Ƿ���ȣ��ܶ�������㶼��Ҫ��Ӧ����ִ��
	 * @param B
	 */
	private void checkMatrixSize(Matrix B) {
		if (B.rows != this.rows || B.cols != this.cols) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}

	/**
	 * ������ӣ�C=A+B
	 * @param B		����ִ�мӷ��ľ���
	 * @return		������ӵĽ��
	 */
	public Matrix plus(Matrix B) {
		checkMatrixSize(B);
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] + B.A[r][c];
			}
		}
		return X;
	}


	/**
	 * ������ÿ����Ա����ƽ��
	 * @return		
	 */
	public Matrix elementSquare() {
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] * A[r][c];
			}
		}
		return X;
	}

	/**
	 * ������ÿ����Ա���ܺ�
	 * @return		
	 */
	public double elementSum() {
		double sumup = 0d;
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				sumup += A[r][c];
			}
		}
		return sumup;
	}

	/**
	 * ���������������ڻ�
	 * @return		
	 */
	public double dot(Matrix B) {
		double innerProduct = 0d;
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				innerProduct += A[r][c]*B.A[r][c];
			}
		}
		return innerProduct;
	}
	
	/**
	 * ������ӣ������¼�ڱ������У�A = A + B
	 * @param B		����ִ�мӷ��ľ���
	 * @return		������ӵĽ��
	 */

	protected Matrix plusUp(Matrix B) {
		checkMatrixSize(B);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				A[r][c] = A[r][c] + B.A[r][c];
			}
		}
		return this;
	}

	/**
	 * ������ӣ�C=A-B
	 * @param B		����ִ�м����ľ���
	 * @return		��������Ľ��
	 */
	public Matrix minus(Matrix B) {
		checkMatrixSize(B);
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] - B.A[r][c];
			}
		}
		return X;
	}

	/**
	 * �����ʵ����C=A+d
	 * @param d		����ִ�мӷ���ʵ��
	 * @return		
	 */
	public Matrix plus(double d) {
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] + d;
			}
		}
		return X;
	}

	/**
	 * �����ʵ����C=A-d
	 * @param d		����ִ�м�����ʵ��
	 * @return		
	 */
	protected Matrix minus(double d) {
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] - d;
			}
		}
		return X;
	}

	/**
	 * �����ʵ����C=A*d
	 * @param d		����ִ�мӷ���ʵ��
	 * @return		
	 */
	protected Matrix times(double d) {
		Matrix X = new Matrix(this.rows, this.cols);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X.A[r][c] = A[r][c] * d;
			}
		}
		return X;
	}

	/**
	 * ��������������¼�ڱ������У�A=A-B
	 * @param B		����ִ�м����ľ���
	 * @return		��������Ľ��
	 */
	protected Matrix minusEquals(Matrix B) {
		checkMatrixSize(B);
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				A[r][c] = A[r][c] - B.A[r][c];
			}
		}
		return this;
	}

	/**
	 * �������
	 * @param B		������˵ľ���
	 * @return		������˵Ľ������
	 * @exception	�쳣
	 */
	public Matrix times(Matrix B) {
		if (B.rows != this.cols) {
			throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		}
		Matrix X = new Matrix(this.rows, B.cols);
		double[] BVectorCol = new double[this.cols];
		for (int c = 0; c < B.cols; c++) {
			for (int k = 0; k < this.cols; k++) {
				BVectorCol[k] = B.A[k][c];
			}
			for (int r = 0; r < this.rows; r++) {
				double[] AVectorRow = A[r];
				double s = 0;
				for (int k = 0; k < this.cols; k++) {
					s += AVectorRow[k] * BVectorCol[k];
				}
				X.A[r][c] = s;
			}
		}
		return X;
	}

	/**
	 * ������Գ���
	 * @param d		����
	 * @return		�������
	 * @exception	�쳣
	 */
	public Matrix divide(double d) {
		Matrix X = new Matrix(this.rows, this.cols);
		for (int c = 0; c < this.cols; c++) {
			for (int r = 0; r < this.rows; r++) {
				X.A[r][c] = this.A[r][c] / d;
			}
		}
		return X;
	}

	/**
	 * ���ɵ�λ����
	 * @param rows		����
	 * @param cols		����
	 * @return			����rows*cols���󣬶Խ�����Ϊ1������Ϊ0
	 */
	protected static Matrix identity(int rows, int cols) {
		Matrix X = new Matrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				X.A[r][c] = (r == c ? 1d : 0d);
			}
		}
		return X;
	}

	/**
	 * ���ɵ�λ����
	 * @param size		����
	 * @return			����size*size�ľ��󣬶Խ�����Ϊ1������Ϊ0
	 */
	protected static Matrix identity(int size) {
		return identity(size, size);
	}

	/**
	 * ���A*X = B
	 * @param		�������B
	 * @return		����ֱ����⣬�������������С���˷���
	 */

	public Matrix solve(Matrix B) {
		//edited by bd, 2020.3.10, ���������е�����ֻ���������Ϣ�����жϣ�����null
		Matrix result = null;
		try {
			result = (this.rows == this.cols ?
					(new LUDecomposition(this)).solve(B)
					:(new QRDecomposition(this)).solve(B));
		}
		catch (Exception e) {
			Logger.warn(e.getMessage());
		}
		return result;
	}

	/**
	 * ���ƶ�ά����A
	 * @return	���ƵĶ�ά����
	 */
	public double[][] getArrayCopy() {
		double[][] X = new double[this.rows][this.cols];
		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.cols; c++) {
				X[r][c] = A[r][c];
			}
		}
		return X;
	}
	
	/**
	 * ����������α�����
	 * @return
	 */
	public Matrix inverse() {
		return solve(identity(this.rows));
	}
	
	/**
	 * ��α�����
	 * @return
	 */
	public Matrix pseudoinverse() {
        //RealMatrix m = this.realMatrix();
        //SimpleMatrix m = new SimpleMatrix(this.A);
    	//SimpleMatrix sm = this.realMatrix().pseudoInverse();
    	//Matrix pinv = new Matrix(sm);
    	//return pinv;
		
		// ��������ȣ�ֱ����QR�ֽ⣬����ת�ú���α�������ת�û���
		if (this.rows >= this.cols) {
			return (new QRDecomposition(this)).solve(identity(this.rows));
		}
		else {
			Matrix X = (new QRDecomposition(this.transpose())).solve(identity(this.cols));
			return X.transpose();
		}
	}
    
    /**
     * ����SimpleMatrix
     * @return
     */
    public RealMatrix realMatrix() {
    	return new Array2DRowRealMatrix(this.A);
    }
	   
	/**
	 * ����������ʽֵ
	 * @return	����ʽֵ
	 */
	public double det() {
		//edited by bd, 2020.3.10, ���������е�����ֻ���������Ϣ�����жϣ�����null
		if (this.rows != this.cols) {
			//throw new IllegalArgumentException("Matrix must be square.");
			Logger.warn("Matrix must be square.");
			return 0;
		}
		double det = 0;
		try {
			det = new LUDecomposition(this).det();
		}
		catch (Exception e) {
			Logger.warn(e.getMessage());
		}
		return det;
	}
	
	public static void main(String[] args) {
		Sequence seq = new Sequence(3);
		Sequence sub1 = new Sequence(3);
		sub1.add(1);
		sub1.add(1);
		sub1.add(1);
		seq.add(sub1);
		Sequence sub2 = new Sequence(3);
		sub2.add(0);
		sub2.add(4);
		sub2.add(-1);
		seq.add(sub2);
		Sequence sub3 = new Sequence(3);
		sub3.add(2);
		sub3.add(-2);
		sub3.add(1);
		seq.add(sub3);
		//seq.add(4);
		Matrix m = new Matrix(seq);
		Sequence seq2 = new Sequence(3);
		seq2.add(6);
		seq2.add(5);
		seq2.add(1);
		m.solve(new Matrix(seq2, true)).output();
		System.out.println("done");
	}
	
	protected void output() {
		for (int i = 0; i < rows; i++) {
			String s = "";
			for (int j = 0; j < cols; j++) {
				s += A[i][j]+"\t";
			}
			System.out.println(s);
		}
	}

	/**
	 * ���ؾ�����ȣ�������ֵ�ֽ⴦��
	 * @return	�������
	 */
	public int rank() {
	      return new SVDecomposition(this).rank();
	}
	
	/**
	 * �������һ������ı�׼�A��B����ͬά
	 * @param B		��һ����
	 * @return		��׼����
	 */
	public double mse(Matrix B) {
		//edited by bd, 2020.3.10, ���������е�����ֻ���������Ϣ�����жϣ�����null
		try {
			Matrix X = this.minus(B);
			double result = 0;
			for (int r = 0; r < this.rows; r++) {
				for (int c = 0; c < this.cols; c++) {
					result += Math.pow(X.get(r, c), 2);
				}
			}
			return result / this.cols / this.rows;
		}
		catch (Exception e) {
			Logger.warn(e.getMessage());
		}
		return 0d;
	}
	
	/**
	 * �������һ������ľ�����A��B����ͬά
	 * @param B		��һ����
	 * @return		�������
	 */
	public double mae(Matrix B) {
		//edited by bd, 2020.3.10, ���������е�����ֻ���������Ϣ�����жϣ�����null
		try {
			Matrix X = this.minus(B);
			double result = 0;
			for (int r = 0; r < this.rows; r++) {
				for (int c = 0; c < this.cols; c++) {
					result += Math.abs(X.get(r, c));
				}
			}
			return result / this.cols / this.rows;
		}
		catch (Exception e) {
			Logger.warn(e.getMessage());
		}
		return 0d;
	}
	
	/**
	 * �Ƿ��������������洢Ϊ���л��о���
	 * @return
	 */
	public boolean ifVector() {
		return this.ifVector;
	}

	/**
	 * �����¾���ʹ���¾���ÿһ�еľ�ֵΪ0
	 * @param matrix	Դ����
	 * @return
	 */
    public Matrix changeAverageToZero() {
        double[] sum = new double[this.cols];
        double[] average = new double[this.cols];
        double[][] averageArray = new double[this.rows][this.cols];
        for (int c = 0; c < this.cols; c++) {
            for (int r = 0; r < this.rows; r++) {
                sum[c] += this.get(r, c);
            }
            average[c] = sum[c] / this.rows;
        }
        for (int c = 0; c < this.cols; c++) {
            for (int r = 0; r < this.rows; r++) {
                averageArray[r][c] = this.get(r, c) - average[c];
            }
        }
        return new Matrix(averageArray);
    }

	/**
	 * �����¾���ʹ���¾���ÿһ�еľ�ֵΪ0
	 * @param matrix	Դ����
	 * @return
	 */
    public Matrix changeAverageToZero(Vector averageV) {
        double[] average = averageV.getValue();
        double[][] averageArray = new double[this.rows][this.cols];
        for (int c = 0; c < this.cols; c++) {
            for (int r = 0; r < this.rows; r++) {
                averageArray[r][c] = this.get(r, c) - average[c];
            }
        }
        return new Matrix(averageArray);
    }
    
    /**
     * ��ȡÿ�еľ�ֵ����
     * @param primary	
     * @return
     */
    public Vector getAverage() {
        // ��ֵ���Ļ���ľ���
        double[] sum = new double[this.cols];
        double[] average = new double[this.cols];
        for (int c = 0; c < this.cols; c++) {
            for (int r = 0; r < this.rows; r++) {
                sum[c] += this.get(r, c);
            }
            average[c] = sum[c] / this.rows;
        }

        return new Vector(average);
    }
}
