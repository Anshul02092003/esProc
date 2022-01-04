package com.scudata.ide.common.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.scudata.common.MessageManager;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.resources.IdeCommonMessage;
import com.scudata.ide.common.swing.VFlowLayout;

/**
 * ��������Ի���
 *
 */
public class DialogInputPassword extends JDialog {
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
	 * ��������
	 */
	private JLabel jLabel1 = new JLabel();
	/**
	 * ����༭��
	 */
	private JPasswordField jPF1 = new JPasswordField();
	/**
	 * ȷ������
	 */
	private JLabel jLabel2 = new JLabel();
	/**
	 * ����༭��
	 */
	private JPasswordField jPF2 = new JPasswordField();
	/**
	 * �˳�ѡ��
	 */
	private int m_option = JOptionPane.CLOSED_OPTION;
	/**
	 * �Ƿ�ֻ����һ������
	 */
	private boolean single = false;
	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/**
	 * ���캯��
	 */
	public DialogInputPassword() {
		this(false);
	}

	/**
	 * ���캯��
	 * 
	 * @param single
	 *            �Ƿ�ֻ����һ������
	 */
	public DialogInputPassword(boolean single) {
		super(GV.appFrame, "��������", true);
		try {
			this.single = single;
			setSize(350, 95);
			initUI();
			resetText();
			init();
			this.setResizable(false);
			GM.setDialogDefaultButton(this, jBOK, jBCancel);
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	/**
	 * ����������Դ
	 */
	private void resetText() {
		this.setTitle(mm.getMessage("dialoginputpassword.title")); // ������������
		jBOK.setText(mm.getMessage("button.ok"));
		jBCancel.setText(mm.getMessage("button.cancel"));
		jLabel1.setText(mm.getMessage("dialoginputpassword.inputpsw")); // ��������
		jLabel2.setText(mm.getMessage("dialoginputpassword.confpsw")); // ȷ������
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
	 * ȡ����
	 * 
	 * @return
	 */
	public String getPassword() {
		return jPF1.getPassword() == null ? null : new String(
				jPF1.getPassword());
	}

	/**
	 * ��������
	 * 
	 * @param psw
	 */
	public void setPassword(String psw) {
		jPF1.setText(psw);
		jPF2.setText(psw);
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		if (single) {
			jLabel2.setVisible(false);
			jPF2.setVisible(false);
			this.setTitle(mm.getMessage("dialoginputpassword.inputcspsw")); // ������������
		}
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void initUI() throws Exception {
		JPanel jPanel1 = new JPanel();
		JPanel jPanel2 = new JPanel();
		VFlowLayout vFlowLayout1 = new VFlowLayout();
		GridBagLayout gridBagLayout1 = new GridBagLayout();
		jPanel1.setLayout(vFlowLayout1);
		jBOK.setMnemonic('O');
		jBOK.setText("ȷ��(O)");
		jBOK.addActionListener(new DialogInputPassword_jBOK_actionAdapter(this));
		jBCancel.setMnemonic('C');
		jBCancel.setText("ȡ��(C)");
		jBCancel.addActionListener(new DialogInputPassword_jBCancel_actionAdapter(
				this));
		jPanel2.setLayout(gridBagLayout1);
		jLabel1.setText("��������");
		jLabel2.setText("ȷ������");
		this.addWindowListener(new DialogInputPassword_this_windowAdapter(this));
		this.getContentPane().add(jPanel1, BorderLayout.EAST);
		jPanel1.add(jBOK, null);
		jPanel1.add(jBCancel, null);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jLabel1, GM.getGBC(1, 1));
		jPanel2.add(jPF1, GM.getGBC(1, 2, true));
		jPanel2.add(jLabel2, GM.getGBC(2, 1));
		jPanel2.add(jPF2, GM.getGBC(2, 2, true));
	}

	/**
	 * ȷ�ϰ�ť�¼�
	 * 
	 * @param e
	 */
	void jBOK_actionPerformed(ActionEvent e) {
		if (single) {
			if (jPF1.getPassword() == null) {
				JOptionPane.showMessageDialog(GV.appFrame,
						mm.getMessage("dialoginputpassword.emptypsw")); // �������������롣
				return;
			}
		} else {
			String psw1 = jPF1.getPassword() == null ? null : new String(
					jPF1.getPassword());
			String psw2 = jPF2.getPassword() == null ? null : new String(
					jPF2.getPassword());
			if (psw1 == null) {
				if (psw2 != null) {
					JOptionPane.showMessageDialog(GV.appFrame,
							mm.getMessage("dialoginputpassword.diffpsw")); // ������������벻ͬ�����������롣
					return;
				}
			} else {
				if (!psw1.equals(psw2)) {
					JOptionPane.showMessageDialog(GV.appFrame,
							mm.getMessage("dialoginputpassword.diffpsw"));
					return;
				}
			}
		}
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

	/**
	 * ɾ����ť�¼�
	 * 
	 * @param e
	 */
	void jBDelete_actionPerformed(ActionEvent e) {
		jPF1.setText(null);
		jPF2.setText(null);
	}
}

class DialogInputPassword_jBOK_actionAdapter implements
		java.awt.event.ActionListener {
	DialogInputPassword adaptee;

	DialogInputPassword_jBOK_actionAdapter(DialogInputPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBOK_actionPerformed(e);
	}
}

class DialogInputPassword_jBCancel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogInputPassword adaptee;

	DialogInputPassword_jBCancel_actionAdapter(DialogInputPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBCancel_actionPerformed(e);
	}
}

class DialogInputPassword_this_windowAdapter extends
		java.awt.event.WindowAdapter {
	DialogInputPassword adaptee;

	DialogInputPassword_this_windowAdapter(DialogInputPassword adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}
