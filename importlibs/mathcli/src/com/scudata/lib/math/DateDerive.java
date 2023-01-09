package com.scudata.lib.math;

import com.scudata.lib.math.prec.Consts;
import com.scudata.lib.math.prec.DateRec;
import com.scudata.lib.math.prec.VarRec;
import com.scudata.lib.math.prec.VarSrcInfo;
import com.scudata.resources.EngineMessage;
import com.scudata.common.MessageManager;

import java.util.ArrayList;
import java.util.Calendar;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.util.Variant;

/**
 * ��ֵĿ������ľ�ƫ����
 * @author bd
 * A.datederive(T)/P.datederive(cn, T), @bnie ѡ��ָ��Ŀ�����ͣ���ѡ����⣬���ȼ����ն�ֵ/��ֵ/����/ö�٣���ѡ���Զ�����
 * A.datederive@r(rec)/P.datederive@r(cn, rec)
 * �������ݼ�D����ɢ����V
 */
public class DateDerive extends SequenceFunction {

	public Object calculate(Context ctx) {
		if (srcSequence == null || srcSequence.length() < 1) {
			return srcSequence;
		}
		boolean re = option != null && option.indexOf('r') > -1;
		String cn = "datederive";
		if (re) {
			if (param == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("datederive@" + option + " " + mm.getMessage("function.invalidParam"));
			}
			Sequence seq = srcSequence;
			Object o1 = null;
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				if (param.isLeaf() || param.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null || sub2 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Record r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				o1 = sub2.getLeafExpression().calculate(ctx);
				Object o2 = sub1.getLeafExpression().calculate(ctx);
				int col = 0;
				if (o2 == null || !(o1 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				if (o2 instanceof Number) {
					col = ((Number) o2).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o2.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				seq = Prep.getFieldValues(srcSequence, col);
			}
			else {
				if (!param.isLeaf()) {
					if (param.getSubSize() != 2) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("datederive@" + option + " " + mm.getMessage("function.invalidParam"));
					}
					IParam sub1 = param.getSub(0);
					IParam sub2 = param.getSub(1);
					if (sub1 == null || sub2 == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("datederive@" + option + " " + mm.getMessage("function.invalidParam"));
					}
					o1 = sub1.getLeafExpression().calculate(ctx);
					Object o2 = sub2.getLeafExpression().calculate(ctx);
					if (o2 == null || !(o1 instanceof Sequence)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("datederive@" + option + " " + mm.getMessage("function.paramTypeError"));
					}
					cn = o2.toString();
				}
				else {
					o1 = param.getLeafExpression().calculate(ctx);
					if (!(o1 instanceof Sequence)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("datederive@" + option + " " + mm.getMessage("function.paramTypeError"));
					}
				}
			}
			DateRec dRec = new DateRec();
			dRec.init((Sequence) o1); 
			
			ArrayList<String> cns = new ArrayList<String>();
			ArrayList<Sequence> cvs = new ArrayList<Sequence>();
			datederive(dRec, seq, cn, cvs, cns);
			Table tab = Prep.toTab(cns, cvs);
			return tab;
		}
		else {
			Sequence seq = srcSequence;
			Sequence tvs = null;
			byte tType = 0;
			if (option != null) {
				if (option.indexOf('b') > -1) {
					tType = Consts.F_TWO_VALUE;
				}
				else if (option.indexOf('n') > -1) {
					tType = Consts.F_NUMBER;
				}
				else if (option.indexOf('i') > -1) {
					tType = Consts.F_COUNT;
				}
				else if (option.indexOf('e') > -1) {
					tType = Consts.F_ENUM;
				}
			}
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				if (param == null ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive" + mm.getMessage("function.invalidParam"));
				}
				Record r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				Object o1 = null;
				int psize = 0;
				if (param.isLeaf()) {
					o1 = param.getLeafExpression().calculate(ctx);
				}
				else {
					psize = param.getSubSize();
				}
				if (psize > 0) {
					IParam sub = param.getSub(0);
					if (sub != null) {
						o1 = sub.getLeafExpression().calculate(ctx);
					}
				}
				int col = 0;
				if (o1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive" + mm.getMessage("function.paramTypeError"));
				}
				else if (o1 instanceof Number) {
					col = ((Number) o1).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o1.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				if (psize > 1) {
					IParam sub = param.getSub(1);
					if (sub != null) {
						Object o2 = sub.getLeafExpression().calculate(ctx);
						if (o2 instanceof Sequence) {
							tvs = (Sequence) o2;
						}
					}
				}
				seq = Prep.getFieldValues(srcSequence, col);
			}
			else {
				if (param.isLeaf()) {
					Object o1 = param.getLeafExpression().calculate(ctx);
					if ( !(o1 instanceof Sequence)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("smooth" + mm.getMessage("function.paramTypeError"));
					}
					tvs = (Sequence) o1;
				}
				else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("datederive" + mm.getMessage("function.invalidParam"));
				}
			}
			if (tType < 1) {
				tType = Prep.getType(tvs);
			}
			ArrayList<Sequence> ncv = new ArrayList<Sequence>();
			ArrayList<String> ncn = new ArrayList<String>();
			DateRec dr = datederive(seq, cn, tvs, tType, ncv, ncn);
			Sequence result = new Sequence(2);
			Table tab = Prep.toTab(ncn, ncv);
			result.add(tab);
			result.add(dr.toSeq());
			return result;
		}
	}

	protected static DateRec datederive(Sequence dvs, String cn, Sequence tvs, byte tType, ArrayList<Sequence> cvs, ArrayList<String> cns) {
		DateRec dr = new DateRec(null);
		datederive(dvs, cn, dr, tvs, tType, cvs, cns);
		return dr;
	}

	/**
	 * ����ֵĿ���������ƫ����
	 * @param tvs	��ֵĿ���������ֵ
	 * @param cn	������
	 * @param filePath	���������Ҫ���������������ݽ϶�ʱ�����ļ���·��
	 * @return
	 */
	protected static void datederive(Sequence dvs, String cn, DateRec dr, Sequence tvs, byte tType,
			ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		//VarSrcInfo vsi = new VarSrcInfo(cn, Consts.F_DATE);
		//vsi.init(dvs);
		
		// step04��abcd�������ֶ���ִ�й�һ����
		//���̣�
		//	(a) ����ʱ�������ͱ����Ĺ��ɣ����date��time�����֡�һ��ʱ�����ڱ������ٻ����date��time֮һ������߽��С�������������֣�ִ������Ĳ���
		int size = dvs.length();
		Object v1 = dvs.get(1);
		ArrayList<VarSrcInfo> vis = new ArrayList<VarSrcInfo>(3);
		//vsi.setDateCols(vis);
		
		//���ǵ����ڴ����ģ��ڲ��д���Ĺ����У����������е����ɲ��ö��ѭ��һ������һ�еķ�ʽ
		byte type = 0;
		if (v1 instanceof java.sql.Timestamp) {
			//����ʱ������
			type = Consts.DCT_DATETIME;
		}
		else if (v1 instanceof java.sql.Date) {
			//��������
			type = Consts.DCT_DATE;
		}
		else if (v1 instanceof java.sql.Time) {
			//ʱ������
			type = Consts.DCT_TIME;			
		}
		else if (v1 instanceof java.util.Date) {
			//��������ʱ������
			type = Consts.DCT_UDATE;
		}
		dr.setDateType(type);
		
		Calendar gc = Calendar.getInstance();
		java.util.Date now = new java.util.Date();
		dr.setNow(now);
		Sequence newCvs = null;
		String newCn = cn;

		if (type == Consts.DCT_DATETIME || type == Consts.DCT_UDATE || type == Consts.DCT_TIME ) {
			//����ʱ�����������ʱ������ �� ʱ������
			//	(b) ����time���֣����������������ֶ���_Hour����ֵΪ0-23������Ϊ���������
			newCn = cn + "_Hour";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf(hour));
				}
				else {
					//gc.setTime(now);
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			VarSrcInfo vi = new VarSrcInfo(newCn, Consts.F_ENUM);
			vi.init(newCvs);
			vis.add(vi);
			double freq = vi.getMissingRate();
			VarRec vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_ENUM);
			// dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_ENUM,
			// 		ResultCol.CL_DATE, (byte) 1, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);
			
			// ���������������ֶ���_is_AM���� ����Ϊ��ֵ������1��ʾhour=0-11��0��ʾhour=12-23��
			newCn = cn + "_is_AM";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf(hour>11 ? 0 : 1));
				}
				else {
					//gc.setTime(now);
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			vi = new VarSrcInfo(newCn, Consts.F_TWO_VALUE);
			vi.init(newCvs);
			vis.add(vi);
			freq = vi.getMissingRate();
			vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_TWO_VALUE);
			//dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_TWO_VALUE,
			//		ResultCol.CL_DATE, (byte) 2, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);
			
			// ���������������ֶ���_is_night���� ����Ϊ��ֵ������1��ʾhour=18-5��0��ʾhour=6-17��
			newCn = cn + "_is_night";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf((hour>=6 && hour < 18) ? 0 : 1));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			vi = new VarSrcInfo(newCn, Consts.F_TWO_VALUE);
			vi.init(newCvs);
			vis.add(vi);
			freq = vi.getMissingRate();
			vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_TWO_VALUE);
			//dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_TWO_VALUE,
			//		ResultCol.CL_DATE, (byte) 3, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_TWO_VALUE, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);
		}
		else {
			dr.addDeriveRecs(null);
			dr.addDeriveRecs(null);
			dr.addDeriveRecs(null);
		}
		
		if (type == Consts.DCT_DATETIME || type == Consts.DCT_UDATE || type == Consts.DCT_DATE ) {
			//�������ڷ���������ʱ������ �� ��������
			//	(c) ����date���֣����������������ֶ���_Month����ֵΪ1-12������Ϊ���������
			newCn = cn + "_Month";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int month = gc.get(Calendar.MONTH);
					newCvs.add(Integer.valueOf(month));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			VarSrcInfo vi = new VarSrcInfo(newCn, Consts.F_ENUM);
			vi.init(newCvs);
			vis.add(vi);
			double freq = vi.getMissingRate();
			VarRec vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_ENUM);
			//dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_ENUM,
			//		ResultCol.CL_DATE, (byte) 4, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);
			
			// ���������������ֶ���_Season���� Month=3\4\5Ϊ	��spring����month=6\7\8Ϊ��summer����
			// month=9\10\11Ϊ��autumn����	month=12\1\2Ϊ��winter���� ����Ϊ���������
			newCn = cn + "_Season";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int month = gc.get(Calendar.MONTH);
					int season = month/3;
					if (season < 1) {
						season = 4;
					}
					newCvs.add(Integer.valueOf(season));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			vi = new VarSrcInfo(newCn, Consts.F_ENUM);
			vi.init(newCvs);
			vis.add(vi);
			freq = vi.getMissingRate();
			vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_ENUM);
			//dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_ENUM, 
			//		ResultCol.CL_DATE, (byte) 5, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);

			// ���������������ֶ���	_weekday���� ֵΪ��Monday��-��Sunday��������Ϊ���������
			newCn = cn + "_weekday";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int weekday = gc.get(Calendar.DAY_OF_WEEK);
					newCvs.add(Integer.valueOf(weekday));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			vi = new VarSrcInfo(newCn, Consts.F_ENUM);
			vi.init(newCvs);
			vis.add(vi);
			freq = vi.getMissingRate();
			vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_ENUM);
			//dealEnum(newCvs, newCn, pr, freq, vr, Consts.F_ENUM, 
			//		ResultCol.CL_DATE, (byte) 6, cn, vi);
			Prep.dealEnum(newCvs, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
			dr.addDeriveRecs(vr);
			
			// ���������������ֶ���_length_to_today����ʾ�ֶ����ں͵�ǰ�������������
			// ����Ϊ��ֵ������
			newCn = cn + "_length_to_today";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					newCvs.add(Integer.valueOf((int) Variant.interval(udv, now, null)));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			vi = new VarSrcInfo(newCn, Consts.F_NUMBER);
			vi.init(newCvs);
			vis.add(vi);
			freq = vi.getMissingRate();
			vr = new VarRec(false, false, vi);
			vr.setType(Consts.F_NUMBER);
			//vr = dealNumerical(newCvs, newCn, pr, freq, vr, Consts.F_NUMBER,
			//		ResultCol.CL_DATE, (byte) 7, cn, vi);
			Prep.dealNumerical(newCvs, newCn, freq, vr, Consts.F_NUMBER, null, ncv, ncn);
			dr.addDeriveRecs(vr);
		}
		else {
			dr.addDeriveRecs(null);
			dr.addDeriveRecs(null);
			dr.addDeriveRecs(null);
			dr.addDeriveRecs(null);
		}
	}
	
	protected static void datederive(DateRec dr, Sequence dvs, String cn, ArrayList<Sequence> ncv, 
			ArrayList<String> ncn) {
		// step04
		//���̣�
		//	(a) ����ʱ�������ͱ����Ĺ��ɣ����date��time�����֡�һ��ʱ�����ڱ������ٻ����date��time֮һ������߽��С�������������֣�ִ������Ĳ���
		int size = dvs.length();
		
		Calendar gc = Calendar.getInstance();
		Sequence newCvs = null;
		String newCn = cn;
		
		ArrayList<VarRec> vrs = dr.getDeriveRecs();
		//	(b) ����time���֣����������������ֶ���_Hour����ֵΪ0-23������Ϊ���������
		VarRec vr = vrs.get(0);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_Hour";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf(hour));
				}
				else {
					//gc.setTime(now);
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
			//reprep(vr, newCvs, newCn, Consts.F_ENUM, pr);
		}
		// ���������������ֶ���_is_AM���� ����Ϊ��ֵ������1��ʾhour=0-11��0��ʾhour=12-23��
		vr = vrs.get(1);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_is_AM";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf(hour>11 ? 0 : 1));
				}
				else {
					//gc.setTime(now);
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
			//reprep(vr, newCvs, newCn, Consts.F_TWO_VALUE, pr);
		}

		// ���������������ֶ���_is_night���� ����Ϊ��ֵ������1��ʾhour=18-5��0��ʾhour=6-17��
		vr = vrs.get(2);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_is_night";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int hour = gc.get(Calendar.HOUR_OF_DAY);
					newCvs.add(Integer.valueOf((hour>=6 && hour < 18) ? 0 : 1));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			//reprep(vr, newCvs, newCn, Consts.F_TWO_VALUE, pr);
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
		}

		//	(c) ����date���֣����������������ֶ���_Month����ֵΪ1-12������Ϊ���������
		vr = vrs.get(3);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_Month";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int month = gc.get(Calendar.MONTH);
					newCvs.add(Integer.valueOf(month));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			//reprep(vr, newCvs, newCn, Consts.F_ENUM, pr);
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
		}

		// ���������������ֶ���_Season���� Month=3\4\5Ϊ	��spring����month=6\7\8Ϊ��summer����
		// month=9\10\11Ϊ��autumn����	month=12\1\2Ϊ��winter���� ����Ϊ���������
		vr = vrs.get(4);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_Season";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int month = gc.get(Calendar.MONTH);
					int season = month/3;
					if (season < 1) {
						season = 4;
					}
					newCvs.add(Integer.valueOf(season));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			//reprep(vr, newCvs, newCn, Consts.F_ENUM, pr);
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
		}

		// ���������������ֶ���	_weekday���� ֵΪ��Monday��-��Sunday��������Ϊ���������
		vr = vrs.get(5);
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_weekday";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					gc.setTime(udv);
					int weekday = gc.get(Calendar.DAY_OF_WEEK);
					newCvs.add(Integer.valueOf(weekday));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			//reprep(vr, newCvs, newCn, Consts.F_ENUM, pr);
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
		}

		// ���������������ֶ���_length_to_today����ʾ�ֶ����ں͵�ǰ�����������.  ����Ϊ��ֵ������
		vr = vrs.get(6);
		java.util.Date now = dr.getNow();
		if (vr != null && !vr.onlyHasMI()) {
			newCn = cn + "_length_to_today";
			newCvs = new Sequence(size);
			for (int i = 1; i<=size; i++ ) {
				Object vo = dvs.get(i);
				if (vo instanceof java.util.Date) {
					java.util.Date udv = (java.util.Date) vo;
					newCvs.add(Integer.valueOf((int) Variant.interval(udv, now, null)));
				}
				else {
					//���ַ�����ʱ������ݣ����߿�ֵ����ʱ���������о��ÿ�ֵ������������ʱ����
					newCvs.add(null);
				}
			}
			//reprep(vr, newCvs, newCn, Consts.F_NUMBER, pr);
			Prep.prep(vr, newCvs, newCn, ncv, ncn);
		}
	}
}
