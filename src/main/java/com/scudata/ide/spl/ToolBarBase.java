package com.scudata.ide.spl;

import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.PrjxAppToolBar;

/**
 * ��������������ҳ��򿪣�
 *
 */
public class ToolBarBase extends PrjxAppToolBar {
	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 */
	public ToolBarBase() {
		super();
		add(getCommonButton(GC.iNEW, GC.NEW));
		add(getCommonButton(GC.iOPEN, GC.OPEN));
		setBarEnabled(false);
	}

	/**
	 * ���ù������Ƿ����
	 */
	public void setBarEnabled(boolean enabled) {
	}

	/**
	 * ִ������
	 */
	public void executeCmd(short cmdId) {
		try {
			GMSpl.executeCmd(cmdId);
		} catch (Exception e) {
			GM.showException(e);
		}
	}
}