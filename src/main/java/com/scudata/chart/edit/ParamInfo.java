package com.scudata.chart.edit;

import java.lang.reflect.*;

import com.scudata.chart.*;
import com.scudata.chart.resources.*;
import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.expression.*;

import java.awt.*;

/**
 * ������Ϣ�࣬��������ĳ����������ı༭����
 * @author Joancy
 *
 */
public class ParamInfo extends ChartParam {

	private String title;
	private int inputType;
	private boolean axisEnable = false;
	private Object defValue;// ֻ���༭�õ�ȱʡֵ������ɾ�����ʽ���ø�ֵ��Ⱦ����ֵ�����ȱʡֵһ��

	private static transient Class currentClass;
	private static transient Object currentObj;

	private MessageManager mm = ChartMessage.get();

	/**
	 * ͼԪ�ļ̳��Ƕ��ģ�Ϊ��׼ȷ��λ����������һ�㸸��
	 * ʹ�ø÷���������ǰ������
	 * @param objClass �����ĳ�㸸��
	 * @param obj �����ʵ������
	 */
	public static void setCurrent(Class objClass, Object obj) {
		currentClass = objClass;
		currentObj = obj;
	}

	/**
	 * ����һ������༭���͵Ĳ�����Ϣ
	 * @param name ��������
	 */
	public ParamInfo(String name) {
		this(name, Consts.INPUT_NORMAL);
	}

	/**
	 * ����ָ�������༭���͵Ĳ�����Ϣ
	 * @param name ����
	 * @param inputType �༭���ͣ�ֵ�ο���Consts.INPUT_XXX
	 */
	public ParamInfo(String name, int inputType) {
		this.name = name;
		try {
			Field f = currentClass.getDeclaredField(name);
			Object paraValue = f.get(currentObj);
			if (paraValue instanceof Para) {
				this.value = ((Para) paraValue).getValue();
				this.axisEnable = true;
			} else if (paraValue instanceof Color) {
				this.value = new Integer(((Color) paraValue).getRGB());
				// }else if(paraValue instanceof Sequence){
				// this.value = getSequenceEditExp( seq );
			} else {
				this.value = paraValue;
			}
			this.title = mm.getMessage(name);
			this.inputType = inputType;
			this.defValue = value;
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * ��ȡ�ò�����ȱʡֵ
	 * @return ֵ
	 */
	public Object getDefValue() {
		return defValue;
	}

	private String getSequenceEditExp(Sequence seq) {
		return "=" + seq.toString();
	}

	/**
	 * ���ݻ���cp�����ݣ����õ�ǰ����
	 * @param cp �������
	 */
	public void setChartParam(ChartParam cp) {
		Object tmp = cp.getValue();
		if (tmp instanceof Sequence) {
			Sequence seq = (Sequence) tmp;
			tmp = Utils.sequenceToChartColor(seq);
			if (tmp == null) {
				tmp = getSequenceEditExp(seq);
			}
		}
		value = tmp;

		if (axisEnable) {
			axis = cp.getAxis();
		}
	}

	public boolean isAxisEnable() {
		return axisEnable;
	}

	/**
	 * ��ȡ��������
	 * @return ��������
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * ���ò����ı���
	 * @param title ��������
	 */
	public void setTitle(String title){
		this.title = title;
	}

	/**
	 * ��ȡ�����ı༭����
	 * @return Consts�ж���õı༭����
	 */
	public int getInputType() {
		return inputType;
	}

}
