package com.scudata.ide.spl;

import com.scudata.ide.common.AppMenu;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.PrjxAppToolBar;
import com.scudata.ide.common.ToolBarPropertyBase;
import com.scudata.ide.common.control.JWindowNames;
import com.scudata.ide.spl.base.JTabbedParam;
import com.scudata.ide.spl.base.PanelSplWatch;
import com.scudata.ide.spl.base.PanelValue;
import com.scudata.ide.spl.control.SplEditor;
import com.scudata.ide.spl.dialog.DialogSearch;

/**
 * ������IDE�еĳ���
 *
 */
public class GVSpl extends GV {
	/**
	 * ����༭��
	 */
	public static SplEditor splEditor = null;

	/**
	 * IDE���½ǵĶ��ǩ�ؼ�,��������������ʽ�ȱ�ǩҳ
	 */
	public static JTabbedParam tabParam = null;

	/**
	 * ��Ԫ��ֵ���
	 */
	public static PanelValue panelValue = null;

	/**
	 * ������ʽ�������
	 */
	public static PanelSplWatch panelSplWatch = null;

	/**
	 * �����Ի���
	 */
	public static DialogSearch searchDialog = null;

	/**
	 * ƥ��Ĵ�������
	 */
	public static JWindowNames matchWindow = null;

	/**
	 * ȡ�������˵�
	 * 
	 * @return
	 */
	public static AppMenu getSplMenu() {
		appMenu = new MenuSpl();
		return appMenu;
	}

	/**
	 * ȡ������������
	 * 
	 * @return
	 */
	public static ToolBarSpl getSplTool() {
		appTool = new ToolBarSpl();
		return (ToolBarSpl) appTool;
	}

	/**
	 * ȡ���Թ�����
	 * 
	 * @return
	 */
	public static ToolBarPropertyBase getSplProperty() {
		toolBarProperty = new ToolBarProperty();
		return toolBarProperty;
	}

	/**
	 * ȡ�����˵������ļ���ʱ��
	 * 
	 * @return
	 */
	public static AppMenu getBaseMenu() {
		appMenu = new MenuBase();
		return appMenu;
	}

	/**
	 * ȡ���������������ļ���ʱ��
	 * 
	 * @return
	 */
	public static PrjxAppToolBar getBaseTool() {
		appTool = new ToolBarBase();
		return appTool;
	}

	/**
	 * ȡ�������Թ����������ļ���ʱ��
	 * 
	 * @return
	 */
	public static ToolBarPropertyBase getBaseProperty() {
		toolBarProperty = new ToolBarProperty();
		return toolBarProperty;
	}
}
