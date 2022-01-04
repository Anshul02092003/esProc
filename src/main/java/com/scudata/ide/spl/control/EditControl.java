package com.scudata.ide.spl.control;

import java.awt.dnd.DropTarget;

import javax.swing.JPanel;

import com.scudata.common.Area;
import com.scudata.common.CellLocation;

/**
 * ����ؼ�
 *
 */
public abstract class EditControl extends SplControl {

	private static final long serialVersionUID = 1L;

	/**
	 * �����Ƿ���Ա༭
	 */
	private boolean editable = true;

	/**
	 * ���캯��
	 *
	 * @param rows int
	 * @param cols int
	 */
	public EditControl(int rows, int cols) {
		super(rows, cols);
	}

	/**
	 * ���ɽǲ����
	 *
	 * @return �ǲ����
	 */
	JPanel createCorner() {
		JPanel panel = new CornerPanel(this, editable);
		CornerListener listener = new CornerListener(this, editable);
		panel.addMouseListener(listener);
		return panel;
	}

	/**
	 * �������׸����
	 *
	 * @return ���׸����
	 */
	JPanel createColHeaderView() {
		headerPanel = new ColHeaderPanel(this, editable);
		ColHeaderListener listener = new ColHeaderListener(this, editable);
		headerPanel.addMouseListener(listener);
		headerPanel.addMouseMotionListener(listener);
		headerPanel.addKeyListener(listener);
		return headerPanel;
	}

	/**
	 * �������׸����
	 *
	 * @return ���׸����
	 */
	JPanel createRowHeaderView() {
		JPanel panel = new RowHeaderPanel(this, editable);
		RowHeaderListener listener = new RowHeaderListener(this, editable);
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);
		panel.addKeyListener(listener);
		return panel;
	}

	/**
	 * �����������
	 *
	 * @return �������
	 */
	ContentPanel createContentView() {
		ContentPanel panel = new ContentPanel(cellSet, 1, cellSet.getRowCount(), 1, cellSet.getColCount(), true, true,
				this);
		CellSelectListener listener = new CellSelectListener(this, panel, editable);
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);
		panel.addKeyListener(listener);
		DropTarget target = new DropTarget(panel, new EditDropListener());
		panel.setDropTarget(target);
		panel.setFocusTraversalKeysEnabled(false);

		return panel;
	}

	/**
	 * �ύ�ı��༭
	 */
	public void acceptText() {
		this.contentView.submitEditor();
	}

	/**
	 * ��������ƥ�䵽�ĸ���
	 * 
	 * @param row                   �к�
	 * @param col                   �к�
	 * @param searchInSelectedCells �Ƿ���ѡ��������������
	 */
	public void setSearchedCell(int row, int col, boolean searchInSelectedCells) {
		setActiveCell(new CellLocation(row, col));
		ControlUtils.scrollToVisible(this.getViewport(), this, row, col);
		if (!searchInSelectedCells) {
			setSelectedArea(new Area(row, col, row, col));
			this.fireRegionSelect(true);
		}
		this.repaint();
	}

}
