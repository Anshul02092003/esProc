package com.scudata.dm.sql;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.resources.EngineMessage;

/**
 * ��SQL������ִʴ����õ�������
 * @author RunQian
 *
 */
public final class Tokenizer {
	public static final char UNKNOWN = 0;
	public static final char KEYWORD = 1; // ������
	public static final char IDENT = 2; // ���ֶΡ�����������
	public static final char NUMBER = 3; // ���ֳ���
	public static final char STRING = 4; // �ַ�������
	public static final char OPERATOR = 5; // �����+-*/=<>&|^%!

	public static final char LPAREN = '(';
	public static final char RPAREN = ')';
	public static final char COMMA = ',';
	public static final char DOT = '.';
	public static final char PARAMMARK = '?';
	

	private static final String OPSTRING = "+-*/=<>&|^%!~"; // ~��λȡ��
	//private final static String[] GATHERS = {"AVG", "COUNT", "MAX", "MIN", "SUM", "COUNTIF", "COUNTD", "AVGD", "SUMD"};
	//private final static String[] ���ں��� = {"AVG", "COUNT", "MAX", "MIN", "SUM", "RANK", "DENSE_RANK", "ROW_NUMBER"};

	//private final static String[] OPERATORS = {
	//	"+","-","*","/","=","<","<=","<>","!=",">",">=","%","^","||"
	//};

	public static final String COL_AS = " "; // " AS "
	public static final String TABLE_AS = " "; // oracle��ı������ܼ�as

	// ѡ���б��ʽ�п����õĹؼ��֣������ж�ѡ�������ı�ʶ���Ƿ��Ǳ���
	private final static String[] OPKEYWORDS = {"AND", "OR", "LIKE", "NOT"};

	// "BETWEEN","ROWS","CASE","INNER","END","ONLY", "OUTER",
	private final static String[] KEYWORDS = {
		"ALL","AND","AS","ASC", "AT",
		"BETWEEN", "BOTTOM","BY",
		"CALL","CREATE",//"CASE",
		"DESC","DISTINCT","DROP",
		"END", "EXCEPT","EXISTS","ELSE",
		"FETCH","FIRST","FROM","FULL",
		"GROUP",
		"HAVING",
		"IN","INTERSECT","IS","INTO",
		"JOIN",
		"LIKE","LEFT","LIMIT",
		"MINUS",
		"NOT","NULL",
		"ON","ONLY","OR","ORDER",
		"ROWS",
		"SELECT",
		"THEN","TO","TOP","TABLE","TEMPORARY","TEMP",
		"UNION",
		"WHEN","WHERE","WITH"
	};

	/**
	 * �������ŵĽ���λ��
	 * @param tokens ��SQL������ִʵõ��Ľ��
	 * @param start �����ŵ�λ��
	 * @param next �����Ľ���λ�ã�������
	 * @return �����ŵ�λ�ã��Ҳ����׳��쳣
	 */
	public static int scanParen(Token []tokens, int start, final int next) {
		int deep = 0;
		for (int i = start + 1; i < next; ++i) {
			if (tokens[i].getType() == LPAREN) {
				deep++;
			} else if (tokens[i].getType() == RPAREN) {
				if (deep == 0) return i;
				deep--;
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException("(,)" + mm.getMessage("Expression.illMatched"));
	}
	
	/**
	 * ��ָ����Χ����������
	 * @param tokens ��SQL������ִʵõ��Ľ��
	 * @param start ������ʼλ�ã�����
	 * @param next �����Ľ���λ�ã�������
	 * @return ���ŵ�λ�ã��Ҳ�������-1
	 */
	public static int scanComma(Token []tokens, int start, final int next) {
		for(int i = start; i < next; ++i) {
			char type = tokens[i].getType();
			if(type == COMMA) {
				return i;
			} else if(type == LPAREN) { // ����()
				i = scanParen(tokens, i, next);
			}
		}

		return -1;
	}

	public static String []getStrings(Token []tokens, int start, final int next) {
		String []ts = new String[next - start];
		for (int i = start; i < next; ++i) {
			ts[i - start] = tokens[i].getString();
		}

		return ts;
	}

	public static boolean isOperatorKeyWord(String name) {
		return isKeyWord(name, OPKEYWORDS);
	}

	public static boolean isKeyWord(String id, String []keyWords) {
		if (id == null)return false;
		id = id.toUpperCase();
		for (int i = 0, len = keyWords.length; i < len; ++i) {
			if (id.equals(keyWords[i]))return true;
		}

		return false;
	}

	public static boolean isKeyWord(String id) {
		return isKeyWord(id, KEYWORDS);
	}

	public static boolean isIdentifierStart(char ch) {
		// sqlserver��ʱ�������#��
		return Character.isJavaIdentifierStart(ch); //  || ch == '#'
	}

	public static boolean isIdentifierPart(char ch) {
		// sqlserver��ʱ�������#��
		return Character.isJavaIdentifierPart(ch); //  || ch == '#'
	}

	/**
	 * ��SQL������ִʣ�˫�����ڵ�Ϊ��ʶ��
	 * @param sql SQL���
	 * @return Token����
	 */
	public static Token[] parse(String sql) {
		int curIndex = 0;
		int cmdLen = sql.length();
		ArrayList<Token> tokenList = new ArrayList<Token>();

		while (curIndex < cmdLen) {
			char ch = sql.charAt(curIndex);
			if (Character.isWhitespace(ch)) {
				curIndex++;
			} else if (isIdentifierStart(ch)) { // ��ʶ�����ֶΡ���
				int next = scanId(sql, curIndex + 1);
				String id = sql.substring(curIndex, next);
				String upId = id.toUpperCase();
				if (isKeyWord(upId)) {
					Token token = new Token(KEYWORD, upId, curIndex);
					tokenList.add(token);
				} else {
					Token token = new Token(IDENT, id, curIndex);
					tokenList.add(token);
				}

				curIndex = next;
			} else if (Character.isDigit(ch)) { // ����
				int next = scanNumber(sql, curIndex + 1);
				String id = sql.substring(curIndex, next);
				Token token = new Token(NUMBER, id, curIndex);
				tokenList.add(token);

				curIndex = next;
			} else if (ch == DOT) { // .������������
				int next = scanNumber(sql, curIndex);
				String id = sql.substring(curIndex, next);
				if (next > curIndex + 1) {
					Token token = new Token(NUMBER, id, curIndex);
					tokenList.add(token);
				} else {
					Token token = new Token(DOT, id, curIndex);
					tokenList.add(token);
				}

				curIndex = next;
			} else if (ch == '\'') { // �ַ���
				int next = scanString(sql, curIndex + 1);
				if (next < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("'" + mm.getMessage("Expression.illMatched"));
				}

				String id = sql.substring(curIndex, next);
				Token token = new Token(STRING, id, curIndex);
				tokenList.add(token);

				curIndex = next;
			} else if (ch == '"') { // �������ֶ�
				int next = scanId(sql, curIndex);
				String id = sql.substring(curIndex, next);
				Token token = new Token(IDENT, id, curIndex);
				tokenList.add(token);

				curIndex = next;
			} else if (ch == PARAMMARK) { // ? ?1 ?2 ...����
				Token token = new Token(PARAMMARK, "?", curIndex);
				tokenList.add(token);
				
				curIndex++;
			} else if (OPSTRING.indexOf(ch) != -1) { // �����
				String id = sql.substring(curIndex, curIndex + 1);
				Token token = new Token(OPERATOR, id, curIndex);
				tokenList.add(token);

				curIndex++;
			} else if (ch == LPAREN || ch == RPAREN || ch == COMMA) { // (),
				String id = sql.substring(curIndex, curIndex + 1);
				Token token = new Token(ch, id, curIndex);
				tokenList.add(token);

				curIndex++;
			} else {
				int next = scanId(sql, curIndex + 1);
				Token token = new Token(UNKNOWN, sql.substring(curIndex, next), curIndex);
				tokenList.add(token);

				curIndex = next;
			}
		}

		int size = tokenList.size();
		Token []tokens = new Token[size];
		tokenList.toArray(tokens);
		return tokens;
	}

	private static int scanId(String command, int start) {
		int len = command.length();
		if (start == len) return start;

		if (command.charAt(start) == '"') {
			start = scanIdString(command, start + 1);
			if (start < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\"" + mm.getMessage("Expression.illMatched"));
			}

			return start;
		} else {
			for (; start < len; ++start) {
				char ch = command.charAt(start);
				if (!isIdentifierPart(ch)) {
					break;
				}
			}

			return start;
		}
	}

	public static int scanNumber(String command, int start) {
		int len = command.length();
		for (; start < len; ++start) {
			if (!Character.isDigit(command.charAt(start))) break;
		}


		if (start < len && command.charAt(start) == '.') {
			for (++start; start < len; ++start) {
				if (!Character.isDigit(command.charAt(start))) break;
			}
		}

		return start;
	}

	// ����'ss'������ַ����ﺬ�е���������������һ��ĵ����ű�ʾ 'dd''ff'
	public static int scanString(String command, int start) {
		int len = command.length();
		for (; start < len; ++start) {
			if (command.charAt(start) == '\'') {
				start++;
				if (start == len || command.charAt(start) != '\'') {
					return start;
				}
			}
		}

		return -1;
	}

	private static int scanIdString(String command, int start) {
		int len = command.length();
		for (; start < len; ++start) {
			if (command.charAt(start) == '"') {
				start++;
				if (start == len || command.charAt(start) != '"') {
					return start;
				}
			}
		}

		return -1;
	}
	
	public static boolean isOperator(String ch) {
		return (ch.length() == 1 && OPSTRING.indexOf(ch) != -1);
	}
}
