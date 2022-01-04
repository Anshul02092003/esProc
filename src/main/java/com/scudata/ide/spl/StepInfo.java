package com.scudata.ide.spl;

import java.util.List;

import com.scudata.common.CellLocation;
import com.scudata.dm.Context;
import com.scudata.expression.fn.Call;
import com.scudata.expression.fn.Func.CallInfo;

/**
 * �������Ե���Ϣ��
 * 
 * ���������֣�һ���Ǵ򿪵ĸ�������һ���Ǳ�call��spl�� ��call��spl��û�д򿪵ģ�������Ҫ�ȴ򿪣��༭���ٱ��档
 *
 */
public class StepInfo {
	/**
	 * �ļ�·��
	 */
	public String filePath;
	/**
	 * ��˳���ҳ�б�
	 */
	public List<SheetSpl> sheets;
	/**
	 * ������
	 */
	public Context splCtx;
	/**
	 * ��ҳ�е�����
	 */
	public CellLocation parentLocation;
	/**
	 * funcʹ��
	 */
	public CallInfo callInfo;
	/**
	 * ������ʼ���������
	 */
	public CellLocation startLocation;
	/**
	 * �����Ľ�����
	 */
	public int endRow;
	/**
	 * callʹ��
	 */
	public Call parentCall;

	/**
	 * ���캯��
	 * 
	 * @param sheets ��˳���ҳ�б�
	 */
	public StepInfo(List<SheetSpl> sheets) {
		this.sheets = sheets;
	}

	/**
	 * �Ƿ�func��������
	 * 
	 * @return
	 */
	public boolean isFunc() {
		return callInfo != null;
	}

	/**
	 * �Ƿ�call��������
	 * 
	 * @return
	 */
	public boolean isCall() {
		return callInfo == null;
	}

}
