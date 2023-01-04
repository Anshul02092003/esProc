package com.scudata.lib.math;

import com.scudata.lib.math.prec.Consts;
import com.scudata.lib.math.prec.DiMvpRec;
import com.scudata.lib.math.prec.VarDateInterval;
import com.scudata.lib.math.prec.VarInfo;
import com.scudata.lib.math.prec.VarRec;
import com.scudata.resources.EngineMessage;
import com.scudata.common.MessageManager;

import java.util.ArrayList;

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
 * A.dateinterval(T)/P.dateinterval(cns, T); @bnie ѡ��ָ��Ŀ�����ͣ���ѡ����⣬���ȼ����ն�ֵ/��ֵ/����/ö�٣���ѡ���Զ�����
 * A.dateinterval@r(rec),A.dateinterval@r(cns, rec)
 * 
 * �������ݼ�D����ɢ����V
 */
public class DateInterval extends SequenceFunction {

	public Object calculate(Context ctx) {
		if (srcSequence == null || srcSequence.length() < 1) {
			return srcSequence;
		}
		boolean re = option != null && option.indexOf('r') > -1;
		if (re) {
			if (param == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.invalidParam"));
			}
			DiMvpRec dmr = new DiMvpRec();
			String[] cns = null;
			ArrayList<Sequence> seqs = null;
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				Record r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param == null ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				if (param.isLeaf() || param.getSubSize() < 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				Object o1 = sub1 == null? null : sub1.getLeafExpression().calculate(ctx);
				Object o2 = sub2 == null? null : sub2.getLeafExpression().calculate(ctx);
				if ( !(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				if (o1 instanceof Sequence) {
					Sequence seq = (Sequence) o1;
					int len = seq.length();
					int[] cols = new int[len];
					cns = new String[len];
					for (int i = 0; i < len; i++) {
						Object o = seq.get(i+1);
						if (o instanceof Number) {
							cols[i] = ((Number) o).intValue() - 1;
							cns[i] = r1.dataStruct().getFieldName(cols[i]);
						}
						else {
							cns[i] = o.toString();
							cols[i] = r1.dataStruct().getFieldIndex(cns[i]);
						}
					}
					seqs = Prep.getFields(srcSequence, cols);
				}
				else {
					// ��������ѡ����������ʱʹ��ȫ���ֶ�
					seqs = Prep.pseqToSeqs(srcSequence);
					cns = r1.getFieldNames();
				}
				dmr.init((Sequence) o2);
			}
			else {
				if (!param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if ( !(o1 instanceof Sequence) ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				dmr.init((Sequence) o1);
				o1 = srcSequence.get(1);
				if (o1 instanceof Sequence) {
					seqs = Prep.seqToSeqs(srcSequence);
				}
				else {
					seqs = new ArrayList<Sequence>();
					seqs.add(srcSequence);
				}
			}
			ArrayList<Sequence> ncv = new ArrayList<Sequence>();
			ArrayList<String> ncn = new ArrayList<String>();
			if (cns == null) {
				int len = seqs.size();
				cns = new String[len];
				for (int i = 1; i <= len; i++) {
					cns[i-1] = "Date"+i;
				}
			}
			dateInterval(dmr, seqs, cns, ncv, ncn);
			return Prep.toTab(ncn, ncv);
		}
		else {
			Sequence tvs = null;
			byte tType = -1;
			String[] cns = null;
			ArrayList<Sequence> seqs = null;
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
				// srcSequence Ҫ�������л����
				if (param == null || param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval" + mm.getMessage("function.invalidParam"));
				}
				Record r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param.getSubSize() < 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval" + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(0);
				Object o1 = sub1 == null ? null : sub1.getLeafExpression().calculate(ctx);
				Object o2 = sub2 == null ? null : sub2.getLeafExpression().calculate(ctx);
				if (!(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval" + mm.getMessage("function.paramTypeError"));
				}
				tvs = (Sequence) o2;
				if (o1 instanceof Sequence) {
					Sequence seq = (Sequence) o1;
					int len = seq.length();
					int[] cols = new int[len];
					cns = new String[len];
					for (int i = 0; i < len; i++) {
						Object o = seq.get(i+1);
						if (o instanceof Number) {
							cols[i] = ((Number) o).intValue() - 1;
							cns[i] = r1.dataStruct().getFieldName(cols[i]);
						}
						else {
							cns[i] = o.toString();
							cols[i] = r1.dataStruct().getFieldIndex(cns[i]);
						}
					}
					seqs = Prep.getFields(srcSequence, cols);
				}
				else {
					// ��������ѡ����������ʱʹ��ȫ���ֶ�
					seqs = Prep.pseqToSeqs(srcSequence);
					cns = r1.getFieldNames();
				}
			}
			else {
				if (!(param.isLeaf())) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval" + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if ( !(o1 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("dateinterval" + mm.getMessage("function.paramTypeError"));
				}
				tvs = (Sequence) o1;
				int len = tvs.length();
				cns = new String[len];
				for (int i = 1; i <= len; i++) {
					cns[i-1] = "Date"+i;
				}
				o1 = srcSequence.get(1);
				if (o1 instanceof Sequence) {
					seqs = Prep.seqToSeqs(srcSequence);
				}
				else {
					// ֻ��һ��
					seqs = new ArrayList<Sequence>();
					seqs.add(srcSequence);
				}
			}
			if (tType < 1) {
				tType = Prep.getType(tvs);
			}
			ArrayList<Sequence> ncv = new ArrayList<Sequence>();
			ArrayList<String> ncn = new ArrayList<String>();
			DiMvpRec dmr = dateInterval(seqs, cns, tvs, tType, ncv, ncn);
			Sequence result = new Sequence(2);
			Table tab = Prep.toTab(ncn, ncv);
			result.add(tab);
			result.add(dmr.toSeq());
			return result;
		}
	}
	
	
	private static double P_maxMI = 0.95;

	/**
	 * ����ֵĿ���������ƫ����
	 * @param tvs	��ֵĿ���������ֵ
	 * @param cn	������
	 * @return
	 */
	protected static DiMvpRec dateInterval(ArrayList<Sequence> dvs, String[] dns, Sequence tvs, byte tType,
			ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		DiMvpRec dmr = new DiMvpRec(null);
		//���̣�
		//	(d) �������е�date��������������date�Ĳ�ֵ����������Ϊ
		//		��distance_�ֶ���1_�ֶ���2����ֻ������ֵȫ��Ϊ����ȫ��Ϊ���������ֶΣ�	
		//		ȫ��Ϊ���������ֶ�ȡ����ֵ.
		int dcsize = dvs.size();
		Sequence dv1 = dvs.get(1);
		int length = dv1.length();
		//ArrayList<Integer> dateLocs = pr.getDateLocs();
		//Sequence tvs = pr.getTvs();
		//DMCTable srcTab = pr.getSrcTable();
		
		//int dcsize = dateLocs.size();
		//int length = tvs.length();
		if (dcsize < 2) {
			return null;
		}

		dv1 = null;
		Sequence dv2 = null;
		Sequence rel = null;
		for (int i = 0; i < dcsize; i++) {
			for (int j = 0; j < dcsize; j++) {
				if (i == j) {
					continue;
				}
				dv1 = dvs.get(i);
				dv2 = dvs.get(j);
				
				rel = new Sequence(length);
				boolean normal = true;
				for (int r = 1; r <= length; r++) {
					Object o1 = dv1.get(r);
					Object o2 = dv2.get(r);
					if ((o1 instanceof java.util.Date) && (o2 instanceof java.util.Date)) {
						int relday = (int) Variant.interval((java.util.Date) o1,
								(java.util.Date) o2, null);
						if (relday < 0) {
							//�ſ�һЩҪ�󣬱������ڲ�ֵȫ��Ϊ�Ǹ�����
							normal = false;
							dv1 = null;
							dv2 = null;
							rel = null;
							break;
						}
						rel.add(Integer.valueOf(relday));
					}
					else if (o1 == null || o2 == null) {
						rel.add(null);
					}
					else {
						normal = false;
						dv1 = null;
						dv2 = null;
						rel = null;
						break;
					}
				}
				if (normal) {
					//String[] cns = {dns[i], dns[j]};
					// cns�Ǵ�����Ҫ��ʹ�õģ�����������ȱ������Ĵ����¼��������
					String newCn = "distance_" + dns[i] + "_" + dns[j];
					VarDateInterval vdi = new VarDateInterval(newCn, Consts.F_NUMBER);
					vdi.init(rel);
					double freq = vdi.getMissingRate();
					// �����뽨ģ���ж��п��ܱ仯
					if (freq > P_maxMI) {
						//ȱʧ�ʴ���95%�����ֶβ����뽨ģ
						dv1 = null;
						dv2 = null;
						rel = null;
						continue;
					}
					vdi.setDateVar(dns[i], dns[j]);
					/*
					/* �����в�ֱ�Ӹ���������ȡ����
					VarSrcInfo vsi = pr.getVarSrcInfo(vns[i], false);
					vsi.addDateInterval(vdi);
					vsi = pr.getVarSrcInfo(vns[j], false);
					vsi.addDateInterval(vdi);
					*/
					VarRec vr = new VarRec(false, false, vdi);

					// �ȴ������ֶε�MissingIndicator
					// ����Ԥ��������ǰ���������ڲ�ֵ��ʱ��Ҫ����ȫ����ȫ��������ǲ������ȱʧ��
					//��ʵ���Ͽ��ǵ������ֵʱ����������������Ŀǰִ�е���ȫ�Ǹ���ȫ���Ļ����ֶε�תʱ��¼��
					//recMI(rel, newCn, pr, freq, vr);

					byte type = Prep.getType(rel);
					vr.setType(type);
					vdi.setType(type);
					if (type == Consts.F_SINGLE_VALUE) {
						// ���������ڲ��ֶ�Ϊ��ֵ���������
						vdi.setStatus(VarInfo.VAR_DEL_SINGLE);
						dv1 = null;
						dv2 = null;
						rel = null;
					} else if (type == Consts.F_TWO_VALUE) {
						// ���������ڲ��ֶ�Ϊ��ֵ
						//dmr.addIntervalRec(vr, dns[i], dns[j]);
						dmr.addIntervalRec(vr, String.valueOf(i), String.valueOf(j));
						//dealEnum(rel, newCn, pr, freq, vr, Consts.F_TWO_VALUE,
						//		ResultCol.CL_OTHERS, (byte) 0, cns, vdi);
						Prep.dealEnum(rel, newCn, freq, vr, Consts.F_TWO_VALUE, tvs, tType, null, ncv, ncn);
					} else if (type == Consts.F_ENUM) {
						// ���������ڲ��ֶ�Ϊö����
						dmr.addIntervalRec(vr, String.valueOf(i), String.valueOf(j));
						//dmr.addIntervalRec(vr, dns[i], dns[j]);
						//dealEnum(rel, newCn, pr, freq, vr, Consts.F_ENUM,
						//		ResultCol.CL_OTHERS, (byte) 0, cns, vdi);
						Prep.dealEnum(rel, newCn, freq, vr, Consts.F_ENUM, tvs, tType, null, ncv, ncn);
					} else if (type == Consts.F_COUNT || type == Consts.F_NUMBER) {
						// ʣ�µĿ��ܾ���Ϊ��������
						dmr.addIntervalRec(vr, String.valueOf(i), String.valueOf(j));
						//dmr.addIntervalRec(vr, dns[i], dns[j]);
						//vr = dealNumerical(rel, newCn, pr, freq, vr, Consts.F_NUMBER,
						//		ResultCol.CL_OTHERS, (byte) 0, cns, vdi);
						Prep.dealNumerical(rel, newCn, freq, vr, Consts.F_ENUM, null, ncv, ncn);
					}
					//if (rel != null) {
						//ncv.add(rel);
						//ncn.add(newCn);
					//}
				}
			}
		}
		return dmr;
	}
	
	protected static void dateInterval(DiMvpRec dmr, ArrayList<Sequence> dvs, String[] dns,
			ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		//���̣�
		//	(d) �������е�date��������������date�Ĳ�ֵ����������Ϊ
		//		��distance_�ֶ���1_�ֶ���2����ֻ������ֵȫ��Ϊ����ȫ��Ϊ���������ֶΣ�	
		//		ȫ��Ϊ���������ֶ�ȡ����ֵ.
		ArrayList<VarRec> vrs = dmr.getIntervalRecs();
		Sequence dv1 = dvs.get(1);
		int length = dv1.length();
		// ��Ӵ�����Ԥ����ݴ���
		int cols = vrs == null ? 0 : vrs.size();
		if (cols > 0) {
			ArrayList<String> interval1 = dmr.getInterval1();
			ArrayList<String> interval2 = dmr.getInterval2();
			// edited by bd, 2022.5.3, �����м�¼��interval1��interva2�����ֶ���ţ�1��ʼ��
			for (int c = 0; c < cols; c++) {
				String cn1 = interval1.get(c);
				String cn2 = interval2.get(c);
				int di1 = Integer.valueOf(cn1);
				int di2 = Integer.valueOf(cn2);
				dv1 = dvs.get(di1);
				Sequence dv2 = dvs.get(di2);
				Sequence rel = new Sequence(length);
				for (int r = 1; r <= length; r++) {
					Object o1 = dv1.get(r);
					Object o2 = dv2.get(r);
					if ((o1 instanceof java.util.Date) && (o2 instanceof java.util.Date)) {
						int relday = (int) Variant.interval((java.util.Date) o1,
								(java.util.Date) o2, null);
						rel.add(Integer.valueOf(relday));
					}
					else {
						rel.add(null);
					}
				}
				
				VarRec vr = vrs.get(c);
				if (!vr.onlyHasMI()) {
					//String newCn = "distance_" + cn1 + "_" + cn2;
					String newCn = "distance_" + dns[di1] + "_" + dns[di2];
					//reprep(vr, rel, newCn, vr.getType(), pr);
					Prep.prep(vr, rel, newCn, ncv, ncn);
				}
			}
		}
	}
}
