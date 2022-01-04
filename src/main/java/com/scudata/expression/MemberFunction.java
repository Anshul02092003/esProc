package com.scudata.expression;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

/**
 * ��Ա�������࣬��Ա������ʵ������Ҫ�̳��Դ���
 * @author WangXiaoJun
 *
 */
public abstract class MemberFunction extends Function {
	private MemberFunction next; // ��һ��ͬ���ĳ�Ա������
	
	/**
	 * �����жϵ����������ĺ����Ƿ��������������ƥ��
	 * @param obj �������
	 * @return true������Ľڵ��������������ƥ�䣬�����Ա���Ա������false������
	 */
	abstract public boolean isLeftTypeMatch(Object obj);
	
	/**
	 * ���õ�������������󵽵�ǰ����
	 * @param obj �������
	 */
	abstract public void setDotLeftObject(Object obj);
	
	/**
	 * ���ú�������������������̳��˴˷�����Ҫ���û���Ĵ˷������ߵ���next�Ĵ˷���
	 * @param cs �������
	 * @param ctx ����������
	 * @param param ���������ַ���
	 */
	public void setParameter(ICellSet cs, Context ctx, String param) {
		super.setParameter(cs, ctx, param);
		if (next != null) {
			next.setParameter(cs, ctx, param);
		}
	}
	
	public void setOption(String opt) {
		super.setOption(opt);
		if (next != null) {
			next.setOption(opt);
		}
	}

	/**
	 * ȡ��һ��ͬ���ĳ�Ա������û���򷵻ؿ�
	 * @return MemberFunction
	 */
	public MemberFunction getNextFunction() {
		return next;
	}
	
	/**
	 * ������һ��ͬ���ĳ�Ա����
	 * @param fn ��Ա����
	 */
	public void setNextFunction(MemberFunction fn) {
		next = fn;
	}
	
	/**
	 * �жϵ�ǰ�ڵ��Ƿ������к���
	 * �������������Ҳ�ڵ������к��������ڵ�������������Ҫ����ת������
	 * @return
	 */
	public boolean isSequenceFunction() {
		// ���ͬ�������������˴˷���������ã�����ͷ���Ĭ��ֵ
		return next == null ? false : next.isSequenceFunction();
	}

	/**
	 * �жϽڵ��Ƿ���޸����еĳ�Աֵ���˷���Ϊ���Ż�[1,2,3].contain(...)���ֱ��ʽ��
	 * ������в��ᱻ������[1,2,3]���Ա������ɳ������У�������ÿ�μ��㶼����һ������
	 * @return true�����޸ģ�false�������޸�
	 */
	public boolean ifModifySequence() {
		// ���ͬ�������������˴˷���������ã�����ͷ���Ĭ��ֵ
		return next == null ? true : next.isSequenceFunction();
	}
	
	/**
	 * �Խڵ����Ż�
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		if (param != null) {
			// �Բ������Ż�
			param.optimize(ctx);
			if (next != null) {
				next.optimize(ctx);
			}
		}
		
		return this;
	}
	
	// x:��,A:y:��,z:F,��
	protected static void parseJoinParam(IParam param, int index, Expression[][] exps,
								   Object[] codes, Expression[][] dataExps,
								   Expression[][] newExps, String[][] newNames, Context ctx) {
		int size = param.getSubSize();
		if (size < 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("join" + mm.getMessage("function.invalidParam"));
		}

		IParam sub = param.getSub(0);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("join" + mm.getMessage("function.invalidParam"));
		} else if (sub.isLeaf()) {
			exps[index] = new Expression[]{sub.getLeafExpression()};
		} else {
			int expCount = sub.getSubSize();
			Expression []tmps = new Expression[expCount];
			exps[index] = tmps;

			for (int i = 0; i < expCount; ++i) {
				IParam p = sub.getSub(i);
				if (p == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("join" + mm.getMessage("function.invalidParam"));
				}

				tmps[i] = p.getLeafExpression();
			}
		}

		sub = param.getSub(1);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("join" + mm.getMessage("function.invalidParam"));
		} else if (sub.isLeaf()) {
			codes[index] = sub.getLeafExpression().calculate(ctx);
		} else {
			IParam p = sub.getSub(0);
			if (p == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("join" + mm.getMessage("function.invalidParam"));
			}

			codes[index] = p.getLeafExpression().calculate(ctx);
			int expCount = sub.getSubSize() - 1;
			Expression []tmps = new Expression[expCount];
			dataExps[index] = tmps;

			for (int i = 0; i < expCount; ++i) {
				p = sub.getSub(i + 1);
				if (p == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("join" + mm.getMessage("function.invalidParam"));
				}

				tmps[i] = p.getLeafExpression();
			}
		}
		
		int expCount = size - 2;
		Expression []tmpExps = new Expression[expCount];
		String []tmpNames = new String[expCount];
		newExps[index] = tmpExps;
		newNames[index] = tmpNames;

		for (int i = 0; i < expCount; ++i) {
			IParam p = param.getSub(i + 2);
			if (p == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("join" + mm.getMessage("function.invalidParam"));
			}

			if (p.isLeaf()) {
				tmpExps[i] = p.getLeafExpression();
			} else {
				if (p.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("join" + mm.getMessage("function.invalidParam"));
				}

				IParam sub0 = p.getSub(0);
				if (sub0 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("join" + mm.getMessage("function.invalidParam"));
				}

				tmpExps[i] = sub0.getLeafExpression();
				IParam sub1 = p.getSub(1);
				if (sub1 != null) {
					tmpNames[i] = sub1.getLeafExpression().getIdentifierName();
				}
			}
		}
	}

	// x:��,A:y:��,z:F,��
	protected static void parseJoinxParam(IParam param, int index, Expression[][] exps,  Object[] codes, 
			Expression[][] dataExps, Expression[][] newExps, String[][] newNames, Context ctx) {
		int size = param.getSubSize();
		if (size < 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
		}

		IParam sub = param.getSub(0);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
		} else if (sub.isLeaf()) {
			exps[index] = new Expression[]{sub.getLeafExpression()};
		} else {
			int expCount = sub.getSubSize();
			Expression []tmps = new Expression[expCount];
			exps[index] = tmps;

			for (int i = 0; i < expCount; ++i) {
				IParam p = sub.getSub(i);
				if (p == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
				}

				tmps[i] = p.getLeafExpression();
			}
		}

		sub = param.getSub(1);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
		} else if (sub.isLeaf()) {
			codes[index] = sub.getLeafExpression().calculate(ctx);
		} else {
			IParam p = sub.getSub(0);
			if (p == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
			}

			codes[index] = p.getLeafExpression().calculate(ctx);
			int expCount = sub.getSubSize() - 1;
			Expression []tmps = new Expression[expCount];
			dataExps[index] = tmps;

			for (int i = 0; i < expCount; ++i) {
				p = sub.getSub(i + 1);
				if (p == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
				}

				tmps[i] = p.getLeafExpression();
			}
		}
		
		int expCount = size - 2;
		Expression []tmpExps = new Expression[expCount];
		String []tmpNames = new String[expCount];
		newExps[index] = tmpExps;
		newNames[index] = tmpNames;

		for (int i = 0; i < expCount; ++i) {
			IParam p = param.getSub(i + 2);
			if (p == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
			}

			if (p.isLeaf()) {
				tmpExps[i] = p.getLeafExpression();
			} else {
				if (p.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
				}

				IParam sub0 = p.getSub(0);
				if (sub0 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("joinx" + mm.getMessage("function.invalidParam"));
				}

				tmpExps[i] = sub0.getLeafExpression();
				IParam sub1 = p.getSub(1);
				if (sub1 != null) {
					tmpNames[i] = sub1.getLeafExpression().getIdentifierName();
				}
			}
		}
	}

	protected static void parseSwitchParam(IParam param, int i, 
			String []fkNames, Object []codes, Expression []exps, Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("switch" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			fkNames[i] = param.getLeafExpression().getIdentifierName();
			return;
		}
		
		int size = param.getSubSize();
		if (size > 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("switch" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub = param.getSub(0);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("switch" + mm.getMessage("function.invalidParam"));
		}
		
		fkNames[i] = sub.getLeafExpression().getIdentifierName();
		sub = param.getSub(1);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("switch" + mm.getMessage("function.invalidParam"));
		} else if (sub.isLeaf()) {
			codes[i] = sub.getLeafExpression().calculate(ctx);
			if (codes[i] == null) {
				codes[i] = new Sequence();
			}
			
			if (size > 2) {
				sub = param.getSub(2);
				if (sub != null) {
					exps[i] = sub.getLeafExpression();
				}
			}
		} else {
			if (sub.getSubSize() != 2 || size > 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("switch" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = sub.getSub(0);
			if (sub0 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("switch" + mm.getMessage("function.invalidParam"));
			}
			
			codes[i] = sub0.getLeafExpression().calculate(ctx);
			if (codes[i] == null) {
				codes[i] = new Sequence();
			}

			IParam sub1 = sub.getSub(1);
			if (sub1 != null) {
				exps[i] = sub1.getLeafExpression();
			}
		}
	}
}
