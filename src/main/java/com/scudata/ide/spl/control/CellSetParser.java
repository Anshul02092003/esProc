package com.scudata.ide.spl.control;

import java.awt.Color;
import java.awt.Font;

import com.scudata.cellset.datamodel.CellSet;
import com.scudata.cellset.datamodel.ColCell;
import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.cellset.datamodel.RowCell;
import com.scudata.common.StringUtils;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.GC;

/**
 * ���������
 *
 */
public class CellSetParser {
	/**
	 * �ո���
	 */
	private static NormalCell blankCell;

	/**
	 * �������
	 */
	private CellSet cellSet;

	/**
	 * ������������
	 * 
	 * @param cellSet
	 *            �������
	 */
	public CellSetParser(CellSet cellSet) {
		this.cellSet = cellSet;
		blankCell = new PgmNormalCell();
	}

	/**
	 * ȡ�������
	 * 
	 * @return
	 */
	public CellSet getCellSet() {
		return cellSet;
	}

	/**
	 * ȡ��Ԫ�����
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public NormalCell getCell(int row, int col) {
		return getCell(row, col, false);
	}

	/**
	 * ȡ��Ԫ�����
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @param create
	 *            ָ����Ԫ��Ϊnullʱ�Ƿ񴴽�����
	 * @return
	 */
	public NormalCell getCell(int row, int col, boolean create) {
		NormalCell cell = (NormalCell) cellSet.getCell(row, col);
		if (cell == null) {
			if (create) {
				return new PgmNormalCell();
			} else {
				return blankCell;
			}
		}
		return cell;
	}

	/**
	 * ȡ��Ԫ��ļ��
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public int getCellIndent(int row, int col) {
		return ConfigOptions.iIndent.intValue();
	}

	/** ���� */
	public static final byte TYPE_OTHER = 0;
	/** ע�͸���߿� */
	public static final byte TYPE_NOTE = 1;
	/** �����ִ�и���߿� */
	public static final byte TYPE_CALC = 2;

	/**
	 * ȡ��Ԫ������
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public byte getCellType(int row, int col) {
		NormalCell cell = getCell(row, col);
		int type = cell.getType();
		byte tempType = TYPE_OTHER;
		switch (type) {
		case NormalCell.TYPE_NOTE_CELL:
		case NormalCell.TYPE_NOTE_BLOCK:
			return TYPE_NOTE;
		case NormalCell.TYPE_CALCULABLE_CELL:
		case NormalCell.TYPE_CALCULABLE_BLOCK:
		case NormalCell.TYPE_EXECUTABLE_CELL:
		case NormalCell.TYPE_EXECUTABLE_BLOCK:
			tempType = TYPE_CALC;
			break;
		}
		// �ӵ�һ�п�ʼ���ϲ��ң��Ҳ������Ҳ���һֱ��col-1��
		int topRow = 1;
		NormalCell temp;
		for (int c = 1; c < col; c++) {
			for (int r = row; r >= topRow; r--) {
				temp = getCell(r, c);
				switch (temp.getType()) {
				case NormalCell.TYPE_NOTE_BLOCK:
					return TYPE_NOTE;
				case NormalCell.TYPE_CALCULABLE_BLOCK:
				case NormalCell.TYPE_EXECUTABLE_BLOCK:
					tempType = TYPE_CALC;
					topRow = r;
					break;
				default:
					if (StringUtils.isValidString(temp.getExpString())) {
						topRow = r;
						break;
					}
				}
			}
		}
		return tempType;
	}

	/**
	 * �Ƿ��Ӹ�
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	private boolean isSubCell(int row, int col) {
		int colCount = cellSet.getColCount();
		NormalCell nc;
		String expStr;
		for (int r = row; r >= 1; r--) {
			for (int c = colCount; c >= 1; c--) {
				if (r == row && c > col) {
					c = col;
					continue;
				}
				nc = getCell(r, c);
				expStr = nc.getExpString();
				if (!StringUtils.isValidString(expStr)) {
					continue;
				}
				if (!isSubString(expStr)) {
					return false;
				}
				switch (nc.getType()) {
				case NormalCell.TYPE_CALCULABLE_CELL:
				case NormalCell.TYPE_CALCULABLE_BLOCK:
				case NormalCell.TYPE_EXECUTABLE_CELL:
				case NormalCell.TYPE_EXECUTABLE_BLOCK:
					return true;
				case NormalCell.TYPE_CONST_CELL:
					continue;
				default:
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * ���ʽ�Ƿ��Ӹ�
	 * 
	 * @param expStr
	 * @return
	 */
	private boolean isSubString(String expStr) {
		char lastChar = expStr.charAt(expStr.length() - 1);
		if (lastChar != ',' && lastChar != ';' && lastChar != '(') {
			return false;
		}
		return true;
	}

	/**
	 * ȡ��Ԫ�񱳾�ɫ
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public Color getBackColor(int row, int col) {
		return getCellTypeColor(row, col, false);
	}

	/**
	 * ȡ��Ԫ��ǰ��ɫ
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public Color getForeColor(int row, int col) {
		return getCellTypeColor(row, col, true);
	}

	/**
	 * ȡ��Ԫ����ʾ�ı�
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public String getDispText(int row, int col) {
		NormalCell nc = getCell(row, col);
		String text;
		text = nc.getExpString();
		return text;
	}

	/**
	 * ȡ��Ԫ������
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public Font getFont(int row, int col) {
		return GC.font;
	}

	/**
	 * ȡ��Ԫ����������
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public String getFontName(int row, int col) {
		return "Dialog";
	}

	/**
	 * ȡ��Ԫ�������С
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public int getFontSize(int row, int col) {
		return ConfigOptions.iFontSize.intValue();
	}

	/**
	 * ȡ��Ԫ����ʾ��ʽ
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public String getFormat(int row, int col) {
		return null;
	}

	/**
	 * ȡ��Ԫ��ˮƽ����
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public byte getHAlign(int row, int col) {
		return ConfigOptions.iHAlign.byteValue();
	}

	/**
	 * ȡ��ֱ����
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public byte getVAlign(int row, int col) {
		return ConfigOptions.iVAlign.byteValue();
	}

	/**
	 * ��Ԫ���Ƿ�Ӵ�
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public boolean isBold(int row, int col) {
		return ConfigOptions.bBold.booleanValue();
	}

	/**
	 * ��Ԫ���Ƿ�б��
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public boolean isItalic(int row, int col) {
		return ConfigOptions.bItalic.booleanValue();
	}

	/**
	 * ��Ԫ���Ƿ��»���
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @return
	 */
	public boolean isUnderline(int row, int col) {
		return ConfigOptions.bUnderline.booleanValue();
	}

	/**
	 * ȡ��Ԫ�����Ͷ�Ӧ����ɫ
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 * @param isGetForeground
	 *            �Ƿ�ȡǰ��ɫ��trueǰ��ɫ��false����ɫ
	 * @return
	 */
	private Color getCellTypeColor(int row, int col, boolean isGetForeground) {
		byte type = getCellType(row, col);
		NormalCell cell = getCell(row, col);
		if (type == TYPE_NOTE) { // ע�͸������ע�Ϳ���
			return isGetForeground ? ConfigOptions.iNoteFColor
					: ConfigOptions.iNoteBColor;
		}
		if (cell.getType() == NormalCell.TYPE_CONST_CELL) { // ������
			if (isSubCell(row, col)) {
				return isGetForeground ? ConfigOptions.iNValueFColor
						: ConfigOptions.iNValueBColor;
			}
			if (type != TYPE_CALC) { // �����ִ�и��ڼ����ִ�п���,�����ʽ����ʾ ���Ҳ�������
				return isGetForeground ? ConfigOptions.iConstFColor
						: ConfigOptions.iConstBColor;
			}
		}
		if (cell.getValue() == null) { // ��ֵ���ʽ
			return isGetForeground ? ConfigOptions.iNValueFColor
					: ConfigOptions.iNValueBColor;
		} else { // ��ֵ���ʽ
			return isGetForeground ? ConfigOptions.iValueFColor
					: ConfigOptions.iValueBColor;
		}
	}

	/**
	 * ȡ����
	 * 
	 * @return
	 */
	public int getRowCount() {
		return cellSet.getRowCount();
	}

	/**
	 * ȡ�и�
	 * 
	 * @param row
	 *            �к�
	 * @param scale
	 *            ��ʾ����
	 * @return
	 */
	public int getRowHeight(int row, float scale) {
		RowCell cell = (RowCell) cellSet.getRowCell(row);
		float height = cell.getHeight();
		int h = (int) Math.ceil(height * scale);
		if (scale != 1.0) {
			h += 1;
		}
		return h;
	}

	/**
	 * ���������
	 * 
	 * @return ������
	 */
	public int getColCount() {
		return cellSet.getColCount();
	}

	/**
	 * ȡ�п�
	 * 
	 * @param col
	 *            �к�
	 * @param scale
	 *            ��ʾ����
	 * @return
	 */
	public int getColWidth(int col, float scale) {
		ColCell cell = (ColCell) cellSet.getColCell(col);
		float width = cell.getWidth();
		int w = (int) Math.ceil(width * scale);
		if (scale != 1.0) {
			w += 1;
		}
		return w;
	}

	/**
	 * ȡ�п��������з���0
	 * 
	 * @param col
	 *            �к�
	 * @return
	 */
	public int getColWidth(int col) {
		if (!isColVisible(col)) {
			return 0;
		}
		ColCell cell = (ColCell) cellSet.getColCell(col);
		float width = cell.getWidth();
		int scale = 1;
		int w = (int) Math.ceil(width * scale);
		if (scale != 1.0) {
			w += 1;
		}
		return w;
	}

	/**
	 * ���Ƿ����
	 * 
	 * @param row
	 *            �к�
	 * @return
	 */
	public boolean isRowVisible(int row) {
		RowCell rc = (RowCell) cellSet.getRowCell(row);
		return rc.getVisible() != RowCell.VISIBLE_ALWAYSNOT;
	}

	/**
	 * ���Ƿ����
	 * 
	 * @param col
	 *            �к�
	 * @return
	 */
	public boolean isColVisible(int col) {
		ColCell cc = (ColCell) cellSet.getColCell(col);
		return cc.getVisible() != ColCell.VISIBLE_ALWAYSNOT;
	}

	/**
	 * ȡ�иߣ��������з���0
	 * 
	 * @param row
	 * @return
	 */
	public int getRowHeight(int row) {
		if (isRowVisible(row)) {
			return (int) cellSet.getRowCell(row).getHeight();
		} else {
			return 0;
		}
	}

	/**
	 * ȡ���п��
	 * 
	 * @param control
	 *            ����ؼ�
	 * @param startCol
	 *            ��ʼ��
	 * @param count
	 *            ����
	 * @param includeHideCol
	 *            �Ƿ���������п�
	 * @return
	 */
	public int getColsWidth(SplControl control, int startCol, int count,
			boolean includeHideCol) {
		int width = 0;
		for (int i = 0, col = startCol; i < count; i++, col++) {
			if (includeHideCol) {
				width += getColWidth(col, control.scale);
			} else {
				width += getColWidth(col);
			}
		}
		return width;
	}

	/**
	 * ȡ���и߶�
	 * 
	 * @param control
	 *            ����ؼ�
	 * @param startRow
	 *            ��ʼ��
	 * @param count
	 *            ����
	 * @param includeHideRow
	 *            �Ƿ���������и�
	 * @return
	 */
	public int getRowsHeight(SplControl control, int startRow, int count,
			boolean includeHideRow) {
		int height = 0;
		for (int i = 0, row = startRow; i < count; i++, row++) {
			if (includeHideRow) {
				height += getRowHeight(row, control.scale);
			} else {
				height += getRowHeight(row);
			}
		}
		return height;
	}

	/**
	 * ȡ���еĽ�����
	 * 
	 * @param row
	 * @return
	 */
	public int getSubEnd(int row) {
		PgmNormalCell cell;
		int subEnd = -1;
		for (int c = 1, cc = cellSet.getColCount(); c <= cc; c++) {
			cell = (PgmNormalCell) cellSet.getCell(row, c);
			switch (cell.getType()) {
			case PgmNormalCell.TYPE_CALCULABLE_BLOCK:
			case PgmNormalCell.TYPE_EXECUTABLE_BLOCK:
			case PgmNormalCell.TYPE_NOTE_BLOCK:
			case PgmNormalCell.TYPE_COMMAND_CELL:
				subEnd = ((PgmCellSet) cellSet).getCodeBlockEndRow(row, c);
				if (subEnd > row) {
					return subEnd;
				}
				break;
			}
		}
		return subEnd;
	}

	/**
	 * �����Ƿ����
	 * 
	 * @param row
	 *            �к�
	 * @param subEnd
	 *            ����ĩ��
	 * @return
	 */
	public boolean isSubExpand(int row, int subEnd) {
		if (subEnd <= row)
			return false;
		RowCell rc = (RowCell) cellSet.getRowCell(row + 1);
		return rc.getVisible() != RowCell.VISIBLE_ALWAYSNOT;
	}
}
