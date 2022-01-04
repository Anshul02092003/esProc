package com.scudata.cellset.datamodel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.cellset.IColCell;
import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;

public class ColCell implements IColCell {
	private static final long serialVersionUID = 0x02010012;
	private final static byte version = (byte) 1;

	// ��������ȡֵ
	public final static byte VISIBLE_ALWAYS = 0; // ���ǿɼ�
	public final static byte VISIBLE_ALWAYSNOT = 1; // ���ǲ��ɼ�
	public final static byte VISIBLE_FIRSTPAGE = 2; // ��ҳ�ɼ�
	public final static byte VISIBLE_FIRSTPAGENOT = 3; // ��ҳ���ɼ�

	private int col;
	private float width = 150.0f;
	private int level; // ��

	private byte visible; // ��������
	private boolean isBreakPage; // �к��Ƿ��ҳ

	// ����ʱʹ��
	public ColCell() {
	}

	public ColCell(int col) {
		this.col = col;
	}

	/**
	 * �����к�
	 * @return int
	 */
	public int getCol() {
		return col;
	}

	/**
	 * �����к�
	 * @param col int
	 */
	public void setCol(int col) {
		this.col = col;
	}

	/**
	 * �����п�
	 * @param w float
	 */
	public void setWidth(float w) {
		width = w;
	}

	/**
	 * �����п�
	 * @return float
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * ���ز��
	 * @return int
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * ���ò��
	 * @param level int
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * �������Ƿ�ɼ�
	 * @return byte ȡֵ VISIBLE_ALWAYS��VISIBLE_ALWAYSNOT��VISIBLE_FIRSTPAGE��VISIBLE_FIRSTPAGENOT
	 */
	public byte getVisible(){
		return visible;
	}

	/**
	 * �������Ƿ�ɼ�
	 * @param b byte ȡֵ VISIBLE_ALWAYS��VISIBLE_ALWAYSNOT��VISIBLE_FIRSTPAGE��VISIBLE_FIRSTPAGENOT
	 */
	public void setVisible(byte b){
		visible = b;
	}

	/**
	 * @return �����к��Ƿ��ҳ
	 */
	public boolean isBreakPage(){
		return isBreakPage;
	}

	/**
	 * �����к��Ƿ��ҳ
	 * @param b Ϊtrue���к��ҳ�����򲻷�ҳ
	 */
	public void setBreakPage(boolean b){
		isBreakPage = b;
	}

	/**
	 * ��ȿ�¡
	 * @return ��¡���Ķ���
	 */
	public Object deepClone(){
		ColCell cell = new ColCell(col);
		cell.width = width;
		cell.level = level;

		cell.visible = visible;
		cell.isBreakPage = isBreakPage;
		return cell;
	}

	/**
	 * д���ݵ���
	 * @param out ObjectOutput �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(version);

		out.writeInt(col);
		out.writeFloat(width);
		out.writeInt(level);

		out.writeByte(visible);
		out.writeBoolean(isBreakPage);
	}

	/**
	 * �����ж�����
	 * @param in ObjectInput ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte(); // version

		col = in.readInt();
		width = in.readFloat();
		level = in.readInt();

		visible = in.readByte();
		isBreakPage = in.readBoolean();
	}

	/**
	 * д���ݵ���
	 * @throws IOException
	 * @return �����
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeInt(col);
		out.writeFloat(width);
		out.writeInt(level);

		out.writeByte(visible);
		out.writeBoolean(isBreakPage);
		return out.toByteArray();
	}

	/**
	 * �����ж�����
	 * @param buf byte[]
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		col = in.readInt();
		width = in.readFloat();
		level = in.readInt();

		visible = in.readByte();
		isBreakPage = in.readBoolean();
	}
}
