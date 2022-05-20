package com.scudata.ide.spl.base;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.ide.common.EditListener;
import com.scudata.ide.common.GM;
import com.scudata.ide.spl.GVSpl;
import com.scudata.ide.spl.resources.IdeSplMessage;

/**
 * ֵ���
 */
public class PanelValue extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * �������
	 */
	public JScrollPane spValue;

	/**
	 * ���ؼ�
	 */
	public JTableValue tableValue;

	/**
	 * ֵ���������
	 */
	public PanelValueBar valueBar;

	/**
	 * ������
	 */
	public JScrollBar sbValue;

	/**
	 * ��ֹ�仯
	 */
	public boolean preventChange = false;
	/**
	 * ����ִ��ʱ��
	 */
	private JLabel jLTime = new JLabel();

	private JLabel jLDispRows1 = new JLabel(IdeSplMessage.get().getMessage(
			"panelvalue.disprows1"));
	private JLabel jLDispRows2 = new JLabel(IdeSplMessage.get().getMessage(
			"panelvalue.disprows2"));

	/**
	 * ��ʾ������������
	 */
	private JSpinner jSDispRows = new JSpinner(new SpinnerNumberModel(100, 1,
			Integer.MAX_VALUE, 1));

	/**
	 * �α�������ݰ�ť
	 */
	private JButton jBCursorFetch = new JButton(IdeSplMessage.get().getMessage(
			"panelvalue.cursorfetch")); // ��������

	/**
	 * ���캯��
	 */
	public PanelValue() {
		GVSpl.panelValue = this;
		this.setLayout(new BorderLayout());
		setMinimumSize(new Dimension(0, 0));
		valueBar = new PanelValueBar();
		add(valueBar, BorderLayout.NORTH);
		tableValue = new JTableValue(this);
		GM.loadWindowSize(this);
		spValue = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spValue.getViewport().add(tableValue);
		tableValue.addMWListener(spValue);
		add(spValue, BorderLayout.CENTER);
		sbValue = new JScrollBar();
		sbValue.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (preventChange)
					return;
				int index = e.getValue();
				if (index > 0) {
					tableValue.resetData(index);
				}
			}
		});
		this.add(sbValue, BorderLayout.EAST);
		tableValue.setValue(null);
		JPanel panelDebug = new JPanel(new GridBagLayout());
		panelDebug.add(jLTime, GM.getGBC(0, 0, true, false, 4));
		panelDebug.add(jLDispRows1, GM.getGBC(0, 2, false, false, 2));
		panelDebug.add(jSDispRows, GM.getGBC(0, 3, false, false, 0));
		panelDebug.add(jLDispRows2, GM.getGBC(0, 4, false, false, 2));
		panelDebug.add(jBCursorFetch, GM.getGBC(0, 5));

		panelSouth.add(CARD_DEBUG, panelDebug);
		panelSouth.add(CARD_EMPTY, new JPanel());
		cl.show(panelSouth, CARD_EMPTY);
		panelSouth.setVisible(false);
		this.add(panelSouth, BorderLayout.SOUTH);
		jBCursorFetch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object value = jSDispRows.getValue();
				if (!(value instanceof Number))
					return;
				int dispRows = ((Number) value).intValue();
				tableValue.cursorFetch(dispRows);
			}
		});
		Dimension dim = new Dimension(100, 25);
		jSDispRows.setPreferredSize(dim);
	}

	public void setCursorValue(boolean isCursor) {
		jLDispRows1.setVisible(isCursor);
		jSDispRows.setVisible(isCursor);
		jLDispRows2.setVisible(isCursor);
		jBCursorFetch.setVisible(isCursor);
	}

	/** ������� */
	private static final String CARD_DEBUG = "CARD_DEBUG";
	/** ����� */
	private static final String CARD_EMPTY = "CARD_EMPTY";
	/**
	 * �����л��Ƿ���ʾǩ���Ŀ�Ƭ����
	 */
	private CardLayout cl = new CardLayout();
	/**
	 * ������塣������ʱ�л�Ϊ�����
	 */
	private JPanel panelSouth = new JPanel(cl);

	/**
	 * ��������
	 * 
	 * @param cs
	 *            �������
	 */
	public void setCellSet(PgmCellSet cs) {
		if (cs == null) {
			panelSouth.setVisible(false);
			cl.show(panelSouth, CARD_EMPTY);
		} else {
			cl.show(panelSouth, CARD_DEBUG);
			panelSouth.setVisible(true);
		}
	}

	/**
	 * ���õ���ִ��ʱ�䣬��λ����
	 * 
	 * @param time
	 */
	public void setDebugTime(Long time) {
		if (time == null) {
			jLTime.setText(null);
		} else {
			jLTime.setText(IdeSplMessage.get().getMessage(
					"panelvalue.debugtime", time));
		}
	}

	/**
	 * ���ñ༭������
	 * 
	 * @param el
	 */
	public void setEditListener(EditListener el) {
		tableValue.setEditListener(el);
	}
}
