package com.scudata.ide.spl.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import com.scudata.common.MessageManager;
import com.scudata.common.Sentence;
import com.scudata.common.StringUtils;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.dialog.DialogInputText;
import com.scudata.ide.common.dialog.DialogResourceSearch;
import com.scudata.ide.common.resources.IdeCommonMessage;
import com.scudata.ide.common.swing.FreeConstraints;
import com.scudata.ide.common.swing.FreeLayout;

/**
 * ���ڶԻ���
 *
 */
public class DialogAbout extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/**
	 * Logo�ؼ�
	 */
	private JLabel jLabelLogo;
	/**
	 * �رհ�ť
	 */
	private JButton jBClose = new JButton();
	/**
	 * �ϲ����
	 */
	private JPanel panelTop = new JPanel();
	/**
	 * ����ʱ��
	 */
	private JLabel jLReleaseDate = new JLabel();
	/**
	 * ��˾��
	 */
	private JLabel jLCompanyName = new JLabel();
	/**
	 * ��ַ
	 */
	private JLabel jLWebsite = new JLabel();
	/**
	 * �绰
	 */
	private JLabel jLTel = new JLabel();

	/**
	 * HTTP
	 */
	private JLabel jLbHttp = new JLabel();
	/**
	 * �绰�ı���
	 */
	private JTextField jTFTele = new JTextField() {
		private static final long serialVersionUID = 1L;

		public Border getBorder() {
			return null;
		}
	};
	/**
	 * ����
	 */
	private JLabel jLbName = new JLabel();
	/**
	 * ��Ʒ��
	 */
	private JLabel jLProductName = new JLabel();
	/**
	 * JDK��ť
	 */
	private JButton jBJDK = new JButton();

	/**
	 * ���캯��
	 * 
	 */
	public DialogAbout() {
		super(GV.appFrame, "", true);
		String productName = GV.appFrame.getProductName();
		this.setTitle(mm.getMessage("dialogabout.title") + productName);
		try {
			jbInit();
			pack();
			resetLangText();
			GM.setDialogDefaultButton(this, jBClose, jBClose);
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	/**
	 * �����Լ���
	 */
	private void resetLangText() {
		jBClose.setText(mm.getMessage("button.close")); // �ر�(C)
		jBJDK.setText(mm.getMessage("dialogabout.jdk")); // JDK����
	}

	/**
	 * ������ʾ�ı�
	 * 
	 * @param sDefault
	 * @param sText
	 * @param lbTitle
	 * @param tfText
	 */
	private void setText(String sDefault, String sText, JLabel lbTitle,
			Object tfText) {
		if (!StringUtils.isValidString(sText)) {
			return;
		}
		int i = -1;
		i = sText.indexOf("��"); // ����������ð����Ϊ�ָ����ֿ����Ժ�http://��Ӣ��ð�����ֿ�

		if (i == -1) {
			lbTitle.setText(sDefault);
			if (tfText instanceof JLabel) {
				((JLabel) tfText).setText(sText);
			} else {
				((JTextField) tfText).setText(sText);
			}
		} else {
			lbTitle.setText(sText.substring(0, i + 1));
			if (tfText instanceof JLabel) {
				((JLabel) tfText).setText(sText.substring(i + 1));
			} else {
				((JTextField) tfText).setText(sText.substring(i + 1));
			}
		}
	}

	/**
	 * ȡ����ʱ��
	 * 
	 * @return
	 */
	private static String getReleaseDate() {
		return "2021-11-04";
	}

	/**
	 * ������Ϣ
	 */
	private void loadMessage() {
		jLProductName.setText(mm.getMessage("dialogabout.productname")
				+ "      " + GV.appFrame.getProductName());
		jLReleaseDate.setText(mm.getMessage("dialogabout.label1",
				getReleaseDate()));

		String tmp = mm.getMessage("dialogabout.providername");// ��˾����
		String vendorName = mm.getMessage("dialogabout.defvendor");
		setText(tmp, vendorName, jLCompanyName, jLbName);
		tmp = mm.getMessage("dialogabout.providerhttp");// ��˾��ַ
		String vendorURL = mm.getMessage("dialogabout.defvendorurl");
		setText(tmp, vendorURL, jLWebsite, jLbHttp);
		tmp = mm.getMessage("dialogabout.providertel");// ��˾�绰
		String vendorTel = "010-51295366";
		setText(tmp, vendorTel, jLTel, jTFTele);
	}

	/**
	 * �ڷ�Logo
	 */
	private void placeLogo() {
		panelTop.add(jLabelLogo, new FreeConstraints(10, 0, 128, 128));
		panelTop.add(jLProductName, new FreeConstraints(163, 15, -1, -1));
		panelTop.add(jLReleaseDate, new FreeConstraints(163, 45, -1, -1));
	}

	/**
	 * �ڷų�Logo
	 */
	private void placeLongLogo() {
		panelTop.add(jLabelLogo, new FreeConstraints(10, 2, 380, 50));
		panelTop.add(jLProductName, new FreeConstraints(10, 60, -1, -1));
		panelTop.add(jLReleaseDate, new FreeConstraints(10, 90, 209, -1));
	}

	/**
	 * ����ʱ������д�˷������滻Logo
	 * 
	 * @return
	 */
	protected ImageIcon getLogoImageIcon() {
		return GM.getLogoImage(false);
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @param product
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		panelTop.setLayout(new FreeLayout());
		ImageIcon icon = getLogoImageIcon();
		boolean isLongLogo = false;
		if (icon != null) {
			Image image = icon.getImage();
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			isLongLogo = w * 1.0 / h > 2;
			if (isLongLogo) {
				image = image.getScaledInstance(380, (int) (380.0 * h / w),
						Image.SCALE_SMOOTH);
			} else {
				if (w > h) {
					image = image.getScaledInstance(128,
							(int) (128 * (h * 1.0 / w)), Image.SCALE_SMOOTH);
				} else {
					image = image.getScaledInstance(
							(int) (128 * (w * 1.0 / h)), 128,
							Image.SCALE_SMOOTH);
				}
			}

			jLabelLogo = new JLabel(new ImageIcon(image));
		} else {
			jLabelLogo = new JLabel();
		}

		jBClose.setDoubleBuffered(false);
		jBClose.setMnemonic('C');
		jBClose.setText("�ر�(C)");

		jBClose.addActionListener(new DialogAbout_jBClose_actionAdapter(this));
		jLWebsite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jLWebsite.setForeground(SystemColor.textHighlight);
		jLbHttp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jLbHttp.setFont(new java.awt.Font("Comic Sans MS", 2, 13));
		jLbHttp.setForeground(Color.blue);
		jLbHttp.setBorder(null);

		jTFTele.setDisabledTextColor(Color.black);
		jTFTele.setEditable(false);
		Color telBg = new Color(jLbHttp.getBackground().getRGB());
		jTFTele.setBackground(telBg);

		jLbHttp.addMouseListener(new DialogAbout_jLbHttp_mouseAdapter(this));
		jTFTele.setHorizontalAlignment(SwingConstants.LEFT);
		jTFTele.addMouseListener(new DialogAbout_jTFTele_mouseAdapter(this));
		jLbName.setFont(new java.awt.Font("Dialog", 0, 12));
		jLbName.setForeground(Color.black);
		jLbName.setHorizontalAlignment(SwingConstants.LEFT);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.getContentPane().setLayout(new BorderLayout());
		this.addWindowListener(new DialogAbout_this_windowAdapter(this));
		jBJDK.setMnemonic('K');
		jBJDK.setText("JDK����");
		jBJDK.addActionListener(new DialogAbout_jBJDK_actionAdapter(this));

		jLCompanyName.setForeground(SystemColor.textHighlight);
		jLTel.setForeground(SystemColor.textHighlight);
		getContentPane().add(panelTop, BorderLayout.CENTER);
		final int DIFF = 25;
		panelTop.add(jLabelLogo, new FreeConstraints(0, 0, 145, 123));
		panelTop.add(jLProductName, new FreeConstraints(163, 15, -1, -1));
		panelTop.add(jLCompanyName, new FreeConstraints(14, 174 - DIFF, 69, -1));
		panelTop.add(jLWebsite, new FreeConstraints(14, 201 - DIFF, 69, -1));
		panelTop.add(jLTel, new FreeConstraints(14, 227 - DIFF, -1, -1));
		panelTop.add(jLbName, new FreeConstraints(95, 174 - DIFF, 288, -1));
		panelTop.add(jLbHttp, new FreeConstraints(95, 201 - DIFF, 288, -1));
		panelTop.add(jTFTele, new FreeConstraints(95, 227 - DIFF, 288, -1));
		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
		jPanel2.add(jBJDK);
		jPanel2.add(jBClose);
		this.getContentPane().add(jPanel2, BorderLayout.SOUTH);

		if (isLongLogo) {
			placeLongLogo();
		} else {
			placeLogo();
		}
		loadMessage();

		KeyListener searchResource = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				boolean isMacOS = GM.isMacOS();
				boolean isCmdKey = isMacOS ? e.isMetaDown() : e.isControlDown();
				if (isCmdKey && e.getKeyCode() == KeyEvent.VK_F) {
					DialogResourceSearch drs = new DialogResourceSearch(
							GV.appFrame);
					drs.setVisible(true);
				}
			}
		};
		jBClose.addKeyListener(searchResource);
		this.getContentPane().addKeyListener(searchResource);
	}

	/**
	 * �رհ�ť����
	 * 
	 * @param e
	 */
	void jBClose_actionPerformed(ActionEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * �����HTTP
	 * 
	 * @param e
	 */
	void jLbHttp_mouseClicked(MouseEvent e) {
		int b = e.getButton();
		if (b != MouseEvent.BUTTON1) {
			return;
		}
		if (GM.getOperationSytem() == GC.OS_WINDOWS) {
			try {
				Runtime.getRuntime().exec("cmd /C start " + jLbHttp.getText());
			} catch (Exception x) {
				GM.showException(x);
			}
		}
	}

	/**
	 * ������绰��
	 * 
	 * @param e
	 */
	void jTFTele_mouseClicked(MouseEvent e) {
		int b = e.getButton();
		if (b != MouseEvent.BUTTON3) {
			return;
		}
		String sele = jTFTele.getSelectedText();
		GM.clipBoard(sele);
		jTFTele.setSelectionEnd(0);
	}

	/**
	 * JDK��ť����
	 * 
	 * @param e
	 */
	void jBJDK_actionPerformed(ActionEvent e) {
		DialogInputText dit = new DialogInputText(false);
		Properties p = System.getProperties();
		String buf = p.toString();
		buf = Sentence.replace(buf, ",", "\r\n", 0);
		dit.setText(buf);
		dit.setVisible(true);
	}

	/**
	 * ���ڹر�
	 * 
	 * @param e
	 */
	void this_windowClosing(WindowEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}

}

class DialogAbout_jBClose_actionAdapter implements
		java.awt.event.ActionListener {
	DialogAbout adaptee;

	DialogAbout_jBClose_actionAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBClose_actionPerformed(e);
	}
}

class DialogAbout_jLbHttp_mouseAdapter extends java.awt.event.MouseAdapter {
	DialogAbout adaptee;

	DialogAbout_jLbHttp_mouseAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}

	public void mouseClicked(MouseEvent e) {
		adaptee.jLbHttp_mouseClicked(e);
	}
}

class DialogAbout_jTFTele_mouseAdapter extends java.awt.event.MouseAdapter {
	DialogAbout adaptee;

	DialogAbout_jTFTele_mouseAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}

	public void mouseClicked(MouseEvent e) {
		adaptee.jTFTele_mouseClicked(e);
	}
}

class DialogAbout_jBJDK_actionAdapter implements java.awt.event.ActionListener {
	DialogAbout adaptee;

	DialogAbout_jBJDK_actionAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBJDK_actionPerformed(e);
	}
}

class DialogAbout_this_windowAdapter extends java.awt.event.WindowAdapter {
	DialogAbout adaptee;

	DialogAbout_this_windowAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}
