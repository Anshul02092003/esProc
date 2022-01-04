package com.scudata.ide.common.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JWindow;

import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.swing.JListEx;

/**
 * ������ʾ�����б�
 *
 */
public abstract class JWindowNames extends JWindow {
	private static final long serialVersionUID = 1L;
	/**
	 * �б�ؼ�
	 */
	private JListEx listWindow = new JListEx();
	/**
	 * ����ɫ
	 */
	private final Color BACK_COLOR = new Color(255, 255, 214);
	/**
	 * ����
	 */
	private Vector<String> nameList;
	/**
	 * ����λ��
	 */
	private int dot;
	/**
	 * ǰ׺�Ƿ�"."
	 */
	private boolean isPeriod;
	/**
	 * �������ؼ�
	 */
	private JScrollPane jSPWin;

	/**
	 * ���캯��
	 * 
	 * @param names
	 *            ��������
	 * @param dot
	 *            ����λ��
	 * @param isPeriod
	 *            ǰ׺�Ƿ�"."
	 */
	public JWindowNames(String[] names, int dot, boolean isPeriod) {
		super(GV.appFrame);
		this.dot = dot;
		this.isPeriod = isPeriod;
		jSPWin = new JScrollPane(listWindow);
		getContentPane().add(jSPWin, BorderLayout.CENTER);
		setFocusable(true);
		String[] cloneNames = new String[names.length];
		System.arraycopy(names, 0, cloneNames, 0, names.length);
		names = cloneNames;
		Arrays.sort(names);
		nameList = new Vector<String>();
		for (String s : names) {
			nameList.add(s);
		}
		listWindow.x_setData(nameList, nameList);
		listWindow.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				e.consume();
			}

			public void mouseReleased(MouseEvent e) {
				e.consume();
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				if (e.getClickCount() == 2) {
					if (listWindow.isSelectionEmpty())
						return;
					try {
						selectName((String) listWindow.getSelectedValue());
						e.consume();
					} catch (Exception e1) {
						GM.showException(e1);
					}
				}
			}
		});
		this.setBackground(BACK_COLOR);
		listWindow.setBackground(BACK_COLOR);
		jSPWin.setBackground(BACK_COLOR);

		KeyListener kl = new KeyAdapter() {

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					e.consume();
					dispose();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (listWindow.isSelectionEmpty()) {
						return;
					}
					selectName((String) listWindow.getSelectedValue());
					e.consume();
				}
			}

		};

		listWindow.addKeyListener(kl);
		jSPWin.addKeyListener(kl);
		this.addKeyListener(kl);
		listWindow.setSelectedIndex(0);
		this.addWindowListener(new WindowAdapter() {

			public void windowClosed(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
				listWindow.requestFocusInWindow();
			}
		});
	}

	/**
	 * ���ý���
	 */
	public void setFocused() {
		this.requestFocus();
		listWindow.requestFocus();
	}

	/**
	 * ��ȡ���λ��
	 * 
	 * @return
	 */
	public int getDot() {
		return dot;
	}

	/**
	 * �Ƿ�ǰ׺��"."
	 * 
	 * @return
	 */
	public boolean isPeriod() {
		return isPeriod;
	}

	/**
	 * ��ȡѡ�е�����
	 * 
	 * @return
	 */
	public String getSelectedName() {
		return (String) listWindow.getSelectedValue();
	}

	/**
	 * ѡ������
	 */
	public void selectName() {
		selectName(getSelectedName());
	}

	/**
	 * ѡ��ǰһ��
	 */
	public void selectBefore() {
		int index = listWindow.getSelectedIndex();
		if (index < 0) {
			index = 1;
		}
		if (index > 0) {
			listWindow.setSelectedIndex(index - 1);
			listWindow.requestFocus();
		}
	}

	/**
	 * ѡ����һ��
	 */
	public void selectNext() {
		int index = listWindow.getSelectedIndex();
		if (index < 0) {
			index = 0;
		}
		if (index < nameList.size() - 1) {
			listWindow.setSelectedIndex(index + 1);
			listWindow.requestFocus();
		}
	}

	/**
	 * ��������
	 * 
	 * @param pre
	 *            ����������ǰ׺
	 * @return
	 */
	public boolean searchName(String pre) {
		if (pre == null)
			return false;
		pre = pre.toLowerCase();
		for (int i = 0; i < nameList.size(); i++) {
			String val = nameList.get(i);
			if (val != null) {
				if (val.toLowerCase().startsWith(pre)) {
					listWindow.setSelectedIndex(i);
					int max = jSPWin.getVerticalScrollBar().getMaximum();
					int value = max * i / nameList.size();
					jSPWin.getVerticalScrollBar().setValue(value);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ѡ��������
	 * 
	 * @param name
	 *            ѡ�е�����
	 */
	public abstract void selectName(String name);
}