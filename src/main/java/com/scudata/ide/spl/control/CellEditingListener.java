package com.scudata.ide.spl.control;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import com.scudata.cellset.datamodel.CellSet;
import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.common.CellLocation;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.KeyWord;
import com.scudata.dm.Param;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.expression.CSVariable;
import com.scudata.expression.ElementRef;
import com.scudata.expression.Expression;
import com.scudata.expression.FieldId;
import com.scudata.expression.FieldRef;
import com.scudata.expression.FunctionLib;
import com.scudata.expression.Node;
import com.scudata.expression.VarParam;
import com.scudata.expression.operator.DotOperator;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.ToolBarPropertyBase;
import com.scudata.ide.common.control.ControlUtilsBase;
import com.scudata.ide.common.control.JWindowNames;
import com.scudata.ide.spl.GCSpl;
import com.scudata.ide.spl.GVSpl;
import com.scudata.ide.spl.SPL;
import com.scudata.ide.spl.SheetSpl;
import com.scudata.util.EnvUtil;

/**
 * �༭ʱ���ڵ�ǰ��Ԫ���ý���ʱ�ļ��̼�������
 */
public class CellEditingListener implements KeyListener {
	/** �༭�ؼ� */
	private SplControl control;

	/** ������� */
	private ContentPanel cp;

	/**
	 * �Ƿ���CTRL��
	 */
	private boolean isCtrlDown = false;

	/**
	 * ���������캯��
	 * 
	 * @param control �༭�ؼ�
	 * @param panel   �������
	 */
	public CellEditingListener(SplControl control, ContentPanel panel) {
		this.control = control;
		this.cp = panel;
	}

	/**
	 * �����ͷ�ʱ����༭��������������ı�
	 * 
	 * @param e �����¼�
	 */
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			isCtrlDown = false;
		}
		final JTextComponent jtext = getSource(e);
		if (jtext == null)
			return;
		if (!Character.isDefined(e.getKeyChar())) {
			return;
		}
		if (e.isControlDown() || e.isAltDown()) {
			return;
		}
		int key = e.getKeyCode();
		if (e.isShiftDown()) {
			if (!e.isActionKey() && key != KeyEvent.VK_PERIOD) {
				matchKeyWord(jtext);
			}
			return;
		}
		if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER) {
			return;
		}
		control.fireEditorInputing(jtext.getText());
		if (key == KeyEvent.VK_PERIOD) {
			startMatch(jtext);
			return;
		}
		matchKeyWord(jtext);
	}

	/**
	 * ƥ��ؼ���
	 * 
	 * @param jtext
	 */
	private void matchKeyWord(JTextComponent jtext) {
		if (GVSpl.matchWindow != null) {
			String text = jtext.getText();
			int caret = jtext.getCaretPosition();
			int dot = GVSpl.matchWindow.getDot();
			if (caret < dot || dot > text.length()) {
				stopMatch();
				return;
			}
			boolean isPeriod = GVSpl.matchWindow.isPeriod();
			int start1 = dot, start2 = dot;
			if (!isPeriod) {
				start1--;
			}
			int end = text.length();
			for (int i = start1 + 1; i < text.length(); i++) {
				char c = text.charAt(i);
				if (KeyWord.isSymbol(c)) {
					end = i;
					break;
				}
			}
			String name = text.substring(start2, end);
			name = name.trim();
			if (GVSpl.matchWindow != null) {
				boolean isSearched = GVSpl.matchWindow.searchName(name);
				if (!isSearched) {
					stopMatch();
				}
			}
		}
	}

	/**
	 * ��������ʱ�Ĵ�������������µ������¼����س�����Ctrl+���Ҽ�����Ӧ�ı䵱ǰ��Ԫ��
	 * 
	 * @param e �����¼�
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		boolean isMatching = GVSpl.matchWindow != null && GVSpl.matchWindow.isVisible();
		switch (key) {
		case KeyEvent.VK_ENTER:
			if (!GV.isCellEditing) {// ���Թ���������
				stopMatch();
				return;
			}
			if (e.isAltDown()) {
				stopMatch();
			} else if (e.isControlDown()) {
				stopMatch();
				JTextComponent ta = getSource(e);
				int c = ta.getCaretPosition();
				try {
					String head = ta.getText(0, c);
					String end = ta.getText(c, ta.getText().length() - c);
					cp.setEditorText(head + "\n" + end);
					ta.requestFocus();
					ta.setCaretPosition(c + 1);
				} catch (BadLocationException ex) {
				} catch (IllegalArgumentException ex1) {
				}
			} else if (e.isShiftDown()) {
				stopMatch();
				// �������ݡ�ִ�У������Ƿ���ʵ�ʸĶ�������ʾ������Զ���ס�����ƶ����
				((SheetSpl) GVSpl.appSheet).calcActiveCell();
				break;
			} else {
				if (isMatching) {
					if (GVSpl.matchWindow != null) {
						GVSpl.matchWindow.selectName();
					}
					isMatching = false;
					break;
				}
				CellSetParser parser = new CellSetParser(control.cellSet);
				PgmNormalCell cell;
				int nextCol = -1;
				CellLocation cl = control.getActiveCell();
				if (cl == null)
					return;
				int curRow = cl.getRow();
				int curCol = cl.getCol();
				for (int c = curCol + 1; c <= control.cellSet.getColCount(); c++) {
					if (parser.isColVisible(c)) {
						cell = control.cellSet.getPgmNormalCell(curRow, c);
						if (!cell.isNoteBlock() && !cell.isNoteCell()) {
							if (StringUtils.isValidString(parser.getDispText(curRow, c))) {
								nextCol = c;
								break;
							}
						}
					}
				}
				if (nextCol > 0) {
					control.getContentPanel().submitEditor();
					control.getContentPanel().revalidate();
					control.scrollToArea(control.setActiveCell(new CellLocation(curRow, nextCol), true));
				} else {
					if (control.getActiveCell() != null) {
						CellSet ics = control.getCellSet();
						if (curRow == ics.getRowCount()) {
							control.getContentPanel().submitEditor();
							SplEditor editor = ControlUtils.extractSplEditor(control);
							control.getContentPanel().revalidate();
							editor.appendRows(1);
						}
					}
					control.scrollToArea(control.toDownCell());
				}

				GVSpl.panelValue.tableValue.setLocked(false);
			}
			break;
		case KeyEvent.VK_ESCAPE:
			if (control.getActiveCell() == null) {
				break;
			}
			if (isMatching) {
				stopMatch();
			}
			JTextComponent ta = getSource(e);
			NormalCell nc = (NormalCell) control.getCellSet().getCell(control.getActiveCell().getRow(),
					control.getActiveCell().getCol());
			String value = nc.getExpString();
			value = value == null ? GCSpl.NULL : value;
			ta.setText(value);
			control.getContentPanel().reloadEditorText();
			control.fireEditorInputing(value);
			cp.requestFocus();
			break;
		case KeyEvent.VK_TAB:
			if (e.isShiftDown()) {
				if (isMatching) {
					stopMatch();
				}
				control.scrollToArea(control.toLeftCell());
			} else if (e.isControlDown()) {
				// CTRL-TAB���ͳ��л���ǰ�SHEET������EXCEL��
				if (isMatching) {
					stopMatch();
				}
				((SPL) GVSpl.appFrame).showNextSheet(isCtrlDown);
				isCtrlDown = true;
			} else {
				if (isMatching) {
					break;
				}
				if (control.getActiveCell() != null) {
					control.getContentPanel().submitEditor();
				}
				int curCol = control.getActiveCell().getCol();
				CellSet ics = control.getCellSet();
				if (curCol == ics.getColCount()) {
					control.getContentPanel().submitEditor();
					ControlUtils.extractSplEditor(control).appendCols(1);
				}
				control.scrollToArea(control.toRightCell());
			}
			break;
		case KeyEvent.VK_RIGHT:
			JTextComponent tar = getSource(e);
			if (tar.getText() == null || tar.getText().equals("")) {
				control.scrollToArea(control.toRightCell());
			} else {
				return;
			}
			break;
		case KeyEvent.VK_LEFT:
			JTextComponent tal = getSource(e);
			if (tal.getText() == null || tal.getText().equals("")) {
				control.scrollToArea(control.toLeftCell());
			} else {
				return;
			}
			break;
		case KeyEvent.VK_UP:
			if (isMatching) {
				if (GVSpl.matchWindow != null) {
					GVSpl.matchWindow.selectBefore();
				}
			}
			if (e.isAltDown()) {
				return;
			}
			JTextComponent tau = getSource(e);
			if (tau.getText() == null || tau.getText().equals("")) {
				control.scrollToArea(control.toUpCell());
			} else {
				return;
			}
			break;
		case KeyEvent.VK_DOWN:
			if (isMatching) {
				if (GVSpl.matchWindow != null) {
					GVSpl.matchWindow.selectNext();
				}
			}
			if (e.isAltDown()) {
				return;
			}
			JTextComponent tad = getSource(e);
			if (tad.getText() == null || tad.getText().equals("")) {
				control.scrollToArea(control.toDownCell());
			} else if (e.isShiftDown()) {
				startMatch(tad);
				return;
			} else {
				return;
			}
			break;
		case KeyEvent.VK_F2: {
			if (control.getActiveCell() == null) {
				break;
			}
			if (isMatching) {
				stopMatch();
			}
			GVSpl.toolBarProperty.getWindowEditor().requestFocus();
			break;
		}
		case KeyEvent.VK_Z: {
			if (e.isControlDown() && control.getActiveCell() != null) {
				if (isMatching) {
					stopMatch();
				}
				control.getContentPanel().undoEditor();
			}
			break;
		}
		default:
			return;
		}
		e.consume();
	}

	/**
	 * ��ʼƥ��
	 * 
	 * @param jtext
	 * @return
	 */
	private boolean startMatch(final JTextComponent jtext) {
		if (GVSpl.matchWindow != null) {
			stopMatch();
		}
		GVSpl.matchWindow = null;
		try {
			if (jtext == null)
				return false;
			CellLocation cl = control.getActiveCell();
			String text = jtext.getText();
			if (!StringUtils.isValidString(text) || text.startsWith("/")) {
				return false;
			}
			final int p = jtext.getCaretPosition();
			if (p <= 0)
				return false;
			String preStr = text.substring(0, p);
			while (preStr != null && (preStr.startsWith("=") || preStr.startsWith(">") || preStr.startsWith("/"))) {
				preStr = preStr.substring(1);
			}
			if (!StringUtils.isValidString(preStr)) {
				return false;
			}
			boolean isPeriod = false;
			char preChar = text.charAt(p - 1);
			if (preChar == '.') {
				isPeriod = true;
				preStr = preStr.substring(0, preStr.length() - 1);
			}
			Context ctx = control.cellSet.getContext();
			Object val = null;
			if (isPeriod) {
				preStr = getMainObj(preStr);
			} else {
				// �ں����������ﰴshift-down�����ҵ�������".func("����".func@opt("
				int funcEnd = -1;
				boolean isFunc = false;
				for (int i = preStr.length() - 1; i >= 0; i--) {
					char c = preStr.charAt(i);
					if (c == '(') {
						funcEnd = i;
					} else if (c == '.') {
						if (funcEnd < 0) {
							continue;
						}
						String func = preStr.substring(i + 1, funcEnd);
						int optIndex = func.indexOf("@");
						if (optIndex > 0) {
							func = func.substring(0, optIndex);
						}
						if (FunctionLib.isMemberFnName(func)) {
							int scount = 1;
							for (int k = funcEnd + 2; k < text.length(); k++) {
								c = text.charAt(k);
								if (c == ')') {
									scount--;
								} else if (c == '(') {
									scount++;
								}
								if (scount == 0) {
									if (p > k) {
										return false;
									}
								}
							}
							preStr = getMainObj(preStr.substring(0, i));
							isFunc = true;
							break;
						}
					}
				}
				if (!isFunc && funcEnd > -1) {
					int scount = 1;
					for (int i = funcEnd + 1; i < text.length(); i++) {
						char c = text.charAt(i);
						if (c == ')') {
							scount--;
						} else if (c == '(') {
							scount++;
						}
						if (scount == 0) {
							if (p > i) {
								return false;
							}
						}
					}
					preStr = getMainObj(preStr.substring(0, funcEnd));
				}
			}
			if (preStr == null) {
				return false;
			}
			// "A.F."����"A(1)."���������Ҫ����
			if (preStr.indexOf(".") > 0 || preStr.indexOf("(") > 0) {
				Expression exp = new Expression(control.cellSet, ctx, preStr);
				Object home = exp.getHome();
				if (home instanceof DotOperator) {
					DotOperator homeNode = (DotOperator) home;
					Node right = homeNode.getRight();
					if (right instanceof FieldRef || right instanceof FieldId) {
						if (isValidNode(homeNode)) {
							val = exp.calculate(ctx);
						}
					}
				} else if (home instanceof ElementRef) {
					ElementRef homeNode = (ElementRef) home;
					if (isValidNode(homeNode)) {
						val = exp.calculate(ctx);
					}
				}
			} else {
				// ���������Ƿ����
				cl = CellLocation.parse(preStr);
				if (cl != null) {
					PgmNormalCell cell = control.cellSet.getPgmNormalCell(cl.getRow(), cl.getCol());
					val = cell.getValue(false);
				} else {
					// �ٿ��������Ƿ����
					Param param = EnvUtil.getParam(preStr, ctx);
					if (param != null)
						val = param.getValue();
				}
			}

			return showMatchWindow(jtext, p, isPeriod, val);
		} catch (Throwable t) {
		}
		return false;
	}

	/**
	 * ȡ������A.F,A.F.F,A(1).F
	 * 
	 * @param preStr
	 * @return
	 */
	private String getMainObj(String preStr) {
		int start = 0;
		int brackets = 0;
		int memberEnd = -1;
		for (int i = preStr.length() - 1; i >= 0; i--) {
			char c = preStr.charAt(i);
			if (c == ')') {
				if (i < preStr.length() - 1) {
					char c1 = preStr.charAt(i + 1);
					if (c1 != '.')
						return null;
				}
				brackets++;
			} else if (c == '(') {
				brackets--;
				if (brackets < 0) {
					start = i + 1;
					break;
				} else if (brackets == 0) {
					memberEnd = i;
				}
			} else if (c == '.') {
				if (memberEnd > -1) { // �������ǰ����Ƿ���
					String member = preStr.substring(i + 1, memberEnd);
					if (FunctionLib.isMemberFnName(member))
						return null;
				}
				memberEnd = -1;
			} else if (KeyWord.isSymbol(c)) {
				start = i + 1;
				break;
			}
		}
		return preStr.substring(start);
	}

	/**
	 * ��ʾƥ�䴰��
	 * 
	 * @param jtext
	 * @param p
	 * @param isPeriod
	 * @param val
	 * @return
	 * @throws BadLocationException
	 */
	private boolean showMatchWindow(final JTextComponent jtext, final int p, boolean isPeriod, Object val)
			throws BadLocationException {
		if (val == null)
			return false;
		DataStruct ds = null;
		if (val instanceof Sequence) {
			ds = ((Sequence) val).dataStruct();
		} else if (val instanceof Record) {
			ds = ((Record) val).dataStruct();
		}
		if (ds != null) {
			String[] fieldNames = ds.getFieldNames();
			if (fieldNames == null || fieldNames.length == 0) {
				return false;
			}
			GVSpl.matchWindow = new JWindowNames(fieldNames, p, isPeriod) {
				private static final long serialVersionUID = 1L;

				public void selectName(String name) {
					int dot = jtext.getCaretPosition();
					int start = GVSpl.matchWindow.getDot();
					jtext.setSelectionStart(start);
					jtext.setSelectionEnd(dot);
					GM.addText(jtext, name);
					dispose();
					jtext.requestFocus();
				}
			};
			int x = GM.getAbsolutePos(jtext, true);
			int y = GM.getAbsolutePos(jtext, false);
			FontMetrics fmText = jtext.getFontMetrics(jtext.getFont());
			if (jtext instanceof JTextArea) {
				JTextArea jta = (JTextArea) jtext;
				for (int r = 0; r < jta.getRows(); r++) {
					int lineEnd = jta.getLineEndOffset(r);
					if (lineEnd >= p) {
						y += (r + 1) * (jta.getHeight() / jta.getRows());
						int lineStart = jta.getLineStartOffset(r);
						x += fmText.stringWidth(jta.getText(lineStart, p - lineStart) + ".");
						break;
					}
				}
			} else if (jtext instanceof JTextPane) {
				String preStr = jtext.getText().substring(0, p);
				int dx = jtext.getVisibleRect().x;
				int dy = jtext.getVisibleRect().y;
				CellLocation activeCell = control.getActiveCell();
				int cellW = control.cellW[activeCell.getCol()];
				if (ConfigOptions.bDispOutCell.booleanValue()) {
					cellW = control.contentView.getPaintableWidth(activeCell.getRow(), activeCell.getCol());
				}
				int rowHeight = fmText.getHeight();
				ArrayList<String> rows = ControlUtilsBase.wrapString(preStr, fmText, cellW);
				y += rows.size() * rowHeight;
				x += fmText.stringWidth((String) rows.get(rows.size() - 1) + ".");
				x -= dx;
				y -= dy;

				x += 2;
				y += 2;
			}

			Dimension d = GV.appFrame.getSize();
			int maxX = d.width;
			int maxY = d.height;
			final int MAX_WIDTH = 300;
			final int MAX_HEIGHT = 220;
			int w = 150;
			FontMetrics fmWindow = GVSpl.matchWindow.getFontMetrics(GVSpl.matchWindow.getFont());
			for (String name : fieldNames) {
				if (StringUtils.isValidString(name)) {
					w = Math.max(w, fmWindow.stringWidth(name));
					if (w >= MAX_WIDTH) {
						w = MAX_WIDTH;
						break;
					}
				}
			}
			final int ROW_HEIGHT = 22;
			final int h = Math.min(ROW_HEIGHT * fieldNames.length, MAX_HEIGHT);

			if (x + w > maxX) { // �ұ߳���
				if (x > w) { // ��߹���
					x -= w;
				} else { // ���Ҳ������������ʾ���ұ�
					x = maxX - w;
				}
			}
			if (y + h > maxY) { // �±߲����ߣ�15��������
				y -= (y + h - maxY) + 15;
			}
			GVSpl.matchWindow.setBounds(x, y, w, h);
			GVSpl.matchWindow.setVisible(true);
			jtext.requestFocus();
			return true;
		}
		return false;
	}

	/**
	 * ֹͣƥ��
	 */
	private void stopMatch() {
		if (GVSpl.matchWindow != null) {
			GVSpl.matchWindow.dispose();
			GVSpl.matchWindow = null;
		}
	}

	/**
	 * �Ƿ�Ϸ��Ľ��
	 * 
	 * @param node
	 * @return
	 */
	private boolean isValidNode(Node node) {
		if (node == null)
			return false;
		Node left = node.getLeft();
		while (left != null) {
			if (!(left instanceof VarParam) && !(left instanceof CSVariable) && !(left instanceof DotOperator)
					&& !(left instanceof ElementRef)) {
				return false;
			}
			left = left.getLeft();
		}
		return true;
	}

	/**
	 * ���̰����¼�
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * ȡԴ���
	 * 
	 * @param e
	 * @return
	 */
	private JTextComponent getSource(KeyEvent e) {
		Object src = e.getSource();
		if (src instanceof JTextComponent) {
			return (JTextComponent) src;
		}
		if (src instanceof ToolBarPropertyBase) {
			return ((ToolBarPropertyBase) src).getWindowEditor();
		}
		return null;
	}
}
