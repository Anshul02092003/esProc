package com.scudata.ide.common.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.scudata.ide.common.GM;
import com.scudata.ide.common.resources.IdeCommonMessage;

/**
 * �����¶Ի���
 *
 */
public class DialogCheckUpdate extends RQDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 * 
	 * @param parent
	 *            ������
	 * @param isAuto
	 *            �Ƿ��Զ�����
	 */
	public DialogCheckUpdate(Frame parent) {
		super(parent, IdeCommonMessage.get().getMessage(
				"updatemanager.downloadprompt"), 350, 120);
		try {
			if (!GM.isChineseLanguage()) {
				setSize(500, 150);
			}
			init();
			GM.centerWindow(this);
		} catch (Exception e) {
			GM.showException(e);
		}
	}

	/**
	 * �����ڴ�ʱ
	 */
	protected void dialogOpened() {
		jBOK.requestFocusInWindow();
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 */
	private void init() {
		JLabel label = new JLabel(IdeCommonMessage.get().getMessage(
				"updatemanager.loadnewversion")); // ���°汾���Ƿ����أ�
		label.setFont(new Font("Dialog", Font.PLAIN, 14));
		label.setHorizontalAlignment(JLabel.CENTER);
		panelCenter.add(label, BorderLayout.CENTER);
		jBOK.setText(IdeCommonMessage.get().getMessage("button.yes"));
		jBOK.setMnemonic('Y');
		jBCancel.setText(IdeCommonMessage.get().getMessage("button.no"));
		jBCancel.setMnemonic('N');
		panelSouth.removeAll();
		panelSouth.setLayout(new GridBagLayout());
		panelSouth.add(new JPanel(), GM.getGBC(0, 1, true));
		panelSouth.add(jBOK, GM.getGBC(0, 3, false, false, 3, 10));
		panelSouth.add(jBCancel, GM.getGBC(0, 4, false, false, 3));
		panelSouth.add(new JPanel(), GM.getGBC(0, 5, false, false, 3));
	}
}
