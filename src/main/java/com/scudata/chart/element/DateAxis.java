package com.scudata.chart.element;

import java.awt.geom.*;
import java.util.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.dm.*;
import com.scudata.util.*;
/**
 * ������
 * ��������ΪDate���ᣬΪ�������ԣ�����Ҳ����д�ɴ����͵�����
 * @author Joancy
 *
 */
public class DateAxis extends TickAxis {
	// �Զ��������Сֵ�ķ�Χ
	public boolean autoCalcValueRange = true;
	// ֵ�����ԣ����ֵ
	public Date endDate = GregorianCalendar.getInstance().getTime();

	// ֵ�����ԣ���Сֵ
	public Date beginDate = endDate;

	// ֵ�����ԣ��̶ȵ�λ
	public int scaleUnit = Consts.DATEUNIT_DAY;

	// ֵ�����ԣ��̶���ʾ��ʽ
	public String format = "yyyy/MM/dd";

	/**
	 * ȱʡֵ���캯��
	 */
	public DateAxis() {
	}

	/**
	 * ������ͼԪ�ı༭������Ϣ�б�
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(DateAxis.class, this);

		String group = "dateaxis";
		paramInfos.add(group, new ParamInfo("autoCalcValueRange",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("endDate", Consts.INPUT_DATE));
		paramInfos.add(group, new ParamInfo("beginDate", Consts.INPUT_DATE));
		paramInfos
				.add(group, new ParamInfo("scaleUnit", Consts.INPUT_DATEUNIT));
		paramInfos.add(group, new ParamInfo("format"));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}

	/**
	 * �����ڰ�getTime()ȡ�������Լ���ֵ���͵�ֵ������ʵ��
	 * �ú�����������Ҫ���ڼ��������Լ���ֵ�ľ�������
	 * @param date ����Ϊ��ֵ�����ڣ����ߴ���������������
	 * @return ʵ�����ȵľ���ֵ
	 */
	public static double getDoubleDate(Object date) {
		if (date instanceof Number) {
			return ((Number) date).doubleValue();
		} else if (date instanceof Date) {
			return ((Date) date).getTime();
		} else {
			Object obj = Variant.parseDate(date.toString());
			if (obj instanceof Date) {
				return ((Date) obj).getTime();
			} else {
				throw new RuntimeException("Wrong data: " + date
						+ " on calculating on axis. ");// + axisName);
			}
		}
	}

	/*
	 * @return Object�����Ϊ����ԭ��ĳ��� ����Ǽ�����Ϊ���᳤�� ����ǽ�����Ϊ���ԽǶ�
	 */
	double getValueLength(Object val, boolean isAbsolute) {
		double len = 0;
		double axisLen = getAxisLength();
		if (isAbsolute) {
			len = ((Number) val).doubleValue();
			long valMillSecs = (long) (len * 24 * 60 * 60 * 1000);// ��val���������ת��Ϊ������
			len = axisLen * (valMillSecs / (t_maxDate - t_minDate));
		} else {
			double tmp = getDoubleDate(val);
			len = axisLen * (tmp - t_minDate) / (t_maxDate - t_minDate);
		}

		return len;
	}

	/**
	 * ��ȡ��ֵ�������
	 * ������ͼԪ�Ļ��ƣ�һ���Ǵӻ�ֵ������һ�߶�
	 * @return Point �����
	 */
	public Point2D getRootPoint() {
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			return new Point2D.Double(t_valueBaseLine, getBottomY());
		case Consts.AXIS_LOC_V:
			return new Point2D.Double(getLeftX(), t_valueBaseLine);
		}
		return null;
	}


	/**
	 * ��ȡv������d������ֵ�ȽϺ�����ֵ
	 * @param v ֵ����
	 * @param d ���Ƚϵ�ֵ
	 * @return ���ֵ
	 */
	public static  double max(double v, Sequence d) {
		Sequence al = (Sequence) d;
		double max = getDoubleDate(al.max());
		if (v > max) {
			return v;
		}
		return max;
	}

	/**
	 * ��ȡv������d������ֵ�ȽϺ����Сֵ
	 * @param v ֵ����
	 * @param d ���Ƚϵ�ֵ
	 * @return ��Сֵ
	 */
	public static double min(double v, Sequence d) {
		Sequence al = (Sequence) d;
		double min = getDoubleDate(al.min());
		if (v < min) {
			return v;
		}
		return min;
	}

	/**
	 * ��ͼǰ������׼������
	 */
	public void prepare(ArrayList<DataElement> dataElements) {
		super.prepare(dataElements);

		if (autoCalcValueRange) {
			for (int i = 0; i < dataElements.size(); i++) {
				DataElement de = dataElements.get(i);
				if(de.isPhysicalCoor()){
					continue;
				}
				
				Sequence data = de.getAxisData(name);
				t_minDate = min(t_minDate, data);
				t_maxDate = max(t_maxDate, data);
				if(de instanceof Column){
					Column col = (Column)de;
					data = col.getData3();
					if(data!=null && data.length()>0){
						Object one = data.get(1);
						if(one instanceof Date){
							t_minDate = min(t_minDate, data);
							t_maxDate = max(t_maxDate, data);}
					}
				}
			}
		} else {
			t_maxDate = Math.max(endDate.getTime(), beginDate.getTime());
			t_minDate = Math.min(endDate.getTime(), beginDate.getTime());
		}

		Date start = new Date((long)t_minDate);
		Date end = new Date((long)t_maxDate);

		// ���ڱ�ǩtick�Ѿ�����tickStep�����˿̶ȣ����Բ����̶Ⱥ�tickStep����Ϊ1
		createCoorValue(start, end, scaleUnit, displayStep, t_coorValue);
		displayStep = 1;
	}

	private void createCoorValue(Date start, Date end, int scale, int step,
			Sequence v) {
		if (step < 1) {
			step = 1;
		}
		GregorianCalendar gc = new GregorianCalendar();
		switch (scale) {
		case Consts.DATEUNIT_YEAR:
			gc.setTime(end);
			int endY = gc.get(GregorianCalendar.YEAR);
			gc.setTime(start);
			int startY = gc.get(GregorianCalendar.YEAR);
			while (startY <= endY) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.YEAR, step);
				startY = gc.get(GregorianCalendar.YEAR);
			}
			break;
		case Consts.DATEUNIT_MONTH:
			gc.setTime(end);
			endY = gc.get(GregorianCalendar.YEAR);
			int endM = gc.get(GregorianCalendar.MONTH);
			gc.setTime(start);
			startY = gc.get(GregorianCalendar.YEAR);
			int startM = gc.get(GregorianCalendar.MONTH);
			while (startY < endY || (startY == endY && startM <= endM)) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.MONTH, step);
				startY = gc.get(GregorianCalendar.YEAR);
				startM = gc.get(GregorianCalendar.MONTH);
			}
			break;
		case Consts.DATEUNIT_DAY:
			gc.setTime(end);
			endY = gc.get(GregorianCalendar.YEAR);
			endM = gc.get(GregorianCalendar.MONTH);
			int endD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			gc.setTime(start);
			startY = gc.get(GregorianCalendar.YEAR);
			startM = gc.get(GregorianCalendar.MONTH);
			int startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			while (startY < endY
					|| (startY == endY && (startM < endM || (startM == endM && startD <= endD)))) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.DAY_OF_MONTH, step);
				startY = gc.get(GregorianCalendar.YEAR);
				startM = gc.get(GregorianCalendar.MONTH);
				startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			}
			break;
		case Consts.DATEUNIT_HOUR:
			gc.setTime(end);
			endY = gc.get(GregorianCalendar.YEAR);
			endM = gc.get(GregorianCalendar.MONTH);
			endD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			int endH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			gc.setTime(start);
			startY = gc.get(GregorianCalendar.YEAR);
			startM = gc.get(GregorianCalendar.MONTH);
			startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			int startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			while (startY < endY
					|| (startY == endY && (startM < endM || (startM == endM && (startD < endD || (startD == endD && startH <= endH)))))) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.HOUR_OF_DAY, step);
				startY = gc.get(GregorianCalendar.YEAR);
				startM = gc.get(GregorianCalendar.MONTH);
				startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
				startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			}
			break;
		case Consts.DATEUNIT_MINUTE:
			gc.setTime(end);
			endY = gc.get(GregorianCalendar.YEAR);
			endM = gc.get(GregorianCalendar.MONTH);
			endD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			endH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			int endMM = gc.get(GregorianCalendar.MINUTE);
			gc.setTime(start);
			startY = gc.get(GregorianCalendar.YEAR);
			startM = gc.get(GregorianCalendar.MONTH);
			startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			int startMM = gc.get(GregorianCalendar.MINUTE);
			while (startY < endY
					|| (startY == endY && (startM < endM || (startM == endM && (startD < endD || (startD == endD
							&& startH < endH || (startH == endH && startMM <= endMM))))))) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.MINUTE, step);
				startY = gc.get(GregorianCalendar.YEAR);
				startM = gc.get(GregorianCalendar.MONTH);
				startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
				startMM = gc.get(GregorianCalendar.MINUTE);
				startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			}
			break;
		case Consts.DATEUNIT_SECOND:
			gc.setTime(end);
			endY = gc.get(GregorianCalendar.YEAR);
			endM = gc.get(GregorianCalendar.MONTH);
			endD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			endH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			endMM = gc.get(GregorianCalendar.MINUTE);
			int endS = gc.get(GregorianCalendar.SECOND);
			gc.setTime(start);
			startY = gc.get(GregorianCalendar.YEAR);
			startM = gc.get(GregorianCalendar.MONTH);
			startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
			startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
			startMM = gc.get(GregorianCalendar.MINUTE);
			int startS = gc.get(GregorianCalendar.SECOND);
			while (startY < endY
					|| (startY == endY && (startM < endM || (startM == endM && (startD < endD || (startD == endD
							&& startH < endH || (startH == endH && (startMM < endMM || (startMM == endMM && startS <= endS))))))))) {
				Date d = gc.getTime();
				v.add(d);
				gc.add(GregorianCalendar.SECOND, step);
				startY = gc.get(GregorianCalendar.YEAR);
				startM = gc.get(GregorianCalendar.MONTH);
				startD = gc.get(GregorianCalendar.DAY_OF_MONTH);
				startMM = gc.get(GregorianCalendar.MINUTE);
				startH = gc.get(GregorianCalendar.HOUR_OF_DAY);
				startS = gc.get(GregorianCalendar.SECOND);
			}
			break;
		}
	}

	String getCoorText(Object coorValue) {
		Date coory = (Date) coorValue;
		return Utils.format(coory, format);
	}

	/**
	 * ÿ���ػ�ǰ���ݵ�ǰ�����ĳߴ磬���������ʱ���Ʋ���
	 */
	public void beforeDraw() {
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			t_valueBaseLine = (int) getLeftX();
			break;
		case Consts.AXIS_LOC_V:
			t_valueBaseLine = (int) getBottomY();
			break;
		case Consts.AXIS_LOC_ANGLE:
			// nothing to do
			break;
		}
	}

	/**
	 * �Ƿ�ö����
	 * @return false
	 */
	public boolean isEnumAxis(){
		return false;
	}

	/**
	 * �Ƿ�������
	 * @return true
	 */
	public boolean isDateAxis(){
		return true;
	}
	
	/**
	 * �Ƿ���ֵ��
	 * @return false
	 */
	public boolean isNumericAxis(){
		return false;
	}

	public void checkDataMatch(Sequence data){
		if(data!=null && data.length()>1){
			Object one = data.get(1);
			getDoubleDate(one);
		}
	}
	/**
	 * ���߼�ֵvalת��Ϊʱ������ֵ
	 */
	public double animateDoubleValue(Object val){
		return getDoubleDate( val );
	}

	// ���л���ͼ����ص��м�����������transient�޶���ָ�������ұ�������t_��Ϊǰ׺
	// ������λ
	private transient double t_maxDate=0, t_minDate=Long.MAX_VALUE;
	private transient int t_valueBaseLine = 0;
}
