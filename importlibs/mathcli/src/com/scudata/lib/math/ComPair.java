package com.scudata.lib.math;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * ���������
 * ������������°���������,Ҫ���������ȫ��Ϊ����ԵĹ������ʵ��
 * ���򣺵���ʵ����ʵ��Ϊ0Ҳ�������ڣ������и�����������ǰ�棻
 *    ��ʵ��һ�������鲿�����ִ�С�����ҷ��ϸ�������ǰ�Ĺ������и�������������ʵ����Ҳ�ǵ���
 */
public class ComPair extends SequenceFunction {
    public Object calculate (Context ctx) {
        if(this.param == null){
            double[][] allConjData = ComBase.toDbl2(this.srcSequence);
            Boolean judge = ComBase.judgePair(allConjData);
            if(judge == Boolean.TRUE){
                double[][] resultDouble = ComBase.comPair(allConjData);
                Sequence result = ComBase.toSeq(resultDouble);
                return result;
            }else{
                MessageManager mm = EngineMessage.get();
                throw new RQException(mm.getMessage("The data in compair() should be paired conjugate complex number."));
            }
        }
        else if(param.isLeaf()){
            Object o = param.getLeafExpression().calculate(ctx);
            if(o instanceof Sequence){
                double[][] allConjData = ComBase.toDbl2((Sequence) o);
                Boolean judge = ComBase.judgePair(allConjData);
                if(judge == Boolean.TRUE){
                    double[][] resultDouble = ComBase.comPair(allConjData);
                    Sequence result = ComBase.toSeq(resultDouble);
                    return result;
                }
            }else{
                MessageManager mm = EngineMessage.get();
                throw new RQException(mm.getMessage("The data in compair() should be paired conjugate complex number."));
            }
        }
        MessageManager mm = EngineMessage.get();
        throw new RQException("compair" + mm.getMessage("function.invalidParam"));
    }
}
