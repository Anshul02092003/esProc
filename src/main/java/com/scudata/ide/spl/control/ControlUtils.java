package com.scudata.ide.spl.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JViewport;

import com.scudata.common.CellLocation;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.control.ControlUtilsBase;

/**
 * �ؼ�������
 *
 */
public class ControlUtils extends ControlUtilsBase {

	/**
	 * ��������ؼ����ض�Ӧ������༭��
	 * 
	 * @param control ����ؼ�
	 * @return
	 */
	public static SplEditor extractSplEditor(SplControl control) {
		if (control.m_editorListener.get(0) instanceof SplControlListener) {
			return ((SplControlListener) control.m_editorListener.get(0)).getEditor();
		}
		return null;
	}

	/**
	 * �ж�ָ����Ԫ���Ƿ�λ����ʾ�����У������ڣ���֮��������ʾ������
	 * 
	 * @param viewport ��ʾ���ڶ���
	 * @param control  ����ؼ�
	 * @param row      ��Ԫ�������к�
	 * @param col      ��Ԫ�������к�
	 * @return ��Ԫ��λ����ʾ������ʱ������false������֮��������ʾ���ں󣬷���true
	 */
	public static boolean scrollToVisible(JViewport viewport, SplControl control, int row, int col) {
		Rectangle fieldArea = new Rectangle();
		fieldArea.x = control.cellX[col];
		CellSetParser parser = new CellSetParser(control.cellSet);
		if (fieldArea.x == 0)
			fieldArea.x = parser.getColsWidth(control, 1, col - 1, false) + 1;
		if (row >= control.cellY.length) {
			fieldArea.y = control.cellY[row - 1];
		} else {
			fieldArea.y = control.cellY[row];
		}
		if (fieldArea.y == 0)
			fieldArea.y = parser.getRowsHeight(control, 1, row - 1, false) + 1;
		if (row == 0) {
			fieldArea.width = control.cellW[col];
			fieldArea.height = 20;
		} else if (col == 0) {
			fieldArea.width = 40;
			fieldArea.height = control.cellH[row];
		} else {
			fieldArea.width = (int) control.cellSet.getColCell(col).getWidth();
			fieldArea.height = (int) control.cellSet.getRowCell(row).getHeight();
		}

		return scrollToVisible(viewport, fieldArea);
	}

	/**
	 * ����������ҵ�Ԫ��λ��
	 * 
	 * @param x     X����
	 * @param y     Y����
	 * @param panel ����������
	 * @return ��Ԫ��λ��
	 */
	public static CellLocation lookupCellPosition(int x, int y, ContentPanel panel) {
		for (int i = 1; i < panel.cellX.length; i++) {
			for (int j = 1; j < panel.cellX[i].length; j++) {
				if (y > panel.cellY[i][j] && y <= panel.cellY[i][j] + panel.cellH[i][j] && x > panel.cellX[i][j]
						&& x <= panel.cellX[i][j] + panel.cellW[i][j]) {
					return new CellLocation(i, (int) j);
				}
			}
		}
		return null;
	}

	/**
	 * �����ı�
	 * 
	 * @param g         ����
	 * @param text      �ı�
	 * @param x         X����
	 * @param y         Y����
	 * @param w         ���
	 * @param h         �߶�
	 * @param underLine �Ƿ����»���
	 * @param halign    ˮƽ����
	 * @param valign    ��ֱ����
	 * @param font      ����
	 * @param c         ǰ��ɫ
	 */
	public static void drawText(Graphics g, String text, int x, int y, int w, int h, boolean underLine, byte halign,
			byte valign, Font font, Color c) {
		ControlUtils.drawText(g, text, x, y, w, h, underLine, halign, valign, font, c,
				ConfigOptions.iIndent.intValue());
	}

}
