package com.scudata.ide.common.function;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import com.scudata.app.common.AppConsts;
import com.scudata.app.common.Section;
import com.scudata.common.Logger;
import com.scudata.common.Sentence;
import com.scudata.common.StringUtils;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.XMLFile;

/**
 * ��������
 *
 */
public class FuncManager {
	/**
	 * �ļ���
	 */
	private static String fileName = null;

	/**
	 * �����������
	 */
	private static FuncManager fm = null;

	/**
	 * ���������б�
	 */
	private ArrayList<FuncInfo> funcList;

	/**
	 * �����
	 */
	private final String ROOT = "funcs";

	/**
	 * ��ͨ���
	 */
	private final String NORMAL = "normal";

	/**
	 * �ļ�·��
	 */
	private static String relativeFile = null;

	/**
	 * Ĭ����·�������ļ�ʱֻ��
	 */
	private boolean readonly = false;

	/**
	 * �ļ�ǰ׺
	 */
	public static String filePrefix = "esProc";

	/**
	 * ȡ�����������
	 * 
	 * @return
	 */
	public static FuncManager getManager() {
		if (fm == null) {
			fm = new FuncManager();
		}
		return fm;
	}

	/**
	 * ˽�й��캯����ͨ��FuncManager.getManager()����
	 */
	private FuncManager() {
		funcList = new ArrayList<FuncInfo>();
		try {
			load(getFileName());
		} catch (Throwable x) {
			fileName = getRelativeFile();
			readonly = true;
			InputStream is = FuncManager.class.getResourceAsStream(getRelativeFile());
			if (is != null) {
				try {
					load(is);
				} catch (Throwable t) {
					Logger.debug(t);
				}
			}

		}
	}

	/**
	 * ȡ�ļ�·��
	 * 
	 * @return
	 */
	private static String getRelativeFile() {
		if (relativeFile == null) {
			String pre = filePrefix;
			String suf = GM.getLanguageSuffix();
			relativeFile = GC.PATH_CONFIG + "/" + pre + "Functions" + suf + "." + AppConsts.FILE_XML;
		}
		return relativeFile;
	}

	/**
	 * �Ƿ�ֻ��
	 * 
	 * @return
	 */
	public boolean readOnly() {
		return readonly;
	}

	/**
	 * ȡ�ļ���
	 * 
	 * @return
	 */
	public static String getFileName() {
		if (fileName == null) {
			fileName = GM.getAbsolutePath(getRelativeFile());
		}
		return fileName;
	}

	/**
	 * �����ļ�
	 * 
	 * @param fileName �ļ���
	 * @throws Throwable
	 */
	public void load(String fileName) throws Throwable {
		XMLFile xml = new XMLFile(fileName);
		load(xml);
	}

	/**
	 * �����ļ�
	 * 
	 * @param is �ļ�������
	 * @throws Throwable
	 */
	public void load(InputStream is) throws Throwable {
		XMLFile xml = new XMLFile(is);
		load(xml);
	}

	/**
	 * �����ļ�
	 * 
	 * @param xml XML�ļ�����
	 * @throws Throwable
	 */
	public void load(XMLFile xml) throws Throwable {
		funcList.clear();
		Section funcIDs = xml.listElement(ROOT + "/" + NORMAL);
		for (int i = 0; i < funcIDs.size(); i++) {
			String fID = funcIDs.get(i);
			String path = ROOT + "/" + NORMAL + "/" + fID + "/";
			FuncInfo fi = new FuncInfo();
			fi.setName(xml.getAttribute(path + "name"));
			fi.setDesc(xml.getAttribute(path + "desc"));
			try {
				fi.setPostfix(xml.getAttribute(path + "postfix"));
			} catch (Throwable e) {
			}
			String tmp = xml.getAttribute(path + "majortype");
			fi.setMajorType(Byte.parseByte(tmp));
			tmp = xml.getAttribute(path + "returntype");
			fi.setReturnType(Byte.parseByte(tmp));
			fi.setOptions(loadOptions(xml, path + "options"));
			fi.setParams(loadParams(xml, path + "params"));

			funcList.add(fi);
		}
	}

	/**
	 * ���غ���ѡ��
	 * 
	 * @param xml      XML�ļ�����
	 * @param rootPath �ڵ�·��
	 * @return
	 */
	ArrayList<FuncOption> loadOptions(XMLFile xml, String rootPath) {
		try {
			Section options = xml.listElement(rootPath);
			if (options.size() < 1) {
				return null;
			}
			ArrayList<FuncOption> al = new ArrayList<FuncOption>(options.size());
			for (int i = 0; i < options.size(); i++) {
				String opKey = options.get(i);
				FuncOption fo = new FuncOption();
				fo.setOptionChar(xml.getAttribute(rootPath + "/" + opKey + "/optionchar"));
				fo.setDescription(xml.getAttribute(rootPath + "/" + opKey + "/description"));
				String select = xml.getAttribute(rootPath + "/" + opKey + "/defaultselect");
				if (StringUtils.isValidString(select)) {
					fo.setDefaultSelect(Boolean.valueOf(select).booleanValue());
				}
				al.add(fo);
			}
			return al;
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * ���溯��ѡ��
	 * 
	 * @param xml      XML�ļ�����
	 * @param rootPath �ڵ�·��
	 * @param options  ����ѡ���б�
	 */
	void storeOptions(XMLFile xml, String rootPath, ArrayList<FuncOption> options) {
		try {
			if (options.size() < 1) {
				return;
			}
			for (int i = 0; i < options.size(); i++) {
				FuncOption fo = options.get(i);
				String opKey = "O" + Integer.toString(i + 1);
				xml.newElement(rootPath, opKey);
				String path = rootPath + "/" + opKey + "/";
				xml.setAttribute(path + "optionchar", fo.getOptionChar());
				xml.setAttribute(path + "description", removeTab(fo.getDescription()));
				xml.setAttribute(path + "defaultselect", fo.isDefaultSelect() + "");
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * ���غ�������
	 * 
	 * @param xml      XML�ļ�����
	 * @param rootPath �ڵ�·��
	 * @return
	 */
	ArrayList<FuncParam> loadParams(XMLFile xml, String rootPath) {
		try {
			Section params = xml.listElement(rootPath);
			if (params.size() < 1) {
				return null;
			}
			ArrayList<FuncParam> al = new ArrayList<FuncParam>(params.size());
			for (int i = 0; i < params.size(); i++) {
				String paraKey = params.get(i);
				FuncParam fp = new FuncParam();
				String path = rootPath + "/" + paraKey + "/";
				fp.setDesc(xml.getAttribute(path + "desc"));
				String tmp = xml.getAttribute(path + "presign");
				if (!StringUtils.isValidString(tmp)) {
					fp.setPreSign(' ');
				} else {
					fp.setPreSign(tmp.charAt(0));
				}
				tmp = xml.getAttribute(path + "subparam");
				if (StringUtils.isValidString(tmp)) {
					fp.setSubParam(Boolean.valueOf(tmp).booleanValue());
				}
				tmp = xml.getAttribute(path + "repeatable");
				if (StringUtils.isValidString(tmp)) {
					fp.setRepeatable(Boolean.valueOf(tmp).booleanValue());
				}
				tmp = xml.getAttribute(path + "identifieronly");
				if (StringUtils.isValidString(tmp)) {
					fp.setIdentifierOnly(Boolean.valueOf(tmp).booleanValue());
				}
				fp.setOptions(loadOptions(xml, path + "options"));
				tmp = xml.getAttribute(path + "filtertype");
				if (StringUtils.isValidString(tmp)) {
					fp.setFilterType(Byte.parseByte(tmp));
				}
				al.add(fp);
			}
			return al;
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * ���溯������
	 * 
	 * @param xml      XML�ļ�����
	 * @param rootPath ���·��
	 * @param params   ���������б�
	 */
	void storeParams(XMLFile xml, String rootPath, ArrayList<FuncParam> params) {
		try {
			if (params.size() < 1) {
				return;
			}
			for (int i = 0; i < params.size(); i++) {
				FuncParam fp = params.get(i);
				String pKey = "P" + Integer.toString(i + 1);
				xml.newElement(rootPath, pKey);
				String path = rootPath + "/" + pKey;

				xml.setAttribute(path + "/desc", removeTab(fp.getDesc()));
				xml.setAttribute(path + "/presign", fp.getPreSign() + "");
				xml.setAttribute(path + "/subparam", fp.isSubParam() + "");
				xml.setAttribute(path + "/repeatable", fp.isRepeatable() + "");
				xml.setAttribute(path + "/identifieronly", fp.isIdentifierOnly() + "");
				xml.setAttribute(path + "/valuestring", fp.getParamValue());

				xml.newElement(path, "options");
				storeOptions(xml, path + "/options", fp.getOptions());

				xml.setAttribute(path + "/filtertype", fp.getFilterType() + "");
			}
		} catch (Exception ex) {
		}
		return;
	}

	/**
	 * �����ȡ����
	 * 
	 * @param index ���
	 * @return
	 */
	public FuncInfo getFunc(int index) {
		return (FuncInfo) funcList.get(index);
	}

	/**
	 * ��������ȡ�����б�����ͬ��������
	 * 
	 * @param funcName
	 * @return
	 */
	public ArrayList<FuncInfo> getFunc(String funcName) {
		ArrayList<FuncInfo> al = null;
		for (int i = 0; i < funcList.size(); i++) {
			FuncInfo fi = (FuncInfo) funcList.get(i);
			if (fi.getName().equalsIgnoreCase(funcName)) {
				if (al == null) {
					al = new ArrayList<FuncInfo>();
				}
				al.add(fi);
			}
		}
		return al;
	}

	/**
	 * ȡ����������
	 * 
	 * @return
	 */
	public int size() {
		return funcList.size();
	}

	/**
	 * �������б�
	 */
	public void clear() {
		funcList.clear();
	}

	public void addFunc(FuncInfo fi) {
		funcList.add(fi);
	}

	/**
	 * ��\t�滻Ϊ�ո�
	 * 
	 * @param str
	 * @return
	 */
	private String removeTab(String str) {
		if (str == null) {
			return str;
		}
		return Sentence.replace(str, "\t", "        ", 0);
	}

	/**
	 * ����
	 * 
	 * @return
	 */
	public boolean save() {
		try {
			if (ConfigOptions.bAutoBackup.booleanValue()) {
				// ����ǰ����ԭ���ļ�
				String backName = fileName + ".bak";
				File fb = new File(backName);
				fb.deleteOnExit();
				File old = new File(fileName);
				if (old.exists()) {
					old.renameTo(fb);
				}
			}

			XMLFile xml = XMLFile.newXML(fileName, ROOT);
			xml.newElement(ROOT, NORMAL);
			for (int i = 0; i < funcList.size(); i++) {
				FuncInfo fi = getFunc(i);
				String fID = "F" + Integer.toString(i + 1);
				xml.newElement(ROOT + "/" + NORMAL, fID);
				String path = ROOT + "/" + NORMAL + "/" + fID;

				xml.setAttribute(path + "/name", fi.getName());
				xml.setAttribute(path + "/desc", removeTab(fi.getDesc()));
				xml.setAttribute(path + "/postfix", removeTab(fi.getPostfix()));
				xml.setAttribute(path + "/majortype", String.valueOf(fi.getMajorType()));
				xml.setAttribute(path + "/returntype", String.valueOf(fi.getReturnType()));

				xml.newElement(path, "options");
				storeOptions(xml, path + "/options", fi.getOptions());

				xml.newElement(path, "params");
				storeParams(xml, path + "/params", fi.getParams());
			}
			xml.save();
		} catch (Throwable t) {
			GM.showException(t);
			return false;
		}
		return true;
	}
}
