package com.scudata.ide.spl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.scudata.common.MessageManager;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.PrjxAppMenu;
import com.scudata.ide.spl.resources.IdeSplMessage;

/**
 * �����˵������ļ���ʱ��
 *
 */
public class MenuBase extends PrjxAppMenu {
	private static final long serialVersionUID = 1L;

	/**
	 * ��������Դ������
	 */
	protected MessageManager mm = IdeSplMessage.get();

	/**
	 * ���캯��
	 */
	public MenuBase() {
		init();
	}

	/**
	 * ��ʼ���˵�
	 */
	protected void init() {
		JMenu menu;
		JMenuItem menuTemp;
		// �ļ��˵���
		menu = getCommonMenuItem(GC.FILE, 'F', true);
		menu.add(newCommonMenuItem(GC.iNEW, GC.NEW, 'N', ActionEvent.CTRL_MASK, true));
		menu.add(newCommonMenuItem(GC.iOPEN, GC.OPEN, 'O', ActionEvent.CTRL_MASK, true));

		// menuTemp = newSplMenuItem(GCSpl.iSPL_IMPORT_TXT, GCSpl.FILE_LOADTXT, 'I', GC.NO_MASK, true);
		// menu.add(menuTemp);
		menu.addSeparator();
		menu.add(getRecentMainPaths());
		menu.add(getRecentFile());
		menuTemp = getRecentConn();
		menu.add(menuTemp);
		menu.addSeparator();
		menu.add(newCommonMenuItem(GC.iQUIT, GC.QUIT, 'X', GC.NO_MASK, true));
		add(menu);

		// ���߲˵�
		add(getToolMenu());

		// ���ڲ˵���
		tmpLiveMenu = getWindowMenu();
		add(tmpLiveMenu);

		// �����˵���
		add(getHelpMenu(true));

		setEnable(getMenuItems(), false);
		resetLiveMenu();
	}

	/**
	 * ȡ���߲˵�
	 * 
	 * @return
	 */
	protected JMenu getToolMenu() {
		JMenu menu = getCommonMenuItem(GC.TOOL, 'T', true);
		JMenuItem menuTemp;
		menuTemp = newCommonMenuItem(GC.iDATA_SOURCE, GC.DATA_SOURCE, 'S', GC.NO_MASK, true);
		menu.add(menuTemp);
		JMenuItem miCmd = newSplMenuItem(GCSpl.iEXEC_CMD, GCSpl.EXEC_CMD, 'C', GC.NO_MASK, true);
		boolean isWin = GM.isWindowsOS();
		miCmd.setVisible(isWin);
		miCmd.setEnabled(isWin);
		menu.add(miCmd);

		JMenuItem miRep = newSplMenuItem(GCSpl.iFILE_REPLACE, GCSpl.FILE_REPLACE, 'R', GC.NO_MASK);
		menu.add(miRep);
		menu.addSeparator();
		menu.add(newCommonMenuItem(GC.iOPTIONS, GC.OPTIONS, 'O', GC.NO_MASK, true));
		if (ConfigOptions.bIdeConsole.booleanValue()) {
			JMenuItem miConsole = newCommonMenuItem(GC.iCONSOLE, GC.CONSOLE, 'A', GC.NO_MASK);
			miConsole.setVisible(false);
			miConsole.setEnabled(false);
			menu.add(miConsole);
		}
		return menu;
	}

	/**
	 * �½��������˵���
	 * 
	 * @param cmdId  ��GCSpl�ж��������
	 * @param menuId ��GCSpl�ж���Ĳ˵���
	 * @param mneKey The Mnemonic
	 * @param mask   int, Because ActionEvent.META_MASK is almost not used. This key
	 *               seems to be only available on Macintosh keyboards. It is used
	 *               here instead of no accelerator key.
	 * @return
	 */
	protected JMenuItem newSplMenuItem(short cmdId, String menuId, char mneKey, int mask) {
		return newSplMenuItem(cmdId, menuId, mneKey, mask, false);
	}

	/**
	 * �½��������˵���
	 * 
	 * @param cmdId   ��GCSpl�ж��������
	 * @param menuId  ��GCSpl�ж���Ĳ˵���
	 * @param mneKey  The Mnemonic
	 * @param mask    int, Because ActionEvent.META_MASK is almost not used. This
	 *                key seems to be only available on Macintosh keyboards. It is
	 *                used here instead of no accelerator key.
	 * @param hasIcon �˵����Ƿ���ͼ��
	 * @return
	 */
	protected JMenuItem newSplMenuItem(short cmdId, String menuId, char mneKey, int mask, boolean hasIcon) {
		String menuText = menuId;
		if (menuText.indexOf('.') > 0) {
			menuText = IdeSplMessage.get().getMessage(GC.MENU + menuId);
		}
		return newMenuItem(cmdId, menuId, mneKey, mask, hasIcon, menuText);
	}

	/**
	 * �½��������˵���
	 * 
	 * @param cmdId    ��GCSpl�ж��������
	 * @param menuId   ��GCSpl�ж���Ĳ˵���
	 * @param mneKey   The Mnemonic
	 * @param mask     int, Because ActionEvent.META_MASK is almost not used. This
	 *                 key seems to be only available on Macintosh keyboards. It is
	 *                 used here instead of no accelerator key.
	 * @param hasIcon  �˵����Ƿ���ͼ��
	 * @param menuText �˵����ı�
	 * @return
	 */
	protected JMenuItem newMenuItem(short cmdId, String menuId, char mneKey, int mask, boolean hasIcon,
			String menuText) {
		JMenuItem mItem = GM.getMenuItem(cmdId, menuId, mneKey, mask, hasIcon, menuText);
		mItem.addActionListener(menuAction);
		menuItems.put(cmdId, mItem);
		return mItem;
	}

	/**
	 * �˵�ִ�еļ�����
	 */
	private ActionListener menuAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String menuId = "";
			try {
				JMenuItem mi = (JMenuItem) e.getSource();
				menuId = mi.getName();
				short cmdId = Short.parseShort(menuId);
				executeCmd(cmdId);
			} catch (Exception ex) {
				GM.showException(ex);
			}
		}
	};

	/**
	 * ȡ���пɱ�״̬�Ĳ˵���
	 */
	public short[] getMenuItems() {
		short[] menus = new short[] {};
		return menus;
	}

	/**
	 * ִ�в˵�����
	 */
	public void executeCmd(short cmdId) {
		try {
			GMSpl.executeCmd(cmdId);
		} catch (Exception e) {
			GM.showException(e);
		}
	}

	/**
	 * ����Դ���Ӻ�
	 */
	public void dataSourceConnected() {
		if (GVSpl.tabParam != null)
			GVSpl.tabParam.resetEnv();
	}
}
