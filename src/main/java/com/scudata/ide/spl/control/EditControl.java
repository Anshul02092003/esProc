package com.scudata.ide.spl.control;

import java.awt.dnd.DropTarget;

import javax.swing.JPanel;

import com.scudata.cellset.datamodel.CellSet;

/**
 * ����ؼ�
 *
 */
public class EditControl extends SplControl {

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
		ContentPanel panel = newContentPanel(cellSet);
		CellSelectListener listener = new CellSelectListener(this, panel,
				editable);
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);
		panel.addKeyListener(listener);
		DropTarget target = new DropTarget(panel, new EditDropListener());
		panel.setDropTarget(target);
		panel.setFocusTraversalKeysEnabled(false);

		return panel;
	}

	/**
	 * ����SPL�������
	 * @param cellSet
	 * @return ContentPanel
	 */
	protected ContentPanel newContentPanel(CellSet cellSet) {
		return new ContentPanel(cellSet, 1, cellSet.getRowCount(), 1,
				cellSet.getColCount(), true, true, this);
	}

	/**
	 * �ύ�ı��༭
	 */
	public void acceptText() {
		this.contentView.submitEditor();
	}

}
