package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;


/**
 * ��һ�����ݣ����������� A �����ݵ� z ֵ������Ϊ 0����׼��Ϊ 1��
 * mnorm(A)������A��С������ 1�ĵ�һ������ά�Ƚ�������
 * mnorm(A, n)�������n��ά�ȣ���֧������
 * @author bd
 *
 */
public class MNormalize extends Function {
	public Object calculate (Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("mnorm" + mm.getMessage("function.missingParam"));
		} else {
			Object oa = null;
			Object o2 = null;
			if (param.isLeaf()) {
				// ֻ��һ��������mnorm(A), ����A��С������ 1�ĵ�һ������ά�Ƚ�������
				oa = param.getLeafExpression().calculate(ctx);
			}
			else if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("mnorm" + mm.getMessage("function.invalidParam"));
			}
			else {
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null || sub2 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("mnorm" + mm.getMessage("function.invalidParam"));
				}
				oa = sub1.getLeafExpression().calculate(ctx);
				o2 = sub2.getLeafExpression().calculate(ctx);
			}
			// �Ƿ�������n-1���㷽�Ĭ�ϲ���
			boolean s = option != null && option.contains("s");
			if (oa instanceof Sequence) {
				MulMatrix A = new MulMatrix((Sequence)oa);
				if (o2 != null && !(o2 instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("mnorm" + mm.getMessage("function.paramTypeError"));
				}
				else {
					int b = 0;
					if (o2 instanceof Number) {
						b = ((Number) o2).intValue();
					}
					MulMatrix result = normalize(A, b, s);
					return result.toSequence();
				}
			}
			MessageManager mm = EngineMessage.get();
			throw new RQException("mnorm" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	protected static MulMatrix normalize(MulMatrix A, int level, boolean s) {
		return A.normalize(level, s);
	}
}
