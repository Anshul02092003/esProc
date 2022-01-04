package com.scudata.expression.fn;

import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * T(fn,A;Fi,��;s) �����ļ���չ�������ļ����س����txt/csv/xls/xlsx/btx/ctx
 * �ı�ʱs�Ƿָ�����xlsʱsΪsheet�� ��Aʱ����/�α�A��д�룬���滻д
 * 
 * @b û�б��⣬ȱʡ��Ϊ�б���
 * @c �����α�
 * 
 *
 */
public class T extends Function {
	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		String opt = option;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("T" + mm.getMessage("function.missingParam"));
		}
		IParam fnParam, fieldParam = null, sParam = null;
		if (param.getType() == IParam.Semicolon) {
			int subParamCount = param.getSubSize();
			if (subParamCount != 2 && subParamCount != 3) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("T"
						+ mm.getMessage("function.invalidParam"));
			}
			fnParam = param.getSub(0);
			fieldParam = param.getSub(1);
			if (subParamCount > 2) {
				sParam = param.getSub(2);
			}
		} else {
			fnParam = param; // ֻ��fn,A
		}
		if (fnParam == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("T" + mm.getMessage("function.missingParam"));
		}
		IParam aParam = null;
		String A = null;
		if (fnParam.getType() == IParam.Comma) {
			if (fnParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("T"
						+ mm.getMessage("function.invalidParam"));
			}
			aParam = fnParam.getSub(1);
			if (aParam != null) {
				if (!aParam.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("T"
							+ mm.getMessage("function.invalidParam"));
				}
				A = aParam.getLeafExpression().toString();
			}
			fnParam = fnParam.getSub(0);
		}
		if (fnParam == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("file"
					+ mm.getMessage("function.missingParam"));
		}
		if (!fnParam.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("T" + mm.getMessage("function.invalidParam"));
		}
		String fnStr = fnParam.getLeafExpression().toString();
		Object fn = fnParam.getLeafExpression().calculate(ctx);
		if (fn != null) {
			if (!(fn instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("T"
						+ mm.getMessage("function.invalidParam"));
			}
		}
		if (!StringUtils.isValidString(fn)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("T" + mm.getMessage("function.missingParam"));
		}
		String fileName = (String) fn; // �������ļ���

		String fields = null;
		if (fieldParam != null) {
			if (fieldParam.isLeaf()) {
				// ���ﲻȡgetIdentifierName������������
				fields = fieldParam.getLeafExpression().toString(); // getIdentifierName
			} else {
				StringBuffer buf = new StringBuffer();
				int count = fieldParam.getSubSize();
				for (int i = 0; i < count; ++i) {
					IParam sub = fieldParam.getSub(i);
					if (sub == null || !sub.isLeaf()) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("T"
								+ mm.getMessage("function.invalidParam"));
					}
					if (i > 0)
						buf.append(",");
					buf.append(sub.getLeafExpression().toString());
				}
				fields = buf.toString();
			}
		}
		String s = null;
		if (sParam != null) {
			s = sParam.getLeafExpression().toString();
		}

		boolean hasTitle = opt == null || opt.indexOf("b") == -1;
		boolean isCursor = opt != null && opt.indexOf("c") != -1;

		StringBuffer buf = new StringBuffer();
		buf.append("file(" + fnStr + ")."); // ��ԭʼ�Ĵ�
		byte fileType = getFileType(fileName);
		if (fileType == TYPE_EXCEL) {
			String sopt = "";
			if (hasTitle || isCursor) {
				sopt += "@";
				if (isCursor)
					sopt += "c";
				if (hasTitle)
					sopt += "t";
			}
			if (A == null) { // ����
				buf.append("xlsimport" + sopt + "(");
				if (fields != null) {
					buf.append(fields);
				}
			} else { // ����
				buf.append("xlsexport" + sopt + "(");
				buf.append(A);
				if (fields != null) {
					buf.append(";");
					buf.append(fields);
				}
			}
			if (StringUtils.isValidString(s)) {
				buf.append(";");
				buf.append(s);
			}
			buf.append(")");
		} else if (fileType == TYPE_CTX) {
			String sopt = "";
			if (hasTitle) {
				sopt = "@t";
			}
			if (A == null) { // ����
				buf.append("create().cursor" + sopt + "(");
				if (fields != null) {
					buf.append(fields);
				}
				buf.append(")");
				if (!isCursor) {
					buf.append(".fetch()");
				}
			} else {// ����
				buf.append("create(");
				if (fields != null) {
					buf.append(fields);
				} else {
					Object aObj = aParam.getLeafExpression().calculate(ctx);
					DataStruct ds = null;
					if (aObj instanceof Sequence) {
						ds = ((Sequence) aObj).dataStruct();
					} else if (aObj instanceof ICursor) {
						ds = ((ICursor) aObj).getDataStruct();
					}
					if (ds != null) {
						String[] fieldNames = ds.getFieldNames();
						if (fieldNames != null) {
							for (int i = 0; i < fieldNames.length; i++) {
								if (i > 0)
									buf.append(",");
								buf.append(fieldNames[i]);
							}
						}
					}
				}
				buf.append(").append(");
				buf.append(A);
				buf.append(").close()"); // ��Ҫclose�Ż�д�����ļ���
			}
		} else {
			boolean isCsv = fileType == TYPE_CSV;
			boolean isBtx = fileType == TYPE_BTX;
			String sopt = "";
			if (isCsv || isBtx || hasTitle) {
				sopt += "@";
				if (isCsv)
					sopt += "c";
				if (isBtx)
					sopt += "b";
				if (hasTitle)
					sopt += "t";
			}
			if (A == null) { // ����
				if (isCursor) {
					buf.append("cursor");
				} else {
					buf.append("import");
				}
				buf.append(sopt + "(");
				if (fields != null) {
					buf.append(fields);
				}
				if (!isBtx)
					if (StringUtils.isValidString(s)) {
						buf.append(";,");
						buf.append(s);
					}
				buf.append(")");
			} else {// ����
				buf.append("export" + sopt + "(");
				buf.append(A);
				if (fields != null) {
					buf.append(";");
					buf.append(fields);
				}
				if (StringUtils.isValidString(s)) {
					buf.append(";");
					buf.append(s);
				}
				buf.append(")");
			}
		}
		Expression exp = new Expression(cs, ctx, buf.toString());
		return exp.calculate(ctx);
	}

	/** δ֪ */
	private static final byte TYPE_UNKNOWN = 0;
	/** excel */
	private static final byte TYPE_EXCEL = 1;
	/** csv */
	private static final byte TYPE_CSV = 2;
	/** btx */
	private static final byte TYPE_BTX = 3;
	/** ctx */
	private static final byte TYPE_CTX = 4;
	/** txt */
	private static final byte TYPE_TXT = 5;

	/**
	 * ȡ�ļ�����
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return �������ϳ����е�һ��
	 */
	private byte getFileType(String fileName) {
		if (fileName == null)
			return TYPE_UNKNOWN; // ǰ���Ѿ��жϿ��ˣ���Ӧ�ó���
		fileName = Escape.removeEscAndQuote(fileName);
		if (fileName.toLowerCase().endsWith(".xls")
				|| fileName.toLowerCase().endsWith(".xlsx")) {
			return TYPE_EXCEL;
		} else if (fileName.toLowerCase().endsWith(".csv")) {
			return TYPE_CSV;
		} else if (fileName.toLowerCase().endsWith(".btx")) {
			return TYPE_BTX;
		} else if (fileName.toLowerCase().endsWith(".ctx")) {
			return TYPE_CTX;
		} else if (fileName.toLowerCase().endsWith(".txt")) {
			return TYPE_TXT;
		}
		return TYPE_UNKNOWN;
	}
}
