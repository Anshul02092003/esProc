package com.scudata.dm.op;

import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.dm.Sequence.Current;
import com.scudata.expression.Expression;

/**
 * ��������������ڹܵ��ĵ�������
 * @author WangXiaoJun
 *
 */
public class IterateResult implements IResult {
	private Expression exp; // �������ʽ
	private Object prevValue; // ǰһ�ε������
	private Expression c; // �����������ʽ
	
	private boolean sign = false; // �Ƿ������������
	
	public IterateResult(Expression exp, Object initVal, Expression c, Context ctx) {
		this.exp = exp;
		this.prevValue = initVal;
		this.c = c;
	}
	
	/**
	 * ����������������
	 * @param table ����
	 * @param ctx ���������ģ����ܶ���߳����ܵ��������ݣ���Ҫʹ���߳��Լ��������ģ����������ܳ���
	 */
	public void push(Sequence table, Context ctx) {
		if (table == null || table.length() == 0 || sign) return;

		Expression exp = this.exp;
		Expression c = this.c;
		Object prevValue = this.prevValue;
		
		ComputeStack stack = ctx.getComputeStack();
		Param param = ctx.getIterateParam();
		Object oldVal = param.getValue();
		
		Current current = table.new Current();
		stack.push(current);
		
		try {
			if (c == null) {
				for (int i = 1, size = table.length(); i <= size; ++i) {
					param.setValue(prevValue);
					current.setCurrent(i);
					prevValue = exp.calculate(ctx);
				}
			} else {
				for (int i = 1, size = table.length(); i <= size; ++i) {
					param.setValue(prevValue);
					current.setCurrent(i);
					Object obj = c.calculate(ctx);
					
					// �������Ϊ���򷵻�
					if (obj instanceof Boolean && ((Boolean)obj).booleanValue()) {
						sign = true;
						break;
					}
					
					prevValue = exp.calculate(ctx);
				}
			}
			
			this.prevValue = prevValue;
		} finally {
			param.setValue(oldVal);
			stack.pop();
		}
	}
	
	/**
	 * �������ͽ�����ȡ���յļ�����
	 * @return
	 */
	public Object result() {
		return prevValue;
	}
	
	/**
	 * �˺�����֧�ֲ�������
	 */
	public Object combineResult(Object []results) {
		throw new RQException("Unimplemented function.");
	}
}