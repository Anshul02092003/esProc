package com.scudata.ide.spl.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.scudata.app.common.AppConsts;
import com.scudata.app.common.AppUtil;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.StringUtils;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.control.PanelConsole;
import com.scudata.ide.common.dialog.RQDialog;
import com.scudata.ide.common.swing.JTableEx;
import com.scudata.ide.spl.GMSpl;
import com.scudata.ide.spl.GVSpl;
import com.scudata.ide.spl.SPL;
import com.scudata.ide.spl.resources.IdeSplMessage;
import com.scudata.util.CellSetUtil;

/**
 * ��������ִ������Ի���
 *
 */
public class DialogExecCmd extends RQDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 */
	public DialogExecCmd() {
		super(IdeSplMessage.get().getMessage("dialogexeccmd.title"), 400, 300);
		try {
			init();
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	/**
	 * ����spl�ļ���
	 * 
	 * @param splFile
	 */
	public void setSplFile(String splFile) {
		textSpl.setText(splFile);
		loadFile();
	}

	/**
	 * ȷ�ϰ�ť�¼�
	 */
	protected boolean okAction(ActionEvent e) {
		String splFile = textSpl.getText();
		if (!StringUtils.isValidString(splFile)) {
			// ��ѡ��Ҫִ�е�SPL�ļ���
			JOptionPane.showMessageDialog(GV.appFrame, IdeSplMessage.get().getMessage("dialogexeccmd.emptydfx"));
			return false;
		}
		final File f = new File(splFile);
		if (!f.isFile() || !f.exists()) {
			// �ļ���{0}�����ڡ�
			JOptionPane.showMessageDialog(GV.appFrame,
					IdeSplMessage.get().getMessage("dialogexeccmd.dfxnotexist", splFile));
			return false;
		}
		String startHome = System.getProperty("start.home");
		File binDir = new File(startHome, "bin");
		if (!binDir.exists() || !binDir.isDirectory()) {
			// Ŀ¼��{0}�����ڡ�
			JOptionPane.showMessageDialog(GV.appFrame,
					IdeSplMessage.get().getMessage("dialogexeccmd.binnotexist", binDir.getAbsolutePath()));
			return false;
		}
		String suffix;
		if (GM.isWindowsOS()) {
			suffix = "exe";
		} else {
			suffix = "sh";
		}
		final File exeFile = new File(binDir, "esprocx." + suffix);
		if (!exeFile.isFile() || !exeFile.exists()) {
			// ��ִ���ļ���{0}�����ڡ�
			JOptionPane.showMessageDialog(GV.appFrame,
					IdeSplMessage.get().getMessage("dialogexeccmd.exenotexist", exeFile.getAbsolutePath()));
			return false;
		}
		paramTable.acceptText();
		PanelConsole pc = ((SPL) GVSpl.appFrame).getPanelConsole();
		if (pc != null)
			pc.autoClean();
		Thread t = new Thread() {
			public void run() {
				List<String> args = new ArrayList<String>();
				Object o;
				for (int i = 0; i < paramTable.getRowCount(); ++i) {
					o = paramTable.data.getValueAt(i, COL_VALUE);
					args.add(o == null ? "" : o.toString());
				}
				String[] cmds;
				if (GM.isWindowsOS()) {
					cmds = new String[7 + args.size()];
					cmds[0] = "cmd.exe";
					cmds[1] = "/c";
					cmds[2] = "start";
					cmds[3] = "/b";
					cmds[4] = " ";
					cmds[5] = exeFile.getAbsolutePath();
					cmds[6] = f.getAbsolutePath();
					for (int i = 0; i < args.size(); i++) {
						cmds[7 + i] = args.get(i);
					}
				} else if (GM.isMacOS()) {
					cmds = new String[3 + args.size()];
					cmds[0] = "sh";
					cmds[1] = exeFile.getAbsolutePath();
					cmds[2] = f.getAbsolutePath();
					for (int i = 0; i < args.size(); i++) {
						cmds[3 + i] = args.get(i);
					}
				} else {
					cmds = new String[4 + args.size()];
					cmds[0] = "sh";
					cmds[1] = "-c";
					cmds[2] = exeFile.getAbsolutePath();
					cmds[3] = f.getAbsolutePath();
					for (int i = 0; i < args.size(); i++) {
						cmds[4 + i] = args.get(i);
					}
				}
				try {
					Process proc = Runtime.getRuntime().exec(cmds);

					WatchThread wt = new WatchThread(proc);
					wt.start();

					WatchErrorThread wt1 = new WatchErrorThread(proc);
					wt1.start();

					proc.waitFor();
					wt.setOver(true);
					wt1.setOver(true);
					closeDialog(JOptionPane.OK_OPTION);
					((SPL) GV.appFrame).viewTabConsole();
				} catch (Exception e1) {
					GM.showException(e1);
					return;
				}
			}
		};
		t.start();
		return false;
	}

	/**
	 * �ȴ��̣߳����ڽ���Process���ص���Ϣ
	 *
	 */
	protected class WatchThread extends Thread {
		Process p;
		boolean over;

		public WatchThread(Process p) {
			this.p = p;
			over = false;
		}

		public void run() {
			if (p == null)
				return;
			Scanner br = null;
			try {
				br = new Scanner(p.getInputStream());
				while (true) {
					if (p == null || over)
						break;
					while (br.hasNextLine()) {
						String tempStream = br.nextLine();
						if (tempStream.trim() == null || tempStream.trim().equals(""))
							continue;
						System.out.println(tempStream);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					br.close();
				}
			}
		}

		public void setOver(boolean over) {
			this.over = over;
		}
	}

	/**
	 * �ȴ��̣߳����ڽ���Process���صĴ�����Ϣ
	 *
	 */
	protected class WatchErrorThread extends Thread {
		Process p;
		boolean over;

		public WatchErrorThread(Process p) {
			this.p = p;
			over = false;
		}

		public void run() {
			if (p == null)
				return;
			Scanner br = null;
			try {
				br = new Scanner(p.getErrorStream());
				while (true) {
					if (p == null || over)
						break;
					while (br.hasNextLine()) {
						String tempStream = br.nextLine();
						if (tempStream.trim() == null || tempStream.trim().equals(""))
							continue;
						System.out.println(tempStream);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					br.close();
				}
			}
		}

		public void setOver(boolean over) {
			this.over = over;
		}
	}

	/**
	 * ��ʼ���ؼ�
	 */
	private void init() {
		JPanel panelNorth = new JPanel(new GridBagLayout());
		// SPL�ļ�
		panelNorth.add(new JLabel(IdeSplMessage.get().getMessage("dialogexeccmd.dfxfile")), GM.getGBC(0, 0));
		panelNorth.add(textSpl, GM.getGBC(0, 1, true, false, 2));
		panelNorth.add(buttonFile, GM.getGBC(0, 2));
		panelCenter.add(panelNorth, BorderLayout.NORTH);
		JPanel panelParam = new JPanel(new GridBagLayout());
		// �������
		panelParam.add(new JLabel(IdeSplMessage.get().getMessage("dialogexeccmd.dfxparam")), GM.getGBC(0, 0));
		panelParam.add(new JScrollPane(paramTable), GM.getGBC(1, 0, true, true));
		panelCenter.add(panelParam, BorderLayout.CENTER);
		buttonFile.setIcon(GM.getMenuImageIcon(GC.OPEN));
		buttonFile.setToolTipText(IdeSplMessage.get().getMessage("dialogexeccmd.openfile")); // ���ļ�
		buttonFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				File f = GM.dialogSelectFile("\"" + AppConsts.SPL_FILE_EXTS + "\"", false);
				if (f != null) {
					textSpl.setText(f.getAbsolutePath());
					loadFile();
				}
			}
		});
		textSpl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadFile();
			}

		});
		paramTable.setRowHeight(20);
		paramTable.setColumnEnable(TITLE_DISP, false);
	}

	/**
	 * �����ļ�
	 */
	private void loadFile() {
		paramTable.acceptText();
		paramTable.removeAllRows();
		paramTable.clearSelection();
		try {
			String filePath = textSpl.getText();
			if (!StringUtils.isValidString(filePath)) {
				return;
			}
			PgmCellSet cellSet = AppUtil.readCellSet(filePath);
			ParamList pl = cellSet.getParamList();
			if (pl != null) {
				for (int i = 0, size = pl.count(); i < size; i++) {
					Param p = pl.get(i);
					int row = paramTable.addRow();
					paramTable.data.setValueAt(StringUtils.isValidString(p.getRemark()) ? p.getRemark() : p.getName(),
							row, COL_DISP);
					paramTable.data.setValueAt(p.getValue(), row, COL_VALUE);
				}
			}
		} catch (Exception e1) {
			GM.showException(e1);
		}
	}

	/**
	 * spl�ļ��༭��
	 */
	private JTextField textSpl = new JTextField();
	/**
	 * ѡ��spl�ļ���ť
	 */
	private JButton buttonFile = new JButton();
	/** ������������˵���� */
	private final int COL_DISP = 0;
	/** ����ֵ�� */
	private final int COL_VALUE = 1;
	/** ������������˵�� */
	private final String TITLE_DISP = IdeSplMessage.get().getMessage("dialogexeccmd.name");
	/** ����ֵ */
	private final String TITLE_VALUE = IdeSplMessage.get().getMessage("dialogexeccmd.value");
	/** �������ؼ� */
	private JTableEx paramTable = new JTableEx(new String[] { TITLE_DISP, TITLE_VALUE }) {

		private static final long serialVersionUID = 1L;

		public void doubleClicked(int xpos, int ypos, int row, int col, MouseEvent e) {
			if (col != COL_VALUE) {
				return;
			}
			GM.dialogEditTableText(paramTable, row, col);
		}
	};
}
