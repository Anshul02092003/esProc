package com.scudata.lib.math;


import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * �����ľ���ֵcomAbs()
 *
 * Y = abs(X) �������� X ��ÿ��Ԫ�صľ���ֵ
 * ��� X �Ǹ������� abs(X) ���ظ�����ģ
 * ��� X �Ǹ������飬�򷵻�ģ��ɵ�����
 */
public class ComAbs extends SequenceFunction {
    public Object calculate(Context ctx){
        if (this.param == null){
            ComBase[] cdata = ComBase.toCom(this.srcSequence);
            int len = cdata.length;
            double[] resultDouble = new double[len];
            for(int i= 0 ;i<len;i++){
                ComBase co = cdata[i];
                resultDouble[i] = co.comAbs();
            }
            return ComBase.toSeq(resultDouble);
        }
        else if(param.isLeaf()){
            Object o = param.getLeafExpression().calculate(ctx);
            if(o instanceof Sequence){
                ComBase[] cdata = ComBase.createCom(this.srcSequence, (Sequence) o);
                int len  = cdata.length;
                double[] resultDouble = new double[len];
                for(int i=0;i<len;i++){
                    ComBase co = cdata[i];
                    resultDouble[i] = co.comAbs();
                }
                return ComBase.toSeq(resultDouble);
            }else{
                MessageManager mm = EngineMessage.get();
                throw new RQException("comabs" + mm.getMessage("function.paramTypeError"));
            }
        }
        MessageManager mm = EngineMessage.get();
        throw new RQException("comabs" + mm.getMessage("function.invalidParam"));
    }
}
