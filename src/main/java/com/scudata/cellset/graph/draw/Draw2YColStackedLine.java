package com.scudata.cellset.graph.draw;

/**
 * ˫��ѻ�����ͼ��ʵ��
 * @author Joancy
 *
 */
public class Draw2YColStackedLine extends DrawBase {
	/**
	 * ʵ�ֻ�ͼ����
	 */
	public void draw(StringBuffer htmlLink) {
		drawing(this, htmlLink);
	}

	/**
	 * ���ݻ�ͼ����db��ͼ��������ͼ��ĳ����Ӵ���htmlLink
	 * @param db ����Ļ�ͼ����
	 * @param htmlLink �����ӻ���
	 */
	public static void drawing(DrawBase db,StringBuffer htmlLink) {
//		ê�����غ�ʱ��˭��ǰ�棬��������ҵ�˭�����ڵ�С�������ê���������ǰ�档 xq 2017��11��13��
		StringBuffer colLink = new StringBuffer();
		int serNum = DrawColStacked.drawing(db,colLink,true);

//		˫������ͼʱ������ԭ���غ�
		db.gp.isOverlapOrigin = false;
		Draw2Y2Line.drawY2Line(db, serNum, htmlLink);
		
		db.outPoints();
		db.outLabels();
		htmlLink.append(colLink.toString());
	}
}
