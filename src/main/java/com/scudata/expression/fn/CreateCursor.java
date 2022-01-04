package com.scudata.expression.fn;

import com.scudata.cellset.ICellSet;
import com.scudata.cellset.INormalCell;
import com.scudata.cellset.datamodel.Command;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DfxManager;
import com.scudata.dm.FileObject;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.dm.cursor.DFXCursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CellSetUtil;

/**
 * ���������ļ�����ִ�г����return�Ľ���������α귵��
 * cursor(dfx,��)���������ļ�����ִ�г����return�Ľ���������α귵�ء�dfx���ж��returnʱ��
 * �Ⱥϲ����return�Ľ�������ٽ��ϲ���Ľ�������س��α꣬dfx�����е�return�Ľ�������������ͬ�����ݽṹ�����򱨴�
 * @author runqian
 *
 */
public class CreateCursor extends Function {
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}

	private PgmCellSet getPgmCellSet(INormalCell cell, Object []args, Context ctx) {
		if (cell == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
		}
		
		ICellSet cs = cell.getCellSet();
		if (!(cs instanceof PgmCellSet)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
		}

		PgmCellSet pcs = (PgmCellSet)cs;
		PgmNormalCell pcell = (PgmNormalCell)cell;
		if (pcell.getCommand() == null || pcell.getCommand().getType() != Command.FUNC) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.callNeedSub"));
		}
		
		return pcs.newCursorDFX(pcell, args);
	}
	
	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.missingParam"));
		}

		boolean useCache = true, isCode = false, isCommand = false;
		if (option != null) {
			if (option.indexOf('r') != -1) useCache = false;
			if (option.indexOf('s') != -1) isCode = true;
			if (option.indexOf('c') != -1) isCommand = true;
		}

		DfxManager dfxManager = DfxManager.getInstance();
		PgmCellSet pcs;
		if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			if (isCommand) {
				INormalCell cell = exp.calculateCell(ctx);
				pcs = getPgmCellSet(cell, null, ctx);
			} else {
				Object obj = exp.calculate(ctx);
				if (obj instanceof String) {
					if (isCode) {
						pcs = CellSetUtil.toPgmCellSet((String)obj);
					} else if (useCache) {
						pcs = dfxManager.removeDfx((String)obj, ctx);
					} else {
						pcs = dfxManager.readDfx((String)obj, ctx);
					}
				} else if (obj instanceof FileObject) {
					if (useCache) {
						pcs = dfxManager.removeDfx((FileObject)obj, ctx);
					} else {
						pcs = dfxManager.readDfx((FileObject)obj, ctx);
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
				}
			}
		} else {
			int size = param.getSubSize();
			IParam sub0 = param.getSub(0);
			if (sub0 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
			}

			Expression exp = sub0.getLeafExpression();
			if (isCommand) {
				INormalCell cell = exp.calculateCell(ctx);
				Object []args = new Object[size - 1];
				for (int i = 1; i < size; ++i) {
					IParam sub = param.getSub(i);
					if (sub != null) {
						args[i - 1] = sub.getLeafExpression().calculate(ctx);
					}
				}
				
				pcs = getPgmCellSet(cell, args, ctx);
			} else {
				Object obj = exp.calculate(ctx);
				if (obj instanceof String) {
					if (isCode) {
						pcs = CellSetUtil.toPgmCellSet((String)obj);
						ParamList paramList = new ParamList();
						pcs.setParamList(paramList);
						for (int i = 1; i < size; ++i) {
							paramList.add("arg" + i, Param.ARG, null);
						}
					} else if (useCache) {
						pcs = dfxManager.removeDfx((String)obj, ctx);
					} else {
						pcs = dfxManager.readDfx((String)obj, ctx);
					}
				} else if (obj instanceof FileObject) {
					if (useCache) {
						pcs = dfxManager.removeDfx((FileObject)obj, ctx);
					} else {
						pcs = dfxManager.readDfx((FileObject)obj, ctx);
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
				}
				
				ParamList list = pcs.getParamList();
				if (list != null) {
					if (size - 1 > list.count()) size = list.count() + 1;

					Context curCtx = pcs.getContext();
					for (int i = 1; i < size; ++i) {
						IParam sub = param.getSub(i);
						Param p = list.get(i - 1);
						if (sub != null) {
							Object val = sub.getLeafExpression().calculate(ctx);
							curCtx.setParamValue(p.getName(), val);
						} else {
							curCtx.setParamValue(p.getName(), null);
						}
					}
				}
			}
		}

		return new DFXCursor(pcs, ctx, useCache);
	}
}
