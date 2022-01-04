package com.scudata.chart.element;

import java.util.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.RQException;
import com.scudata.dm.*;

import java.awt.Shape;
/**
 * ö����
 * ö�������ֵͨ��Ϊ�ַ�������Ҫ���������ϵ��ʱ������Ӣ�Ķ��ŷָ��Ĵ���ʾ
 * @author Joancy
 *
 */
public class EnumAxis extends TickAxis {

	// ö�������ԣ�ö��ֵ,�����ϵ�е�ֵ��ֻ���Ǵ�����Ϊ����ֵ��¼���ʽΪ "����ֵ,ϵ��ֵ"��������ȡ�ķ���ֱֵ��
	// ����ʹ�ã�����parseValue.
	public Sequence categories;
	public Sequence series;

	// ö�����϶��ϵ�п��ռ��
	public double gapRatio = 1.50;

	/**
	 * ȱʡ���������ö����
	 */
	public EnumAxis() {
	}

	/*
	 * getPhyValue ���ݵĸ�ʽΪ ����,ϵ�� �����ϵ��λ���ܱ����ԣ��������Ƕѵ����Σ�
	 */
	double getValueLength(Object val, boolean isAbsolute) {
		double len = 0;
		if (isAbsolute) {
			len = ((Number) val).doubleValue();
			len = getSeriesWidth() * len;
		} else {
			Object cat = Utils.parseCategory(val);
			Object ser = Utils.parseSeries(val);
			int catIndex = categories.firstIndexOf(cat);
			if (catIndex == 0)
				throw new RuntimeException(Dot.NOT_IN_DEFINE + ":" + cat);
			double j = 0;
			double serCount = t_serNum;
			if (ser == null) { // ��ʱ������Ƿ����ǩ��λ��
				j = serCount / 2f; // ��ǩλ�������ϵ�п�ȣ�����
			} else {
				int serIndex = series.firstIndexOf(ser);
				if (serIndex == 0)
					throw new RuntimeException(Dot.NOT_IN_DEFINE + ":" + ser);
				j = (serIndex - 1) + 0.5; // λ��Ϊ���Ӷ����м䣬�����ټ�
											// 0.5��ϵ�п��
											// ,��ֻ�з���ʱͳһ��������Ϊ���ӵĶ����м��
			}

			switch (location) {
			case Consts.AXIS_LOC_H:
			case Consts.AXIS_LOC_V:
			case Consts.AXIS_LOC_POLAR:
				// ����ֱ������ϵ���ۻ�����ͬ����ĸ�ϵ���ۼӣ����Լ���ö������ʱ��������������
				len = catIndex * t_categorySpan
						+ ((catIndex - 1) * serCount + j) * t_seriesWidth;// getLeftX()
																			// +
				break;
			case Consts.AXIS_LOC_ANGLE:
				// ����ֱ������ϵ���ۻ�����ͬ����ĸ�ϵ���ۼӣ����Լ���ö������ʱ��������������
				len = catIndex * t_categorySpan
						+ ((catIndex - 1) * serCount + j) * t_seriesWidth;// getLeftX()
				if(isCircleAngle()){
//					���᷶Χ����Բʱ��������һ������Ŀ�ȣ��õ�һ���������ڼ���λ��
					double tmp = t_categorySpan+ (serCount / 2f) * t_seriesWidth;
					len -= tmp;
				}
				break;
			}
		}
		return len;
	}

	private static void putAData(Sequence container, Object data, boolean putCategory) {
		if (data == null) {
			return;
		}
		Object tmp = null;
		Object cat = Utils.parseCategory(data);
		Object ser = Utils.parseSeries(data);

		if (putCategory) {
			tmp = cat;
		} else if (ser != null) {
			tmp = ser;
		}

		if (tmp != null && container.firstIndexOf(tmp) == 0) {
			container.add(tmp);
		}
	}

	/**
	 * ��ͼǰ׼������
	 */
	public void beforeDraw() {
		double length = getAxisLength();
		// ����Ľ�����Բʱ���׸�β��ͬһ�����࣬Ҫ��һ������Gap
		if (location == Consts.AXIS_LOC_ANGLE && isCircleAngle()) {
			t_seriesWidth = length
					/ ((t_catNum * gapRatio) + t_catNum * t_serNum);
		} else {
			t_seriesWidth = length
					/ (((t_catNum + 1) * gapRatio) + t_catNum * t_serNum);
		}
		t_categorySpan = t_seriesWidth * (gapRatio);

	}

	/**
	 * ����������data�г�ȡ���з��������
	 * @param data ���Ϸ������������ݴ�����
	 * @return ���������ɵ�����
	 */
	public static Sequence extractCatNames(Sequence data){
		int dSize = data.length();
		Sequence catNames = new Sequence();
		for (int j = 1; j <= dSize; j++) {
			Object one = data.get(j);
			putAData(catNames, one, true);
		}
		return catNames;
	}
	
	/**
	 * ����������data�г�ȡ����ϵ�е�����
	 * @param data ���Ϸ���ϵ�����������ݴ�����
	 * @return ϵ�������ɵ�����
	 */
	public static Sequence extractSerNames(Sequence data){
		int dSize = data.length();
		Sequence serNames = new Sequence();
		for (int j = 1; j <= dSize; j++) {
			Object one = data.get(j);
			putAData(serNames, one, false);
		}
		return serNames;
	}
	
	/**
	 * ��ͼǰ׼������������У��
	 * ö�����Ӧ��ͼԪ���ݸ�ʽΪ���֣� 1�� ����ֵ�� 2, ����ֵ,ϵ��ֵ
	 * @param dataElements ����ͼԪ�б�
	 */
	public void prepare(ArrayList<DataElement> dataElements) {
		super.prepare(dataElements);

		if (categories == null) {
			categories = new Sequence();
			for (int i = 0; i < dataElements.size(); i++) {
				DataElement de = dataElements.get(i);
				if(de.isPhysicalCoor()){
					continue;
				}
				
				Sequence data = de.getAxisData(name);
				if (data == null) {
					continue;
				}
				Sequence catNames = extractCatNames(data);
				for( int n=1; n<=catNames.length(); n++){
					Object cat = catNames.get(n);
					if( categories.contains(cat, false)) continue;
					categories.add(cat);
				}
			}
		}

		if (series == null) {
			series = new Sequence();
			for (int i = 0; i < dataElements.size(); i++) {
				DataElement de = (DataElement) dataElements.get(i);
				if(de.isPhysicalCoor()){
					continue;
				}
				
				Sequence data = de.getAxisData(name);
				if (data == null) {
					continue;
				}
				Sequence serNames = extractSerNames(data);
				for( int n=1; n<=serNames.length(); n++){
					Object ser = serNames.get(n);
					if( series.contains(ser, false)) continue;
					series.add(ser);
				}
			}
		}

		t_catNum = categories.length();
		if( t_catNum==0 )throw new RQException("Empty categories data of EnumAxis:[ "+name+" ]!");
		Object catVal = categories.get(1);
		if (!(catVal instanceof String)) {
			throw new RQException(
					"Category value must be 'String' type,current value is: "+catVal+",  and it's type is: "
							+ catVal.getClass().getName());
		}
		t_serNum = series.length() == 0 ? 1 : series.length();// û��ϵ��ʱ��ϵ����ĿΪ1����
		if (series.length() > 0) {
			Object serVal = series.get(1);
			if (!(serVal instanceof String)) {
				throw new RQException(
						"Series value must be 'String' type,current value is: "+serVal+",  and it's type is: "
								+ serVal.getClass().getName());
			}
		}

		t_coorValue.addAll(categories);
	}

	/**
	 * ��ȡͼԪ���ƺ��Ӧ�����ӵĿռ���״
	 * 
	 * @return Shape �����壬����null
	 */
	public Shape getShape() {
		return null;
	}

	/**
	 * ��ȡϵ�еĿ�ȣ���λ���أ�Ϊ�˷�ֹ��ͼԪ����ʱ
	 * ��ʹ������ֵ��Ҳ������doubleʵ��
	 * @return ϵ�п��
	 */
	public double getSeriesWidth() {
		return t_seriesWidth;
	}

	// ö�����ö�ٸ���
	private transient int t_catNum = 0;

	// ����ϵ����
	private transient int t_serNum = 1;

	private transient double t_categorySpan = 190, t_seriesWidth = 0;

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		
		ParamInfo.setCurrent(EnumAxis.class, this);

		String group = "enumaxis";
		paramInfos.add(group, new ParamInfo("categories"));
		paramInfos.add(group, new ParamInfo("series"));
		paramInfos.add(group, new ParamInfo("gapRatio", Consts.INPUT_DOUBLE));
		// paramInfos.add(group, new ParamInfo("coorWidthRate",
		// Consts.INPUT_DOUBLE));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}

	
	/**
	 * �Ƿ�ö����
	 * @return true
	 */
	public boolean isEnumAxis() {
		return true;
	}

	/**
	 * �Ƿ�������
	 * @return false
	 */
	public boolean isDateAxis() {
		return false;
	}

	/**
	 * �Ƿ���ֵ��
	 * @return false
	 */
	public boolean isNumericAxis() {
		return false;
	}

	public void checkDataMatch(Sequence data){
		if(data!=null && data.length()>1){
			Object one = data.get(1);
			if(!(one instanceof String)){
				throw new RuntimeException("Axis "+name+" is enumeration axis, error data got:" + one);
			}
		}
	}
	
	public double animateDoubleValue(Object val){
		throw new RuntimeException("Enumeration axis does not support animate double value.");
	}
}
