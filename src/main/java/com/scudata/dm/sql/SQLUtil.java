package com.scudata.dm.sql;

import com.scudata.common.ArgumentTokenizer;
import com.scudata.common.DBTypes;
import com.scudata.common.IntArrayList;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

public final class SQLUtil {
	private static Sequence parseFilter(String sql, Token []tokens, int index, int endPos) {
		Sequence seq = new Sequence();
		IntArrayList andList = new IntArrayList();
		for (int i = index + 1, count = tokens.length; i < count && tokens[i].getPos() < endPos; ++i) {
			Token token = tokens[i];
			if (token.isKeyWord("AND")) {
				andList.addInt(i);
			} else if (token.isKeyWord("OR")) {
				andList.clear();
			} else if (token.getType() == Tokenizer.LPAREN) {
				i = Tokenizer.scanParen(tokens, i, count);
			}
		}
		
		int size = andList.size();
		int prev = tokens[index].getPos();
		for (int i = 0; i < size; ++i) {
			int t = andList.getInt(i);
			String exp = sql.substring(prev, tokens[t].getPos());
			seq.add(exp.trim());
			prev = tokens[t + 1].getPos();
		}
		
		String exp = sql.substring(prev, endPos);
		seq.add(exp.trim());
		return seq;
	}
	
	private static int pos(int p1, Token []tokens, int len) {
		if (p1 > 0) return tokens[p1].getPos();
		return len;
	}
	
	private static int pos(int p1, int p2, Token []tokens, int len) {
		if (p1 > 0) return tokens[p1].getPos();
		if (p2 > 0) return tokens[p2].getPos();
		return len;
	}
	
	private static int pos(int p1, int p2, int p3, Token []tokens, int len) {
		if (p1 > 0) return tokens[p1].getPos();
		if (p2 > 0) return tokens[p2].getPos();
		if (p3 > 0) return tokens[p3].getPos();
		return len;
	}
	
	private static int pos(int p1, int p2, int p3, int p4, Token []tokens, int len) {
		if (p1 > 0) return tokens[p1].getPos();
		if (p2 > 0) return tokens[p2].getPos();
		if (p3 > 0) return tokens[p3].getPos();
		if (p4 > 0) return tokens[p4].getPos();
		return len;
	}
	
	// ����()����ʼλ����������
	public static int scanParen(Token []tokens, int start, final int next) {
		int deep = 0;
		for (int i = start + 1; i < next; ++i) {
			if (tokens[i].getType() == Tokenizer.LPAREN) {
				deep++;
			} else if (tokens[i].getType() == Tokenizer.RPAREN) {
				if (deep == 0) return i;
				deep--;
			}
		}

		return -1;
	}

	private static DataStruct DS_SQL = new DataStruct(new String[]{"type", "id", "value"});
	private static String TYPE_KEYWORD = "keyword";
	
	private static int splitWith(Token []tokens, int pos, Record result) {
		return -1;
	}
	
	private static int splitSelect(Token []tokens, int pos, Record result) {
		return -1;
	}
	
	public static Sequence splitSql(String sql) {
		Token []tokens = Tokenizer.parse(sql);
		int len = tokens.length;
		if (len == 0) {
			return null;
		}
		
		Sequence result = new Sequence();
		int pos = 0;
		Token token = tokens[0];
		
		if (token.isKeyWord("WITH")) {
			// with alias_name1 as (select1)[,alias_namei as (selecti) ]
			Record r = new Record(DS_SQL);
			r.setNormalFieldValue(0, TYPE_KEYWORD);
			r.setNormalFieldValue(1, "WITH");
			int end = splitWith(tokens, 0, r);
			result.add(r);
			
			if (end < len) {
				pos = end;
				token = tokens[pos];
			} else {
				return result;
			}
		}
		
		if (token.isKeyWord("SELECT")) {
			Record r = new Record(DS_SQL);
			r.setNormalFieldValue(0, TYPE_KEYWORD);
			r.setNormalFieldValue(1, "SELECT");
			int end = splitSelect(tokens, 0, r);
			result.add(r);
			
			if (end < len) {
				pos = end;
				token = tokens[pos];
			} else {
				return result;
			}
		}
		
		return null;
	}
	
	public static Object parse(String sql, String option) {
		boolean isSelect = false, isFrom = false, isWhere = false, 
				isGroupBy = false, isHaving = false, isOrderBy = false;
		boolean isArray = false, isAll = true;
		
		if (option != null) {
			if (option.indexOf('s') != -1) {
				isSelect = true;
				isAll = false;
			}
			if (option.indexOf('f') != -1) {
				isFrom = true;
				isAll = false;
			}
			if (option.indexOf('w') != -1) {
				isWhere = true;
				isAll = false;
			}
			if (option.indexOf('g') != -1) {
				isGroupBy = true;
				isAll = false;
			}
			if (option.indexOf('h') != -1) {
				isHaving = true;
				isAll = false;
			}
			if (option.indexOf('o') != -1) {
				isOrderBy = true;
				isAll = false;
			}
			if (option.indexOf('a') != -1) {
				isArray = true;
			}
		}
		
		int len = sql.length();
		Token []tokens = Tokenizer.parse(sql);
		int select = -1;
		int count = tokens.length;
		
		for (int i = 0; i < count; ++i) {
			if (tokens[i].isKeyWord("SELECT")) {
				select = i;
				break;
			} else if (tokens[i].getType() == Tokenizer.LPAREN) {
				i = scanParen(tokens, i, count);
				if (i == -1) {
					return null;
				}
			}
		}
		
		if (select == -1) {
			return null;
		}
		
		int from = -1;
		for (int i = select + 1; i < count; ++i) {
			if (tokens[i].isKeyWord("FROM")) {
				from = i;
				break;
			} else if (tokens[i].getType() == Tokenizer.LPAREN) {
				i = scanParen(tokens, i, count);
				if (i == -1) {
					return null;
				}
			}
		}
		
		if (from == -1) {
			return null;
		}
		
		int where = -1;
		int group = -1;
		int having = -1;
		int order = -1;
		for (int i = from + 1; i < count; ++i) {
			if (where == -1 && tokens[i].isKeyWord("WHERE")) {
				where = i;
			} else if (group == -1 && tokens[i].isKeyWord("GROUP")) {
				group = i;
			} else if (having == -1 && tokens[i].isKeyWord("HAVING")) {
				having = i;
			} else if (order == -1 && tokens[i].isKeyWord("ORDER")) {
				order = i;
			} else if (tokens[i].getType() == Tokenizer.LPAREN) {
				i = scanParen(tokens, i, count);
				if (i == -1) {
					return null;
				}
			}
		}
		
		if (isAll) {
			Sequence result = new Sequence(6);
			String cols = sql.substring(tokens[select + 1].getPos(), tokens[from].getPos());
			cols = cols.trim();
			if (isArray) {
				ArgumentTokenizer arg = new ArgumentTokenizer(cols);
				Sequence seq = new Sequence();
				result.add(seq);
				while (arg.hasMoreElements()) {
					seq.add(arg.nextElement());
				}
			} else {
				result.add(cols);
			}
			
			String tables = sql.substring(tokens[from + 1].getPos(), pos(where, group, having, order, tokens, len));
			result.add(tables.trim());
			
			if (where == -1) {
				result.add(null);
			} else if (isArray) {
				Sequence seq = parseFilter(sql, tokens, where + 1, pos(group, having, order, tokens, len));
				result.add(seq);
			} else {
				String whereExp = sql.substring(tokens[where + 1].getPos(), pos(group, having, order, tokens, len));
				result.add(whereExp.trim());
			}
			
			if (group == -1) {
				result.add(null);
			} else {
				group++;
				if (group == count || !tokens[group].isKeyWord("BY")) {
					throw new RQException("Invalid group.");
				}
				
				String groupExp = sql.substring(tokens[group + 1].getPos(), pos(having, order, tokens, len));
				groupExp = groupExp.trim();
				if (isArray) {
					ArgumentTokenizer arg = new ArgumentTokenizer(groupExp);
					Sequence seq = new Sequence();
					result.add(seq);
					while (arg.hasMoreElements()) {
						seq.add(arg.nextElement());
					}
				} else {
					result.add(groupExp);
				}
			}
			
			if (having == -1) {
				result.add(null);
			} else if (isArray) {
				Sequence seq = parseFilter(sql, tokens, having + 1, pos(order, tokens, len));
				result.add(seq);
			} else {
				String havingExp = sql.substring(tokens[having + 1].getPos(), pos(order, tokens, len));
				result.add(havingExp.trim());
			}
			
			if (order == -1) {
				result.add(null);
			} else {
				order++;
				if (order == count || !tokens[order].isKeyWord("BY")) {
					throw new RQException("Invalid order.");
				}
				
				String orderExp = sql.substring(tokens[order + 1].getPos());
				orderExp = orderExp.trim();
				if (isArray) {
					ArgumentTokenizer arg = new ArgumentTokenizer(orderExp);
					Sequence seq = new Sequence();
					result.add(seq);
					while (arg.hasMoreElements()) {
						seq.add(arg.nextElement());
					}
				} else {
					result.add(orderExp);
				}
			}
			
			return result;
		}
		
		Sequence result = new Sequence(6);
		if (isSelect) {
			String cols = sql.substring(tokens[select + 1].getPos(), tokens[from].getPos());
			cols = cols.trim();
			if (isArray) {
				ArgumentTokenizer arg = new ArgumentTokenizer(cols);
				Sequence seq = new Sequence();
				result.add(seq);
				while (arg.hasMoreElements()) {
					seq.add(arg.nextElement());
				}
			} else {
				result.add(cols);
			}
		}
		
		if (isFrom) {
			String tables = sql.substring(tokens[from + 1].getPos(), pos(where, group, having, order, tokens, len));
			result.add(tables.trim());
		}
		
		if (isWhere) {
			if (where == -1) {
				result.add(null);
			} else if (isArray) {
				Sequence seq = parseFilter(sql, tokens, where + 1, pos(group, having, order, tokens, len));
				result.add(seq);
			} else {
				String whereExp = sql.substring(tokens[where + 1].getPos(), pos(group, having, order, tokens, len));
				result.add(whereExp.trim());
			}
		}
		
		if (isGroupBy) {
			if (group == -1) {
				result.add(null);
			} else {
				group++;
				if (group == count || !tokens[group].isKeyWord("BY")) {
					throw new RQException("Invalid group.");
				}
				
				String groupExp = sql.substring(tokens[group + 1].getPos(), pos(having, order, tokens, len));
				groupExp = groupExp.trim();
				if (isArray) {
					ArgumentTokenizer arg = new ArgumentTokenizer(groupExp);
					Sequence seq = new Sequence();
					result.add(seq);
					while (arg.hasMoreElements()) {
						seq.add(arg.nextElement());
					}
				} else {
					result.add(groupExp);
				}
			}
		}
		
		if (isHaving) {
			if (having == -1) {
				result.add(null);
			} else if (isArray) {
				Sequence seq = parseFilter(sql, tokens, having + 1, pos(order, tokens, len));
				result.add(seq);
			} else {
				String havingExp = sql.substring(tokens[having + 1].getPos(), pos(order, tokens, len));
				result.add(havingExp.trim());
			}
		}
		
		if (isOrderBy) {
			if (order == -1) {
				result.add(null);
			} else {
				order++;
				if (order == count || !tokens[order].isKeyWord("BY")) {
					throw new RQException("Invalid order.");
				}
				
				String orderExp = sql.substring(tokens[order + 1].getPos());
				orderExp = orderExp.trim();
				if (isArray) {
					ArgumentTokenizer arg = new ArgumentTokenizer(orderExp);
					Sequence seq = new Sequence();
					result.add(seq);
					while (arg.hasMoreElements()) {
						seq.add(arg.nextElement());
					}
				} else {
					result.add(orderExp);
				}
			}
		}
		
		if (result.length() == 1) {
			return result.get(1);
		} else {
			return result;
		}
	}
	
	public static String replace(String sql, String replacement, String option) {
		if (option == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sqlparse" + mm.getMessage("engine.optError"));
		}

		Token []tokens = Tokenizer.parse(sql);
		int count = tokens.length;
		int start = -1;
		int end = -1;
		
		if (option.indexOf('s') != -1) {
			for (int i = 0; i < count; ++i) {
				if (tokens[i].isKeyWord("SELECT")) {
					start = i;
					break;
				}
			}
			
			if (start == -1) {
				return sql;
			}
			
			start++;
			for (int i = start; i < count; ++i) {
				Token token = tokens[i];
				if (token.isKeyWord("FROM")) {
					end = i;
					break;
				} else if (token.isKeyWord("TOP")) {
					i++; // top n
					start = i + 1;
				} else if (token.isKeyWord("DISTINCT")) {
					start = i + 1;
				}
			}
			
			if (end == -1) {
				return sql;
			}
		} else {
			String startKeyWord;
			String []endKeyWords;
			if (option.indexOf('f') != -1) {
				startKeyWord = "FROM";
				endKeyWords = new String[]{"WHERE", "GROUP", "HAVING", "ORDER", "LIMIT"};
			} else if (option.indexOf('w') != -1) {
				startKeyWord = "WHERE";
				endKeyWords = new String[]{"GROUP", "HAVING", "ORDER", "LIMIT"};
			} else if (option.indexOf('g') != -1) {
				startKeyWord = "GROUP";
				endKeyWords = new String[]{"HAVING", "ORDER", "LIMIT"};
			} else if (option.indexOf('h') != -1) {
				startKeyWord = "HAVING";
				endKeyWords = new String[]{"ORDER", "LIMIT"};
			} else if (option.indexOf('o') != -1) {
				startKeyWord = "ORDER";
				endKeyWords = new String[]{"LIMIT"};
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sqlparse" + mm.getMessage("engine.optError"));
			}
			
			for (int i = 0; i < count; ++i) {
				if (tokens[i].isKeyWord(startKeyWord)) {
					start = i + 1;
					if (start < count && tokens[start].isKeyWord("BY")) {
						start++; // group by / order by
					}
					
					break;
				}
			}
			
			if (start == -1) {
				return sql;
			}
			
			int len = endKeyWords.length;
			
			Next:
			for (int i = start; i < count; ++i) {
				Token token = tokens[i];
				if (token.isKeyWord()) {
					for (int k = 0; k < len; ++k) {
						if (token.isKeyWord(endKeyWords[k])) {
							end = i;
							break Next;
						}
					}
				}
			}
		}
		
		int startPos = tokens[start].getPos();
		if (end == -1) {
			return sql.substring(0, startPos) + replacement;
		} else {
			int endPos = tokens[end].getPos();
			return sql.substring(0, startPos) + replacement + sql.substring(endPos - 1);
		}
	}
	
	// ���߼�SQL���������SQL������top�ͺ���
	// DBTypes�ﶨ�������ݿ����ͳ���
	public static String translate(String sql, String dbType) {
		Token []tokens = Tokenizer.parse(sql);
		int count = tokens.length;
		int prevPos = 0;
		String rowNum = null;
		
		StringBuffer sb = new StringBuffer(sql.length() * 2);
		for (int i = 0, last = count - 1; i < last; ++i) {
			Token token = tokens[i];
			if (token.getType() == Tokenizer.IDENT) {
				if (tokens[i + 1].getType() == Tokenizer.LPAREN) {
					int match = Tokenizer.scanParen(tokens, i + 1, count);
					String exp = scanFunction(sql, tokens, i, match, dbType);
					if (exp != null) {
						sb.append(sql.substring(prevPos, token.getPos()));
						sb.append(exp);
						prevPos = tokens[match].getPos() + 1;
						i = match;
					} else {
						i++;
					}
				}
			} else if (token.isKeyWord("TOP")) {
				// TODO���������ݿ�����ת������Ӧ���﷨
				if (tokens[i + 1].getType() == Tokenizer.NUMBER) {
					if ("ORACLE".endsWith(dbType) || "DB2".endsWith(dbType) || "MYSQL".endsWith(dbType)
							 || "HSQL".endsWith(dbType) || "POSTGRES".endsWith(dbType) || "HIVE".endsWith(dbType)
							 || "IMPALA".endsWith(dbType) || "GREENPLUM".endsWith(dbType)) {
						i++;
						rowNum = tokens[i].getString();
						sb.append(sql.substring(prevPos, token.getPos()));
						
						prevPos = tokens[i].getPos() + rowNum.length();
					} else if ("INFMIX".endsWith(dbType)) {
						i++;
						sb.append(sql.substring(prevPos, token.getPos()));
						sb.append(" FIRST ");
						sb.append(tokens[i].getString());
						
						prevPos = tokens[i].getPos() + tokens[i].getString().length();
					}
				}
			} //else if (token.isKeyWord("BOTTOM")) {
			//}
		}
		
		if (sb.length() == 0) {
			return sql;
		}

		sb.append(sql.substring(prevPos));
		if (rowNum != null) {
			if ("ORACLE".endsWith(dbType)) {
				sb.insert(0, "SELECT * FROM (");
				sb.append(")t WHERE ROWNUM<=");
				sb.append(rowNum);
			} else if ("DB2".endsWith(dbType)) {
				sb.append(" FETCH FIRST ");
				sb.append(rowNum);
				sb.append(" ROWS ONLY");
			} else if ("MYSQL".endsWith(dbType)||"HSQL".endsWith(dbType)||"POSTGRES".endsWith(dbType)
					||"HIVE".endsWith(dbType)||"IMPALA".endsWith(dbType)||"GREENPLUM".endsWith(dbType)) {
				sb.append(" LIMIT ");
				sb.append(rowNum);
			} else {
				throw new RQException();
			}
			
		}
		
		return sb.toString();
	}
	
	// end������
	private static String translate(String sql, Token []tokens, int start, int end, String dbType) {
		StringBuffer sb = new StringBuffer();
		int prevPos = tokens[start].getPos();
		
		for (int i = start; i < end; ++i) {
			Token token = tokens[i];
			if (token.getType() == Tokenizer.IDENT && tokens[i + 1].getType() == Tokenizer.LPAREN) {
				int match = Tokenizer.scanParen(tokens, i + 1, end);
				String exp = scanFunction(sql, tokens, i, match, dbType);
				if (exp != null) {
					sb.append(sql.substring(prevPos, token.getPos()));
					sb.append(exp);
					prevPos = tokens[match].getPos() + 1;
					i = match;
				} else {
					i++;
				}
			}
		}
		
		if (sb.length() == 0) {
			return null;
		} else {
			sb.append(sql.substring(prevPos, tokens[end].getPos()));
			return sb.toString();
		}
	}
	
	// f(), f(p1,...) next��')'��λ��
	private static String scanFunction(String sql, Token []tokens, int start, int next, String dbType) {
		String name = tokens[start].getString();

		start += 2;
		int pcount;
		int commaCount;
		IntArrayList commaList;
		
		if (start < next) {
			commaList = new IntArrayList();
			for (int i = start; i < next;) {
				int p = Tokenizer.scanComma(tokens, i, next);
				if (p > 0) {
					commaList.addInt(p);
					i = p + 1;
				} else {
					break;
				}
			}
	
			commaCount = commaList.size();
			pcount = commaCount + 1;
		} else {
			return null;
		}
		
//		FixedParamFunInfo funInfo = FunInfoManager.getFixedParamFunInfo(name, pcount);
//		if (funInfo == null) {
//			return null;
//		}
//		
//		String exp = funInfo.getInfo(dbType);
//		if (exp == null || exp.length() == 0 || exp.equalsIgnoreCase(FixedParamFunInfo.NONSUPPORT)) {
//			return null;
//		}

		String []params = new String[pcount];
		for (int i = 0; i < commaCount; ++i) {
			int pos = commaList.getInt(i);
			params[i] = translate(sql, tokens, start, pos, dbType);
			if (params[i] == null) {
				params[i] = sql.substring(tokens[start].getPos(), tokens[pos].getPos());
			}
			
			start = pos + 1;
		}
		
		params[commaCount] = translate(sql, tokens, start, next, dbType);
		if (params[commaCount] == null) {
			params[commaCount] = sql.substring(tokens[start].getPos(), tokens[next].getPos());
		}

		return FunInfoManager.getFunctionExp(dbType, name, params);
		//return changeFunction(name, exp, params);
	}
	
	private static String changeFunction(String name, String exp, String []params) {
		int pcount = params.length;
		int len = exp.length();
		StringBuffer sb = new StringBuffer(128 + len);

		for (int i = 0; i < len; ++i) {
			// �����ڵ�Ҳ�滻��
			char ch = exp.charAt(i);
			/*if (ch == '\'' || ch == '\"') { // �ַ���
				int pos = Sentence.scanQuotation(exp, i);
				if (pos == -1) {
					sb.append(exp.substring(i));
					break;
				} else {
					sb.append(exp.substring(i, pos + 1));
					i = pos;
				}
			} else */if (ch == Tokenizer.PARAMMARK) {
				int numIndex = i + 1;
				int next = Tokenizer.scanNumber(exp, numIndex);
				int paramSeq = 1;
				if (next > numIndex) { // ?n
					try {
						String strNum = exp.substring(numIndex, next);
						paramSeq = Integer.parseInt(strNum);
					} catch (NumberFormatException e) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("function.invalidParam"));
					}

					if (paramSeq < 1 || paramSeq > pcount) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("function.invalidParam"));
					}
					
					i = next - 1;
				}

				sb.append(params[paramSeq - 1]);
			} else {
				sb.append(ch);
			}
		}

		return sb.toString();
	}
}