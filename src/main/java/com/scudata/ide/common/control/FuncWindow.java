package com.scudata.ide.common.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import com.scudata.cellset.ICellSet;
import com.scudata.common.IntArrayList;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.function.EditingFuncInfo;
import com.scudata.ide.common.function.EditingFuncParam;
import com.scudata.ide.common.function.FuncInfo;
import com.scudata.ide.common.function.FuncManager;
import com.scudata.ide.common.function.FuncOption;
import com.scudata.ide.common.function.FuncParam;
import com.scudata.ide.common.function.IParamTreeNode;
import com.scudata.ide.common.function.ParamUtil;
import com.scudata.ide.common.swing.FreeConstraints;
import com.scudata.ide.common.swing.FreeLayout;
import com.scudata.ide.common.swing.JListEx;

/**
 * ��������
 *
 */
public class FuncWindow extends JWindow {
	private static final long serialVersionUID = 1L;

	/**
	 * �����
	 */
	private JPanel panelMain = new JPanel();

	/**
	 * �����б�ؼ�
	 */
	private JListEx listFunc = new JListEx();

	/**
	 * ���������ı���
	 */
	private JTextPane textDesc = new JTextPane();

	/**
	 * �������
	 */
	private ICellSet cellSet;

	/**
	 * ����������
	 */
	private FuncManager funcManager = FuncManager.getManager();

	/**
	 * �Ƿ����
	 */
	private boolean isEnabled;

	/**
	 * �Ƿ���ִֹ�ж���
	 */
	private boolean preventChange = false;

	/**
	 * �����б�Ĺ������ؼ�
	 */
	private JScrollPane jSPFunc = new JScrollPane(listFunc);

	/**
	 * �����ı���Ĺ������ؼ�
	 */
	private JScrollPane jSPDesc = new JScrollPane(textDesc);

	/**
	 * ������Ϣ�Աȵ�ӳ���
	 */
	private HashMap<FuncInfo, FuncInfo> matchedMap = new HashMap<FuncInfo, FuncInfo>();

	/**
	 * ���ڱ༭�ĺ�����Ϣ
	 */
	private EditingFuncInfo efi;

	/**
	 * �����Ƿ�������ʾ
	 */
	private boolean isDisplay = false;

	/**
	 * ��һ�εĺ���ѡ��
	 */
	private String oldEfo;

	/**
	 * ��һ�εĺ�����
	 */
	private String oldFuncName;

	/**
	 * �������б��е����,��0��ʼ
	 */
	private int funcIndex = -1;

	/**
	 * ���캯��
	 */
	public FuncWindow() {
		super(GV.appFrame);
		panelMain.setLayout(new FreeLayout());
		panelMain.setBorder(BorderFactory.createLineBorder(Color.black));
		textDesc.setContentType("text/html");
		textDesc.setFont(GC.font);
		getContentPane().add(panelMain);
		setBounds(-100, -100, 5, 5);
		textDesc.setEditable(false);

		listFunc.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JEditorPane jtf = new JEditorPane();
				String exp = (value == null) ? "" : value.toString();
				jtf.setContentType("text/html");
				jtf.setFont(GC.font);
				jtf.setText(exp);
				if (isSelected) {
					jtf.setBackground(list.getSelectionBackground());
					jtf.setForeground(list.getSelectionForeground());
				} else {
					jtf.setBackground(list.getBackground());
					jtf.setForeground(list.getForeground());
				}
				jtf.setEnabled(list.isEnabled());
				jtf.setFont(list.getFont());
				jtf.setBorder(null);
				return jtf;
			}
		});
	}

	/**
	 * ���������Ƿ����
	 * 
	 * @return
	 */
	public boolean isFuncEnabled() {
		return isEnabled;
	}

	/**
	 * ���ú��������Ƿ����
	 * 
	 * @param enabled
	 */
	public void setFuncEnabled(boolean enabled) {
		this.isEnabled = enabled;
		resetOldFunc();
	}

	/**
	 * ���������Ƿ�������ʾ
	 * 
	 * @return
	 */
	public boolean isDisplay() {
		return isEnabled && isDisplay;
	}

	/**
	 * ���õ�ǰ�������
	 * 
	 * @param cellSet
	 */
	public void setCellSet(ICellSet cellSet) {
		this.cellSet = cellSet;
	}

	/**
	 * X���꣬Y���꣬���
	 */
	private int x, y, w;

	/**
	 * ����X���꣬Y���꣬���
	 * 
	 * @param x
	 *            X����
	 * @param y
	 *            Y����
	 * @param w
	 *            ���
	 */
	public void setPosition(int x, int y, int w) {
		this.x = x;
		this.y = y;
		this.w = w;
	}

	/**
	 * ��ȡ�б�ؼ��ĸ߶�
	 * 
	 * @return
	 */
	private int getListHeight() {
		FontMetrics fm = new JTextField().getFontMetrics(GC.font);
		int h = (int) (listFunc.data.getSize() * (fm.getHeight() + 4));
		return h;
	}

	/**
	 * ��ȡ���������ı���ĸ߶�
	 * 
	 * @param fi
	 * @return
	 */
	private int getDescHeight(FuncInfo fi) {
		String text = GM.getFuncDesc(fi, null, null, -1);
		FontMetrics fm = textDesc.getFontMetrics(textDesc.getStyledDocument()
				.getFont(textDesc.getCharacterAttributes()));
		IntArrayList list = new IntArrayList();
		GM.transTips(text, list, fm, w - 6);
		int height = list.getInt(1);
		int lineCount = height / fm.getHeight();
		return 22 + lineCount * (fm.getHeight() + 6);
	}

	/**
	 * ��ȡ�����ܸ߶�
	 * 
	 * @param fi
	 * @return
	 */
	private int getTotalHeight(FuncInfo fi) {
		return getListHeight() + getDescHeight(fi) + 14;
	}

	/**
	 * ������һ�εĺ�����Ϣ
	 */
	private void resetOldFunc() {
		oldFuncName = null;
		funcIndex = -1;
	}

	/**
	 * ���ش���
	 */
	public void hideWindow() {
		invalidate();
		setBounds(-100, -100, 5, 5);
		Thread t = new Thread() {
			public void run() {
				validate();
				repaint();
			}
		};
		SwingUtilities.invokeLater(t);
		isDisplay = false;
	}

	/**
	 * ��ʾ��һ������
	 * 
	 * @param textExp
	 */
	public void showNextFunc(JTextComponent textExp) {
		if (listFunc.isSelectionEmpty()) {
			return;
		}
		if (!isFuncEnabled()) {
			return;
		}
		int index = listFunc.getSelectedIndex();
		if (index == listFunc.data.size() - 1) {
			index = 0;
		} else {
			index++;
		}
		listFunc.setSelectedIndex(index);
		showFunc(index, textExp);
	}

	/**
	 * ��ʾ����
	 * 
	 * @param index
	 *            ���
	 * @param textExp
	 */
	private void showFunc(int index, JTextComponent textExp) {
		funcIndex = index;
		Object disp = listFunc.data.getElementAt(index);
		Object code = listFunc.x_getCodeItem((String) disp);
		FuncInfo fi = (FuncInfo) code;
		FuncInfo matchedFi = matchedMap.get(fi);
		ActiveParamConfig apc = getActiveParamConfig(matchedFi, efi, textExp);
		FuncParam activeParam = null;
		int paramCaret = -1;
		if (apc != null) {
			activeParam = apc.activeParam;
			paramCaret = apc.paramCaret;
		}
		String efo = getActiveFuncOption(efi, textExp);
		textDesc.setText(GM.getFuncDesc(fi, efo, activeParam, paramCaret));
		resetUI(fi);
	}

	/**
	 * ����ƶ�
	 * 
	 * @param textExp
	 * @param ctx
	 */
	public void caretPositionChanged(final JTextComponent textExp, Context ctx) {
		caretPositionChanged(textExp, ctx, false);
	}

	/**
	 * ����ƶ�
	 * 
	 * @param textExp
	 * @param ctx
	 * @param resizeFuncWin
	 */
	public void caretPositionChanged(final JTextComponent textExp, Context ctx,
			boolean resizeFuncWin) {
		if (!isEnabled) {
			return;
		}
		ListSelectionListener[] listeners = listFunc
				.getListSelectionListeners();
		if (listeners != null) {
			int len = listeners.length;
			for (int i = len - 1; i >= 0; i--) {
				listFunc.removeListSelectionListener(listeners[i]);
			}
		}
		listFunc.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (preventChange) {
					return;
				}
				if (listFunc.isSelectionEmpty()) {
					return;
				}
				if (!isFuncEnabled()) {
					return;
				}
				int index = listFunc.getSelectedIndex();
				showFunc(index, textExp);
			}
		});

		matchedMap.clear();
		efi = ParamUtil.getEditingFunc(textExp, cellSet, ctx);
		if (efi == null) {
			hideWindow();
			return;
		}
		FuncInfo fi;
		String name = efi.getFuncName();
		ArrayList<FuncInfo> funcs = funcManager.getFunc(name);
		if (funcs == null) {
			hideWindow();
			return;
		}
		ArrayList<FuncInfo> filterFuncs = new ArrayList<FuncInfo>();
		for (int i = 0; i < funcs.size(); i++) {
			fi = funcs.get(i);
			if (efi.getMajorType() == fi.getMajorType()
					|| efi.getMajorType() == Expression.TYPE_UNKNOWN
					|| fi.getMajorType() == Expression.TYPE_UNKNOWN) {
				filterFuncs.add(fi);
			}
		}
		funcs = filterFuncs;
		if (funcs.isEmpty()) {
			hideWindow();
			return;
		}
		if (oldFuncName == null || !oldFuncName.equals(name)) {
			resetOldFunc();
		}
		oldFuncName = name;
		for (int i = 0; i < funcs.size(); i++) {
			fi = null;
			try {
				fi = ParamUtil.matchFuncInfoParams((FuncInfo) funcs.get(i), efi
						.getFuncParam().getParamString());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			if (fi != null) {
				matchedMap.put(funcs.get(i), fi);
			}
		}

		int size = matchedMap.size();
		if (size > 1) {
			ArrayList<FuncInfo> optFuncs = new ArrayList<FuncInfo>();
			Iterator<FuncInfo> it = matchedMap.keySet().iterator();
			while (it.hasNext()) {
				FuncInfo key = it.next();
				fi = matchedMap.get(key);
				if (!compareFuncOption(fi, efi)) {
					optFuncs.add(key);
				}
			}
			if (size - optFuncs.size() >= 1) {
				for (int i = 0; i < optFuncs.size(); i++) {
					matchedMap.remove(optFuncs.get(i));
				}
			}
		}

		setFuncInfo(funcs, efi, textExp, matchedMap, resizeFuncWin);
	}

	/**
	 * ���ú�����Ϣ
	 * 
	 * @param fList
	 *            �����б�
	 * @param efi
	 *            ���ڱ༭�ĺ�����Ϣ
	 * @param textExp
	 * @param matchedMap
	 * @param resizeFuncWin
	 */
	private void setFuncInfo(ArrayList<FuncInfo> fList, EditingFuncInfo efi,
			JTextComponent textExp, HashMap<FuncInfo, FuncInfo> matchedMap,
			boolean resizeFuncWin) {
		preventChange = true;
		ActiveParamConfig apc;
		FuncInfo fi;
		FuncInfo matchedFi;
		Vector<FuncInfo> codes = new Vector<FuncInfo>();
		Vector<String> disps = new Vector<String>();

		String efo = getActiveFuncOption(efi, textExp);
		int listIndex = funcIndex;
		String newDesc = null;
		for (int i = 0; i < fList.size(); i++) {
			fi = (FuncInfo) fList.get(i);
			matchedFi = (FuncInfo) matchedMap.get(fi);
			apc = getActiveParamConfig(matchedFi, efi, textExp);
			FuncParam activeParam = null;
			int paramCaret = -1;
			if (apc != null) {
				activeParam = apc.activeParam;
				paramCaret = apc.paramCaret;
				if (listIndex == -1) {
					listIndex = i;
					newDesc = GM.getFuncDesc(fi, efo, activeParam, paramCaret);
				} else if (funcIndex == i) {
					newDesc = GM.getFuncDesc(fi, efo, activeParam, paramCaret);
				}
			}
			codes.add(fi);
			disps.add(getFuncString(fi, efo, activeParam, paramCaret));
		}

		if (newDesc == null) {
			if (listIndex < 0) {
				listIndex = 0;
			}
			newDesc = GM.getFuncDesc((FuncInfo) fList.get(listIndex), efo,
					null, -1);
		}

		if (!resizeFuncWin && isDisplay) {
			int size = listFunc.data.size();
			if (size == codes.size()) {
				boolean sameFunc = true;
				Object code, disp;
				for (int i = 0; i < size; i++) {
					disp = listFunc.data.getElementAt(i);
					code = listFunc.x_getCodeItem((String) disp);
					if (!code.equals(codes.get(i))
							|| !disp.equals(disps.get(i))) {
						sameFunc = false;
						break;
					}
				}
				if (sameFunc && listIndex == listFunc.getSelectedIndex()) {
					// ��ȫһ���ĺ����Ͳ��仯��
					if (oldEfo == null && efo == null) {
						return;
					} else if (oldEfo.equals(efo)) {
						return;
					}
				}
			}
		}
		oldEfo = efo;
		try {
			textDesc.setText(newDesc);
		} catch (Throwable ex) {
		}
		listFunc.x_setData(codes, disps);
		listFunc.setSelectedIndex(listIndex);
		resetUI((FuncInfo) fList.get(listIndex));
		isDisplay = true;
		preventChange = false;
	}

	/**
	 * ȡ��ǰ�ĺ���ѡ��
	 * 
	 * @param efi
	 * @param textExp
	 * @return
	 */
	private String getActiveFuncOption(EditingFuncInfo efi,
			JTextComponent textExp) {
		String efo = efi.getFuncOption();
		if (StringUtils.isValidString(efo)) {
			int fos = efi.getFuncOptionStart();
			int oPos = textExp.getCaretPosition() - fos - 1;
			if (oPos < efo.length() && oPos > -1) {
				char foChar = efo.charAt(oPos);
				return foChar + "";
			}
		}
		return null;
	}

	/**
	 * ȡ��ǰ�ĺ�����������
	 * 
	 * @param matchedFi
	 * @param efi
	 * @param textExp
	 * @return
	 */
	private ActiveParamConfig getActiveParamConfig(FuncInfo matchedFi,
			EditingFuncInfo efi, JTextComponent textExp) {
		if (matchedFi == null || efi == null) {
			return null;
		}
		EditingFuncParam efp = efi.getFuncParam();
		if (efp == null) {
			return null;
		}
		int paramStart = efi.getFuncParamStart();
		int caretPos = textExp.getCaretPosition();
		int paramPos = caretPos - paramStart;
		String paramString = efp.getParamString();
		ArrayList<FuncParam> params = matchedFi.getParams();
		FuncParam fp;
		if (!StringUtils.isValidString(efp.getParamString()) || paramPos < 0) {
			return new ActiveParamConfig();
		}
		if (params != null && params.size() > 0) {
			if (paramPos == 0) {
				fp = (FuncParam) params.get(0);
				return new ActiveParamConfig(fp, 0);
			}
			int preSize = 0;
			int start, end;
			for (int j = 0; j < params.size(); j++) {
				fp = (FuncParam) params.get(j);
				if (!StringUtils.isValidString(fp.getParamValue())) {
					if (!StringUtils.isValidString(paramString)) {
						return new ActiveParamConfig(fp, paramPos - preSize);
					}
					if (paramString.startsWith(fp.getPreSign() + "")) {
						paramString = paramString.substring(1);
						preSize += 1;
						if (!StringUtils.isValidString(paramString)) {
							return new ActiveParamConfig(fp, paramPos - preSize);
						}
					}
					continue;
				}
				start = paramString.indexOf(fp.getParamValue());
				end = start + fp.getParamValue().length();
				if ((start + preSize <= paramPos && end + preSize >= paramPos)
						|| (j == params.size() - 1)) {
					return new ActiveParamConfig(fp, paramPos - start - preSize);
				} else {
					paramString = paramString.substring(end);
					preSize += end;
					if (!StringUtils.isValidString(paramString)) {
						return new ActiveParamConfig(fp, paramPos - preSize);
					}
				}
			}
		}
		return null;
	}

	/**
	 * ��ǰ�ĺ�����������
	 *
	 */
	class ActiveParamConfig {
		FuncParam activeParam;

		int paramCaret;

		public ActiveParamConfig() {
		}

		public ActiveParamConfig(FuncParam activeParam, int paramCaret) {
			this.activeParam = activeParam;
			this.paramCaret = paramCaret;
		}
	}

	/**
	 * ����UI
	 * 
	 * @param fi
	 */
	private void resetUI(FuncInfo fi) {
		int h = getListHeight() + 2;
		panelMain.add(jSPFunc, new FreeConstraints(3, 3, w - 6, h));
		panelMain.add(jSPDesc, new FreeConstraints(3, 5 + h, w - 6,
				getDescHeight(fi) + 2));
		invalidate();
		setBounds(x, y, w, getTotalHeight(fi));
		validate();
		repaint();
	}

	/**
	 * ��������ϢתΪ�ַ���
	 * 
	 * @param fi
	 * @param efo
	 * @param activeParam
	 * @param paramCaret
	 * @return
	 */
	private String getFuncString(FuncInfo fi, String efo,
			FuncParam activeParam, int paramCaret) {
		ArrayList<FuncParam> params = fi.getParams();
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isValidString(fi.getPostfix())) {
			sb.append(fi.getPostfix());
		}
		sb.append(fi.getName());
		ArrayList<FuncOption> options = fi.getOptions();
		FuncOption fo;
		String optChar;
		if (options != null && options.size() > 0) {
			sb.append("@");
			for (int i = 0; i < options.size(); i++) {
				fo = options.get(i);
				optChar = fo.getOptionChar();
				if (StringUtils.isValidString(efo)) {
					if (efo.indexOf(optChar) > -1) {
						optChar = "<b>" + optChar + "</b>"; // ��ǰѡ��Ӵ�
					}
				}
				sb.append(optChar);
			}
		}
		sb.append("(");
		if (params != null && params.size() != 0) {
			FuncParam fp, fpNext;
			String name;
			char preSign;
			boolean hasSub;
			StringBuffer option;
			boolean isActiveParam;
			String paramValue;
			int optPos;
			char activeOptChar;
			for (int i = 0; i < params.size(); i++) {
				fp = (FuncParam) params.get(i);
				isActiveParam = activeParam != null
						&& fp.getDesc().equals(activeParam.getDesc());
				name = getFuncParamName(fp);
				activeOptChar = '@';
				if (isActiveParam) {
					name = "<b>" + name + "</b>"; // ��ǰ�����Ӵ�
					paramValue = activeParam.getParamValue();
					optPos = paramValue.indexOf("@");
					if (optPos > -1 && paramCaret > optPos) { // ����ڲ���ѡ����
						activeOptChar = paramValue.charAt(paramCaret - 1);
					}
				}
				preSign = fp.getPreSign();
				option = new StringBuffer();
				options = fp.getOptions();
				if (fp.isIdentifierOnly() && options != null
						&& options.size() > 0) {
					option.append("@");
					for (int j = 0; j < options.size(); j++) {
						fo = (FuncOption) options.get(j);
						optChar = fo.getOptionChar();
						if (optChar.equals(activeOptChar + "")) {
							optChar = "<b>" + optChar + "</b>"; // ��ǰ����ѡ��Ӵ�
						}
						option.append(optChar);
					}
				}
				if (fp.isRepeatable()) {
					if (i != 0) {
						sb.append(preSign);
					}
					hasSub = false;
					if (i < params.size() - 1) {
						fpNext = (FuncParam) params.get(i + 1);
						if (fpNext.getPreSign() == IParamTreeNode.COLON) { // ,x:y,...
							hasSub = true;
							sb.append(name);
							if (option.length() > 0) {
								sb.append(option);
							}
							sb.append(fpNext.getPreSign());
							isActiveParam = activeParam != null
									&& fpNext.getDesc().equals(
											activeParam.getDesc());
							name = getFuncParamName(fpNext);
							if (isActiveParam) {
								name = "<b>" + name + "</b>";
							}
							sb.append(name);
							sb.append(preSign);
							sb.append("...");
							i++;
							continue;
						}
					}
					if (!hasSub) { // ,x,...
						sb.append(name);
						if (option.length() > 0) {
							sb.append(option);
						}
						sb.append(preSign);
						sb.append("...");
					}
				} else {
					if (i != 0) {
						sb.append(preSign);
					}
					sb.append(name);
					if (option.length() > 0) {
						sb.append(option);
					}
				}
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * ȡ�����Ĳ�������
	 * 
	 * @param fp
	 * @return
	 */
	private String getFuncParamName(FuncParam fp) {
		String name = fp.getDesc();
		int index = name.indexOf("(");
		if (index > 0) {
			name = name.substring(0, index);
		}
		return name;
	}

	/**
	 * �ȽϺ���ѡ��
	 * 
	 * @param fi
	 *            ������Ϣ
	 * @param efi
	 *            ���ڱ༭�ĺ�����Ϣ
	 * @return
	 */
	private boolean compareFuncOption(FuncInfo fi, EditingFuncInfo efi) {
		String opts = efi.getFuncOption();
		ArrayList<FuncOption> options = fi.getOptions();
		Vector<String> allOpts = new Vector<String>();
		if (options != null) {
			for (int i = 0; i < options.size(); i++) {
				FuncOption fo = (FuncOption) options.get(i);
				allOpts.add(String.valueOf(fo.getOptionChar()));
			}
		}
		for (int i = 0; i < opts.length(); i++) {
			if (!allOpts.contains(opts.substring(i, i + 1))) {
				return false;
			}
		}
		return true;
	}
}
