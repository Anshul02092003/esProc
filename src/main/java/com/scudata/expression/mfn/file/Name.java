package com.scudata.expression.mfn.file;

import java.io.File;

import com.scudata.dm.Context;
import com.scudata.dm.LocalFile;
import com.scudata.expression.FileFunction;

/**
 * ȡ�ļ��������Ŀ¼��·������������Ŀ¼�·���ȫ·��
 * f.name()
 * @author RunQian
 *
 */
public class Name extends FileFunction {
	public Object calculate(Context ctx) {
		try {
			File f = file.getLocalFile().getFile();
			if (f == null) {
				return file.getFileName();
			}
			
			String pathName = f.getAbsolutePath();
			if (option == null || option.indexOf('p') == -1) {
				return LocalFile.removeMainPath(pathName, ctx);
			} else {
				return pathName;
			}
		} catch (Exception e) {
			return file.getFileName();
		}
	}
}
