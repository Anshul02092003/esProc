package com.scudata.expression.mfn.cluster;

import com.scudata.dm.Context;
import com.scudata.expression.ClusterFileFunction;

/**
 * �򿪼�Ⱥ���
 * f.open()
 * @author RunQian
 *
 */
public class Open extends ClusterFileFunction {
	public Object calculate(Context ctx) {
		return file.openGroupTable(ctx);
	}
}
