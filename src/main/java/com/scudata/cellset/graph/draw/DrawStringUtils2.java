package com.scudata.cellset.graph.draw;

import java.util.*;

import com.scudata.common.ArgumentTokenizer;

import java.awt.FontMetrics;
import java.lang.Character;

/**
 * Ϊ�˲������ϲ��࣬����ȫ�̸���StringUtils2����DrawBase�����ݱ�ʹ��
 * @author Joancy
 *
 */
public class DrawStringUtils2 {
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * �ж�s�Ƿ�Ϊ�մ�
	 * @param s �ַ���
	 * @return ����Ƿ���true�����򷵻�false
	 */
	public final static boolean isSpaceString(String s) {
		if (s == null) {
			return true;
		}
		for (int i = 0, len = s.length(); i < len; i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ��һ���������е�ָ���ֽ���ת����16���ƴ�
	 * 
	 * @param l
	 *            ������
	 * @param byteNum
	 *            �������еĵ��ֽ���(������������ֽڸ���)
	 */
	public final static String toHexString(long l, int byteNum) {
		StringBuffer sb = new StringBuffer(16);
		appendHexString(sb, l, byteNum);
		return sb.toString();
	}

	/**
	 * ��һ���������е�ָ���ֽ���ת����16���ƴ��������ӵ��ַ�����������
	 * 
	 * @param sb
	 *            �ַ���������
	 * @param l
	 *            ������
	 * @param byteNum
	 *            �������еĵ��ֽ���(������������ֽڸ���)
	 */
	public final static void appendHexString(StringBuffer sb, long l,
			int byteNum) {
		for (int i = byteNum * 2 - 1; i >= 0; i--) {
			long x = (l >> (i * 4)) & 0xf;
			sb.append(hexDigits[(int) x]);
		}
	}

	/**
	 * ���ַ�����unicode�ַ�ת��Ϊ&#92;uxxxx��ʽ������'\\','\t','\n','\r','\f'����
	 * ��������specialChars���κ��ַ���ǰ��\
	 * 
	 * @params s ��Ҫ������ַ���
	 * @params sb ׷�Ӵ������Ļ�����
	 * @params specialChars ��Ҫ��ǰ��\���ر��ַ���
	 * @return ��sb!=null�򷵻�sb�����򷵻�׷���˴���������StringBuffer
	 */
	public final static StringBuffer deunicode(String s, StringBuffer sb,
			String specialChars) {
		int len = s.length();
		if (sb == null) {
			sb = new StringBuffer(len * 2);

		}
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '\\':
				sb.append('\\').append('\\');
				break;
			case '\t':
				sb.append('\\').append('t');
				break;
			case '\n':
				sb.append('\\').append('n');
				break;
			case '\r':
				sb.append('\\').append('r');
				break;
			case '\f':
				sb.append('\\').append('f');
				break;
			default:
				if ((ch < 0x0020) || (ch > 0x007e)) {
					sb.append('\\').append('u');
					sb.append(hexDigits[(ch >> 12) & 0xF]);
					sb.append(hexDigits[(ch >> 8) & 0xF]);
					sb.append(hexDigits[(ch >> 4) & 0xF]);
					sb.append(hexDigits[ch & 0xF]);
				} else {
					if (specialChars != null && specialChars.indexOf(ch) != -1) {
						sb.append('\\');
					}
					sb.append(ch);
				}
			}
		}
		return sb;
	}

	public final static StringBuffer deunicode(String s, StringBuffer sb) {
		return deunicode(s, sb, null);
	}

	public final static String deunicode(String s, String specialChars) {
		return deunicode(s, null, specialChars).toString();
	}

	public final static String deunicode(String s) {
		return deunicode(s, null, null).toString();
	}

	/**
	 * ���ÿ��ǲ�ո񣬽������ı����տ��w�ض�Ϊ�����ı�
	 * @param text �ı���
	 * @param fm ������Ϣ
	 * @param w ���
	 * @return �ضϺ�Ķ����ı�
	 */
	public static ArrayList<String> wrapString2(String text, FontMetrics fm, float w) {
		ArrayList<String> al = wrapString(text, fm, w, false);
		if (al == null) {
			al = wrapString(text, fm, w, true);
		}
		return al;
	}

	/**
	 * ����ָ���������ı��ض�Ϊ�����ı�
	 * @param text �ı�
	 * @param fm ����
	 * @param w ���
	 * @param wrapChar �ܷ�ضϵ���
	 * @param align ���뷽ʽ
	 * @return �ضϺ�Ķ����ı�
	 */
	public static ArrayList<String> wrapString(String text, FontMetrics fm, float w,
			boolean wrapChar, byte align) {
		ArrayList<String> al = null;
		if (wrapChar == false) {
			al = wrapString(text, fm, w, false);
		}
		if (al == null) {
			al = wrapString(text, fm, w, true);
		}

		return al;
	}

	private static String rightTrim(String str) {
		while (str.lastIndexOf(" ") == str.length() - 1) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}


	/**
	 * �����з��İ��տ�ȷ�ɢ����
	 * @param al �����ı�
	 * @param fm ������Ϣ
	 * @param w ���
	 */
	public static void scatter(ArrayList al, FontMetrics fm, float w) {
		String line = rightTrim(String.valueOf((String) al.get(al.size() - 1)));
		String newLine = scatterLine(line,fm,w);
		al.set(al.size() - 1, newLine);
	}
	
	/**
	 * ��һ���ı���ɢ����
	 * @param src Դ�ı���
	 * @param fm ������Ϣ
	 * @param w ���
	 * @return �ÿո��ɢ�������˺���ַ���
	 */
	public static String scatterLine(String src, FontMetrics fm, float w) {
		String line = src;
		if (fm.stringWidth(line) >= w) return src;
			String leftSpace = ""; // ��¼��ͷ�Ŀո����
			while (line.indexOf(" ") == 0) {
				line = line.substring(1, line.length());
				leftSpace += " ";
			}

			int numLongspace = 0; // �ַ����ּ���
			int widthChars = 0; // �ַ����ܿ��
			StringBuffer sb = new StringBuffer();
			StringBuffer sbchars = new StringBuffer();
			char[] cline = line.toCharArray();
			boolean flag = true;
			for (int i = 0; i < cline.length; i++) {
				char c = cline[i];
				if (flag && Character.isSpaceChar(c)) {
					flag = false;
					numLongspace++;
				}
				if (!Character.isSpaceChar(c)) {
					flag = true;
					sbchars.append(c);
				}
				sb.append(c);
			}
			widthChars = fm.stringWidth(sbchars.toString());
			boolean iss = (widthChars + numLongspace * 2) < (int) (w / 2);

			int index = 0;
			flag = true;
			boolean isInsert = false;
			while (true) { // c
				if (fm.stringWidth(leftSpace + sb.toString() + " ") > w) {
					break;
				}

				char[] nchars = sb.toString().toCharArray();
				char c = nchars[index];
				if (flag && Character.isSpaceChar(c)) {
					flag = false;
					sb.insert(index + 1, ' ');
					index++;
				}
				if (!Character.isSpaceChar(c)) {
					flag = true;
					if (numLongspace == 0 || iss) {
						sb.insert(index + 1, ' ');
						index++;
					}
				}

				index++;
				if (index > sb.length() - 2) {
					index = 0;
				}
			}
			return leftSpace + sb.toString();

	}

/**
 * ��Դ�������ո�
 * @param src Դ�ַ���
 * @param fm ������Ϣ
 * @param w ���
 * @return �������ַ���
 */
	public static String fitSpaces(String src, FontMetrics fm, float w) {
		String line = rightTrim(src);
		if (fm.stringWidth(line) >= w)
			return src;
		String myLine = String.valueOf(line);
		int scount = 0; // ��Ҫ����Ŀո���
		while (true) {
			myLine += " ";
			if (fm.stringWidth(myLine) > w) {
				break;
			}
			scount++;
		}
		if (scount <= 0)
			return src;
		char[] cline = line.toCharArray();
		int len = cline.length;
		StringBuffer sb = new StringBuffer();
		for (int c = 0; c < len; c++) {
			sb.append(cline[c]);
		}
		int index = 0;
		boolean flag = false; // �ǿո�ʼ�ı�־
		boolean haspace = false; // �������Ƿ���ڿո�
		while (true) {
			char c = sb.charAt(index);
			if (!Character.isSpaceChar(c)) {
				flag = true;
			}
			if (flag && index < sb.length() - 1) {
				char c1 = sb.charAt(index + 1);
				// �ո�ͷǿո���ӿո�
				if (Character.isSpaceChar(c) && !Character.isSpaceChar(c1)) {
					haspace = true;
					sb.insert(index, ' ');
					index++; // ����ո���ǰ��һ��
					scount--;
					if (scount == 0) {
						break; // �ո��������˳�
					}
				}
			}
			index++;
			if (index > sb.length() - 1) {
				if (!flag) {
					break; // �ַ����в����ڷǿո��˳�
				}
				if (!haspace) {
					break; // �ַ����м��в����ڿո��˳�
				}
				index = 0; // �ַ�����β��������¼���
			}
		}
		return sb.toString();
	}

	/**
	 * ����Զ����к����γɵ��ı��м��� 
	 */
	public static ArrayList wrapString(String text, FontMetrics fm, float w) {
		return wrapString(text, fm, w, false);
	}

	/** ����Զ����к����γɵ��ı��м��� */
	public static ArrayList<String> wrapString(String text, FontMetrics fm, float w,
			boolean wrapChar) {
		ArrayList<String> al = new ArrayList<String>();
		text = replace(text, "\\n", "\n");
		text = replace(text, "\\r", "\r");
		text = replace(text, "\r\n", "\n");
		text = replace(text, "\r", "\n");
		// ������ʹ��Argumenttokenizer���µĹ�������
		ArgumentTokenizer at = new ArgumentTokenizer(text, '\n', true, true,
				true, true);
		while (at.hasNext()) {
			String line = at.next();
			if (at.hasNext()) {
				line += "\n";
			}
			int len = line.length();
			String tmp = "";
			for (int i = 0; i < len; i++) {
				char c = line.charAt(i);
				tmp += String.valueOf(c);
				if (fm.stringWidth(tmp) > w) {
					int cut = cutLine(tmp, c, wrapChar);
					al.add(tmp.substring(0, cut));
					tmp = tmp.substring(cut);
				}
			}
			al.add(tmp);
		}
		return al;
	}

	private static int cutLine(String s, char c, boolean wrapChar) {
		// �����ǰ���г���len��֪���ǾͲ�ȥ���㣬������㵱ǰ����ʱ�����ַ���
		int len = s.length() - 1;
		if (wrapChar) {
			return len;
		}

		// ���β�ַ�c��֪���ǾͲ���ȥ���㣬�������β�ַ�
		if (c == 0) {
			c = s.charAt(len);
		}
		boolean canBeHead = canBeHead(c);
		boolean isEnglishChar = isEnglishChar(c);
		if (!canBeHead && isEnglishChar) {
			// ����������Ӣ���ַ���Ҫ��Ϊһ�����ʹ�ͬ���У�������Ҫ�ж��Ƿ���ȫ��Ϊ����Ӣ���ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = canBeHead(c);
			boolean letterbreak = false;
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!isEnglishChar(seekChar)) {
					letterbreak = true;
					if (!hasHead) {
						if (canBeHead(seekChar)) {
							// ��������г��ַǱ����ַ����ų����֣�����ô�趨������Ϊtrue
							hasHead = true;
						}
						seek--;
					} else {
						// ��������г��ַ�Ӣ���ַ�����ô�жϸ��ַ��Ƿ��Ǳ�β�ַ�
						if (canBeFoot(seekChar)) {
							// ����ǷǱ�β�ַ�����ô������ַ������м���
							loc = seek + 1;
						} else {
							if (canBeHead(seekChar)) {
								hasHead = true;
							} else {
								hasHead = false;
							}
							seek--;
						}
					}
				} else if (letterbreak) {
					// ������ֱ�β�ַ�֮�����ҵ�Ӣ���ַ�����ô�Ӹ�Ӣ���ַ���Ͽ��ͺ�
					loc = seek + 1;
				} else {
					if (canBeHead(seekChar)) {
						hasHead = true;
					} else {
						hasHead = false;
					}
					seek--;
				}
			}
			if (loc > 0) {
				// ����������з�Ӣ���ַ�
				return loc;
			} else {
				// ���������ȫ����Ӣ���ַ������������ı�β�ַ�����ô�����з�
				return len;
			}
		} else if (!canBeHead) {
			// c�Ǳ����ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = false;
			// �ҵ���һ���Ǳ����ַ�
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!hasHead) {
					if (canBeHead(seekChar)) {
						// ��������г��ַǱ����ַ����ų����֣�����ô�趨������Ϊtrue
						hasHead = true;
					}
					seek--;
				} else {
					if (isEnglishChar(seekChar)) {
						// ����Ӣ���ַ�����ǰ��ѯ����һ��������Ӣ���ַ�Ϊֹ
						int eseek = seek;
						boolean eng = true;
						while (eng && seek > 0) {
							seek--;
							eng = isEnglishChar(s.charAt(seek));
						}
						// added by bdl, 2011.8.12, ����ӵ�ǰ�ַ�ֱ����һ������������Ӣ���ַ���
						// ��ô�����һ��Ӣ���ַ�ǰ����
						if (seek == 0) {
							loc = eseek + 1;
						}
					}
					// ����Ѿ������֣���ô�ж��Ƿ�Ҫ��β
					else if (canBeFoot(seekChar)) {
						// �������β����ô�ڸ��ַ�����м���
						loc = seek + 1;
					} else {
						// ��Ҫ��β
						seek--;
					}
				}
			}
			if (loc > 0) {
				// ��������п���������
				return loc;
			} else {
				// ���������ȫ���Ǳ��׻����������ı�β�ַ�����ô�����з�
				return len;
			}
		}
		// Ȼ�����ж�c�Ƿ���Ӣ���ַ�
		else if (isEnglishChar) {
			// ����������Ӣ���ַ���Ҫ��Ϊһ�����ʹ�ͬ���У�������Ҫ�ж��Ƿ���ȫ��Ϊ����Ӣ���ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = canBeHead(c);
			boolean letterbreak = false;
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!isEnglishChar(seekChar)) {
					// edited by bdl, 20111.5.17, ����ʱ�����жϵ�ǰ���ַ��Ƿ����
					letterbreak = true;
					if (!hasHead) {
						if (canBeHead(seekChar)) {
							hasHead = true;
						}
						seek--;
					}
					// ��������г��ַ�Ӣ���ַ�����ô�жϸ��ַ��Ƿ��Ǳ�β�ַ�
					else if (canBeFoot(seekChar)) {
						// ����ǷǱ�β�ַ�����ô������ַ������м���
						loc = seek + 1;
					} else {
						if (canBeHead(seekChar)) {
							hasHead = true;
						} else {
							hasHead = false;
						}
						seek--;
					}
				} else if (letterbreak) {
					// ������ֱ�β�ַ�֮�����ҵ�Ӣ���ַ�����ô�Ӹ�Ӣ���ַ���Ͽ��ͺ�
					loc = seek + 1;
				} else {
					if (canBeHead(seekChar)) {
						hasHead = true;
					} else {
						hasHead = false;
					}
					seek--;
				}
			}
			if (loc > 0) {
				// ����������з�Ӣ���ַ�
				return loc;
			} else {
				// ���������ȫ����Ӣ���ַ������������ı�β�ַ�����ô�����з�
				return len;
			}
		}
		return seekCanBeFoot(s.substring(0, len), len);
	}

	//���ַ���s�������һ�������ڱ�����ĩ���ַ��ĺ���Ͽ������ضϿ������ַ���
	private static int seekCanBeFoot(String s, int len) {
		// �����ǰ���г���len��֪���ǾͲ�ȥ���㣬������㵱ǰ����ʱ�����ַ���
		if (len == -1) {
			len = s.length();
		}
		if (len <= 1) {
			return len;
		}

		int seek = len - 1;
		int loc = 0;
		while (seek >= 0 && loc == 0) {
			char seekChar = s.charAt(seek);
			if (canBeFoot(seekChar)) {
				loc = seek + 1;
			} else {
				seek--;
			}
		}
		if (loc > 0) {
			return loc;
		}
		// ���s�������ַ����Ǳ�β�ַ�����ô���б���
		return len;
	}

	//�ж�ĳ�ַ�ͨ��������Ƿ�����Ϊ��β
	private static boolean canBeFoot(char c) {
		// ���е�ʱ��Ӧ�ÿ��ǵı�β�ַ�
		String cannotFoot = "([{�������������������������ۣ��꣤";
		return cannotFoot.indexOf(c) < 0;
	}

	//�ж�ĳ�ַ�ͨ��������Ƿ�����Ϊ����
	private static boolean canBeHead(char c) {
		// ���е�ʱ��Ӧ�ÿ��ǵı����ַ�, edited by bdl, 2011.5.17����Ӱٷֺű���
		String cannotHead = "%��!),.:;?]}���������D���������á����������������������������������������ݣ��������";
		return cannotHead.indexOf(c) < 0;
	}

	private static boolean isEnglishChar(char c) {
		//����ĸ�����ж��У����С����
		return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9') || c == '.' || c == '��' || c == '%' || c == '��');
	}

	/**
	 * ��&#92;uxxxxת��Ϊunicode�ַ�������'\\','\t','\n','\r','\f'���д���
	 * 
	 * @params s ��Ҫ������ַ���
	 * @params sb ׷�Ӵ������Ļ�����
	 * @return ��sb!=null�򷵻�sb�����򷵻�׷���˴���������StringBuffer
	 */
	public final static StringBuffer unicode(String s, StringBuffer sb) {
		int len = s.length();
		if (sb == null) {
			sb = new StringBuffer(len);

		}
		char ch;
		for (int i = 0; i < len;) {
			ch = s.charAt(i++);
			if (ch != '\\') {
				sb.append(ch);
				continue;
			}
			ch = s.charAt(i++);
			if (ch == 'u') {
				// Read the xxxx
				int value = 0;
				for (int j = 0; j < 4; j++) {
					ch = s.charAt(i++);
					switch (ch) {
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						value = (value << 4) + ch - '0';
						break;
					case 'a':
					case 'b':
					case 'c':
					case 'd':
					case 'e':
					case 'f':
						value = (value << 4) + 10 + ch - 'a';
						break;
					case 'A':
					case 'B':
					case 'C':
					case 'D':
					case 'E':
					case 'F':
						value = (value << 4) + 10 + ch - 'A';
						break;
					default:
						throw new IllegalArgumentException("���Ϸ���\\uxxxx����");
					} // switch(ch)
				} // for(int j)
				sb.append((char) value);
			} else {
				switch (ch) {
				case 't':
					ch = '\t';
					break;
				case 'r':
					ch = '\r';
					break;
				case 'n':
					ch = '\n';
					break;
				case 'f':
					ch = '\f';
					break;
				}
				sb.append(ch);
			}
		} // for(int i)
		return sb;
	}

	/**
	 * ���ַ���ת��Ϊunicode��
	 * @param s Դ��
	 * @return unicode��
	 */
	public final static String unicode(String s) {
		return unicode(s, null).toString();
	}

	/**
	 * ���ַ���ת��Ϊunicode��
	 * @param theString Դ��
	 * @return unicode��
	 */
	public final static String unicode2String(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				} // if(aChar)
			} else {
				outBuffer.append(aChar);
			}
		} // for(int x)
		return outBuffer.toString();
	}

	final static char[] c1Digit = { '��', 'Ҽ', '��', '��', '��', '��', '½', '��',
			'��', '��' };
	final static char[] c2Digit = { '��', 'һ', '��', '��', '��', '��', '��', '��',
			'��', '��' };
	final static char[] c1Unit = { 'ʰ', '��', 'Ǫ' };
	final static char[] c2Unit = { 'ʮ', '��', 'ǧ' };
	final static String[] chinaUnit = { "��", "��", "����" };

	private final static StringBuffer toRMB2(long l, char[] cDigit, char[] cUnit) {
		long ml = l;
		int unit = 0, bit = 0, d;
		boolean hasZero = false, sf = false;
		StringBuffer sb = new StringBuffer(64);
		while (l > 0) {
			if (bit == 4) {
				if (unit > 2) {
					throw new IllegalArgumentException("��д��֧�ִ���һ�����ڵ���");
				}

				if (sf) {
					if (hasZero || l % 10 == 0) {
						sb.append(cDigit[0]);
						hasZero = false;
					}
				} else {
					int len = sb.length();
					if (len > 0) {
						sb.deleteCharAt(len - 1);
					}
				}
				sb.append(chinaUnit[unit]);
				unit++;
				bit = 0;
				sf = false;
			}

			d = (int) (l % 10);
			if (d > 0) {
				sf = true;
				if (hasZero) {
					sb.append(cDigit[0]);
					hasZero = false;
				}
				if (bit != 0) {
					sb.append(cUnit[bit - 1]);
				}
				//С��100����cUnitΪʮcDigitΪһʱ��һ������
				if (bit == 1 && d == 1 && ml < 100) {
				} else {
					sb.append(cDigit[d]);
				}
			} else {
				if (sf) { // ����β����0����
					hasZero = true;
				}
			}

			bit++;
			l /= 10;
		}
		return sb.reverse();
	}

	/**
	 * ����������ʽ������Ҵ�д��ʽ
	 * 
	 * @param money
	 *            ������
	 * @return ��ʽ�����ַ���
	 * @exception IllegalArgumentException
	 *                ��money<0��money>=һ������ʱ
	 */
	public final static String toRMB(double money) {
		char[] cDigit = c1Digit, cUnit = c1Unit;
		StringBuffer sb = new StringBuffer(64);
		if (money < 0) {
			sb.append("��");
			money = -money;
		}
		long yuan = (long) money; // Ԫ

		if (yuan == 0) {
			sb.append("��");
		} else {
			sb.append(toRMB2(yuan, cDigit, cUnit));
		}
		sb.append('Ԫ');

		int jaoFeng = (int) ((money + 0.001 - (long) money) * 100) % 100;
		int jao = jaoFeng / 10;
		int feng = jaoFeng % 10;
		if (jao > 0) {
			sb.append(cDigit[jao]);
			sb.append('��');
		}
		if (feng > 0) {
			if (jao == 0) {
				sb.append('��');
			}
			sb.append(cDigit[feng]);
			sb.append('��');
		} else {
			sb.append('��');
		}
		return sb.toString();
	}

	/**
	 * ������ת��Ϊ����д��
	 * @param l ��ֵ
	 * @param abbreviate ��д
	 * @param uppercase ��д
	 * @return
	 */
	public final static String toChinese(long l, boolean abbreviate,
			boolean uppercase) {

		String fu = "";
		if (l == 0) {
			return "��";
		} else if (l < 0) {
			fu = "��";
			l = -l;
		}
		char[] cDigit = uppercase ? c1Digit : c2Digit;
		char[] cUnit = uppercase ? c1Unit : c2Unit;
		if (abbreviate) {
			return fu + toRMB2(l, cDigit, cUnit).toString();
		} else {
			StringBuffer sb = new StringBuffer(64);
			for (; l > 0; l /= 10) {
				int digit = (int) l % 10;
				sb.append(cDigit[digit]);
			}
			sb = sb.reverse();
			return fu + sb.toString();
		} // if ( abbreviate )
	}

	private final static boolean matches(String value, int pos1, String fmt,
			int pos2, boolean ignoreCase) {
		if (value == null || fmt == null) {
			return false;
		}
		int len1 = value.length(), len2 = fmt.length();
		while (pos2 < len2) {
			char ch = fmt.charAt(pos2++);
			if (ch == '*') {
				if (pos1 == len1) {
					while (pos2 < len2) {
						if (fmt.charAt(pos2++) != '*') {
							return false;
						}
					}
					return true;
				}
				do {
					if (matches(value, pos1, fmt, pos2, ignoreCase)) {
						return true;
					}
				} while (pos1++ < len1);

				return false;
			}

			if (ch == '?') {
				if (pos1 == len1) {
					return false;
				}
			} else if (ch == '\\' && pos2 < len2 && fmt.charAt(pos2) == '*') {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (pos1 == len1 || value.charAt(pos1) != '*') {
					return false;
				}

				pos2++;
			} else {
				if (ignoreCase) {
					if (pos1 == len1
							|| Character.toUpperCase(ch) != Character
									.toUpperCase(value.charAt(pos1))) {
						return false;
					}
				} else {
					if (pos1 == len1 || ch != value.charAt(pos1)) {
						return false;
					}
				}
			}
			pos1++;
		}
		return pos1 == len1;
	}

	/**
	 * �ж��ַ����Ƿ����ָ���ĸ�ʽ
	 * 
	 * @param value
	 *            �ַ���
	 * @param fmt
	 *            ��ʽ��(*��ʾ0�������ַ���?��ʾ�����ַ�)
	 * @param ifcase
	 *            �Ƿ��Сд
	 * @return ��value��fmtΪnullʱ����false������ƥ��ʱҲ����false�����򷵻�true
	 */
	public final static boolean matches(String value, String fmt, boolean ifcase) {
		return matches(value, 0, fmt, 0, ifcase);
	}

	private final static String[] provinces = { null, null, null, null, null,
			null, null, null, null, null, null, "����", "���", "�ӱ�", "ɽ��", "���ɹ�",
			null, null, null, null, null, "����", "����", "������", null, null, null,
			null, null, null, null, "�Ϻ�", "����", "�㽭", "��΢", "����", "����", "ɽ��",
			null, null, null, "����", "����", "����", "�㶫", "����", "����", null, null,
			null, "����", "�Ĵ�", "����", "����", "����", null, null, null, null, null,
			null, "����", "����", "�ຣ", "����", "�½�", null, null, null, null, null,
			"̨��", null, null, null, null, null, null, null, null, null, "���",
			"����", null, null, null, null, null, null, null, null, "����" };

	private final static int[] wi = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10,
			5, 8, 4, 2, 1 };
	private final static char[] codes = { '1', '0', 'X', '9', '8', '7', '6',
			'5', '4', '3', '2' };

	/**
	 * ����GB11643-1999<<������ݺ���>>��GB11643-1989<<��ᱣ�Ϻ���>>�涨������֤���Ƿ���Ϲ淶
	 */
	public final static boolean identify(String ident) {
		if (ident == null) {
			return false;
		}

		int len = ident.length();
		if (len != 15 && len != 18) {
			return false;
		}

		for (int i = 0; i < ((len == 15) ? 15 : 17); i++) {
			char ch = ident.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}

		// ��黧�������ص����������� GB/T2260
		int p = (ident.charAt(0) - '0') * 10 + (ident.charAt(1) - '0');
		if (p >= provinces.length || provinces[p] == null) {
			return false;
		}

		// ������������ GB/T7408
		int year = 0, month = 0, day = 0;
		if (len == 15) {
			year = 1900 + (ident.charAt(6) - '0') * 10
					+ (ident.charAt(7) - '0');
			month = (ident.charAt(8) - '0') * 10 + (ident.charAt(9) - '0');
			day = (ident.charAt(10) - '0') * 10 + (ident.charAt(11) - '0');
		} else {
			year = (ident.charAt(6) - '0') * 1000 + (ident.charAt(7) - '0')
					* 100 + (ident.charAt(8) - '0') * 10
					+ (ident.charAt(9) - '0');
			month = (ident.charAt(10) - '0') * 10 + (ident.charAt(11) - '0');
			day = (ident.charAt(12) - '0') * 10 + (ident.charAt(13) - '0');
		}
		if (month == 2) {
			if ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) {
				// ����2��29��
				if (day > 29) {
					return false;
				}
			} else {
				if (day > 28) {
					return false;
				}
			}
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			if (day > 30) {
				return false;
			}
		} else if (month <= 12) {
			if (day > 31) {
				return false;
			}
		} else {
			return false;
		}

		// ���У����
		if (len == 18) {
			int[] w = wi;
			int mod = 0;
			for (int i = 0; i < 17; i++) {
				mod += (ident.charAt(i) - '0') * w[i];
			}
			mod = mod % 11;
			if (ident.charAt(17) != codes[mod]) {
				return false;
			}
		}
		return true;
	}
/**
 * ȫ�滻�ַ���
 * @param src Դ��
 * @param findString ���Ҵ�
 * @param replaceString �滻��
 * @return �滻��ɺ�Ĵ�
 */
	public static String replace(String src, String findString,
			String replaceString) {
		if (src == null) {
			return src;
		}
		int len = src.length();
		if (len == 0) {
			return src;
		}
		if (findString == null) {
			return src;
		}
		int len1 = findString.length();
		if (len1 == 0) {
			return src;
		}
		if (replaceString == null) {
			return src;
		}

		int start = 0;
		StringBuffer sb = null;
		while (true) {
			int pos = src.indexOf(findString, start);
			if (pos >= 0) {
				if (sb == null) {
					sb = new StringBuffer(len + 100);
				}
				for (int i = start; i < pos; i++) {
					sb.append(src.charAt(i));
				}
				sb.append(replaceString);
				start = pos + len1;
			} else {
				if (sb != null) {
					for (int i = start; i < len; i++) {
						sb.append(src.charAt(i));
					}
				}
				break;
			}
		}
		if (sb != null) {
			return sb.toString();
		}
		return src;
	}

	private static String[] excelLabels = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	private static String toExcel(int index) {
		if (index < 26) {
			return excelLabels[index];
		}

		int shang = index / 26;
		int yu = index % 26;

		return toExcel(shang - 1) + excelLabels[yu];
	}

	/**
	 * ������ת��ΪExcel���б�ǩ
	 * 
	 * @param index
	 *            int,Ҫת��������,��1��ʼ,��1��ΪA
	 * @return String,ת������б�ǩ,ע��Excel��ǩ����26����,26���Ƶ�Z��Ӧ��ΪBA;��Excel��ǩZ��ΪAA
	 */
	public static String toExcelLabel(int index) {
		return toExcel(index - 1);
	}


	/**
	 * ȥ���ַ����еĿհ��ַ�
	 * @param s Դ��
	 * @return ȥ���ո�Ĵ�
	 */
	public static String trimWhitespace(String s) {
		if (s == null) {
			return null;
		}
		int st = 0, len = s.length();
		while (st < len && Character.isWhitespace(s.charAt(st))) {
			st++;
		}
		while (st < len && Character.isWhitespace(s.charAt(len - 1))) {
			len--;
		}
		return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;

	}

	/**
	 * ��ȡ��ǰ������ı��߶�
	 * @param fm ������Ϣ
	 * @return �ı��߶�
	 */
	public static int getTextRowHeight(FontMetrics fm) {
		int tmpH = (int) Math.ceil(fm.getFont().getSize() * 1.28); // ���ֵ��и߹���߶�
		int textH = fm.getHeight(); // ��������߶�
		if (tmpH < textH) {
			return textH;
		}
		int dh = tmpH - textH;
		if (dh % 2 == 0) { // Ϊ�˱�֤��������߶����Ǿ������иߣ���֤dh����Ϊż��
			return tmpH;
		}
		return tmpH + 1;
	}
	
	
}
