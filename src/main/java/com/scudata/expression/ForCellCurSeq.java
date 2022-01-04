package com.scudata.expression;

import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.dm.Context;

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
		return new Integer(pcs.getForCellRepeatSeq(row, col));
	}
}
