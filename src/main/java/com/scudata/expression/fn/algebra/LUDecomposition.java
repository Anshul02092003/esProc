package com.scudata.expression.fn.algebra;

import com.scudata.common.RQException;

/**
 * ��������Ƿֽ⴦��
 * @author bd
 */
public class LUDecomposition {
	// �������Ǿ���
	private double[][] LU;
	// �û�����
	private int[] piv;
	
	private int rows, cols, pivsign; 

	/**
	 * ������ִ�����Ƿֽⷨ
	 */
	protected LUDecomposition(Matrix A) {
		this.LU = A.getArrayCopy();
		this.rows = A.getRows();
		this.cols = A.getCols();
		this.piv = new int[rows];
		for (int r = 0; r < rows; r++) {
			piv[r] = r;
		}
		this.pivsign = 1;
		double[] LUrow;
		double[] LUcol = new double[rows];

		// ���ѭ���������п�ʼ����LU����
		for (int c = 0; c < cols; c++) {
			// ��¼���е�ԭֵ
			for (int r = 0; r < rows; r++) {
				LUcol[r] = LU[r][c];
			}
			// ׼����⵱ǰλ�õ�ֵ��
			// ���£�r>c, a(r,c)=l(r,1)*u(1,c)+l(r,2)*u(2,c)...+l(r,c)*u(c,c)
			// ������r<c, a(r,c)=l(r,1)*u(1,c)+...+l(r,r-1)*u(r-1,c)...+u(r,c)
			for (int r = 0; r < rows; r++) {
				LUrow = LU[r];
				// ǰ�沿�ֵĲ������漰֮ǰ�к�֮ǰ�У�LU�����еĶ�ӦֵӦ���Ѿ��������
				int kmax = Math.min(r, c);
				double s = 0d;
				for (int k = 0; k < kmax; k++) {
					s += LUrow[k] * LUcol[k];
				}
				// �ѵ�ǰλ�õ�ǰ�ò�������ȥ
				// ʣ�µģ�����=l(r,c)*u(c,c)���������־���u(r,c)��
				LUrow[c] = LUcol[r] -= s;
			}
			// �����Ƿ���Ҫpivotת�þ���
			int p = c;
			for (int r = c + 1; r < rows; r++) {
				if (Math.abs(LUcol[r]) > Math.abs(LUcol[p])) {
					p = r;
				}
			}
			if (p != c) {
				for (int k = 0; k < cols; k++) {
					double t = LU[p][k];
					LU[p][k] = LU[c][k];
					LU[c][k] = t;
				}
				int k = piv[p];
				piv[p] = piv[c];
				piv[c] = k;
				pivsign = -pivsign;
			}
			// �����������Ǽ��㣬��ʱ��λ�ü����Ѿ�����ˣ�ֻ���޸�LU������У�LURow��LUCol���ù���
			if (c < rows & LU[c][c] != 0d) {
				for (int r = c + 1; r < rows; r++) {
					LU[r][c] /= LU[c][c];
				}
			}
		}
	}

	/**
	 * �þ�������Ƿֽⷨ���A*X = B
	 * @param B		�������B
	 * @return
	 */
	protected Matrix solve(Matrix B) {
		if (B.getRows() != this.rows) {
			throw new RQException("Matrix row dimensions must agree.");
		}
		if (!isNonsingular()) {
			//������󣬷�������������޽�
			throw new RQException("Matrix is singular.");
		}

		//ִ�����Ƿֽ�󣬷��̱�ΪL*U*X = B����L*(U*X) = B
		//L*Y=B, �ȼ���B��U*X
	    int nx = B.getCols();
		Matrix Xmat = B.getMatrix(piv, 0, nx - 1);
		double[][] X = Xmat.getArray();

		// ��� L*Y = B(piv,:)
		for (int k = 0; k < this.cols; k++) {
			for (int i = k + 1; i < this.cols; i++) {
				for (int j = 0; j < nx; j++) {
					X[i][j] -= X[k][j] * LU[i][k];
				}
			}
		}
		// ��� U*X = Y;
		for (int k = this.cols - 1; k >= 0; k--) {
			for (int j = 0; j < nx; j++) {
				X[k][j] /= LU[k][k];
			}
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < nx; j++) {
					X[i][j] -= X[k][j] * LU[i][k];
				}
			}
		}
		return Xmat;
	}

	/**
	 * ������ʽ���Ƿ������쳣
	 * @return		A������ʽdet(A)
	 * @exception	�쳣
	 */
	protected double det() {
		if (this.rows != this.cols) {
			throw new IllegalArgumentException("Matrix must be square.");
		}
		double d = (double) pivsign;
		for (int j = 0; j < this.cols; j++) {
			d *= LU[j][j];
		}
		double scale = 1000000;
		d = d * scale;
		if (d > Long.MIN_VALUE && d < Long.MAX_VALUE) {
			d = Math.round(d)/scale;
		} else {
			d = d / scale;
		}
		return d;
	}
	
	/**
	 * ���ݽ������Ƿֽ�����ж��Ƿ������������������0����һ���Ƿ��������
	 * @param LU	�������Ƿֽ����
	 * @return		�Ƿ���������������󷵻�false
	 */
	private boolean isNonsingular() {
		for (int c = 0; c < this.cols; c++) {
			if (this.LU[c][c] == 0)
				return false;
		}
		return true;
	}
}
