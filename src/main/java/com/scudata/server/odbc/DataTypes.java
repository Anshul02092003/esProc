package com.scudata.server.odbc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;

/**
 * �������͵Ķ���
 * �Լ���odbc dll��ӦԼ����ʽ�Ķ�д���ݵķ���
 * 
 * @author Joancy
 *
 */
public class DataTypes {
	public static final byte TYPE_NULL = 0;
	public static final byte TYPE_BOOL = 1;//boolean
	public static final byte TYPE_INT = 2;//int
	public static final byte TYPE_LONG = 3;//long
	public static final byte TYPE_FLOAT = 4;//double
	public static final byte TYPE_DECIMAL = 5;//BigDecimal
	public static final byte TYPE_STRING = 6;//String
	public static final byte TYPE_DATE = 7;//sql.Date
	public static final byte TYPE_TIME = 8;//sql.Time
	public static final byte TYPE_DATETIME = 9;//util.Date
	public static final byte TYPE_BINARY = 10;//byte[]
	
	public static final byte TYPE_UNSUPPORT = (byte)126;//
	
	/**
	 * �������дһ���ֽ�
	 * @param out �����
	 * @param size �ֽ�
	 * @throws Exception
	 */
	public static void writeByte(OutputStream out, byte size) throws Exception {
		if(out==null) return;
		out.write(size);
	}
	/**
	 * ����������ȡһ���ֽ�
	 * @param in ������
	 * @return һ���ֽ�����
	 * @throws IOException
	 */
	public static byte readByte(InputStream in) throws IOException {
		int ch1 = in.read();
		if (ch1 < 0) {
			throw new EOFException();
		}
		return (byte)ch1;
	}

	/**
	 * �������дһ������
	 * outΪnullʱ���ڼ��������Ƿ�֧�֣����ڲ�֧�ֵ����ݣ�
	 * Ҫ��ǰУ�飻����д�˳ɹ���־�������쳣���������ˡ�
	 * @param out �����
	 * @param size 32λ����
	 * @throws Exception
	 */
	public static void writeInt(OutputStream out, int size) throws Exception {
		if(out==null) return;
		out.write(size >>> 24);
		out.write(size >>> 16);
		out.write(size >>> 8);
		out.write(size);
	}
	/**
	 * ����������ȡһ��32λ������
	 * @param in ������
	 * @return ����
	 * @throws IOException
	 */
	public static int readInt(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
	}
	
	/**
	 * д��һ������ֵ
	 * @param out �����
	 * @param b ����ֵ
	 * @throws Exception
	 */
	public static void writeBool(OutputStream out, boolean b) throws Exception {
		if(out==null) return;
		if( b ){
			out.write(1);
		}else{
			out.write(0);
		}
	}

	/**
	 * ����һ������ֵ
	 * @param in ������
	 * @return ����ֵ
	 * @throws IOException
	 */
	public static boolean readBool(InputStream in) throws IOException {
		int ch1 = in.read();
		return ch1==1;
	}

	/**
	 * ��ȡһ���ַ���
	 * @param is ������
	 * @return �ַ���
	 * @throws IOException
	 */
	public static String readString(InputStream is) throws IOException {
		int len = readInt(is) * 2;
		byte[] buf = new byte[len];
		for(int i=0;i<len; i++){
			buf[i] = (byte)is.read();
		}
		String str = new String(buf, "utf-16be");
		return str;
	}

	/**
	 * д��һ���ַ���
	 * @param out �����
	 * @param str �ַ���
	 * @throws Exception
	 */
	public static void writeString(OutputStream out, String str) throws Exception {
		if(out==null) return;
		if(str==null){
			writeInt(out, 0);
			return;
		}
		int len = str.length();
		writeInt(out, len);
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			out.write(ch >> 8);
			out.write(ch);
		}
	}

	/**
	 * д��һ��64λ�ĳ�����
	 * @param out �����
	 * @param size ������
	 * @throws Exception
	 */
	public static void writeLong(OutputStream out, long size) throws Exception {
		if(out==null) return;
		out.write((int) (size >>> 56));
		out.write((int) (size >>> 48));
		out.write((int) (size >>> 40));
		out.write((int) (size >>> 32));
		out.write((int) (size >>> 24));
		out.write((int) (size >>> 16));
		out.write((int) (size >>> 8));
		out.write((int) size);
	}

	/**
	 * ��ȡһ��64λ�ĳ�����
	 * @param in ������
	 * @return 64λ������
	 * @throws IOException
	 */
	public static long readLong(InputStream in) throws IOException {
		long ch1 = in.read();
		long ch2 = in.read();
		long ch3 = in.read();
		long ch4 = in.read();
		long ch5 = in.read();
		long ch6 = in.read();
		long ch7 = in.read();
		long ch8 = in.read();
		if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40)
				+ (ch4 << 32) + (ch5 << 24) + (ch6 << 16)
				 + (ch7 << 8)+ ch8);
	}

	/**
	 * д��һ��������
	 * @param out �����
	 * @param d ����ʵ��
	 * @throws Exception
	 */
	public static void writeFloat(OutputStream out, double d) throws Exception {
		if(out==null) return;
		long l = Double.doubleToLongBits(d);
		writeLong(out,l);
	}

	/**
	 * ��ȡһ��������
	 * @param in ������
	 * @return double���ȵĸ�����
	 * @throws IOException
	 */
	public static Double readFloat(InputStream in) throws IOException {
		long bits = readLong(in);
		return Double.longBitsToDouble(bits);
	}

	/**
	 * ѧ��һ������ֵ
	 * @param out �����
	 * @param dec ����ֵ
	 * @throws Exception
	 */
	public static void writeDecimal(OutputStream out, BigDecimal dec) throws Exception {
		if(out==null) return;
		String str = dec.toString();
		writeString(out,str);
	}

	/**
	 * ��ȡһ������ֵ
	 * @param in ������
	 * @return ����ֵ
	 * @throws IOException
	 */
	public static BigDecimal readDecimal(InputStream in) throws IOException {
		String str = readString(in);
		return new BigDecimal(str);
	}

	/**
	 * д��һ������ֵ
	 * @param out �����
	 * @param d ����ֵ
	 * @throws Exception
	 */
	public static void writeDate(OutputStream out, java.sql.Date d) throws Exception {
		if(out==null) return;
		long l = d.getTime();
		writeLong(out,l);
	}

	/**
	 * ��ȡһ������ֵ
	 * @param in ������
	 * @return ����ֵ
	 * @throws IOException
	 */
	public static java.sql.Date readDate(InputStream in) throws IOException {
		long bits = readLong(in);
		return new java.sql.Date(bits);
	}
	
	/**
	 * д��һ��ʱ��ֵ
	 * @param out �����
	 * @param t ʱ��ֵ
	 * @throws Exception
	 */
	public static void writeTime(OutputStream out, java.sql.Time t) throws Exception {
		if(out==null) return;
		Long l = t.getTime();
		writeLong(out, l);
	}
	
	/**
	 * ��ȡһ���Ǽ�ְ
	 * @param in ������
	 * @return ʱ��ֵ
	 * @throws IOException
	 */
	public static java.sql.Time readTime(InputStream in) throws IOException {
		Long L = readLong(in);
		return new java.sql.Time(L);
	}
	
	/**
	 * д��һ������ʱ��ֵ
	 * @param out �����
	 * @param date ����ʱ��
	 */
	public static void writeDateTime(OutputStream out, java.util.Date date) throws Exception {
		if(out==null) return;
		long l = date.getTime();
		writeLong(out,l);
	}
	
	/**
	 * ����һ������ʱ��ֵ
	 * @param in ������
	 * @return ����ʱ��ֵ
	 * @throws IOException
	 */
	public static java.util.Date readDateTime(InputStream in) throws IOException {
		long l = readLong(in);
		return new java.util.Date(l);
	}
	
	/**
	 * д��һ��byte����
	 * @param out �����
	 * @param bytes byte����
	 * @throws Exception
	 */
	public static void writeBinary(OutputStream out, byte[] bytes) throws Exception {
		if(out==null) return;
		int len = bytes.length;
		writeInt(out, len);
		out.write(bytes);
	}

	/**
	 * ����һ��byte����
	 * @param in ������
	 * @return byte����
	 * @throws IOException
	 */
	public static byte[] readBinary(InputStream in) throws IOException {
		int len = readInt(in);
		byte[] bytes = new byte[len];
		in.read(bytes);
		return bytes;
	}
	
	private static byte getDataType(Object obj){
		if(obj==null){
			return TYPE_NULL;
		}
		if(obj instanceof Boolean){
			return TYPE_BOOL;
		}
		if(obj instanceof Integer){
			return TYPE_INT;
		}
		if(obj instanceof Long){
			return TYPE_LONG;
		}
		if(obj instanceof Double){
			return TYPE_FLOAT;
		}
		if(obj instanceof BigDecimal){
			return TYPE_DECIMAL;
		}
		if(obj instanceof String){
			return TYPE_STRING;
		}
		if(obj instanceof java.sql.Date){
			return TYPE_DATE;
		}
		if(obj instanceof java.sql.Time){
			return TYPE_TIME;
		}
		if(obj instanceof java.util.Date){
			return TYPE_DATETIME;
		}
		if(obj instanceof byte[]){
			return TYPE_BINARY;
		}
		
		return TYPE_UNSUPPORT;
	}
	
	private static void writeObject(OutputStream out,Object obj) throws Exception{
		byte dataType = getDataType(obj);
		writeByte(out, dataType);
		switch(dataType){
		case TYPE_NULL:
			break;
		case TYPE_BOOL:
			writeBool(out,(Boolean)obj);
			break;
		case TYPE_INT:
			writeInt(out,(Integer)obj);
			break;
		case TYPE_LONG:
			writeLong(out,(Long)obj);
			break;
		case TYPE_FLOAT:
			writeFloat(out,(Double)obj);
			break;
		case TYPE_DECIMAL:
			writeDecimal(out,(BigDecimal)obj);
			break;
		case TYPE_STRING:
			writeString(out,(String)obj);
			break;
		case TYPE_DATE:
			writeDate(out,(java.sql.Date)obj);
			break;
		case TYPE_TIME:
			writeTime(out,(java.sql.Time)obj);
			break;
		case TYPE_DATETIME:
			writeDateTime(out,(java.util.Date)obj);
			break;
		case TYPE_BINARY:
			writeBinary(out,(byte[])obj);
			break;
		default:
			throw new Exception("Unsupported data:"+obj.getClass().getName());
		}
	}
	
	private static Object readObject(InputStream in) throws Exception{
		byte dataType = readByte(in);
		switch(dataType){
		case TYPE_NULL:
			return null;
		case TYPE_BOOL:
			return readBool(in);
		case TYPE_INT:
			return readInt(in);
		case TYPE_LONG:
			return readLong(in);
		case TYPE_FLOAT:
			return readFloat(in);
		case TYPE_DECIMAL:
			return readDecimal(in);
		case TYPE_STRING:
			return readString(in);
		case TYPE_DATE:
			return readDate(in);
		case TYPE_TIME:
			return readTime(in);
		case TYPE_DATETIME:
			return readDateTime(in);
		case TYPE_BINARY:
			return readBinary(in);
		}
		return null;
	}
	
	/**
	 * д��һ������
	 * @param out �����
	 * @param row һ������
	 * @throws Exception
	 */
	public static void writeRowData(OutputStream out, Object[] row) throws Exception{
		int cols = 0;
		if( row!=null){
			cols =	row.length;
		}
		writeInt(out,cols);
		for(int c=0;c<cols;c++){
			Object val = row[c];
			writeObject(out, val);
		}
	}
	
	/**
	 * ��ȡһ������
	 * @param is ������
	 * @return һ������
	 * @throws Exception
	 */
	public static Object[] readRowData(InputStream is) throws Exception{
		int cols = readInt(is);
		if(cols==0){
			return null;
		}
		Object[] data = new Object[cols];
		for(int c=0;c<cols;c++){
			data[c] = readObject( is );
		}
		return data;
	}
	/**
	 * д��ά��
	 * @param out
	 * @param table
	 * @throws Exception
	 */
	public static void writeTable(OutputStream out, Object[][] table) throws Exception{
		OutputStream baos = out;
		if(table==null){
			writeInt(baos,0);
		}else{
			int rows = table.length;
			writeInt(baos,rows);
			for(int r=0;r<rows;r++){
				Object[] row = table[r];
				writeRowData(baos, row);
			}
		}
	}
	
	/**
	 * �ȼ��һ���Ƿ��в�֧�ֵ����ݣ��������쳣������д���ɹ���־��
	 * �ֳ��쳣����ô�ģ�������Ӱ�죬�൱�ڱ�������������
	 * @param table
	 * @throws Exception
	 */
	public static void checkTable(Sequence table) throws Exception{
		writeTable(null, table);
	}
	
	/**
	 * д��һ�����
	 * @param out �����
	 * @param table ���
	 * @throws Exception
	 */
	public static void writeTable(OutputStream out, Sequence table) throws Exception{
		if(table==null){
			writeInt(out,0);
		}else{
			int rows = table.length();
			writeInt(out,rows);
			for(int r=1; r<=table.length(); r++){
				Object rowObj = table.get(r);
				Object[] rowData;
				if(rowObj instanceof Record){
					Record rec = (Record)rowObj;
					rowData = rec.getFieldValues();
				}else{
					rowData = new Object[]{rowObj};
				}
				writeRowData(out, rowData);
//				����Ǽ���������ͣ��������һ�У�������������ݶ�����ͬ���ͣ�Ϊ������
				if(out==null) break;
			}
		}
	}

	/**
	 * ����һ����ά��
	 * @param is ������
	 * @return ��ά������
	 * @throws Exception
	 */
	public static Object[][] readTable(InputStream is) throws Exception{
		int rows = readInt(is);
		if(rows==0){
			return null;
		}
		Object[] data = new Object[rows];
		int cols = 0;
		for(int r=0;r<data.length;r++){
			Object[] row = readRowData( is ); 
			data[r] = row;
			if(r==0){
				cols = row.length;
			}
		}
		Object[][] table = new Object[rows][cols];
		for(int r = 0; r<rows; r++){
			Object[] row = (Object[])data[r];
			for(int c=0; c<cols; c++){
				table[r][c] = row[c];
			}
		}
		return table;
	}
	
	/**
	 * д��һ������������ݽṹ������
	 * @param os �����
	 * @param table ������
	 * @throws Exception
	 */
	public static void writeDatastructAndData(OutputStream os, Table table) throws Exception{
		String[] columns = table.dataStruct().getFieldNames();
		// ��д�ֶ�����
		int size = columns.length;
		writeInt(os, size);
		for (int i = 0; i < size; i++) {
			writeString(os, columns[i]);
		}
		// ��д��������
		writeTable(os, table);
	}
	
}
