package com.scudata.lib.math;

import com.scudata.common.Logger;
import com.scudata.dm.Sequence;
import com.scudata.expression.fn.algebra.Matrix;

import java.util.HashMap;

/**
 * ������������������
 * 1�����츴����
 * 2��double[][]���ComBase[]
 * 3��double[]��double[]���ComBase[]
 * 4��Sequence��Sequence���ComBase[]
 * 5��ComBase[]�����ַ���
 * 6��ComBaseתΪSequence
 * 7��double[][]תSequence
 * 8��Sequenceתdouble[][]
 * 9��Sequenceתdouble[]
 * 10��double[]תSequence
 * 11��SequenceתComBase[]
 * 12����������ֵ����λ�ǡ������������ָ������ȡ�鲿����ȡʵ������������顢���ź�����ƽ����λ��
 */
public class ComBase {
    protected double real = 0;
    protected double imaginary = 0;

    //��ʼ��
    public ComBase(double r,double i){
        this.real =r;
        this.imaginary = i;
    }

    //��ʼ��
    public ComBase(Object r,Object i){
        if(r instanceof Number){
            this.real = ((Number) r).doubleValue();
        }
        if(i instanceof Number){
            this.imaginary = ((Number) i).doubleValue();
        }
    }

    //double[][]���ComBase[]
    protected static ComBase[] createCom(double[][] data){
        ComBase[] complexData = new ComBase[data.length];
        ComBase co = null;
        for(int i=0;i< data.length;i++){
            //��������������ֻ��һλ������Ĭ����Ϊʵ��
            if(data[i].length<2){
                co = new ComBase(data[i][0],0);
            }//������λ���������������
            else if(data[i].length>2){
                Logger.warn("The length of data in toStr() should be less than or equal to 2");
                co = new ComBase(data[i][0],data[i][1]);
            }
            else{
                co = new ComBase(data[i][0],data[i][1]);
            }
            complexData[i] = co;
        }
        return complexData;
    }

    //double[]��double[]���ComBase[]����δ�õ����ȱ���
    protected static ComBase[] createCom(double[] realPart, double[] imaginePart){
        //����û�����ʵ�����鲿�ֿ�
        int realLen =realPart.length;
        int imagineLen = imaginePart.length;
        ComBase[] complexData =null;
        //ʵ�����鲿ͬ������
        if(realLen == imagineLen){
            complexData = new ComBase[realLen];
            for(int i=0;i<realLen;i++){
                complexData[i] = new ComBase(realPart[i],imaginePart[i]);
            }
        }
        //ʵ�������鲿
        else if(realLen < imagineLen){
            complexData = new ComBase[imagineLen];
            for(int i=0; i<realLen;i++){
                complexData[i] = new ComBase(realPart[i],imaginePart[i]);
            }
            for(int j=realLen;j<imagineLen;j++){
                complexData[j] = new ComBase(0,imaginePart[j]);
            }

        }
        //ʵ�������鲿
        else{
            complexData =  new ComBase[realLen];
            for(int i=0;i<imagineLen;i++){
                complexData[i] = new ComBase(realPart[i],imaginePart[i]);
            }
            for(int j=imagineLen;j<realLen;j++){
                complexData[j] = new ComBase(realPart[j],0);
            }
        }
        return complexData;
    }

    //Sequence��Sequence���ComBase[]
    protected static ComBase[] createCom(Sequence realPart, Sequence imaginePart){
        int realLen = realPart.length();
        int imagineLen = imaginePart.length();
        ComBase[] complexData = null;
        //ʵ�����鲿ͬ������
        if(realLen == imagineLen){
            complexData = new ComBase[realLen];
            for(int i=0;i<realLen;i++){
                complexData[i] = new ComBase(realPart.get(i+1),imaginePart.get(i+1));
            }
        }
        //ʵ�������鲿
        else if(realLen < imagineLen){
            complexData = new ComBase[imagineLen];
            for(int i =0;i < realLen;i++){
                complexData[i] = new ComBase(realPart.get(i+1),imaginePart.get(i+1));
            }
            for(int i = realLen;i<imagineLen;i++){
                complexData[i] = new ComBase(0,imaginePart.get(i+1));
            }
        }
        //ʵ�������鲿
        else{
            complexData = new ComBase[realLen];
            for(int i=0;i<imagineLen;i++){
                complexData[i] = new ComBase(realPart.get(i+1),imaginePart.get(i+1));
            }
            for(int i = realLen;i<realLen;i++){
                complexData[i] = new ComBase(realPart.get(i+1),0);
            }
        }
        return complexData;
    }

    //ComBase[]�����ַ���
    private final static String I = "i";
    private final static String ADD = "+";
    private final static String EMP = " "; //��������Ϊ��ʱ��ո�
    protected String toStr(){
        if(this.imaginary == 0)
            return Double.toString(this.real);
        else if(this.real == 0)
            return this.imaginary + I;
        else {
            if (imaginary < 0) {
                return this.real + EMP + this.imaginary + I;
            } else {
                return this.real + ADD + this.imaginary + I;
            }
        }
    }

    protected static String[] comToStr(ComBase[] com){
        String[] result = new String[com.length];
        for(int j = 0;j<com.length;j++){
            ComBase co = com[j];
            if(co == null){
                result[j] = "";
            }
            else{
                result[j] = co.toStr();
            }
        }
        return result;
    }


    //ComBaseתΪSequence
    protected Sequence toSeq(){
        Sequence seq = new Sequence(2);
        seq.add(this.real);
        seq.add(this.imaginary);
        return seq;
    }

    //ComBase[]תSequence
    protected static Sequence toSeq(ComBase[] com){
        Sequence result = new Sequence(com.length);
        for(int i=0,iLen = com.length;i<iLen;i++){
            ComBase co = com[i];
            if(co == null){
                result.add(null);
            }
            else{
                result.add(co.toSeq());
            }
        }
        return result;
    }


    //double[][]תSequence
    protected static Sequence toSeq(double[][] d){
        Sequence result = new Sequence();
        for (int i=0,iLen = d.length;i<iLen;i++){
            double[] data1 = d[i];
            Sequence seq = new Sequence();
            for(int j=0,jLen = data1.length;j<jLen;j++){
                seq.add(data1[j]);
            }
            result.add(seq);
        }
        return result;
    }

    //Sequenceתdouble[][]
    protected static double[][] toDbl2(Sequence s) {
        Matrix a = new Matrix(s);
        double[][] result = a.getArray();
        return result;
    }


    //Sequenceתdouble[]
    protected static double[] toDbl(Sequence s){
        int len = s.length();
        double[] result = new double[len];
        for(int i=1,iLen = len+1;i<iLen;i++){
            Object o = s.get(i);
            if(o instanceof Number){
                result[i-1] = ((Number) o).doubleValue();
            }else if(o instanceof String){
                result[i-1] = Double.valueOf((String) o);
            }
        }
        return result;
    }


    //double[]תSequence
    protected static Sequence toSeq(double[] doubles) {
        int len = doubles.length;
        Sequence seq = new Sequence(len);
        for (int i = 0; i < len; i++) {
            seq.add(doubles[i]);
        }
        return seq;
    }


    //SequenceתComBase[]
    protected static ComBase[] toCom(Sequence s){
        Matrix a = new Matrix(s);
        if(a.getCols() == 1){
            a = a.transpose();
        }
        double[][] result = a.getArray();
        return createCom(result);
    }

    //����ֵ
    protected double comAbs(){
        return Math.sqrt(this.real *this.real +this.imaginary *this.imaginary);
    }

    //��λ��
    protected double comAngle(){
        return Math.atan2(this.imaginary,this.real);
    }

    //������
    protected static ComBase[] comConj(ComBase[] cdata){
        ComBase[] conjResult = new ComBase[cdata.length];
        for(int i =0;i<cdata.length;i++){
            ComBase co = cdata[i];
            if(co.imaginary == 0){
                conjResult[i] = new ComBase(co.real,0);
            }else{
                conjResult[i] = new ComBase(co.real,-co.imaginary);
            }

        }
        return conjResult;
    }

    //������ָ��
    protected static ComBase[] comExp(ComBase[] cdata){
        int len = cdata.length;
        ComBase[] expResult = new ComBase[len];
        for(int i = 0;i <len;i++){
            ComBase co = cdata[i];
            expResult[i] = new ComBase((Math.exp(co.real))*(Math.cos(co.imaginary)),
                    (Math.exp(co.real)) * (Math.sin(co.imaginary)));
        }
        return expResult;
    }

    //��ȡ�鲿
    protected static Sequence comGetImage(ComBase[] cdata){
        int len = cdata.length;
        double[] resultDouble = new double[len];
        for(int i=0; i<len;i++){
            ComBase co = cdata[i];
            resultDouble[i] = co.imaginary;
        }
        return toSeq(resultDouble);
    }

    //��ȡʵ��
    protected static Sequence comGetReal(ComBase[] cdata){
        int len = cdata.length;
        double[] resultDouble = new double[len];
        for(int i=0; i<len;i++){
            ComBase co = cdata[i];
            resultDouble[i] = co.real;
        }
        return toSeq(resultDouble);
    }

    //���������
    protected static double[][] comPair(double[][] allConjData){
        //���ܷ��������飬��Ҫ��ԭ������������,���Ը���һ��һ��������
        double[][] cplxpairResult = allConjData.clone();
        int len = cplxpairResult.length;

        for (int i=0;i<len-1;i++){

            int index = i; //��ǵ�һ��Ϊ���Ƚϵ���

            for(int j=i+1;j<len;j++) { //�ӵ�i������������i���Ƚϴ�С

                double[] valueCompare = compareForCplxpair(cplxpairResult[index], cplxpairResult[j]); //�Ƚ��ҵ�Сֵ
                if (valueCompare == cplxpairResult[j]) { //���jλ�õ�ֵС���ͽ�����ǰ��ȥ
                    index = j;
                }
            }
            //�ҵ���Сֵ�󣬽���С��ֵ�ŵ���һ��λ�ã�������һ��ѭ��
            double[] temp = cplxpairResult[index];
            cplxpairResult[index] = cplxpairResult[i];
            cplxpairResult[i] = temp;
        }
        return cplxpairResult;
    }


    //����������ıȴ�С
    protected static double[] compareForCplxpair(double[] a,double[] b) {
        double[] compareResult = new double[2];
        //ʵ��һ��,�Ҵ��ڵ���0
        if (a[0] == b[0] && a[0]>=0){
            //{0,-6},{0,-7}���������{5��9},{5,0}���鲿����ֵ��ķ�ǰ��
            if (Math.abs(a[1]) > Math.abs(b[1])){
                compareResult = a;
            }else{
                compareResult= b;
            }
            //{0,7},{0,-7}�����
            if(Math.abs(a[1]) == Math.abs(b[1]) && a[1]<0){
                compareResult = a;
            }else if (Math.abs(a[1]) == Math.abs(b[1]) && b[1]<0){
                compareResult = b;
            }
        }

        //�鲿һ��
        if(a[1] == b[1]){
            if (a[0] >= b[0]){
                compareResult = b;
            }else{
                compareResult = a;
            }
        }

        //��ʵ����һ��
        if(a[0] != b[0] && a[1] != b[1]){
            //{0,-7},{3,2}
            if(a[1] !=0 && b[1]!=0){
                if (a[0] < b[0]){
                    compareResult = a;
                }else{
                    compareResult = b;
                }
            }
            //{4,-7},{3,0)
            if(a[1] ==0){
                compareResult = b;
            }else if(b[1] ==0){
                compareResult = a;
            }
        }
        return compareResult;
    }

    //�ж����������Ƿ�Ϊ�ɶԵĸ�����
    protected static Boolean judgePair(double[][] d){
        ComBase[] combases = ComBase.createCom(d);
        HashMap<String,Integer> recordCount = new HashMap<String,Integer>();

        for (ComBase combase : combases){
            ComBase reverseCombase = new ComBase(combase.real,-combase.imaginary);
            if (recordCount.containsKey(reverseCombase.toStr())) {
                Integer counts = recordCount.get(reverseCombase.toStr());
                counts -=1;
                recordCount.remove(reverseCombase.toStr());
                if (counts >0 ) {
                    recordCount.put(reverseCombase.toStr(),counts);
                }

            }
            else {
                if (recordCount.containsKey(combase.toStr())) {
                    Integer counts = recordCount.get(combase.toStr());
                    recordCount.remove(combase.toStr());
                    recordCount.put(combase.toStr(), counts + 1);
                }
                else {
                    recordCount.put(combase.toStr(), 1);
                }
            }

        }

        if (recordCount.isEmpty()){
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }


    //���ź���
    protected static ComBase[] comSign(ComBase[] cdata){
        ComBase[] result = new ComBase[cdata.length];
        for(int i = 0;i<cdata.length;i++){
            ComBase co = cdata[i];
            if(co.real == 0 & co.imaginary == 0){
                result[i] =new ComBase(0,0);
            }else{
                result[i] = new ComBase(co.real / co.comAbs(),co.imaginary/ co.comAbs());
            }
        }
        return result;
    }

    //ƽ����λ��
    protected static Sequence comUnwrap(double[] angleResult){
        int len =angleResult.length;
        double[] unwrapResult = new double[len];
        unwrapResult[0] = angleResult[0]; //��һ��λ�õĲ���
        for(int i=1;i<len;i++){
            double diff = Math.abs(angleResult[i]-angleResult[i-1]); //��һ����ǰһ����ֵ
            if (diff<Math.PI){
                unwrapResult[i] = angleResult[i];//��ֵС��piʱ������
            }else{
                double multiple = diff / (2*Math.PI); //ȡ��
                double remainder = diff % (2*Math.PI);  //ȡ��
                int move = (int) Math.floor(multiple); //�ƶ��ĵ�λ��multiple����ȡ��
                if(remainder >Math.PI){
                    move++;
                }

                if(angleResult[i] >unwrapResult[i-1]){
                    unwrapResult[i] = angleResult[i] - move*2*Math.PI;
                }else{
                    unwrapResult[i] = angleResult[i] + move*2*Math.PI;
                }

            }

        }
        return toSeq(unwrapResult);
    }

    //��ά����ƽ����λ��,���ά��˵���Ĳ���dim�������Ծ��ֵtol��matlabĬ����pi��Ŀǰ��������ֵ
    // dim = 1������;
    // dim = 2������;
    protected static Sequence comUnwrap(double[][] angleResultArr,double tol,int dim) {
        int row = angleResultArr.length;
        int col = angleResultArr[0].length;
        double[][] unwrapResult = new double[row][col];

        if(dim ==1){
            unwrapResult[0] = angleResultArr[0]; //��һ��λ�õĲ���
            for (int j = 0; j < col; j++) {//���н��м���
                for (int i = 1; i < row; i++) {
                    double diff = Math.abs(angleResultArr[i][j] - unwrapResult[i - 1][j]);
                    if (diff < tol) {
                        unwrapResult[i][j] = angleResultArr[i][j];
                    } else {
                        double multiple = diff / (2 * Math.PI); //ȡ��
                        double remainder = diff % (2 * Math.PI);  //ȡ��
                        int move = (int) Math.floor(multiple); //�ƶ��ĵ�λ��multiple����ȡ��
                        if (remainder > Math.PI) {
                            move++;
                        }
                        if (angleResultArr[i][j] > unwrapResult[i - 1][j]) {
                            unwrapResult[i][j] = angleResultArr[i][j] - move * 2 * Math.PI;
                        } else {
                            unwrapResult[i][j] = angleResultArr[i][j] + move * 2 * Math.PI;
                        }
                    }
                }
            }
        }else if(dim ==2){
            //��һ��λ�õĲ���
            for (int i = 0;i<row;i++){
                unwrapResult[i][0] = angleResultArr[i][0];
            }
            for(int i=0;i<row;i++){
                for (int j=1;j<col;j++){
                    double diff =Math.abs(angleResultArr[i][j] - unwrapResult[i][j-1]);
                    if (diff < tol){
                        unwrapResult[i][j] = angleResultArr[i][j];
                    }else{
                        double multiple = diff / (2 * Math.PI); //ȡ��
                        double remainder = diff % (2 * Math.PI);  //ȡ��
                        int move = (int) Math.floor(multiple); //�ƶ��ĵ�λ��multiple����ȡ��
                        if (remainder > Math.PI) {
                            move++;
                        }
                        if (angleResultArr[i][j] > unwrapResult[i][j-1]){
                            unwrapResult[i][j] = angleResultArr[i][j] - move *2*Math.PI;
                        }else{
                            unwrapResult[i][j] = angleResultArr[i][j] + move *2*Math.PI;
                        }
                    }
                }

            }
        }
        return toSeq(unwrapResult);
    }

}
