package com.scudata.ide.spl.dialog;

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
	 * splashͼƬ·��
	 */
	private String splashImage;

	/**
	 * ���캯��
	 * 
	 * @param splashImage
	 */
	public DialogSplash(String splashImage) {
		this.splashImage = splashImage;
		try {
			initUI();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void initUI() throws Exception {
		this.setUndecorated(true);
		this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		this.getRootPane().setBorder(null);
		ImageIcon ii = getImageIcon();
		Image image = ii.getImage();
		ImagePanel panel = new ImagePanel(image);
		int width = image.getWidth(this);
		int height = image.getHeight(this);
		panel.setSize(width, height);
		this.setSize(width, height);
		panel.setOpaque(false);
		panel.setLayout(new FreeLayout());
		this.setResizable(false);
		this.setModal(false);
		this.getContentPane().add(panel);
		GM.centerWindow(this);
	}

	/**
	 * �رմ���
	 */
	public void closeWindow() {
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * ȡͼƬ����
	 * 
	 * @return
	 */
	private ImageIcon getImageIcon() {
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
			String img = getDefaultImage();
			ii = GM.getImageIcon(img);
		}
		return ii;
	}

	/**
	 * ͼƬ·��
	 * 
	 * @return
	 */
	private String getDefaultImage() {
		String img = "/com/scudata/ide/common/resources/esproc"
				+ GM.getLanguageSuffix() + ".png";
		return img;
	}

	/**
	 * ��ʾͼƬ�����
	 *
	 */
	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image image = null;

		public ImagePanel(Image image) {
			setImage(image);
		}

		public void setImage(Image image) {
			this.image = image;
		}

		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
			super.paint(g);
		}
	}
}
