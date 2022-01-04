package com.scudata.expression.mfn.db;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.DBFunction;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.DatabaseUtil;

/**
 * ���ô洢���̼��㣬���صĽ�����ɲ����е�v����
 * db.proc(sql,a:t:m:v,��)
 * @author RunQian
 *
 */
public class Proc extends DBFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("proc" + mm.getMessage("function.missingParam"));
		}

		String strSql;
		Object[] sqlParams = null;
		byte[] types = null;
		byte[] modes = null;
		String[] outParams = null;

		char type = param.getType();
		if (type == IParam.Normal) { // û�в���
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("proc" + mm.getMessage("function.paramTypeError"));
			}

			strSql = (String)obj;
		} else if (type == IParam.Comma) {
			IParam sub0 = param.getSub(0); // sql���ʽ
			if (sub0 == null || !sub0.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("proc" + mm.getMessage("function.invalidParam"));
			}

			Object obj = sub0.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("proc" + mm.getMessage("function.paramTypeError"));
			}

			strSql = (String)obj;

			int paramSize = param.getSubSize() - 1; // ��������
			sqlParams = new Object[paramSize];
			types = new byte[paramSize];
			modes = new byte[paramSize];
			outParams = new String[paramSize];

			for (int i = 0; i < paramSize; ++i) {
				modes[i] = DatabaseUtil.PROC_MODE_IN;
				IParam sub = param.getSub(i + 1);
				if (sub == null) continue;

				if (sub.isLeaf()) { // ֻ�в���û��ָ������
					sqlParams[i] = sub.getLeafExpression().calculate(ctx);
				} else {
					int size = sub.getSubSize();
					if (size > 4) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("proc" + mm.getMessage("function.invalidParam"));
					}

					IParam subi0 = sub.getSub(0); // ����ֵ
					IParam subi1 = sub.getSub(1); // ��������
					if (subi0 != null) sqlParams[i] = subi0.getLeafExpression().calculate(ctx);
					if (subi1 != null) {
						Object tmp = subi1.getLeafExpression().calculate(ctx);
						if (!(tmp instanceof Number)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException("proc" + mm.getMessage("function.paramTypeError"));
						}

						types[i] = ((Number)tmp).byteValue();
					}

					if (size > 2) {
						IParam subi2 = sub.getSub(2); // �������ģʽ
						if (subi2 != null) {
							Object tmp = subi2.getLeafExpression().calculate(ctx);
							if (!(tmp instanceof String)) {
								MessageManager mm = EngineMessage.get();
								throw new RQException("proc" + mm.getMessage("function.paramTypeError"));
							}

							// Ĭ��Ϊ�������
							String modeStr = (String)tmp;
							if (modeStr.indexOf('i') != -1) {
								if (modeStr.indexOf('o') != -1) {
									modes[i] = DatabaseUtil.PROC_MODE_INOUT;
								}
							} else {
								if (modeStr.indexOf('o') != -1) {
									modes[i] = DatabaseUtil.PROC_MODE_OUT;
								}
							}
						}
					}

					if (size > 3) {
						IParam subi3 = sub.getSub(3); // ���������
						if (subi3 != null) {
							outParams[i] = subi3.getLeafExpression().getIdentifierName();
						}
					}
				}
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("proc" + mm.getMessage("function.invalidParam"));
		}

		return db.proc(strSql, sqlParams, types, modes, outParams, ctx);
	}
}
