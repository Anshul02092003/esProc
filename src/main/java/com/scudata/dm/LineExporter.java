package com.scudata.dm;

import java.io.IOException;
import java.io.OutputStream;

import com.scudata.util.Variant;

/**
 * ���ڵ������е��ı��ļ�
 * @author WangXiaoJun
 *
 */
public class LineExporter implements ILineOutput {
	private OutputStream os; // �����
	private final String charset; // �ַ���
	private final byte []colSeparator; // �зָ���
	private final byte []lineSeparator; // �зָ���
	private boolean isAppend; // �Ƿ�׷��д
	
	private char escapeChar = '\\';
	private boolean isQuote = false; // �ַ����Ƿ������

	/**
	 * ���찴���������
	 * @param os �����
	 * @param charset �ַ���
	 * @param colSeparator �зָ���
	 * @param lineSeparator �зָ���
	 * @param isAppend �Ƿ�׷��д
	 */
	public LineExporter(OutputStream os, String charset, byte []colSeparator, byte []lineSeparator, boolean isAppend) {
		this.os = os;
		this.charset = charset;
		this.colSeparator = colSeparator;
		this.lineSeparator = lineSeparator;
		this.isAppend = isAppend;
	}
	
	/**
	 * �����ַ����Ƿ������
	 * @param b
	 */
	public void setQuote(boolean b) {
		this.isQuote = b;
	}
	
	/**
	 * ����ת���
	 * @param c ת���
	 */
	public void setEscapeChar(char c) {
		escapeChar = c;
	}
	
	/**
	 * ȡת���
	 * @return char
	 */
	public char getEscapeChar() {
		return escapeChar;
	}

	/**
	 * �ر����
	 * @throws IOException
	 */
	public void close() throws IOException {
		os.close();
	}

	/**
	 * д��һ������
	 * @param items ��ֵ��ɵ�����
	 * @throws IOException
	 */
	public void writeLine(Object []items) throws IOException {
		if (isAppend) {
			os.write(lineSeparator);
		} else {
			isAppend = true;
		}
		
		int last = items.length - 1;
		if (isQuote) {
			for (int i = 0; i < last; ++i) {
				String str = Variant.toExportString(items[i], escapeChar);
				if (str != null) {
					os.write(str.getBytes(charset));
				}

				os.write(colSeparator);
			}

			String str = Variant.toExportString(items[last], escapeChar);
			if (str != null) {
				os.write(str.getBytes(charset));
			}
		} else {
			for (int i = 0; i < last; ++i) {
				String str = Variant.toExportString(items[i]);
				if (str != null) {
					os.write(str.getBytes(charset));
				}

				os.write(colSeparator);
			}

			String str = Variant.toExportString(items[last]);
			if (str != null) {
				os.write(str.getBytes(charset));
			}
		}
	}
}
