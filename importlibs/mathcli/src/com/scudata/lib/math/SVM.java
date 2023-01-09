package com.scudata.lib.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.fn.algebra.Matrix;
import com.scudata.resources.EngineMessage;
import libsvm.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SVM extends Function {

    public Object calculate (Context ctx) {
        if (param == null) {
            MessageManager mm = EngineMessage.get();
            throw new RQException("svm" + mm.getMessage("function.missingParam"));
        } else if (param.isLeaf()) {
            MessageManager mm = EngineMessage.get();
            throw new RQException("svm" + mm.getMessage("function.invalidParam"));
        } else {

            //�������������������ѵ��
            if (param.getSubSize() == 3) {
                // train������3����Ա��ɣ���1��Ϊ��ά���飬��2��Ϊһά���飬��3��Ϊ������һά����
                IParam sub1 = param.getSub(0);
                IParam sub2 = param.getSub(1);
                IParam sub3 = param.getSub(2);
                //����������һΪ�ն���Ч
                if (sub1 == null || sub2 == null || sub3 == null) {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("svm" + mm.getMessage("function.invalidParam"));
                }
                Object o1 = sub1.getLeafExpression().calculate(ctx);
                Object o2 = sub2.getLeafExpression().calculate(ctx);
                Object o3 = sub3.getLeafExpression().calculate(ctx);

                //������������ͬʱ����ſ��Լ��㣬����ѵ���Ľ�������ڴ����д���Ϊ��������
                if (o1 instanceof Sequence && o2 instanceof Sequence && o3 instanceof Sequence) {
                    Matrix x = new Matrix((Sequence) o1);
                    double[][] X = x.getArray();
                    double[] Y = SeqToDouble1((Sequence) o2);
                    double[] param = SeqToDouble1((Sequence) o3);

                    Sequence instanceData = train(X, Y, param);//����ģ�͵�����
                    return instanceData; //�����������͵Ľ��
                } else {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("svm" + mm.getMessage("function.invalidParam"));
                }
            }

            //�������4��������ѵ����Ԥ��ֱ������
            else if (param.getSubSize() == 4) {
                IParam sub1 = param.getSub(0);
                IParam sub2 = param.getSub(1);
                IParam sub3 = param.getSub(2);
                IParam sub4 = param.getSub(3);
                if (sub1 == null || sub2 == null || sub3 == null || sub4 == null) {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("svm" + mm.getMessage("function.invalidParam"));
                }
                Object o1 = sub1.getLeafExpression().calculate(ctx);
                Object o2 = sub2.getLeafExpression().calculate(ctx);
                Object o3 = sub3.getLeafExpression().calculate(ctx);
                Object o4 = sub4.getLeafExpression().calculate(ctx);

                if (o1 instanceof Sequence && o2 instanceof Sequence && o3 instanceof Sequence && o4 instanceof Sequence) {
                    Matrix x = new Matrix((Sequence) o1);
                    double[][] X = x.getArray();
//                    double[][] X = SeqToDouble2((Sequence) o1);
                    double[] Y = SeqToDouble1((Sequence) o2);
                    double[] param = SeqToDouble1((Sequence) o3); //����ѵ����Ҫ����

                    Matrix xPre = new Matrix((Sequence) o4);
                    double[][] XPre = xPre.getArray();//Ԥ������
//                    double[][] XPre = SeqToDouble2((Sequence) o4);

                    Sequence instanceData = train(X, Y, param);
                    Sequence result = predict(instanceData,XPre);
                    return result;
                } else {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("svm" + mm.getMessage("function.invalidParam"));
                }
            }

            //����ѵ��������ݵ��������ʱ����������һ����ѵ�������һ������Ԥ�����ݣ���ֱ��Ԥ��
            else if (param.getSubSize() == 2) {
                IParam sub1 = param.getSub(0);
                IParam sub2 = param.getSub(1);
                if (sub1 == null || sub2 == null) {
                    MessageManager mm = EngineMessage.get();
                    throw new RQException("svm" + mm.getMessage("function.invalidParam"));
                }
                Object o1 = sub1.getLeafExpression().calculate(ctx);
                Object o2 = sub2.getLeafExpression().calculate(ctx);
                if (o1 instanceof Sequence && o2 instanceof Sequence) {
                    //��ʱ��o1�����е�ѵ��������ݣ�o2����Ԥ������
                    Sequence insData = (Sequence) o1;

                    Matrix xPre = new Matrix((Sequence) o2);
                    double[][] XPre = xPre.getArray();//Ԥ������
//                    double[][] XPre = SeqToDouble2((Sequence) o2);//Ԥ������
                    Sequence result = predict(insData,XPre);
                    return result;
                }
            } else {
                MessageManager mm = EngineMessage.get();
                throw new RQException("svm" + mm.getMessage("function.invalidParam"));
            }
        }
        MessageManager mm = EngineMessage.get();
        throw new RQException("svm" + mm.getMessage("function.invalidParam"));
    }

    //double[]תΪsequence������Ԥ��ʱ���ת������������
    protected static Sequence dou1toSequence(double[] doubles) {
        int len = doubles.length;
        Sequence seq = new Sequence(len);
        for (int i = 0; i < len; i++) {
            seq.add(doubles[i]);
        }
        return seq;
    }

    //Sequenceתdouble[]��ѵ��ʱ����Y�����ת��
    protected static double[] SeqToDouble1(Sequence s){
//        int len = s.length();
//        double[] result = new double[len];
//        for(int i =1,iLen = len+1;i<iLen;i++){
//            result[i-1] = ((Number) s.get(i)).doubleValue();
//        }
//        return result;

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

    //double[][]תSequence��Ϊ����֤calculate��sequenceתdouble��ǰ��ת��
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

    //Sequenceתdouble[][],ѵ��ʱ����Xת���������ٵ�ֱ�������calculate���棬����ѵ������ʵ������Ҳ�õ�
    protected static double[][] SeqToDouble2(Sequence s) {
        Matrix a = new Matrix(s);
        double[][] result = a.getArray();
        return result;
    }


    //int[]תSequence,ѵ��ʵ���������Ϊ����ʱʹ��
    protected static Sequence int1ToSequence(int[] intDatas){
        if(intDatas == null){
            return null;
        }
        int len = intDatas.length;
        Sequence seq = new Sequence(len);
        for(int i = 0;i<len;i++){
            seq.add(intDatas[i]);
        }
        return seq;
    }

    //Sequenceתint[]��predictʱת��ʹ��
    protected static int[] SequenceToInt1(Sequence s){
        if(s == null){
            return null;
        }
        int len = s.length();
        int[] result = new int[len];
        for (int i=1,iLen = len +1;i<iLen;i++){
//            result[i-1] = (int) s.get(i);
            Object o = s.get(i);
            if(o instanceof Number){
                result[i-1] = ((Number) o).intValue();
            }else if(o instanceof String){
                result[i-1] = Integer.valueOf((String) o);
            }
        }
        return result;
    }

    //svm_node[][]תSequence
    protected static Sequence svmNode2ToSeq(svm_node[][] sNodes){
//        double[][][] svmNode= new double[sNodes.length][sNodes[0].length][2];
////        svmNode[0][0][0] =sNodes[0][0].index;
////        svmNode[0][0][1] = sNodes[0][0].value;
//        for(int i=0;i<sNodes.length;i++){
//            for(int j = 0;j<sNodes[0].length;j++){
//
//                svmNode[i][j][0] = sNodes[i][j].index;
//                svmNode[i][j][1] = sNodes[i][j].value;
        Sequence result = new Sequence();
        for (int i=0,iLen = sNodes.length;i<iLen;i++){
            svm_node[] data1 = sNodes[i];
            Sequence seq = new Sequence();
            for(int j=0,jLen = data1.length;j<jLen;j++){
                seq.add(data1[j].value);
            }
            result.add(seq);
        }
        return result;
    }

    //Sequenceתsvm_node[][]
    protected static svm_node[][] SeqToSvmNode2(Sequence s) {
        Matrix a = new Matrix(s);
        double[][] result = a.getArray();
        return dataProcess(result);
    }



    private static svm_node[][] dataProcess(double[][] X){
        //��������ת����libsvm���յĸ�ʽ��Yֱ�ӵ�����ǩ�þ��У���ʽ����
        int indexLength = X.length;//��
        int valueLength = X[0].length;//��
        //��double[][]���͵�Xת����libsvm��Ҫ��svm_node[]
        ArrayList<svm_node[]> nodeSet = new ArrayList<svm_node[]>();//������svm_node[]
//        ѵ������������
        for(int i=0;i<indexLength;i++){
            svm_node[] vector = new svm_node[valueLength];
            double[] datasForRows = X[i];
            for(int j= 0,jLen = datasForRows.length;j<jLen;j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = datasForRows[j];
                vector[j] = node;

            }
            nodeSet.add(vector);
        }

//        ��nodeSetת��Ϊsvm_node[][]
        svm_node[][] nodeDatas  = new svm_node[indexLength][valueLength];
        for(int i=0;i<indexLength;i++){
            for(int j=0;j<valueLength;j++){
                nodeDatas[i][j] = nodeSet.get(i)[j];
            }
        }
//        int a =1;
        return nodeDatas;

    }


    //���ⶨ��
//    public svm_problem problemSet(double[][] X,double[] Y){
//        svm_problem problem = new svm_problem();
//        svm_node[][] XDatas = dataProcess(X);
//        problem.l = XDatas.length; //��������
//        problem.x = XDatas; //ѵ����������
//        problem.y = Y; //��Ӧ��label����
//        return problem;
//    }

    //��������
//    private svm_parameter paramSet(int svm_type_set, int kernel_type_set,
//                                  int degree_set, double cache_size_set,
//                                  double eps_set, double C_set, double gamma_set,
//                                  double coef0_set, double nu_set,
//                                  double p_set, int nr_weight_set,
////                                  int[] weight_label_set, double[] weight_set,
//                                  int shrinking_set, int probability_set) {
//        svm_parameter params = new svm_parameter();
//        params.svm_type = svm_type_set;//��ѡ����ģ��
//        params.kernel_type = kernel_type_set;//�˺�������ѡ5�֣�Ĭ��rbf��˹��
//        params.degree = degree_set;//���ö���ʽ�˵ļ�����poly��Ĭ��3
//        params.cache_size = cache_size_set;//�ڴ�����
//        params.eps = eps_set;//����,��ֹ׼���еĿ�����ƫ�Ĭ��ֵΪ0.001
//        params.C = C_set;//���򻯲�����C-SVC����-SVR��nu-SVR�дӳͷ�ϵ��C��Ĭ��ֵΪ1����l2���򻯣�
//        params.gamma = gamma_set;//�˺�����gammaֵ��Ĭ����������֮һ��poly/rbf/sigmoid�ĺ�ϵ��
//        params.coef0 = coef0_set;//�˺����е�coef0��Ĭ��0��poly/sigmoid
//        params.nu = nu_set;//C-SVC��one-class-SVM��nu-SVR�в���n��Ĭ��ֵ0.5
//        params.p = p_set;//��-SVR
//        params.nr_weight = nr_weight_set;//�Ը��������ĳͷ�ϵ��C��Ȩ��Ĭ��ֵΪ1��C_SVC
////        params.weight_label = weight_label_set;//C_SVC
////        params.weight = weight_set;//C_SVC
//        params.shrinking = shrinking_set;//�Ƿ�ʹ����������ʽ������ֵ��Ĭ��ֵ0
//        params.probability = probability_set;//�Ƿ����SVC��SVR�ĸ��ʹ��ƣ�����ֵ����ѡֵ0��1��Ĭ��0
//        return params;
//    }



    private Sequence train(double[][] X, double[] Y, double[] param){
        svm_problem problem = new svm_problem();
        svm_node[][] XDatas = dataProcess(X);
        problem.l = XDatas.length; //��������
        problem.x = XDatas; //ѵ����������
        problem.y = Y; //��Ӧ��label����

        svm_parameter params =new svm_parameter();//����˵����ǰ�������������
        params.svm_type = (int) param[0];
        params.kernel_type = (int)param[1];
        params.degree = (int)param[2];
        params.cache_size = param[3];
        params.eps = param[4];
        params.C = param[5];
        params.gamma = param[6];
        params.coef0 = param[7];
        params.nu = param[8];
        params.p = param[9];
        params.nr_weight = (int) param[10];
        params.shrinking = (int) param[11];
        params.probability = (int) param[12];

        //�����weight_label��weight
        HashMap<Double, Integer> labelCount = new HashMap<Double, Integer>();
        for (double label : problem.y) {
            Integer num = labelCount.get(label);
            labelCount.put(label, num == null ? 1 : num + 1);
        }
        params.weight_label = new int[labelCount.size()];
        params.weight = new double[labelCount.size()];
        int weight_label_index=0;
        for (double label:labelCount.keySet()){
            params.weight_label[weight_label_index] =(int) label;
            // ƽ��Ȩ��
//            params.weight[weight_label_index] = 1.0/labelCount.keySet().size();
            // ��������ռ��Ȩ��
            params.weight[weight_label_index] = labelCount.get(label)*1.0/problem.y.length;
            weight_label_index +=1;
        }



        libsvm.svm.svm_check_parameter(problem,params); //��������趨

        svm_model model = libsvm.svm.svm_train(problem,params); //ѵ���õ�ģ��,ת��Ϊʵ���������
        double[][] modelSVM_coef = model.sv_coef; //֧��������Ӧ��Ȩ��
        int[] modelSVM_label = model.label;//��ǩ����
        int modelSVM_nr_class = model.nr_class;
        int modelSVM_l = model.l;
        int[] modelSVM_nSV = model.nSV; //ÿ��֧�������ĸ���
        double[] modelSVM_rho = model.rho;
        svm_node[][] modelSVM_SV = model.SV;

        Sequence instanceData = new Sequence();

        instanceData.add(dou2ToSequence(modelSVM_coef));//1
        instanceData.add(int1ToSequence(modelSVM_label));//2
        instanceData.add(modelSVM_nr_class); //3
        instanceData.add(modelSVM_l); //4
        instanceData.add(int1ToSequence(modelSVM_nSV));//5
        instanceData.add(dou1toSequence(modelSVM_rho));//6
        instanceData.add(svmNode2ToSeq(modelSVM_SV));//7
        instanceData.add(dou1toSequence(param));//8

        //Ȩ�ز��������û��λ�÷ţ�����ʵ�������У����û�չʾ�Ĳ���Ҳ������9��10����
        instanceData.add(int1ToSequence(params.weight_label));
        instanceData.add(dou1toSequence(params.weight));

//        int test = 1;
        return instanceData;
    }

    //Ԥ��
//    private double[] predict(double[][] XPre,svm_model model){
//        svm_node[][] XPreDatas = dataProcess(XPre);
//        double[] results = new double[XPreDatas.length];
//        for(int i=0;i< XPreDatas.length;i++){
//            double result = svm.svm_predict(model,XPreDatas[i]);
//            results[i] = result;
//        }
//        return results;
//    }

    private Sequence predict(Sequence instanceData,double[][] XPre){
        //ʵ������
        svm_model reorgModel = new svm_model();
        svm_parameter preParam = new svm_parameter();
        try {
            reorgModel.sv_coef = SeqToDouble2((Sequence) instanceData.get(1));
            reorgModel.label =SequenceToInt1((Sequence) instanceData.get(2));
//        reorgModel.nr_class = (int)instanceData.get(3);
            reorgModel.nr_class = ((Number)instanceData.get(3)).intValue();
//        reorgModel.l = (int)instanceData.get(4);
            reorgModel.l = ((Number)instanceData.get(4)).intValue();
            reorgModel.nSV = SequenceToInt1((Sequence) instanceData.get(5));
            reorgModel.rho = SeqToDouble1((Sequence) instanceData.get(6));
            reorgModel.SV = SeqToSvmNode2((Sequence) instanceData.get(7));
            preParam.weight_label = SequenceToInt1((Sequence) instanceData.get(9));//Ȩ�ز�����ʵ�������ж�ȡ
            preParam.weight = SeqToDouble1((Sequence) instanceData.get(10));
        }catch (Exception e){
            MessageManager mm = EngineMessage.get();
            throw new RQException("svm" + mm.getMessage("function.paramTypeError"));
        };


        //ģ��ʵ�����践�ز������ԣ���������13��
        double[] preParams = SeqToDouble1((Sequence) instanceData.get(8));

        preParam.svm_type = (int) preParams[0];
        preParam.kernel_type = (int) preParams[1];
        preParam.degree = (int)preParams[2];
        preParam.cache_size = preParams[3];
        preParam.eps = preParams[4];
        preParam.C = preParams[5];
        preParam.gamma = preParams[6];
        preParam.coef0 = preParams[7];
        preParam.nu = preParams[8];
        preParam.p = preParams[9];
        preParam.nr_weight = (int) preParams[10];
        preParam.shrinking = (int) preParams[11];
        preParam.probability = (int)preParams[12];


//        preParam.weight_label = null;
//        preParam.weight = null;
//        preParam.weight_label = new int[6];
//        preParam.weight_label[0] = 3;
//        preParam.weight_label[1] = 4;
//        preParam.weight_label[2] = 5;
//        preParam.weight_label[3] = 6;
//        preParam.weight_label[4] = 7;
//        preParam.weight_label[5] = 8;
//
//
//        preParam.weight = new double[6];
//        preParam.weight[0] = 0.1;
//        preParam.weight[1]= 0.1;
//        preParam.weight[2] = 0.1;
//        preParam.weight[3] = 0.3;
//        preParam.weight[4] = 0.2;
//        preParam.weight[5] = 0.2;

        reorgModel.param=preParam;


        svm_node[][] XPreDatas = dataProcess(XPre);
        double[] results = new double[XPreDatas.length];
        for(int i=0,iLen = XPreDatas.length ;i<iLen ;i++){
            double result = libsvm.svm.svm_predict(reorgModel,XPreDatas[i]);
            results[i] = result;
        }
        return dou1toSequence(results);
    }


    public static double[][]  readCSV(String pFilename,int rows,int cols) throws NumberFormatException, IOException {

        double[][] arr = new double[rows][cols];

        BufferedReader br = new BufferedReader(new FileReader(pFilename));
        String line = " ";

        int i = -1;
        while ((line = br.readLine())!= null && i < arr.length){
            String [] temp = line.split(","); //split spaces
            ++i;

            for (int j = 0; j<arr[i].length; j++) {
                arr[i][j] = Double.parseDouble(temp[j]);
            }

        }

        return arr;
    }

    public static void main(String[] args) throws IOException {

////        double[][] trainDataX = readCSV("testSVM_x.csv",284807 ,30);
////        double[][] trainDataY1 = readCSV("testSVM_y.csv",284807,1);
//        double[][] trainDataX = readCSV("wineTrainX.csv",3900 ,11);
//        double[][] trainDataY1 = readCSV("wineTrainY.csv",3900,1);
//        double[]  trainDataY = new double[3900];
//        for (int i =0;i<trainDataY1.length;i++){
//            trainDataY[i] = trainDataY1[i][0];
//        }
//
//        double[][] testDataX = readCSV("wineTestX.csv",998,11);
//        double[][] testDataY1 = readCSV("wineTestY.csv",998,1);//�������������֤������
//        double[] testDataY = new double[998];
//        for(int i=0;i<testDataY1.length;i++){
//            testDataY[i] = testDataY1[i][0];
//        }
////        double[] param = new double[]{3,0,3,100,0.00001,1.9,0,0,0.5,0.1,1,1,1};
//        //�������࣬��Ϊһ���������룬Ȼ����train�������ζ�ȡ�����ǲ������������ƣ�Ŀǰʹ����ǿ��ת��
//        double[] param = new double[]{1,2,3,100,0.00001,1,0.25,0,0.5,0.1,7,1,1};
//        svm svmModel = new svm();
//        Sequence trainModel =svmModel.train(trainDataX,trainDataY,param);
//        Sequence preResult = svmModel.predict(trainModel,testDataX);
//
//        //���������rmse
//        double[] preResultDouble = SeqToDouble1(preResult);//���ת��ʽ
//        double square = 0;
//        for(int i=0;i< testDataY.length;i++){
//            square += Math.pow((preResultDouble[i]-testDataY[i]),2); //ƽ����
//        }
//        double rmse = Math.sqrt(square/ testDataY.length);
//        int a=1;//debug��


        //�ع����
//        double[][] trainDataX = new double[][]{{17.6,17.7,17.7,17.7},{17.7,17.7,17.7,17.8},
//                {17.7,17.7,17.8,17.8}, {17.7,17.8,17.8,17.9},{17.8,17.8,17.9,18},
//                {17.8,17.9,18,18.1},{17.9,18,18.1,18.2}, {18,18.1,18.2,18.4},
//                {18.1,18.2,18.4,18.6},{18.2,18.4,18.6,18.7},{18.4,18.6,18.7,18.9},
//                {18.6,18.7,18.9,19.1}};
//        double[] trainDataY = new double[]{17.8,17.8,17.9,18,18.1,18.2,18.4,18.6,18.7,18.9,19.1,19.3};
//        //�������࣬��Ϊһ���������룬Ȼ����train�������ζ�ȡ�����ǲ������������ƣ�Ŀǰʹ����ǿ��ת��
//        double[] param = new double[]{1,2,3,100,0.00001,1,0.25,0,0.5,0.1,0,1,1};
//
//        double[][] predictDataX = new double[][]{{18.7,18.9,19.1,19.3},{18.9,19.1,19.3,19.6},
//                {19.1,19.3,19.6,19.9},{19.3,19.6,19.9,20.2},{19.6,19.9,20.2,20.6},
//                {19.9,20.2,20.6,21}, {20.2,20.6,21,21.5}};
//        double[] predictDataY = new double[]{19.6,19.9,20.2,20.6,21,21.5,22};//�������������֤������
//
//        svm svmModel = new svm();
//        Sequence trainModel =svmModel.train(trainDataX,trainDataY,param);
//        Sequence preResult = svmModel.predict(trainModel,predictDataX);


        //��������
        double[][] trainDataX = new double[][]{{7.0, 0.27, 0.36, 20.7, 0.045, 45.0, 170.0, 1.001, 3.0, 0.45, 8.8},
                {8.1, 0.27, 0.41, 1.45, 0.033, 11.0, 63.0, 0.9908, 2.99, 0.56, 12.0},
                {6.6, 0.16, 0.4, 1.5, 0.044, 48.0, 143.0, 0.9912, 3.54, 0.52, 12.4},
                {6.2, 0.66, 0.48, 1.2, 0.029, 29.0, 75.0, 0.9892, 3.33, 0.39, 12.8},
                {6.2,0.45,0.26,4.4,0.063,63.0,206.0,0.994,3.27,0.52,9.8},
                {8.5,0.26,0.21,16.2,0.074,41.0,197.0,0.998,3.02,0.5,9.8},
                {7.2,0.23,0.32,8.5,0.058,47.0,186.0,0.9956,3.19,0.4,9.9},
                {8.6,0.23,0.4,4.2,0.035,17.0,109.0,0.9947,3.14,0.53,9.7},
                {6.2,0.66,0.48,1.2,0.029,29.0,75.0,0.9892,3.33,0.39,12.8},
                {7.2,0.32,0.36,2.0,0.033,37.0,114.0,0.9906,3.1,0.71,12.3},
                {6.2,0.45,0.26,4.4,0.063,63.0,206.0,0.994,3.27,0.52,9.8},
                {9.8,0.36,0.46,10.5,0.038,4.0,83.0,0.9956,2.89,0.3,10.1},
                {7.1,0.32,0.32,11.0,0.038,16.0,66.0,0.9937,3.24,0.4,11.5},
                {9.1,0.27,0.45,10.6,0.035,28.0,124.0,0.997,3.2,0.46,10.4}};
        double[] trainDataY = new double[]{6,5,7,8,4,3,6,5,8,7,4,4,3,9};
        //�������࣬��Ϊһ���������룬Ȼ����train�������ζ�ȡ�����ǲ������������ƣ�Ŀǰʹ����ǿ��ת��
        double[] param = new double[]{1,2,3,100,0.00001,1,0.25,0,0.5,0.1,7,1,1};

        double[][] predictDataX = new double[][]{{7.0,0.34,0.1,3.5,0.044,17.0,63.0,0.9937,3.01,0.39,9.2},
                {4.8,0.65,0.12,1.1,0.013,4.0,10.0,0.99246,3.32,0.36,13.5},
                {6.1,0.22,0.38,2.8,0.144,12.0,65.0,0.9908,2.95,0.64,11.4},
                {5.8,0.27,0.26,3.5,0.071,26.0,69.0,0.98994,3.1,0.38,11.5},
                {5.0,0.455,0.18,1.9,0.036,33.0,106.0,0.98746,3.21,0.83,14.0},
                {6.5,0.33,0.3,3.8,0.036,34.0,88.0,0.99028,3.25,0.63,12.5},
                {7.0,0.16,0.3,2.6,0.043,34.0,90.0,0.99047,2.88,0.47,11.2},
                {8.4,0.22,0.3,1.3,0.038,45.0,122.0,0.99178,3.13,0.54,10.8},
                {6.3,0.33,0.2,17.9,0.066,36.0,161.0,0.9991,3.14,0.51,8.8},
                {7.0,0.16,0.3,2.6,0.043,34.0,90.0,0.99047,2.88,0.47,11.2},
                {5.4,0.24,0.18,2.3,0.05,22.0,145.0,0.99207,3.24,0.46,10.3},
                {7.7,0.31,0.36,4.3,0.026,15.0,87.0,0.99152,3.11,0.48,12.0},
                {5.6,0.185,0.19,7.1,0.048,36.0,110.0,0.99438,3.26,0.41,9.5},
                {5.6,0.185,0.19,7.1,0.048,36.0,110.0,0.99438,3.26,0.41,9.5}};
//        double[] predictDataY = new double[]{19.6,19.9,20.2,20.6,21,21.5,22};//�������������֤������

        SVM svmModel = new SVM();
        Sequence trainModel =svmModel.train(trainDataX,trainDataY,param);
        Sequence preResult = svmModel.predict(trainModel,predictDataX);
        int a=1;


//        double[][] trainDataX = readCSV("testSVM_x.csv",284807 ,30);
//        double[][] trainDataY1 = readCSV("testSVM_y.csv",284807,1);
//        double[]  trainDataY = new double[284807];
//        for (int i =0;i<trainDataY1.length;i++){
//            trainDataY[i] = trainDataY1[i][0];
//        }
//
//        double[] param = new double[]{1,2,3,100,0.00001,1,0.25,0,0.5,0.1,0,1,1};
//
//        double[][] testDataX =  readCSV("testSVM_x.csv",284807 ,30);
//        svm svmModel = new svm();
//        Sequence trainModel =svmModel.train(trainDataX,trainDataY,param);
//        Sequence preResult = svmModel.predict(trainModel,testDataX);
//        int a=1;

        //����int[]��Sequenceת��
//        int[] teste = new int[]{1,2,3,4,5,6};
//        Sequence testf = int1ToSequence(teste);
//        int[] testg = SequenceToInt1(testf);


//        svmAddParam svmModel = new svmAddParam();
//        Sequence trainModel =svmModel.train(trainDataX1,trainDataY1,param1);
//        Sequence preResult = svmModel.predict(trainModel,predictDataX1);


        //����double��Sequence��ת��
//        Sequence testa = dou1toSequence(param); //double[] ת Sequence
//        double[] testb = SeqToDouble1(testa);//Sequence ��ת double[]
//
//        Sequence testc = dou2ToSequence(trainDataX); //double[][]��תΪSequence
//        Matrix testd = new Matrix(testc);
//        double[][] testD = testd.getArray();
//        double[][] testd = SeqToDouble2(testc); //Sequence��תdouble[][]
//
//        svmAddParam svmModel = new svmAddParam();
//        Sequence trainModel =svmModel.train(trainDataX,trainDataY,param);
//        Sequence preResult = svmModel.predict(trainModel,predictDataX);


        //������ʱ��
        //ƽ���������MAE
//        double err = 0;
//        for(int i=0;i<predictDataY.length;i++){
//            err += Math.abs(preResult[i]-predictDataY[i]);
//        }
//        double absErr =  err/ predictDataY.length;


    }
}
