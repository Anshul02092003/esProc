package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.util.Variant;

/**
 * �α��ܵ����ӵĹ������㴦����
 * op.select(x) op.select(x;f) op.select(x;ch) op���α��ܵ���f���ļ���ch�ǹܵ���������������д���ļ���ܵ�
 * @author RunQian
 *
 */
public class Select extends Operation {
	private Expression fltExp; // ���˱��ʽ
	private String opt; // ѡ��
	private IPipe pipe; // �ܵ��������Ϊ�գ���Ѳ������������������͵��ܵ���

	private boolean isContinuous; // ���������ļ�¼�Ƿ���������һ������
	private boolean isFound; // �Ƿ��Ѿ��ҵ���@cʱ�ҵ�����������ƥ��Ľ�������
	private boolean isOrg; // �Ƿ�ı�ԭ����
	
	public Select(Expression fltExp, String opt) {
		this(null, fltExp, opt, null);
	}

	public Select(Function function, Expression fltExp, String opt) {
		this(function, fltExp, opt, null);
	}

	public Select(Function function, Expression fltExp, String opt, IPipe pipe) {
		super(function);
		this.fltExp = fltExp;
		this.opt = opt;
		this.pipe = pipe;
		
		if (opt != null) {
			this.isContinuous = opt.indexOf('c') != -1;
			this.isOrg = opt.indexOf('o') != -1;
		}
	}
	
	/**
	 * ȡ���˱��ʽ
	 * @return
	 */
	public Expression getFilterExpression() {
		return fltExp;
	}

	/**
	 * ȡ�����Ƿ�����Ԫ������������˺�������ټ�¼
	 * �˺��������α�ľ�ȷȡ����������ӵĲ�������ʹ��¼��������ֻ�谴���������ȡ������
	 * @return true���ᣬfalse������
	 */
	public boolean isDecrease() {
		return true;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(fltExp, ctx);
		return new Select(function, dupExp, opt, pipe);
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		Expression exp = this.fltExp;
		int len = seq.length();
		Sequence result = new Sequence();
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			if (isContinuous) {
				if (isFound) {
					for (int i = 1; i <= len; ++i) {
						current.setCurrent(i);
						Object obj = exp.calculate(ctx);
						if (Variant.isTrue(obj)) {
							result.add(current.getCurrent());
						} else {
							break;
						}
					}
				} else {
					int i = 1;
					for (; i <= len; ++i) {
						current.setCurrent(i);
						Object obj = exp.calculate(ctx);
						if (Variant.isTrue(obj)) {
							isFound = true;
							result.add(current.getCurrent());
							break;
						}
					}
	
					for (++i; i <= len; ++i) {
						current.setCurrent(i);
						Object obj = exp.calculate(ctx);
						if (Variant.isTrue(obj)) {
							result.add(current.getCurrent());
						} else {
							break;
						}
					}
				}
			} else if (pipe == null) {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					Object obj = exp.calculate(ctx);
					if (Variant.isTrue(obj)) {
						result.add(current.getCurrent());
					}
				}
			} else {
				Sequence other = new Sequence();
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					Object obj = exp.calculate(ctx);
					if (Variant.isTrue(obj)) {
						result.add(current.getCurrent());
					} else {
						other.add(current.getCurrent());
					}
				}
				
				if (other.length() != 0) {
					pipe.push(other, ctx);
				}
			}
		} finally {
			stack.pop();
		}

		if (isOrg) {
			seq.setMems(result.getMems());
			return seq;
		} else if (result.length() != 0) {
			return result;
		} else {
			return null;
		}
	}
}
