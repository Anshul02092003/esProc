package com.scudata.expression.mfn.sequence;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * median
 * 		A.median(k:n)
 * 		A.median(k:n, x)
 * ȡ��λ������
 * �Ȱ�����Դ��С��������Ȼ��Ѽ�¼�ֳɶ�Σ�ȡָ���εĵ�һ����¼ֵ
 * ��������Ϊ��������������':'��','������
 * �ڶ�������Ϊ�����ķֶ���
 * ��һ������Ϊȡ�ڼ����ֶεĵ�һ����¼
 * ����������Ϊȡֵ���ʽ��Դ���и��ݸñ��ʽ������µ����У���ִ�зֶ�ȡ����
 * 		A.median(k:n,x)�൱��A.(x).median(k:n)
 * ����	3��5 ��ʾ�������ļ�¼��Ϊ5�Σ�ȡ�����ε�һ����¼��ֵ��
 * 		����¼��ƽ���������ķֶμ�¼��
 * 
 * ������Ϊ�գ���Ϊȡ��λ����ȡֵ��ʽ���þ���ȡֵ�ķ�ʽ��
 * 			�Ѽ��㣬�����������У�����Ϊn�Σ����ص�k�ηֽ����ϵ�����
 * 
 * ��֧��sequence����Դ
 * 
 * @author ��־��
 *
 */
public class Median extends SequenceFunction  {
	public Object calculate(Context ctx) {		
		if (param == null) {
			return srcSequence.median(0, 0);
		} 
		
		int value1=-1, value2=-1;
		char type = param.getType();
		if (type == IParam.Colon) {		//�������� A.median(k:n)
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			
			if (null != sub0 && !sub0.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
			if (null != sub1 && !sub1.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}

			try {
				if (null != sub0) {
					value1 = (int)Variant.longValue(sub0.getLeafExpression().calculate(ctx));
					// ����һ����С��1
					if (value1 < 1) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("median" + mm.getMessage("function.invalidParam"));
					}
				}
				if (null != sub1) {
					value2 = (int)Variant.longValue(sub1.getLeafExpression().calculate(ctx));
					// �������������1���Ҵ��ڲ���һ��
					if (value2 < 1 || value1 > value2 ){
						MessageManager mm = EngineMessage.get();
						throw new RQException("median" + mm.getMessage("function.invalidParam"));
					}
				}
				
				// ��������Ϊȱʡֵ������һ����ҲΪȱʡֵ��
				if (-1 == value2 && -1 != value1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("median" + mm.getMessage("function.invalidParam"));
				}
				
				if (-1 == value1)
					value1 = 0;
				if (-1 == value2)
					value2 = 0;
			} catch (Exception e) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
			return srcSequence.median(value1, value2);
		} else if (type == IParam.Comma ) {		// �������� A.median(k:n, x)
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			
			// ���зָ�����x����Ϊ��
			if ((null != sub1 && !sub1.isLeaf()) || null == sub1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			// �зָ����� �ͱ����зָ���:
			if (null != sub0 && sub0.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub00 = null;
			IParam sub01 = null;
			if (sub0 != null) {
				sub00 = sub0.getSub(0);
				sub01 = sub0.getSub(1);
			}
			
			if (null != sub00 && !sub00.isLeaf()) {	// k������Ҷ����
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			if (null != sub01 && !sub01.isLeaf()) {	// n������Ҷ����
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}

			Sequence seq = null;	// ���о������ʽx����������
			try {
				// �����м�����ʽx
				seq = srcSequence.calc(sub1.getLeafExpression(), ctx);
				
				if (null != sub00) {
					value1 = (int)Variant.longValue(sub00.getLeafExpression().calculate(ctx));
					// ����һ����С��1
					if (value1 < 1) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("median" + mm.getMessage("function.invalidParam"));
					}
				}
				if (null != sub01) {
					value2 = (int)Variant.longValue(sub01.getLeafExpression().calculate(ctx));
					// �������������1���Ҵ��ڲ���һ��
					if (value2 < 1 || value1 > value2 ){
						MessageManager mm = EngineMessage.get();
						throw new RQException("median" + mm.getMessage("function.invalidParam"));
					}
				}
				
				// ��������Ϊȱʡֵ������һ����ҲΪȱʡֵ��
				if (-1 == value2 && -1 != value1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("median" + mm.getMessage("function.invalidParam"));
				}
				
				if (-1 == value1)
					value1 = 0;
				if (-1 == value2)
					value2 = 0;
			} catch (Exception e) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("median" + mm.getMessage("function.invalidParam"));
			}
			
			// ȡ������ֵ
			return seq.median(value1, value2);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("median" + mm.getMessage("function.invalidParam"));
		}
	}
}
