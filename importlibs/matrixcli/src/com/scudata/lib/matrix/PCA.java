package com.scudata.lib.matrix;

import java.util.*;

import org.apache.commons.math3.linear.*;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.fn.algebra.Matrix;
import com.scudata.expression.fn.algebra.Vector;
import com.scudata.resources.EngineMessage;

public class PCA extends Function {
	/**
	 * pca(A,F)��pca(A,n), nʡ��ΪA�����������ص����ɷ�ϵ�������޷���ά�������������������ɷֵ÷�
	 * @param ctx	������
	 * @return
	 */
	public Object calculate (Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pca" + mm.getMessage("function.missingParam"));
		}
		Object o1 = null;
		Object o2 = null;
		if (param.isLeaf()) {
			// ֻ��һ��������pca(A), n���Զ�����ΪA������
			o1 = param.getLeafExpression().calculate(ctx);
		} else if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pca" + mm.getMessage("function.invalidParam"));
		} else {
			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("pca" + mm.getMessage("function.invalidParam"));
			}
			o1 = sub1.getLeafExpression().calculate(ctx);
			o2 = sub2.getLeafExpression().calculate(ctx);
		}
		if (o1 instanceof Sequence) {
			Matrix A = new Matrix((Sequence)o1);
			if (o2==null || o2 instanceof Number) {
				int b = A.getCols();
				if (o2 != null) {
					b = ((Number) o2).intValue();
				}
				if (option != null && option.contains("r")) {
					// ֱ�Ӷ�A��ά���ȱ�����һ���ò���
					FitResult fr = fit(A, b);
					Matrix result = transformj(fr, A);
					return result.toSequence(option, true);
				}
				else {
					// ����fit������������Ϊ���У���3����Ա��ɣ���1��Ϊ��ֵ����mu����2��ΪǱ���е����ɷַ���latent����3��Ϊ���ɷ�ϵ������coeff������������Ϊ�ڶ�������ִ��pca(A,X)
					FitResult fr = fit(A, b);
					Sequence result = new Sequence(3);
					result.add(fr.mu.toSequence());
					result.add(fr.latent.toSequence());
					result.add(fr.coeff.toSequence(option, true));
					return result;
				}
			}
			else if (o2 instanceof Sequence) {
				Sequence seq = (Sequence) o2;
				if (seq.length() >= 3) {
					o1 = seq.get(1);
					o2 = seq.get(2);
					Object o3 = seq.get(3);
					if (o1 instanceof Sequence && o2 instanceof Sequence && o3 instanceof Sequence) {
						Vector dv = new Vector((Sequence) o1);
						Vector dv2 = new Vector((Sequence) o2);
						Matrix dm = new Matrix((Sequence) o3);
						FitResult fr = new FitResult(dv, dv2, dm);
						Matrix result = transformj(fr, A);
						return result.toSequence(option, true);
					}
				}
			}
			MessageManager mm = EngineMessage.get();
			throw new RQException("pca" + mm.getMessage("function.paramTypeError"));
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("pca" + mm.getMessage("function.paramTypeError"));
		}
	}
    
	/**
	 * ��ȡЭ�������
	 * @param matrix
	 * @return
	 */
    private Matrix getVarianceMatrix(Matrix matrix) {
        int rows = matrix.getRows();
        int cols = matrix.getCols();
        double[][] result = new double[cols][cols];// Э�������
        for (int c = 0; c < cols; c++) {
            for (int c2 = 0; c2 < cols; c2++) {
                double temp = 0;
                for (int r = 0; r < rows; r++) {
                    temp += matrix.get(r, c) * matrix.get(r, c2);
                }
                result[c][c2] = temp / (rows - 1);
            }
        }
        return new Matrix(result);
    }

    /**
     * ��ȡ���ɷַ�������
     * @param primaryArray
     * @param eigenvalue
     * @param eigenVectors
     * @param n_components
     * @return
     */
    private Matrix getPrincipalComponent(Matrix eigenVectors, int n_components) {
        Matrix X = eigenVectors.transpose();
        double[][] tEigenVectors = X.getArray();
        double[][] principalArray = new double[n_components][];
        for (int i = 0; i < n_components; i++) {
            principalArray[i] = tEigenVectors[i];
        }

        return new Matrix(principalArray);
    }
    /*
    private Matrix getPrincipalComponent(double[] eigenvalueArray, Matrix eigenVectors, int n_components) {
        //edited by bd, 2021.4.9, ȥ��ejml��ʹ�ã������������jdk1.8�Ƚ��鷳
        Matrix X = eigenVectors.transpose();
        double[][] tEigenVectors = X.getArray();
        Map<Integer, double[]> principalMap = new HashMap<Integer, double[]>();// key=���ɷ�����ֵ��value=������ֵ��Ӧ����������
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
                Collections.reverseOrder());// key=����ֵ��value=��Ӧ��������������ʼ��Ϊ��ת����ʹmap��keyֵ��������

        for (int i = 0; i < tEigenVectors.length; i++) {
            double[] value = new double[tEigenVectors[0].length];
            value = tEigenVectors[i];
            eigenMap.put(eigenvalueArray[i], value);
        }

        // ѡ��ǰ�������ɷ�
        List<Double> plist = new ArrayList<Double>();// ���ɷ�����ֵ
        int now_component = 0;
        for (double key : eigenMap.keySet()) {
            if (now_component < n_components) {
                now_component += 1;
                plist.add(key);
                //principalComponentNum++;
            }
            else {
                break;
            }
        }

        // �����ɷ�map����������
        for (int i = 0; i < plist.size(); i++) {
            if (eigenMap.containsKey(plist.get(i))) {
                principalMap.put(i, eigenMap.get(plist.get(i)));
            }
        }

        // ��map���ֵ�浽��ά������
        double[][] principalArray = new double[principalMap.size()][];
        Iterator<Map.Entry<Integer, double[]>> it = principalMap.entrySet()
                .iterator();
        for (int i = 0; it.hasNext(); i++) {
            principalArray[i] = it.next().getValue();
        }

        return new Matrix(principalArray);
    }
    */
    
    // ѵ������
    protected FitResult fit(Matrix inputData, int n_components) {
    	// ȡÿ�о�ֵ�������Ļ�����ʹ��ÿ�о�ֵΪ0
        Vector averageVector = inputData.getAverage();
        Matrix averageArray = inputData.changeAverageToZero(averageVector);
        System.out.println(averageArray.toSequence(option, true).toString());
        // ����Э�������X(XT)
        Matrix varMatrix = averageArray.covm();
        System.out.println(varMatrix.toSequence(option, true).toString());
        // Э������������ֵ�ֽ�
        RealMatrix m = new org.apache.commons.math3.linear.Array2DRowRealMatrix(varMatrix.getArray());
        EigenDecomposition evd = new org.apache.commons.math3.linear.EigenDecomposition(m);
        double[] ev = evd.getRealEigenvalues();
        double[][] V = evd.getV().getData();
        
        //edited by bd, 2021.12.29, ���Է���math3������ֵ�ֽⲢ���ܱ�֤ev�������Ի���Ӧ���Ÿ���
        Sequence evSeq = (new Vector(ev)).toSequence();
        Sequence sortP = evSeq.psort("z");
        Sequence vSeq = (Sequence) new Matrix(V).toSequence(option, true);
        System.out.println("****************");
        System.out.println(n_components);
        System.out.println(vSeq.toString());
        vSeq = vSeq.get(sortP);
        System.out.println(vSeq.toString());
        evSeq = evSeq.get(sortP);
        Matrix principalArray = getPrincipalComponent(new Matrix(vSeq), n_components);
        System.out.println(principalArray.toSequence(option, true).toString());
        
        //edited by bd, 2021.12.29, ������ֵ�ֽ�ʱ���õ��������������������matlabͳһ
        dealV(principalArray); 
        System.out.println(principalArray.toSequence(option, true).toString());

        //Matrix principalArray = getPrincipalComponent(ev, new Matrix(V), n_components);
        return new FitResult(averageVector, new Vector(evSeq), principalArray.transpose());
    }
    
    private void dealV(Matrix V) {
    	//Ŀǰ����matlab������ֵ�����Ĺ�����ʹ�����о���ֵ����Ϊ������û�ҵ��ĵ�������������ʮ���������eig(vpa(A))�Ľ��������
    	double[][] vs = V.getArray();
    	int cols = V.getCols();
    	for (int i = 0, len = vs.length; i < len; i++) {
			int loc = 0;
			double max = Math.abs(vs[i][0]);
    		for (int j = 1; j < cols; j++) {
    			double tmp = Math.abs(vs[i][j]); 
    			if (tmp>max) {
    				max = tmp;
    				loc = j;
    			}
    		} 
    		if (vs[i][loc]<0) {
        		for (int j = 0; j < cols; j++) {
        			vs[i][j] = -vs[i][j];
        		} 
    		}
    	}
    }
    
//    ת������
    /**
     * ת������
     * @param principalDouble
     * @param averageObject
     * @param testinput
     * @return
     */
    public Matrix transform(Matrix principalMatrix, Vector averageObject, Matrix testinput){
        testinput.changeAverageToZero(averageObject);
        Matrix resultMatrix = testinput.times(principalMatrix);
        return resultMatrix;
    }

    //    ������ʹ��ת������
    /**
     * ����ѵ�����ת��
     * @param fr
     * @param testinput
     * @return
     */
    protected Matrix transformj(FitResult fr, Matrix testinput) {
    	testinput.changeAverageToZero(fr.mu);
        Matrix resultMatrix = testinput.times(fr.coeff);
        return resultMatrix;
    }
    
    protected class FitResult {
    	// mu
    	protected Vector mu;
    	// coeff
    	protected Matrix coeff;
    	// latent
    	protected Vector latent;
    	
    	protected FitResult(Vector mu, Vector latent, Matrix coeff) {
    		this.coeff = coeff;
    		this.mu = mu;
    		this.latent = latent;
    	}
    }
    
    public static void main(String[] args) {
    	double[][] data = new double[][]{{1,2,3,4},{2,3,1,2},{1,1,1,-1},{1,0,-2,-6}};
    	//double[][] data = new double[][] {{17,24,1,8,15},{23,5,7,14,16},{4,6,13,20,22},{10,12,19,21,3},{11,18,25,2,9}};
    	Sequence seq = new Sequence();
    	for (int i=0, ilen=data.length; i<ilen;i++) {
    		double[] data1 = data[i];
        	Sequence sub = new Sequence();
    		for (int j = 0, jlen = data1.length; j < jlen; j++) {
    			sub.add(data1[j]);
    		}
    		seq.add(sub);
    	}
    	Matrix A = new Matrix(data);
    	PCA pca = new PCA();
    	//pca.fitj(A, 3);
    	FitResult fr = pca.fit(A, 3);
    	System.out.println(fr.mu.toSequence().toString());
    	System.out.println(fr.latent.toSequence().toString());
    	System.out.println(fr.coeff.toSequence(pca.option, true).toString());
    	
        RealMatrix m = new org.apache.commons.math3.linear.Array2DRowRealMatrix(data);
        EigenDecomposition evd = new org.apache.commons.math3.linear.EigenDecomposition(m);
        double[] ev = evd.getRealEigenvalues();
        double[][] V = evd.getV().getData();
        double[] iev = evd.getImagEigenvalues();
    	System.out.println((new Vector(ev)).toSequence().toString());
    	System.out.println((new Vector(iev)).toSequence().toString());
    	System.out.println((new Matrix(V)).toSequence(pca.option, true).toString());
    	System.out.println("--done--");
    }
    
    

 // ѵ������
     /**
      * ѵ������
      * @param inputData	ѵ������
      * @param n_components
      * @return
      */
    protected void fitj(Matrix inputData, int n_components) {
        Vector averageVector = inputData.getAverage();
        Matrix averageArray = inputData.changeAverageToZero();
        Matrix varMatrix = getVarianceMatrix(averageArray);
        Vector eigValueR = getEigenvalueMatrix2(varMatrix);

        Matrix eigVectorR = getEigenVectorMatrix2(varMatrix);
        List<Matrix>  resultList =  merge(eigValueR,eigVectorR);
        Matrix eigenvalueMatrix = resultList.get(0);
        Matrix eigenVectorMatrix = resultList.get(1);
        Matrix principalArray = getPrincipalComponent2(inputData, eigenvalueMatrix, eigenVectorMatrix, n_components);
        //Object[] dimenRedut = PCA.ArrToOb(principalArray);
        //Object[] result = mergeObject(averageVector,dimenRedut);
        //return principalArray;
        //return new FitResult(averageVector, eigValueR, principalArray);
		System.out.println(averageVector.toSequence().toString());
		System.out.println(eigValueR.toSequence().toString());
		System.out.println(principalArray.toSequence(option, true).toString());
    }
     

    /**
     * ��ȡ���ɷַ�������
     * @param primaryArray
     * @param eigenvalue
     * @param eigenVectors
     * @param n_components
     * @return
     */
    private Matrix getPrincipalComponent2(Matrix primaryArray,
                                                   Matrix eigenvalue, Matrix eigenVectors, int n_components) {
        SimpleMatrix X = new SimpleMatrix(eigenVectors.getArray()).transpose();
        double[][] tEigenVectors = getArray(X);
//        Matrix A = new Matrix(eigenVectors);// ����һ��������������
//        double[][] tEigenVectors = A.transpose().getArray();// ��������ת��
        Map<Integer, double[]> principalMap = new HashMap<Integer, double[]>();// key=���ɷ�����ֵ��value=������ֵ��Ӧ����������
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
                Collections.reverseOrder());// key=����ֵ��value=��Ӧ��������������ʼ��Ϊ��ת����ʹmap��keyֵ��������
        //double total = 0;// �洢����ֵ�ܺ�
        int index = 0, n = eigenvalue.getRows();
        double[] eigenvalueArray = new double[n];// ������ֵ����Խ����ϵ�Ԫ�طŵ�����eigenvalueArray��
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    eigenvalueArray[index] = eigenvalue.get(i, j);
            }
            index++;
        }

        for (int i = 0; i < tEigenVectors.length; i++) {
            double[] value = new double[tEigenVectors[0].length];
            value = tEigenVectors[i];
            eigenMap.put(eigenvalueArray[i], value);
        }

        // �������ܺ�
        //for (int i = 0; i < n; i++) {
        //    total += eigenvalueArray[i];
        //}
        // ѡ��ǰ�������ɷ�
        //double temp = 0;
        //int principalComponentNum = 0;// ���ɷ���
        List<Double> plist = new ArrayList<Double>();// ���ɷ�����ֵ
        int now_component = 0;
        for (double key : eigenMap.keySet()) {
            if (now_component < n_components) {
                now_component += 1;
                plist.add(key);
                //principalComponentNum++;
            }
            else {
                break;
            }
        }

        // �����ɷ�map����������
        for (int i = 0; i < plist.size(); i++) {
            if (eigenMap.containsKey(plist.get(i))) {
                principalMap.put(i, eigenMap.get(plist.get(i)));
            }
        }

        // ��map���ֵ�浽��ά������
        double[][] principalArray = new double[principalMap.size()][];
        Iterator<Map.Entry<Integer, double[]>> it = principalMap.entrySet()
                .iterator();
        for (int i = 0; it.hasNext(); i++) {
            principalArray[i] = it.next().getValue();
        }

        return new Matrix(principalArray);
    }

    /**
     * ��ȡ������������
     * @param matrix	Դ����
     * @return
     */
    private Matrix getEigenVectorMatrix2(Matrix matrix) {
        SimpleMatrix X = new SimpleMatrix(matrix.getArray());
        SimpleEVD<SimpleMatrix> U = X.eig();
        org.ejml.interfaces.decomposition.EigenDecomposition<?> aa = U.getEVD();
        double[][] result = new double[matrix.getRows()][matrix.getRows()];
        for (int i =0; i<result.length;i++){
            org.ejml.data.Matrix cc = aa.getEigenVector(i);
            DMatrixRMaj dd =  (DMatrixRMaj ) cc;
            double[] singleMatrix = dd.data;
            result[i] = singleMatrix;
        }
        double[][] result1 = new double[matrix.getRows()][matrix.getRows()];
        for (int i =0; i<result.length;i++){
            for (int j =0; j<result.length;j++){
                result1[i][result.length-1-j] = result[j][i];
            }
        }

        return new Matrix(result1);
    }
    
    /**
     * ��ȡ����ֵ����
     * @param matrix	Դ����
     * @return
     */
    private Vector getEigenvalueMatrix2(Matrix matrix) {
        SimpleMatrix X = new SimpleMatrix(matrix.getArray());
        SimpleEVD<SimpleMatrix> U = X.eig();
        Object[] bb =  U.getEigenvalues().toArray();
        Vector cc = ejmloneObToVector(bb);
//        Arrays.sort(cc);
//        // ������ֵ��ɵĶԽǾ���,eig()��ȡ����ֵ
        double[] result = new double[cc.len()];
        for (int i =0, iSize = cc.len(); i<iSize;i++){
            result[i] = cc.get(result.length-1-i);
        }
        return new Vector(result);
    }
    
    private Vector ejmloneObToVector(Object []pracdata) {

        Object[] toss = (Object[]) pracdata;
        List<Object> seconds = Arrays.asList(toss);

        double[]testData = new double[toss.length];

        for (int i =0; i< toss.length; i++) {
            Object bb = seconds.get(i);
            Complex_F64 cc = (Complex_F64) bb;
            testData[i] = cc.real;
        }
        return new Vector(testData);
    }
	
	/**
	 * ��ʼ������
	 * @param value	��ά�����ʾ�ľ���ֵ
	 */
	protected double[][] getArray(SimpleMatrix smatrix) {
		int rows = smatrix.numRows();
		int cols = smatrix.numCols();
		double[][] A = new double[rows][cols];
        for (int r = 0; r < rows ; r++) {
            for (int c = 0; c < cols; c++) {
            	A[r][c] = smatrix.get(r, c);
            }
        }
        return A;
	}
    /**
     * 
     * @param eigenvalueMatrix	
     * @param eigenVectorMatrix	������������
     * @return
     */
    private List<Matrix> merge(Vector eigenvalueMatrix, Matrix eigenVectorMatrix) {
        double[][] eigVectorT = eigenVectorMatrix.transpose().getArray();
        TreeMap<Double, double[]> eigenMap = new TreeMap<Double, double[]>(
        );//
        for (int i = 0; i < eigVectorT.length; i++) {
            double[] value = eigVectorT[i];
            eigenMap.put(eigenvalueMatrix.get(i), value);
        }
        int len = eigenvalueMatrix.len();
        double[] eigValue = new double[len];
        double[][] eigValueR = new double[len][len];



        double[][] eigVector = new double[eigVectorT.length][eigVectorT[0].length];
        int i = 0;
        for (double key : eigenMap.keySet()) {
            eigValue[i] = key;
            eigVector[i] = eigenMap.get(key);
            i++;
        }
        for (int j =0; j<len; j++){
            eigValueR[j][j] = eigValue[j];
        }
        Matrix eigVectorR = new Matrix(eigVector).transpose();

        List<Matrix> result = new ArrayList<Matrix>();
        result.add(new Matrix(eigValueR));
        result.add(eigVectorR);
        return result;
    }
}
