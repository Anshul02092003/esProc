package com.scudata.lib.math;

import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.lib.math.prec.Consts;
import com.scudata.lib.math.prec.FNARec;
import com.scudata.lib.math.prec.VarInfo;
import com.scudata.resources.EngineMessage;
import com.scudata.common.MessageManager;

/**
 * ����ƫ��
 * @author bd
 * A.impute(), P.impute(cn), A.impute@r(rec), P.impute@r(cn, rec) 
 */
public class Impute extends SequenceFunction {
	
	public Object calculate(Context ctx) {
		boolean cover = option != null && option.indexOf('c') > -1;
		boolean re = option != null && option.indexOf('r') > -1;
		String cn = "impute";
		Sequence seq = srcSequence;
		Record r1 = null;
		int col = 0;
		if (re) {
			FNARec fRec = new FNARec();
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param == null ) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				if (param.isLeaf() || param.getSubSize() < 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Object o1 = sub1.getLeafExpression().calculate(ctx);
				Object o2 = sub2 == null ? null : sub2.getLeafExpression().calculate(ctx);
				if (o1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				if (!(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				fRec.init((Sequence) o2);
				if (o1 instanceof Number) {
					col = ((Number) o1).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o1.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				seq = Prep.getFieldValues(srcSequence, col);
			}
			else {
				if (param != null && param.isLeaf()) {
					Object o1 = param.getLeafExpression().calculate(ctx);
					if (!(o1 instanceof Sequence)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("impute@" + option + " " + mm.getMessage("function.paramTypeError"));
					}
					fRec.init((Sequence) o1);
				}
				if (!cover) {
					seq = Prep.dup(seq);
				}
			}
			impute(seq, cn, fRec);
			if (cover) {
				if (r1 != null) {
					Prep.coverPSeq(srcSequence, seq, null, r1.dataStruct(), col);
				}
				//return result;
			}
			return seq;
		}
		else {
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
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
				if (param == null || !param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("impute" + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if (o1 instanceof Number) {
					col = ((Number) o1).intValue() - 1;
					cn = r1.dataStruct().getFieldName(col);
				}
				else {
					cn = o1.toString();
					col = r1.dataStruct().getFieldIndex(cn);
				}
				seq = Prep.getFieldValues(srcSequence, col);
			}
			if (r1 == null && !cover ) {
				seq = Prep.dup(seq);
			}
			if (type < 1) {
				type = Prep.getType(seq);
			}
			FNARec fRec = recFNA(seq, cn, type);
			Sequence result = fRec == null ? null : fRec.toSeq();
			if (cover) {
				if (r1 != null) {
					Prep.coverPSeq(srcSequence, seq, null, r1.dataStruct(), col);
				}
				//return result;
			}
			Sequence bak = new Sequence(2);
			bak.add(seq);
			bak.add(result);
			return bak;
		}
	}
	
	private static double P_m = 50d;

	/**
	 * ����ֵ��������ƫ����
	 * @param cvs	��ֵ��������ֵ
	 * @param cn	������
	 * @param filePath	���������Ҫ���������������ݽ϶�ʱ�����ļ���·������ʱ��֧��
	 * @return
	 */
	protected static FNARec recFNA(Sequence cvs, String cn, byte ctype) {
		if (cvs == null) {
			return null;
		}
		VarInfo vi = new VarInfo(cn, ctype);
		vi.init(cvs);
		return recFNA(cvs, cn, ctype, vi);
	}
	
	protected static FNARec recFNA(Sequence cvs, String cn, byte ctype, VarInfo vi) {
		if (cvs == null) {
			return null;
		}
		double pm = Impute.P_m;
		int size = cvs.length();
		double freq = vi.getMissingRate();

		FNARec fnaRec = new FNARec();
		int msize = 0;
		//����
		Object maxv = null;

		if (ctype == Consts.F_COUNT) {
			//�����ͣ����������ֵ�Ͳ�ȱ�����ö�ٵļ������Ͷ��ܼ򵥣�ֻ��Ҫ���գ�����Ҫ���Ǻϲ���Ƶ���������
			maxv = Prep.clnvCount(cvs);
			fnaRec.setMissing(maxv);
		}
		else if (ctype == Consts.F_NUMBER ) {
			//��ֵ�ͣ���ȱ�õĲ����������Ǿ�ֵ
			Prep.clnv(cvs, vi.getAverage());
			fnaRec.setMissing(vi.getAverage());
			vi.setFillMissing(vi.getAverage());
		}
		else if (ctype == Consts.F_ENUM || ctype == Consts.F_TWO_VALUE || ctype == Consts.F_SINGLE_VALUE ) {
			//��ֵ��ö�٣���������ִ�����˵ĵ�ֵ			
			if (size*1d/vi.getCategory() <= 50) {
				//�������̫�����ˣ�ƽ��ÿ������ֵ������50��������ɾ��
				vi.setStatus(VarInfo.VAR_DEL_CATEGORY);;
				return null;
			}
			
			//��ֵ��
			ArrayList<Integer> nA = new ArrayList<Integer>();
			//��Ƶ������
			ArrayList<Integer> A = new ArrayList<Integer>();

			ArrayList<ArrayList<Integer>> groups = Prep.group(cvs);
			//ö����
			int len = groups.size();
			
			int check = 6;
			if (freq >= Prep.MISSING_MAX || (freq <= Prep.MISSING_MIN && freq > 0)) {
				//��ֵ���ڣ����Ǳ��滻Ϊ����ʱ����ֵ��ռ�ж�����checkҪ+1
				check ++;
			}
			
			boolean merge = true;
			if (len <= check) {
				//������<=6����ʱ���ϲ���Ƶ����
				merge = false;
			}
			
			Object setting = Consts.CONST_OTHERNUMS;
			Object missing = Consts.CONST_NULLNUM;
			//�������ķ���ֵ
			Sequence keepValues = new Sequence();
			//���ϲ���ʧ�ĵ�Ƶ����ֵ
			Sequence otherValues = new Sequence();
			for (int i = 0; i < len; i++ ) {
				ArrayList<Integer> thisg = groups.get(i);
				size = thisg.size();
				if (size < 1) {
					continue;
				}
				Integer index = thisg.get(0);
				Object value = cvs.get(index.intValue());
				if (value == null) {
					//��ֵ�飬��¼����Ŀ�ֵ���
					for (int ri = 0; ri < size;ri ++ ) {
						index = thisg.get(ri);
						nA.add(index);
					}
				}
				else if (merge && size < pm) {
					//�豻�ϲ��ĵ�Ƶ������
					otherValues.add(value);
					for (int ri = 0; ri < size;ri ++ ) {
						index = thisg.get(ri);
						A.add(index);
					}
				}
				else {
					//�������ķ����飬�鿴�Ƿ�������
					keepValues.add(value);
					if (msize < size) {
						msize = size;
						maxv = value;
					}
				}
			}
			len = A.size();
			// edited by bd, 2022.5.13, ���������ж��������ͣ��������ֵ��ʹ���ַ������
			if (!(maxv instanceof Number)) {
				missing = Consts.CONST_NULL;
				setting = Consts.CONST_OTHERS;
			}
			// �����޸ģ�nA��Ƶ�Ȳ�����5%�򲻵���95%ʱ���������û�null
			//��Ҫ��֤������Ϊnull
			if ((freq >= Prep.MISSING_MAX || freq <= Prep.MISSING_MIN) && maxv != null) {
				for (Integer index : nA) {
					missing = maxv;
					cvs.set(index.intValue(), maxv);
				}
				nA.clear();
			}
			
			if (nA.size() < pm) {
				//��ֵΪ��Ƶ�飬���߲����ڣ���͵�Ƶ��ϲ�����
				if (len + nA.size() < pm && maxv != null) {
					//��ֵ�������������Ȼ�ǵ�Ƶ�飬�����������ʱ�豣֤�����Ǵ��ڵ�
					setting = maxv;
					fnaRec.setOtherValues(null);
				}
				else {
					// ��ֵ�͵�Ƶ�飬��ͬ�ϲ�Ϊ�µġ�����"��
					// ����ֵ���趨��������趨�Ļ�����Щֵ���ᱻ��Ϊ��������û��С��ֵ��nullֵ��������, �浽UP���sgnvʱ�ٵ���
					if (nA.size() > 0) {
						otherValues.add(null);
					}
					fnaRec.setOtherValues(otherValues);
				}
				for (Integer index : nA) {
					cvs.set(index.intValue(), setting);
				}
				for (Integer index : A) {
					cvs.set(index.intValue(), setting);
				}
				fnaRec.setSetting(setting);
				fnaRec.setMissing(setting);
				vi.setFillMissing(setting);
				vi.setFillOthers(setting);
				vi.setFillOthers(otherValues);
			}
			else {
				//��ֵ�鵥�����ã������Ƶ�ϲ�������
				if (len < pm && maxv != null) {
					//��Ƶ�ϲ�������δ��꣬����������
					setting = maxv;
					fnaRec.setOtherValues(null);
				}
				else {
					//  ����ֵ���趨��������趨�Ļ�����Щֵ���ᱻ��Ϊ��������û��С��ֵ��nullֵ��������, �浽UP���sgnvʱ�ٵ���
					fnaRec.setOtherValues(otherValues);
				}
				for (Integer index : nA) {
					cvs.set(index.intValue(), missing);
				}
				for (Integer index : A) {
					cvs.set(index.intValue(), setting);
				}
				fnaRec.setSetting(setting);
				fnaRec.setMissing(missing);
				vi.setFillMissing(missing);
				vi.setFillOthers(setting);
				vi.setFillOthers(otherValues);
			}
			fnaRec.setKeepValues(keepValues);
			// �����޸ģ����ֵ��Ϊ��ֵ�ģ�����null
			if (keepValues.length() < 2 && Prep.card(cvs) == 1) {
				vi.setStatus(VarInfo.VAR_DEL_SINGLE);
				//return null;
				// ��������ȥֱ�ӷ��ؿ�ֵ��
			}
		}
		return fnaRec;
	}
	
	protected static void impute(Sequence cvs, String cn, FNARec fr) {
		Object missing = fr.getMissing();
		Object setting = fr.getSetting();
		if (setting == null) {
			//�����ͣ����������ֵ�Ͳ�ȱ�����ö�ٵļ������Ͷ��ܼ򵥣�ֻ��Ҫ���գ�����Ҫ���Ǻϲ���Ƶ���������
			if (missing == null) {
				//δִ�й���ȱ��ֱ�ӷ���
				return;
			}
			Prep.clnv(cvs, missing);
		}
		else {
			Sequence keepValues = fr.getKeepValues();
			
			if (setting instanceof Sequence) {
				missing = ((Sequence) setting).get(2);
				setting = ((Sequence) setting).get(1);
			}
			
			if (missing == null && setting == null) {
				//����������null��˵����δִ�й���ȱ��ϲ���ֱ�ӷ��ؼ���
				return;
			}
			// Ϊ�˷�ֹ�����ֵ���������setting��missing����Ϊ�ǿ�
			else if (missing == null) {
				missing = setting;
			}
			else if (setting == null) {
				setting = missing;
			}
			
			int iSize = cvs == null ? 0 : cvs.length();
			for ( int i = 1; i <= iSize; i++ ) {
				Object v = cvs.get(i);
				if (missing != null && v == null) {
					cvs.set(i, missing);
				}
				else if (keepValues == null || keepValues.pos(v, null) == null) {
					cvs.set(i, setting);
				}
			}
		}
	}
}
