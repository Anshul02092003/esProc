package com.scudata.cellset.graph;

import java.awt.Graphics2D;
/**
 * �ػ��ӿ����ڵ���PDF����ӡʱ�����Խ�����豸��gֱ�Ӵ������ػ�����
 * ʹ��ImageValue���Բ�ʹ���Ѿ����õ�ͼ����������ǽ���ͼ����ֱ����
 * g�ػ�����������ֵ��������
 * 
 * �Ѿ�ʵ�ֵ��б���ͼ BackGraphConfig��ͳ��ͼ  ReportStatisticGraph
 * 
 * ������Ҳ�������ı�������������Ļ��ƹ��̺ܸ��ӣ���logo����ת�Լ�ͼ�ηŴ󵽸�����һ����ȸ���ͼƬ����
 * ʵ��g��ֱ���ػ��е��鷳��Ŀǰûʵ��
 * ������������ı������������һ������ר�Ż����ı�
 * @author Joancy
 *
 */
public interface IRedraw{
	public void repaint(Graphics2D g, int w, int h);
}