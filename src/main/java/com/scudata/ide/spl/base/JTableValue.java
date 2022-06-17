package com.scudata.ide.spl.base;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.common.DBConfig;
import com.scudata.common.DBInfo;
import com.scudata.common.IntArrayList;
import com.scudata.common.Matrix;
import com.scudata.common.MessageManager;
import com.scudata.common.StringUtils;
import com.scudata.dm.Canvas;
import com.scudata.dm.DBObject;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.IMultipath;
import com.scudata.ide.common.AppendDataThread;
import com.scudata.ide.common.DBTypeEx;
import com.scudata.ide.common.EditListener;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.control.CellRect;
import com.scudata.ide.common.control.CellSelection;
import com.scudata.ide.common.control.TransferableObject;
import com.scudata.ide.common.dialog.DialogCellFormat;
import com.scudata.ide.common.swing.AllPurposeEditor;
import com.scudata.ide.common.swing.AllPurposeRenderer;
import com.scudata.ide.common.swing.JTableEx;
import com.scudata.ide.common.swing.JTextFieldReadOnly;
import com.scudata.ide.spl.GMSpl;
import com.scudata.ide.spl.dialog.DialogDisplayChart;
import com.scudata.ide.spl.dialog.DialogTextEditor;
import com.scudata.ide.spl.resources.IdeSplMessage;
import com.scudata.util.Variant;

/**
 * ֵ���
 *
 */
public class JTableValue extends JTableEx {
	private static final long serialVersionUID = -4530154524747498116L;

	private PanelValue panelValue;
	/**
	 * ��������Դ������
	 */
	private static MessageManager mm = IdeSplMessage.get();
	/**
	 * �������
	 */
	private final String TITLE_INDEX = mm.getMessage("public.index");
	/**
	 * ��Ա����
	 */
	private final String TITLE_SERIES = mm.getMessage("jtablevalue.menber");

	/**
	 * ��һ��
	 */
	private final int COL_FIRST = 0;

	/** ȱʡ */
	private final byte TYPE_DEFAULT = 0;
	/** ��� */
	private final byte TYPE_TABLE = 1;
	/** ���� */
	private final byte TYPE_SERIES = 2;
	/** ��¼ */
	private final byte TYPE_RECORD = 3;
	/** ������ */
	private final byte TYPE_PMT = 4;
	/** ���У���ʱ���ã�������ͨ���н�����ʾ */
	private final byte TYPE_SERIESPMT = 5;
	/** DBInfo���� */
	private final byte TYPE_DB = 6;
	/** FileObject���� */
	private final byte TYPE_FILE = 7;

	/**
	 * ֵ����
	 */
	private byte m_type = TYPE_DEFAULT;

	/**
	 * ֵ��ԭʼֵ
	 */
	private Object value, originalValue;

	/**
	 * �������ڻ�ͼʱ�õ�
	 */
	private Canvas canvas;

	/**
	 * �Ƿ�ɱ༭
	 */
	private boolean editable;

	/**
	 * ����������ȡ�ͷ��صĶ�ջ
	 */
	private Stack<Object> undo = new Stack<Object>();
	private Stack<Object> redo = new Stack<Object>();

	/**
	 * �Ƿ�̶���ʾĳ����Ԫ��ֵ�������Ź��仯
	 */
	private boolean isLocked = false;
	/**
	 * ���������Ƿ���Ϊ����ģʽ���ġ�����ȡ���˼����ʽ����ʱû����
	 */
	private boolean isLocked1 = false;

	/**
	 * ��Ԫ������
	 */
	private String cellId;
	/** ����ֵ */
	public static final short iCOPY = 11;
	/** �������� */
	public static final short iCOPY_COLNAMES = 12;
	/** ճ�� */
	private final short iPASTE = 13;
	/** �����и�ʽ */
	private final short iFORMAT = 17;

	/**
	 * �и�
	 */
	private final int ROW_HEIGHT = 20;

	/**
	 * ����
	 */
	private int rowCount = 0;

	/**
	 * �༭������
	 */
	private EditListener editListener = null;

	/**
	 * ѡ����к�
	 */
	private IntArrayList selectedRows = new IntArrayList();

	/**
	 * ֮ǰѡ����к�
	 */
	private int lastRow = -1;

	/**
	 * ���캯��
	 */
	public JTableValue(PanelValue panelValue) {
		this.panelValue = panelValue;
		DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent dge) {
				try {
					Transferable tf = new TransferableObject(value);
					if (tf != null) {
						dge.startDrag(GM.getDndCursor(), tf);
					}
				} catch (Exception x) {
				}
			}
		};
		DragSource ds = DragSource.getDefaultDragSource();
		ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
				dgl);

		DropTargetListener dtl = new DropTargetListener() {
			public void dragEnter(DropTargetDragEvent dtde) {
				acceptText();
			}

			public void dragOver(DropTargetDragEvent dtde) {
				if (!editable) {
					return;
				}
				Point p = dtde.getLocation();
				int row = rowAtPoint(p);
				int col = columnAtPoint(p);
				if (row < 0 || col < 0) {
					return;
				}
				setRowSelectionInterval(row, row);
				setColumnSelectionInterval(col, col);
			}

			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			public void dragExit(DropTargetEvent dte) {
			}

			public void drop(DropTargetDropEvent dtde) {
				if (!editable) {
					return;
				}
				Point p = dtde.getLocation();
				int row = rowAtPoint(p);
				int col = columnAtPoint(p);
				if (row < 0 || col < 0) {
					return;
				}
				if (!isCellEditable(row, col)) {
					return;
				}
				Object value = null;
				try {
					value = dtde.getTransferable().getTransferData(
							TransferableObject.objectFlavor);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (value == null) {
					return;
				}
				setValueAt(value, row, col);
			}
		};
		DropTarget dt = new DropTarget(this, dtl);
		setDropTarget(dt);
		setRowHeight(ROW_HEIGHT);
		addMWListener(this);

		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int row = lastRow;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					row--;
					if (row < 0)
						row = 0;
					rowSelected(e, row);
					break;
				case KeyEvent.VK_DOWN:
					row++;
					if (row > rowCount - 1)
						row = rowCount - 1;
					rowSelected(e, row);
					break;
				case KeyEvent.VK_PAGE_UP:
					row -= getPageRows();
					if (row < 0)
						row = 0;
					rowSelected(e, row);
					break;
				case KeyEvent.VK_PAGE_DOWN:
					row += getPageRows();
					if (row > rowCount - 1)
						row = rowCount - 1;
					rowSelected(e, row);
					break;
				case KeyEvent.VK_A:
					if (e.isControlDown()) {
						selectedRows.clear();
						for (int i = 0; i < rowCount; i++)
							selectedRows.addInt(i);
						resetSelection();
						lastRow = rowCount - 1;
					}
					break;
				case KeyEvent.VK_C:
					if (e.isControlDown()) {
						copyValue();
					}
					e.consume();
					break;
				}
			}
		});
	}

	/**
	 * ���ñ༭������
	 * 
	 * @param el
	 */
	public void setEditListener(EditListener el) {
		this.editListener = el;
	}

	/**
	 * ȡÿҳ��ʾ������
	 * 
	 * @return
	 */
	private int getPageRows() {
		int height = panelValue.spValue.getPreferredSize().height;
		return height / ROW_HEIGHT + 1;
	}

	/**
	 * ѡ������
	 * 
	 * @param e
	 * @param row
	 */
	private void rowSelected(InputEvent e, int row) {
		if (e.isControlDown()) {
			if (selectedRows.containsInt(row))
				selectedRows.removeInt(row);
			else
				selectedRows.addInt(row);
			resetSelection();
			lastRow = row;
		} else if (e.isShiftDown()) {
			selectedRows.clear();
			int min = Math.min(row, lastRow);
			int max = Math.max(row, lastRow);
			for (int i = min; i <= max; i++) {
				selectedRows.addInt(i);
			}
			resetSelection();
		} else {
			selectedRows.clear();
			selectedRows.addInt(row);
			resetSelection();
			lastRow = row;
		}
		if (e instanceof KeyEvent) {
			e.consume();
		}
	}

	/**
	 * ���������ּ���
	 * 
	 * @param com
	 */
	public void addMWListener(JComponent com) {
		com.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int amount = e.getScrollAmount();
					int rotation = e.getWheelRotation();
					if (rotation < 0) {
						amount = -amount;
					}
					JScrollBar sbValue = panelValue.sbValue;
					sbValue.setValue(sbValue.getValue() + amount);
					resetData(sbValue.getValue());
				}
			}
		});
	}

	/**
	 * ���˫��ʱ��ȡ����
	 */
	public void doubleClicked(int xpos, int ypos, int row, int col, MouseEvent e) {
		drillValue(row, col);
	}

	/**
	 * ����Ҽ��˵�
	 */
	public void rightClicked(int xpos, int ypos, final int row, final int col,
			MouseEvent e) {
		JPopupMenu pm = new JPopupMenu();
		JMenuItem mItem;
		int selectedCol = getSelectedColumn();
		if (selectedCol > -1
				&& (m_type == TYPE_TABLE || m_type == TYPE_PMT || m_type == TYPE_SERIESPMT)) {
			mItem = new JMenuItem(mm.getMessage("jtablevalue.editformat")); // �и�ʽ�༭
			mItem.setIcon(GM.getMenuImageIcon("blank"));
			mItem.setName(String.valueOf(iFORMAT));
			mItem.addActionListener(popAction);
			pm.add(mItem);
		}
		mItem = new JMenuItem(LABEL_COPY_COLUMN); // ��������
		mItem.setIcon(GM.getMenuImageIcon("blank"));
		mItem.setName(String.valueOf(iCOPY_COLNAMES));
		mItem.addActionListener(popAction);
		pm.add(mItem);

		if (row > -1 && col > -1) {
			final Object cellVal = data.getValueAt(row, col);
			if (cellVal != null && cellVal instanceof String) {
				mItem = new JMenuItem(LABEL_VIEW_TEXT);
				mItem.setIcon(GM
						.getImageIcon(GC.IMAGES_PATH + "b_showtext.gif"));
				mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						showText(row, col, cellVal);
					}
				});
				pm.add(mItem);
			}
		}
		pm.show(e.getComponent(), e.getX(), e.getY());
	}

	/** �������� */
	public static final String LABEL_COPY_COLUMN = mm
			.getMessage("jtablevalue.copycolnames");
	/** �鿴���ı� */
	public static final String LABEL_VIEW_TEXT = mm
			.getMessage("dialogtexteditor.title1");

	/**
	 * �Ҽ��˵��¼�����
	 */
	private ActionListener popAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JMenuItem mItem = (JMenuItem) e.getSource();
			short cmd = Short.parseShort(mItem.getName());
			switch (cmd) {
			case iCOPY:
				copyValue();
				break;
			case iCOPY_COLNAMES:
				copyColumnNames();
				break;
			case iPASTE:
				pasteValue();
				break;
			case iFORMAT:
				colFormat();
				break;
			}
		}
	};

	/**
	 * �и�ʽ�༭
	 */
	private void colFormat() {
		int col = getSelectedColumn();
		if (col < 0) {
			return;
		}
		String colName = null;
		if (!StringUtils.isValidString(colName)) {
			colName = getColumnName(col);
		}
		String format = GM.getColumnFormat(colName);
		DialogCellFormat dcf = new DialogCellFormat();
		if (format != null) {
			dcf.setFormat(format);
		}
		dcf.setVisible(true);
		if (dcf.getOption() == JOptionPane.OK_OPTION) {
			format = dcf.getFormat();
			GM.saveFormat(colName, format);
			setColFormat(col, format);
		}

	}

	/**
	 * �����и�ʽ
	 * 
	 * @param col
	 *            �к�
	 * @param format
	 *            ��ʽ
	 */
	private void setColFormat(int col, String format) {
		TableColumn tc = getColumn(col);
		tc.setCellEditor(getAllPurposeEditor());
		tc.setCellRenderer(new AllPurposeRenderer(format));
		this.repaint();
	}

	/**
	 * ˢ��
	 */
	public void refresh() {
		forceSetValue(value);
	}

	/**
	 * ֵ�Ƿ�null
	 * 
	 * @return
	 */
	public boolean valueIsNull() {
		return value == null;
	}

	/**
	 * �����Ƿ����ģʽ��
	 * 
	 * @param locked1
	 *            �Ƿ����ģʽ��
	 */
	public void setLocked1(boolean locked1) {
		this.isLocked1 = locked1;
		setLocked(locked1);
	}

	/**
	 * �Ƿ����ģʽ��
	 * 
	 * @return
	 */
	public boolean isLocked1() {
		return this.isLocked1;
	}

	/**
	 * �����Ƿ�������Ԫ��
	 * 
	 * @param locked
	 *            �Ƿ�������Ԫ��
	 */
	public void setLocked(boolean locked) {
		this.isLocked = locked;
		panelValue.valueBar.setLocked(locked);
	}

	/**
	 * ȡ�Ƿ�������Ԫ��
	 * 
	 * @return
	 */
	public boolean isLocked() {
		return isLocked;
	}

	/**
	 * �α�ȡ��
	 * 
	 * @param dispRows
	 *            ��ʾ����
	 */
	public void cursorFetch(int dispRows) {
		if (originalValue == null || !(originalValue instanceof ICursor)
				|| originalValue instanceof IMultipath) {
			return;
		}
		ICursor cursor = (ICursor) originalValue;
		Sequence data = cursor.peek(dispRows); // ��ʵ�ʴ��α�ȡ��
		forceSetValue(data);
	}

	/**
	 * ��갴���¼�
	 */
	public void mousePressed(MouseEvent e) {
		refreshValueButton();
		int row = rowAtPoint(e.getPoint());
		if (row < 0) {
			return;
		}
		row += panelValue.sbValue.getValue() - 1;
		if (selectedRows.isEmpty()) {
			selectedRows.addInt(row);
			resetSelection();
			lastRow = row;
			return;
		}
		rowSelected(e, row);
	}

	/**
	 * ˢ�°�ť״̬
	 */
	private void refreshValueButton() {
		panelValue.valueBar.refresh();
	}

	/**
	 * ����ѡ��״̬
	 */
	private void resetSelection() {
		ListSelectionModel selectModel = getSelectionModel();
		selectModel.clearSelection();
		if (!selectedRows.isEmpty()) {
			int r;
			for (int i = 0; i < selectedRows.size(); i++) {
				r = selectedRows.getInt(i);
				r = r - panelValue.sbValue.getValue() + 1;
				if (r > -1 && r < getRowCount()) {
					selectModel.addSelectionInterval(r, r);
				}
			}
		}
		this.setSelectionModel(selectModel);
	}

	/**
	 * ��������
	 * 
	 * @param index
	 *            ��ʼ�к�
	 */
	public void resetData(int index) {
		resetData(index, false);
	}

	/**
	 * ��������
	 * 
	 * @param index
	 *            ��ʼ�к�
	 * @param isFirst �Ƿ��һ������ֵ
	 */
	public void resetData(int index, boolean isFirst) {
		dispStartIndex = index;
		if (resetThread != null) {
			resetThread.stopThread();
			try {
				resetThread.join();
			} catch (Exception e) {
			}
		}
		resetThread = null;
		Sequence s;
		switch (m_type) {
		case TYPE_DB:
			s = dbTable;
			break;
		case TYPE_TABLE:
		case TYPE_PMT:
		case TYPE_SERIESPMT:
		case TYPE_SERIES:
			if (!(value instanceof Sequence))
				return;
			s = (Sequence) value;
			break;
		default:
			return;
		}
		resetThread = new ResetDataThread(s, index, m_type);
		if (isFirst)
			resetThread.setFirst();
		SwingUtilities.invokeLater(resetThread);
	}

	private final int DISP_ROWS = 50;

	/**
	 * �߳�ʵ��
	 */
	private ResetDataThread resetThread = null;

	/**
	 * ��������(����)���ݵ��߳�
	 *
	 */
	class ResetDataThread extends Thread {
		/**
		 * ���ж���
		 */
		Sequence seq;
		/**
		 * ��ʼ��
		 */
		int index;
		/**
		 * ��������
		 */
		byte dataType;
		/**
		 * �Ƿ�ֹͣ�ˣ��Ƿ������
		 */
		boolean isStoped = false, isFinished = false;

		boolean isFirst = false;

		/**
		 * ���캯��
		 * 
		 * @param s
		 *            ����
		 * @param index
		 *            ��ʼ��
		 * @param dataType
		 *            ����
		 */
		ResetDataThread(Sequence s, int index, byte dataType) {
			this.seq = s;
			this.index = index;
			this.dataType = dataType;
		}

		public void setFirst() {
			this.isFirst = true;
		}

		/**
		 * ִ��
		 */
		public void run() {
			try {
				if (seq == null) {
					removeAllRows();
					return;
				}
				boolean isSeq = false;
				switch (m_type) {
				case TYPE_TABLE:
				case TYPE_PMT:
				case TYPE_SERIESPMT:
				case TYPE_SERIES:
					isSeq = true;
					break;
				}
				int height = panelValue.spValue.getPreferredSize().height;
				int startRow = index;
				int count = height / ROW_HEIGHT + 1;
				count = Math.max(DISP_ROWS, count);
				int endRow = Math.min(rowCount, startRow + count);

				int oldRowCount = getRowCount();
				int dispRowCount = endRow - startRow + 1;

				if (isStoped)
					return;
				if (dispRowCount > oldRowCount) {
					for (int i = 0, size = dispRowCount - oldRowCount; i < size; i++) {
						addRow();
					}
				} else if (dispRowCount < oldRowCount) {
					for (int i = 0, size = oldRowCount - dispRowCount; i < size; i++) {
						removeRow(0);
					}
				}
				if (isStoped)
					return;
				boolean isDup = isDupColNames();
				Object rowData;
				for (int i = startRow; i <= endRow; i++) {
					if (isStoped)
						return;
					rowData = seq.get(i);
					if (rowData instanceof Record
							&& (dataType == TYPE_PMT || dataType == TYPE_TABLE
									|| dataType == TYPE_SERIESPMT || dataType == TYPE_DB)) {
						setRecordRow((Record) seq.get(i), i - startRow, isSeq,
								i, isDup);
					} else {
						if (isSeq) {
							data.setValueAt(new Integer(i), i - startRow,
									COL_FIRST);
							data.setValueAt(seq.get(i), i - startRow,
									COL_FIRST + 1);
						} else {
							data.setValueAt(seq.get(i), i - startRow, COL_FIRST);
						}
					}
				}
				if (isFirst) { // ����Ӧ�п�
					FontMetrics fm = getFontMetrics(getFont());
					int cc = getColumnCount();
					int rc = getRowCount();
					Object val;
					for (int c = isSeq ? 1 : 0; c < cc; c++) {
						TableColumn tc = getColumn(c);
						int newWidth = 0;
						for (int r = 0; r < rc; r++) {
							val = data.getValueAt(r, c);
							if (val != null) {
								String dispStr = GM.renderValueText(val);
								if (StringUtils.isValidString(dispStr)) {
									newWidth = Math.max(newWidth,
											fm.stringWidth(dispStr));
								}
							}
						}
						newWidth = Math
								.min(MAX_COL_WIDTH, newWidth + WIDTH_GAP);
						if (newWidth > tc.getWidth()) {
							setColWidth(tc, newWidth);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				resetSelection();
				isFinished = true;
			}
		}

		/**
		 * ֹͣ�߳�
		 */
		void stopThread() {
			seq = null;
			isStoped = true;
		}

		/**
		 * �Ƿ��Ѿ����
		 * 
		 * @return
		 */
		boolean isFinished() {
			return isFinished;
		}
	}

	/**
	 * ���ü�¼������һ��
	 * 
	 * @param record
	 * @param r
	 * @param isSeq
	 * @param index
	 * @param isDup
	 *            �Ƿ����ظ�����
	 */
	private void setRecordRow(Record record, int r, boolean isSeq, int index,
			boolean isDup) {
		if (record == null || r < 0)
			return;
		if (m_type == TYPE_TABLE) {
			int colCount = this.getColumnCount();
			if (isSeq) {
				data.setValueAt(new Integer(index), r, COL_FIRST);
			}
			for (int j = isSeq ? 1 : 0; j < colCount; j++) {
				data.setValueAt(record.getFieldValue(isSeq ? j - 1 : j), r, j);
			}
		} else {
			if (isSeq) {
				data.setValueAt(new Integer(index), r, COL_FIRST);
			}
			DataStruct ds = record.dataStruct();
			String[] colNames = ds.getFieldNames();
			if (colNames != null) {
				if (isDup) { // ֧����ʾ�ظ�����������
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					Object val;
					for (int j = 0; j < colNames.length; j++) {
						try {
							val = record.getFieldValue(j);
						} catch (Exception e) {
							// ȡ��������ʾ��
							val = null;
						}
						Integer colIndex = map.get(colNames[j]);
						if (colIndex == null) {
							colIndex = 0;
						} else {
							colIndex = colIndex + 1;
						}
						int col = getColumnIndex(colNames[j], isSeq ? 1 : 0,
								colIndex);
						if (col > -1) {
							map.put(colNames[j], colIndex);
							data.setValueAt(val, r, col);
						}
					}
				} else {
					Object val;
					for (int j = 0; j < colNames.length; j++) {
						try {
							val = record.getFieldValue(colNames[j]);
						} catch (Exception e) {
							// ȡ��������ʾ��
							val = null;
						}
						int col = getColumnIndex(colNames[j], isSeq ? 1 : 0);
						if (col > -1) {
							data.setValueAt(val, r, col);
						}
					}
				}
			}
		}
	}

	/**
	 * ��������ȡ�����
	 * 
	 * @param colName
	 *            ����
	 * @param startIndex
	 *            ��ʼ��
	 * @return
	 */
	private int getColumnIndex(String colName, int startIndex) {
		return getColumnIndex(colName, startIndex, 0);
	}

	/**
	 * ��������ȡ�����
	 * 
	 * @param colName
	 *            ����
	 * @param startIndex
	 *            ��ʼ��
	 * @param colIndex
	 *            �ظ������Ĵ���
	 * @return
	 */
	private int getColumnIndex(String colName, int startIndex, int colIndex) {
		int dupCount = 0;
		for (int i = startIndex; i < this.getColumnCount(); i++) {
			String name = getColumnName(i);
			if (name.equals(colName)) {
				if (colIndex > dupCount) {
					dupCount++;
					continue;
				}
				return i;
			}
		}
		return -1;
	}

	/**
	 * �Ƿ����ظ�����
	 * 
	 * @return
	 */
	private boolean isDupColNames() {
		HashSet<String> hs = new HashSet<String>();
		String name;
		for (int i = 0; i < this.getColumnCount(); i++) {
			name = getColumnName(i);
			if (hs.contains(name)) {
				return true;
			} else {
				hs.add(name);
			}
		}
		return false;
	}

	/**
	 * ��Ԫ���Ƿ���Ա༭
	 */
	public boolean isCellEditable(int row, int column) {
		switch (m_type) {
		case TYPE_TABLE:
		case TYPE_PMT:
		case TYPE_SERIESPMT:
			Sequence s = (Sequence) value;
			if (s.dataStruct() != null) {
				int count = s.dataStruct().getFieldCount();
				if (column > count) {
					return false;
				}
			}
		}
		TableColumn tc = getColumn(column);
		TableCellEditor tce = tc.getCellEditor();
		boolean readOnly = true;
		if (tce instanceof AllPurposeEditor) {
			AllPurposeEditor ape = (AllPurposeEditor) tce;
			readOnly = !ape.isCellEditable(null);
		}
		return editable && !readOnly;
	}

	/**
	 * ����
	 */
	public void clear() {
		undo.clear();
		redo.clear();
		isLocked = false;
		value = null;
		initJTable();
	}

	/**
	 * ���õ�Ԫ������
	 * 
	 * @param id
	 */
	public void setCellId(String id) {
		if (this.isLocked) {
			return;
		}
		this.cellId = id;
	}

	/**
	 * ȡ��Ԫ������
	 * 
	 * @return
	 */
	public String getCellId() {
		return cellId;
	}

	/**
	 * ���õ�Ԫ��ֵ
	 * 
	 * @param value
	 *            ��Ԫ��ֵ
	 */
	public void setValue(Object value) {
		setValue(value, false);
	}

	/**
	 * �������õ�Ԫ��ֵ������������״̬
	 * 
	 * @param value
	 *            ��Ԫ��ֵ
	 * @param id
	 *            ��Ԫ������
	 */
	public void setValue1(Object value, String id) {
		this.originalValue = value;
		setValue(value, false, true);
		this.cellId = id;
	}

	/**
	 * ���õ�Ԫ��ֵ
	 * 
	 * @param value
	 *            ��Ԫ��ֵ
	 * @param editable
	 *            �Ƿ���Ա༭
	 */
	public void setValue(Object value, boolean editable) {
		setValue(value, editable, false);
	}

	/**
	 * ���õ�Ԫ��ֵ
	 * 
	 * @param value
	 *            ��Ԫ��ֵ
	 * @param editable
	 *            �Ƿ���Ա༭
	 * @param forceSetValue
	 *            �Ƿ���������״̬��ǿ�����ø�ֵ
	 */
	private synchronized void setValue(Object value, boolean editable,
			final boolean forceSetValue) {
		setValue(value, editable, forceSetValue, null);
	}

	/**
	 * 
	 * ���õ�Ԫ��ֵ
	 * 
	 * @param value
	 *            ��Ԫ��ֵ
	 * @param editable
	 *            �Ƿ���Ա༭
	 * @param forceSetValue
	 *            �Ƿ���������״̬��ǿ�����ø�ֵ
	 * @param dispStartRow
	 *            ��ʾ��ʼ��
	 */
	private synchronized void setValue(Object value, boolean editable,
			final boolean forceSetValue, UndoObject uo) {
		if (isLocked && !forceSetValue) {
			return;
		}
		if (value != null) {
			if (value instanceof Canvas) {
				this.canvas = (Canvas) value;
				value = ((Canvas) value).getChartElements();
			} else {
				this.canvas = null;
			}
		} else {
			this.canvas = null;
		}
		this.value = value;
		if (!forceSetValue) {
			this.originalValue = value;
		}
		this.editable = editable;
		rowCount = 0;
		dbTable = null;
		final int dispStartIndex;
		if (uo != null) {
			this.selectedRows = uo.selectedRows;
			dispStartIndex = uo.dispStartIndex;
		} else {
			selectedRows.clear();
			dispStartIndex = 1;
		}
		boolean isCursor = false;
		if (originalValue != null && originalValue instanceof ICursor) {
			if (!(originalValue instanceof IMultipath)) // ��֧�ֶ�·�α�
				isCursor = true;
		}
		panelValue.setCursorValue(isCursor);
		final Object aValue = value;
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				resetValue(forceSetValue, aValue, dispStartIndex);
			}
		});
	}

	/**
	 * ȡԭʼֵ
	 * 
	 * @return
	 */
	public Object getOriginalValue() {
		return originalValue;
	}

	/**
	 * ��ʾ����ʼ��
	 */
	private int dispStartIndex = 1;

	/**
	 * �����ֵ
	 * 
	 * @param forceSetValue
	 *            ǿ�����ø�ֵ
	 * @param aValue
	 *            ֵ
	 */
	private synchronized void resetValue(boolean forceSetValue, Object aValue,
			int dispStartIndex) {
		try {
			initJTable();
			if (!forceSetValue) {
				undo.clear();
				redo.clear();
			}
			refreshValueButton();
			// null������ʾ��(null)��ֻ��û��ҳ��ʱ�Ų���ʾ
			if (GV.appSheet == null) {
				if (aValue == null) {
					// ����null�Ļ���resetDataǰ�ر�
					if (resetThread != null) {
						resetThread.stopThread();
						try {
							resetThread.join();
						} catch (Exception e) {
						}
					}
					resetThread = null;
					return;
				}
			}
			setColumnModel();
			m_type = getValueType(aValue);
			switch (m_type) {
			case TYPE_TABLE:
				rowCount = initTable((Table) aValue);
				break;
			case TYPE_PMT:
				rowCount = initPmt((Sequence) aValue);
				break;
			case TYPE_RECORD:
				rowCount = initRecord((Record) aValue);
				break;
			case TYPE_SERIES:
				rowCount = initSeries((Sequence) aValue);
				break;
			case TYPE_SERIESPMT:
				rowCount = initSeriesPmt((Sequence) aValue);
				break;
			case TYPE_DB:
				rowCount = initDB((DBObject) aValue);
				break;
			case TYPE_FILE:
				rowCount = initFile((FileObject) aValue);
				break;
			default:
				rowCount = initDefault(aValue);
				break;
			}
		} finally {
			try {
				panelValue.preventChange = true;
				panelValue.sbValue.setMinimum(1);
				panelValue.sbValue.setMaximum(rowCount);
				panelValue.sbValue.setValue(dispStartIndex);
				panelValue.spValue.getHorizontalScrollBar().setValue(1);
				resetData(dispStartIndex, true);
			} finally {
				panelValue.preventChange = false;
			}
		}
	}

	/**
	 * ��ʼ�����ؼ�
	 */
	private void initJTable() {
		removeAllRows();
		data.setColumnCount(0);
		data.getDataVector().clear();
	}

	/**
	 * ȡֵ������
	 * 
	 * @param value
	 *            ֵ
	 * @return
	 */
	private byte getValueType(Object value) {
		if (value instanceof Table) {
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			return TYPE_TABLE;
		} else if (value instanceof Sequence) {
			if (((Sequence) value).isPurePmt()) {
				setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				return TYPE_PMT;
			}
			// ���У��Ǵ����У�������ͨ���н�����ʾ
			// else if (((Sequence) value).isPmt()) {
			// setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			// return TYPE_SERIESPMT;
			// }
			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			return TYPE_SERIES;
		} else if (value instanceof Record) {
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			return TYPE_RECORD;
		} else if (value instanceof DBObject) {
			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			return TYPE_DB;
		} else if (value instanceof FileObject) {
			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			return TYPE_FILE;
		} else {
			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			return TYPE_DEFAULT;
		}
	}

	/**
	 * ��ʼ�����
	 * 
	 * @param table
	 *            ���
	 * @return
	 */
	private int initTable(Table table) {
		DataStruct ds = table.dataStruct();
		setTableColumns(ds, table == null ? 0 : table.length(), true);
		setEditStyle(ds, true);
		return table.length();
	}

	/**
	 * ��ʼ��������
	 * 
	 * @param pmt
	 *            ������
	 * @return
	 */
	private int initPmt(Sequence pmt) {
		DataStruct ds = pmt.dataStruct();
		setTableColumns(ds, pmt == null ? 0 : pmt.length(), true);
		setEditStyle(ds, true);
		return pmt.length();
	}

	/**
	 * ��ʼ������
	 * 
	 * @param pmt
	 * @return
	 */
	private int initSeriesPmt(Sequence pmt) {
		DataStruct ds = getFirstDataStruct(pmt);
		setTableColumns(ds, pmt == null ? 0 : pmt.length(), true);
		setEditStyle(ds, true);
		return pmt.length();
	}

	/**
	 * ȡ��һ�����ݽṹ�����ﲻ����ifn��Ҫ�ҵ���һ���нṹ�ļ�¼
	 * 
	 * @return
	 */
	private DataStruct getFirstDataStruct(Sequence pmt) {
		int size = pmt.length();
		DataStruct ds;
		for (int i = 1; i <= size; ++i) {
			Object obj = pmt.get(i);
			if (obj != null && obj instanceof Record) {
				ds = ((Record) obj).dataStruct();
				if (ds != null && ds.getFieldCount() > 0)
					return ds;
			}
		}
		return null;
	}

	/**
	 * ��ʼ������
	 * 
	 * @param series
	 * @return
	 */
	private int initSeries(Sequence series) {
		addColumn(TITLE_INDEX);
		addColumn(TITLE_SERIES);
		TableColumn tc;
		tc = getColumn(TITLE_INDEX);
		tc.setCellEditor(getAllPurposeEditor());
		tc.setCellRenderer(new AllPurposeRenderer(true));
		final int INDEX_WIDTH = getIndexColWidth(series == null ? 0 : series
				.length());
		setColWidth(tc, INDEX_WIDTH, false);
		tc = getColumn(TITLE_SERIES);
		setColWidth(tc, 99999);
		tc.setCellEditor(getAllPurposeEditor());
		tc.setCellRenderer(new AllPurposeRenderer(true));
		return series.length();
	}

	/**
	 * ��ʼ����¼
	 * 
	 * @param record
	 *            ��¼
	 * @return
	 */
	private int initRecord(Record record) {
		DataStruct ds = record.dataStruct();
		setTableColumns(ds, 1, false);
		try {
			AppendDataThread.addRecordRow(this, record);
		} catch (Exception ex) {
			GM.showException(ex);
		}
		setEditStyle(ds, false);
		return 1;
	}

	/**
	 * �Ƿ�����
	 * 
	 * @param primaries
	 *            ��������
	 * @param colName
	 *            �ֶ���
	 * @return
	 */
	private boolean isPrimary(String[] primaries, String colName) {
		if (primaries == null) {
			return false;
		}
		for (int i = 0; i < primaries.length; i++) {
			if (colName.equals(primaries[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ���ñ༭���
	 * 
	 * @param ds
	 *            ���ݽṹ
	 * @param hasIndex
	 *            �Ƿ��������
	 */
	private void setEditStyle(DataStruct ds, boolean hasIndex) {
		if (ds == null)
			return;
		TableColumn tc;
		if (hasIndex) {
			tc = getColumn(COL_FIRST);
			tc.setCellRenderer(new AllPurposeRenderer(hasIndex));
		}
		String cols[] = ds.getFieldNames();
		String[] primaries = ds.getPrimary();
		for (int i = 0; i < cols.length; i++) {
			if (hasIndex) {
				tc = this.getColumn(i + 1);
			} else {
				tc = getColumn(i);
			}
			// �������ܱ༭
			boolean isPrimary = isPrimary(primaries, cols[i]);
			boolean editable = !isPrimary;
			tc.setCellEditor(getAllPurposeEditor(editable));

			String format = GM.getColumnFormat(cols[i]);
			if (StringUtils.isValidString(format)) {
				tc.setCellRenderer(new AllPurposeRenderer(format));
			} else {
				tc.setCellRenderer(new AllPurposeRenderer(hasIndex));
			}
		}
	}

	private final int MAX_COL_WIDTH = 300; // �����ݱȽϳ�ʱ�������ʾ�Ŀ��
	private final int WIDTH_GAP = 8;

	/**
	 * ���ñ�����
	 * 
	 * @param ds
	 *            ���ݽṹ
	 * @param len
	 *            ���ݳ��ȣ����ڼ�������п��
	 * @param hasIndexCol
	 *            �Ƿ��������
	 */
	private synchronized void setTableColumns(DataStruct ds, int len,
			boolean hasIndexCol) {
		if (ds == null)
			return;
		String nNames[] = ds.getFieldNames();
		if (nNames != null) {
			Vector<String> cols = new Vector<String>();
			if (value instanceof Sequence) {
				cols.add(TITLE_INDEX);
			}
			for (int i = 0; i < nNames.length; i++) {
				cols.add(nNames[i]);
			}
			data.setDataVector(null, cols);
		}
		try {
			int cc = getColumnCount();
			TableColumn tc;
			final int INDEX_WIDTH;
			if (hasIndexCol) {
				INDEX_WIDTH = getIndexColWidth(len);
				tc = getColumn(TITLE_INDEX);
				setColWidth(tc, INDEX_WIDTH, false);
			} else {
				INDEX_WIDTH = 0;
			}

			// ���ݱ��ⳤ�������п�
			int[] pkIndex = ds.getPKIndex();
			final int IMAGE_WIDTH = 35;
			final int startCol = hasIndexCol ? 1 : 0;
			for (int i = startCol; i < cc; i++) {
				tc = getColumn(i);
				int titleWidth = getFontMetrics(getFont()).stringWidth(
						getColumnName(i));
				int colWidth = tc.getWidth();
				if (isPK(pkIndex, i - startCol)) {
					tc.setHeaderRenderer(new PKRenderer());
					titleWidth += IMAGE_WIDTH;
				}
				titleWidth = Math.min(titleWidth + WIDTH_GAP, MAX_COL_WIDTH);
				if (titleWidth > colWidth) {
					setColWidth(tc, titleWidth);
				}
			}

			int totalColWidth = 0;
			for (int i = 0; i < cc; i++) {
				totalColWidth += getColumn(i).getWidth();
			}
			int width = getParent().getWidth();
			if (totalColWidth < width && cc > 0) { // �����������ʾ���£���ʣ����ƽ�����䵽����
				int aveWidth;
				width -= totalColWidth;
				if (cc > 1 && hasIndexCol) {
					aveWidth = width / (cc - 1);
				} else {
					aveWidth = width / cc;
				}
				for (int i = hasIndexCol ? 1 : 0; i < cc; i++) {
					tc = getColumn(i);
					int newWidth = tc.getWidth() + aveWidth;
					setColWidth(tc, newWidth);
				}
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * �����п�
	 * @param tc
	 * @param colWidth
	 */
	private void setColWidth(TableColumn tc, int colWidth) {
		setColWidth(tc, colWidth, true);
	}

	/**
	 * �����п�
	 * @param tc
	 * @param colWidth
	 */
	private void setColWidth(TableColumn tc, int colWidth, boolean canResize) {
		tc.setMinWidth(colWidth);
		tc.setWidth(colWidth);
		tc.setPreferredWidth(colWidth);
		if (canResize)
			tc.setMinWidth(0);
	}

	/**
	 * �����е���Ⱦ��
	 */
	class PKRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public PKRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			c.setBackground(table.getBackground());
			c.setForeground(table.getForeground());
			if (c instanceof JLabel) {
				((JLabel) c).setText((String) value);
				((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
				((JLabel) c).setIcon(GM
						.getImageIcon(GC.IMAGES_PATH + "key.png"));
			}
			return c;
		}
	}

	/**
	 * ���Ƿ�����
	 * 
	 * @param pkIndex
	 *            �������
	 * @param index
	 *            �����
	 * @return
	 */
	private boolean isPK(int[] pkIndex, int index) {
		if (pkIndex == null || pkIndex.length == 0)
			return false;
		for (int pk : pkIndex) {
			if (pk == index)
				return true;
		}
		return false;
	}

	/**
	 * ȡ����п�
	 * 
	 * @param len
	 * @return
	 */
	private int getIndexColWidth(int len) {
		if (len <= 9999) {
			return 40;
		}
		if (len <= 999999) {
			return 60;
		}
		return 80;
	}

	/**
	 * ��������쳣���ǲ�Ӱ����ʾ�����⴦��һ��
	 */
	private void setColumnModel() {
		this.setColumnModel(new DefaultTableColumnModel() {
			private static final long serialVersionUID = 1L;

			public TableColumn getColumn(int col) {
				try {
					return super.getColumn(col);
				} catch (Exception ex) {
					return new TableColumn();
				}
			}
		});
	}

	/**
	 * DBInfo�����Ӧ�����
	 */
	private Table dbTable = null;

	/**
	 * ��ʼ��DBInfo����
	 * 
	 * @param db
	 *            DBInfo����
	 * @return
	 */
	private int initDB(DBObject db) {
		dbTable = GMSpl.getDBTable(db);
		addColumn(GMSpl.TITLE_NAME); // ����
		addColumn(GMSpl.TITLE_PROP); // ����
		for (int i = 0; i < this.getColumnCount(); i++) {
			setColumnEditable(i, false);
		}
		if (db == null || db.getDbSession() == null) {
			return 0;
		}
		// initDBTable(db);
		return dbTable.length();
	}

	/**
	 * ����DBInfo���󵽱��
	 * 
	 * @param db
	 *            DBInfo����
	 */
	// private void initDBTable(DBObject db) {
	// DBInfo info = db.getDbSession().getInfo();
	// if (info == null) {
	// return;
	// }
	// dbTable.newLast(new Object[] { DB_NAME, info.getName() });
	// if (info instanceof DBConfig) {
	// int type = info.getDBType();
	// dbTable.newLast(new Object[] { DB_TYPE,
	// DBTypeEx.getDBTypeName(type) });
	//
	// DBConfig dc = (DBConfig) info;
	// dbTable.newLast(new Object[] { DRIVER, dc.getDriver() });
	// dbTable.newLast(new Object[] { URL, dc.getUrl() });
	// dbTable.newLast(new Object[] { USER, dc.getUser() });
	// String pwd = dc.getPassword();
	// dbTable.newLast(new Object[] { PASSWORD, pwd });
	// dbTable.newLast(new Object[] { USE_SCHEMA,
	// Boolean.toString(dc.isUseSchema()) });
	// dbTable.newLast(new Object[] { ADD_TILDE,
	// Boolean.toString(dc.isAddTilde()) });
	// }
	// }

	/**
	 * ��ʼ��FileObject����
	 * 
	 * @param file
	 *            FileObject����
	 * @return
	 */
	private int initFile(FileObject file) {
		addColumn(mm.getMessage("jtablevalue.file"));
		return initSingleValue(file);
	}

	/**
	 * ��ʼ����ֵͨ
	 * 
	 * @param value
	 *            ��ֵͨ
	 * @return
	 */
	private int initDefault(Object value) {
		addColumn(mm.getMessage("jtablevalue.value"));
		return initSingleValue(value);
	}

	/**
	 * ��ʼ��������ֵ
	 * 
	 * @param value
	 * @return
	 */
	private int initSingleValue(final Object value) {
		TableColumn tc = getColumn(COL_FIRST);
		tc.setCellEditor(getAllPurposeEditor());
		tc.setCellRenderer(new AllPurposeRenderer());
		if (getRowCount() == 0) {
			addRow();
		}
		data.setValueAt(value, 0, COL_FIRST);
		return 1;
	}

	/**
	 * ȡ������ֵ�ı༭��
	 * 
	 * @return
	 */
	public AllPurposeEditor getAllPurposeEditor() {
		return getAllPurposeEditor(false);
	}

	/**
	 * ȡ������ֵ�ı༭��
	 * 
	 * @param editable
	 *            �Ƿ���Ա༭
	 * @return
	 */
	public AllPurposeEditor getAllPurposeEditor(boolean editable) {
		JTextFieldReadOnly jtf;
		if (editable) {
			jtf = new JTextFieldReadOnly(new String(), 0, editListener);
		} else {
			jtf = new JTextFieldReadOnly();
		}
		return new AllPurposeEditor(jtf, this);
	}

	/**
	 * ��ʾ��ֵ
	 */
	public void dispCellValue() {
		int r = getSelectedRow();
		int c = getSelectedColumn();
		drillValue(r, c);
	}

	/**
	 * ��ȡ��Աֵ
	 * 
	 * @param row
	 *            �к�
	 * @param col
	 *            �к�
	 */
	private void drillValue(int row, int col) {
		if (editable) {
			return;
		}
		JScrollBar sbValue = panelValue.sbValue;
		int scrollVal = Math.max(sbValue.getValue(), 1);
		int realRow = scrollVal + row;
		Object newValue = null;
		switch (m_type) {
		case TYPE_TABLE:
		case TYPE_PMT:
		case TYPE_SERIES:
		case TYPE_SERIESPMT:
			if (!(value instanceof Sequence))
				return;
			Sequence s = (Sequence) value;
			Object temp = s.get(realRow);
			if (temp instanceof Record) {
				Record r = (Record) temp;
				if (r.dataStruct() != null && s.dataStruct() != null
						&& !r.dataStruct().isCompatible(s.dataStruct())) { // �칹����
					newValue = temp;
				} else if ((TYPE_SERIES == m_type || TYPE_SERIESPMT == m_type)) {
					newValue = temp;
				}
			}
			break;
		default:
			break;
		}
		if (newValue == null) {
			newValue = data.getValueAt(row, col);
			if (newValue == null) {
				return;
			}
		}
		if (newValue.equals(value)) { // ��ȡ��Ԫ���Ǳ���ʱ
			return;
		}
		redo.clear();
		undo.push(getUndoObject());
		value = newValue;
		forceSetValue(value);
	}

	/**
	 * ǿ������ֵ������������״̬
	 * 
	 * @param newValue
	 */
	private void forceSetValue(Object newValue) {
		setValue(newValue, editable, true);
	}

	/**
	 * �Ƿ���Գ���
	 * 
	 * @return
	 */
	public boolean canUndo() {
		return !undo.empty() && !editable;
	}

	/**
	 * �Ƿ��������
	 * 
	 * @return
	 */
	public boolean canRedo() {
		return !redo.empty() && !editable;
	}

	/**
	 * ����
	 */
	public void undo() {
		UndoObject uo = getUndoObject();
		redo.push(uo);
		uo = (UndoObject) undo.pop();
		this.selectedRows = uo.selectedRows;
		setValue(uo.value, editable, true, uo);
	}

	/**
	 * ����
	 */
	public void redo() {
		UndoObject uo = getUndoObject();
		undo.push(uo);
		uo = (UndoObject) redo.pop();
		this.selectedRows = uo.selectedRows;
		setValue(uo.value, editable, true, uo);
	}

	private UndoObject getUndoObject() {
		UndoObject uo = new UndoObject();
		uo.value = value;
		if (value != null && value instanceof Sequence) {
			uo.dispStartIndex = dispStartIndex;
			uo.selectedRows.addAll(selectedRows);
		} else {
			uo.dispStartIndex = 1;
			if (selectedRows.size() == 1 && selectedRows.getInt(0) == 0) {
				uo.selectedRows.addInt(0);
			}
		}
		return uo;
	}

	class UndoObject {
		Object value;
		int dispStartIndex = 1;
		IntArrayList selectedRows = new IntArrayList();
	}

	/**
	 * ���Ի���ͳ��ͼ
	 * 
	 * @return
	 */
	public boolean canDrawChart() {
		if (canvas != null && canvas instanceof Canvas)
			return true;

		if (value != null) {
			if (value instanceof byte[])
				return true;
			if (value instanceof Table)
				return true;
			if (value instanceof Sequence) {
				Sequence seq = (Sequence) value;
				if (seq.isPurePmt())
					return true;
			}
		}

		return false;
	}

	/**
	 * �Ƿ������ʾ���ı�
	 * 
	 * @return
	 */
	public boolean canShowText() {
		int row = getSelectedRow();
		int col = getSelectedColumn();
		if (row == -1 || col == -1) {
			return false;
		}
		final Object cellVal = data.getValueAt(row, col);
		return StringUtils.isValidString(cellVal);
	}

	/**
	 * ȡ�������ݵ���ʼ��
	 * 
	 * @return
	 */
	private int getCopyStartCol() {
		boolean isSeq = false;
		switch (m_type) {
		case TYPE_TABLE:
		case TYPE_PMT:
		case TYPE_SERIESPMT:
		case TYPE_SERIES:
			isSeq = true;
			break;
		}
		int startCol = 0;
		if (isSeq) {
			startCol = 1; // ���
		}
		return startCol;
	}

	/**
	 * �����б���
	 */
	public void copyColumnNames() {
		StringBuffer buf = new StringBuffer();
		for (int c = getCopyStartCol(); c < getColumnCount(); c++) {
			if (buf.length() > 0)
				buf.append(",");
			buf.append(getColumnName(c));
		}
		GM.clipBoard(buf.toString());
	}

	/**
	 * ��������
	 * 
	 * @return
	 */
	public boolean copyValue() {
		return copyValue(false);
	}

	/**
	 * ��������
	 * 
	 * @param copyTitle
	 *            �Ƿ��Ʊ���
	 * @return
	 */
	public boolean copyValue(boolean copyTitle) {
		IntArrayList selectedRows = this.selectedRows;
		if (selectedRows.isEmpty()) {
			int count = 1;
			if (m_type == TYPE_DB) {
				count = dbTable.length();
			} else if (value instanceof Sequence) {
				count = ((Sequence) value).length();
			}
			for (int i = 0; i < count; i++) {
				selectedRows.add(i);
			}
		}
		int startCol = getCopyStartCol();
		int cc = getColumnCount() - startCol;
		int rowCount = selectedRows.size();
		if (copyTitle) {
			rowCount += 1;
		}
		Matrix matrix;
		CellRect cr = new CellRect(0, (short) 0, selectedRows.size() - 1,
				(short) (cc - 1));
		Sequence seq = null;
		switch (m_type) {
		case TYPE_DB:
			matrix = new Matrix(rowCount, cc);
			seq = dbTable;
			break;
		case TYPE_TABLE:
		case TYPE_PMT:
		case TYPE_SERIESPMT:
		case TYPE_SERIES:
			matrix = new Matrix(rowCount, cc);
			seq = (Sequence) value;
			break;
		default:
			matrix = new Matrix(selectedRows.size(), cc);
			for (int r = 0; r < selectedRows.size(); r++) {
				for (int c = startCol; c < getColumnCount(); c++) {
					Object value = data.getValueAt(selectedRows.getInt(r), c);
					PgmNormalCell pnc = new PgmNormalCell();
					pnc.setValue(value);
					try {
						pnc.setExpString(Variant.toExportString(value));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					matrix.set(r, c, pnc);
				}
			}
			break;
		}
		if (seq != null) {
			if (copyTitle) {
				for (int i = startCol; i < getColumnCount(); i++) {
					PgmNormalCell pnc = new PgmNormalCell();
					pnc.setExpString(getColumnName(i));
					matrix.set(0, i - startCol, pnc);
				}
			}
			Object rowData;
			List<String> messages = new ArrayList<String>();
			for (int i = 0; i < selectedRows.size(); i++) {
				rowData = seq.get(selectedRows.getInt(i) + 1);
				int row = i;
				if (copyTitle)
					row = i + 1;
				if (rowData instanceof Record) {
					Record rec = (Record) rowData;
					Object[] values = rec.getFieldValues();
					for (int c = 0; c < cc; c++) {
						if (c >= values.length)
							break;
						PgmNormalCell pnc = new PgmNormalCell();
						pnc.setValue(values[c]);
						try {
							pnc.setExpString(Variant.toExportString(values[c]));
						} catch (Exception ex) {
							if (StringUtils.isValidString(ex.getMessage()))
								if (!messages.contains(ex.getMessage()))
									messages.add(ex.getMessage());
						}

						matrix.set(row, c, pnc);
					}
				} else {
					PgmNormalCell pnc = new PgmNormalCell();
					pnc.setValue(rowData);
					try {
						pnc.setExpString(Variant.toExportString(rowData));
					} catch (Exception ex) {
						if (StringUtils.isValidString(ex.getMessage()))
							if (!messages.contains(ex.getMessage()))
								messages.add(ex.getMessage());
					}
					matrix.set(row, 0, pnc);
				}
			}
			if (!messages.isEmpty()) {
				for (int i = 0; i < messages.size(); i++) {
					System.out.println(messages.get(i));
				}
			}
		}
		GV.cellSelection = new CellSelection(matrix, cr, null);
		Clipboard cb = null;
		try {
			cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		} catch (HeadlessException e) {
			cb = null;
		}
		String strCS = GM.getCellSelectionString(matrix, false);
		if (cb != null) {
			cb.setContents(new StringSelection(strCS), null);
		}
		GV.cellSelection.systemClip = strCS;
		return true;
	}

	/**
	 * ճ������
	 */
	public void pasteValue() {
		if (!editable) {
			return;
		}
		int row = getSelectedRow();
		int col = getSelectedColumn();
		if (row == -1 || col == -1) {
			return;
		}
		acceptText();
		CellSelection cs = GV.cellSelection;
		if (cs != null) {
			setMatrix2Table(cs.matrix, row, col);
			acceptText();
			return;
		}
		String clip = GM.clipBoard();
		if (StringUtils.isValidString(clip)) {
			Matrix matrix = GM.string2Matrix(clip);
			setMatrix2Table(matrix, row, col);
			acceptText();
			return;
		}
	}

	/**
	 * ����ͼ��
	 */
	public void drawChart() {
		if (!canDrawChart())
			return;
		DialogDisplayChart ddc = null;
		if (canvas != null) {
			ddc = new DialogDisplayChart(canvas);
		} else if (value instanceof byte[]) {
			ddc = new DialogDisplayChart((byte[]) value);
		} else if (value instanceof Table) {
			ddc = new DialogDisplayChart((Table) value);
		} else if (value instanceof Sequence) {
			Sequence seq = (Sequence) value;
			Table t = seq.derive("o");
			ddc = new DialogDisplayChart(t);
		} else {
			return;
		}
		ddc.setVisible(true);
	}

	/**
	 * �鿴���ı�
	 */
	public void showText() {
		int row = getSelectedRow();
		int col = getSelectedColumn();
		if (row == -1 || col == -1) {
			return;
		}
		final Object cellVal = data.getValueAt(row, col);
		if (!StringUtils.isValidString(cellVal))
			return;
		showText(row, col, cellVal);
	}

	private void showText(int row, int col, Object cellVal) {
		DialogTextEditor dte = new DialogTextEditor(false);
		dte.setText((String) cellVal);
		dte.setVisible(true);
	}

	/**
	 * ���ø��ӻ����ݾ��󵽱��
	 * 
	 * @param matrix
	 *            ���ݾ���
	 * @param row
	 *            ��ʼ��
	 * @param col
	 *            ��ʼ��
	 */
	private void setMatrix2Table(Matrix matrix, int row, int col) {
		int rowCount = matrix.getRowSize();
		int colCount = matrix.getColSize();
		int endRow = row + rowCount;
		int endCol = Math.min(col + colCount, getColumnCount());
		for (int i = row; i < endRow; i++) {
			if (i > getRowCount() - 1) {
				addRow();
			}
			for (int j = col; j < endCol; j++) {
				Object temp = matrix.get(i - row, j - col);
				if (temp == null) {
					setValueAt(null, i, j);
					continue;
				}
				if (temp instanceof NormalCell) {
					NormalCell nc = (NormalCell) temp;
					if (nc.getValue() != null) {
						setValueAt(nc.getValue(), i, j);
					} else if (StringUtils.isValidString(nc.getExpString())) {
						setValueAt(nc.getExpString(), i, j);
					}
				} else {
					setValueAt(temp, i, j);
				}
			}
		}
	}

	/**
	 * ȡ��Ԫ��ֵ
	 */
	public Object getValueAt(int row, int col) {
		try {
			return super.getValueAt(row, col);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * ȡ�ߴ��С
	 */
	public Dimension getPreferredSize() {
		try {
			Dimension size = super.getPreferredSize();
			return size;
		} catch (Exception ex) {
			return new Dimension(1, 1);
		}
	}
}
