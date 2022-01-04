package com.scudata.expression.mfn.file;

import java.io.File;
import java.io.IOException;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileGroup;
import com.scudata.dm.FileObject;
import com.scudata.dw.GroupTable;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * ����������ݻ��߸���������ݵ������
 * f.reset(f��) f.reset(f��;x)
 * @author RunQian
 *
 */
public class Reset extends FileFunction {
	public Object calculate(Context ctx) {
		Object obj = null;
		if (param == null) {
			FileObject fo = (FileObject) this.file;
			File file = fo.getLocalFile().file();
			
			GroupTable gt;
			try {
				gt = GroupTable.open(file, ctx);
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
			
			return gt.reset(null, option, ctx, null);
		} else if (param.isLeaf()) {
			obj = param.getLeafExpression().calculate(ctx);
			File f = null;
			if (obj instanceof FileObject) {
				f = ((FileObject) obj).getLocalFile().file();
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.paramTypeError"));
			}
			
			FileObject fo = (FileObject) this.file;
			File file = fo.getLocalFile().file();
			
			try {
				GroupTable gt = GroupTable.open(file, ctx);
				return gt.reset(f, option, ctx, null);
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		} else {
			if (param.getType() != IParam.Semicolon) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.invalidParam"));
			}
			
			int size = param.getSubSize();
			if (size != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.invalidParam"));
			}
			
			File f = null;
			FileGroup fg = null;
			String distribute = null;
			IParam sub0 = param.getSub(0);
			if (sub0 != null) {
				obj = sub0.getLeafExpression().calculate(ctx);
				if (obj instanceof FileObject) {
					f = ((FileObject) obj).getLocalFile().file();
				} else if (obj instanceof FileGroup) {
					fg= (FileGroup) obj;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("reset" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			IParam expParam = param.getSub(1);
			if (expParam != null) {
				distribute = expParam.getLeafExpression().toString();
			}
			
			FileObject fo = (FileObject) this.file;
			File file = fo.getLocalFile().file();

			try {
				GroupTable gt = GroupTable.open(file, ctx);
				if (f != null) {
					return gt.reset(f, option, ctx, distribute);
				} else {
					if (distribute == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("reset" + mm.getMessage("function.invalidParam"));
					}
					return gt.resetFileGroup(fg, option, ctx, distribute);
				}
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}
}
