package com.scudata.dm;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.scudata.cellset.datamodel.PgmCellSet;

/**
 * dfx���������
 */
public class DfxManager {
	private static DfxManager dfxManager = new DfxManager();
	private HashMap<String, SoftReference<PgmCellSet>> dfxRefMap = 
		new HashMap<String, SoftReference<PgmCellSet>>();

	private DfxManager() {}

	/**
	 * ȡdfx���������ʵ��
	 * @return DfxManager
	 */
	public static DfxManager getInstance() {
		return dfxManager;
	}

	/**
	 * �������ĳ�����
	 */
	public void clear() {
		synchronized(dfxRefMap) {
			dfxRefMap.clear();
		}
	}
	
	/**
	 * ʹ����dfx���������������
	 * @param dfx PgmCellSet
	 */
	public void putDfx(PgmCellSet dfx) {
		Context dfxCtx = dfx.getContext();
		dfxCtx.setParent(null);
		dfxCtx.setJobSpace(null);
		dfx.reset();

		synchronized(dfxRefMap) {
			dfxRefMap.put(dfx.getName(), new SoftReference<PgmCellSet>(dfx));
		}
	}

	/**
	 * �ӻ����������ȡdfx��ʹ�������Ҫ����putDfx�������������
	 * @param name dfx�ļ���
	 * @param ctx ����������
	 * @return PgmCellSet
	 */
	public PgmCellSet removeDfx(String name, Context ctx) {
		PgmCellSet dfx = null;
		synchronized(dfxRefMap) {
			SoftReference<PgmCellSet> sr = dfxRefMap.remove(name);
			if (sr != null) dfx = (PgmCellSet)sr.get();
		}

		if (dfx == null) {
			return readDfx(name, ctx);
		} else {
			// ���ٹ���ctx�еı���
			Context dfxCtx = dfx.getContext();
			dfxCtx.setEnv(ctx);
			return dfx;
		}
	}

	/**
	 * �ӻ����������ȡdfx��ʹ�������Ҫ����putDfx�������������
	 * @param fo dfx�ļ�����
	 * @param ctx ����������
	 * @return PgmCellSet
	 */
	public PgmCellSet removeDfx(FileObject fo, Context ctx) {
		PgmCellSet dfx = null;
		String name = fo.getFileName();
		synchronized(dfxRefMap) {
			SoftReference<PgmCellSet> sr = dfxRefMap.remove(name);
			if (sr != null) dfx = (PgmCellSet)sr.get();
		}
		
		if (dfx == null) {
			return readDfx(fo, ctx);
		} else {
			// ���ٹ���ctx�еı���
			Context dfxCtx = dfx.getContext();
			dfxCtx.setEnv(ctx);
			return dfx;
		}
	}
	
	/**
	 * ��ȡdfx������ʹ�û���
	 * @param fo dfx�ļ�����
	 * @param ctx ����������
	 * @return PgmCellSet
	 */
	public PgmCellSet readDfx(FileObject fo, Context ctx) {
		PgmCellSet dfx = fo.readPgmCellSet();
		dfx.setName(fo.getFileName());
		dfx.resetParam();
		
		// ���ٹ���ctx�еı���
		Context dfxCtx = dfx.getContext();
		dfxCtx.setEnv(ctx);
		return dfx;
	}
	
	/**
	 * ��ȡdfx������ʹ�û���
	 * @param name dfx�ļ���
	 * @param ctx ����������
	 * @return PgmCellSet
	 */
	public PgmCellSet readDfx(String name, Context ctx) {
		return readDfx(new FileObject(name, null, "s", ctx), ctx);
	}
}
