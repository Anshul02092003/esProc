package com.scudata.ide.spl.base;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import com.scudata.common.MessageManager;
import com.scudata.common.StringUtils;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.Param;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.control.TransferableObject;
import com.scudata.ide.common.resources.IdeCommonMessage;
import com.scudata.ide.common.swing.AllPurposeEditor;
import com.scudata.ide.common.swing.AllPurposeRenderer;
import com.scudata.ide.common.swing.JTableEx;

/**
 * ����ռ��ؼ�
 */
public abstract class JTableJobSpace extends JScrollPane {
	private static final long serialVersionUID = 1L;

	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/** ����� */
	private final byte COL_INDEX = 0;
	/** ����ռ�ID */
	private final byte COL_SPACE = 1;
	/** ������ */
	private final byte COL_NAME = 2;
	/** ����ֵ */
	private final byte COL_VALUE = 3;
	/** �������������� */
	private final byte COL_VAR = 4;

	/** ���������б�ǩ�������� */
	private final String TITLE_VAR = "TITLE_VAR";

	/**
	 * ������ؼ��� ���,�ռ���,������,����ֵ,TITLE_VAR
	 */
	private JTableEx tableVar = new JTableEx(
			mm.getMessage("jtablejobspace.tablenames") + "," + TITLE_VAR) {
		private static final long serialVersionUID = 1L;

		public void rowfocusChanged(int oldRow, int newRow) {
			if (preventChange) {
				return;
			}
			if (newRow != -1) {
				select(data.getValueAt(newRow, COL_VALUE),
						data.getValueAt(newRow, COL_NAME) == null ? ""
								: (String) data.getValueAt(newRow, COL_NAME));
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (!isItemDataChanged(row, col, value)) {
				return;
			}
			super.setValueAt(value, row, col);
			if (preventChange) {
				return;
			}
			Param p = (Param) data.getValueAt(row, COL_VAR);
			if (col == COL_NAME) {
				p.setName(value == null ? null : (String) value);
			} else if (col == COL_VALUE) {
				p.setValue(value);
			}
		}

		public void mousePressed(MouseEvent e) {
			if (e == null) {
				return;
			}
			Point p = e.getPoint();
			if (p == null) {
				return;
			}
			int row = rowAtPoint(p);
			if (row != -1) {
				select(data.getValueAt(row, COL_VALUE),
						data.getValueAt(row, COL_NAME) == null ? ""
								: (String) data.getValueAt(row, COL_NAME));
			}
		}

		public void doubleClicked(int xpos, int ypos, int row, int col,
				MouseEvent e) {
			if (row != -1) {
				select(data.getValueAt(row, COL_VALUE),
						data.getValueAt(row, COL_NAME) == null ? ""
								: (String) data.getValueAt(row, COL_NAME));
			}
		}
	};

	/**
	 * �Ƿ���ֹ�仯
	 */
	private boolean preventChange = false;

	/**
	 * ���캯��
	 */
	public JTableJobSpace() {
		this.setMinimumSize(new Dimension(0, 0));
		init();
	}

	/**
	 * ѡ���˲���
	 * 
	 * @param val
	 * @param varName
	 */
	public abstract void select(Object val, String varName);

	/**
	 * ��������ռ�
	 */
	public synchronized void resetJobSpaces() {
		tableVar.acceptText();
		tableVar.removeAllRows();
		preventChange = true;
		HashMap hm = JobSpaceManager.listSpaceParams();
		Iterator it = hm.keySet().iterator();
		while (it.hasNext()) {
			String jsId = (String) it.next();
			Param[] paras = (Param[]) hm.get(jsId);
			addJobSpaceRow(jsId, paras);
		}
		preventChange = false;
	}

	/**
	 * ��������ռ䵽�����
	 * 
	 * @param id
	 * @param params
	 */
	private void addJobSpaceRow(String id, Param[] params) {
		for (int j = 0; j < params.length; j++) {
			int row = tableVar.addRow();
			tableVar.data.setValueAt(id, row, COL_SPACE);
			tableVar.data.setValueAt(params[j].getName(), row, COL_NAME);
			tableVar.data.setValueAt(params[j].getValue(), row, COL_VALUE);
		}
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		this.getViewport().add(tableVar);

		tableVar.setIndexCol(COL_INDEX);
		tableVar.setRowHeight(20);

		TableColumn tc = tableVar.getColumn(COL_VALUE);
		tc.setCellEditor(new AllPurposeEditor(new JTextField(), tableVar));
		tc.setCellRenderer(new AllPurposeRenderer());

		DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent dge) {
				try {
					int row = tableVar.getSelectedRow();
					if (!StringUtils.isValidString(tableVar.data.getValueAt(
							row, COL_NAME))) {
						return;
					}
					String name = (String) tableVar.data.getValueAt(row,
							COL_NAME);
					Object data = null;
					if (dge.getTriggerEvent().isControlDown()) {
						data = name;
					} else {
						data = "=" + name;
					}
					Transferable tf = new TransferableObject(data);
					if (tf != null) {
						dge.startDrag(GM.getDndCursor(), tf);
					}
				} catch (Exception x) {
					GM.showException(x);
				}
			}
		};
		DragSource ds = DragSource.getDefaultDragSource();
		ds.createDefaultDragGestureRecognizer(tableVar,
				DnDConstants.ACTION_COPY, dgl);
		tableVar.setColumnVisible(TITLE_VAR, false);
	}
}
