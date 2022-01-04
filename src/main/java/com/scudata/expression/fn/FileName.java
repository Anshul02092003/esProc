package com.scudata.expression.fn;

import java.io.File;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.LocalFile;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * filename(fn) ���ȫ·��fn�е��ļ�������չ�����ݡ�
 * @author runqian
 *
 */
public class FileName extends Function {
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}

	private static String createTempFile(IParam param, Context ctx) {
		if (param == null) {
			String pathName = Env.getTempPath();
			if (pathName == null || pathName.length() == 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("filename" + mm.getMessage("function.missingParam"));
			}
			
			FileObject fo = new FileObject(pathName, null, ctx);
			String str = fo.createTempFile();
			return LocalFile.removeMainPath(str, ctx);
		} else {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("filename" + mm.getMessage("function.paramTypeError"));
			}

			String pathName = (String)obj;
			FileObject fo = new FileObject(pathName, null, ctx);
			File file = new File(fo.createTempFile());
			return file.getName();
		}
	}
	
	public Object calculate(Context ctx) {
		if (option != null && option.indexOf('t') != -1) {
			return createTempFile(param, ctx);
		} else if (option != null && option.indexOf('p') != -1) {
			String name = null;
			if (param != null) {
				Object obj = param.getLeafExpression().calculate(ctx);
				if (obj instanceof String) {
					name = (String)obj;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("filename" + mm.getMessage("function.paramTypeError"));
				}
			}
			
			if (name == null || name.length() == 0) {
				return Env.getMainPath();
			} else {
				File file = new File(Env.getMainPath(), name);
				return file.getAbsolutePath();
			}
		}

		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("filename" + mm.getMessage("function.invalidParam"));
		}

		Object obj = param.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("filename" + mm.getMessage("function.paramTypeError"));
		}

		LocalFile lf = new LocalFile((String)obj, null, ctx);
		File file = lf.file();
		if (option == null) {
			return file.getName();
		} else if (option.indexOf('e') != -1) {
			String name = file.getName();
			int dot = name.lastIndexOf('.');
			if (dot == -1) {
				return null;
			} else {
				return name.substring(dot + 1);
			}
		} else if (option.indexOf('n') != -1) {
			String name = file.getName();
			int dot = name.lastIndexOf('.');
			if (dot == -1) {
				return name;
			} else {
				return name.substring(0, dot);
			}
		} else if (option.indexOf('d') != -1) {
			String str = file.getParent();
			return LocalFile.removeMainPath(str, ctx);
		} else {
			return file.getName();
		}
	}
}
