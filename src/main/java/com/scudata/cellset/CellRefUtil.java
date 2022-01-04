package com.scudata.cellset;

import com.scudata.common.CellLocation;
import com.scudata.common.Sentence;
import com.scudata.dm.KeyWord;

public final class CellRefUtil {
	// ����ĵ�Ԫ�����ñ�ʶ
	public static final String ERRORREF = "#REF!";

	/**
	 * �ж��ַ��Ƿ����з���
	 * @param c �ַ�
	 * @return true���ǣ�false������
	 */
	public static boolean isRowChar(char c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * �ж��ַ��Ƿ����з���
	 * @param c �ַ�
	 * @return true���ǣ�false������
	 */
	public static boolean isColChar(char c) {
		return c >= 'A' && c <= 'Z';
	}

	/**
	 * �ж��ַ���ָ��λ�õ�ǰһ���ַ��Ƿ���'.'
	 * @param str ���ʽ�ַ���
	 * @param pos λ��
	 * @return true���ǣ�false������
	 */
	public static boolean isPrevDot(String str, int pos) {
		for (pos = pos - 1; pos >= 0; --pos) {
			char c = str.charAt(pos);
			if (c == '.') return true;
			if (!Character.isWhitespace(c)) return false;
		}

		return false;
	}

	/**
	 * �ı������õĵ�Ԫ����к�
	 * @param cellRow Դ�к�
	 * @param rowBase �ڴ��н������ӻ���ɾ����
	 * @param rowIncrement ���ӻ���ɾ��������
	 * @param oldRowCount ԭ����������
	 * @return �任�������������null��ʾ���ø�ɾ��
	 */
	public static String changeRow(int cellRow, int rowBase, int rowIncrement, int oldRowCount) {
		if (rowBase != -1) {
			if (rowIncrement < 0) {
				//�����б�ɾ����������ʽ
				if (cellRow >= rowBase && cellRow <= (rowBase - rowIncrement - 1)) {
					return null;
				}
			}

			if (cellRow >= rowBase) {
				cellRow += rowIncrement;
			}
		}
		return CellLocation.toRow(cellRow);
	}

	/**
	 * �ı������õĵ�Ԫ����к�
	 * @param cellCol Դ�к�
	 * @param colBase �ڴ��н������ӻ���ɾ����
	 * @param colIncrement ���ӻ���ɾ��������
	 * @param oldColCount ԭ����������
	 * @return �任�������������null��ʾ���ø�ɾ��
	 */
	public static String changeCol(int cellCol, int colBase, int colIncrement, int oldColCount) {
		if (colBase != -1) {
			if (colIncrement < 0) {
				//�����б�ɾ����������ʽ
				if (cellCol >= colBase && cellCol <= (colBase - colIncrement - 1)) {
					return null;
				}
			}

			if (cellCol >= colBase) {
				cellCol += colIncrement;
			}
		}
		return CellLocation.toCol(cellCol);
	}

	/**
	 * �ı������õĵ�Ԫ����кţ����ڴ�һ�����Ʊ��ʽ����һ���񣬱��ʽ�����õĸ�Ҫ����Դ��Ŀ�������λ��
	 * @param cellRow Դ�к�
	 * @param rowIncrement ���ӵ����������ı�ʾɾ��������
	 * @param rowCount ������
	 * @return �任�������������null��ʾ�����
	 */
	public static String changeRow(int cellRow, int rowIncrement, int rowCount) {
		cellRow += rowIncrement;
		if (cellRow <= 0 || cellRow > rowCount) return null;

		return CellLocation.toRow(cellRow);
	}

	/**
	 * �ı������õĵ�Ԫ����кţ����ڴ�һ�����Ʊ��ʽ����һ���񣬱��ʽ�����õĸ�Ҫ����Դ��Ŀ�������λ��
	 * @param cellCol Դ�к�
	 * @param colIncrement ���ӵ����������ı�ʾɾ��������
	 * @param colCount ������
	 * @return �任�������������null��ʾ�����
	 */
	public static String changeCol(int cellCol, int colIncrement, int colCount) {
		if (cellCol <= 0 || cellCol > colCount) return null;

		cellCol += colIncrement;
		if (cellCol <= 0 || cellCol > colCount) return null;

		return CellLocation.toCol(cellCol);
	}

	/**
	 * ���롢ɾ������ʱ�Ա��ʽ�еĵ�Ԫ�����ý��б�Ǩ
	 * @param str ���ʽ
	 * @param rowBase �������ڵ���
	 * @param rowIncrement �������ɾ��������
	 * @param colBase �������ڵ���
	 * @param colIncrement �������ɾ��������
	 * @param oldRowCount ԭ����������
	 * @param oldColCount ԭ����������
	 * @param error ������õĵ�Ԫ��ɾ������error[0]Ϊtrue
	 * @return ��Ǩ��ı��ʽ
	 */
	public static String relativeRegulateString(String str, int rowBase, int rowIncrement,
										  int colBase, int colIncrement,
										  int oldRowCount, int oldColCount, boolean []error) {
		error[0] = false;
		if (str == null || str.length() == 0 || str.startsWith(ERRORREF)) {
			//��������ĵ�Ԫ�񲻴���
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();
		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = KeyWord.scanId(str, idx + 1);
				if (last - idx < 2 || (!isColChar(ch) && ch != '$') || isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����
				
				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));
						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = changeCol(lct.getCol(), colBase,
								colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							String strRow = changeRow(lct.getRow(), rowBase,
								rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(strCol);
							strNew.append(strRow);
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == -1 || row == -1 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = changeCol(col, colBase, colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							String strRow = changeRow(row, rowBase, rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(strCol);
							strNew.append('$');
							strNew.append(strRow);
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = changeCol(lct.getCol(), colBase,
								colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							String strRow = changeRow(lct.getRow(), rowBase,
								rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append(strRow);
						}
					} else { // A$2  A$2@cs
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == -1 || row == -1 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = changeCol(col, colBase, colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							String strRow = changeRow(row, rowBase, rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append('$');
							strNew.append(strRow);
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	/**
	 * ��ɾ��ʱ������Ԫ������
	 * @param r ��Ԫ����к�
	 * @param rows �����ɾ�����к�
	 * @param isInsert true�����룬false��ɾ��
	 * @return ��������кţ�����-1��ʾ���õ��б�ɾ��
	 */
	public static int adjustRowReference(int r, int []rows, boolean isInsert) {
		int count = rows.length;
		for (int i = 0; i < count; ++i) {
			if (rows[i] > r) {
				return isInsert ? r + i : r - i;
			} else if (rows[i] == r) {
				return isInsert ? r + i + 1 : -1;
			}
		}

		return isInsert ? r + count : r - count;
	}

	/**
	 * �ѱ��ʽ�ж�lct1�����ø�Ϊlct2�����ڰ�һ������е���һ���񣬸ı��Դ������õ�Ŀ���
	 * @param str ���ʽ
	 * @param lct1 ԭ�����õĵ�Ԫ��
	 * @param lct2 Ŀ�굥Ԫ��
	 * @return �任��ı��ʽ
	 */
	public static String exchangeCellString(String str, CellLocation lct1, CellLocation lct2) {
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();
		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = KeyWord.scanId(str, idx + 1);
				if (last - idx < 2 || (!isColChar(ch) && ch != '$') || isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));

						if (lct1.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(lct2.toString());
						} else if (lct2.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(lct1.toString());
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));

						if (col == lct1.getCol() && row == lct1.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(CellLocation.toCol(lct2.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(lct2.getRow()));
						} else if (col == lct2.getCol() && row == lct2.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(CellLocation.toCol(lct1.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(lct1.getRow()));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct1.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(lct2.toString());
						} else if (lct2.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(lct1.toString());
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // A$2
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == lct1.getCol() && row == lct1.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(CellLocation.toCol(lct2.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(lct2.getRow()));
						} else if (col == lct2.getCol() && row == lct2.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(CellLocation.toCol(lct1.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(lct1.getRow()));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}
}
