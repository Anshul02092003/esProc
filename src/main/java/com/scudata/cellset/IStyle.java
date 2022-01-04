package com.scudata.cellset;

public interface IStyle {
	/** ����ȡֵ���ı� */
	public final static byte TYPE_TEXT = (byte) 0;
	/** ����ȡֵ��ͼƬ */
	public final static byte TYPE_PIC = (byte) 1;
	/** ����ȡֵ��ͳ��ͼ */
	public final static byte TYPE_CHART = (byte) 2;
	/** ��������ȡֵ���ӱ��� */
	public final static byte TYPE_SUBREPORT = (byte) 3;
	/** ��������ȡֵ��HTML */
	public final static byte TYPE_HTML = (byte) 4;
	/** ��������ȡֵ�������� */
	public final static byte TYPE_BARCODE = (byte) 5;
	/** �๦���ı�����ȡֵ���๦���ı� */
	public final static byte TYPE_RICHTEXT = (byte) 6;
	/** SVG */
	public final static byte TYPE_SVG = (byte) 7;
	/** ����� */
	public final static byte TYPE_BLOB = (byte) 8;
	/** ����ȡֵ���Զ��� */
	public final static byte TYPE_CUSTOM = (byte) 20;

	/**
	 * ˮƽ����ȡֵ������� * / public final static byte HALIGN_LEFT = (byte) 0; /**
	 * ˮƽ����ȡֵ���ж��� * / public final static byte HALIGN_CENTER = (byte) 1; /**
	 * ˮƽ����ȡֵ���Ҷ��� * / public final static byte HALIGN_RIGHT = (byte) 2;
	 * 
	 * 
	 * /** ��ֱ����ȡֵ������ * / public final static byte VALIGN_TOP = (byte) 0; /**
	 * ��ֱ����ȡֵ������ * / public final static byte VALIGN_MIDDLE = (byte) 1; /**
	 * ��ֱ����ȡֵ������ * / public final static byte VALIGN_BOTTOM = (byte) 2; /*
	 * ���붨���Ϊ���£���Ϊ�˸�ͼ�ε�Constsͳһ�Ҽ��ݹ�ȥ��ͼ�� xq 2014.9.23,ע�⣺�������ֵҪ���㣬��������Ķ���
	 */
	/** ----------------------ˮƽ����ȡֵ---------------------------- */
	public final static byte HALIGN_LEFT = 0; // �����
	public final static byte HALIGN_CENTER = 2; // �ж���
	public final static byte HALIGN_RIGHT = 4; // �Ҷ���

	/** ----------------------��ֱ����ȡֵ---------------------------- */
	public final static byte VALIGN_TOP = 8; // ����
	public final static byte VALIGN_MIDDLE = 16; // ����
	public final static byte VALIGN_BOTTOM = 32; // ����

	/** ������ʽȡֵ��������ʵֵ */
	public final static byte EXPORT_REAL = (byte) 0;
	/** ������ʽȡֵ��������ʾֵ */
	public final static byte EXPORT_DISP = (byte) 1;
	/** ������Excel�ķ�ʽȡֵ��������ʽ */
	public final static byte EXPORT_FORMULA = (byte) 2;

	/** �ߴ����ȡֵ������Ƴߴ粻�� */
	public final static byte ADJUST_FIXED = (byte) 0;
	/** �ߴ����ȡֵ������Ԫ���������� */
	public final static byte ADJUST_EXTEND = (byte) 1;
	/** �ߴ����ȡֵ��ͼƬ������Ԫ�� */
	public final static byte ADJUST_FILL = (byte) 2;
	/** �ߴ����ȡֵ����С������� */
	public final static byte ADJUST_SHRINK = (byte) 3;

	/**
	 * �߿���ʽȡֵ���ޱ߿� * / public final static byte LINE_NONE = (byte) 0; /**
	 * �߿���ʽȡֵ������ * / public final static byte LINE_DOT = (byte)1; /** �߿���ʽȡֵ������
	 * * / public final static byte LINE_DASHED = (byte) 2; /** �߿���ʽȡֵ��ʵ�� * /
	 * public final static byte LINE_SOLID = (byte) 3; /** �߿���ʽȡֵ��˫�� * / public
	 * final static byte LINE_DOUBLE = (byte) 4; /** �߿���ʽȡֵ���㻮�� * / public final
	 * static byte LINE_DOTDASH = (byte) 5; /* �߶����Ϊ���£���Ϊ�˸�ͼ�ε�Constsͳһ�Ҽ��ݹ�ȥ��ͼ��
	 * xq 2014.9.23
	 */
	public final static byte LINE_NONE = 0x0; // ��
	public final static byte LINE_SOLID = 0x1; // ʵ��
	public final static byte LINE_DASHED = 0x2; // ����
	public final static byte LINE_DOT = 0x3; // ����
	public final static byte LINE_DOUBLE = 0x4; // ˫ʵ��
	public final static byte LINE_DOTDASH = 0x5; // �㻮��

	/** ״̬�Զ� */
	public final static byte STATE_AUTO = -1;
	/** ״̬��ֹ */
	public final static byte STATE_NO = 0;
	/** ״̬���� */
	public final static byte STATE_YES = 1;
}
