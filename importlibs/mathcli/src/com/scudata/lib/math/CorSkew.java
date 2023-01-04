package com.scudata.lib.math;

import org.apache.commons.math3.special.Erf;

import com.scudata.common.Logger;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.lib.math.prec.NumStatis;
import com.scudata.lib.math.prec.SCRec;
import com.scudata.resources.EngineMessage;
import com.scudata.common.MessageManager;

/**
 * ����ƫ��
 * @author bd
 * ԭ��A.corskew()��n=A.len()��a=A.avg()��s= A.sd()��A.sum((~-a)3)/s3/(n-1)
 * A.corcorskew(), P.corcorskew(cn), A.corcorskew@r(rec), P.corcorskew@r(cn, rec) 
 */
public class CorSkew extends SequenceFunction {
	
	public Object calculate(Context ctx) {
		boolean cover = option != null && option.indexOf('c') > -1;
		boolean re = option != null && option.indexOf('r') > -1;
		String cn = "corskew";
		if (re) {
			if (param == null ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("corskew@" + option + " " + mm.getMessage("function.invalidParam"));
			}
			Sequence seq = srcSequence;
			Object o2 = null;
			int col = 0;
			Record r1 = null;
			if (srcSequence instanceof Table || srcSequence.isPmt()) {
				if (param.isLeaf() || param.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				IParam sub1 = param.getSub(0);
				IParam sub2 = param.getSub(1);
				if (sub1 == null || sub2 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				Object o1 = sub1.getLeafExpression().calculate(ctx);
				o2 = sub2.getLeafExpression().calculate(ctx);
				if (o2 == null || !(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				for (int i = 1, size = srcSequence.length(); i < size; i++ ) {
					Record r = (Record) srcSequence.get(i);
					if (r != null) {
						r1 = r;
						break;
					}
				}
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
				if (!param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew@" + option + " " + mm.getMessage("function.invalidParam"));
				}
				o2 = param.getLeafExpression().calculate(ctx);
				if (!(o2 instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew@" + option + " " + mm.getMessage("function.paramTypeError"));
				}
				if (!cover) {
					seq = Prep.dup(seq);
				}
			}
			SCRec scRec = new SCRec();
			scRec.init((Sequence) o2);
			CorSkew.corSkew(seq, cn, scRec);
			cn = scRec.getPrefix() + cn;
			
			if (cover && r1 != null) {
				cn = scRec.getPrefix() + cn;
				Prep.coverPSeq(srcSequence, seq, cn, r1.dataStruct(), col);
			}
			return seq;
		}
		else {
			Sequence seq = srcSequence;
			Record r1 = null;
			int col = 0;
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
					throw new RQException("corskew" + mm.getMessage("function.invalidParam"));
				}
				if (!param.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew" + mm.getMessage("function.invalidParam"));
				}
				Object o1 = param.getLeafExpression().calculate(ctx);
				if (o1 == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("corskew" + mm.getMessage("function.paramTypeError"));
				}
				else if (o1 instanceof Number) {
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
			SCRec scRec = corSkew(seq, cn);
			Sequence result = scRec.toSeq();
			if (cover) {
				if (r1 != null) {
					cn = scRec.getPrefix() + cn;
					Prep.coverPSeq(srcSequence, seq, cn, r1.dataStruct(), col);
				}
				//return result;
			}
			Sequence bak = new Sequence(2);
			bak.add(seq);
			bak.add(result);
			return bak;
		}
	}
	
	protected final static double SC_MAX = 0.15;
	protected final static double SC_MIN = -0.15;
	protected final static double SC_MAX0 = 0.000001;
	protected final static double SC_MIN0 = -0.000001;
	protected final static int SC_MAXTRY = 10000;

	/**
	 * ����ֵ��������ƫ����
	 * @param cvs	��ֵ��������ֵ
	 * @param cn	������
	 * @param filePath	���������Ҫ���������������ݽ϶�ʱ�����ļ���·������ʱ��֧��
	 * @return
	 */
	protected static SCRec corSkew(Sequence cvs, String cn) {
		SCRec scRec = new SCRec();
		// ��ֵ��ƫ����ĵ�һ�����Ȱ�����ת��Ϊdouble���ͣ�
		// �Է�ֹ�������߳�����֮������ͻ�������ۼӳ���
		int len = cvs == null ? 0 : cvs.length();
		Double zero = Double.valueOf(0d);
		if (len < 1) {
			return scRec;
		}
		else {
			for (int i = 1; i <= len; i ++) {
				Object o = cvs.get(i);
				if (o instanceof Number) {
					cvs.set(i, ((Number) o).doubleValue());
				}
				else {
					//�Ѿ���ʼִ�о�ƫ�ˣ�����Ͳ�Ӧ�ô��ڿ�ֵ���߷���ֵ�ˣ���һ�У������0
					cvs.set(i, zero);
				}
			}
		}
		double skew = Skew.skew(cvs);

		NumStatis ns = new NumStatis(cvs);
		scRec.setNumStatis(ns);
		if (skew >= SC_MIN && skew <= SC_MAX ) {
			//2.13(b) �����ƫ������
			scRec.setMode(SCRec.MODE_ORI);
			return scRec;
		}
		
		Sequence pts1 = Pts.pts(cvs, ns.getMin(), 1);
		if (skew > SC_MAX) {
			//����log(transbase(x)).corskew�����ж�
			Sequence power = Pts.power(pts1, 0d);
			double corskew0 = Skew.skew(power, ns);
			if (corskew0 >= SC_MIN && corskew0 <= SC_MAX) { //step2
				//2.13(c) log�任
				scRec.setMode(SCRec.MODE_LOG);
				scRec.setP(0);
				// ���λ�þ�ƫ���ֵ���ж�ֵ��ͬ�ˣ�����transpose��ֱ����ln���㷨
				Pts.ptsSeq(cvs, 0d, 0d, true);
				//vi.setcorskewness1(corskew0);
			}
			else if (corskew0 < SC_MIN) { // step 6
				//2.13(d) ��(0,1)֮��Ѱ��p��ʹ��Vs.pts(p).corskewΪ0
				scRec.setMode(SCRec.MODE_POWER);
				power = Pts.power(pts1, 1d);
				double top = Skew.skew(power, ns);
				if ( top <= SC_MAX0 && top >= SC_MIN0) {
					scRec.setP(1);
					cvs.setMems(power.getMems());
					//vi.setcorskewness1(top);
				}
				else if (top > SC_MAX){
					double high = 1d;
					double low = 0d;
					double p = 0.5d;
					power = Pts.power(pts1, p);
					double fp = Skew.skew(power, ns);
					int ti = 0;
					boolean normal = true;
					double bottom = corskew0; 
					while ((fp > SC_MAX0 || fp < SC_MIN0) && normal) {
						if (fp > top || fp < bottom) {
							MessageManager mm = EngineMessage.get();
							Logger.warn(mm.getMessage("prep.nmnvError", cn, high, low, p, fp));
							normal = false;
						}
						else if (fp > SC_MAX0) {
							top = fp;
							high = p;
						}
						else if (fp < SC_MIN0) {
							bottom = fp;
							low = p;
						}
						p = (low + high) / 2;
						power = Pts.power(pts1, p);
						fp = Skew.skew(power, ns);
						if (ti ++ >= SC_MAXTRY) {
							MessageManager mm = EngineMessage.get();
							Logger.warn(mm.getMessage("prep.nmnvExceed", cn, ti));
							normal = false;
						}
					}
					if (normal) {
						//p = keep4(p);
						cvs.setMems(power.getMems());
						scRec.setP(p);
						//vi.setcorskewness1(fp);
					}
					else { // step 8, condition A1
						MessageManager mm = EngineMessage.get();
						Logger.warn(mm.getMessage("prep.nmnvSolution8", cn));
						//û�ҵ�p�����������Ϣ����rank����
						recSCRank(cvs, cn, scRec);
						//recSCRank(cvs, cn, scRec, filePath);
					}
				}
				else { // step 8, condition A2
					MessageManager mm = EngineMessage.get();
					Logger.warn(mm.getMessage("prep.nmnvReverse", cn, 0, corskew0, 1, top));
					Logger.warn(mm.getMessage("prep.nmnvSolution8", cn));
					//û�ҵ�p�����������Ϣ����rank����
					recSCRank(cvs, cn, scRec);
					//recSCRank(cvs, cn, scRec, filePath);
					//vi.setcorskewness1(0d);
				}
			}
			else { // step 3
				//2.13(g)������
				recSCRank(cvs, cn, scRec);
				//recSCRank(cvs, cn, scRec, filePath);
				//vi.setcorskewness1(0d);
			}
		}
		else { //corskew < Min
			//����transbase(x)^2.corskew�����ж�
			Sequence power = Pts.power(pts1, 2d);
			double corskew2 = Skew.skew(power, ns);
			if (corskew2 >= SC_MIN && corskew2 <= SC_MAX) { //step4
				//2.13(e) ƽ���任
				cvs.setMems(power.getMems());
				scRec.setMode(SCRec.MODE_SQUARE);
				scRec.setP(2);
				//vi.setcorskewness1(corskew2);
			}
			else if (corskew2 < SC_MIN) { // step 5
				//����sert����Ȼ�����ж�
				//step12(g)������
				recSCRank(cvs, cn, scRec);
				//recSCRank(cvs, cn, scRec, filePath);
				//vi.setcorskewness1(0d);
			}
			else { // step 7
				//step12(f) ��(0,1)֮��Ѱ��p��ʹ��Vs.pts(p).corskewΪ0
				scRec.setMode(SCRec.MODE_POWER);
				power = Pts.power(pts1, 1d);
				double bottom = Skew.skew(power, ns);
				if ( bottom >= SC_MIN0 && bottom <= SC_MAX0) {
					scRec.setP(1);
					cvs.setMems(power.getMems());
					//vi.setcorskewness1(bottom);
				}
				else if (bottom < SC_MIN){
					double high = 2d;
					double low = 1d;
					double p = 1.5d;
					power = Pts.power(pts1, p);
					double fp = Skew.skew(power, ns);
					int ti = 0;
					boolean normal = true;
					double top = corskew2; 
					while ((fp > SC_MAX0 || fp < SC_MIN0) && normal) {
						if (fp > top || fp < bottom) {
							MessageManager mm = EngineMessage.get();
							Logger.warn(mm.getMessage("prep.nmnvError", cn, high, low, p, fp));
							normal = false;
						}
						else if (fp > SC_MAX0) {
							top = fp;
							high = p;
						}
						else if (fp < SC_MIN0) {
							bottom = fp;
							low = p;
						}
						p = (low + high) / 2;
						power = Pts.power(pts1, p);
						fp = Skew.skew(power, ns);
						if (ti ++ >= SC_MAXTRY) {
							MessageManager mm = EngineMessage.get();
							Logger.warn(mm.getMessage("prep.nmnvExceed", cn, ti));
							normal = false;
						}
					}
					if (normal) {
						//p = keep4(p);
						cvs.setMems(power.getMems());
						scRec.setP(p);
						//vi.setcorskewness1(fp);
					}
					else { // step 8, condition B1
						MessageManager mm = EngineMessage.get();
						Logger.warn(mm.getMessage("prep.nmnvSolution8", cn));
						//û�ҵ�p�����������Ϣ����rank����
						recSCRank(cvs, cn, scRec);
						//recSCRank(cvs, cn, scRec, filePath);
						//vi.setcorskewness1(0d);
					}
				}
				else { // step 8, condition B2
					MessageManager mm = EngineMessage.get();
					Logger.warn(mm.getMessage("prep.nmnvReverse", cn, 2, corskew2, 1, bottom));
					Logger.warn(mm.getMessage("prep.nmnvSolution8", cn));
					//û�ҵ�p�����������Ϣ����rank����
					recSCRank(cvs, cn, scRec);
					//recSCRank(cvs, cn, scRec, filePath);
					//vi.setcorskewness1(0d);
				}
			}
		}
		scRec.setPrefix(cvs);
		return scRec;
	}
	
	protected static void corSkew(Sequence cvs, String cn, SCRec scRec) {
		NumStatis ns = scRec.getNumStatis();
		byte mode = scRec.getMode();
		if (mode == SCRec.MODE_ORI) {
		}
		else if (mode == SCRec.MODE_RANK) {
			//Rank
			Sequence V11 = scRec.getRankV();
			Sequence X11 = scRec.getRankX();
			/* ���������в�ʹ���ⲿ�ļ�
			String file = scRec.getRankFile();
			if (file != null) {
				FileObject fo = new FileObject(new File(pr.getPath(), file).getAbsolutePath());
				Table tab = null;
				try {
					tab = (Table) fo.importSeries("b");
				} catch (IOException e) {
					e.printStackTrace();
					throw new RQException(e);
				}
				Record rec = tab.getRecord(1);
				V11 = (Sequence) rec.getFieldValue(0);
				X11 = (Sequence) rec.getFieldValue(1);
			}
			*/
			for (int i = 1, n = cvs.length(); i <= n; i++ ) {
				Object o = cvs.get(i);
				int index = V11.pseg(o, null);
				if (index < 1){
					index = 1;
				}
				Object r = X11.get(index);
				cvs.set(i, r);
			}
		}
		else if (mode == SCRec.MODE_LOG || mode == SCRec.MODE_POWER || mode == SCRec.MODE_SQUARE) {
			//2:Ln, 4:Square, 6/7:Pow, ����Sert(Pts(p))
			double m = ns.getMin();
			double p = scRec.getP();
			try {
				Pts.ptsSeq(cvs, m, p, true);
			}
			catch (Exception e) {
				MessageManager mm = EngineMessage.get();
				Logger.error(mm.getMessage("prep.powerError", cn, p));
				for (int i = 0; i < cvs.length(); i ++) {
					if (cvs.get(i+1) == null) {
						Logger.error(mm.getMessage("prep.powerNull", (i+1)));
					}
				}
			}
		}
	}
	
	//copied from ColProcessor
	private static int NMNV_SIZE = 1000;
	private static double sqrt2 = Math.sqrt(2);
	private static void recSCRank(Sequence cvs, String cn, SCRec scRec) {
		Sequence X1 = cvs.ranks("s");
		int len0 = X1.length();
		// v2.5��������������ʱִ��������
		for (int i = 1 ; i <= len0; i++) {
			double r = ((Number) X1.get(i)).doubleValue();
			double x = (r - 0.5)/len0;
			X1.set(i, sqrt2 * Erf.erfInv(2*x - 1));
		}
		Sequence V11 = cvs.calc(new Expression("~*1.0"), new Context()).id(null);
		Sequence X11 = X1.id(null);
		int len = X11.length();
		if (len >= NMNV_SIZE) {
			// �����ļ���ʱ��ȥ���쳣�ַ�
			// edited by bd, 2022.4.7, �ں�������ʱ��֧�ִ洢Ϊ��¼�ļ��Ĳ��������ǽ���������洢��SCRec
			scRec.setRankRec(X11, V11, len);
			/*
			String V = String.valueOf(this.ci) + delString(cn) + "_nmnv.btx";
			FileObject fo = new FileObject(new File(path, V).getAbsolutePath());
			String[] fields = {"V", "X"};
			Table t = new Table(fields);
			Record rec = t.newLast();
			rec.set(0, V11);
			rec.set(1, X11);
			fo.exportSeries(t, "b", null);
			scRec.setRankFile(V, len);
			*/
		}
		else {
			scRec.setRankRec(X11, V11, len);
		}
		cvs.setMems(X1.getMems());
		scRec.setMode(SCRec.MODE_RANK);
	}
}
