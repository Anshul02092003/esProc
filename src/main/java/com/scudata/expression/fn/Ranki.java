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
 * ѭ�������е������㣬������ͬ�ֶ�ֵ�ĵĳ�Աͳһ���
 * ranki(F; Gi,��)	iterate(if(F==F[-1],~~,~~+1); Gi,��)
 * ����ѭ�������У����Gi�ֶ�ֵ��ͬ�������ζ�F�ֶ�ֵ��ͬ�ĳ�Ա��1��ʼ��ţ�F�ֶ�ֵ��ͬʱ����Ų������仯��
 * F�ֶ�ֵ�����仯ʱ����ż�1�����Gi�ֶ�ֵ�����仯�����ظ��������㡣
 * @author runqian
 *
 */
public class Ranki extends Function {
	private Expression exp;
	private Expression []gexps;
	
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
			throw new RQException("ranki" + mm.getMessage("function.missingParam"));
		}
		
		if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("ranki" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("ranki" + mm.getMessage("function.invalidParam"));
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
						throw new RQException("ranki" + mm.getMessage("function.invalidParam"));
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
			
			prevVal = exp.calculate(ctx);
			prevRank = new Integer(1);
		} else {
			if (gexps == null) {
				Object val = exp.calculate(ctx);
				if (!Variant.isEquals(prevVal, val)) {
					prevVal = val;
					prevRank = new Integer(prevRank.intValue() + 1);
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
					Object val = exp.calculate(ctx);
					if (!Variant.isEquals(prevVal, val)) {
						prevVal = val;
						prevRank = new Integer(prevRank.intValue() + 1);
					}
				} else {
					prevVal = exp.calculate(ctx);
					prevRank = new Integer(1);
				}
			}
		}
		
		return prevRank;
	}
}
