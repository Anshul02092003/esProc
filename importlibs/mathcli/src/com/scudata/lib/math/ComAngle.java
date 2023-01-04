package com.scudata.lib.math;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;

/**
 *��λ��angle
 *��������z��ÿ��Ԫ�ط�������[-pi��pi]�е���λ�ǣ�theta �еĽǶȱ�ʾΪ z = abs(z).*exp(i*theta)��
 *����⣺�踴��ΪA+Bi����ô��λ����arctan(B/A)��
 *��� z = x + iy ��Ԫ���ǷǸ�ʵ������ angle ���� 0����� z ��Ԫ���Ǹ�ʵ������ angle ���� �С�
 */

public class ComAngle extends SequenceFunction {
    public Object calculate(Context ctx) {
        if (this.param == null) {
            ComBase[] cdata = ComBase.toCom(this.srcSequence);
            int len = cdata.length;
            double[] resultDouble = new double[len];
            for (int i = 0; i < len; i++) {
                ComBase co = cdata[i];
                resultDouble[i] = co.comAngle();
            }
            return ComBase.toSeq(resultDouble);
        } else if (param.isLeaf()) {
            Object o = param.getLeafExpression().calculate(ctx);
            if (o instanceof Sequence) {
                ComBase[] cdata = ComBase.createCom(this.srcSequence, (Sequence) o);
                int len = cdata.length;
                double[] resultDouble = new double[len];
                for (int i = 0; i < len; i++) {
                    ComBase co = cdata[i];
                    resultDouble[i] = co.comAngle();
                }
                return ComBase.toSeq(resultDouble);
            } else {
                MessageManager mm = EngineMessage.get();
                throw new RQException("comangle" + mm.getMessage("function.paramTypeError"));
            }
        }
        MessageManager mm = EngineMessage.get();
        throw new RQException("comangle" + mm.getMessage("function.invalidParam"));
    }
}
