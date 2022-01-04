package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.util.Variant;

/**
 * ��������������ִ��������飬���ڹܵ����α��group�ӳټ��㺯��
 * @author RunQian
 *
 */
public class Group extends Operation {
	private Expression []exps; // ������ʽ����
	private String opt; // ѡ��
	private boolean isign = false; // �Ƿ���@iѡ��
	private boolean isDistinct = false; // �Ƿ�ȥ��
	
	private Sequence data; // ��ǰ�������
	private Object []values; // ��ǰ����ķ����ֶ�ֵ
	
	// group@q�����Ѱ�ǰ�벿�ֱ��ʽ����
	private Expression []sortExps; // ��벿��������ʽ
	private boolean isSort; // �Ƿ�ֻ����
	
	public Group(Expression []exps, String opt) {
		this(null, exps, opt);
	}

	public Group(Function function, Expression []exps, String opt) {
		super(function);
		this.exps = exps;
		this.opt = opt;
		if (opt != null) {
			if (opt.indexOf('i') != -1) {
				isign = true;
			} else if (opt.indexOf('1') != -1) {
				isDistinct = true;
				values = new Object[exps.length];
			} else {
				values = new Object[exps.length];
			}
		} else {
			values = new Object[exps.length];
		}
	}
	
	public Group(Function function, Expression []exps,  Expression []sortExps, String opt) {
		super(function);
		this.exps = exps;
		this.opt = opt;
		this.sortExps = sortExps;
		
		data = new Sequence();
		values = new Object[exps.length];
		isSort = opt != null && opt.indexOf('s') != -1;
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
		Expression []dupExps = dupExpressions(exps, ctx);
		if (sortExps == null) {
			return new Group(function, dupExps, opt);
		} else {
			Expression []dupSortExps = dupExpressions(sortExps, ctx);
			return new Group(function, dupExps, dupSortExps, opt);
		}
	}
	
	/**
	 * ����ȫ���������ʱ���ã��������һ�������
	 * @param ctx ����������
	 * @return Sequence
	 */
	public Sequence finish(Context ctx) {
		if (data != null) {
			if (sortExps == null) {
				Sequence seq = new Sequence(1);
				seq.add(data);
				data = null;
				return seq;
			} else {
				if (isSort) {
					Sequence tmp = data.sort(sortExps, null, null, ctx);
					data = null;
					return tmp;
				} else {
					Sequence tmp = data.group(sortExps, null, ctx);
					data = null;
					return tmp;
				}
			}
		} else {
			return null;
		}
	}

	private Sequence group_i(Sequence seq, Context ctx) {
		Expression boolExp = exps[0];
		Sequence result = new Sequence();
		Sequence group = data;
		if (group == null) {
			group = new Sequence();
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				if (Variant.isTrue(boolExp.calculate(ctx)) && group.length() > 0) {
					result.add(group);
					group = new Sequence();
				}
				
				group.add(current.getCurrent());
			}
		} finally {
			stack.pop();
		}

		data = group;
		if (result.length() > 0) {
			return result;
		} else {
			return null;
		}
	}
	
	private Sequence group_o(Sequence seq, Context ctx) {
		Expression[] exps = this.exps;
		int vcount = exps.length;
		Object []values = this.values;
		Sequence result = new Sequence();
		Sequence group = data;
		if (group == null) {
			group = new Sequence();
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				if (group.length() > 0) {
					boolean sign = true;
					for (int v = 0; v < vcount; ++v) {
						if (sign) {
							Object value = exps[v].calculate(ctx);
							if (!Variant.isEquals(values[v], value)) {
								sign = false;
								result.add(group);
								group = new Sequence();
								values[v] = value;
							}
						} else {
							values[v] = exps[v].calculate(ctx);
						}
					}
				} else {
					for (int v = 0; v < vcount; ++v) {
						values[v] = exps[v].calculate(ctx);
					}
				}

				group.add(current.getCurrent());
			}
		} finally {
			stack.pop();
		}

		data = group;
		if (result.length() > 0) {
			return result;
		} else {
			return null;
		}
	}
	
	private Sequence group_1(Sequence seq, Context ctx) {
		Expression[] exps = this.exps;
		int vcount = exps.length;
		Object []values = this.values;
		Sequence result = new Sequence();
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean sign = true;
				for (int v = 0; v < vcount; ++v) {
					if (sign) {
						Object value = exps[v].calculate(ctx);
						if (!Variant.isEquals(values[v], value)) {
							sign = false;
							result.add(current.getCurrent());
							values[v] = value;
						}
					} else {
						values[v] = exps[v].calculate(ctx);
					}
				}
			}
		} finally {
			stack.pop();
		}

		if (result.length() > 0) {
			return result;
		} else {
			return null;
		}
	}
	
	private Sequence group_q(Sequence seq, Context ctx) {
		Expression[] exps = this.exps;
		Expression[] sortExps = this.sortExps;
		boolean isSort = this.isSort;
		int fcount1 = exps.length;
		
		Object []values = this.values;
		Sequence data = this.data;
		Sequence result = new Sequence();
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = seq.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean isSame = true;
				
				// ����ǰ��α��ʽ������Ƿ���ǰһ����¼��ͬ
				for (int v = 0; v < fcount1; ++v) {
					if (isSame) {
						Object value = exps[v].calculate(ctx);
						if (!Variant.isEquals(values[v], value)) {
							isSame = false;
							values[v] = value;
						}
					} else {
						values[v] = exps[v].calculate(ctx);
					}
				}

				if (isSame || data.length() == 0) {
					data.add(current.getCurrent());
				} else {
					if (isSort) {
						Sequence tmp = data.sort(sortExps, null, null, ctx);
						result.addAll(tmp);
					} else {
						Sequence tmp = data.group(sortExps, null, ctx);
						result.addAll(tmp);
					}
					
					data.clear();
					data.add(current.getCurrent());
				}
			}
		} finally {
			stack.pop();
		}
		
		if (result.length() > 0) {
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (isign) {
			return group_i(seq, ctx);
		} else if (isDistinct) {
			return group_1(seq, ctx);
		} else if (sortExps != null) {
			return group_q(seq, ctx);
		} else {
			return group_o(seq, ctx);
		}
	}
	
	public String getOpt() {
		return opt;
	}
}
