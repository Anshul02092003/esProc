package com.scudata.lib.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.fn.algebra.Matrix;
import com.scudata.resources.EngineMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class KMeans extends Function {

    public Object calculate (Context ctx) {

        if (param == null) {
            MessageManager mm = EngineMessage.get();
            throw new RQException("KMeans" + mm.getMessage("function.missingParam"));
        }

        if (param.isLeaf()) {
            MessageManager mm = EngineMessage.get();
            throw new RQException("KMeans" + mm.getMessage("function.invalidParam"));
        } else {
            if (param.getSubSize() == 2) {
                //������2����Ա��ɣ����������
                IParam sub1 = param.getSub(0);
                IParam sub2 = param.getSub(1);

                if (sub1 == null || sub2 == null) {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("KMeans" + mm.getMessage("function.invalidParam"));
                }

                Object o1 = sub1.getLeafExpression().calculate(ctx); //A
                Object o2 = sub2.getLeafExpression().calculate(ctx); //k


                //���1��o1��o2�������У���Ϊo1�����е�ѵ�������o2����Ԥ������ݣ�����ֱ��Ԥ��,Ϊ���ж���䷽�㣬�ͷ�����ǰ��
                if (o1 instanceof Sequence && o2 instanceof Sequence){
                    ArrayList<HashMap<String ,Double>> fitResult = SeqToArrLstHashMap((Sequence) o1);
                    Matrix B = new Matrix((Sequence) o2);
                    Sequence preResult = predict(fitResult,B);
                    return preResult;
                }
                //���2��ֱ�ӷ���fit�Ľ������������
                else if (o1 instanceof Sequence){
                    Matrix A = new Matrix((Sequence) o1);
                    int k=0;
                    if(o2 ==null){
                        k = 2;
                    }else if(o2 instanceof Number){
                        k = ((Number) o2).intValue();
                    }
                    Sequence fitResult = fit (A,k);
                    return fitResult;

                }
                else {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("KMeans" + mm.getMessage("function.invalidParam"));
                }

            }
            //���3���û�����3��������ѵ����Ԥ����������
            else if(param.getSubSize() == 3){
                IParam sub1 = param.getSub(0);
                IParam sub2 = param.getSub(1);
                IParam sub3 = param.getSub(2);
                if (sub1 == null || sub2 == null || sub3 == null ) {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("KMeans" + mm.getMessage("function.invalidParam"));
                }
                Object o1 = sub1.getLeafExpression().calculate(ctx); //A
                Object o2 = sub2.getLeafExpression().calculate(ctx); //k
                Object o3 = sub3.getLeafExpression().calculate(ctx); //B
                if(o1 instanceof Sequence && o2 instanceof Number && o3 instanceof Sequence){
                    Matrix A = new Matrix((Sequence) o1);
                    int k= ((Number) o2).intValue();
                    Matrix B = new Matrix((Sequence) o3);
                    Sequence fitResult= fit(A,k);
                    Sequence preResult = predict(SeqToArrLstHashMap(fitResult),B);
                    return preResult;
                }
            }
            MessageManager mm = EngineMessage.get();
            throw new RQException("KMeans" + mm.getMessage("function.paramTypeError"));
        }
    }


    private final Random random = new Random();

    public Sequence fit(Matrix inputdata, int n_clusters){

        ArrayList<HashMap<String, Double>> data = dataPreProcess(inputdata); //���ݲ�ִ���
        ArrayList<HashMap<String,Double>> centroids = kmeans(n_clusters,data);
        return arrlstHaspMapToSequence(centroids);
    }

    public Sequence predict(ArrayList<HashMap<String,Double>> preCentroids,Matrix preInput){
        //preCentroids������fit���Ҳ�����Ƕ����ֵ���룬����������
        ArrayList<HashMap<String, Double>>  data = dataPreProcess(preInput); //�������·ֽ�
        for(HashMap<String, Double>  perData : data){
            Double minDist = Double.MAX_VALUE;
            // �ҵ���������������Ĳ�����¼��ӵ����ļ�Ⱥ��
            for(int i=0,iLen = preCentroids.size(); i<iLen; i++){
                Double dist = euclideanDistance(preCentroids.get(i), perData);
                if(dist<minDist){
                    minDist = dist;
                    perData.put("clusterNo",new Double(i));
                }
            }
        }
        return dou2ToSequence(createDouble(data));
    }


    static final Double PRECISION = 0.0;

    //�������г�ʼ��k������
    protected ArrayList<HashMap<String, Double>> kmeanspp(int K, ArrayList<HashMap<String, Double>> data){
        ArrayList<HashMap<String,Double>> centroids = new ArrayList<HashMap<String,Double>>();
        centroids.add(randomFromDataSet( data)); //���records�Ĳ���

        for(int i=1; i<K; i++){
            centroids.add(calculateWeighedCentroid(data));
        }
        return centroids;
    }

    protected ArrayList<HashMap<String, Double>> kmeans(int K,ArrayList<HashMap<String, Double>>  data){
        ArrayList<HashMap<String,Double>> centroids = new ArrayList<HashMap<String,Double>>();
        // ѡ��k����ʼ����
        centroids = kmeanspp(K,data);

        // ��ƽ�����ͳ�ʼ��Ϊ���ֵ��ÿ�ε���ʱ���͸�ֵ
        Double SSE = Double.MAX_VALUE;
        while (true) {
            // ���۲�ֵ���������
//            ArrayList<HashMap<String, Double>> data = new ArrayList<HashMap<String, Double>>();
            for(HashMap<String, Double> perData: data){
                Double minDist = Double.MAX_VALUE;
                // �ҵ��������������Ĳ���¼���伯Ⱥ��
                for(int i=0,iLen = centroids.size(); i<iLen; i++){
                    Double dist =euclideanDistance(centroids.get(i),perData);
                    if(dist<minDist){
                        minDist = dist;
                        perData.put("clusterNo",new Double(i));
                    }
                }
            }
            // �����µļ�Ⱥ�������¼�������
            centroids = recomputeCentroids(K,data);

            // �˳�������sse�仯С��precision����
            Double newSSE = calculateTotalSSE(centroids,data);
            if(SSE-newSSE <= PRECISION){
                break;
            }
            SSE = newSSE;
        }
        return centroids;
    }



    public ArrayList<String> getAttrNames(Matrix inputX){
        ArrayList<String> attrNames = new ArrayList<String>();

//        ArrayList<HashMap<String, Double>> records = new ArrayList<HashMap<String, Double>>();
        StringBuffer buf = new StringBuffer();
        int rows = inputX.getRows();
        int cols = inputX.getCols();
        for (int attrName = 0; attrName < cols; attrName += 1) {
            buf.append(Integer.toString(attrName));
            buf.append(',');
        }

        String row = buf.toString();
        String[] data = row.split(",");
        Collections.addAll(attrNames, data);
        return attrNames;

    }

    //����תarraylist
    protected static ArrayList<HashMap<String, Double>> dataPreProcess(Matrix inputX) {
        ArrayList<String> attrNames = new ArrayList<String>();

        ArrayList<HashMap<String, Double>> data = new ArrayList<HashMap<String, Double>>();
        StringBuffer buf = new StringBuffer();
        int rows = inputX.getRows();
        int cols = inputX.getCols();
        for (int attrName = 0; attrName < cols; attrName += 1) {
            buf.append(Integer.toString(attrName));
            buf.append(',');
        }

        String row = buf.toString();
        String[] row_data = row.split(",");
        Collections.addAll(attrNames, row_data);
        for (int i = 0; i < rows; i += 1) {
            HashMap<String, Double> perdata = new HashMap<String, Double>();
            for (int j = 0; j < cols; j += 1) {
                String name = attrNames.get(j);
                perdata.put(name,inputX.get(i, j));
                updateMin(name,inputX.get(i,j));
                updateMax(name,inputX.get(i,j));
            }
            data.add(perdata);
        }

        return data;
    }

    //��ȡֵ
    private double[][] createDouble(ArrayList<HashMap<String, Double>> data){
        double[][] result = new double[data.size()][1];
        int i = 0;
        for (HashMap<String, Double> perdata:data){
            result[i][0] = perdata.get("clusterNo");
            i += 1;
        }
        return result;

    }


    private static void updateMin(String name, Double val){
        HashMap<String, Double> minimums = new HashMap<String, Double>();
        if(minimums.containsKey(name)){
            if(val < minimums.get(name)){
                minimums.put(name, val);
            }
        } else{
            minimums.put(name, val);
        }
    }

    private static void updateMax(String name, Double val){
        HashMap<String ,Double> maximums = new HashMap<String ,Double>();
        if(maximums.containsKey(name)){
            if(val > maximums.get(name)){
                maximums.put(name, val);
            }
        } else{
            maximums.put(name, val);
        }
    }

    private Double meanOfAttr(String attrName, ArrayList<Integer> indices,ArrayList<HashMap<String, Double>> data){
        Double sum = 0.0;
        for(int i : indices){
            if(i<data.size()){
                sum += data.get(i).get(attrName);
            }
        }
        return sum / indices.size();
    }

    private HashMap<String, Double> calculateCentroid(int clusterNo,ArrayList<HashMap<String, Double>> data){
//        ArrayList<String> attrNames = data.get(0).keySet();
        HashMap<String, Double> centroid = new HashMap<String, Double>();
        ArrayList<Integer> recsInCluster = new ArrayList<Integer>();
        for(int i=0,iLen =data.size(); i<iLen; i++){
            HashMap<String, Double> perdata = data.get(i);
            if(perdata.get("clusterNo") == clusterNo){
                recsInCluster.add(i);
            }
        }
        for(String name : data.get(0).keySet()){
            centroid.put(name, meanOfAttr(name, recsInCluster,data));
        }
        return centroid;
    }

    private ArrayList<HashMap<String,Double>> recomputeCentroids(int K,ArrayList<HashMap<String, Double>> data){
        ArrayList<HashMap<String ,Double>> centroids = new ArrayList<HashMap<String ,Double>>();
        for(int i=0; i<K; i++){
            centroids.add(calculateCentroid(i,data));
        }
        return centroids;
    }


    private HashMap<String, Double> randomFromDataSet(ArrayList<HashMap<String, Double>> data){
        int index = random.nextInt(data.size());
        return data.get(index);
    }

    private Double euclideanDistance(HashMap<String, Double> a, HashMap<String, Double> b){
//        if(!a.keySet().equals(b.keySet())){
//            return Double.POSITIVE_INFINITY;
//        }
        double sum = 0.0;
        for(String attrName : a.keySet()){
            if (attrName != "clusterNo"){
                sum += Math.pow(a.get(attrName) - b.get(attrName), 2);
            }}
        return Math.sqrt(sum);
    }


    private Double calculateClusterSSE(HashMap<String, Double> centroid, int clusterNo, ArrayList<HashMap<String, Double>> data ){
        double SSE = 0.0;
        for(int i=0; i<data.size(); i++){
            if(data.get(i).get("clusterNo") == clusterNo){
                SSE += Math.pow(euclideanDistance(centroid, data.get(i)), 2);
            }
        }
        return SSE;
    }


    private Double calculateTotalSSE(ArrayList<HashMap<String,Double>> centroids, ArrayList<HashMap<String, Double>> data ){

        Double SSE = 0.0;
        for(int i=0; i<centroids.size(); i++) {
            SSE += calculateClusterSSE(centroids.get(i), i,data);
        }
        return SSE;
    }


    private HashMap<String,Double> calculateWeighedCentroid( ArrayList<HashMap<String, Double>> data){
        ArrayList<Integer> indicesOfCentroids = new ArrayList<Integer>();
        double sum = 0.0;
        for(int i=0; i<data.size(); i++){
            if(!indicesOfCentroids.contains(i)){
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(data.get(i), data.get(ind));
                    if(dist<minDist)
                        minDist = dist;
                }
                if(indicesOfCentroids.isEmpty())
                    sum = 0.0;
                sum += minDist;
            }
        }

        double threshold = sum * random.nextDouble();

        for(int i=0,iLen =data.size(); i<iLen; i++){
            if(!indicesOfCentroids.contains(i)){
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(data.get(i), data.get(ind));
                    if(dist<minDist)
                        minDist = dist;
                }
                sum += minDist;

                if(sum > threshold){
                    indicesOfCentroids.add(i);
                    return data.get(i);
                }
            }
        }

        return new HashMap<String,Double>();
    }

    //arraylistת��ΪSequence
//    private static Sequence arrlstToSequence(ArrayList<HashMap<String, Double>> centors){
//        double[][] centorsDouble = new double[centors.size()][centors.get(0).size()];
//        for(int i =0;i< centors.size();i++){
//            for(int j =0;j<centors.get(0).size()-1;j++){
//                centorsDouble[i][j] = centors.get(i).get(Integer.toString(j));
//            }
//        }
//        return dou2ToSequence(centorsDouble);
//    }

    protected static Sequence arrlstHaspMapToSequence(ArrayList<HashMap<String, Double>> centors){
        Sequence result = new Sequence();
        for (int i=0,iLen = centors.size();i<iLen;i++){
            Sequence resultLevel1 = new Sequence();

            for(String key :centors.get(i).keySet()){
                Sequence resultLevel2 = new Sequence();
                resultLevel2.add(key);
                resultLevel2.add(centors.get(i).get(key));
                resultLevel1.add(resultLevel2);
                }
            result.add(resultLevel1);

            }
        return result;
    }

    //double[][]תSequence
    protected static Sequence dou2ToSequence(double[][] d){
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

    //SequenceתArrayList
    protected static ArrayList<HashMap<String,Double>> SeqToArrLstHashMap(Sequence fitResult){
        ArrayList<HashMap<String,Double>> result = new ArrayList<HashMap<String,Double>>();
            for (int i = 1; i <= fitResult.length(); i++) {
                Object oLevel1 = fitResult.get(i);
                Sequence resultLevel1 = (Sequence) oLevel1;
                HashMap<String,Double> centor= new HashMap<String,Double>();
                for (int j = 1; j <= resultLevel1.length(); j++) {
                    Object oLevel2 = resultLevel1.get(j);
                    Sequence resultLevel2 = (Sequence) oLevel2;
                    Object keyObject = resultLevel2.get(1);
                    String key =(String) keyObject;
                    Object valueObject = resultLevel2.get(2);
                    double value = ((Number) valueObject).doubleValue();
                    centor.put(key,value);
                }
          result.add(centor);}
            return result;


    }


    public static void main(String[] args) {
        double[][] data = new double[][]{{1,2,3,4},{2,3,1,2},{1,1,1,-1},{1,0,-2,-6}};
        double[][] testData = new double[][]{{6,2,3,5},{0,3,1,5},{1,2,1,-1},{1,5,2,-6}};

//        //condition 1,����1Ϊ���飬����2Ϊ����
//        // �û�����������A��������fit��predict
        // ѵ��
        Matrix A = new Matrix(data);
        KMeans model = new KMeans();

        Sequence fitResult = model.fit(A,2);//fit���صĽ�����ģ����ڴ�fit����ת��������


        Matrix matrixPreData = new Matrix(testData);//Ԥ����Ҫmatrix����

        ArrayList<HashMap<String,Double>> seqFitResult = SeqToArrLstHashMap(fitResult);
        Sequence preResult = new Sequence(model.predict(seqFitResult,matrixPreData));

        int a=1;   //debug��


    }
}
