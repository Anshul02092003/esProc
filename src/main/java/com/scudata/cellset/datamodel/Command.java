package com.scudata.cellset.datamodel;

import java.util.HashMap;
import java.util.List;

import com.scudata.cellset.ICellSet;
import com.scudata.cellset.INormalCell;
import com.scudata.common.CellLocation;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.Context;
import com.scudata.dm.KeyWord;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.ParamParser;
import com.scudata.resources.EngineMessage;

/**
 * �������������䣬if��for��
 * @author WangXiaoJun
 *
 */
public class Command {
	public static final byte IF = 1;
	public static final byte ELSE = 2;
	public static final byte ELSEIF = 3;

	public static final byte FOR = 4;
	public static final byte CONTINUE = 5;
	public static final byte BREAK = 6;

	//public static final byte FUNCCALL = 7;
	public static final byte FUNC = 8;
	public static final byte RETURN = 9;
	public static final byte END = 10;
	public static final byte RESULT = 11; // ���񷵻�ֵ

	public static final byte SQL = 12;
	public static final byte CLEAR = 13;
	//public static final byte ERROR = 14;

	public static final byte FORK = 15;
	public static final byte REDUCE = 16;
	public static final byte GOTO = 17;
	
	public static final byte CHANNEL = 18; // �ܵ�
	public static final byte TRY = 19; // �쳣��׽

	private static final HashMap<String, Byte> keyMap = new HashMap<String, Byte>(20);
	static {
		keyMap.put("if", new Byte(IF));
		keyMap.put("else", new Byte(ELSE));
		keyMap.put("elseif", new Byte(ELSEIF));

		keyMap.put("for", new Byte(FOR));
		keyMap.put("next", new Byte(CONTINUE));
		keyMap.put("break", new Byte(BREAK));

		//keyMap.put("call", new Byte(CALL));
		keyMap.put("func", new Byte(FUNC));
		keyMap.put("return", new Byte(RETURN));
		keyMap.put("end", new Byte(END));
		keyMap.put("result", new Byte(RESULT));
		keyMap.put("$", new Byte(SQL));
		keyMap.put("clear", new Byte(CLEAR));
		keyMap.put("fork", new Byte(FORK));
		keyMap.put("reduce", new Byte(REDUCE));
		
		keyMap.put("goto", new Byte(GOTO));
		keyMap.put("cursor", new Byte(CHANNEL));
		keyMap.put("try", new Byte(TRY));
	}

	private byte type; // ��������

	/**
	 * goto��  ��Ԫ��λ��
	 * break  for���ڸ��λ��
	 * next��  for���ڸ��λ��
	 */
	private String lctStr;
	private CellLocation lct;

	/**
	 * if��    �������ʽ
	 * elseif: �������ʽ
	 * for��   ѭ�����б��ʽ
	 * func c��  �������ʽ
	 * return������ֵ���ʽ
	 */
	protected String expStr;
	private IParam param;

	/**
	 * ����������
	 * @param type �������
	 * @param lctStr ��Ԫ���ʶ��
	 * @param expStr ���ʽ��
	 */
	public Command(byte type, String lctStr, String expStr) {
		this.type = type;
		this.expStr = expStr;

		if (lctStr != null && lctStr.length() != 0) {
			this.lctStr = lctStr;
		}
	}

	public byte getType() {
		return type;
	}

	public String getLocation() {
		return lctStr;
	}

	/**
	 * ȡ������õĵ�Ԫ��
	 * @param ctx ����������
	 * @return ��Ԫ��λ��
	 */
	public CellLocation getCellLocation(Context ctx) {
		if (lct != null) {
			return lct;
		}

		if (lctStr != null) {
			lct = CellLocation.parse(lctStr);
			if (lct == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(lctStr + mm.getMessage("cellset.cellNotExist"));
			}
		}

		return lct;
	}

	/**
	 * ȡ���������ʽ�ַ���
	 * @return �������ʽ�ַ���
	 */
	public String getExpression() {
		return expStr;
	}

	/**
	 * ȡ���Ĳ���
	 * @param cs
	 * @param ctx
	 * @return
	 */
	public IParam getParam(ICellSet cs, Context ctx) {
		if (param == null && expStr != null) {
			param = ParamParser.parse(expStr, cs, ctx, true);
		}

		return param;
	}
	
	/**
	 * ȡ��������Ӧ�ı��ʽ�������ֻ��һ������
	 * @param cs ����
	 * @param ctx ����������
	 * @return ���ʽ����
	 */
	public Expression getExpression(ICellSet cs, Context ctx) {
		IParam param = getParam(cs, ctx);
		if (param == null) {
			return null;
		} else if (param.isLeaf()) {
			return param.getLeafExpression();
		} else {
			IParam sub = param.getSub(0);
			while (sub != null && !sub.isLeaf()) {
				sub = sub.getSub(0);
			}

			return sub == null ? Expression.NULL : sub.getLeafExpression();
		}
	}

	/**
	 * ȡ��������Ӧ�ı��ʽ���飬���������ж������
	 * @param cs ����
	 * @param ctx ����������
	 * @return ���ʽ��������
	 */
	public Expression[] getExpressions(ICellSet cs, Context ctx) {
		IParam param = getParam(cs, ctx);
		if (param == null) {
			return new Expression[0];
		} else if (param.isLeaf()) {
			return new Expression[] {param.getLeafExpression()};
		} else {
			int size = param.getSubSize();
			Expression []exps = new Expression[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub != null) exps[i] = sub.getLeafExpression();
			}

			return exps;
		}
	}

	/**
	 * �жϱ���Ƿ���result���
	 * @param cmdStr ���ʽ��
	 * @return true���ǣ�false������
	 */
	public static boolean isResultCommand(String cmdStr) {
		return cmdStr.startsWith("result");
	}

	/**
	 * �жϱ���Ƿ���sql���
	 * @param cmdStr ���ʽ��
	 * @return true���ǣ�false������
	 */
	public static boolean isSqlCommand(String cmdStr) {
		return cmdStr != null && cmdStr.length() > 0 && cmdStr.charAt(0) == '$';
	}

	/**
	 * �жϱ��ʽ�Ƿ������
	 * @param cmdStr ���ʽ��
	 * @return
	 */
	public static boolean isCommand(String cmdStr) {
		if (cmdStr == null || cmdStr.length() == 0) {
			return false;
		} else if (cmdStr.charAt(0) == '$') {
			return true;
		}

		int pos = KeyWord.scanId(cmdStr, 0);
		int atIndex = cmdStr.lastIndexOf(KeyWord.OPTION, pos);
		if (atIndex != -1) {
			pos = atIndex;
		}

		String key = cmdStr.substring(0, pos);
		return (Byte)keyMap.get(key) != null;
	}

	/**
	 * �Ѵ���������Ӧ�����
	 * @param cmdStr���ʽ��
	 * @return ���
	 */
	public static Command parse(String cmdStr) {
		if (cmdStr == null || cmdStr.length() == 0) {
			return null;
		} else if (cmdStr.charAt(0) == '$') {
			return parseSqlCommand(cmdStr);
		}

		int pos = KeyWord.scanId(cmdStr, 0);
		int atIndex = cmdStr.lastIndexOf(KeyWord.OPTION, pos);
		if (atIndex != -1) {
			pos = atIndex;
		}

		Byte value = (Byte)keyMap.get(cmdStr.substring(0, pos));
		if (value == null) {
			return null;
		} else {
			return parse(value.byteValue(), cmdStr.substring(pos));
		}
	}

	// ���������ڵ��ַ�
	private static int scanSemicolon(String str, int start) {
		int idx = 0, len = str.length();
		while(idx < len) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') {
				idx = Sentence.scanQuotation(str, idx);
				if(idx < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\",\'" + mm.getMessage("Expression.illMatched"));
				}
				
				idx ++;
			} else if (ch == '{') {
				// ���������Ǽ��������ʽ���ܺ�����
				idx = Sentence.scanBrace(str, idx);
				if(idx < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\",\'" + mm.getMessage("Expression.illMatched"));
				}
				
				idx ++;
			} else if( ch == ';') {
				return idx;
			} else {
				idx ++;
			}
		}
		
		return -1;
	}

	// $@1(db)select .... ; param...
	//
	private static Command parseSqlCommand(String str) {
		String opt = null;
		String db = null;
		String sql;
		String param = null;

		int len = str.length();
		int sqlStart = 1;
		while (sqlStart < len) {
			char c = str.charAt(sqlStart);
			if (c == KeyWord.OPTION) { // @
				sqlStart++;
				int pos = KeyWord.scanId(str, sqlStart);
				opt = str.substring(sqlStart, pos);

				sqlStart = pos;
			} else if (c == '(') {
				int match = Sentence.scanParenthesis(str, sqlStart);
				if (match == -1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("(,)" + mm.getMessage("Expression.illMatched"));
				}

				db = str.substring(sqlStart + 1, match).trim();

				sqlStart = match + 1;
				break;
			} else if (Character.isWhitespace(c)) {
				sqlStart++;
			} else {
				break;
			}
		}

		int paramPos = scanSemicolon(str, sqlStart);
		if (paramPos == -1) {
			sql = str.substring(sqlStart, len);
		} else {
			sql = str.substring(sqlStart, paramPos);
			param = str.substring(paramPos + 1);
		}

		sql = sql.trim();
		if (sql.length() == 0) sql = null;
		return new SqlCommand(sql, db, opt, param);
	}

	private static Command parse(byte type, String param) {
		param = param.trim();
		String location = null;
		String exp = null;

		switch (type) {
		case FOR:
		case IF:
		case ELSEIF:
		case RETURN:
		case RESULT:
		case CLEAR:
		case END:
		case FORK:
		case CHANNEL:
		case FUNC: // func fn(arg,��)
			exp = param;
			break;
		case CONTINUE:
		case BREAK:
		case GOTO:
			location = param;
			break;
		case ELSE: // else if exp
			if (param.length() > 0) {
				if (param.startsWith("if")) {
					type = ELSEIF;
					exp = param.substring(2).trim();
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.unknownSentence"));
				}
			}
			break;
		case REDUCE:
		case TRY:
			if (param.length() > 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.unknownSentence"));
			}
			break;
		default:
			throw new RuntimeException();
		}

		Command command = new Command(type, location, exp);
		return command;
	}
	
	/**
	 * ȡ������õ��ĵ�Ԫ��
	 * @param cs �������
	 * @param ctx ����������
	 * @param resultList ���������������õ�������
	 */
	public void getUsedCells(ICellSet cs, Context ctx, List<INormalCell> resultList) {
		IParam param = getParam(cs, ctx);
		if (param != null) {
			param.getUsedCells(resultList);
		}
		
		CellLocation lct = getCellLocation(ctx);
		if (lct != null) {
			INormalCell cell = cs.getCell(lct.getRow(), lct.getCol());
			if (!resultList.contains(cell)) {
				resultList.add(cell);
			}
		}
	}
}
