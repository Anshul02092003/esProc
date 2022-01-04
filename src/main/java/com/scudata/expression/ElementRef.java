package com.scudata.expression;

import java.util.List;

import com.scudata.cellset.INormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ����Ԫ������
 * A(2)  A([2,4])
 * @author WangXiaoJun
 *
 */
public class ElementRef extends Function {
	private Node left;

	public ElementRef() {
		priority = PRI_SUF;
	}

	public void setLeft(Node node) {
		left = node;
	}

	public Node getLeft() {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("operator.missingleftOperation"));
		}
		return left;
	}

	protected boolean containParam(String name) {
		if (getLeft().containParam(name)) return true;
		return super.containParam(name);
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		getLeft().getUsedParams(ctx, resultList);
		super.getUsedParams(ctx, resultList);
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		getLeft().getUsedFields(ctx, resultList);
		super.getUsedFields(ctx, resultList);
	}
	
	protected void getUsedCells(List<INormalCell> resultList) {
		getLeft().getUsedCells(resultList);
		super.getUsedCells(resultList);
	}

	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		left = getLeft().optimize(ctx);
		return this;
	}

	public Object calculate(Context ctx) {
		Node left = getLeft();
		Object result1 = left.calculate(ctx);

		if (result1 == null) {
			return null;
		} else if (!(result1 instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("dot.seriesLeft"));
		}

		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.invalidParam"));
		}

		Expression param1 = param.getLeafExpression();
		Object o = param1.calculate(ctx);

		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Sequence)result1).get(((Number)o).intValue());
		} else if (o instanceof Sequence) {
			return ((Sequence)result1).get((Sequence)o);
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException("()" + mm.getMessage("function.paramTypeError"));
	}

	/**
	 * ������Ԫ�ظ�ֵ
	 * @param ��ֵ
	 * @param ctx ����������
	 * @return ��ֵ
	 */
	public Object assign(Object value, Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.invalidParam"));
		}

		Node left = getLeft();
		Object result1 = left.calculate(ctx);
		if (!(result1 instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("dot.seriesLeft"));
		}

		Sequence srcSeries = (Sequence)result1;
		int len = srcSeries.length();
		Object pval = param.getLeafExpression().calculate(ctx);

		// Խ�籨�����Զ���
		if (pval instanceof Number) {
			int index = ((Number)pval).intValue();
			if (index > len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(index + mm.getMessage("engine.indexOutofBound"));
			}

			srcSeries.set(index, value);
		} else if (pval instanceof Sequence) {
			Sequence posSeries = (Sequence)pval;
			int count = posSeries.length();
			if (value instanceof Sequence) {
				Sequence tseq = (Sequence)value;
				if (count != tseq.length()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.memCountNotMatch"));
				}

				for (int i = 1; i<= count; ++i) {
					Object posObj = posSeries.get(i);
					if (!(posObj instanceof Number)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needIntSeries"));
					}

					int index = ((Number)posObj).intValue();
					if (index > len) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(index + mm.getMessage("engine.indexOutofBound"));
					}

					srcSeries.set(index, tseq.get(i));
				}
			} else {
				for (int i = 1; i<= count; ++i) {
					Object posObj = posSeries.get(i);
					if (!(posObj instanceof Number)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needIntSeries"));
					}

					int index = ((Number)posObj).intValue();
					if (index > len) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(index + mm.getMessage("engine.indexOutofBound"));
					}

					srcSeries.set(index, value);
				}
			}
		} else if (pval == null) {
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.paramTypeError"));
		}

		return value;
	}
	
	/**
	 * ������Ԫ����+=����
	 * @param ֵ
	 * @param ctx ����������
	 * @return ��ֵ
	 */
	public Object addAssign(Object value, Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.invalidParam"));
		}

		Node left = getLeft();
		Object result1 = left.calculate(ctx);
		if (!(result1 instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("dot.seriesLeft"));
		}

		Sequence srcSeries = (Sequence)result1;
		int len = srcSeries.length();
		Object pval = param.getLeafExpression().calculate(ctx);

		// Խ�籨�����Զ���
		if (pval instanceof Number) {
			int index = ((Number)pval).intValue();
			if (index > len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(index + mm.getMessage("engine.indexOutofBound"));
			}

			Object result = Variant.add(srcSeries.getMem(index), value);
			srcSeries.set(index, result);
			return result;
		} else if (pval == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.paramTypeError"));
		}
	}
}
