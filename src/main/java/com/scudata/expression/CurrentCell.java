package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.cellset.ICellSet;
import com.scudata.cellset.INormalCell;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * @��ǰ��Ԫ������
 * @author WangXiaoJun
 *
 */
public class CurrentCell extends Node {
	private ICellSet cs;

	public CurrentCell(ICellSet cs) {
		this.cs = cs;
	}

	public Object assign(Object value, Context ctx) {
		INormalCell cell = cs.getCurrent();
		if (cell != null) {
			cell.setValue(value);
		}
		return value;
	}
	
	public Object addAssign(Object value, Context ctx) {
		INormalCell cell = cs.getCurrent();
		Object result = Variant.add(cell.getValue(true), value);
		cell.setValue(result);
		return result;
	}

	public Object calculate(Context ctx) {
		INormalCell cell = cs.getCurrent();
		return cell.getValue(true);
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		Object val = cs.getCurrent().getValue(true);
		return new ConstArray(val, sequence.length());
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
		Object value = cs.getCurrent().getValue(true);
		
		if (Variant.isFalse(value)) {
			int size = result.size();
			for (int i = 1; i <= size; ++i) {
				result.set(i, false);
			}
		}
		
		return result;
	}
	
	/**
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
