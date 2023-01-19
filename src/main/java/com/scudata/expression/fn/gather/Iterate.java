package com.scudata.expression.fn.gather;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.Env;
import com.scudata.dm.Param;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �������㺯��
 * iterate(x,a) iterate(x,a;Gi,��)
 * @author WangXiaoJun
 *
 */
public class Iterate extends Gather {
	private Expression exp;
	private Expression initExp;
	private Param valParam;
	
	private Expression []gexps;
	
	private Object prevVal;
	private Object []prevGroupVals;
	
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}
	
	public void prepare(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iterate" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			if (sub0 == null || !sub0.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}

			exp = sub0.getLeafExpression();
			IParam sub1 = param.getSub(1);
			if (sub1 != null) {
				if (!sub1.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
				}

				initExp = sub1.getLeafExpression();
			}
		}
		
		valParam = ctx.getIterateParam();
	}
	
	public Object gather(Context ctx) {
		if (initExp == null) {
			valParam.setValue(null);
		} else {
			valParam.setValue(initExp.calculate(ctx));
		}
		
		return exp.calculate(ctx);
	}

	public Object gather(Object oldValue, Context ctx) {
		valParam.setValue(oldValue);
		return exp.calculate(ctx);
	}
	
	public Expression getRegatherExpression(int q) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("engine.invalidLoopsGroups"));
	}
	
	private void prepare(IParam param, Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iterate" + mm.getMessage("function.missingParam"));
		}

		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}
			
			if (sub1.isLeaf()) {
				gexps = new Expression[]{sub1.getLeafExpression()};
			} else {
				int size = sub1.getSubSize();
				gexps = new Expression[size];
				for (int i = 0; i < size; ++i) {
					IParam sub = sub1.getSub(i);
					if (sub == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
					}
					
					gexps[i] = sub.getLeafExpression();
				}
			}
			
			param = sub0;
		}
		
		if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			if (sub0 == null || !sub0.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
			}

			exp = sub0.getLeafExpression();
			IParam sub1 = param.getSub(1);
			if (sub1 != null) {
				if (!sub1.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("iterate" + mm.getMessage("function.invalidParam"));
				}

				initExp = sub1.getLeafExpression();
			}
		}
		
		valParam = ctx.getIterateParam();
	}

	public Object calculate(Context ctx) {
		if (valParam == null) {
			// ����ִ��
			prepare(param, ctx);
			
			Object oldVal = valParam.getValue();
			if (initExp == null) {
				valParam.setValue(null);
			} else {
				valParam.setValue(initExp.calculate(ctx));
			}
			
			if (gexps != null) {
				int gcount = gexps.length;
				prevGroupVals = new Object[gcount];
				for (int i = 0; i < gcount; ++i) {
					prevGroupVals[i] = gexps[i].calculate(ctx);
				}
			}
			
			prevVal = exp.calculate(ctx);
			valParam.setValue(oldVal);
		} else {
			Object oldVal = valParam.getValue();
			Current current = ctx.getComputeStack().getTopCurrent();

			// �ж�ջ�������еĵ�ǰ����Ƿ���1������������ó�ʼֵ
			if (current != null && current.getCurrentIndex() == 1) {
				if (initExp == null) {
					valParam.setValue(null);
				} else {
					valParam.setValue(initExp.calculate(ctx));
				}
				
				if (gexps != null) {
					int gcount = gexps.length;
					for (int i = 0; i < gcount; ++i) {
						prevGroupVals[i] = gexps[i].calculate(ctx);
					}
				}
			} else {
				if (gexps == null) {
					valParam.setValue(prevVal);
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
						valParam.setValue(prevVal);
					} else if (initExp == null) {
						valParam.setValue(null);
					} else {
						valParam.setValue(initExp.calculate(ctx));
					}
				}
			}
			
			prevVal = exp.calculate(ctx);
			valParam.setValue(oldVal);
		}
		
		return prevVal;
	}
	
	/**
	 * �������м�¼��ֵ�����ܵ����������
	 * @param result �������
	 * @param resultSeqs ÿ����¼��Ӧ�Ľ����������
	 * @param ctx ����������
	 * @return IArray �������
	 */
	public IArray gather(IArray result, int []resultSeqs, Context ctx) {
		if (result == null) {
			result = new ObjectArray(Env.INITGROUPSIZE);
		}
		
		Expression exp = this.exp;
		Expression initExp = this.initExp;
		Param valParam = this.valParam;
		Current current = ctx.getComputeStack().getTopCurrent();
		int len = current.length();
		
		for (int i = 1; i <= len; ++i) {
			current.setCurrent(i);
			if (result.size() < resultSeqs[i]) {
				if (initExp == null) {
					valParam.setValue(null);
				} else {
					valParam.setValue(initExp.calculate(ctx));
				}

				result.add(exp.calculate(ctx));
			} else {
				valParam.setValue(result.get(resultSeqs[i]));
				result.set(resultSeqs[i], exp.calculate(ctx));
			}
		}
		
		return result;
	}
}
