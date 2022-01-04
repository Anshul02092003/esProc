package com.scudata.ide.spl.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.MessageManager;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.resources.IdeCommonMessage;
import com.scudata.ide.common.swing.VFlowLayout;

/**
 * ����Ի���
 *
 */
public class DialogPassword extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * ȷ�ϰ�ť
	 */
	private JButton jBOK = new JButton();
	/**
	 * ȡ����ť
	 */
	private JButton jBCancel = new JButton();
	/**
	 * ������Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();
	/** ��ȫ����Ȩ */
	private final String STR_FULL = mm.getMessage("dialoginputpassword.full");
	/** ִ��Ȩ */
	private final String STR_EXE = mm.getMessage("dialoginputpassword.exe");

	/** ��ȫ����Ȩ����ֵ */
	private final int INDEX_FULL = 0;
	/** ִ��Ȩ����ֵ */
	private final int INDEX_EXE = 1;

	/**
	 * �˳�ѡ��
	 */
	private int m_option = JOptionPane.CLOSED_OPTION;
	/**
	 * �������
	 */
	private PgmCellSet cellSet;
	/**
	 * ����༭��
	 */
	private JPasswordField jPF1 = new JPasswordField();
	/**
	 * ȷ������༭��
	 */
	private JPasswordField jPF2 = new JPasswordField();
	// private JTextField textPsw = new JTextField();
	/**
	 * ���ܼ���������
	 */
	private JComboBox cbLevel;

	/**
	 * ���캯��
	 */
	public DialogPassword() {
		super(GV.appFrame, "��������", true);
		try {
			init();
			setSize(350, 120);
			resetText();
			this.setResizable(false);
			GM.setDialogDefaultButton(this, jBOK, jBCancel);
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	/**
	 * �����������
	 * 
	 * @param cellSet
	 *            �������
	 */
	public void setCellSet(PgmCellSet cellSet) {
		this.cellSet = cellSet;
		int cp = cellSet.getNullPasswordPrivilege();
		if (cp == PgmCellSet.PRIVILEGE_FULL) {
			cbLevel.setSelectedIndex(INDEX_FULL);
		} else {
			cbLevel.setSelectedIndex(INDEX_EXE);
		}
	}

	/**
	 * ����������Դ
	 */
	private void resetText() {
		this.setTitle(mm.getMessage("dialoginputpassword.title")); // ������������
		jBOK.setText(mm.getMessage("button.ok"));
		jBCancel.setText(mm.getMessage("button.cancel"));
	}

	/**
	 * ȡ�˳�ѡ��
	 * 
	 * @return
	 */
	public int getOption() {
		return m_option;
	}

	/**
	 * ��ʼ����ť
	 * 
	 * @param b
	 */
	public void initButton(JButton b) {
		b.setIcon(getLockIcon(false));
		Dimension d = new Dimension(60, 60);
		b.setMaximumSize(d);
		b.setMinimumSize(d);
		b.setPreferredSize(d);
	}

	/**
	 * ȡ����ͼ��
	 * 
	 * @param locked
	 * @return
	 */
	public ImageIcon getLockIcon(boolean locked) {
		String image = locked ? "b_lock.png" : "b_unlock.png";
		return GM.getImageIcon(GC.IMAGES_PATH + image);
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		JPanel jPanel1 = new JPanel();
		VFlowLayout VFlowLayout1 = new VFlowLayout();
		jPanel1.setLayout(VFlowLayout1);
		jBOK.setMnemonic('O');
		jBOK.setText("ȷ��(O)");
		jBOK.addActionListener(new DialogPassword_jBOK_actionAdapter(this));
		jBCancel.setMnemonic('C');
		jBCancel.setText("ȡ��(C)");
		jBCancel.addActionListener(new DialogPassword_jBCancel_actionAdapter(
				this));
		this.addWindowListener(new DialogPassword_this_windowAdapter(this));
		JPanel panelCenter = new JPanel(new GridBagLayout());
		this.getContentPane().add(jPanel1, BorderLayout.EAST);
		jPanel1.add(jBOK, null);
		jPanel1.add(jBCancel, null);
		this.getContentPane().add(panelCenter, BorderLayout.CENTER);
		Object[] levels = new Object[] { STR_FULL, STR_EXE };
		cbLevel = new JComboBox(levels);
		panelCenter.add(
				new JLabel(mm.getMessage("dialoginputpassword.inputpsw")),
				GM.getGBC(0, 0));
		panelCenter.add(jPF1, GM.getGBC(0, 1, true));
		panelCenter.add(
				new JLabel(mm.getMessage("dialoginputpassword.confpsw")),
				GM.getGBC(1, 0));
		panelCenter.add(jPF2, GM.getGBC(1, 1, true));
		panelCenter.add(
				new JLabel(mm.getMessage("dialoginputpassword.nopswallow")),
				GM.getGBC(2, 0));
		panelCenter.add(cbLevel, GM.getGBC(2, 1, true));
		jPF1.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (jPF1.getPassword() != null) {
					if (!cbLevel.isEnabled())
						cbLevel.setEnabled(true);
					if (cbLevel.getSelectedIndex() != INDEX_EXE)
						cbLevel.setSelectedIndex(INDEX_EXE);
				} else {
					if (cbLevel.isEnabled())
						cbLevel.setEnabled(false);
					if (cbLevel.getSelectedIndex() != INDEX_FULL)
						cbLevel.setSelectedIndex(INDEX_FULL);
				}
			}
		});
		cbLevel.setEnabled(false);
		cbLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int level = cbLevel.getSelectedIndex();
				if (level == INDEX_FULL) {
					jPF1.setText(null);
					if (cbLevel.isEnabled())
						cbLevel.setEnabled(false);
				}
			}
		});
	}

	/**
	 * ȷ�ϰ�ť�¼�
	 * 
	 * @param e
	 */
	void jBOK_actionPerformed(ActionEvent e) {
		String psw1 = jPF1.getPassword() == null ? null : new String(
				jPF1.getPassword());
		String psw2 = jPF2.getPassword() == null ? null : new String(
				jPF2.getPassword());
		if (psw1 == null) {
			if (psw2 != null) {
				// ������������벻ͬ�����������롣
				JOptionPane.showMessageDialog(GV.appFrame,
						mm.getMessage("dialoginputpassword.diffpsw"));
				return;
			}
		} else {
			if (!psw1.equals(psw2)) {
				JOptionPane.showMessageDialog(GV.appFrame,
						mm.getMessage("dialoginputpassword.diffpsw"));
				return;
			}
		}
		cellSet.setPassword(psw1);
		int level = cbLevel.getSelectedIndex();
		cellSet.setNullPasswordPrivilege(level == INDEX_FULL ? PgmCellSet.PRIVILEGE_FULL
				: PgmCellSet.PRIVILEGE_EXEC);
		m_option = JOptionPane.OK_OPTION;
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * ȡ����ť�¼�
	 * 
	 * @param e
	 */
	void jBCancel_actionPerformed(ActionEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * ���ڹر��¼�
	 * 
	 * @param e
	 */
	void this_windowClosing(WindowEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}
}

class DialogPassword_jBOK_actionAdapter implements
		java.awt.event.ActionListener {
	DialogPassword adaptee;

	DialogPassword_jBOK_actionAdapter(DialogPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBOK_actionPerformed(e);
	}
}

class DialogPassword_jBCancel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogPassword adaptee;

	DialogPassword_jBCancel_actionAdapter(DialogPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBCancel_actionPerformed(e);
	}
}

class DialogPassword_this_windowAdapter extends java.awt.event.WindowAdapter {
	DialogPassword adaptee;

	DialogPassword_this_windowAdapter(DialogPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}