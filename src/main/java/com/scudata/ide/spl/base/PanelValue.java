package com.scudata.ide.spl.base;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

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
	private JLabel labelTime = new JLabel();

	/**
	 * ���캯��
	 */
	public PanelValue() {
		GVSpl.panelValue = this;
		this.setLayout(new BorderLayout());
		setMinimumSize(new Dimension(0, 0));
		valueBar = new PanelValueBar();
		add(valueBar, BorderLayout.NORTH);
		tableValue = new JTableValue();
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
		JPanel panelSign = new JPanel(new GridBagLayout());
		panelSign.add(labelTime, GM.getGBC(0, 0, true, false, 4));
		panelSouthSign.add(CARD_SIGN, panelSign);
		panelSouthSign.add(CARD_EMPTY, new JPanel());
		cl.show(panelSouthSign, CARD_EMPTY);
		panelSouthSign.setVisible(false);
		this.add(panelSouthSign, BorderLayout.SOUTH);
	}

	/** ǩ����� */
	private static final String CARD_SIGN = "CARD_SIGN";
	/** ����� */
	private static final String CARD_EMPTY = "CARD_EMPTY";
	/**
	 * �����л��Ƿ���ʾǩ���Ŀ�Ƭ����
	 */
	private CardLayout cl = new CardLayout();
	/**
	 * ǩ����塣����ʾǩ��ʱ�л��ƿ����
	 */
	private JPanel panelSouthSign = new JPanel(cl);

	/**
	 * ��������
	 * 
	 * @param cs
	 *            �������
	 */
	public void setCellSet(PgmCellSet cs) {
		if (cs == null) {
			panelSouthSign.setVisible(false);
			cl.show(panelSouthSign, CARD_EMPTY);
		} else {
			cl.show(panelSouthSign, CARD_SIGN);
			panelSouthSign.setVisible(true);
		}
	}

	/**
	 * ���õ���ִ��ʱ�䣬��λ����
	 * 
	 * @param time
	 */
	public void setDebugTime(Long time) {
		if (time == null) {
			labelTime.setText(null);
		} else {
			labelTime.setText(IdeSplMessage.get().getMessage(
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
