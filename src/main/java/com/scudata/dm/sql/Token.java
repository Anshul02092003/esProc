package com.scudata.dm.sql;

/**
 * SQL���ִʺ�õ��Ĵʻ��߷���
 * @author RunQian
 *
 */
public final class Token {
	private String id;
	private int pos;
	private char type;

	public Token(char type, String id, int pos) {
		this.type = type;
		this.id = id;
		this.pos = pos;
	}

	public boolean isKeyWord() {
		return type == Tokenizer.KEYWORD;
	}

	public int getPos() {
		return pos;
	}

	public void setType(char type) {
		this.type = type;
	}
	
	public char getType() {
		return type;
	}

	public void setString(String str) {
		id = str;
	}

	public String getString() {
		return id;
	}

	public String toString() {
		return id;
	}

	public boolean equals(Token other) {
		return id.equals(other.id);
	}

	public boolean equals(String str) {
		return id.equals(str);
	}

	// �жϱ��ʽ�ڵ��Ƿ�������ҽڵ�
	// ��������������ؼ��ֿ������ұ��ʽ
	public boolean canHaveRightExp() {
		if (type == Tokenizer.OPERATOR || type == Tokenizer.DOT) { //type == Tokenizer.TABLEMARK || LEVELMARK
			return true;
		}

		return type == Tokenizer.KEYWORD && Tokenizer.isOperatorKeyWord(id);
	}

	public boolean isKeyWord(String str) {
		return type == Tokenizer.KEYWORD && id.equals(str);
	}

	public boolean isMergeKeyWord() {
		if (type != Tokenizer.KEYWORD) return false;
		return id.equals("UNION") || id.equals("INTERSECT") || id.equals("EXCEPT") || id.equals("MINUS");
	}

	// #L
	public String getLevelName() {
		return id.substring(1);
	}

	// @s
	public String getTableName() {
		return id.substring(1);
	}
}
