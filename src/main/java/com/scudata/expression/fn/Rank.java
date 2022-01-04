package com.scudata.expression.fn;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ѭ�������е������㣬������ͬ�ֶ�ֵ�ĳ�Աͳһ���
 * rank(F; Gi,��)
 * ����ѭ�������У����Gi�ֶ�ֵ��ͬ���F�ֶ�ֵ��ͬ�ĳ�Ա�Ĵ�1��ʼ��ţ�F�ֶ�ֵ��ͬ�ı����ͬ��
 * ��F�ֶ�ֵ�����仯ʱ��һ��F�ֶ�ֵ�ı�ű�Ϊ��һ��F�ֶ�ֵ�ı�ż�����һ��F�ֶ���ֵͬ�ĸ�����
 * ���Gi�ֶ�ֵ�����仯�����ظ���������
 * @author runqian
 *
 */
public class Rank extends Function {
	private Expression exp;
	private Expression []gexps;
	
	private int seq;
	private Object prevVal;
	private Integer prevRank;
	private Object []prevGroupVals;
	
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}
	
	private void prepare(IParam param, Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rank" + mm.getMessage("function.missingParam"));
		}
		
		if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("rank" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("rank" + mm.getMessage("function.invalidParam"));
			}
			
			exp = sub0.getLeafExpression();
			if (sub1.isLeaf()) {
				gexps = new Expression[]{sub1.getLeafExpression()};
			} else {
				int size = sub1.getSubSize();
				gexps = new Expression[size];
				for (int i = 0; i < size; ++i) {
					IParam sub = sub1.getSub(i);
					if (sub == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("rank" + mm.getMessage("function.invalidParam"));
					}
					
					gexps[i] = sub.getLeafExpression();
				}
			}
		}
	}

	public Object calculate(Context ctx) {
		if (prevRank == null) {
			prepare(param, ctx);
			
			if (gexps != null) {
				int gcount = gexps.length;
				prevGroupVals = new Object[gcount];
				for (int i = 0; i < gcount; ++i) {
					prevGroupVals[i] = gexps[i].calculate(ctx);
				}
			}
			
			seq = 1;
			prevVal = exp.calculate(ctx);
			prevRank = new Integer(1);
		} else {
			if (gexps == null) {
				seq++;
				Object val = exp.calculate(ctx);
				if (!Variant.isEquals(prevVal, val)) {
					prevVal = val;
					prevRank = new Integer(seq);
				}
			} else {
				boolean isSame = true;
				int gcount = gexps.length;
				for (int i = 0; i < gcount; ++i) {
					Object val = gexps[i].calculate(ctx);
					if (!Variant.isEquals(prevGroupVals[i], val)) {
						isSame = false;
						prevGroupVals[i] = val;
						
						for (++i; i < gcount; ++i) {
							prevGroupVals[i] = gexps[i].calculate(ctx);
						}
						
						break;
					}
				}
				
				if (isSame) {
					seq++;
					Object val = exp.calculate(ctx);
					if (!Variant.isEquals(prevVal, val)) {
						prevVal = val;
						prevRank = new Integer(seq);
					}
				} else {
					seq = 1;
					prevVal = exp.calculate(ctx);
					prevRank = new Integer(1);
				}
			}
		}
		
		return prevRank;
	}
}
