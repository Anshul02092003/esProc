package com.scudata.ide.common.swing;

import javax.swing.DefaultCellEditor;

/**
 * JTable��Ԫ��������༭��
 *
 */
public class JComboBoxExEditor extends DefaultCellEditor {

	private static final long serialVersionUID = 1L;

	/**
	 * ������ؼ�
	 */
	JComboBoxEx combo;

	/**
	 * ���캯��
	 * 
	 * @param cbe
	 *            ������ؼ�
	 */
	public JComboBoxExEditor(JComboBoxEx cbe) {
		super(cbe);
		combo = cbe;
	}

	/**
	 * ȡ�༭ֵ
	 */
	public Object getCellEditorValue() {
		return combo.x_getSelectedItem();
	}

	/**
	 * ȡ�ؼ�
	 * 
	 * @return
	 */
	public JComboBoxEx getJComboBoxEx() {
		return combo;
	}

}
