package com.scudata.ide.common.function;

import java.util.ArrayList;

import javax.swing.text.JTextComponent;

import com.scudata.common.StringUtils;
import com.scudata.expression.Expression;

/**
 * ���ڱ༭�ĺ�����Ϣ
 *
 */
public class EditingFuncInfo {
	/**
	 * ��������
	 */
	private String funcName;
	/**
	 * ����ѡ��
	 */
	private String funcOption;
	/**
	 * ���ڱ༭�ĺ�������
	 */
	private EditingFuncParam funcParam;
	/**
	 * ����������
	 */
	private byte majorType = Expression.TYPE_UNKNOWN;
	/**
	 * ������ֵ
	 */
	private Object majorValue;
	/**
	 * ������Ϣ
	 */
	private FuncInfo funcInfo;
	/**
	 * �༭��
	 */
	private JTextComponent editor;
	/**
	 * �ɱ��ʽ
	 */
	private String oldExp;
	/**
	 * ������ʼ�㡢������
	 */
	private int funcStart, funcEnd;

	/**
	 * ���캯��
	 * 
	 * @param editor
	 *            �༭��
	 * @param funcName
	 *            ������
	 * @param funcOption
	 *            ����ѡ��
	 * @param funcParam
	 *            ���ڱ༭�ĺ�������
	 * @param funcStart
	 *            ������ʼ��
	 * @param funcEnd
	 *            ����������
	 */
	public EditingFuncInfo(JTextComponent editor, String funcName,
			String funcOption, EditingFuncParam funcParam, int funcStart,
			int funcEnd) {
		this.editor = editor;
		oldExp = editor.getText();
		this.funcName = funcName;
		this.funcOption = funcOption;
		this.funcParam = funcParam;
		this.funcStart = funcStart;
		this.funcEnd = funcEnd;
	}

	/**
	 * ���ú�����
	 * 
	 * @param funcName
	 */
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	/**
	 * ȡ������
	 * 
	 * @return
	 */
	public String getFuncName() {
		return funcName;
	}

	/**
	 * ���ú���ѡ��
	 * 
	 * @param funcOption
	 */
	public void setFuncOption(String funcOption) {
		this.funcOption = funcOption;
		refreshEditor();
	}

	/**
	 * ȡ����ѡ��
	 * 
	 * @return
	 */
	public String getFuncOption() {
		return funcOption;
	}

	/**
	 * �������ڱ༭�ĺ�������
	 * 
	 * @param funcParam
	 */
	public void setFuncParam(EditingFuncParam funcParam) {
		this.funcParam = funcParam;
		refreshEditor();
	}

	/**
	 * ȡ���ڱ༭�ĺ�������
	 * 
	 * @return
	 */
	public EditingFuncParam getFuncParam() {
		return funcParam;
	}

	/**
	 * ��������������
	 * 
	 * @param type
	 */
	public void setMajorType(byte type) {
		this.majorType = type;
	}

	/**
	 * ȡ����������
	 * 
	 * @return
	 */
	public byte getMajorType() {
		return majorType;
	}

	/**
	 * ����������ֵ
	 * 
	 * @param value
	 */
	public void setMajorValue(Object value) {
		this.majorValue = value;
	}

	/**
	 * ȡ������ֵ
	 * 
	 * @return
	 */
	public Object getMajorValue() {
		return majorValue;
	}

	/**
	 * ���ú�����Ϣ
	 * 
	 * @param funcInfo
	 */
	public void setFuncInfo(FuncInfo funcInfo) {
		this.funcInfo = funcInfo;
	}

	/**
	 * ȡ������Ϣ
	 * 
	 * @return
	 */
	public FuncInfo getFuncInfo() {
		return funcInfo;
	}

	/**
	 * ȡ������ʼ��
	 * 
	 * @return
	 */
	public int getFuncStart() {
		return funcStart;
	}

	/**
	 * ȡ����ѡ����ʼ��
	 * 
	 * @return
	 */
	public int getFuncOptionStart() {
		return funcStart + funcName.length() + 1;
	}

	/**
	 * ȡ����������ʼ��
	 * 
	 * @return
	 */
	public int getFuncParamStart() {
		return funcStart + getPreFuncString().length() + 1;
	}

	/**
	 * ȡ��������ǰ���ַ���
	 * 
	 * @return
	 */
	private StringBuffer getPreFuncString() {
		StringBuffer sb = new StringBuffer();
		sb.append(funcName);
		if (StringUtils.isValidString(funcOption)) {
			sb.append("@");
			sb.append(funcOption);
		}
		return sb;
	}

	/**
	 * ת�ַ���
	 */
	public String toString() {
		StringBuffer sb = getPreFuncString();
		if (StringUtils.isValidString(funcParam)) {
			sb.append("(");
			sb.append(funcParam);
			sb.append(")");
		} else {
			sb.append("()");
		}

		return sb.toString();
	}

	/**
	 * ׷�ӱ༭���ı�
	 * 
	 * @param container
	 */
	public void appendEditingText(ArrayList<EditingText> container) {
		container.add(new EditingText(getPreFuncString().toString()));
		if (funcParam != null) {
			container.add(new EditingText("(", EditingText.STYLE_HIGHLIGHT));
			funcParam.appendEditingText(container);
			container.add(new EditingText(")", EditingText.STYLE_HIGHLIGHT));
		} else {
			container.add(new EditingText("()"));
		}
	}

	/**
	 * ˢ�±༭��
	 */
	public void refreshEditor() {
		int caretPos = editor.getCaretPosition();
		int ss = editor.getSelectionStart();
		int se = editor.getSelectionEnd();

		ArrayList<EditingText> container = new ArrayList<EditingText>();
		container.add(new EditingText(oldExp.substring(0, funcStart)));
		appendEditingText(container);
		container.add(new EditingText(oldExp.substring(funcEnd)));

		editor.setCaretPosition(caretPos);
		editor.setSelectionStart(ss);
		editor.setSelectionEnd(se);
	}

}
