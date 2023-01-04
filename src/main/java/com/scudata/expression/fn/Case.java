package com.scudata.expression.fn;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.ParamParser;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �����Ҽ�����ʽ�������xi�ķ���ֵ��x����������yi������
 * ���û�б��ʽ�����������򷵻�ȱʡֵ��û��ȱʡֵ�򷵻ؿ�
 * case(x,x1:y1,��,xk:yk;y)
 * @author RunQian
 *
 */
public class Case extends Function {
	public void setParameter(ICellSet cs, Context ctx, String param) {
		strParam = param;
		this.cs = cs;
		this.param = ParamParser.parse(param, cs, ctx, false, false);
	}

	public byte calcExpValueType(Context ctx) {
		return Expression.TYPE_UNKNOWN;
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("case" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		IParam defaultParam = null;
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
			
			defaultParam = param.getSub(1);
			param = param.getSub(0);
			if (param == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
		}
		
		if (param.getType() != IParam.Comma) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("case" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub = param.getSub(0);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("case" + mm.getMessage("function.invalidParam"));
		}

		Object val = sub.getLeafExpression().calculate(ctx);
		for (int i = 1, size = param.getSubSize(); i < size; ++i) {
			sub = param.getSub(i);
			if (sub.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
			
			IParam p = sub.getSub(0);
			Object condition = (p == null ? null : p.getLeafExpression().calculate(ctx));
			if (Variant.isEquals(val, condition)) {
				p = sub.getSub(1);
				return (p == null ? null : p.getLeafExpression().calculate(ctx));
			}
		}

		if (defaultParam != null) {
			return defaultParam.getLeafExpression().calculate(ctx);
		} else {
			return null;
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IParam param = this.param;
		IParam defaultParam = null;
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
			
			defaultParam = param.getSub(1);
			param = param.getSub(0);
			if (param == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
		}
		
		if (param.getType() != IParam.Comma) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("case" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub = param.getSub(0);
		if (sub == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("case" + mm.getMessage("function.invalidParam"));
		}

		IArray valueArray = sub.getLeafExpression().calculateAll(ctx);
		BoolArray signArray = null;
		IArray resultArray = null;
		
		for (int i = 1, size = param.getSubSize(); i < size; ++i) {
			sub = param.getSub(i);
			if (sub.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub1 = sub.getSub(0);
			IParam sub2 = sub.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("case" + mm.getMessage("function.invalidParam"));
			}
			
			if (i == 1) {
				IArray curValueArray = sub1.getLeafExpression().calculateAll(ctx);
				signArray = valueArray.calcRelation(curValueArray, Relation.EQUAL);
				resultArray = sub2.getLeafExpression().calculateAll(ctx, signArray, true);
			} else {
				IArray curValueArray = sub1.getLeafExpression().calculateAll(ctx, signArray, false);
				IArray curResultArray = sub2.getLeafExpression().calculateAll(ctx, signArray, false);
				resultArray = resultArray.combine(signArray, curResultArray);
				valueArray.calcRelations(curValueArray, Relation.EQUAL, signArray, false);
			}
		}

		if (defaultParam != null) {
			IArray curValueArray = defaultParam.getLeafExpression().calculateAll(ctx, signArray, false);
			return resultArray.combine(signArray, curValueArray);
		} else {
			boolean []signs = signArray.getDatas();
			for (int i = 1, len = signs.length; i < len; ++i) {
				if (!signs[i]) {
					resultArray.set(i, null);
				}
			}
			
			return resultArray;
		}
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		return calculateAll(ctx);
	}
	
	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		BoolArray result = leftResult.isTrue();
		IArray array = calculateAll(ctx);
		
		for (int i = 1, size = result.size(); i <= size; ++i) {
			if (result.isTrue(i) && array.isFalse(i)) {
				result.set(i, false);
			}
		}
		
		return result;
	}
}
