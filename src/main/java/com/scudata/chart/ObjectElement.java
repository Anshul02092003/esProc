package com.scudata.chart;

import java.lang.reflect.Field;

import com.scudata.chart.resources.ChartMessage;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.expression.ChartParam;

/**
 * ����ͼԪ����ͼԪ��ͼ�α����Ǹ�������࣬��ͼԪ�����Ա���Ϊpublic��������Զ����л���ͼԪ����ֵ
 * ��ͼԪ���Զ����ü̳��˸��������ֵ��
 */
public abstract class ObjectElement implements IElement{
	MessageManager mm = ChartMessage.get();
	
	public void loadProperties(Sequence params){
		setParams(getClass(),this,params);
	}

	private void setParams(Class elementClass, IElement elementObject,
			Sequence chartParams) {
		int size = chartParams.length();
		for (int i = 1; i <= size; i++) {
			ChartParam cp = (ChartParam) chartParams.get(i);
			Para p = new Para(cp.getValue(), cp.getAxis(), cp.getName());
			Field f = null;
			try {
				f = elementClass.getField(cp.getName());
				if (p.getValue() == null) {
					// 2014.8.20 ����ֵΪnull�������������������������л��кܶ಻��Ԥ�ϵ�null�쳣
					String cName = elementClass.getName();
					int lastIndex = cName.lastIndexOf('.')+1;
					cName = cName.substring(lastIndex);
					String pName = mm.getMessage(cp.getName());
					throw new RQException(mm.getMessage("ObjectElement.nullvalue",cName,pName));
//							elementClass.getName()
//							+ ": property [ " + cp.getName()
//							+ " ]'s value can not be null!");
				}
				String className = f.getType().getName().toLowerCase();
				if (className.endsWith("boolean")) {
					f.set(elementObject, new Boolean(p.booleanValue()));
				} else if (className.endsWith("byte")) {
					f.set(elementObject, new Byte((byte) p.intValue()));
				} else if (className.endsWith("int")
						|| className.endsWith("integer")) {
					f.set(elementObject, new Integer(p.intValue()));
				} else if (className.endsWith("float")) {
					f.set(elementObject, new Float(p.floatValue()));
				} else if (className.endsWith("double")) {
					f.set(elementObject, new java.lang.Double(p.doubleValue()));
				} else if (className.endsWith(".color")) {// ���ϵ�Ͳ����chartcolor����color��
					f.set(elementObject, p.colorValue(0));
				} else if (className.endsWith("string")) {
					f.set(elementObject, p.stringValue());
				} else if (className.endsWith("sequence")) {
					f.set(elementObject, p.sequenceValue());
				} else if (className.endsWith("date")) {
					f.set(elementObject, p.dateValue());
				} else if (className.endsWith("chartcolor")) {
					f.set(elementObject, p.chartColorValue());
				} else if (className.endsWith("object")) {
					f.set(elementObject, cp.getValue());
				} else {
					Para defP = (Para)f.get(elementObject);//��ȱʡ���Ե�ͼ�����ã���ֵ���±���
					if(defP!=null){
						p.setLegendProperty(defP.getLegendProperty());
					}
					f.set(elementObject, p);
				}
			} catch (java.lang.NoSuchFieldException nField) {
			} catch (RQException rqe) {
				throw rqe;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	
}
