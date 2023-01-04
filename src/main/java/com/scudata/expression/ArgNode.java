package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * eval�������õ��Ĳ���������?��?1
 * @author WangXiaoJun
 *
 */
public class ArgNode extends Node {
	private int index = -1; // ֵΪ0ʱ��ʾȡ���вΣ�����0ʱ��ʾȡ��Ӧλ�õĲ���

	public ArgNode(String id) {
		if (id.length() > 1) index = Integer.parseInt(id.substring(1));
	}

	public Object calculate(Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Current current = stack.getArg();
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.argStackEmpty"));
		}

		if (index > 0) {
			return current.get(index);
		} else if (index == 0) {
			return current.getCurrentSequence();
		} else {
			index = current.getCurrentIndex() + 1;
			if (index <= current.length()) {
				current.setCurrent(index);
				return current.get(index);
			} else {
				if (current.length() > 0) {
					index = 1;
					current.setCurrent(1);
					return current.get(1);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.argStackEmpty"));
				}
			}
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		Object value = calculate(ctx);
		return new ConstArray(value, sequence.length());
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
		Object value = calculate(ctx);
		BoolArray result = leftResult.isTrue();
		
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
