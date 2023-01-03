package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;

/**
 * ȡfor��Ԫ��ĵ�ǰѭ�����
 * #cell
 * @author RunQian
 *
 */
public class ForCellCurSeq extends Node {
	private PgmCellSet pcs;
	private int row, col;

	public ForCellCurSeq(PgmCellSet pcs, int row, int col) {
		this.pcs = pcs;
		this.row = row;
		this.col = col;
	}

	public Object calculate(Context ctx) {
		return pcs.getForCellRepeatSeq(row, col);
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		int q = pcs.getForCellRepeatSeq(row, col);
		return new ConstArray(q, sequence.length());
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
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
