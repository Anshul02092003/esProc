package com.scudata.lib.math;

import java.util.ArrayList;

import com.scudata.common.Logger;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.array.IArray;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.lib.math.prec.BIRec;
import com.scudata.lib.math.prec.Consts;
import com.scudata.lib.math.prec.DateRec;
import com.scudata.lib.math.prec.FNARec;
import com.scudata.lib.math.prec.NorRec;
import com.scudata.lib.math.prec.NumStatis;
import com.scudata.lib.math.prec.SCRec;
import com.scudata.lib.math.prec.SmMulRec;
import com.scudata.lib.math.prec.SmRec;
import com.scudata.lib.math.prec.VarInfo;
import com.scudata.lib.math.prec.VarRec;
import com.scudata.lib.math.prec.VarSrcInfo;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;
import com.scudata.common.MessageManager;

/**
 * ĳһ�е�Ԥ����ȫ����
 * A.prep(T)/P.prep(cn, T); @bnie ѡ��ָ��Ŀ�����ͣ���ѡ����⣬���ȼ����ն�ֵ/��ֵ/����/ö�٣���ѡ���Զ�����
 * 							@BNIED ѡ��ָ���������ͣ���ѡ����⣬���ȼ����ն�ֵ/��ֵ/����/ö��/���ڣ���ѡ���Զ�����
 * A.prep@r(rec)/P.prep@r(cn, rec)
 * @author bd
 */
public class Prep extends SequenceFunction {
	
	public Object calculate(Context ctx) {
		boolean re = option != null && option.indexOf('r') > -1;
		boolean ori = option != null && option.indexOf('o') > -1;
		String cn = "prep";
		if (re) {
			if (param == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("prep@" + option + " " + mm.getMessage("function.invalidParam"));
			}
			Sequence seq = srcSequence;
			VarRec vr = new VarRec();
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				BaseRecord r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					BaseRecord r = (BaseRecord) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param == null ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				if (param.isLeaf() || param.getSubSize() < 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null || sub2 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Object o1 = sub1.getLeafExpression().calculate(ctx);
				Object o2 = sub2.getLeafExpression().calculate(ctx);
				if (o1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				if ( !(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				int col = 0;
				if (o1 instanceof Number) {
					col = ((Number) o1).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o1.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				seq = Prep.getFieldValues(srcSequence, col);
				vr.init((Sequence) o2);
			}
			else {
				if (!param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if ( !(o1 instanceof Sequence) ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				vr.init((Sequence) o1);
				seq = Prep.dup(seq);
			}
			ArrayList<Sequence> ncv = new ArrayList<Sequence>();
			ArrayList<String> ncn =  new ArrayList<String>();
			Prep.prep(seq, cn, vr, ncv, ncn);
			return Prep.toTab(ncn, ncv);
		}
		else {
			Sequence seq = srcSequence;
			Sequence tvs = null;
			byte type = 0;
			if (option != null) {
				if (option.indexOf('B') > -1) {
					type = Consts.F_TWO_VALUE;
				}
				else if (option.indexOf('N') > -1) {
					type = Consts.F_NUMBER;
				}
				else if (option.indexOf('I') > -1) {
					type = Consts.F_COUNT;
				}
				else if (option.indexOf('E') > -1) {
					type = Consts.F_ENUM;
				}
				else if (option.indexOf('D') > -1) {
					type = Consts.F_DATE;
				}
			}
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
				BaseRecord r1 = null;
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					BaseRecord r = (BaseRecord) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param == null ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep" + mm.getMessage("function.invalidParam"));
				}
				Object o1 = null;
				if (param.isLeaf()) {
					o1 = param.getLeafExpression().calculate(ctx);
				}
				int plen = param.getSubSize();
				if (plen > 0) {
					IParam sub1 = param.getSub(0);
					if (sub1 == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("prep" + mm.getMessage("function.invalidParam"));
					}
					o1 = sub1.getLeafExpression().calculate(ctx);
				}
				if (o1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep" + mm.getMessage("function.paramTypeError"));
				}
				int col = 0;
				if (o1 instanceof Number) {
					col = ((Number) o1).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o1.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				if (plen > 1) {
					IParam sub = param.getSub(1);
					Object o2 = sub == null ? null : sub.getLeafExpression().calculate(ctx);
					if ( !(o2 instanceof Sequence)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("prep" + mm.getMessage("function.paramTypeError"));
					}
					tvs = (Sequence) o2;
				}
				seq = Prep.getFieldValues(srcSequence, col);
			}
			else {
				if (param == null || !param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("prep" + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if ( o1 instanceof Sequence) {
					tvs = (Sequence) o1;
				}
				seq = Prep.dup(seq);
			}
			if (type < 1) {
				type = getType(seq);
			}
			if (tType < 1) {
				tType = getType(tvs);
			}
			ArrayList<Sequence> ncv = new ArrayList<Sequence>();
			ArrayList<String> ncn =  new ArrayList<String>();
			VarRec vr = prep(seq, cn, type, !ori, tvs, type, ncv, ncn);
			Sequence result = vr.toSeq();
			Table tab = Prep.toTab(ncn, ncv);
			Sequence bak = new Sequence(2);
			bak.add(tab);
			bak.add(result);
			return result;
		}
	}
	
	protected static double P_maxMI = 0.95;
	
	protected static VarRec prep(Sequence cvs, String cn, byte type, boolean ifErf, Sequence tvs, 
			byte tarType, ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		double maxMI = P_maxMI;
		// ��ÿһ�ж����ռ�����ͳ����Ϣ
		VarSrcInfo vsi = new VarSrcInfo(cn, type);
		vsi.init(cvs);
		double freq = vsi.getMissingRate();

		try {
			// ����Ԥ������򣬲�����Ӳ�Ե�95%��Ϊ��׼��
			if (freq > maxMI) {
				// ȱʧ�ʴ�����ֵ(���ٶ���95%)�����ֶβ����뽨ģ
				vsi.setStatus(VarInfo.VAR_DEL_MISSING);
				return new VarRec(false, true, vsi);
			}

			if (type == Consts.F_SINGLE_VALUE) {
				// ��ֵ�ֶΣ�ֻ��ȱʧֵ����MI
				// ��ֵ�ֶ�����missing�󣬲����м�ֵ��ȫ��ɾ��
				return recMI(cvs, cn, freq, null, vsi, ncv, ncn);
			} else if (type == Consts.F_CONTINUITY) {
				// ID�ֶΣ�������
				vsi.setStatus(VarInfo.VAR_DEL_ID);
				return new VarRec(false, true, vsi);
			} else if (type == Consts.F_DATE) {
				// ������������
				DateRec dr = new DateRec(vsi);

				// �ȴ������ֶε�MissingIndicator
				recMI(cvs, cn, freq, dr, vsi, ncv, ncn);
				
				// �����е������ֶΣ����߳���������
				DateDerive.datederive(cvs, cn, dr, tvs, tarType, ncv, ncn);
				//dealDateCols(cvs, cn, dr, pr, vsi);
				//byte dtype = dr.getDateType();
				//if (dtype == Consts.DCT_DATETIME || dtype == Consts.DCT_UDATE
				//		|| dtype == Consts.DCT_DATE) {
					// �漰���ڵģ��п��ܻ���Ҫ����������ڲ�
				//	pr.addDate(Integer.valueOf(ci));
				//}
				// ������Ԥ�����У��������ڲ�ֵ����
				return dr;
			}
			// ��ֵ��ȡ��ö��һ���Ĵ����� 
			else if (type == Consts.F_ENUM || type == Consts.F_TWO_VALUE) {
				VarRec vr = new VarRec(false, false, vsi);
				vr.setType(type);
				// �ȴ���MissingIndicator
				recMI(cvs, cn, freq, vr, vsi, ncv, ncn);
				
				freq = vsi.getMissingRate();
				dealEnum(cvs, cn, freq, vr, type, tvs, tarType, vsi, ncv, ncn);
				return vr;
			} else if (type == Consts.F_NUMBER || type == Consts.F_COUNT) {
				// ��������ֵ��
				VarRec vr = new VarRec(false, false, vsi);
				vr.setType(type);
				// �ȴ���MissingIndicator
				//recMI(cvs, cn, pr, freq, vr, vsi);
				recMI(cvs, cn, freq, vr, vsi, ncv, ncn);
				dealNumerical(cvs, cn, freq, vr, type, vsi, ncv, ncn);
				
				return vr;
			} else {
				vsi.setStatus(VarInfo.VAR_DEL_WRONGTYPE);
				// �������������ͣ�ɾ��
				return new VarRec(false, true, vsi);
			}
		}
		catch (Exception e) {
			MessageManager mm = EngineMessage.get();
			Logger.error(mm.getMessage("prep.varError", cn));
			Logger.error(e.getMessage());
		}
		return null;
	}

	
	protected static Sequence dup(Sequence srcSequence) {
		IArray mems = srcSequence.getMems();
		int len = srcSequence.length();
		Sequence result = new Sequence(len);
		for (int i = 1; i <= len; i++) {
			result.add(mems.get(i));
		}
		return result;
	}
	
	/**
	 * �������л�ȡĳ������
	 * @param src
	 * @param field
	 * @return
	 */
	protected static Sequence getFieldValues(Sequence src, int field) {
		IArray mems = src.getMems();
		int size = mems.size();
		Sequence result = new Sequence(size);

		for (int i = 1; i <= size; ++i) {
			BaseRecord cur = (BaseRecord)mems.get(i);
			if (cur == null) {
				result.add(null);
			} else {
				result.add(cur.getFieldValue2(field));
			}
		}

		return result;
	}
	
	/**
	 * �������л�ȡ���������ֵ
	 * @param src
	 * @param fields
	 * @return
	 */
	protected static ArrayList<Sequence> getFields(Sequence src, int[] fields) {
		IArray mems = src.getMems();
		int size = mems.size();
		int cols = fields.length;
		ArrayList<Sequence> result = new ArrayList<Sequence>(cols);
		for (int i = 0; i < cols; i++) {
			Sequence seq = new Sequence(size);
			result.add(seq);
		}

		for (int i = 1; i <= size; ++i) {
			BaseRecord cur = (BaseRecord)mems.get(i);
			for (int col = 0; col < cols; col++) {
				if (cur == null) {
					result.get(col).add(null);
				} else {
					result.get(col).add(cur.getFieldValue2(fields[col]));
				}
			}
		}

		return result;
	}
	
	protected static ArrayList<Integer> filter(String[] cns, String key, boolean ifPrefix) {
		ArrayList<Integer> locs = new ArrayList<Integer>();
		int len = cns == null ? 0 : cns.length;
		for (int i = 0; i < len; i++) {
			String cn = cns[i];
			boolean find = false;
			if (ifPrefix) {
				if (cn != null && cn.startsWith(key)) {
					find = true;
				}
			}
			else if(cn != null && cn.endsWith(key)) {
				find = true;
			}
			if (find) {
				locs.add(i);
			}
		}
		return locs;
	}
	
	protected static byte getType(Sequence vs) {
		if (vs == null) return Consts.F_CONTINUITY;
		Sequence X = vs.id("");
		int len = X.length();
		boolean hasStr = false;
		boolean hasNull = false;
		boolean hasDecimal = false;
		for (int i = 1; i <= len; i++) {
			Object obj = X.get(i);
			if (obj == null) {
				hasNull = true;
			} else if (obj instanceof String) {
				hasStr = true;
			} else if (!hasDecimal && obj instanceof Number) {
				if (obj != null && !(obj instanceof Integer) && !(obj instanceof Long)) {
					hasDecimal = true;
				}
			}
		}
		if (hasNull) {
			// ��ֵ������
			len--;
		}
		if (len <= 1) {
			// ��ֵ���ر�ģ�ȫ��nullҲ��Ϊ��ֵ
			return Consts.F_SINGLE_VALUE;
		} else if (len == 2) {
			// ��ֵ����
			return Consts.F_TWO_VALUE;
		} else if (hasStr || len < 20) {
			return Consts.F_ENUM;
		} else if (!hasDecimal) {
			return Consts.F_COUNT;
		} else {
			return Consts.F_NUMBER;
		}
	}
	
	protected static VarRec recMI(Sequence cvs, String cn, double freq, VarRec vr, VarSrcInfo vsi,
			ArrayList<Sequence> ncv, ArrayList<String> ncn) throws Exception {
		Sequence mivs = Mi.mi(cvs, freq);
		if (mivs != null) {
			String micn = "MI_" + cn;
			// step06���ж��Ƿ��ظ�
			// boolean add = pr.addMICol(micn, mivs, this.ci, cn);
			// �������ж��Ƿ��ظ�
			ncv.add(mivs);
			ncn.add(micn);
			//if (add) {
				vsi.setMI(true);
				if (vr == null) {
					// ԭʼ������ֻ����MI�� 
					vsi.setStatus(VarInfo.VAR_DEL_SINGLE);
					return new VarRec(true, true, vsi);
				}
				else {
					vr.setMI(true);
				}
			//}
		}
		vsi.setMI(false);
		if (vr == null) {
			vsi.setStatus(VarInfo.VAR_DEL_SINGLE);
			return new VarRec(false, true, vsi);
		}
		return vr;
	}
	
	protected static void dealEnum(Sequence cvs, String cn, double freq, VarRec vr,
			byte ctype, Sequence tvs, byte tType, VarInfo vsi, ArrayList<Sequence> ncv,
			ArrayList<String> ncn) {
		boolean ifErf = true;
		if (vr == null) {
			throw new RQException("Can't find Variable Record.");
		}
		
		// ��һ�����в��ٴ���MI�ֶε����ɣ���ǰ����recMI
		// step07, �ȱʧֵ
		// step09, �ϲ���Ƶ����
		FNARec fnaRec = Impute.recFNA(cvs, cn, ctype);
		// fnaRec�п��ܷ���null��˵������Ϊ��ֵ�л����ǹ��������ö���У���ʱ���б�ɾ��
		if (fnaRec == null) {
			vr.setOnlyMI(true);
			return;
		}
		vr.setFNARec(fnaRec);

		// step10, ����BI����
		BIRec biRec = Bi.recBi(cvs, cn, ncn, ncv);
		vr.setBIRec(biRec);

		if (biRec == null) {
			// �߻����������
			// step11, ƽ����
			SmRec smRec = Smooth.smooth(cvs, cn, ifErf, tvs, tType, ncv, ncn);
			//recprep(cvs, pr, vsi, level, index, srcCn, cn);
			vr.setSmRec(smRec);
			// �޸��ⲿ�ֵ��жϣ��п����Ѿ������˶��������
			if (smRec instanceof SmMulRec) {
				// ��������Ƕ�����Ŀ�꣬ƽ����ʱ����Ӷ�������в���¼
			}
			else {
				cn = "prep_" + cn;
				// ������Ϣ������������������¼��ƫ������Ϣ
				if (vsi != null) 
					vsi.setName(cn);
				// step12, ��ƫ�任
				SCRec scRec = CorSkew.corSkew(cvs, cn); 
				//recSC(cvs, cn, pr.getPath(), vsi);
				cn = scRec.getPrefix() + cn;
				vr.setSCRec(scRec);
				// step13, �����쳣ֵ
				NumStatis ns = scRec.getNumStatis();
				//Sert.sert(cvs, cvs, ns.getAvg(), ns.getSd(cvs), vsi);
				Sert.sertSeq(cvs, ns.getAvg(), ns.getSd(cvs));

				// step15��������������ֵ����������һ������
				NorRec nr = Normal.normal(cvs);//recNor(cvs);
				vr.setNorRec(nr);

				//if (level == ResultCol.CL_DATE ) {
					//ʱ���ֶΣ���Ҫ������
					//pr.addCol(cn, cvs, level, this.ci, index, srcCn);
				//}
				//else {
					//pr.addCol(cn, cvs, level, this.ci, srcCn);
				//}
				ncn.add(cn);
				ncv.add(cvs);
				if (vsi != null) 
					vsi.setIfSmooth(true);
				//pr.addNum(cn);
			}
		}
	}
	
	protected static VarRec dealNumerical(Sequence cvs, String cn, double freq,
			VarRec vr, byte ctype, VarInfo vi, ArrayList<Sequence> ncv,
			ArrayList<String> ncn) {
		if (vr == null) {
			vr = new VarRec(false, false, vi);
		}

		// step07, �ȱʧֵ
		//FNARec fnaRec = recFNA(cvs,freq, ctype, pr, vi);
		FNARec fnaRec = Impute.recFNA(cvs, cn, ctype);
		vr.setFNARec(fnaRec);
		
		// step12, ��ƫ�任
		SCRec scRec = CorSkew.corSkew(cvs, cn); 
		//SCRec scRec = recSC(cvs, cn, pr.getPath(), vi);
		cn = scRec.getPrefix() + cn;
		if(vi != null)
			vi.setName(cn);
		vr.setSCRec(scRec);
		// step13, �����쳣ֵ
		NumStatis ns = scRec.getNumStatis();
		if (ns == null) {
			System.out.println(cn);
		}
		//Sert.sert(cvs, cvs, ns.getAvg(), ns.getSd(cvs), vi);
		Sert.sertSeq(cvs, ns.getAvg(), ns.getSd(cvs));

		// step15��������������ֵ����������һ������
		NorRec nr = Normal.normal(cvs);//NorRec nr = recNor(cvs);
		vr.setNorRec(nr);

		//if (level == ResultCol.CL_DATE ) {
			//ʱ���ֶΣ���Ҫ������
		//	pr.addCol(cn, cvs, level, this.ci, index, srcCn);
		//}
		//else {
		//	pr.addCol(cn, cvs, level, this.ci, srcCn);
		//}
		//pr.addNum(cn);
		ncn.add(cn);
		ncv.add(cvs);
		return vr;
	}
	
	protected static Sequence toSeq(ArrayList<Sequence> seqs) {
		int size = seqs.size();
		Sequence seq = new Sequence(size);
		for (int i = 1; i <= size; i++) {
			seq.add(seqs.get(i));
		}
		return seq;
	}
	
	protected static Table toTab(ArrayList<String> cns, ArrayList<Sequence> seqs) {
		if (seqs == null || cns == null) {
			return null;
		}
		int size = cns.size();
		if (size < 1 || seqs.size() < size) {
			return null;
		}
		if (seqs.size() < size) {
			size = seqs.size();
		}
		
		String[] cols = new String[size];
		for (int i = 0; i < size; i++) {
			cols[i] = cns.get(i); 
		}
		
		DataStruct ds = new DataStruct(cols);
		int length = seqs.get(0).length();
		Table tab = new Table(ds);
		for (int i = 1; i <= length; i++) {
			BaseRecord rec = tab.newLast();
			for (int j = 0; j < size; j++) {
				rec.set(j, seqs.get(j).get(i));
			}
		}
		return tab;
	}
	
	protected static ArrayList<Sequence> pseqToSeqs(Sequence pseq) {
		BaseRecord r = (BaseRecord) pseq.get(1);
		DataStruct ds = r.dataStruct();
		IArray mems = pseq.getMems();
		int size = mems.size();
		int cols = ds.getFieldCount();
		ArrayList<Sequence> result = new ArrayList<Sequence>(cols);
		for (int i = 0; i < cols; i++) {
			Sequence seq = new Sequence(size);
			result.add(seq);
		}

		for (int i = 1; i <= size; ++i) {
			BaseRecord cur = (BaseRecord)mems.get(i);
			for (int col = 0; col < cols; col++) {
				if (cur == null) {
					result.get(col).add(null);
				} else {
					result.get(col).add(cur.getFieldValue2(col));
				}
			}
		}

		return result;
	}
	
	protected static ArrayList<Sequence> seqToSeqs(Sequence seq) {
		int len = seq.length();
		ArrayList<Sequence> seqs = new ArrayList<Sequence>(len);
		for (int i = 1; i <= len; i++) {
			Object o = seq.get(i);
			seqs.add((Sequence) o);
		}
		return seqs;
	}
	
	protected static void prep(Sequence cvs, String cn, VarRec vr, 
			ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		if (vr == null) {
			// ����ѡ������б�ɾ��
			return;
		}
		Sequence newcvs = null;
		// �Ƿ���ȱʧֵ��MissingIndex
		if (vr.hasMI()) {
			newcvs = Mi.ifNull(cvs);
			ncn.add("MI_"+cn);
			ncv.add(newcvs);
			//pr.addCol2("MI_"+cn, newcvs);
			//DMUtils.debug("**MI Variable created: "+"MI_"+cn+".");
		}
		//���ڳ���MI������������ֶΣ��Ѵ������
		if (vr.onlyHasMI()) {
			return;
		}
		
		if (vr instanceof DateRec) {
			DateDerive.datederive((DateRec) vr, cvs, cn, ncv, ncn);
		}
		else {
			prep(vr, cvs, cn, ncv, ncn);
		}
	}
	
	protected static void prep(VarRec vr, Sequence cvs, String cn,
			ArrayList<Sequence> ncv, ArrayList<String> ncn) {
		// ��һ�����в��ٴ���MI�ֶε����ɣ���ǰ����recMI
		// step07, �ȱʧֵ
		// step09, �ϲ���Ƶ����
		FNARec fnaRec = vr.getFNARec();
		//  ���������fnaRec��Ӧ��Ϊ�գ���һ���ֿ�ֵ��˵����ģʱʹ�õİ汾�Ƚϵ�
		// ��ôԤ��ʱ��ִ�в�ȱ���Է�ֹ���󣬵��п��ܻ����ڿ�ֵ�������⡣
		if (fnaRec != null) {
			Impute.impute(cvs, cn, fnaRec);
		}

		// step10, ����BI����
		BIRec biRec = vr.getBIRec();

		if (biRec != null) {
			// ������BI����
			Bi.bi(cvs, cn, biRec, ncv, ncn);
		}
		else {
			// ������ֵ��������ִ��[ƽ����],��ƫ,ȥ���쳣ֵ,��׼���ȴ���
			SmRec smRec = vr.getSmRec();
			if (smRec != null) {
				// �߻����������
				//  ƽ�����������������Ŀ��Ϊ��ֵ���ֵ
				Smooth.smooth(cvs, cn, smRec, ncv, ncn);
			}

			// step12, ��ƫ�任
			SCRec scRec = vr.getSCRec();
			if (scRec == null) {
				// ���δִ�о�ƫ��˵���Ƕ�ֵ������ֱ���������ֵ���ؾ�����
				ncn.add(cn);
				ncv.add(cvs);
				return;
			}
			CorSkew.corSkew(cvs, cn, scRec);
			NumStatis ns = scRec.getNumStatis();
			cn = scRec.getPrefix() + cn;
			
			// step13, �����쳣ֵ
			Sert.sertSeq(cvs, ns.getAvg(), ns.getSd(cvs));

			// step15��������������ֵ����������һ������
			NorRec nr = vr.getNorRec();
			Normal.normal(cvs, nr);
			ncn.add(cn);
			ncv.add(cvs);
		}
	}
	
	protected static void coverPSeq(Sequence pseq, Sequence seq, String ncn, DataStruct ds, int col) {
		if (ncn != null) {
			String[] cns = ds.getFieldNames();
			cns[col] = ncn;
		}
		for (int i = 1, n = seq.length(); i <= n; i++ ) {
			Object o = seq.get(i);
			Object r = pseq.get(i);
			if (r instanceof BaseRecord) {
				((BaseRecord) r).set(col, o);
			}
		}
	}
	
	protected static void coverSeq(Sequence src, Sequence seq) {
		src.setMems(seq.getMems());
	}
	
	protected static int card(Sequence seq) {
		return seq.id("").length();
	}

	protected static void clnv(Sequence Vs, Object def) {
		int n = Vs.length();
		for (int i = 1; i <= n; i++ ) {
			if (Vs.get(i) == null) {
				Vs.set(i, def);
			}
		}
	}

	/**
	 * �Լ����ͱ�����ȱ��ֻ��������
	 * @param Vs
	 * @param avg
	 */
	protected static Object clnvCount(Sequence Vs) {
		int size = Vs.length();
		
		int msize = 0;
		Object maxv = null;

		ArrayList<ArrayList<Integer>> groups = Prep.group(Vs);
		int len = groups == null ? 0 : groups.size();
		for (int i = 0; i < len; i++ ) {
			ArrayList<Integer> thisg = groups.get(i);
			size = thisg.size();
			if (size < 1) {
				continue;
			}
			Integer index = thisg.get(0);
			Object value = Vs.get(index.intValue());
			if (value == null) {
			}
			else if (msize < size) {
				msize = size;
				maxv = value;
			}
		}
		clnv(Vs, maxv);
		return maxv;
	}
	
	protected static double MISSING_MAX = 0.95;
	protected static double MISSING_MIN = 0.05;

	//�������а�ֵ�����У�ÿ���Ա�����List
	protected static ArrayList<ArrayList<Integer>> group(Sequence values) {
		int size = values.length();
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>(size / 4);
		if (size < 1) {
			return result;
		}
		Sequence psorts = values.psort(null);
		Object prev = values.get(((Integer) psorts.get(1)).intValue());

		ArrayList<Integer> group = new ArrayList<Integer>(7);
		group.add((Integer) psorts.get(1));
		result.add(group);

		for (int i = 2; i <= size; ++i) {
			Integer pos = (Integer) psorts.get(i);
			Object cur = values.get(pos.intValue());
			if (Variant.isEquals(prev, cur)) {
				group.add(pos);
			} else {
				// ����
				prev = cur;
				group.trimToSize();
				group = new ArrayList<Integer>(7);
				group.add(pos);
				result.add(group);
			}
		}
		result.trimToSize();
		return result;
	}
	
	protected static Sequence calcX1B(Sequence N1, double r1, Sequence N0,
			double r0, double m) {
		int xlen = N1 == null ? 0 : N1.length();
		Sequence res = new Sequence(xlen);
		for (int x = 1; x<=xlen; x++) {
			double ln = Math.log((((Number) N1.get(x)).doubleValue()
					+ m * r1)/(((Number) N0.get(x)).doubleValue() + m * r0));
			res.add(new Double(ln));
		}
		return res;
	}
	
	protected static void setSequenceB(Sequence Vs, Sequence X, Sequence X1) {
		int dlen = Vs.length();
		for (int d = 1; d<= dlen; d ++ ) {
			Object V = Vs.get(d);
			Object px = X.pos(V, null);
			if (px instanceof Number) {
				int p = ((Number) px).intValue();
				Vs.set(d, X1.get(p));
			}
			else {
				Vs.set(d, X1.get(X1.length()));
			}
		}
	}
	
	protected static Sequence createSequenceB(Sequence Vs, Sequence X, Sequence X1) {
		int dlen = Vs.length();
		Sequence result = new Sequence(dlen);
		Object def = X1.get(X1.length());
		for (int d = 1; d<= dlen; d ++ ) {
			Object V = Vs.get(d);
			Object px = X.pos(V, null);
			if (px instanceof Number) {
				int p = ((Number) px).intValue();
				result.add(X1.get(p));
			}
			else {
				result.add(def);
			}
		}
		return result;
	}

	protected static Sequence calcN3(Sequence Vs, Sequence X) {
		int xlen = X == null ? 0 : X.length();
		int len = Vs == null ? 0 : Vs.length();
		Sequence res = new Sequence(xlen);
		for (int x = 1; x<=xlen; x++) {
			int n = 0;
			Object v = X.get(x);
			for (int d = 1; d<= len; d++) {
				if (Variant.isEquals(Vs.get(d), v) ) {
					n ++;
				}
			}
			res.add(new Integer(n));
		}
		return res;
	}
	
	protected static Sequence calcY(Sequence Vs, Sequence Ts, Sequence X) {
		int xlen = X == null ? 0 : X.length();
		int dlen = Vs == null ? 0 : Vs.length();
		Sequence res = new Sequence(xlen);
		for (int x = 1; x<=xlen; x++) {
			int n = 0;
			double sum = 0d;
			Object v = X.get(x);
			for (int d = 1; d<= dlen; d++) {
				if (Variant.isEquals(Vs.get(d), v) ) {
					n ++;
					sum += ((Number) Ts.get(d)).doubleValue();
				}
			}
			res.add(new Double(sum/n));
		}
		return res;
	}
	
	//�޸ĺ��ƽ��������������Ȼ����
	protected static Sequence calcX1N2(Sequence X, Sequence N, Sequence Y, double y, double m) {
		int xlen = X == null ? 0 : X.length();
		Sequence res = new Sequence(xlen);
		for (int x = 1; x<=xlen; x++) {
			double n = ((Number) N.get(x)).doubleValue();
			double y1 = ((Number) Y.get(x)).doubleValue();
			double smooth = (y + n/m * y1)/(1+ n/m);
			res.add(new Double(smooth));
		}
		return res;
	}
}
