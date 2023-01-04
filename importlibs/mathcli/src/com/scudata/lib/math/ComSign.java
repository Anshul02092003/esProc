package com.scudata.lib.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 * Sign���������ź�����
 * ֻ�������ķ��ź���
 * Y = sign(x) ������ x ��С��ͬ������ Y������ Y ��ÿ��Ԫ���ǣ�x./abs(x)��ǰ���� x Ϊ������
 *
 */
public class ComSign extends SequenceFunction {
    public Object calculate (Context ctx){
        if(this.param == null){
            ComBase[] cdata = ComBase.toCom(this.srcSequence);
            ComBase[] comResult = ComBase.comSign(cdata);
            Sequence result = ComBase.toSeq(comResult);
            return result;
        }
        else if(param.isLeaf()){
            Object o = param.getLeafExpression().calculate(ctx);
            if(o instanceof Sequence){
                ComBase[] cdata = ComBase.createCom(this.srcSequence,(Sequence) o);
                ComBase[] comResult = ComBase.comSign(cdata);
                Sequence result = ComBase.toSeq(comResult);
                return result;
            }else{
                MessageManager mm = EngineMessage.get();
                throw new RQException("comsign" + mm.getMessage("function.paramTypeError"));
            }
        }
        MessageManager mm = EngineMessage.get();
        throw new RQException("comsign" + mm.getMessage("function.invalidParam"));
    }
}
