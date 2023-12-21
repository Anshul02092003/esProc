package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * ��ǰ��ѭ�����
 * #@
 * @author RunQian
 *
 */
public class CurrentCellSeq extends Node {
	private int seq = 0;
	
	public Object calculate(Context ctx) {
		return ++seq;
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
		return leftResult.isTrue();
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Sequence topSequence = stack.getTopSequence();
		int start = seq + 1;
		seq += topSequence.length();
		IntArray array = new IntArray(start, seq);
		array.setTemporary(true);
		return array;
	}
	
	/**
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
