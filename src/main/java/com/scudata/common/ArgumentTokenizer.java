package com.scudata.common;

import java.util.Enumeration;

/**
 * �������ڲ���ַ�����Ĭ�ϵķָ���Ϊ����
 * �ָ�ʱ��������˫������ָ�����������ڵķָ���
 * �ر�ע�⣺�հ״�����һ����ǣ���ֵҲΪ�հ״�
 * @author RunQian
 *
 */
public final class ArgumentTokenizer implements Enumeration<String> {
	private String str; // Դ��
	private int len; // ����
	private int index; // ��ǰ�ָ��λ��
	private char delim = ','; // �ָ�����Ĭ��Ϊ����

	private boolean parentheses = false; // �Ƿ����Բ�����ڵķָ���
	private boolean brackets = false; // �Ƿ�����������ڵķָ���
	private boolean braces = false; // �Ƿ���Ի������ڵķָ���
	private boolean singleQuotation = false; // �Ƿ���Ե������ڵķָ���
	
	private boolean count; // ֻȡ��������ȡ�ָ����Ӵ�

	/**
	 * Ϊָ���ַ�������һ�������ָ�����ȱʡ�ָ���Ϊ','��
	 * @param s ָ�����ַ���
	 */
	public ArgumentTokenizer(String s) {
		this(s, ',', false, false, false);
	}

	/**
	 * Ϊָ���ַ�������һ�������ָ���
	 * @param s ָ�����ַ���
	 * @param delim ָ���ķָ���
	 */
	public ArgumentTokenizer(String s, char delim) {
		this(s, delim, false, false, false);
	}

	/**
	 * Ϊָ���ַ�������һ�������ָ���
	 * @param s ָ�����ַ���
	 * @param ignoreParentheses ����Բ�����ڵķָ���
	 * @param ignoreBrackets �����������ڵķָ���
	 * @param ignoreBraces ���Ի������ڵķָ���
	 */
	public ArgumentTokenizer(String s, boolean ignoreParentheses,
			boolean ignoreBrackets, boolean ignoreBraces) {
		this(s, ',', ignoreParentheses, ignoreBrackets, ignoreBraces);
	}

	/**
	 * Ϊָ���ַ�������һ�������ָ���
	 * @param s ָ�����ַ���
	 * @param delim ָ���ķָ���
	 * @param ignoreParentheses ����Բ�����ڵķָ���
	 * @param ignoreBrackets �����������ڵķָ���
	 * @param ignoreBraces ���Ի������ڵķָ���
	 */
	public ArgumentTokenizer(String s, char delim, boolean ignoreParentheses,
			boolean ignoreBrackets, boolean ignoreBraces) {
		this(s, delim, ignoreParentheses, ignoreBrackets, ignoreBraces, false);
	}

	/**
	 * Ϊָ���ַ�������һ�������ָ���
	 * @param s ָ�����ַ���
	 * @param delim ָ���ķָ���
	 * @param ignoreParentheses ����Բ�����ڵķָ���
	 * @param ignoreBrackets �����������ڵķָ���
	 * @param ignoreBraces ���Ի������ڵķָ���
	 * @param ignoreSingleQuotation ���Ե������ڵķָ���
	 */
	public ArgumentTokenizer(String s, char delim, boolean ignoreParentheses,
			boolean ignoreBrackets, boolean ignoreBraces,
			boolean ignoreSingleQuotation) {
		// str = s.trim();
		str = s;
		this.delim = delim;
		this.parentheses = !ignoreParentheses;
		this.brackets = !ignoreBrackets;
		this.braces = !ignoreBraces;
		this.singleQuotation = !ignoreSingleQuotation;
		len = (str == null || str.length() == 0) ? -1 : str.length();
	}

	/**
	 * ȡ��һ�����
	 * @return ���ַ���Ϊnull���򷵻�null����hasNext()��hasMoreTokens()Ϊ�棬�򷵻�
	 *         �ָ���ָ�ı��(�ǿմ���մ�)�����򷵻�null�� ����ƥ�������(��/˫)�򷵻����ź�������ַ�
	 *         ������ǰת���\��������Ų�����������
	 * 
	 */
	public String next() {
		if (str == null || index > len)
			return null;
		int old = index;
		while (index <= len) {
			if (index == len) {
				index++;
				if (len > 1 && str.charAt(len - 1) == delim)
					return count ? null : "";
				break;
			}
			char ch = str.charAt(index);
			if (ch == '\\') {
				index += 2;
				continue;
			}
			if (ch == '\"' || (singleQuotation && ch == '\'')) {
				int tmp = Sentence.scanQuotation(str, index);
				if (tmp < 0) {
					index = len + 1;
					return count ? null : str.substring(old);
				}
				index = tmp + 1;
				continue;
			}
			if (parentheses && ch == '(') {
				int tmp = Sentence.scanParenthesis(str, index);
				if (tmp < 0) {
					index = len + 1;
					return count ? null : str.substring(old);
				}
				index = tmp + 1;
				continue;
			}
			if (brackets && ch == '[') {
				int tmp = Sentence.scanBracket(str, index);
				if (tmp < 0) {
					index = len + 1;
					return count ? null : str.substring(old);
				}
				index = tmp + 1;
				continue;
			}
			if (braces && ch == '{') {
				int tmp = Sentence.scanBrace(str, index);
				if (tmp < 0) {
					index = len + 1;
					return count ? null : str.substring(old);
				}
				index = tmp + 1;
				continue;
			}
			index++;
			if (ch == delim)
				break;
		}
		return count ? null : str.substring(old, index - 1);
	}

	/**
	 * ȡ��һ�����
	 * @return ���ַ���Ϊnull���򷵻�null����hasMoreTokens()Ϊ�棬�򷵻طָ���ָ�
	 *         �ı��(�ǿմ���մ�)�����򷵻�null��
	 */
	public String nextToken() {
		return next();
	}

	/**
	 * ȡ��һ�����
	 * @return ���ַ���Ϊnull���򷵻�null����hasMoreTokens()Ϊ�棬�򷵻طָ���ָ�
	 *         �ı��(�ǿմ���մ�)�����򷵻�null��
	 */
	public String nextElement() {
		return next();
	}

	/**
	 * ����ָ���ַ���������δ���ʱ�ǵĸ�����
	 * @return �ַ����б����
	 */
	public int countTokens() {
		int j = index;
		count = true;
		int i;
		for (i = 0; index <= len; i++)
			next();

		index = j;
		count = false;
		return i;
	}

	/**
	 * ����Ƿ��б��
	 * @return ���б�Ƿ���true�����򷵻�false
	 */
	public boolean hasNext() {
		return index <= len;
	}

	/**
	 * ����Ƿ��б��
	 * @return ���б�Ƿ���true�����򷵻�false
	 */
	public boolean hasMoreTokens() {
		return hasNext();
	}

	/**
	 * ����Ƿ��б��
	 * @return ���б�Ƿ���true�����򷵻�false
	 */
	public boolean hasMoreElements() {
		return hasNext();
	}
}
