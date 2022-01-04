package com.scudata.ide.common.control;

import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.StringUtils;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.resources.IdeCommonMessage;
import com.scudata.ide.common.swing.JTableEx;

/**
 * �����б༭���
 *
 */
public class PanelSeries extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/**
	 * �����
	 */
	private final int COL_INDEX = 0;
	/**
	 * ֵ��
	 */
	private final int COL_VALUE = 1;
	/**
	 * ������ֵ�ı����
	 */
	private JTableEx tableParam = new JTableEx(
			mm.getMessage("panelseries.tableparam")) { // ���,ֵ
		private static final long serialVersionUID = 1L;

		/**
		 * ˫�������ı��Ի���༭ֵ
		 */
		public void doubleClicked(int xpos, int ypos, int row, int col,
				MouseEvent e) {
			GM.dialogEditTableText(tableParam, row, col);
		}

		/**
		 * ֵ�ύʱת������Ӧ�Ķ���
		 */
		public void setValueAt(Object aValue, int row, int column) {
			if (!isItemDataChanged(row, column, aValue)) {
				return;
			}
			super.setValueAt(aValue, row, column);
			if (preventChange) {
				return;
			}
			try {
				if (StringUtils.isValidString(aValue)) {
					aValue = PgmNormalCell.parseConstValue((String) aValue);
				}
				series.set(row + 1, aValue);
			} catch (Exception e) {
				GM.showException(e);
				return;
			}
		}
	};

	/**
	 * �����ж���
	 */
	private Sequence series;

	/**
	 * ��������
	 */
	private Param param;
	/**
	 * �Ƿ���ֹ�仯
	 */
	private boolean preventChange = false;

	/**
	 * ���캯��
	 */
	public PanelSeries() {
		try {
			rqInit();
			initTable();
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void rqInit() throws Exception {
		this.setLayout(new GridBagLayout());
		this.add(new JScrollPane(tableParam), GM.getGBC(0, 0, true, true));
	}

	/**
	 * ���ó�������
	 * 
	 * @param param
	 */
	public void setParam(Param param) {
		this.param = param;
		if (param.getValue() == null) {
			tableParam.data.setRowCount(0);
			this.series = new Sequence();
		} else {
			this.series = (Sequence) param.getValue();
			preventChange = true;
			refresh();
			preventChange = false;
		}
	}

	/**
	 * ȡ��������
	 * 
	 * @return
	 */
	public Param getParam() {
		param.setValue(series);
		return param;
	}

	/**
	 * ˢ��
	 */
	private void refresh() {
		tableParam.removeAllRows();
		tableParam.data.setRowCount(0);
		int rowCount = series.length();
		if (rowCount < 1) {
			return;
		}
		for (int i = 1; i <= rowCount; i++) {
			int r = tableParam.addRow();
			Object value = series.get(i);
			tableParam.data.setValueAt(value, r, COL_VALUE);
		}
	}

	/**
	 * ȫѡ
	 */
	public void selectAll() {
		tableParam.acceptText();
		tableParam.selectAll();
	}

	/**
	 * ������
	 */
	public void rowUp() {
		tableParam.acceptText();
		int row = tableParam.getSelectedRow();
		if (row < 0) {
			return;
		}
		tableParam.shiftRowUp(row);
	}

	/**
	 * ������
	 */
	public void rowDown() {
		tableParam.acceptText();
		int row = tableParam.getSelectedRow();
		if (row < 0) {
			return;
		}
		tableParam.shiftRowDown(row);
	}

	/**
	 * ������
	 */
	public void addRow() {
		tableParam.acceptText();
		series.add(null);
		refresh();
	}

	/**
	 * ������
	 */
	public void insertRow() {
		tableParam.acceptText();
		int row = tableParam.getSelectedRow();
		if (row < 0) {
			return;
		}
		series.insert(row + 1, null);
		refresh();
		tableParam.setRowSelectionInterval(row, row);
	}

	/**
	 * �������
	 * 
	 * @return
	 */
	public boolean checkData() {
		tableParam.acceptText();
		return true;
	}

	/**
	 * �����ݸ��Ƶ�������
	 */
	public void clipBoard() {
		String blockData = tableParam.getBlockData();
		GM.clipBoard(blockData);
	}

	/**
	 * ɾ��ѡ�е���
	 */
	public void deleteRows() {
		tableParam.acceptText();
		int rows[] = tableParam.getSelectedRows();
		if (rows.length == 0) {
			return;
		}
		for (int i = rows.length - 1; i >= 0; i--) {
			series.delete(rows[i] + 1);
		}
		refresh();
	}

	/**
	 * ��ʼ����ؼ�
	 */
	private void initTable() {
		preventChange = true;
		tableParam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableParam.setRowHeight(20);
		tableParam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableParam.getTableHeader().setReorderingAllowed(false);
		tableParam.setClickCountToStart(1);
		tableParam.setIndexCol(COL_INDEX);
		tableParam.setColumnWidth(COL_VALUE, 250);
		preventChange = false;
	}
}
