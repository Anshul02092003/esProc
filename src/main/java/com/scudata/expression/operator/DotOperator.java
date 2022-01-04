package com.scudata.expression.operator;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Move;
import com.scudata.expression.Node;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���������.
 * A.f()
 * @author WangXiaoJun
 *
 */
public class DotOperator extends Operator {
	public DotOperator() {
		priority = PRI_SUF;
	}
	
	public Node optimize(Context ctx) {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\".\"" + mm.getMessage("operator.missingleftOperation"));
		}
		
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\".\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		// ����Ҳຯ�������޸��������ֵ���������������г���������Ȳ�������
		// ����[1,2,3].contain(n)
		if (!right.ifModifySequence()) {
			left = left.optimize(ctx, true);
			right = right.optimize(ctx);
			return this;
		} else {
			return super.optimize(ctx);
		}
	}

	/**
	 * �����������ֵ
	 * @param ctx ����������
	 * @return ������
	 */
	private Object getLeftObject(Context ctx) {
		Object obj = left.calculate(ctx);
		
		// n.f()��f�����к���ʱ��n���ͳ�to(n)
		if (obj instanceof Number && right.isSequenceFunction()) {
			int n = ((Number)obj).intValue();
			if (n > 0) {
				return new Sequence(1, n);
			} else {
				return new Sequence(0);
			}
		} else {
			return obj;
		}
	}

	public Object calculate(Context ctx) {
		Object leftValue = getLeftObject(ctx);
		if (leftValue == null) {
			return null;
		}

		Node right = this.right;
		while (right != null) {
			if (right.isLeftTypeMatch(leftValue)) {
				right.setDotLeftObject(leftValue);
				return right.calculate(ctx);
			} else {
				right = right.getNextFunction();
			}
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("dot.leftTypeError", Variant.getDataType(leftValue)));
	}

	public Object assign(Object value, Context ctx) {
		Object result1 = getLeftObject(ctx);
		if (result1 == null) {
			return value;
		}
		
		right.setDotLeftObject(result1);
		return right.assign(value, ctx);
	}

	public Object addAssign(Object value, Context ctx) {
		Object result1 = getLeftObject(ctx);
		if (result1 == null) {
			return null;
		}
		
		right.setDotLeftObject(result1);
		return right.addAssign(value, ctx);
	}

	public Object move(Move node, Context ctx) {
		Object result1 = getLeftObject(ctx);
		if (result1 == null) {
			return null;
		}
		
		right.setDotLeftObject(result1);
		return right.move(node, ctx);
	}

	public Object moveAssign(Move node, Object value, Context ctx) {
		Object result1 = getLeftObject(ctx);
		if (result1 == null) {
			return value;
		}
		
		right.setDotLeftObject(result1);
		return right.moveAssign(node, value, ctx);
	}
	
	public Object moves(Move node, Context ctx) {
		Object result1 = getLeftObject(ctx);
		if (result1 == null) {
			return null;
		}
		
		right.setDotLeftObject(result1);
		return right.moves(node, ctx);
	}
}
