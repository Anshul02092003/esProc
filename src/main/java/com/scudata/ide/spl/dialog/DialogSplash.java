package com.scudata.ide.spl.dialog;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.scudata.common.StringUtils;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.swing.FreeLayout;

/**
 * ��������ʾ����ͼƬ����
 * 
 */
public class DialogSplash extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 * 
	 * @param splashImage splashͼƬ·��
	 */
	public DialogSplash(String splashImage) {
		try {
			this.setUndecorated(true);
			this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			this.getRootPane().setBorder(null);
			initUI(splashImage);
			this.setResizable(false);
			GM.centerWindow(this);
			this.setModal(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void initUI(String splashImage) throws Exception {
		ImageIcon ii = getImageIcon(splashImage);
		Image image = ii.getImage();
		ImagePanel panel = new ImagePanel(image);
		int width = image.getWidth(this);
		int height = image.getHeight(this);
		panel.setSize(width, height);
		this.setSize(width, height);
		panel.setOpaque(false);
		panel.setLayout(new FreeLayout());

		this.getContentPane().add(panel);
	}

	/**
	 * �رմ���
	 */
	public void closeWindow() {
		dispose();
	}

	/**
	 * ȡͼƬ����
	 * 
	 * @return
	 */
	private ImageIcon getImageIcon(String splashImage) {
		ImageIcon ii = null;
		if (StringUtils.isValidString(splashImage)) {
			String path = GM.getAbsolutePath(splashImage);
			File f = new File(path);
			if (f.exists()) {
				ii = new ImageIcon(path);
			} else {
				ii = GM.getImageIcon(splashImage);
			}
		}
		if (ii == null) {
			String imgPath = "/com/scudata/ide/common/resources/esproc"
					+ GM.getLanguageSuffix() + ".png";
			ii = GM.getImageIcon(imgPath);
		}
		return ii;
	}

	/**
	 * ��ʾͼƬ�����
	 *
	 */
	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image image = null;

		public ImagePanel(Image image) {
			this.image = image;
		}

		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
			super.paint(g);
		}
	}
}
